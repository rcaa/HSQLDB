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


package org.hsqldb.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import junit.framework.TestCase;
import java.sql.ResultSet;

/**
 * @author fredt@users
 */
public class TestPreparedStatements extends TestCase {

    private Connection con = null;

    private class sqlStmt {

        boolean prepare;
        boolean update;
        String  command;

        sqlStmt(String c, boolean p, boolean u) {

            command = c;
            prepare = p;
            update  = u;
        }
    }

    private sqlStmt[] stmtArray = {
        new sqlStmt("drop table public.dttest if exists cascade", false, false),
        new sqlStmt(
            "create cached table dttest(adate date not null, "
            + "atime time not null,bg int, primary key(adate,atime))", false,
                false),
        new sqlStmt(
            "insert into dttest values(current_date - 10 day, current_time + 1 hour, 1)",
            false, true),
        new sqlStmt(
            "insert into dttest values(current_date - 8 day, current_time - 5 hour, 2)",
            false, true),
        new sqlStmt(
            "insert into dttest values(current_date - 7 day, current_time - 4 hour, 3)",
            false, true),
        new sqlStmt(
            "insert into dttest values(current_date, '12:44:31', 4)",
            false, true),
        new sqlStmt(
            "insert into dttest values(current_date + 3 day, current_time - 12 hour, 5)",
            false, true),
        new sqlStmt(
            "insert into dttest values(current_date + 1 day, current_time - 1 hour, 6)",
            false, true),
        new sqlStmt(
            "select atime adate from dttest where atime =  ? and adate = ?",
            true, false),
    };
    private Object[][] stmtArgs = {
        {}, {}, {}, {}, {}, {}, {}, {},
        new Object[]{  "12:44:31", new java.sql.Date(System.currentTimeMillis()) }
    };

    public TestPreparedStatements(String name) {
        super(name);
    }

    protected void setUp() {

        String url = "jdbc:hsqldb:test";

        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");

            con = java.sql.DriverManager.getConnection(url, "sa", "");
        } catch (Exception e) {}
    }

    public void testA() {

        try {
            int i = 0;

            for (i = 0; i < stmtArray.length; i++) {
                int j;

                System.out.println(" -- #" + i + " ----------------------- ");

                if (stmtArray[i].prepare) {
                    PreparedStatement ps = null;

                    System.out.println(" -- preparing\n<<<\n"
                                       + stmtArray[i].command + "\n>>>\n");

                    ps = con.prepareStatement(stmtArray[i].command);

                    System.out.print(" -- setting " + stmtArgs[i].length
                                     + " Args [");

                    for (j = 0; j < stmtArgs[i].length; j++) {
                        System.out.print((j > 0 ? "; "
                                                : "") + stmtArgs[i][j]);
                        ps.setObject(j + 1, stmtArgs[i][j]);
                    }

                    System.out.println("]");
                    System.out.println(" -- executing ");

                    if (stmtArray[i].update) {
                        int r = ps.executeUpdate();

                        System.out.println(" ***** ps.executeUpdate gave me "
                                           + r);
                    } else {
                        boolean b = ps.execute();
                        int count = 0;
                        if (b) {

                            ResultSet rs = ps.getResultSet();
                            while(rs.next()) {
                                count++;
                            }
                            System.out.print(" ***** ps.execute returned result row count " + count);

                        } else {
                            System.out.print(" ***** ps.execute gave me " + b);
                        }
                    }
                } else {
                    System.out.println(" -- executing directly\n<<<\n"
                                       + stmtArray[i].command + "\n>>>\n");

                    Statement s = con.createStatement();
                    boolean   b = s.execute(stmtArray[i].command);

                    System.out.println(" ***** st.execute gave me " + b);
                }
            }
        } catch (Exception e) {
            System.out.println(" ?? Caught Exception " + e);
            assertTrue(false);
        }

        assertTrue(true);
    }
}
