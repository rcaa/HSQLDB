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


package org.hsqldb.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class TestDima {

    public void testOne() {

        try {
            Class.forName("org.hsqldb.jdbcDriver");

            Connection conn = java.sql.DriverManager.getConnection(
                "jdbc:hsqldb:file:/hsql/testdima/test", "sa", "");

            conn.setAutoCommit(false);

            Statement stat = conn.createStatement();

            stat.executeUpdate("DROP TABLE BAZ IF EXISTS");
            stat.executeUpdate("DROP TABLE BAR IF EXISTS");
            stat.executeUpdate("DROP TABLE FOO IF EXISTS");
            conn.commit();
            stat.executeUpdate("CHECKPOINT");
            stat.executeUpdate(
                "CREATE CACHED TABLE FOO (ID INTEGER IDENTITY PRIMARY KEY, VAL VARCHAR(80))");
            stat.executeUpdate(
                "CREATE TABLE BAR (ID INTEGER IDENTITY PRIMARY KEY, FOOID INTEGER NOT NULL, "
                + "VAL VARCHAR(80), FOREIGN KEY(FOOID) REFERENCES FOO(ID) ON DELETE CASCADE)");
            stat.executeUpdate(
                "CREATE TABLE BAZ (ID INTEGER IDENTITY PRIMARY KEY, BARID INTEGER NOT NULL, "
                + "VAL VARCHAR(80), FOREIGN KEY(BARID) REFERENCES BAR(ID) ON DELETE CASCADE)");
            conn.commit();
            stat.executeUpdate("CHECKPOINT");
            stat.executeUpdate("INSERT INTO FOO (VAL) VALUES ('foo 1')");
            stat.executeUpdate(
                "INSERT INTO BAR (FOOID,VAL) VALUES (IDENTITY(),'bar 1')");
            stat.executeUpdate(
                "INSERT INTO BAZ (BARID,VAL) VALUES (IDENTITY(),'baz 1')");
            stat.executeUpdate("INSERT INTO FOO (VAL) VALUES ('foo 2')");
            stat.executeUpdate(
                "INSERT INTO BAR (FOOID,VAL) VALUES (IDENTITY(),'bar 2')");
            stat.executeUpdate(
                "INSERT INTO BAZ (BARID,VAL) VALUES (IDENTITY(),'baz 2')");
            stat.executeUpdate("INSERT INTO FOO (VAL) VALUES ('foo 3')");
            stat.executeUpdate(
                "INSERT INTO BAR (FOOID,VAL) VALUES (IDENTITY(),'bar 3')");
            stat.executeUpdate(
                "INSERT INTO BAZ (BARID,VAL) VALUES (IDENTITY(),'baz 3')");

            ResultSet rs;
            Statement query = conn.createStatement(
                java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE,
                java.sql.ResultSet.CONCUR_READ_ONLY);

            rs = query.executeQuery("SELECT ID,VAL FROM FOO");

            System.out.println("Table FOO:");

            while (rs.next()) {
                System.out.println(rs.getInt(1));
                System.out.println(rs.getString(2));
            }

            rs = query.executeQuery("SELECT ID,FOOID,VAL FROM BAR");

            System.out.println("Table BAR:");

            while (rs.next()) {
                System.out.println(rs.getInt(1));
                System.out.println(rs.getInt(2));
                System.out.println(rs.getString(3));
            }

            rs = query.executeQuery("SELECT ID,BARID,VAL FROM BAZ");

            System.out.println("Table BAZ:");

            while (rs.next()) {
                System.out.println(rs.getInt(1));
                System.out.println(rs.getInt(2));
                System.out.println(rs.getString(3));
            }

            rs.close();
            query.close();
            stat.close();
            conn.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testTwo() {

        TestSelf.deleteDatabase("test");

        try {
            Class.forName("org.hsqldb.jdbcDriver");

            Properties pp = new Properties();

            pp.put("user", "sa");
            pp.put("password", "");

            Connection c =
                DriverManager.getConnection("jdbc:hsqldb:file:test", pp);

            c.createStatement().executeUpdate(
                "create cached table SNS_OIDS(NAME varchar(20) not null primary key, ID int)");
            c.createStatement().executeUpdate(
                "UPDATE SNS_OIDS SET \"ID\"=1 WHERE \"NAME\"='_snsLog'");
            c.createStatement().executeUpdate(
                "INSERT INTO SNS_OIDS (\"NAME\", \"ID\") VALUES ('_snsLog', 1)");
            c.createStatement().executeUpdate(
                "UPDATE SNS_OIDS SET \"ID\"=1 WHERE \"NAME\"='visitorTags'");
            c.createStatement().executeUpdate(
                "INSERT INTO SNS_OIDS (\"NAME\", \"ID\") VALUES ('visitorTags', 1)");
            c.createStatement().executeUpdate(
                "UPDATE SNS_OIDS SET \"ID\"=1 WHERE \"NAME\"='departments'");
            c.createStatement().executeUpdate(
                "INSERT INTO SNS_OIDS (\"NAME\", \"ID\") VALUES ('departments', 1)");
            c.createStatement().executeUpdate(
                "UPDATE SNS_OIDS SET \"ID\"=1 WHERE \"NAME\"='operators'");
            c.createStatement().executeUpdate(
                "INSERT INTO SNS_OIDS (\"NAME\", \"ID\") VALUES ('operators', 1)");
            c.createStatement().executeUpdate(
                "UPDATE SNS_OIDS SET \"ID\"=1 WHERE \"NAME\"='zones'");
            c.createStatement().executeUpdate(
                "INSERT INTO SNS_OIDS (\"NAME\", \"ID\") VALUES ('zones', 1)");
            c.createStatement().executeUpdate(
                "UPDATE SNS_OIDS SET \"ID\"=1 WHERE \"NAME\"='pages'");
            c.createStatement().executeUpdate(
                "INSERT INTO SNS_OIDS (\"NAME\", \"ID\") VALUES ('pages', 1)");
            c.createStatement().executeUpdate(
                "UPDATE SNS_OIDS SET \"ID\"=21 WHERE \"NAME\"='visitorTags'");
            c.createStatement().executeUpdate(
                "UPDATE SNS_OIDS SET \"ID\"=1 WHERE \"NAME\"='actionDefinitions'");
            c.createStatement().executeUpdate(
                "INSERT INTO SNS_OIDS (\"NAME\", \"ID\") VALUES ('actionDefinitions', 1)");
            c.createStatement().executeUpdate(
                "UPDATE SNS_OIDS SET \"ID\"=1 WHERE \"NAME\"='actionVariants'");
            c.createStatement().executeUpdate(
                "INSERT INTO SNS_OIDS (\"NAME\", \"ID\") VALUES ('actionVariants', 1)");
            c.createStatement().executeUpdate(
                "UPDATE SNS_OIDS SET \"ID\"=1 WHERE \"NAME\"='actionPoints'");
            c.createStatement().executeUpdate(
                "INSERT INTO SNS_OIDS (\"NAME\", \"ID\") VALUES ('actionPoints', 1)");
            c.createStatement().executeUpdate(
                "UPDATE SNS_OIDS SET \"ID\"=1 WHERE \"NAME\"='actionTags'");
            c.createStatement().executeUpdate(
                "INSERT INTO SNS_OIDS (\"NAME\", \"ID\") VALUES ('actionTags', 1)");
            c.createStatement().executeUpdate(
                "UPDATE SNS_OIDS SET \"ID\"=1 WHERE \"NAME\"='captureFields'");
            c.createStatement().executeUpdate(
                "INSERT INTO SNS_OIDS (\"NAME\", \"ID\") VALUES ('captureFields', 1)");
            c.createStatement().executeUpdate(
                "UPDATE SNS_OIDS SET \"ID\"=1 WHERE \"NAME\"='reactions'");
            c.createStatement().executeUpdate(
                "INSERT INTO SNS_OIDS (\"NAME\", \"ID\") VALUES ('reactions', 1)");
            c.createStatement().executeUpdate(
                "UPDATE SNS_OIDS SET \"ID\"=1 WHERE \"NAME\"='reactionOperations'");
            c.createStatement().executeUpdate(
                "INSERT INTO SNS_OIDS (\"NAME\", \"ID\") VALUES ('reactionOperations', 1)");

            int count = c.createStatement().executeUpdate(
                "UPDATE SNS_OIDS SET \"ID\"=21 WHERE \"NAME\"='actionTags'");

            System.out.println("count == " + count);    // should be 1, 0 instead!
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {

        TestDima test = new TestDima();

        test.testOne();
        test.testTwo();

    }
}
