/* Copyright (c) 2001-2005, The HSQL Development Group
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

import org.hsqldb.lib.*;

class TxManager {

    LongKeyIntValueHashMap rowSessionMap;

    TxManager() {
        rowSessionMap = new LongKeyIntValueHashMap();
    }

    void checkDelete(Session session, Row row) throws HsqlException {}

    void checkDelete(Session session,
                     HashMappedList rowSet) throws HsqlException {

        int sessionid = session.getId();

        for (int i = 0, size = rowSet.size(); i < size; i++) {
            Row  row   = (Row) rowSet.getKey(i);
            long rowid = row.getId();

            if (rowSessionMap.get(rowid, sessionid) != sessionid) {
                throw Trace.error(Trace.INVALID_TRANSACTION_STATE_NO_SUBCLASS,
                                  Trace.ITSNS_OVERWRITE);
            }
        }
    }

    void checkDelete(Session session,
                     HsqlArrayList rowSet) throws HsqlException {

        int sessionid = session.getId();

        for (int i = 0, size = rowSet.size(); i < size; i++) {
            Row  row   = (Row) rowSet.get(i);
            long rowid = row.getId();

            if (rowSessionMap.get(rowid, sessionid) != sessionid) {
                throw Trace.error(Trace.INVALID_TRANSACTION_STATE_NO_SUBCLASS,
                                  Trace.ITSNS_OVERWRITE);
            }
        }
    }

    void commit(Session session) {

        Object[] list = session.transactionList.getArray();
        int      size = session.transactionList.size();

        for (int i = 0; i < size; i++) {
            Transaction tx    = (Transaction) list[i];
            long        rowid = tx.row.getId();

            rowSessionMap.remove(rowid);
        }

        session.transactionList.clear();
        session.savepoints.clear();
    }

    synchronized void rollback(Session session) {

        int size = session.transactionList.size();

        rollbackTransactions(session, 0, false);
        session.savepoints.clear();
    }

    void rollbackSavepoint(Session session,
                           String name) throws HsqlException {

        int index = session.savepoints.getIndex(name);

        if (index < 1) {
            throw Trace.error(Trace.SAVEPOINT_NOT_FOUND, name);
        }

        Integer oi    = (Integer) session.savepoints.get(index);
        int     limit = oi.intValue();

        rollbackTransactions(session, limit, false);

        while (session.savepoints.size() > index) {
            session.savepoints.remove(session.savepoints.size() - 1);
        }
    }

    void rollbackTransactions(Session session, int limit, boolean log) {

        Object[] list = session.transactionList.getArray();
        int      size = session.transactionList.size();

        for (int i = size - 1; i >= limit; i--) {
            Transaction tx = (Transaction) list[i];

            tx.rollback(session, false);
        }

        for (int i = limit; i < size; i++) {
            Transaction tx    = (Transaction) list[i];
            long        rowid = tx.row.getId();

            rowSessionMap.remove(rowid);
        }

        session.transactionList.setSize(limit);
    }

    void addTransaction(Session session, Transaction transaction) {
        rowSessionMap.put(transaction.row.getId(), session.getId());
    }
}
