/* Copyright (c) 2001-2009, The HSQL Development Group
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


package org.hsqldb.types;

import java.io.Reader;
import java.io.Writer;

import org.hsqldb.Error;
import org.hsqldb.ErrorCode;
import org.hsqldb.HsqlException;
import org.hsqldb.SessionInterface;
import org.hsqldb.result.Result;
import org.hsqldb.result.ResultLob;
import org.hsqldb.lib.ArrayUtil;

/**
 * Implementation of CLOB for client and server.<p>
 *
 * @author Fred Toussi (fredt@users dot sourceforge.net)
 * @version 1.9.0
 * @since 1.9.0
 */
public class ClobDataID implements ClobData {

    long id;

    public ClobDataID(long id) {
        this.id = id;
    }

    public char[] getChars(SessionInterface session, long position,
                           int length) throws HsqlException {

        ResultLob resultOut = ResultLob.newLobGetCharsRequest(id, position,
            length);
        Result resultIn = session.execute(resultOut);

        if (resultIn.isError()) {
            throw resultIn.getException();
        }

        return ((ResultLob) resultIn).getCharArray();
    }

    public long length(SessionInterface session) throws HsqlException {

        ResultLob resultOut = ResultLob.newLobGetLengthRequest(id);
        Result    resultIn  = session.execute(resultOut);

        if (resultIn.isError()) {
            throw resultIn.getException();
        }

        return ((ResultLob) resultIn).getBlockLength();
    }

    public String getSubString(SessionInterface session, long pos,
                               int length) throws HsqlException {

        char[] chars = getChars(session, pos, length);

        return new String(chars);
    }

    public ClobData getClob(SessionInterface session, long position,
                                 long length) throws HsqlException {

        ResultLob resultOut = ResultLob.newLobGetRequest(id, position, length);
        Result    resultIn  = session.execute(resultOut);

        if (resultIn.isError()) {
            throw resultIn.getException();
        }

        return new ClobDataID(((ResultLob) resultIn).getLobID());
    }

    public void truncate(SessionInterface session,
                         long len) throws HsqlException {}

    public Reader getCharacterStream(SessionInterface session)
    throws HsqlException {
        return null;
    }

    public long setCharacterStream(SessionInterface session, long pos,
                                   Reader in) throws HsqlException {
        return 0;
    }

    public int setString(SessionInterface session, long pos,
                         String str) throws HsqlException {

        ResultLob resultOut = ResultLob.newLobSetCharsRequest(id, pos,
            str.toCharArray());
        Result resultIn = session.execute(resultOut);

        if (resultIn.isError()) {
            throw resultIn.getException();
        }

        return str.length();
    }

    public int setString(SessionInterface session, long pos, String str,
                         int offset, int len) throws HsqlException {

        if (!isInLimits(str.length(), offset, len)) {
            throw Error.error(ErrorCode.X_22001);
        }

        ResultLob resultOut = ResultLob.newLobSetCharsRequest(id, pos,
            str.substring(offset, len).toCharArray());
        Result resultIn = session.execute(resultOut);

        if (resultIn.isError()) {
            throw resultIn.getException();
        }

        return str.length();
    }

    public int setChars(SessionInterface session, long pos, char[] chars,
                        int offset, int len) throws HsqlException {

        if (!isInLimits(chars.length, offset, len)) {
            throw Error.error(ErrorCode.X_22001);
        }

        char[] newChars = new char[len];

        System.arraycopy(chars, offset, newChars, 0, len);

        ResultLob resultOut = ResultLob.newLobSetCharsRequest(id, pos, chars);
        Result    resultIn  = session.execute(resultOut);

        if (resultIn.isError()) {
            throw resultIn.getException();
        }

        return len;
    }

    public long position(SessionInterface session, String searchstr,
                         long start) throws HsqlException {

        ResultLob resultOut = ResultLob.newLobGetCharPatternPositionRequest(id,
            searchstr.toCharArray(), start);
        Result resultIn = session.execute(resultOut);

        if (resultIn.isError()) {
            throw resultIn.getException();
        }

        return ((ResultLob) resultIn).getOffset();
    }

    public long position(SessionInterface session, ClobData searchstr,
                         long start) throws HsqlException {
        return 0L;
    }

    public long nonSpaceLength(SessionInterface session) throws HsqlException {
        return 0;
    }

    public Reader getCharacterStream(SessionInterface session, long pos,
                                     long length) throws HsqlException {
        return null;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRightTrimSize(SessionInterface session) {
        return 0;
    }

    static boolean isInLimits(long fullLength, long pos, long len) {
        return pos >= 0 && len >= 0 && pos + len <= fullLength;
    }
}
