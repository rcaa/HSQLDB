/* Copyright (c) 2001-2010, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb;

import org.hsqldb.lib.DoubleIntIndex;
import org.hsqldb.persist.CachedObject;
import org.hsqldb.persist.PersistentStore;

/**
 * Manages rows involved in transactions
 *
 * @author Fred Toussi (fredt@users dot sourceforge.net)
 * @version 2.0.1
 * @since 2.0.0
 */
public class TransactionManager2PL extends TransactionManagerCommon
implements TransactionManager {

    public TransactionManager2PL(Database db) {

        database       = db;
        lobSession     = database.sessionManager.getSysLobSession();
        txModel        = LOCKS;
    }

    public long getGlobalChangeTimestamp() {
        return globalChangeTimestamp.get();
    }

    public boolean isMVRows() {
        return false;
    }

    public boolean isMVCC() {
        return false;
    }

    public int getTransactionControl() {
        return LOCKS;
    }

    public void setTransactionControl(Session session, int mode) {

        writeLock.lock();

        try {
            switch (mode) {

                case MVCC : {
                    TransactionManagerMVCC manager =
                        new TransactionManagerMVCC(database);

                    manager.globalChangeTimestamp.set(
                        globalChangeTimestamp.get());
                    manager.liveTransactionTimestamps.addLast(
                        session.transactionTimestamp);

                    database.txManager = manager;

                    break;
                }
                case MVLOCKS : {
                    TransactionManagerMV2PL manager =
                        new TransactionManagerMV2PL(database);

                    manager.globalChangeTimestamp.set(
                        globalChangeTimestamp.get());
                    manager.liveTransactionTimestamps.addLast(
                        session.transactionTimestamp);

                    database.txManager = manager;

                    break;
                }
                case LOCKS :
                    break;
            }

            return;
        } finally {
            writeLock.unlock();
        }
    }

    public void completeActions(Session session) {
        endActionTPL(session);
    }

    public boolean prepareCommitActions(Session session) {

        session.actionTimestamp = nextChangeTimestamp();

        return true;
    }

    public boolean commitTransaction(Session session) {

        if (session.abortTransaction) {
            return false;
        }

        int      limit = session.rowActionList.size();
        Object[] list  = session.rowActionList.getArray();

        writeLock.lock();

        try {
            // new actionTimestamp used for commitTimestamp
            session.actionTimestamp = nextChangeTimestamp();
            session.transactionEndTimestamp = session.actionTimestamp;

            endTransaction(session);

            for (int i = 0; i < limit; i++) {
                RowAction action = (RowAction) list[i];

                action.commit(session);
            }

            persistCommit(session, list, limit);
            endTransactionTPL(session);
        } finally {
            writeLock.unlock();
        }

        session.tempSet.clear();

        return true;
    }

    public void rollback(Session session) {

        session.abortTransaction = false;
        session.actionTimestamp  = nextChangeTimestamp();
        session.transactionEndTimestamp = session.actionTimestamp;

        rollbackPartial(session, 0, session.transactionTimestamp);
        endTransaction(session);
        writeLock.lock();

        try {
            endTransactionTPL(session);
        } finally {
            writeLock.unlock();
        }
    }

    public void rollbackSavepoint(Session session, int index) {

        long timestamp = session.sessionContext.savepointTimestamps.get(index);
        Integer oi = (Integer) session.sessionContext.savepoints.get(index);
        int     start  = oi.intValue();

        while (session.sessionContext.savepoints.size() > index + 1) {
            session.sessionContext.savepoints.remove(
                session.sessionContext.savepoints.size() - 1);
            session.sessionContext.savepointTimestamps.removeLast();
        }

        rollbackPartial(session, start, timestamp);
    }

    public void rollbackAction(Session session) {
        rollbackPartial(session, session.actionIndex, session.actionTimestamp);
        endActionTPL(session);
    }

    /**
     * rollback the row actions from start index in list and
     * the given timestamp
     */
    void rollbackPartial(Session session, int start, long timestamp) {

        Object[] list  = session.rowActionList.getArray();
        int      limit = session.rowActionList.size();

        if (start == limit) {
            return;
        }

        for (int i = limit - 1; i >= start; i--) {
            RowAction action = (RowAction) list[i];

            if (action == null || action.type == RowActionBase.ACTION_NONE
                    || action.type == RowActionBase.ACTION_DELETE_FINAL) {
                continue;
            }

            Row row = action.memoryRow;

            if (row == null) {
                row = (Row) action.store.get(action.getPos(), false);
            }

            if (row == null) {
                continue;
            }

            action.rollback(session, timestamp);

            int type = action.mergeRollback(session, timestamp, row);

            action.store.rollbackRow(session, row, type, txModel);
        }

        session.rowActionList.setSize(start);
    }

    public RowAction addDeleteAction(Session session, Table table, Row row,
                                     int[] colMap) {

        RowAction action;

        synchronized (row) {
            action = RowAction.addDeleteAction(session, table, row, colMap);
        }

        session.rowActionList.add(action);

        PersistentStore store = session.sessionData.getRowStore(table);

        store.delete(session, row);

        row.rowAction = null;

        return action;
    }

    public void addInsertAction(Session session, Table table,
                                PersistentStore store, Row row,
                                int[] changedColumns) {

        RowAction action = row.rowAction;

        if (action == null) {
            System.out.println("null insert action " + session + " "
                               + session.actionTimestamp);
        }

        store.indexRow(session, row);
        session.rowActionList.add(action);

        row.rowAction = null;
    }

// functional unit - accessibility of rows
    public boolean canRead(Session session, Row row, int mode, int[] colMap) {
        return true;
    }

    public boolean canRead(Session session, int id, int mode) {
        return true;
    }

    /**
     * add transaction info to a row just loaded from the cache. called only
     * for CACHED tables
     */
    public void setTransactionInfo(CachedObject object) {}

    public void removeTransactionInfo(CachedObject object) {}

    public void beginTransaction(Session session) {

        if (!session.isTransaction) {
            session.actionTimestamp      = nextChangeTimestamp();
            session.transactionTimestamp = session.actionTimestamp;
            session.isTransaction        = true;

            transactionCount++;
        }
    }

    /**
     * add session to the end of queue when a transaction starts
     * (depending on isolation mode)
     */
    public void beginAction(Session session, Statement cs) {

        if (session.hasLocks(cs)) {
            return;
        }

        writeLock.lock();

        try {
            boolean canProceed = setWaitedSessionsTPL(session, cs);

            if (canProceed) {
                if (session.tempSet.isEmpty()) {
                    lockTablesTPL(session, cs);

                    // we don't set other sessions that would now be waiting for this one too
                    // next lock release will do it
                } else {
                    setWaitingSessionTPL(session);
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void beginActionResume(Session session) {

        session.actionTimestamp = nextChangeTimestamp();

        if (!session.isTransaction) {
            session.transactionTimestamp = session.actionTimestamp;
            session.isTransaction        = true;

            transactionCount++;
        }

        return;
    }

    void endTransaction(Session session) {

        if (session.isTransaction) {
            session.isTransaction = false;

            transactionCount--;
        }
    }

// functional unit - list actions and translate id's

    /**
     * Return a lookup of all row ids for cached tables in transactions.
     */
    public DoubleIntIndex getTransactionIDList() {

        DoubleIntIndex lookup = new DoubleIntIndex(10, false);

        return lookup;
    }

    /**
     * Convert row ID's for cached table rows in transactions
     */
    public void convertTransactionIDs(DoubleIntIndex lookup) {}
}
