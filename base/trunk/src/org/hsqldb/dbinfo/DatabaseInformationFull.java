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


package org.hsqldb.dbinfo;

import java.lang.reflect.Method;

import org.hsqldb.Collation;
import org.hsqldb.ColumnSchema;
import org.hsqldb.Constraint;
import org.hsqldb.Database;
import org.hsqldb.Expression;
import org.hsqldb.HsqlException;
import org.hsqldb.HsqlNameManager.HsqlName;
import org.hsqldb.NumberSequence;
import org.hsqldb.SchemaObject;
import org.hsqldb.SchemaObjectSet;
import org.hsqldb.Session;
import org.hsqldb.SqlInvariants;
import org.hsqldb.Table;
import org.hsqldb.TextTable;
import org.hsqldb.Tokens;
import org.hsqldb.TriggerDef;
import org.hsqldb.Types;
import org.hsqldb.View;
import org.hsqldb.lib.ArrayUtil;
import org.hsqldb.lib.FileUtil;
import org.hsqldb.lib.HashMap;
import org.hsqldb.lib.HashSet;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.Iterator;
import org.hsqldb.lib.OrderedHashSet;
import org.hsqldb.lib.Set;
import org.hsqldb.lib.WrapperIterator;
import org.hsqldb.persist.DataFileCache;
import org.hsqldb.persist.HsqlDatabaseProperties;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.persist.PersistentStore;
import org.hsqldb.persist.TextCache;
import org.hsqldb.result.Result;
import org.hsqldb.rights.Grantee;
import org.hsqldb.rights.Right;
import org.hsqldb.scriptio.ScriptWriterBase;
import org.hsqldb.store.ValuePool;
import org.hsqldb.types.Charset;
import org.hsqldb.types.NumberType;
import org.hsqldb.types.TimestampData;
import org.hsqldb.types.Type;

// fredt@users - 1.7.2 - structural modifications to allow inheritance
// boucherb@users - 1.7.2 - 20020225
// - factored out all reusable code into DIXXX support classes
// - completed Fred's work on allowing inheritance
// boucherb@users - 1.7.2 - 20020304 - bug fixes, refinements, better java docs
// fredt@users - 1.8.0 - updated to report latest enhancements and changes
// boucherb@users - 1.8.0 - 20050515 - further SQL 2003 metadata support
// boucherb@users 20051207 - patch 1.8.x initial JDBC 4.0 support work
// fredt@users - 1.9.0 - new tables + renaming + upgrade of some others to SQL/SCHEMATA

/**
 * Provides definitions for most of the SQL Standard Schemata views that are
 * supported by HSQLDB.<p>
 *
 * Provides definitions for some of HSQLDB's additional system vies.
 *
 * The definitions for the rest of system vies are provided by
 * DatabaseInformationMain, which this class extends. <p>
 *
 * @author Campbell Boucher-Burnett (boucherb@users dot sourceforge.net)
 * @author Fred Toussi (fredt@users dot sourceforge.net)
 * @version 1.9.0
 * @since 1.7.2
 */
final class DatabaseInformationFull
extends org.hsqldb.dbinfo.DatabaseInformationMain {

    /**
     * Constructs a new DatabaseInformationFull instance. <p>
     *
     * @param db the database for which to produce system tables.
     * @throws HsqlException if a database access error occurs.
     */
    DatabaseInformationFull(Database db) throws HsqlException {
        super(db);
    }

    /**
     * Retrieves the system table corresponding to the specified index. <p>
     *
     * @param tableIndex index identifying the system table to generate
     * @throws HsqlException if a database access error occurs
     * @return the system table corresponding to the specified index
     */
    protected Table generateTable(int tableIndex) throws HsqlException {

        switch (tableIndex) {

            case SYSTEM_PROCEDURECOLUMNS :
                return SYSTEM_PROCEDURECOLUMNS();

            case SYSTEM_PROCEDURES :
                return SYSTEM_PROCEDURES();

            case SYSTEM_SUPERTABLES :
                return SYSTEM_SUPERTABLES();

            case SYSTEM_SUPERTYPES :
                return SYSTEM_SUPERTYPES();

            case SYSTEM_UDTATTRIBUTES :
                return SYSTEM_UDTATTRIBUTES();

            case SYSTEM_UDTS :
                return SYSTEM_UDTS();

            case SYSTEM_VERSIONCOLUMNS :
                return SYSTEM_VERSIONCOLUMNS();

            // HSQLDB-specific
            case SYSTEM_ALIASES :
                return SYSTEM_ALIASES();

            case SYSTEM_CACHEINFO :
                return SYSTEM_CACHEINFO();

            case SYSTEM_CLASSPRIVILEGES :
                return SYSTEM_CLASSPRIVILEGES();

            case SYSTEM_SESSIONINFO :
                return SYSTEM_SESSIONINFO();

            case SYSTEM_PROPERTIES :
                return SYSTEM_PROPERTIES();

            case SYSTEM_SESSIONS :
                return SYSTEM_SESSIONS();

            case SYSTEM_TRIGGERCOLUMNS :
                return SYSTEM_TRIGGERCOLUMNS();

            case SYSTEM_TRIGGERS :
                return SYSTEM_TRIGGERS();

            case SYSTEM_TEXTTABLES :
                return SYSTEM_TEXTTABLES();

            // SQL views
            case ADMINISTRABLE_ROLE_AUTHORIZATIONS :
                return ADMINISTRABLE_ROLE_AUTHORIZATIONS();

            case APPLICABLE_ROLES :
                return APPLICABLE_ROLES();

            case ASSERTIONS :
                return ASSERTIONS();

            case AUTHORIZATIONS :
                return AUTHORIZATIONS();

            case CHARACTER_SETS :
                return CHARACTER_SETS();

            case CHECK_CONSTRAINT_ROUTINE_USAGE :
                return CHECK_CONSTRAINT_ROUTINE_USAGE();

            case CHECK_CONSTRAINTS :
                return CHECK_CONSTRAINTS();

            case COLLATIONS :
                return COLLATIONS();

            case COLUMN_COLUMN_USAGE :
                return COLUMN_COLUMN_USAGE();

            case COLUMN_DOMAIN_USAGE :
                return COLUMN_DOMAIN_USAGE();

            case COLUMN_UDT_USAGE :
                return COLUMN_UDT_USAGE();

            case CONSTRAINT_COLUMN_USAGE :
                return CONSTRAINT_COLUMN_USAGE();

            case CONSTRAINT_TABLE_USAGE :
                return CONSTRAINT_TABLE_USAGE();

            case COLUMNS :
                return COLUMNS();

            case DATA_TYPE_PRIVILEGES :
                return DATA_TYPE_PRIVILEGES();

            case DOMAIN_CONSTRAINTS :
                return DOMAIN_CONSTRAINTS();

            case DOMAINS :
                return DOMAINS();

            case ENABLED_ROLES :
                return ENABLED_ROLES();

            case JAR_JAR_USAGE :
                return JAR_JAR_USAGE();

            case JARS :
                return JARS();

            case KEY_COLUMN_USAGE :
                return KEY_COLUMN_USAGE();

            case METHOD_SPECIFICATIONS :
                return METHOD_SPECIFICATIONS();

            case MODULE_COLUMN_USAGE :
                return MODULE_COLUMN_USAGE();

            case MODULE_PRIVILEGES :
                return MODULE_PRIVILEGES();

            case MODULE_TABLE_USAGE :
                return MODULE_TABLE_USAGE();

            case MODULES :
                return MODULES();

            case PARAMETERS :
                return PARAMETERS();

            case REFERENTIAL_CONSTRAINTS :
                return REFERENTIAL_CONSTRAINTS();

            case ROLE_AUTHORIZATION_DESCRIPTORS :
                return ROLE_AUTHORIZATION_DESCRIPTORS();

            case ROLE_COLUMN_GRANTS :
                return ROLE_COLUMN_GRANTS();

            case ROLE_ROUTINE_GRANTS :
                return ROLE_ROUTINE_GRANTS();

            case ROLE_TABLE_GRANTS :
                return ROLE_TABLE_GRANTS();

            case ROLE_USAGE_GRANTS :
                return ROLE_USAGE_GRANTS();

            case ROLE_UDT_GRANTS :
                return ROLE_UDT_GRANTS();

            case ROUTINE_JAR_USAGE :
                return ROUTINE_JAR_USAGE();

            case ROUTINES :
                return ROUTINES();

            case SCHEMATA :
                return SCHEMATA();

            case SEQUENCES :
                return SEQUENCES();

            case SQL_FEATURES :
                return SQL_FEATURES();

            case SQL_IMPLEMENTATION_INFO :
                return SQL_IMPLEMENTATION_INFO();

            case SQL_PACKAGES :
                return SQL_PACKAGES();

            case SQL_PARTS :
                return SQL_PARTS();

            case SQL_SIZING :
                return SQL_SIZING();

            case SQL_SIZING_PROFILES :
                return SQL_SIZING_PROFILES();

            case TABLE_CONSTRAINTS :
                return TABLE_CONSTRAINTS();

            case TABLES :
                return TABLES();

            case TRANSLATIONS :
                return TRANSLATIONS();

            case TRIGGERED_UPDATE_COLUMNS :
                return TRIGGERED_UPDATE_COLUMNS();

            case TRIGGER_COLUMN_USAGE :
                return TRIGGER_COLUMN_USAGE();

            case TRIGGER_ROUTINE_USAGE :
                return TRIGGER_ROUTINE_USAGE();

            case TRIGGER_SEQUENCE_USAGE :
                return TRIGGER_SEQUENCE_USAGE();

            case TRIGGER_TABLE_USAGE :
                return TRIGGER_TABLE_USAGE();

            case TRIGGERS :
                return TRIGGERS();

            case TYPE_JAR_USAGE :
                return TYPE_JAR_USAGE();

            case USAGE_PRIVILEGES :
                return USAGE_PRIVILEGES();

            case USER_DEFINED_TYPES :
                return USER_DEFINED_TYPES();

            case VIEW_COLUMN_USAGE :
                return VIEW_COLUMN_USAGE();

            case VIEW_TABLE_USAGE :
                return VIEW_TABLE_USAGE();

            case VIEW_ROUTINE_USAGE :
                return VIEW_ROUTINE_USAGE();

            case VIEWS :
                return VIEWS();

            default :
                return super.generateTable(tableIndex);
        }
    }

    /**
     * Retrieves a <code>Table</code> object describing the aliases defined
     * within this database. <p>
     *
     * Currently two types of alias are reported: DOMAIN alaises (alternate
     * names for column data types when issuing "CREATE TABLE" DDL) and
     * ROUTINE aliases (alternate names that can be used when invoking
     * routines as SQL functions or stored procedures). <p>
     *
     * Each row is an alias description with the following columns: <p>
     *
     * <pre class="SqlCodeExample">
     * OBJECT_TYPE  VARCHAR   type of the aliased object
     * OBJECT_CAT   VARCHAR   catalog of the aliased object
     * OBJECT_SCHEM VARCHAR   schema of the aliased object
     * OBJECT_NAME  CHARACTER_DATA   simple identifier of the aliased object
     * ALIAS_CAT    VARCHAR   catalog in which alias is defined
     * ALIAS_SCHEM  VARCHAR   schema in which alias is defined
     * ALIAS        VARCHAR   alias for the indicated object
     * </pre> <p>
     *
     * <b>Note:</b> Up to and including HSQLDB 1.7.2, user-defined aliases
     * are supported only for SQL function and stored procedure calls
     * (indicated by the value "ROUTINE" in the OBJECT_TYPE
     * column), and there is no syntax for dropping aliases, only for
     * creating them. <p>
     * @return a Table object describing the accessisble
     *      aliases in the context of the calling session
     * @throws HsqlException if an error occurs while producing the table
     */
    Table SYSTEM_ALIASES() throws HsqlException {

        Table t = sysTables[SYSTEM_ALIASES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_ALIASES]);

            addColumn(t, "OBJECT_TYPE", SQL_IDENTIFIER);    // not null
            addColumn(t, "OBJECT_CAT", SQL_IDENTIFIER);
            addColumn(t, "OBJECT_SCHEM", SQL_IDENTIFIER);
            addColumn(t, "OBJECT_NAME", CHARACTER_DATA);    // not null
            addColumn(t, "ALIAS_CAT", SQL_IDENTIFIER);
            addColumn(t, "ALIAS_SCHEM", SQL_IDENTIFIER);
            addColumn(t, "ALIAS", SQL_IDENTIFIER);          // not null

            // order: OBJECT_TYPE, OBJECT_NAME, ALIAS.
            // true PK.
            t.createPrimaryKey(null, new int[] {
                0, 3, 6
            }, true);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // Holders for calculated column values
        String cat;
        String schem;
        String alias;
        String objName;
        String objType;

        // Intermediate holders
        String   className;
        HashMap  aliasMap;
        Iterator aliases;
        Object[] row;
        int      pos;

        // Column number mappings
        final int ialias_object_type  = 0;
        final int ialias_object_cat   = 1;
        final int ialias_object_schem = 2;
        final int ialias_object_name  = 3;
        final int ialias_cat          = 4;
        final int ialias_schem        = 5;
        final int ialias              = 6;

        // Initialization
        aliasMap = new HashMap();    //database.aliasManager.getAliasMap();
        aliases  = aliasMap.keySet().iterator();
        objType  = "ROUTINE";
        cat      = database.getCatalogName().name;
        schem    = database.schemaManager.getDefaultSchemaHsqlName().name;

        // Do it.
        while (aliases.hasNext()) {
            row     = t.getEmptyRowData();
            alias   = (String) aliases.next();
            objName = (String) aliasMap.get(alias);

            // must have class grant to see method call aliases
            pos = objName.lastIndexOf('.');

            if (pos <= 0) {

                // should never occur in practice, as this is typically a Java
                // method name, but there's nothing preventing a user from
                // creating an alias entry that is not in method FQN form;
                // such entries are not illegal, only useless.  Probably,
                // we should eventually try to disallow them.
                continue;
            }

            className = objName.substring(0, pos);

// todo 190
            SchemaObject object =
                database.schemaManager.findSchemaObject(className, schem,
                    SchemaObject.FUNCTION);

            if (object == null || !session.getGrantee().isAccessible(object)) {
                continue;
            }

            row[ialias_object_type]  = objType;
            row[ialias_object_cat]   = cat;
            row[ialias_object_schem] = schem;
            row[ialias_object_name]  = objName;
            row[ialias_cat]          = cat;
            row[ialias_schem]        = schem;
            row[ialias]              = alias;

            t.insertSys(store, row);
        }

        // must have create/alter table rights to see domain aliases
        if (session.isAdmin()) {
            Iterator typeAliases = Type.typeAliases.keySet().iterator();

            objType = "DOMAIN";

            while (typeAliases.hasNext()) {
                row   = t.getEmptyRowData();
                alias = (String) typeAliases.next();

                int tn = Type.typeAliases.get(alias, Integer.MIN_VALUE);

                objName = Type.getDefaultType(tn).getFullNameString();

                if (alias.equals(objName)) {
                    continue;
                }

                row[ialias_object_type]  = objType;
                row[ialias_object_cat]   = cat;
                row[ialias_object_schem] = schem;
                row[ialias_object_name]  = objName;
                row[ialias_cat]          = cat;
                row[ialias_schem]        = schem;
                row[ialias]              = alias;

                t.insertSys(store, row);
            }
        }

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the current
     * state of all row caching objects for the accessible
     * tables defined within this database. <p>
     *
     * Currently, the row caching objects for which state is reported are: <p>
     *
     * <OL>
     * <LI> the system-wide <code>Cache</code> object used by CACHED tables.
     * <LI> any <code>TextCache</code> objects in use by [TEMP] TEXT tables.
     * </OL> <p>
     *
     * Each row is a cache object state description with the following
     * columns: <p>
     *
     * <pre class="SqlCodeExample">
     * CACHE_FILE          CHARACTER_DATA   absolute path of cache data file
     * MAX_CACHE_SIZE      INTEGER   maximum allowable cached Row objects
     * MAX_CACHE_BYTE_SIZE INTEGER   maximum allowable size of cached Row objects
     * CACHE_LENGTH        INTEGER   number of data bytes currently cached
     * CACHE_SIZE          INTEGER   number of rows currently cached
     * FREE_BYTES          INTEGER   total bytes in available file allocation units
     * FREE_COUNT          INTEGER   total # of allocation units available
     * FREE_POS            INTEGER   largest file position allocated + 1
     * </pre> <p>
     *
     * <b>Notes:</b> <p>
     *
     * <code>TextCache</code> objects do not maintain a free list because
     * deleted rows are only marked deleted and never reused. As such, the
     * columns FREE_BYTES, SMALLEST_FREE_ITEM, LARGEST_FREE_ITEM, and
     * FREE_COUNT are always reported as zero for rows reporting on
     * <code>TextCache</code> objects. <p>
     *
     * Currently, CACHE_SIZE, FREE_BYTES, SMALLEST_FREE_ITEM, LARGEST_FREE_ITEM,
     * FREE_COUNT and FREE_POS are the only dynamically changing values.
     * All others are constant for the life of a cache object. In a future
     * release, other column values may also change over the life of a cache
     * object, as SQL syntax may eventually be introduced to allow runtime
     * modification of certain cache properties. <p>
     *
     * @return a description of the current state of all row caching
     *      objects associated with the accessible tables of the database
     * @throws HsqlException if an error occurs while producing the table
     */
    Table SYSTEM_CACHEINFO() throws HsqlException {

        Table t = sysTables[SYSTEM_CACHEINFO];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_CACHEINFO]);

            addColumn(t, "CACHE_FILE", CHARACTER_DATA);          // not null
            addColumn(t, "MAX_CACHE_COUNT", CARDINAL_NUMBER);    // not null
            addColumn(t, "MAX_CACHE_BYTES", CARDINAL_NUMBER);    // not null
            addColumn(t, "CACHE_SIZE", CARDINAL_NUMBER);         // not null
            addColumn(t, "CACHE_BYTES", CARDINAL_NUMBER);        // not null
            addColumn(t, "FILE_FREE_BYTES", CARDINAL_NUMBER);    // not null
            addColumn(t, "FILE_FREE_COUNT", CARDINAL_NUMBER);    // not null
            addColumn(t, "FILE_FREE_POS", CARDINAL_NUMBER);      // not null
            t.createPrimaryKey(null, new int[]{ 0 }, true);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());
        DataFileCache cache = null;
        Object[]      row;
        HashSet       cacheSet;
        Iterator      caches;
        Iterator      tables;
        Table         table;
        int           iFreeBytes;
        int           iLargestFreeItem;
        long          lSmallestFreeItem;

        // column number mappings
        final int icache_file      = 0;
        final int imax_cache_sz    = 1;
        final int imax_cache_bytes = 2;
        final int icache_size      = 3;
        final int icache_length    = 4;
        final int ifree_bytes      = 5;
        final int ifree_count      = 6;
        final int ifree_pos        = 7;

        // Initialization
        cacheSet = new HashSet();

        // dynamic system tables are never cached
        tables =
            database.schemaManager.databaseObjectIterator(SchemaObject.TABLE);

        while (tables.hasNext()) {
            table = (Table) tables.next();

            PersistentStore currentStore =
                database.persistentStoreCollection.getStore(
                    t.getPersistenceId());

            if (session.getGrantee().isFullyAccessibleByRole(table)) {
                if (currentStore != null) {
                    cache = currentStore.getCache();
                }

                if (cache != null) {
                    cacheSet.add(cache);
                }
            }
        }

        caches = cacheSet.iterator();

        // Do it.
        while (caches.hasNext()) {
            cache = (DataFileCache) caches.next();
            row   = t.getEmptyRowData();
            row[icache_file] =
                FileUtil.getDefaultInstance().canonicalOrAbsolutePath(
                    cache.getFileName());
            row[imax_cache_sz]    = ValuePool.getInt(cache.capacity());
            row[imax_cache_bytes] = ValuePool.getLong(cache.bytesCapacity());
            row[icache_size] = ValuePool.getInt(cache.getCachedObjectCount());
            row[icache_length] =
                ValuePool.getLong(cache.getTotalCachedBlockSize());
            row[ifree_bytes] = ValuePool.getInt(cache.getTotalFreeBlockSize());
            row[ifree_count] = ValuePool.getInt(cache.getFreeBlockCount());
            row[ifree_pos]   = ValuePool.getLong(cache.getFileFreePos());

            t.insertSys(store, row);
        }

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the visible
     * access rights for all accessible Java Class objects defined
     * within this database.<p>
     *
     * Each row is a Class privilege description with the following
     * columns: <p>
     *
     * <pre class="SqlCodeExample">
     * CLASS_CAT    VARCHAR   catalog in which the class is defined
     * CLASS_SCHEM  VARCHAR   schema in which the class is defined
     * CLASS_NAME   CHARACTER_DATA   fully qualified name of class
     * GRANTOR      VARCHAR   grantor of access
     * GRANTEE      VARCHAR   grantee of access
     * PRIVILEGE    CHARACTER_DATA   name of access: {"EXECUTE" | "TRIGGER"}
     * IS_GRANTABLE YES_OR_NO   grantable?: {"YES" | "NO" | NULL (unknown)}
     * </pre>
     *
     * <b>Note:</b> Users with the administrative privilege implicily have
     * full and unrestricted access to all Classes available to the database
     * class loader.  However, only explicitly granted rights are reported
     * in this table.  Explicit Class grants/revokes to admin users have no
     * effect in reality, but are reported in this table anyway for
     * completeness. <p>
     *
     * @return a <code>Table</code> object describing the visible
     *        access rights for all accessible Java Class
     *        objects defined within this database
     * @throws HsqlException if an error occurs while producing the table
     */
    Table SYSTEM_CLASSPRIVILEGES() throws HsqlException {

        Table t = sysTables[SYSTEM_CLASSPRIVILEGES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_CLASSPRIVILEGES]);

            addColumn(t, "CLASS_CAT", SQL_IDENTIFIER);
            addColumn(t, "CLASS_SCHEM", SQL_IDENTIFIER);
            addColumn(t, "CLASS_NAME", CHARACTER_DATA);    // not null
            addColumn(t, "GRANTOR", SQL_IDENTIFIER);       // not null
            addColumn(t, "GRANTEE", SQL_IDENTIFIER);       // not null
            addColumn(t, "PRIVILEGE", SQL_IDENTIFIER);     // not null
            addColumn(t, "IS_GRANTABLE", YES_OR_NO);       // not null
            t.createPrimaryKey(null, new int[] {
                2, 4, 5
            }, true);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // calculated column values
        String clsCat;
        String clsSchem;
        String clsName;
        String grantorName;
        String granteeName;
        String privilege;
        String isGrantable;

        // intermediate holders
        Grantee  grantee;
        Iterator grantees;
        HashSet  classNameSet;
        Iterator classNames;
        Object[] row;

        // column number mappings
        final int icls_cat   = 0;
        final int icls_schem = 1;
        final int icls_name  = 2;
        final int igrantor   = 3;
        final int igrantee   = 4;
        final int iprivilege = 5;
        final int iis_grntbl = 6;

        // Initialization
        grantorName = SqlInvariants.SYSTEM_AUTHORIZATION_NAME;
        grantees    = session.getGrantee().nonReservedVisibleGrantees(    /*andPublic*/
            true).iterator();
        clsCat   = database.getCatalogName().name;
        clsSchem = database.schemaManager.getDefaultSchemaHsqlName().name;

        while (grantees.hasNext()) {
            grantee     = (Grantee) grantees.next();
            granteeName = grantee.getNameString();
            isGrantable = grantee.isAdmin() ? Tokens.T_YES
                                            : Tokens.T_NO;
            classNames  = grantee.getGrantedClassNamesDirect().iterator();
            privilege   = "EXECUTE";

            while (classNames.hasNext()) {
                clsName         = (String) classNames.next();
                row             = t.getEmptyRowData();
                row[icls_cat]   = clsCat;
                row[icls_schem] = clsSchem;
                row[icls_name]  = clsName;
                row[igrantor]   = grantorName;
                row[igrantee]   = granteeName;
                row[iprivilege] = privilege;
                row[iis_grntbl] = isGrantable;

                t.insertSys(store, row);
            }

            classNames = ns.iterateAccessibleTriggerClassNames(grantee);
            privilege  = "TRIGGER";

            while (classNames.hasNext()) {
                clsName         = (String) classNames.next();
                row             = t.getEmptyRowData();
                row[icls_cat]   = clsCat;
                row[icls_schem] = clsSchem;
                row[icls_name]  = clsName;
                row[igrantor]   = grantorName;
                row[igrantee]   = granteeName;
                row[iprivilege] = privilege;
                row[iis_grntbl] = null;    // can't make a direct grant

                t.insertSys(store, row);
            }
        }

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the capabilities
     * and operating parameter properties for the engine hosting this
     * database, as well as their applicability in terms of scope and
     * name space. <p>
     *
     * Reported properties include certain predefined <code>Database</code>
     * properties file values as well as certain database scope
     * attributes. <p>
     *
     * It is intended that all <code>Database</code> attributes and
     * properties that can be set via the database properties file,
     * JDBC connection properties or SQL SET/ALTER statements will
     * eventually be reported here or, where more applicable, in an
     * ANSI/ISO conforming feature info base table in the defintion
     * schema. <p>
     *
     * Currently, the database properties reported are: <p>
     *
     * <OL>
     *     <LI>hsqldb.cache_file_scale - the scaling factor used to translate data and index structure file pointers
     *     <LI>hsqldb.cache_scale - base-2 exponent scaling allowable cache row count
     *     <LI>hsqldb.cache_size_scale - base-2 exponent scaling allowable cache byte count
     *     <LI>hsqldb.cache_version -
     *     <LI>hsqldb.catalogs - whether to report the database catalog (database uri)
     *     <LI>hsqldb.compatible_version -
     *     <LI>hsqldb.files_readonly - whether the database is in files_readonly mode
     *     <LI>hsqldb.gc_interval - # new records forcing gc ({0|NULL}=>never)
     *     <LI>hsqldb.max_nio_scale - scale factor for cache nio mapped buffers
     *     <LI>hsqldb.nio_data_file - whether cache uses nio mapped buffers
     *     <LI>hsqldb.original_version -
     *     <LI>sql.enforce_strict_size - column length specifications enforced strictly (raise exception on overflow)?
     *     <LI>textdb.all_quoted - default policy regarding whether to quote all character field values
     *     <LI>textdb.cache_scale - base-2 exponent scaling allowable cache row count
     *     <LI>textdb.cache_size_scale - base-2 exponent scaling allowable cache byte count
     *     <LI>textdb.encoding - default TEXT table file encoding
     *     <LI>textdb.fs - default field separator
     *     <LI>textdb.vs - default varchar field separator
     *     <LI>textdb.lvs - default long varchar field separator
     *     <LI>textdb.ignore_first - default policy regarding whether to ignore the first line
     *     <LI>textdb.quoted - default policy regarding treatement character field values that _may_ require quoting
     *     <LI>IGNORECASE - create table VARCHAR_IGNORECASE?
     *     <LI>LOGSIZSE - # bytes to which REDO log grows before auto-checkpoint
     *     <LI>REFERENTIAL_INTEGITY - currently enforcing referential integrity?
     *     <LI>SCRIPTFORMAT - 0 : TEXT, 1 : BINARY, ...
     *     <LI>WRITEDELAY - does REDO log currently use buffered write strategy?
     * </OL> <p>
     *
     * @return table describing database and session operating parameters
     *      and capabilities
     * @throws HsqlException if an error occurs while producing the table
     */
    Table SYSTEM_PROPERTIES() throws HsqlException {

        Table t = sysTables[SYSTEM_PROPERTIES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_PROPERTIES]);

            addColumn(t, "PROPERTY_SCOPE", CHARACTER_DATA);
            addColumn(t, "PROPERTY_NAMESPACE", CHARACTER_DATA);
            addColumn(t, "PROPERTY_NAME", CHARACTER_DATA);
            addColumn(t, "PROPERTY_VALUE", CHARACTER_DATA);
            addColumn(t, "PROPERTY_CLASS", CHARACTER_DATA);

            // order PROPERTY_SCOPE, PROPERTY_NAMESPACE, PROPERTY_NAME
            // true PK
            t.createPrimaryKey(null, new int[] {
                0, 1, 2
            }, true);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // calculated column values
        String scope;
        String nameSpace;

        // intermediate holders
        Object[]               row;
        HsqlDatabaseProperties props;

        // column number mappings
        final int iscope = 0;
        final int ins    = 1;
        final int iname  = 2;
        final int ivalue = 3;
        final int iclass = 4;

        // First, we want the names and values for
        // all JDBC capabilities constants
        scope     = "SESSION";
        props     = database.getProperties();
        nameSpace = "database.properties";

        // boolean properties
        Iterator it = props.getUserDefinedPropertyData().iterator();

        while (it.hasNext()) {
            Object[] metaData = (Object[]) it.next();

            row         = t.getEmptyRowData();
            row[iscope] = scope;
            row[ins]    = nameSpace;
            row[iname]  = metaData[HsqlProperties.indexName];
            row[ivalue] = props.getProperty((String) row[iname]);
            row[iclass] = metaData[HsqlProperties.indexClass];

            t.insertSys(store, row);
        }

        row         = t.getEmptyRowData();
        row[iscope] = scope;
        row[ins]    = nameSpace;
        row[iname]  = "SCRIPTFORMAT";

        try {
            row[ivalue] =
                ScriptWriterBase
                    .LIST_SCRIPT_FORMATS[database.logger.getScriptType()];
        } catch (Exception e) {}

        row[iclass] = "java.lang.String";

        t.insertSys(store, row);

        // write delay
        row         = t.getEmptyRowData();
        row[iscope] = scope;
        row[ins]    = nameSpace;
        row[iname]  = "WRITE_DELAY";
        row[ivalue] = "" + database.logger.getWriteDelay();
        row[iclass] = "int";

        t.insertSys(store, row);

        // ignore case
        row         = t.getEmptyRowData();
        row[iscope] = scope;
        row[ins]    = nameSpace;
        row[iname]  = "IGNORECASE";
        row[ivalue] = database.isIgnoreCase() ? "true"
                                              : "false";
        row[iclass] = "boolean";

        t.insertSys(store, row);

        // referential integrity
        row         = t.getEmptyRowData();
        row[iscope] = scope;
        row[ins]    = nameSpace;
        row[iname]  = "REFERENTIAL_INTEGRITY";
        row[ivalue] = database.isReferentialIntegrity() ? "true"
                                                        : "false";
        row[iclass] = "boolean";

        t.insertSys(store, row);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing attributes
     * for the calling session context.<p>
     *
     * The rows report the following {key,value} pairs:<p>
     *
     * <pre class="SqlCodeExample">
     * KEY (VARCHAR)       VALUE (VARCHAR)
     * ------------------- ---------------
     * SESSION_ID          the id of the calling session
     * AUTOCOMMIT          YES: session is in autocommit mode, else NO
     * USER                the name of user connected in the calling session
     * (was READ_ONLY)
     * SESSION_READONLY    TRUE: session is in read-only mode, else FALSE
     * (new)
     * DATABASE_READONLY   TRUE: database is in read-only mode, else FALSE
     * MAXROWS             the MAXROWS setting in the calling session
     * DATABASE            the name of the database
     * IDENTITY            the last identity value used by calling session
     * </pre>
     *
     * <b>Note:</b>  This table <em>may</em> become deprecated in a future
     * release, as the information it reports now duplicates information
     * reported in the newer SYSTEM_SESSIONS and SYSTEM_PROPERTIES
     * tables. <p>
     *
     * @return a <code>Table</code> object describing the
     *        attributes of the connection associated
     *        with the current execution context
     * @throws HsqlException if an error occurs while producing the table
     */
    Table SYSTEM_SESSIONINFO() throws HsqlException {

        Table t = sysTables[SYSTEM_SESSIONINFO];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_SESSIONINFO]);

            addColumn(t, "KEY", CHARACTER_DATA);      // not null
            addColumn(t, "VALUE", CHARACTER_DATA);    // not null
            t.createPrimaryKey();

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());
        Object[] row;

        row    = t.getEmptyRowData();
        row[0] = "SESSION_ID";
        row[1] = String.valueOf(session.getId());

        t.insertSys(store, row);

        row    = t.getEmptyRowData();
        row[0] = "AUTOCOMMIT";
        row[1] = session.isAutoCommit() ? "TRUE"
                                        : "FALSE";

        t.insertSys(store, row);

        row    = t.getEmptyRowData();
        row[0] = "USER";
        row[1] = session.getUsername();

        t.insertSys(store, row);

        row    = t.getEmptyRowData();
        row[0] = "SESSION_READONLY";
        row[1] = session.isReadOnlyDefault() ? "TRUE"
                                             : "FALSE";

        t.insertSys(store, row);

        row    = t.getEmptyRowData();
        row[0] = "DATABASE_READONLY";
        row[1] = database.isReadOnly() ? "TRUE"
                                       : "FALSE";

        t.insertSys(store, row);

        // fredt - value set by SET MAXROWS in SQL, not Statement.setMaxRows()
        row    = t.getEmptyRowData();
        row[0] = "MAXROWS";
        row[1] = String.valueOf(session.getSQLMaxRows());

        t.insertSys(store, row);

        row    = t.getEmptyRowData();
        row[0] = "DATABASE";
        row[1] = database.getURI();

        t.insertSys(store, row);

        row    = t.getEmptyRowData();
        row[0] = "IDENTITY";
        row[1] = String.valueOf(session.getLastIdentity());

        t.insertSys(store, row);

        row    = t.getEmptyRowData();
        row[0] = "SCHEMA";
        row[1] = String.valueOf(session.getSchemaName(null));

        t.insertSys(store, row);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing all visible
     * sessions. ADMIN users see *all* sessions
     * while non-admin users see only their own session.<p>
     *
     * Each row is a session state description with the following columns: <p>
     *
     * <pre class="SqlCodeExample">
     * SESSION_ID         INTEGER   session identifier
     * CONNECTED          TIMESTAMP time at which session was created
     * USER_NAME          VARCHAR   db user name of current session user
     * IS_ADMIN           BOOLEAN   is session user an admin user?
     * AUTOCOMMIT         BOOLEAN   is session in autocommit mode?
     * READONLY           BOOLEAN   is session in read-only mode?
     * MAXROWS            INTEGER   session's MAXROWS setting
     * LAST_IDENTITY      INTEGER   last identity value used by this session
     * TRANSACTION_SIZE   INTEGER   # of undo items in current transaction
     * SCHEMA             VARCHAR   current schema for session
     * </pre> <p>
     *
     * @return a <code>Table</code> object describing all visible
     *      sessions
     * @throws HsqlException if an error occurs while producing the table
     */
    Table SYSTEM_SESSIONS() throws HsqlException {

        Table t = sysTables[SYSTEM_SESSIONS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_SESSIONS]);

            addColumn(t, "SESSION_ID", CARDINAL_NUMBER);
            addColumn(t, "CONNECTED", TIME_STAMP);
            addColumn(t, "USER_NAME", SQL_IDENTIFIER);
            addColumn(t, "IS_ADMIN", Type.SQL_BOOLEAN);
            addColumn(t, "AUTOCOMMIT", Type.SQL_BOOLEAN);
            addColumn(t, "READONLY", Type.SQL_BOOLEAN);
            addColumn(t, "MAXROWS", CARDINAL_NUMBER);

            // Note: some sessions may have a NULL LAST_IDENTITY value
            addColumn(t, "LAST_IDENTITY", CARDINAL_NUMBER);
            addColumn(t, "TRANSACTION_SIZE", CARDINAL_NUMBER);
            addColumn(t, "SCHEMA", SQL_IDENTIFIER);

            // order:  SESSION_ID
            // true primary key
            t.createPrimaryKey(null, new int[]{ 0 }, true);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // intermediate holders
        Session[] sessions;
        Session   s;
        Object[]  row;

        // column number mappings
        final int isid      = 0;
        final int ict       = 1;
        final int iuname    = 2;
        final int iis_admin = 3;
        final int iautocmt  = 4;
        final int ireadonly = 5;
        final int imaxrows  = 6;
        final int ilast_id  = 7;
        final int it_size   = 8;
        final int it_schema = 9;

        // Initialisation
        sessions = ns.listVisibleSessions(session);

        // Do it.
        for (int i = 0; i < sessions.length; i++) {
            s              = sessions[i];
            row            = t.getEmptyRowData();
            row[isid]      = ValuePool.getLong(s.getId());
            row[ict]       = new TimestampData(s.getConnectTime() / 1000);
            row[iuname]    = s.getUsername();
            row[iis_admin] = ValuePool.getBoolean(s.isAdmin());
            row[iautocmt]  = ValuePool.getBoolean(s.isAutoCommit());
            row[ireadonly] = ValuePool.getBoolean(s.isReadOnlyDefault());
            row[imaxrows]  = ValuePool.getInt(s.getSQLMaxRows());
            row[ilast_id] =
                ValuePool.getLong(((Number) s.getLastIdentity()).longValue());
            row[it_size]   = ValuePool.getInt(s.getTransactionSize());
            row[it_schema] = s.getCurrentSchemaHsqlName().name;

            t.insertSys(store, row);
        }

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * direct super table (if any) of each accessible table defined
     * within this database. <p>
     *
     * Each row is a super table description with the following columns: <p>
     *
     * <pre class="SqlCodeExample">
     * TABLE_CATALOG       VARCHAR   the table's catalog
     * TABLE_SCHEMA     VARCHAR   table schema
     * TABLE_NAME      VARCHAR   table name
     * SUPERTABLE_NAME VARCHAR   the direct super table's name
     * </pre> <p>
     * @return a <code>Table</code> object describing the accessible
     *        direct supertable (if any) of each accessible
     *        table defined within this database
     * @throws HsqlException if an error occurs while producing the table
     */
    Table SYSTEM_SUPERTABLES() throws HsqlException {

        Table t = sysTables[SYSTEM_SUPERTABLES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_SUPERTABLES]);

            addColumn(t, "TABLE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TABLE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TABLE_NAME", SQL_IDENTIFIER);         // not null
            addColumn(t, "SUPERTABLE_NAME", SQL_IDENTIFIER);    // not null
            t.createPrimaryKey();

            return t;
        }

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * direct super type (if any) of each accessible user-defined type (UDT)
     * defined within this database. <p>
     *
     * Each row is a super type description with the following columns: <p>
     *
     * <pre class="SqlCodeExample">
     * TYPE_CATALOG        VARCHAR   the UDT's catalog
     * TYPE_SCHEMA      VARCHAR   UDT's schema
     * TYPE_NAME       VARCHAR   type name of the UDT
     * SUPERTYPE_CATALOG   VARCHAR   the direct super type's catalog
     * SUPERTYPE_SCHEMA VARCHAR   the direct super type's schema
     * SUPERTYPE_NAME  VARCHAR   the direct super type's name
     * </pre> <p>
     * @return a <code>Table</code> object describing the accessible
     *        direct supertype (if any) of each accessible
     *        user-defined type (UDT) defined within this database
     * @throws HsqlException if an error occurs while producing the table
     */
    Table SYSTEM_SUPERTYPES() throws HsqlException {

        Table t = sysTables[SYSTEM_SUPERTYPES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_SUPERTYPES]);

            addColumn(t, "USER_DEFINED_TYPE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "USER_DEFINED_TYPE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "USER_DEFINED_TYPE_NAME", SQL_IDENTIFIER);    // not null
            addColumn(t, "SUPERTYPE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "SUPERTYPE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "SUPERTYPE_NAME", SQL_IDENTIFIER);            // not null
            t.createPrimaryKey();

            return t;
        }

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the TEXT TABLE objects
     * defined within this database. The table contains one row for each row
     * in the SYSTEM_TABLES table with a HSQLDB_TYPE of  TEXT . <p>
     *
     * Each row is a description of the attributes that defines its TEXT TABLE,
     * with the following columns:
     *
     * <pre class="SqlCodeExample">
     * TABLE_CAT                 VARCHAR   table's catalog name
     * TABLE_SCHEM               VARCHAR   table's simple schema name
     * TABLE_NAME                VARCHAR   table's simple name
     * DATA_SOURCE_DEFINITION    VARCHAR   the "spec" proption of the table's
     *                                     SET TABLE ... SOURCE DDL declaration
     * FILE_PATH                 VARCHAR   absolute file path.
     * FILE_ENCODING             VARCHAR   endcoding of table's text file
     * FIELD_SEPARATOR           VARCHAR   default field separator
     * VARCHAR_SEPARATOR         VARCAHR   varchar field separator
     * LONGVARCHAR_SEPARATOR     VARCHAR   longvarchar field separator
     * IS_IGNORE_FIRST           BOOLEAN   ignores first line of file?
     * IS_QUOTED                 BOOLEAN   fields are quoted if necessary?
     * IS_ALL_QUOTED             BOOLEAN   all fields are quoted?
     * IS_DESC                   BOOLEAN   read rows starting at end of file?
     * </pre> <p>
     *
     * @return a <code>Table</code> object describing the text attributes
     * of the accessible text tables defined within this database
     * @throws HsqlException if an error occurs while producing the table
     *
     */
    Table SYSTEM_TEXTTABLES() throws HsqlException {

        Table t = sysTables[SYSTEM_TEXTTABLES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_TEXTTABLES]);

            addColumn(t, "TABLE_CAT", SQL_IDENTIFIER);
            addColumn(t, "TABLE_SCHEM", SQL_IDENTIFIER);
            addColumn(t, "TABLE_NAME", SQL_IDENTIFIER);    // not null
            addColumn(t, "DATA_SOURCE_DEFINTION", CHARACTER_DATA);
            addColumn(t, "FILE_PATH", CHARACTER_DATA);
            addColumn(t, "FILE_ENCODING", CHARACTER_DATA);
            addColumn(t, "FIELD_SEPARATOR", CHARACTER_DATA);
            addColumn(t, "VARCHAR_SEPARATOR", CHARACTER_DATA);
            addColumn(t, "LONGVARCHAR_SEPARATOR", CHARACTER_DATA);
            addColumn(t, "IS_IGNORE_FIRST", Type.SQL_BOOLEAN);
            addColumn(t, "IS_ALL_QUOTED", Type.SQL_BOOLEAN);
            addColumn(t, "IS_QUOTED", Type.SQL_BOOLEAN);
            addColumn(t, "IS_DESC", Type.SQL_BOOLEAN);

            // ------------------------------------------------------------
            t.createPrimaryKey();

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // intermediate holders
        Iterator tables;
        Table    table;
        Object[] row;

//        DITableInfo ti;
        TextCache tc;

        // column number mappings
        final int itable_cat   = 0;
        final int itable_schem = 1;
        final int itable_name  = 2;
        final int idsd         = 3;
        final int ifile_path   = 4;
        final int ifile_enc    = 5;
        final int ifs          = 6;
        final int ivfs         = 7;
        final int ilvfs        = 8;
        final int iif          = 9;
        final int iiq          = 10;
        final int iiaq         = 11;
        final int iid          = 12;

        // Initialization
        tables =
            database.schemaManager.databaseObjectIterator(SchemaObject.TABLE);

        // Do it.
        while (tables.hasNext()) {
            table = (Table) tables.next();

            PersistentStore currentStore =
                database.persistentStoreCollection.getStore(
                    t.getPersistenceId());

            if (!table.isText() || !isAccessibleTable(table)) {
                continue;
            }

            row               = t.getEmptyRowData();
            row[itable_cat]   = database.getCatalogName().name;
            row[itable_schem] = table.getSchemaName().name;
            row[itable_name]  = table.getName().name;
            row[idsd]         = ((TextTable) table).getDataSource();

            TextCache cache = (TextCache) currentStore.getCache();

            if (cache != null) {
                row[ifile_path] =
                    FileUtil.getDefaultInstance().canonicalOrAbsolutePath(
                        cache.getFileName());
                row[ifile_enc] = cache.stringEncoding;
                row[ifs]       = cache.fs;
                row[ivfs]      = cache.vs;
                row[ilvfs]     = cache.lvs;
                row[iif]       = ValuePool.getBoolean(cache.ignoreFirst);
                row[iiq]       = ValuePool.getBoolean(cache.isQuoted);
                row[iiaq]      = ValuePool.getBoolean(cache.isAllQuoted);
                row[iid] = ((TextTable) table).isDescDataSource()
                           ? Boolean.TRUE
                           : Boolean.FALSE;
            }

            t.insertSys(store, row);
        }

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the usage
     * of accessible columns in accessible triggers defined within
     * the database. <p>
     *
     * Each column usage description has the following columns: <p>
     *
     * <pre class="SqlCodeExample">
     * TRIGGER_CAT   VARCHAR   Trigger catalog.
     * TRIGGER_SCHEM VARCHAR   Trigger schema.
     * TRIGGER_NAME  VARCHAR   Trigger name.
     * TABLE_CAT     VARCHAR   Catalog of table on which the trigger is defined.
     * TABLE_SCHEM   VARCHAR   Schema of table on which the trigger is defined.
     * TABLE_NAME    VARCHAR   Table on which the trigger is defined.
     * COLUMN_NAME   VARCHAR   Name of the column used in the trigger.
     * COLUMN_LIST   VARCHAR   Specified in UPDATE clause?: ("Y" | "N"}
     * COLUMN_USAGE  VARCHAR   {"NEW" | "OLD" | "IN" | "OUT" | "IN OUT"}
     * </pre> <p>
     * @return a <code>Table</code> object describing of the usage
     *        of accessible columns in accessible triggers
     *        defined within this database
     * @throws HsqlException if an error occurs while producing the table
     */
    Table SYSTEM_TRIGGERCOLUMNS() throws HsqlException {

        Table t = sysTables[SYSTEM_TRIGGERCOLUMNS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_TRIGGERCOLUMNS]);

            addColumn(t, "TRIGGER_CAT", SQL_IDENTIFIER);
            addColumn(t, "TRIGGER_SCHEM", SQL_IDENTIFIER);
            addColumn(t, "TRIGGER_NAME", SQL_IDENTIFIER);
            addColumn(t, "TABLE_CAT", SQL_IDENTIFIER);
            addColumn(t, "TABLE_SCHEM", SQL_IDENTIFIER);
            addColumn(t, "TABLE_NAME", SQL_IDENTIFIER);
            addColumn(t, "COLUMN_NAME", SQL_IDENTIFIER);
            addColumn(t, "COLUMN_LIST", CHARACTER_DATA);
            addColumn(t, "COLUMN_USAGE", CHARACTER_DATA);

            // order:  all columns, in order, as each column
            // of each table may eventually be listed under various capacities
            // (when a more comprehensive trugger system is put in place)
            // false PK, as cat and schem may be null
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 3, 4, 5, 6, 7, 8
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());
        Session sys = database.sessionManager.newSysSession(
            SqlInvariants.INFORMATION_SCHEMA_HSQLNAME, session.getUser());
        Result rs = sys.executeDirectStatement(
            "select a.TRIGGER_CAT,a.TRIGGER_SCHEM,a.TRIGGER_NAME, "
            + "a.TABLE_CAT,a.TABLE_SCHEM,a.TABLE_NAME,b.COLUMN_NAME,'Y',"
            + "'IN' from INFORMATION_SCHEMA.SYSTEM_TRIGGERS a, "
            + "INFORMATION_SCHEMA.SYSTEM_COLUMNS b where "
            + "a.TABLE_NAME=b.TABLE_NAME and a.TABLE_SCHEM=b.TABLE_SCHEM");

/*
            // - used appends to make class file constant pool smaller
            // - saves ~ 100 bytes jar space
            (new StringBuffer(185)).append("SELECT").append(' ').append(
                "a.").append("TRIGGER_CAT").append(',').append("a.").append(
                "TRIGGER_SCHEM").append(',').append("a.").append(
                "TRIGGER_NAME").append(',').append("a.").append(
                "TABLE_CAT").append(',').append("a.").append(
                "TABLE_SCHEM").append(',').append("a.").append(
                "TABLE_NAME").append(',').append("b.").append(
                "COLUMN_NAME").append(',').append("'Y'").append(',').append(
                "'IN'").append(' ').append("from").append(' ').append(
                "INFORMATION_SCHEMA").append('.').append(
                "SYSTEM_TRIGGERS").append(" a,").append(
                "INFORMATION_SCHEMA").append('.').append(
                "SYSTEM_COLUMNS").append(" b ").append("where").append(
                ' ').append("a.").append("TABLE_NAME").append('=').append(
                "b.").append("TABLE_NAME").toString();
*/
        t.insertSys(store, rs);
        sys.close();

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * triggers defined within the database. <p>
     *
     * Each row is a trigger description with the following columns: <p>
     *
     * <pre class="SqlCodeExample">
     * TRIGGER_CAT       VARCHAR   Trigger catalog.
     * TRIGGER_SCHEM     VARCHAR   Trigger Schema.
     * TRIGGER_NAME      VARCHAR   Trigger Name.
     * TRIGGER_TYPE      VARCHAR   {("BEFORE" | "AFTER") + [" EACH ROW"] }
     * TRIGGERING_EVENT  VARCHAR   {"INSERT" | "UPDATE" | "DELETE"}
     *                             (future?: "INSTEAD OF " + ("SELECT" | ...))
     * TABLE_CAT         VARCHAR   Table's catalog.
     * TABLE_SCHEM       VARCHAR   Table's schema.
     * BASE_OBJECT_TYPE  VARCHAR   "TABLE"
     *                             (future?: "VIEW" | "SCHEMA" | "DATABASE")
     * TABLE_NAME        VARCHAR   Table on which trigger is defined
     * COLUMN_NAME       VARCHAR   NULL (future?: nested table column name)
     * REFERENCING_NAMES VARCHAR   ROW, OLD, NEW, etc.
     * WHEN_CLAUSE       VARCHAR   Condition firing trigger (NULL => always)
     * STATUS            VARCHAR   {"ENABLED" | "DISABLED"}
     * DESCRIPTION       VARCHAR   typically, the trigger's DDL
     * ACTION_TYPE       VARCHAR   "CALL" (future?: embedded language name)
     * TRIGGER_BODY      VARCHAR   Statement(s) executed
     * </pre> <p>
     *
     * @return a <code>Table</code> object describing the accessible
     *    triggers defined within this database.
     * @throws HsqlException if an error occurs while producing the table
     */
    Table SYSTEM_TRIGGERS() throws HsqlException {

        Table t = sysTables[SYSTEM_TRIGGERS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_TRIGGERS]);

            addColumn(t, "TRIGGER_CAT", SQL_IDENTIFIER);
            addColumn(t, "TRIGGER_SCHEM", SQL_IDENTIFIER);
            addColumn(t, "TRIGGER_NAME", SQL_IDENTIFIER);
            addColumn(t, "TRIGGER_TYPE", CHARACTER_DATA);
            addColumn(t, "TRIGGERING_EVENT", CHARACTER_DATA);
            addColumn(t, "TABLE_CAT", SQL_IDENTIFIER);
            addColumn(t, "TABLE_SCHEM", SQL_IDENTIFIER);
            addColumn(t, "BASE_OBJECT_TYPE", CHARACTER_DATA);
            addColumn(t, "TABLE_NAME", SQL_IDENTIFIER);
            addColumn(t, "COLUMN_NAME", SQL_IDENTIFIER);
            addColumn(t, "REFERENCING_NAMES", CHARACTER_DATA);
            addColumn(t, "WHEN_CLAUSE", CHARACTER_DATA);
            addColumn(t, "STATUS", CHARACTER_DATA);
            addColumn(t, "DESCRIPTION", CHARACTER_DATA);
            addColumn(t, "ACTION_TYPE", CHARACTER_DATA);
            addColumn(t, "TRIGGER_BODY", CHARACTER_DATA);

            // order: TRIGGER_TYPE, TRIGGER_SCHEM, TRIGGER_NAME
            // added for unique: TRIGGER_CAT
            // false PK, as TRIGGER_SCHEM and/or TRIGGER_CAT may be null
            t.createPrimaryKey(null, new int[] {
                3, 1, 2, 0
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // calculated column values
        String triggerCatalog;
        String triggerSchema;
        String triggerName;
        String triggerType;
        String triggeringEvent;
        String tableCatalog;
        String tableSchema;
        String baseObjectType;
        String tableName;
        String columnName;
        String referencingNames;
        String whenClause;
        String status;
        String description;
        String actionType;
        String triggerBody;

        // Intermediate holders
        Iterator     tables;
        Table        table;
        TriggerDef[] triggerList;
        TriggerDef   def;
        Object[]     row;

        // column number mappings
        final int itrigger_cat       = 0;
        final int itrigger_schem     = 1;
        final int itrigger_name      = 2;
        final int itrigger_type      = 3;
        final int itriggering_event  = 4;
        final int itable_cat         = 5;
        final int itable_schem       = 6;
        final int ibase_object_type  = 7;
        final int itable_name        = 8;
        final int icolumn_name       = 9;
        final int ireferencing_names = 10;
        final int iwhen_clause       = 11;
        final int istatus            = 12;
        final int idescription       = 13;
        final int iaction_type       = 14;
        final int itrigger_body      = 15;

        // Initialization
        tables =
            database.schemaManager.databaseObjectIterator(SchemaObject.TABLE);

        // these are the only values supported, currently
        actionType       = "CALL";
        baseObjectType   = "TABLE";
        columnName       = null;
        referencingNames = "ROW";
        whenClause       = null;

        // Do it.
        while (tables.hasNext()) {
            table       = (Table) tables.next();
            triggerList = table.getTriggers();

            // faster test first
            if (triggerList == null) {
                continue;
            }

            if (!session.getGrantee().isFullyAccessibleByRole(table)) {
                continue;
            }

            tableCatalog   = database.getCatalogName().name;
            triggerCatalog = tableCatalog;
            tableSchema    = table.getSchemaName().name;
            triggerSchema  = tableSchema;
            tableName      = table.getName().name;

            for (int j = 0; j < triggerList.length; j++) {
                def         = (TriggerDef) triggerList[j];
                triggerName = def.getName().name;
                description = def.getDDL();
                status      = def.isValid() ? "ENABLED"
                                            : "DISABLED";
                triggerBody = def.getClassName();
                triggerType = def.getWhenClause();

                if (def.isForEachRow()) {
                    triggerType += " EACH ROW";
                }

                triggeringEvent         = def.getOperationClause();
                row                     = t.getEmptyRowData();
                row[itrigger_cat]       = triggerCatalog;
                row[itrigger_schem]     = triggerSchema;
                row[itrigger_name]      = triggerName;
                row[itrigger_type]      = triggerType;
                row[itriggering_event]  = triggeringEvent;
                row[itable_cat]         = tableCatalog;
                row[itable_schem]       = tableSchema;
                row[ibase_object_type]  = baseObjectType;
                row[itable_name]        = tableName;
                row[icolumn_name]       = columnName;
                row[ireferencing_names] = referencingNames;
                row[iwhen_clause]       = whenClause;
                row[istatus]            = status;
                row[idescription]       = description;
                row[iaction_type]       = actionType;
                row[itrigger_body]      = triggerBody;

                t.insertSys(store, row);
            }
        }

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * attributes of the accessible user-defined type (UDT) objects
     * defined within this database. <p>
     *
     * This description does not contain inherited attributes. <p>
     *
     * Each row is a user-defined type attributes description with the
     * following columns:
     *
     * <pre class="SqlCodeExample">
     * TYPE_CAT          VARCHAR   type catalog
     * TYPE_SCHEM        VARCHAR   type schema
     * TYPE_NAME         VARCHAR   type name
     * ATTR_NAME         VARCHAR   attribute name
     * DATA_TYPE         SMALLINT  attribute's SQL type from DITypes
     * ATTR_TYPE_NAME    VARCHAR   UDT: fully qualified type name
     *                            REF: fully qualified type name of target type of
     *                            the reference type.
     * ATTR_SIZE         INTEGER   column size.
     *                            char or date types => maximum number of characters;
     *                            numeric or decimal types => precision.
     * DECIMAL_DIGITS    INTEGER   # of fractional digits (scale) of number type
     * NUM_PREC_RADIX    INTEGER   Radix of number type
     * NULLABLE          INTEGER   whether NULL is allowed
     * REMARKS           VARCHAR   comment describing attribute
     * ATTR_DEF          VARCHAR   default attribute value
     * SQL_DATA_TYPE     INTEGER   expected value of SQL CLI SQL_DESC_TYPE in the SQLDA
     * SQL_DATETIME_SUB  INTEGER   DATETIME/INTERVAL => datetime/interval subcode
     * CHAR_OCTET_LENGTH INTEGER   for char types:  max bytes in column
     * ORDINAL_POSITION  INTEGER   index of column in table (starting at 1)
     * IS_NULLABLE       VARCHAR   "NO" => strictly no NULL values;
     *                             "YES" => maybe NULL values;
     *                             "" => unknown.
     * SCOPE_CATALOG     VARCHAR   catalog of REF attribute scope table or NULL
     * SCOPE_SCHEMA      VARCHAR   schema of REF attribute scope table or NULL
     * SCOPE_TABLE       VARCHAR   name of REF attribute scope table or NULL
     * SOURCE_DATA_TYPE  SMALLINT  For DISTINCT or user-generated REF DATA_TYPE:
     *                            source SQL type from DITypes
     *                            For other DATA_TYPE values:  NULL
     * </pre>
     *
     * <B>Note:</B> Currently, neither the HSQLDB engine or the JDBC driver
     * support UDTs, so an empty table is returned. <p>
     * @return a <code>Table</code> object describing the accessible
     *        attrubutes of the accessible user-defined type
     *        (UDT) objects defined within this database
     * @throws HsqlException if an error occurs while producing the table
     */
    Table SYSTEM_UDTATTRIBUTES() throws HsqlException {

        Table t = sysTables[SYSTEM_UDTATTRIBUTES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_UDTATTRIBUTES]);

            addColumn(t, "TYPE_CAT", SQL_IDENTIFIER);
            addColumn(t, "TYPE_SCHEM", SQL_IDENTIFIER);
            addColumn(t, "TYPE_NAME", SQL_IDENTIFIER);             // not null
            addColumn(t, "ATTR_NAME", SQL_IDENTIFIER);             // not null
            addColumn(t, "DATA_TYPE", Type.SQL_SMALLINT);          // not null
            addColumn(t, "ATTR_TYPE_NAME", SQL_IDENTIFIER);        // not null
            addColumn(t, "ATTR_SIZE", Type.SQL_INTEGER);
            addColumn(t, "DECIMAL_DIGITS", Type.SQL_INTEGER);
            addColumn(t, "NUM_PREC_RADIX", Type.SQL_INTEGER);
            addColumn(t, "NULLABLE", Type.SQL_INTEGER);
            addColumn(t, "REMARKS", CHARACTER_DATA);
            addColumn(t, "ATTR_DEF", CHARACTER_DATA);
            addColumn(t, "SQL_DATA_TYPE", Type.SQL_INTEGER);
            addColumn(t, "SQL_DATETIME_SUB", Type.SQL_INTEGER);
            addColumn(t, "CHAR_OCTET_LENGTH", Type.SQL_INTEGER);
            addColumn(t, "ORDINAL_POSITION", Type.SQL_INTEGER);    // not null
            addColumn(t, "IS_NULLABLE", YES_OR_NO);                // not null
            addColumn(t, "SCOPE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "SCOPE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "SCOPE_TABLE", SQL_IDENTIFIER);
            addColumn(t, "SOURCE_DATA_TYPE", Type.SQL_SMALLINT);
            t.createPrimaryKey();

            return t;
        }

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * user-defined types defined in this database. <p>
     *
     * Schema-specific UDTs may have type JAVA_OBJECT, STRUCT, or DISTINCT.
     *
     * <P>Each row is a UDT descripion with the following columns:
     * <OL>
     *   <LI><B>TYPE_CAT</B> <code>VARCHAR</code> => the type's catalog
     *   <LI><B>TYPE_SCHEM</B> <code>VARCHAR</code> => type's schema
     *   <LI><B>TYPE_NAME</B> <code>VARCHAR</code> => type name
     *   <LI><B>CLASS_NAME</B> <code>VARCHAR</code> => Java class name
     *   <LI><B>DATA_TYPE</B> <code>VARCHAR</code> =>
     *         type value defined in <code>DITypes</code>;
     *         one of <code>JAVA_OBJECT</code>, <code>STRUCT</code>, or
     *        <code>DISTINCT</code>
     *   <LI><B>REMARKS</B> <code>VARCHAR</code> =>
     *          explanatory comment on the type
     *   <LI><B>BASE_TYPE</B><code>SMALLINT</code> =>
     *          type code of the source type of a DISTINCT type or the
     *          type that implements the user-generated reference type of the
     *          SELF_REFERENCING_COLUMN of a structured type as defined in
     *          DITypes (null if DATA_TYPE is not DISTINCT or not
     *          STRUCT with REFERENCE_GENERATION = USER_DEFINED)
     *
     * </OL> <p>
     *
     * <B>Note:</B> Currently, neither the HSQLDB engine or the JDBC driver
     * support UDTs, so an empty table is returned. <p>
     *
     * @return a <code>Table</code> object describing the accessible
     *      user-defined types defined in this database
     * @throws HsqlException if an error occurs while producing the table
     */
    Table SYSTEM_UDTS() throws HsqlException {

        Table t = sysTables[SYSTEM_UDTS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_UDTS]);

            addColumn(t, "TYPE_CAT", SQL_IDENTIFIER);
            addColumn(t, "TYPE_SCHEM", SQL_IDENTIFIER);
            addColumn(t, "TYPE_NAME", SQL_IDENTIFIER);     // not null
            addColumn(t, "CLASS_NAME", CHARACTER_DATA);    // not null
            addColumn(t, "DATA_TYPE", SQL_IDENTIFIER);     // not null
            addColumn(t, "REMARKS", CHARACTER_DATA);
            addColumn(t, "BASE_TYPE", Type.SQL_SMALLINT);
            t.createPrimaryKey();

            return t;
        }

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * columns that are automatically updated when any value in a row
     * is updated. <p>
     *
     * Each row is a version column description with the following columns: <p>
     *
     * <OL>
     * <LI><B>SCOPE</B> <code>SMALLINT</code> => is not used
     * <LI><B>COLUMN_NAME</B> <code>VARCHAR</code> => column name
     * <LI><B>DATA_TYPE</B> <code>SMALLINT</code> =>
     *        SQL data type from java.sql.Types
     * <LI><B>TYPE_NAME</B> <code>SMALLINT</code> =>
     *       Data source dependent type name
     * <LI><B>COLUMN_SIZE</B> <code>INTEGER</code> => precision
     * <LI><B>BUFFER_LENGTH</B> <code>INTEGER</code> =>
     *        length of column value in bytes
     * <LI><B>DECIMAL_DIGITS</B> <code>SMALLINT</code> => scale
     * <LI><B>PSEUDO_COLUMN</B> <code>SMALLINT</code> =>
     *        is this a pseudo column like an Oracle <code>ROWID</code>:<BR>
     *        (as defined in <code>java.sql.DatabaseMetadata</code>)
     * <UL>
     *    <LI><code>versionColumnUnknown</code> - may or may not be
     *        pseudo column
     *    <LI><code>versionColumnNotPseudo</code> - is NOT a pseudo column
     *    <LI><code>versionColumnPseudo</code> - is a pseudo column
     * </UL>
     * </OL> <p>
     *
     * <B>Note:</B> Currently, the HSQLDB engine does not support version
     * columns, so an empty table is returned. <p>
     *
     * @return a <code>Table</code> object describing the columns
     *        that are automatically updated when any value
     *        in a row is updated
     * @throws HsqlException if an error occurs while producing the table
     */
    Table SYSTEM_VERSIONCOLUMNS() throws HsqlException {

        Table t = sysTables[SYSTEM_VERSIONCOLUMNS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_VERSIONCOLUMNS]);

            // ----------------------------------------------------------------
            // required by DatabaseMetaData.getVersionColumns result set
            // ----------------------------------------------------------------
            addColumn(t, "SCOPE", Type.SQL_INTEGER);
            addColumn(t, "COLUMN_NAME", SQL_IDENTIFIER);         // not null
            addColumn(t, "DATA_TYPE", Type.SQL_SMALLINT);        // not null
            addColumn(t, "TYPE_NAME", SQL_IDENTIFIER);           // not null
            addColumn(t, "COLUMN_SIZE", Type.SQL_SMALLINT);
            addColumn(t, "BUFFER_LENGTH", Type.SQL_INTEGER);
            addColumn(t, "DECIMAL_DIGITS", Type.SQL_SMALLINT);
            addColumn(t, "PSEUDO_COLUMN", Type.SQL_SMALLINT);    // not null

            // -----------------------------------------------------------------
            // required by DatabaseMetaData.getVersionColumns filter parameters
            // -----------------------------------------------------------------
            addColumn(t, "TABLE_CAT", SQL_IDENTIFIER);
            addColumn(t, "TABLE_SCHEM", SQL_IDENTIFIER);
            addColumn(t, "TABLE_NAME", SQL_IDENTIFIER);          // not null

            // -----------------------------------------------------------------
            t.createPrimaryKey();

            return t;
        }

        return t;
    }

//------------------------------------------------------------------------------
// SQL SCHEMATA VIEWS

    /**
     * Returns roles that are grantable by an admin user, which means all the
     * roles
     *
     * @throws HsqlException
     * @return Table
     */
    Table ADMINISTRABLE_ROLE_AUTHORIZATIONS() throws HsqlException {

        Table t = sysTables[ADMINISTRABLE_ROLE_AUTHORIZATIONS];

        if (t == null) {
            t = createBlankTable(
                sysTableHsqlNames[ADMINISTRABLE_ROLE_AUTHORIZATIONS]);

            addColumn(t, "GRANTEE", SQL_IDENTIFIER);
            addColumn(t, "ROLE_NAME", SQL_IDENTIFIER);
            addColumn(t, "IS_GRANTABLE", SQL_IDENTIFIER);
            t.createPrimaryKey(null, new int[] {
                0, 1, 2
            }, false);

            return t;
        }

        if (session.isAdmin()) {
            insertRoles(t, session.getGrantee(), true);
        }

        return t;
    }

    /**
     * Returns current user's roles.
     *
     * @throws HsqlException
     * @return Table
     */
    Table APPLICABLE_ROLES() throws HsqlException {

        Table t = sysTables[APPLICABLE_ROLES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[APPLICABLE_ROLES]);

            addColumn(t, "GRANTEE", SQL_IDENTIFIER);
            addColumn(t, "ROLE_NAME", SQL_IDENTIFIER);
            addColumn(t, "IS_GRANTABLE", SQL_IDENTIFIER);
            t.createPrimaryKey(null, new int[] {
                0, 1, 2
            }, false);

            return t;
        }

        insertRoles(t, session.getGrantee(), session.isAdmin());

        return t;
    }

    private void insertRoles(Table t, Grantee role,
                             boolean isGrantable) throws HsqlException {

        final int grantee      = 0;
        final int role_name    = 1;
        final int is_grantable = 2;
        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        if (isGrantable) {
            Set      roles = database.getGranteeManager().getRoleNames();
            Iterator it    = roles.iterator();

            while (it.hasNext()) {
                String   roleName = (String) it.next();
                Object[] row      = t.getEmptyRowData();

                row[grantee]      = role.getNameString();
                row[role_name]    = roleName;
                row[is_grantable] = "YES";

                t.insertSys(store, row);
            }
        } else {
            OrderedHashSet roles = role.getDirectRoles();

            for (int i = 0; i < roles.size(); i++) {
                String   roleName = (String) roles.get(i);
                Object[] row      = t.getEmptyRowData();

                row[grantee]      = role.getNameString();
                row[role_name]    = roleName;
                row[is_grantable] = Tokens.T_NO;

                t.insertSys(store, row);

                role = database.getGranteeManager().getRole(roleName);

                insertRoles(t, role, isGrantable);
            }
        }
    }

    Table ASSERTIONS() throws HsqlException {

        Table t = sysTables[ASSERTIONS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[ASSERTIONS]);

            addColumn(t, "CONSTRAINT_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "CONSTRAINT_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "CONSTRAINT_NAME", SQL_IDENTIFIER);    // not null
            addColumn(t, "IS_DEFERRABLE", YES_OR_NO);
            addColumn(t, "INITIALLY_DEFERRED", YES_OR_NO);
            t.createPrimaryKey(null, new int[] {
                0, 1, 2
            }, false);

            return t;
        }

        final int constraint_catalog = 0;
        final int constraint_schema  = 1;
        final int constraint_name    = 2;
        final int is_deferrable      = 3;
        final int initially_deferred = 4;

        return t;
    }

    /**
     *  SYSTEM_AUTHORIZATIONS<p>
     *
     *  <b>Function</b><p>
     *
     *  The AUTHORIZATIONS table has one row for each &lt;role name&gt; and
     *  one row for each &lt;authorization identifier &gt; referenced in the
     *  Information Schema. These are the &lt;role name&gt;s and
     *  &lt;authorization identifier&gt;s that may grant privileges as well as
     *  those that may create a schema, or currently own a schema created
     *  through a &lt;schema definition&gt;. <p>
     *
     *  <b>Definition</b><p>
     *
     *  <pre class="SqlCodeExample">
     *  CREATE TABLE AUTHORIZATIONS (
     *       AUTHORIZATION_NAME INFORMATION_SCHEMA.SQL_IDENTIFIER,
     *       AUTHORIZATION_TYPE INFORMATION_SCHEMA.CHARACTER_DATA
     *           CONSTRAINT AUTHORIZATIONS_AUTHORIZATION_TYPE_NOT_NULL
     *               NOT NULL
     *           CONSTRAINT AUTHORIZATIONS_AUTHORIZATION_TYPE_CHECK
     *               CHECK ( AUTHORIZATION_TYPE IN ( 'USER', 'ROLE' ) ),
     *           CONSTRAINT AUTHORIZATIONS_PRIMARY_KEY
     *               PRIMARY KEY (AUTHORIZATION_NAME)
     *       )
     *  </pre>
     *
     *  <b>Description</b><p>
     *
     *  <ol>
     *  <li> The values of AUTHORIZATION_TYPE have the following meanings:<p>
     *
     *  <table border cellpadding="3">
     *       <tr>
     *           <td nowrap>USER</td>
     *           <td nowrap>The value of AUTHORIZATION_NAME is a known
     *                      &lt;user identifier&gt;.</td>
     *       <tr>
     *       <tr>
     *           <td nowrap>NO</td>
     *           <td nowrap>The value of AUTHORIZATION_NAME is a &lt;role
     *                      name&gt; defined by a &lt;role definition&gt;.</td>
     *       <tr>
     *  </table> <p>
     *  </ol>
     *
     * @throws HsqlException
     * @return Table
     */
    Table AUTHORIZATIONS() throws HsqlException {

        Table t = sysTables[AUTHORIZATIONS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[AUTHORIZATIONS]);

            addColumn(t, "AUTHORIZATION_NAME", SQL_IDENTIFIER);    // not null
            addColumn(t, "AUTHORIZATION_TYPE", SQL_IDENTIFIER);    // not null

            // true PK
            t.createPrimaryKey(null, new int[]{ 0 }, true);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // Intermediate holders
        Iterator grantees;
        Grantee  grantee;
        Object[] row;

        // initialization
        grantees = session.getGrantee().visibleGrantees().iterator();

        // Do it.
        while (grantees.hasNext()) {
            grantee = (Grantee) grantees.next();
            row     = t.getEmptyRowData();
            row[0]  = grantee.getNameString();
            row[1]  = grantee.isRole() ? "ROLE"
                                       : "USER";

            t.insertSys(store, row);
        }

        return t;
    }

    Table CHARACTER_SETS() throws HsqlException {

        Table t = sysTables[CHARACTER_SETS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[CHARACTER_SETS]);

            addColumn(t, "CHARACTER_SET_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "CHARACTER_SET_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "CHARACTER_SET_NAME", SQL_IDENTIFIER);
            addColumn(t, "CHARACTER_REPERTOIRE", SQL_IDENTIFIER);
            addColumn(t, "FORM_OF_USE", SQL_IDENTIFIER);
            addColumn(t, "DEFAULT_COLLATE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "DEFAULT_COLLATE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "DEFAULT_COLLATE_NAME", SQL_IDENTIFIER);
            t.createPrimaryKey(null, new int[] {
                0, 1, 2
            }, false);

            return t;
        }

        final int character_set_catalog   = 0;
        final int character_set_schema    = 1;
        final int character_set_name      = 2;
        final int character_repertoire    = 3;
        final int form_of_use             = 4;
        final int default_collate_catalog = 5;
        final int default_collate_schema  = 6;
        final int default_collate_name    = 7;
        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());
        Iterator it = database.schemaManager.databaseObjectIterator(
            SchemaObject.CHARSET);

        while (it.hasNext()) {
            Charset charset = (Charset) it.next();

            if (!session.getGrantee().isAccessible(charset)) {
                continue;
            }

            Object[] data = t.getEmptyRowData();

            data[character_set_catalog]   = database.getCatalogName().name;
            data[character_set_schema]    = charset.getSchemaName().name;
            data[character_set_name]      = charset.getName().name;
            data[character_repertoire]    = "UCS";
            data[form_of_use]             = "UTF16";
            data[default_collate_catalog] = data[character_set_catalog];

            if (charset.base == null) {
                data[default_collate_schema] = data[character_set_schema];
                data[default_collate_name]   = data[character_set_name];
            } else {
                data[default_collate_schema] = charset.base.schema.name;
                data[default_collate_name]   = charset.base.name;
            }

            t.insertSys(store, data);
        }

        return t;
    }

    /**
     * The CHECK_CONSTRAINT_ROUTINE_USAGE view has one row for each
     * SQL-invoked routine identified as the subject routine of either a
     * &lt;routine invocation&gt;, a &lt;method reference&gt;, a
     * &lt;method invocation&gt;, or a &lt;static method invocation&gt;
     * contained in an &lt;assertion definition&gt;, a &lt;domain
     * constraint&gt;, or a &lt;table constraint definition&gt;. <p>
     *
     * <b>Definition:</b> <p>
     *
     * <pre class="SqlCodeExample">
     * CREATE TABLE SYSTEM_CHECK_ROUTINE_USAGE (
     *      CONSTRAINT_CATALOG      VARCHAR NULL,
     *      CONSTRAINT_SCHEMA       VARCHAR NULL,
     *      CONSTRAINT_NAME         VARCHAR NOT NULL,
     *      SPECIFIC_CATALOG        VARCHAR NULL,
     *      SPECIFIC_SCHEMA         VARCHAR NULL,
     *      SPECIFIC_NAME           VARCHAR NOT NULL,
     *      UNIQUE( CONSTRAINT_CATALOG, CONSTRAINT_SCHEMA, CONSTRAINT_NAME,
     *              SPECIFIC_CATALOG, SPECIFIC_SCHEMA, SPECIFIC_NAME )
     * )
     * </pre>
     *
     * <b>Description:</b> <p>
     *
     * <ol>
     * <li> The CHECK_ROUTINE_USAGE table has one row for each
     *      SQL-invoked routine R identified as the subject routine of either a
     *      &lt;routine invocation&gt;, a &lt;method reference&gt;, a &lt;method
     *      invocation&gt;, or a &lt;static method invocation&gt; contained in
     *      an &lt;assertion definition&gt; or in the &lt;check constraint
     *      definition&gt; contained in either a &lt;domain constraint&gt; or a
     *      &lt;table constraint definition&gt;. <p>
     *
     * <li> The values of CONSTRAINT_CATALOG, CONSTRAINT_SCHEMA, and
     *      CONSTRAINT_NAME are the catalog name, unqualified schema name, and
     *      qualified identifier, respectively, of the assertion or check
     *     constraint being described. <p>
     *
     * <li> The values of SPECIFIC_CATALOG, SPECIFIC_SCHEMA, and SPECIFIC_NAME
     *      are the catalog name, unqualified schema name, and qualified
     *      identifier, respectively, of the specific name of R. <p>
     *
     * </ol>
     *
     * @throws HsqlException
     * @return Table
     */
    Table CHECK_CONSTRAINT_ROUTINE_USAGE() throws HsqlException {

        Table t = sysTables[CHECK_CONSTRAINT_ROUTINE_USAGE];

        if (t == null) {
            t = createBlankTable(
                sysTableHsqlNames[CHECK_CONSTRAINT_ROUTINE_USAGE]);

            addColumn(t, "CONSTRAINT_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "CONSTRAINT_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "CONSTRAINT_NAME", SQL_IDENTIFIER);    // not null
            addColumn(t, "SPECIFIC_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "SPECIFIC_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "SPECIFIC_NAME", SQL_IDENTIFIER);      // not null
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 3, 4, 5
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // calculated column values
        String constraintCatalog;
        String constraintSchema;
        String constraintName;
        String specificSchema;

        // Intermediate holders
        Iterator       tables;
        Table          table;
        Constraint[]   constraints;
        int            constraintCount;
        Constraint     constraint;
        OrderedHashSet collector;
        Iterator       iterator;
        OrderedHashSet methodSet;
        Method         method;
        Object[]       row;

        // column number mappings
        final int constraint_catalog = 0;
        final int constraint_schema  = 1;
        final int constraint_name    = 2;
        final int specific_catalog   = 3;
        final int specific_schema    = 4;
        final int specific_name      = 5;

        tables =
            database.schemaManager.databaseObjectIterator(SchemaObject.TABLE);
        collector = new OrderedHashSet();

        while (tables.hasNext()) {
            collector.clear();

            table = (Table) tables.next();

            if (table.isView()
                    || !session.getGrantee().isFullyAccessibleByRole(table)) {
                continue;
            }

            constraints       = table.getConstraints();
            constraintCount   = constraints.length;
            constraintCatalog = database.getCatalogName().name;
            constraintSchema  = table.getSchemaName().name;
            specificSchema =
                database.schemaManager.getDefaultSchemaHsqlName().name;

            for (int i = 0; i < constraintCount; i++) {
                constraint = constraints[i];

                if (constraint.getConstraintType() != Constraint.CHECK) {
                    continue;
                }

                constraintName = constraint.getName().name;

                constraint.getCheckExpression().collectAllFunctionExpressions(
                    collector);

                methodSet = new OrderedHashSet();
                iterator  = collector.iterator();

                while (iterator.hasNext()) {
/*
                    Function expression = (Function) iterator.next();
                    String className =
                        expression.getMethod().getDeclaringClass().getName();
                    String schema =
                        database.schemaManager.getDefaultSchemaHsqlName().name;
                    SchemaObject object =
                        database.schemaManager.getSchemaObject(className,
                            schema, SchemaObject.FUNCTION);

                    if (!session.getGrantee().isAccessible(object)) {
                        continue;
                    }

                    methodSet.add(expression.getMethod());
*/
                }

                iterator = methodSet.iterator();

                while (iterator.hasNext()) {
                    method                  = (Method) iterator.next();
                    row                     = t.getEmptyRowData();
                    row[constraint_catalog] = constraintCatalog;
                    row[constraint_schema]  = constraintSchema;
                    row[constraint_name]    = constraintName;
                    row[specific_catalog]   = database.getCatalogName();
                    row[specific_schema]    = specificSchema;
                    row[specific_name] =
                        DINameSpace.getMethodSpecificName(method);

                    t.insertSys(store, row);
                }
            }
        }

        return t;
    }

    /**
     * The CHECK_CONSTRAINTS view has one row for each domain
     * constraint, table check constraint, and assertion. <p>
     *
     * <b>Definition:</b><p>
     *
     * <pre class="SqlCodeExample">
     *      CONSTRAINT_CATALOG  VARCHAR NULL,
     *      CONSTRAINT_SCHEMA   VARCHAR NULL,
     *      CONSTRAINT_NAME     VARCHAR NOT NULL,
     *      CHECK_CLAUSE        VARCHAR NOT NULL,
     * </pre>
     *
     * <b>Description:</b><p>
     *
     * <ol>
     * <li> A constraint is shown in this view if the authorization for the
     *      schema that contains the constraint is the current user or is a role
     *      assigned to the current user. <p>
     *
     * <li> The values of CONSTRAINT_CATALOG, CONSTRAINT_SCHEMA and
     *      CONSTRAINT_NAME are the catalog name, unqualified schema name,
     *      and qualified identifier, respectively, of the constraint being
     *      described. <p>
     *
     * <li> Case: <p>
     *
     *      <table>
     *          <tr>
     *               <td valign="top" halign="left">a)</td>
     *               <td> If the character representation of the
     *                    &lt;search condition&gt; contained in the
     *                    &lt;check constraint definition&gt;,
     *                    &lt;domain constraint definition&gt;, or
     *                    &lt;assertion definition&gt; that defined
     *                    the check constraint being described can be
     *                    represented without truncation, then the
     *                    value of CHECK_CLAUSE is that character
     *                    representation. </td>
     *          </tr>
     *          <tr>
     *              <td align="top" halign="left">b)</td>
     *              <td>Otherwise, the value of CHECK_CLAUSE is the
     *                  null value.</td>
     *          </tr>
     *      </table>
     * </ol>
     *
     * @throws HsqlException
     * @return Table
     */
    Table CHECK_CONSTRAINTS() throws HsqlException {

        Table t = sysTables[CHECK_CONSTRAINTS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[CHECK_CONSTRAINTS]);

            addColumn(t, "CONSTRAINT_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "CONSTRAINT_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "CONSTRAINT_NAME", SQL_IDENTIFIER);    // not null
            addColumn(t, "CHECK_CLAUSE", CHARACTER_DATA);       // not null
            t.createPrimaryKey(null, new int[] {
                2, 1, 0
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // calculated column values
        // Intermediate holders
        Iterator     tables;
        Table        table;
        Constraint[] tableConstraints;
        int          constraintCount;
        Constraint   constraint;
        Object[]     row;

        // column number mappings
        final int constraint_catalog = 0;
        final int constraint_schema  = 1;
        final int constraint_name    = 2;
        final int check_clause       = 3;

        tables =
            database.schemaManager.databaseObjectIterator(SchemaObject.TABLE);

        while (tables.hasNext()) {
            table = (Table) tables.next();

            if (table.isView()
                    || !session.getGrantee().isFullyAccessibleByRole(table)) {
                continue;
            }

            tableConstraints = table.getConstraints();
            constraintCount  = tableConstraints.length;

            for (int i = 0; i < constraintCount; i++) {
                constraint = tableConstraints[i];

                if (constraint.getConstraintType() != Constraint.CHECK) {
                    continue;
                }

                row                     = t.getEmptyRowData();
                row[constraint_catalog] = database.getCatalogName().name;
                row[constraint_schema]  = table.getSchemaName().name;
                row[constraint_name]    = constraint.getName().name;

                try {
                    row[check_clause] = constraint.getCheckDDL();
                } catch (Exception e) {}

                t.insertSys(store, row);
            }
        }

        Iterator it =
            database.schemaManager.databaseObjectIterator(SchemaObject.DOMAIN);

        while (it.hasNext()) {
            Type domain = (Type) it.next();

            if (!domain.isDomainType()) {
                continue;
            }

            if (!session.getGrantee().isFullyAccessibleByRole(domain)) {
                continue;
            }

            tableConstraints = domain.userTypeModifier.getConstraints();
            constraintCount  = tableConstraints.length;

            for (int i = 0; i < constraintCount; i++) {
                constraint              = tableConstraints[i];
                row                     = t.getEmptyRowData();
                row[constraint_catalog] = database.getCatalogName().name;
                row[constraint_schema]  = domain.getSchemaName().name;
                row[constraint_name]    = constraint.getName().name;

                try {
                    row[check_clause] = constraint.getCheckDDL();
                } catch (Exception e) {}

                t.insertSys(store, row);
            }
        }

        return t;
    }

    /**
     * COLLATIONS<p>
     *
     * <b>Function<b><p>
     *
     * The COLLATIONS view has one row for each character collation
     * descriptor. <p>
     *
     * <b>Definition</b>
     *
     * <pre class="SqlCodeExample">
     * CREATE TABLE COLLATIONS (
     *      COLLATION_CATALOG INFORMATION_SCHEMA.SQL_IDENTIFIER,
     *      COLLATION_SCHEMA INFORMATION_SCHEMA.SQL_IDENTIFIER,
     *      COLLATION_NAME INFORMATION_SCHEMA.SQL_IDENTIFIER,
     *      PAD_ATTRIBUTE INFORMATION_SCHEMA.CHARACTER_DATA
     *          CONSTRAINT COLLATIONS_PAD_ATTRIBUTE_CHECK
     *              CHECK ( PAD_ATTRIBUTE IN
     *                  ( 'NO PAD', 'PAD SPACE' ) )
     * )
     * </pre>
     *
     * <b>Description</b><p>
     *
     * <ol>
     *      <li>The values of COLLATION_CATALOG, COLLATION_SCHEMA, and
     *          COLLATION_NAME are the catalog name, unqualified schema name,
     *          and qualified identifier, respectively, of the collation being
     *          described.<p>
     *
     *      <li>The values of PAD_ATTRIBUTE have the following meanings:<p>
     *
     *      <table border cellpadding="3">
     *          <tr>
     *              <td nowrap>NO PAD</td>
     *              <td nowrap>The collation being described has the NO PAD
     *                  characteristic.</td>
     *          <tr>
     *          <tr>
     *              <td nowrap>PAD</td>
     *              <td nowrap>The collation being described has the PAD SPACE
     *                         characteristic.</td>
     *          <tr>
     *      </table> <p>
     * </ol>
     *
     * @throws HsqlException
     * @return Table
     */
    Table COLLATIONS() throws HsqlException {

        Table t = sysTables[COLLATIONS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[COLLATIONS]);

            addColumn(t, "COLLATION_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "COLLATION_SCHEMA", SQL_IDENTIFIER);    // not null
            addColumn(t, "COLLATION_NAME", SQL_IDENTIFIER);      // not null
            addColumn(t, "PAD_ATTRIBUTE", CHARACTER_DATA);

            // false PK, as rows may have NULL COLLATION_CATALOG
            t.createPrimaryKey(null, new int[] {
                0, 1, 2
            }, false);

            return t;
        }

        // Intermediate holders
        Iterator collations;
        String   collation;
        String   collationSchema = SqlInvariants.PUBLIC_SCHEMA;
        String   padAttribute    = "NO PAD";
        Object[] row;

        // Column number mappings
        final int collation_catalog = 0;
        final int collation_schema  = 1;
        final int collation_name    = 2;
        final int pad_attribute     = 3;
        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // Initialization
        collations = Collation.nameToJavaName.keySet().iterator();

        // Do it.
        while (collations.hasNext()) {
            row                    = t.getEmptyRowData();
            collation              = (String) collations.next();
            row[collation_catalog] = database.getCatalogName().name;
            row[collation_schema]  = collationSchema;
            row[collation_name]    = collation;
            row[pad_attribute]     = padAttribute;

            t.insertSys(store, row);
        }

        return t;
    }

    Table COLUMN_COLUMN_USAGE() throws HsqlException {

        Table t = sysTables[COLUMN_COLUMN_USAGE];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[COLUMN_COLUMN_USAGE]);

            addColumn(t, "TABLE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TABLE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TABLE_NAME", SQL_IDENTIFIER);
            addColumn(t, "COLUMN_NAME", SQL_IDENTIFIER);
            addColumn(t, "DEPENDENT_COLUMN", SQL_IDENTIFIER);
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 3, 4
            }, false);

            return t;
        }

        final int table_catalog    = 0;
        final int table_schema     = 1;
        final int table_name       = 2;
        final int column_name      = 3;
        final int dependent_column = 4;

        return t;
    }

    /**
     * Domains are shown if the authorization is the user or a role given to the
     * user.
     *
     * <p>
     *
     * @throws HsqlException
     * @return Table
     */
    Table COLUMN_DOMAIN_USAGE() throws HsqlException {

        Table t = sysTables[COLUMN_DOMAIN_USAGE];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[COLUMN_DOMAIN_USAGE]);

            addColumn(t, "DOMAIN_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "DOMAIN_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "DOMAIN_NAME", SQL_IDENTIFIER);
            addColumn(t, "TABLE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TABLE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TABLE_NAME", SQL_IDENTIFIER);     // not null
            addColumn(t, "COLUMN_NAME", SQL_IDENTIFIER);    // not null
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 3, 4, 5, 6
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());
        Session sys = database.sessionManager.newSysSession(
            SqlInvariants.INFORMATION_SCHEMA_HSQLNAME, session.getUser());
        Result rs = sys.executeDirectStatement(
            "SELECT DOMAIN_CATALOG, DOMAIN_SCHEMA, DOMAIN_NAME, TABLE_CATALOG, "
            + "TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS "
            + "WHERE DOMAIN_NAME IS NOT NULL;");

        t.insertSys(store, rs);
        sys.close();

        return t;
    }

    /**
     * UDT's are shown if the authorization is the user or a role given to the
     * user.
     *
     * <p>
     *
     * @throws HsqlException
     * @return Table
     */
    Table COLUMN_UDT_USAGE() throws HsqlException {

        Table t = sysTables[COLUMN_UDT_USAGE];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[COLUMN_UDT_USAGE]);

            addColumn(t, "UDT_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "UDT_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "UDT_NAME", SQL_IDENTIFIER);
            addColumn(t, "TABLE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TABLE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TABLE_NAME", SQL_IDENTIFIER);     // not null
            addColumn(t, "COLUMN_NAME", SQL_IDENTIFIER);    // not null
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 3, 4, 5, 6
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());
        Session sys = database.sessionManager.newSysSession(
            SqlInvariants.INFORMATION_SCHEMA_HSQLNAME, session.getUser());
        Result rs = sys.executeDirectStatement(
            "SELECT UDT_CATALOG, UDT_SCHEMA, UDT_NAME, TABLE_CATALOG, "
            + "TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS "
            + "WHERE UDT_NAME IS NOT NULL;");

        t.insertSys(store, rs);
        sys.close();

        return t;
    }

    Table COLUMNS() throws HsqlException {

        Table t = sysTables[COLUMNS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[COLUMNS]);

            addColumn(t, "TABLE_CATALOG", SQL_IDENTIFIER);           //0
            addColumn(t, "TABLE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TABLE_NAME", SQL_IDENTIFIER);
            addColumn(t, "COLUMN_NAME", SQL_IDENTIFIER);
            addColumn(t, "ORDINAL_POSITION", CARDINAL_NUMBER);
            addColumn(t, "COLUMN_DEFAULT", CHARACTER_DATA);
            addColumn(t, "IS_NULLABLE", YES_OR_NO);
            addColumn(t, "DATA_TYPE", CHARACTER_DATA);
            addColumn(t, "CHARACTER_MAXIMUM_LENGTH", CARDINAL_NUMBER);
            addColumn(t, "CHARACTER_OCTET_LENGTH", CARDINAL_NUMBER);
            addColumn(t, "NUMERIC_PRECISION", CARDINAL_NUMBER);      //10
            addColumn(t, "NUMERIC_PRECISION_RADIX", CARDINAL_NUMBER);
            addColumn(t, "NUMERIC_SCALE", CARDINAL_NUMBER);
            addColumn(t, "DATETIME_PRECISION", CARDINAL_NUMBER);
            addColumn(t, "INTERVAL_TYPE", CHARACTER_DATA);
            addColumn(t, "INTERVAL_PRECISION", CARDINAL_NUMBER);
            addColumn(t, "CHARACTER_SET_CATALOG", CHARACTER_DATA);
            addColumn(t, "CHARACTER_SET_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "CHARACTER_SET_NAME", SQL_IDENTIFIER);
            addColumn(t, "COLLATION_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "COLLATION_SCHEMA", SQL_IDENTIFIER);        //20
            addColumn(t, "COLLATION_NAME", SQL_IDENTIFIER);
            addColumn(t, "DOMAIN_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "DOMAIN_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "DOMAIN_NAME", SQL_IDENTIFIER);
            addColumn(t, "UDT_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "UDT_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "UDT_NAME", SQL_IDENTIFIER);
            addColumn(t, "SCOPE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "SCOPE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "SCOPE_NAME", SQL_IDENTIFIER);              //30
            addColumn(t, "MAXIMUM_CARDINALITY", CARDINAL_NUMBER);    // NULL (only for array tyes)
            addColumn(t, "DTD_IDENTIFIER", SQL_IDENTIFIER);
            addColumn(t, "IS_SELF_REFERENCING", YES_OR_NO);
            addColumn(t, "IS_IDENTITY", YES_OR_NO);
            addColumn(t, "IDENTITY_GENERATION", CHARACTER_DATA);     // ALLWAYS / BY DEFAULT
            addColumn(t, "IDENTITY_START", CHARACTER_DATA);
            addColumn(t, "IDENTITY_INCREMENT", CHARACTER_DATA);
            addColumn(t, "IDENTITY_MAXIMUM", CHARACTER_DATA);
            addColumn(t, "IDENTITY_MINIMUM", CHARACTER_DATA);
            addColumn(t, "IDENTITY_CYCLE", YES_OR_NO);               //40
            addColumn(t, "IS_GENERATED", CHARACTER_DATA);            // ALLWAYS / NEVER
            addColumn(t, "GENERATION_EXPRESSION", CHARACTER_DATA);
            addColumn(t, "IS_UPDATABLE", YES_OR_NO);
            addColumn(t, "DECLARED_DATA_TYPE", CHARACTER_DATA);
            addColumn(t, "DECLARED_NUMERIC_PRECISION", CARDINAL_NUMBER);
            addColumn(t, "DECLARED_NUMERIC_SCALE", CARDINAL_NUMBER);

            // order: TABLE_SCHEM, TABLE_NAME, ORDINAL_POSITION
            // added for unique: TABLE_CAT
            // false PK, as TABLE_SCHEM and/or TABLE_CAT may be null
            t.createPrimaryKey(null, new int[] {
                3, 2, 1, 4
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // intermediate holders
        int            columnCount;
        Iterator       tables;
        Table          table;
        Object[]       row;
        DITableInfo    ti;
        OrderedHashSet columnList;

        // column number mappings
        final int table_cat                  = 0;
        final int table_schem                = 1;
        final int table_name                 = 2;
        final int column_name                = 3;
        final int ordinal_position           = 4;
        final int column_default             = 5;
        final int is_nullable                = 6;
        final int data_type                  = 7;
        final int character_maximum_length   = 8;
        final int character_octet_length     = 9;
        final int numeric_precision          = 10;
        final int numeric_precision_radix    = 11;
        final int numeric_scale              = 12;
        final int datetime_precision         = 13;
        final int interval_type              = 14;
        final int interval_precision         = 15;
        final int character_set_catalog      = 16;
        final int character_set_schema       = 17;
        final int character_set_name         = 18;
        final int collation_catalog          = 19;
        final int collation_schema           = 20;
        final int collation_name             = 21;
        final int domain_catalog             = 22;
        final int domain_schema              = 23;
        final int domain_name                = 24;
        final int udt_catalog                = 25;
        final int udt_schema                 = 26;
        final int udt_name                   = 27;
        final int scope_catalog              = 28;
        final int scope_schema               = 29;
        final int scope_name                 = 30;
        final int maximum_cardinality        = 31;
        final int dtd_identifier             = 32;
        final int is_self_referencing        = 33;
        final int is_identity                = 34;
        final int identity_generation        = 35;
        final int identity_start             = 36;
        final int identity_increment         = 37;
        final int identity_maximum           = 38;
        final int identity_minimum           = 39;
        final int identity_cycle             = 40;
        final int is_generated               = 41;
        final int generation_expression      = 42;
        final int is_updatable               = 43;
        final int declared_data_type         = 44;
        final int declared_numeric_precision = 45;
        final int declared_numeric_scale     = 46;

        // Initialization
        tables = allTables();
        ti     = new DITableInfo();

        while (tables.hasNext()) {
            table = (Table) tables.next();
            columnList =
                session.getGrantee().getColumnsForAllPrivileges(table);

            if (columnList.isEmpty()) {
                continue;
            }

            ti.setTable(table);

            columnCount = table.getColumnCount();

            for (int i = 0; i < columnCount; i++) {
                ColumnSchema column = table.getColumn(i);

                if (!columnList.contains(column.getName())) {
                    continue;
                }

                row                   = t.getEmptyRowData();
                row[table_cat]        = table.getCatalogName().name;
                row[table_schem]      = table.getSchemaName().name;
                row[table_name]       = table.getName().name;
                row[column_name]      = column.getName().name;
                row[ordinal_position] = ValuePool.getInt(i + 1);
                row[column_default]   = column.getDefaultDDL();
                row[is_nullable]      = column.isNullable() ? "YES"
                                                            : "NO";
                row[data_type] = column.getDataType().getFullNameString();

                if (column.getDataType().isCharacterType()) {
                    row[character_maximum_length] =
                        ValuePool.getLong(column.getDataType().precision);
                    row[character_octet_length] =
                        ValuePool.getLong(column.getDataType().precision * 2);
                }

                if (column.getDataType().isNumberType()) {
                    row[numeric_precision] =
                        ValuePool.getLong(column.getDataType().precision);
                    row[numeric_precision_radix] = ti.getColPrecRadix(i);
                    row[numeric_scale] =
                        ValuePool.getLong(column.getDataType().scale);
                }

                if (column.getDataType().isDateTimeType()) {
                    row[datetime_precision] =
                        ValuePool.getLong(column.getDataType().scale);
                }

                if (column.getDataType().isIntervalType()) {
                    row[interval_type] =
                        column.getDataType().getFullNameString();
                    row[interval_precision] =
                        ValuePool.getLong(column.getDataType().precision);
                    row[datetime_precision] =
                        ValuePool.getLong(column.getDataType().scale);
                }

                if (column.getDataType().isCharacterType()) {
                    row[character_set_catalog] = null;
                    row[character_set_schema]  = null;
                    row[character_set_name]    = null;
                    row[collation_catalog]     = null;
                    row[collation_schema]      = null;
                    row[collation_name]        = null;
                }

                if (column.getDataType().isDomainType()) {
                    Type type = column.getDataType();

                    row[domain_catalog] = database.getCatalogName().name;
                    row[domain_schema]  = type.getSchemaName().name;
                    row[domain_name]    = type.getName().name;
                }

                if (column.getDataType().isDistinctType()) {
                    Type type = column.getDataType();

                    row[udt_catalog] = database.getCatalogName().name;
                    row[udt_schema]  = type.getSchemaName().name;
                    row[udt_name]    = type.getName().name;
                }

                row[scope_catalog]       = null;
                row[scope_schema]        = null;
                row[scope_name]          = null;
                row[maximum_cardinality] = null;
                row[dtd_identifier]      = null;
                row[is_self_referencing] = null;

                if (column.isIdentity()) {
                    NumberSequence sequence = column.getIdentitySequence();

                    row[is_identity]         = Boolean.TRUE;
                    row[identity_generation] = sequence.isAlways() ? "ALWAYS"
                                                                   : "BY DEFAULT";
                    row[identity_start] =
                        Long.toString(sequence.getStartValue());
                    row[identity_increment] =
                        Long.toString(sequence.getIncrement());
                    row[identity_maximum] =
                        Long.toString(sequence.getMaxValue());
                    row[identity_minimum] =
                        Long.toString(sequence.getMinValue());
                    row[identity_cycle] = sequence.isCycle() ? "YES"
                                                             : "NO";
                }

                row[is_generated]          = "NEVER";
                row[generation_expression] = null;
                row[is_updatable]          = table.isWritable() ? "YES"
                                                                : "NO";
                row[declared_data_type]    = row[data_type];

                if (column.getDataType().isNumberType()) {
                    row[declared_numeric_precision] = row[numeric_precision];
                    row[declared_numeric_scale]     = row[numeric_scale];
                }

                t.insertSys(store, row);
            }
        }

        return t;
    }

    /**
     * The CONSTRAINT_COLUMN_USAGE view has one row for each column identified by
     * a table constraint or assertion.<p>
     *
     * <b>Definition:</b><p>
     *
     *      TABLE_CATALOG       VARCHAR
     *      TABLE_SCHEMA        VARCHAR
     *      TABLE_NAME          VARCHAR
     *      COLUMN_NAME         VARCHAR
     *      CONSTRAINT_CATALOG  VARCHAR
     *      CONSTRAINT_SCHEMA   VARCHAR
     *      CONSTRAINT_NAME     VARCHAR
     *
     * </pre>
     *
     * <b>Description:</b> <p>
     *
     * <ol>
     * <li> The values of TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME, and
     *      COLUMN_NAME are the catalog name, unqualified schema name,
     *      qualified identifier, and column name, respectively, of a column
     *      identified by a &lt;column reference&gt; explicitly or implicitly
     *      contained in the &lt;search condition&gt; of the constraint
     *      being described.
     *
     * <li> The values of CONSTRAINT_CATALOG, CONSTRAINT_SCHEMA, and
     *      CONSTRAINT_NAME are the catalog name, unqualified schema name,
     *      and qualified identifier, respectively, of the constraint being
     *      described. <p>
     *
     * </ol>
     *
     * @throws HsqlException
     * @return Table
     */
    Table CONSTRAINT_COLUMN_USAGE() throws HsqlException {

        Table t = sysTables[CONSTRAINT_COLUMN_USAGE];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[CONSTRAINT_COLUMN_USAGE]);

            addColumn(t, "TABLE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TABLE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TABLE_NAME", SQL_IDENTIFIER);         // not null
            addColumn(t, "COLUMN_NAME", SQL_IDENTIFIER);        // not null
            addColumn(t, "CONSTRAINT_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "CONSTRAINT_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "CONSTRAINT_NAME", SQL_IDENTIFIER);    // not null
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 3, 4, 5, 6
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // calculated column values
        String constraintCatalog;
        String constraintSchema;
        String constraintName;

        // Intermediate holders
        Iterator     tables;
        Table        table;
        Constraint[] constraints;
        int          constraintCount;
        Constraint   constraint;
        Iterator     iterator;
        Object[]     row;

        // column number mappings
        final int table_catalog      = 0;
        final int table_schems       = 1;
        final int table_name         = 2;
        final int column_name        = 3;
        final int constraint_catalog = 4;
        final int constraint_schema  = 5;
        final int constraint_name    = 6;

        // Initialization
        tables =
            database.schemaManager.databaseObjectIterator(SchemaObject.TABLE);

        // Do it.
        while (tables.hasNext()) {
            table = (Table) tables.next();

            if (table.isView()
                    || !session.getGrantee().isFullyAccessibleByRole(table)) {
                continue;
            }

            constraints       = table.getConstraints();
            constraintCount   = constraints.length;
            constraintCatalog = database.getCatalogName().name;
            constraintSchema  = table.getSchemaName().name;

            // process constraints
            for (int i = 0; i < constraintCount; i++) {
                constraint     = constraints[i];
                constraintName = constraint.getName().name;

                switch (constraint.getConstraintType()) {

                    case Constraint.CHECK : {
                        OrderedHashSet names = constraint.getReferences();

                        if (names == null) {
                            break;
                        }

                        iterator = names.iterator();

                        // calculate distinct column references
                        while (iterator.hasNext()) {
                            HsqlName name = (HsqlName) iterator.next();

                            if (name.type != SchemaObject.COLUMN) {
                                continue;
                            }

                            row = t.getEmptyRowData();
                            row[table_catalog] =
                                database.getCatalogName().name;
                            row[table_schems]       = name.schema.name;
                            row[table_name]         = name.parent.name;
                            row[column_name]        = name.name;
                            row[constraint_catalog] = constraintCatalog;
                            row[constraint_schema]  = constraintSchema;
                            row[constraint_name]    = constraintName;

                            try {
                                t.insertSys(store, row);
                            } catch (HsqlException e) {}
                        }

                        break;
                    }
                    case Constraint.UNIQUE :
                    case Constraint.PRIMARY_KEY :
                    case Constraint.FOREIGN_KEY : {
                        Table target = table;
                        int[] cols   = constraint.getMainColumns();

                        if (constraint.getConstraintType()
                                == Constraint.FOREIGN_KEY) {
                            target = constraint.getMain();
                        }

/*
                       checkme - it seems foreign key columns are not included
                       but columns of the referenced unique constraint are included

                        if (constraint.getType() == Constraint.FOREIGN_KEY) {
                            for (int j = 0; j < cols.length; j++) {
                                row = t.getEmptyRowData();

                                Table mainTable = constraint.getMain();

                                row[table_catalog] = database.getCatalog();
                                row[table_schems] =
                                    mainTable.getSchemaName().name;
                                row[table_name] = mainTable.getName().name;
                                row[column_name] = mainTable.getColumn(
                                    cols[j]).columnName.name;
                                row[constraint_catalog] = constraintCatalog;
                                row[constraint_schema]  = constraintSchema;
                                row[constraint_name]    = constraintName;

                                try {
                                    t.insertSys(row);
                                } catch (HsqlException e) {}
                            }

                            cols = constraint.getRefColumns();
                        }
*/
                        for (int j = 0; j < cols.length; j++) {
                            row = t.getEmptyRowData();
                            row[table_catalog] =
                                database.getCatalogName().name;
                            row[table_schems] = constraintSchema;
                            row[table_name]   = target.getName().name;
                            row[column_name] =
                                target.getColumn(cols[j]).getName().name;
                            row[constraint_catalog] = constraintCatalog;
                            row[constraint_schema]  = constraintSchema;
                            row[constraint_name]    = constraintName;

                            try {
                                t.insertSys(store, row);
                            } catch (HsqlException e) {}
                        }

                        //
                    }
                }
            }
        }

        return t;
    }

    /**
     * The CONSTRAINT_TABLE_USAGE view has one row for each table identified by a
     * &lt;table name&gt; simply contained in a &lt;table reference&gt;
     * contained in the &lt;search condition&gt; of a check constraint,
     * domain constraint, or assertion. It has one row for each table
     * containing / referenced by each PRIMARY KEY, UNIQUE and FOREIGN KEY
     * constraint<p>
     *
     * <b>Definition:</b> <p>
     *
     * <pre class="SqlCodeExample">
     *      CONSTRAINT_CATALOG      VARCHAR
     *      CONSTRAINT_SCHEMA       VARCHAR
     *      CONSTRAINT_NAME         VARCHAR
     *      TABLE_CATALOG           VARCHAR
     *      TABLE_SCHEMA            VARCHAR
     *      TABLE_NAME              VARCHAR
     * </pre>
     *
     * <b>Description:</b> <p>
     *
     * <ol>
     * <li> The values of CONSTRAINT_CATALOG, CONSTRAINT_SCHEMA, and
     *      CONSTRAINT_NAME are the catalog name, unqualified schema name,
     *       and qualified identifier, respectively, of the constraint being
     *      described. <p>
     *
     * <li> The values of TABLE_CATALOG, TABLE_SCHEMA, and TABLE_NAME are the
     *      catalog name, unqualified schema name, and qualified identifier,
     *      respectively, of a table identified by a &lt;table name&gt;
     *      simply contained in a &lt;table reference&gt; contained in the
     *      *lt;search condition&gt; of the constraint being described, or
     *      its columns.
     * </ol>
     *
     * @throws HsqlException
     * @return Table
     */
    Table CONSTRAINT_TABLE_USAGE() throws HsqlException {

        Table t = sysTables[CONSTRAINT_TABLE_USAGE];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[CONSTRAINT_TABLE_USAGE]);

            addColumn(t, "CONSTRAINT_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "CONSTRAINT_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "CONSTRAINT_NAME", SQL_IDENTIFIER);    // not null
            addColumn(t, "TABLE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TABLE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TABLE_NAME", SQL_IDENTIFIER);         // not null
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 3, 4, 5
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        //
        Session sys = database.sessionManager.newSysSession(
            SqlInvariants.INFORMATION_SCHEMA_HSQLNAME, session.getUser());
        Result rs = sys.executeDirectStatement(
            "select DISTINCT CONSTRAINT_CATALOG, CONSTRAINT_SCHEMA, "
            + "CONSTRAINT_NAME, TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME "
            + "from INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE");

        t.insertSys(store, rs);
        sys.close();

        return t;
    }

    Table DATA_TYPE_PRIVILEGES() throws HsqlException {

        Table t = sysTables[DATA_TYPE_PRIVILEGES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[DATA_TYPE_PRIVILEGES]);

            addColumn(t, "OBJECT_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "OBJECT_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "OBJECT_NAME", SQL_IDENTIFIER);    // not null
            addColumn(t, "OBJECT_TYPE", SQL_IDENTIFIER);
            addColumn(t, "DTD_IDENTIFIER", SQL_IDENTIFIER);
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 3, 4
            }, false);

            return t;
        }

        return t;
    }

    /**
     * a DEFINITION_SCHEMA table. Not in the INFORMATION_SCHEMA list
     */
/*
    Table DATA_TYPE_DESCRIPTOR() throws HsqlException {

        Table t = sysTables[DATA_TYPE_DESCRIPTOR];

        if (t == null) {
            t = createBlankTable(
                sysTableHsqlNames[DATA_TYPE_DESCRIPTOR]);

            addColumn(t, "OBJECT_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "OBJECT_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "OBJECT_NAME", SQL_IDENTIFIER);
            addColumn(t, "OBJECT_TYPE", CHARACTER_DATA);
            addColumn(t, "DTD_IDENTIFIER", SQL_IDENTIFIER);
            addColumn(t, "DATA_TYPE", CHARACTER_DATA);
            addColumn(t, "CHARACTER_SET_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "CHARACTER_SET_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "CHARACTER_SET_NAME", SQL_IDENTIFIER);
            addColumn(t, "CHARACTER_MAXIMUM_LENGTH", CARDINAL_NUMBER);
            addColumn(t, "CHARACTER_OCTET_LENGTH", CARDINAL_NUMBER);
            addColumn(t, "COLLATION_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "COLLATION_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "COLLATION_NAME", SQL_IDENTIFIER);
            addColumn(t, "NUMERIC_PRECISION", CARDINAL_NUMBER);
            addColumn(t, "NUMERIC_PRECISION_RADIX", CARDINAL_NUMBER);
            addColumn(t, "NUMERIC_SCALE", CARDINAL_NUMBER);
            addColumn(t, "DECLARED_DATA_TYPE", CHARACTER_DATA);
            addColumn(t, "DECLARED_NUMERIC_PRECISION", CARDINAL_NUMBER);
            addColumn(t, "DECLARED_NUMERIC_SCLAE", CARDINAL_NUMBER);
            addColumn(t, "DATETIME_PRECISION", CARDINAL_NUMBER);
            addColumn(t, "INTERVAL_TYPE", CHARACTER_DATA);
            addColumn(t, "INTERVAL_PRECISION", CARDINAL_NUMBER);
            addColumn(t, "USER_DEFINED_TYPE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "USER_DEFINED_TYPE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "USER_DEFINED_TYPE_NAME", SQL_IDENTIFIER);
            addColumn(t, "SCOPE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "SCOPE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "SCOPE_NAME", SQL_IDENTIFIER);
            addColumn(t, "MAXIMUM_CARDINALITY", SQL_IDENTIFIER);
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 4, 5, 6
            }, false);

            return t;
        }

        return t;
    }
*/

    /**
     *
     * @throws HsqlException
     * @return Table
     */
    Table DOMAIN_CONSTRAINTS() throws HsqlException {

        Table t = sysTables[DOMAIN_CONSTRAINTS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[DOMAIN_CONSTRAINTS]);

            addColumn(t, "CONSTRAINT_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "CONSTRAINT_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "CONSTRAINT_NAME", SQL_IDENTIFIER);    // not null
            addColumn(t, "DOMAIN_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "DOMAIN_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "DOMAIN_NAME", SQL_IDENTIFIER);
            addColumn(t, "IS_DEFERRABLE", YES_OR_NO);
            addColumn(t, "INITIALLY_DEFERRED", YES_OR_NO);
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 4, 5, 6
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());
        final int constraint_catalog = 0;
        final int constraint_schema  = 1;
        final int constraint_name    = 2;
        final int domain_catalog     = 3;
        final int domain_schema      = 4;
        final int domain_name        = 5;
        final int is_deferrable      = 6;
        final int initially_deferred = 7;
        Iterator it =
            database.schemaManager.databaseObjectIterator(SchemaObject.DOMAIN);

        while (it.hasNext()) {
            Type domain = (Type) it.next();

            if (!domain.isDomainType()) {
                continue;
            }

            if (!session.getGrantee().isFullyAccessibleByRole(domain)) {
                continue;
            }

            Constraint[] constraints =
                domain.userTypeModifier.getConstraints();

            for (int i = 0; i < constraints.length; i++) {
                Object[] data = t.getEmptyRowData();

                data[constraint_catalog] = data[domain_catalog] =
                    database.getCatalogName().name;
                data[constraint_schema] = data[domain_schema] =
                    domain.getSchemaName().name;
                data[constraint_name]    = constraints[i].getName().name;
                data[domain_name]        = domain.getName().name;
                data[is_deferrable]      = Tokens.T_NO;
                data[initially_deferred] = Tokens.T_NO;

                t.insertSys(store, data);
            }
        }

        return t;
    }

    /**
     * The DOMAINS view has one row for each domain. <p>
     *
     *
     * <pre class="SqlCodeExample">
     *
     * </pre>
     *
     * @throws HsqlException
     * @return Table
     */
    Table DOMAINS() throws HsqlException {

        Table t = sysTables[DOMAINS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[DOMAINS]);

            addColumn(t, "DOMAIN_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "DOMAIN_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "DOMAIN_NAME", SQL_IDENTIFIER);
            addColumn(t, "DATA_TYPE", SQL_IDENTIFIER);
            addColumn(t, "CHARACTER_MAXIMUM_LENGTH", CARDINAL_NUMBER);
            addColumn(t, "CHARACTER_OCTET_LENGTH", CARDINAL_NUMBER);
            addColumn(t, "CHARACTER_SET_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "CHARACTER_SET_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "CHARACTER_SET_NAME", SQL_IDENTIFIER);
            addColumn(t, "COLLATION_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "COLLATION_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "COLLATION_NAME", SQL_IDENTIFIER);
            addColumn(t, "NUMERIC_PRECISION", CARDINAL_NUMBER);
            addColumn(t, "NUMERIC_PRECISION_RADIX", CARDINAL_NUMBER);
            addColumn(t, "NUMERIC_SCALE", CARDINAL_NUMBER);
            addColumn(t, "DATETIME_PRECISION", CARDINAL_NUMBER);
            addColumn(t, "INTERVAL_TYPE", CHARACTER_DATA);
            addColumn(t, "INTERVAL_PRECISION", CARDINAL_NUMBER);
            addColumn(t, "DOMAIN_DEFAULT", CHARACTER_DATA);
            addColumn(t, "MAXIMUM_CARDINALITY", SQL_IDENTIFIER);
            addColumn(t, "DTD_IDENTIFIER", SQL_IDENTIFIER);
            addColumn(t, "DECLARED_DATA_TYPE", CHARACTER_DATA);
            addColumn(t, "DECLARED_NUMERIC_PRECISION", CARDINAL_NUMBER);
            addColumn(t, "DECLARED_NUMERIC_SCLAE", CARDINAL_NUMBER);
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 4, 5, 6
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());
        final int domain_catalog             = 0;
        final int domain_schema              = 1;
        final int domain_name                = 2;
        final int data_type                  = 3;
        final int character_maximum_length   = 4;
        final int character_octet_length     = 5;
        final int character_set_catalog      = 6;
        final int character_set_schema       = 7;
        final int character_set_name         = 8;
        final int collation_catalog          = 9;
        final int collation_schema           = 10;
        final int collation_name             = 11;
        final int numeric_precision          = 12;
        final int numeric_precision_radix    = 13;
        final int numeric_scale              = 14;
        final int datetime_precision         = 15;
        final int interval_type              = 16;
        final int interval_precision         = 17;
        final int domain_default             = 18;
        final int maximum_cardinality        = 19;
        final int dtd_identifier             = 20;
        final int declared_data_type         = 21;
        final int declared_numeric_precision = 22;
        final int declared_numeric_scale     = 23;
        Iterator it =
            database.schemaManager.databaseObjectIterator(SchemaObject.DOMAIN);

        while (it.hasNext()) {
            Type domain = (Type) it.next();

            if (!domain.isDomainType()) {
                continue;
            }

            if (!session.getGrantee().isAccessible(domain)) {
                continue;
            }

            Object[] data = t.getEmptyRowData();

            data[domain_catalog] = database.getCatalogName().name;
            data[domain_schema]  = domain.getSchemaName().name;
            data[domain_name]    = domain.getName().name;
            data[data_type]      = domain.getFullNameString();

            if (domain.isCharacterType()) {
                data[character_maximum_length] =
                    ValuePool.getLong(domain.precision);
                data[character_octet_length] =
                    ValuePool.getLong(domain.precision * 2);
            } else if (domain.isNumberType()) {
                data[numeric_precision] =
                    ValuePool.getLong(((NumberType) domain).getPrecision());
                data[declared_numeric_precision] =
                    ValuePool.getLong(((NumberType) domain).getPrecision());

                if (domain.typeCode != Types.SQL_DOUBLE) {
                    data[numeric_scale] = ValuePool.getLong(domain.scale);
                    data[declared_numeric_scale] =
                        ValuePool.getLong(domain.scale);
                }

                data[numeric_precision_radix] = ValuePool.getLong(2);

                if (domain.typeCode == Types.SQL_DECIMAL
                        || domain.typeCode == Types.SQL_NUMERIC) {
                    data[numeric_precision_radix] = ValuePool.getLong(10);
                }
            } else if (domain.isBooleanType()) {}
            else if (domain.isDateTimeType()) {
                data[datetime_precision] = ValuePool.getLong(domain.scale);
            } else if (domain.isIntervalType()) {
                data[interval_precision] = ValuePool.getLong(domain.precision);
                data[interval_type]      = domain.getFullNameString();
                data[datetime_precision] = ValuePool.getLong(domain.scale);
            } else if (domain.isBinaryType()) {
                data[character_maximum_length] =
                    ValuePool.getLong(domain.precision);
                data[character_octet_length] =
                    ValuePool.getLong(domain.precision);
            } else if (domain.isBitType()) {
                data[character_maximum_length] =
                    ValuePool.getLong(domain.precision);
                data[character_octet_length] =
                    ValuePool.getLong(domain.precision);
            }

            Expression defaultExpression =
                domain.userTypeModifier.getDefaultClause();

            if (defaultExpression != null) {
                data[domain_default] = defaultExpression.getDDL();
            }

            t.insertSys(store, data);
        }

        return t;
    }

    Table ENABLED_ROLES() throws HsqlException {

        Table t = sysTables[ENABLED_ROLES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[ENABLED_ROLES]);

            addColumn(t, "ROLE_NAME", SQL_IDENTIFIER);

            // true PK
            t.createPrimaryKey(null, new int[]{ 0 }, true);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // Intermediate holders
        Iterator grantees;
        Grantee  grantee;
        Object[] row;

        // initialization
        grantees = session.getGrantee().getAllRoles().iterator();

        while (grantees.hasNext()) {
            grantee = (Grantee) grantees.next();
            row     = t.getEmptyRowData();
            row[0]  = grantee.getNameString();

            t.insertSys(store, row);
        }

        return t;
    }

    Table JAR_JAR_USAGE() {
        return null;
    }

    Table JARS() {
        return null;
    }

    /**
     * Retrieves a <code>Table</code> object describing the
     * primary key and unique constraint columns of each accessible table
     * defined within this database. <p>
     *
     * Each row is a PRIMARY KEY or UNIQUE column description with the following
     * columns: <p>
     *
     * <pre class="SqlCodeExample">
     * CONSTRAINT_CATALOG              VARCHAR NULL,
     * CONSTRAINT_SCHEMA               VARCHAR NULL,
     * CONSTRAINT_NAME                 VARCHAR NOT NULL,
     * TABLE_CATALOG                   VARCHAR   table catalog
     * TABLE_SCHEMA                    VARCHAR   table schema
     * TABLE_NAME                      VARCHAR   table name
     * COLUMN_NAME                     VARCHAR   column name
     * ORDINAL_POSITION                INT
     * POSITION_IN_UNIQUE_CONSTRAINT   INT
     * </pre> <p>
     *
     * @return a <code>Table</code> object describing the visible
     *        primary key and unique columns of each accessible table
     *        defined within this database.
     * @throws HsqlException if an error occurs while producing the table
     */
    Table KEY_COLUMN_USAGE() throws HsqlException {

        Table t = sysTables[KEY_COLUMN_USAGE];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[KEY_COLUMN_USAGE]);

            addColumn(t, "CONSTRAINT_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "CONSTRAINT_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "CONSTRAINT_NAME", SQL_IDENTIFIER);                   // not null
            addColumn(t, "TABLE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TABLE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TABLE_NAME", SQL_IDENTIFIER);                        // not null
            addColumn(t, "COLUMN_NAME", SQL_IDENTIFIER);                       // not null
            addColumn(t, "ORDINAL_POSITION", CARDINAL_NUMBER);                 // not null
            addColumn(t, "POSITION_IN_UNIQUE_CONSTRAINT", CARDINAL_NUMBER);    // not null
            t.createPrimaryKey(null, new int[] {
                2, 1, 0, 6, 7
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // Intermediate holders
        Iterator tables;
        Object[] row;

        // column number mappings
        final int constraint_catalog            = 0;
        final int constraint_schema             = 1;
        final int constraint_name               = 2;
        final int table_catalog                 = 3;
        final int table_schema                  = 4;
        final int table_name                    = 5;
        final int column_name                   = 6;
        final int ordinal_position              = 7;
        final int position_in_unique_constraint = 8;

        // Initialization
        tables =
            database.schemaManager.databaseObjectIterator(SchemaObject.TABLE);

        while (tables.hasNext()) {
            Table  table        = (Table) tables.next();
            String tableCatalog = database.getCatalogName().name;
            String tableSchema  = table.getSchemaName().name;
            String tableName    = table.getName().name;

            // todo - requires access to the actual columns
            if (table.isView() || !isAccessibleTable(table)) {
                continue;
            }

            Constraint[] constraints = table.getConstraints();

            for (int i = 0; i < constraints.length; i++) {
                Constraint constraint = constraints[i];

                if (constraint.getConstraintType() == Constraint.PRIMARY_KEY
                        || constraint.getConstraintType() == Constraint.UNIQUE
                        || constraint.getConstraintType()
                           == Constraint.FOREIGN_KEY) {
                    String constraintName = constraint.getName().name;
                    int[]  cols           = constraint.getMainColumns();
                    int[]  uniqueColMap   = null;

                    if (constraint.getConstraintType()
                            == Constraint.FOREIGN_KEY) {
                        Table uniqueConstTable = constraint.getMain();
                        Constraint uniqueConstraint =
                            uniqueConstTable.getConstraint(
                                constraint.getUniqueName().name);
                        int[] uniqueConstIndexes =
                            uniqueConstraint.getMainColumns();

                        uniqueColMap = new int[cols.length];

                        for (int j = 0; j < cols.length; j++) {
                            uniqueColMap[j] =
                                ArrayUtil.find(uniqueConstIndexes, cols[j]);
                        }

                        cols = constraint.getRefColumns();
                    }

                    for (int j = 0; j < cols.length; j++) {
                        row                     = t.getEmptyRowData();
                        row[constraint_catalog] = tableCatalog;
                        row[constraint_schema]  = tableSchema;
                        row[constraint_name]    = constraintName;
                        row[table_catalog]      = tableCatalog;
                        row[table_schema]       = tableSchema;
                        row[table_name]         = tableName;
                        row[column_name] =
                            table.getColumn(cols[j]).getName().name;
                        row[ordinal_position] = ValuePool.getInt(j + 1);

                        if (constraint.getConstraintType()
                                == Constraint.FOREIGN_KEY) {
                            row[position_in_unique_constraint] =
                                ValuePool.getInt(uniqueColMap[j] + 1);
                        }

                        t.insertSys(store, row);
                    }
                }
            }
        }

        return t;
    }

    Table METHOD_SPECIFICATIONS() {
        return null;
    }

    Table MODULE_COLUMN_USAGE() {
        return null;
    }

    Table MODULE_PRIVILEGES() {
        return null;
    }

    Table MODULE_TABLE_USAGE() {
        return null;
    }

    Table MODULES() {
        return null;
    }

    Table PARAMETERS() {
        return null;
    }

    /**
     * <ol>
     * <li> A constraint is shown in this view if the user has table level
     * privilege of at lease one of the types, INSERT, UPDATE, DELETE,
     * REFERENCES or TRIGGER.
     * </ol>
     *
     * @throws HsqlException
     * @return Table
     */
    Table REFERENTIAL_CONSTRAINTS() throws HsqlException {

        Table t = sysTables[REFERENTIAL_CONSTRAINTS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[REFERENTIAL_CONSTRAINTS]);

            addColumn(t, "CONSTRAINT_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "CONSTRAINT_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "CONSTRAINT_NAME", SQL_IDENTIFIER);              // not null
            addColumn(t, "UNIQUE_CONSTRAINT_CATALOG", SQL_IDENTIFIER);    // not null
            addColumn(t, "UNIQUE_CONSTRAINT_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "UNIQUE_CONSTRAINT_NAME", SQL_IDENTIFIER);
            addColumn(t, "MATCH_OPTION", CHARACTER_DATA);                 // not null
            addColumn(t, "UPDATE_RULE", CHARACTER_DATA);                  // not null
            addColumn(t, "DELETE_RULE", CHARACTER_DATA);                  // not null
            t.createPrimaryKey(null, new int[] {
                0, 1, 2,
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // Intermediate holders
        Iterator     tables;
        Table        table;
        Constraint[] constraints;
        Constraint   constraint;
        Object[]     row;

        // column number mappings
        final int constraint_catalog        = 0;
        final int constraint_schema         = 1;
        final int constraint_name           = 2;
        final int unique_constraint_catalog = 3;
        final int unique_constraint_schema  = 4;
        final int unique_constraint_name    = 5;
        final int match_option              = 6;
        final int update_rule               = 7;
        final int delete_rule               = 8;

        tables =
            database.schemaManager.databaseObjectIterator(SchemaObject.TABLE);

        while (tables.hasNext()) {
            table = (Table) tables.next();

            if (table.isView()
                    || !session.getGrantee().hasNonSelectTableRight(table)) {
                continue;
            }

            constraints = table.getConstraints();

            for (int i = 0; i < constraints.length; i++) {
                constraint = constraints[i];

                if (constraint.getConstraintType() != Constraint.FOREIGN_KEY) {
                    continue;
                }

                HsqlName uniqueName = constraint.getUniqueName();

                row                     = t.getEmptyRowData();
                row[constraint_catalog] = database.getCatalogName().name;
                row[constraint_schema]  = constraint.getSchemaName().name;
                row[constraint_name]    = constraint.getName().name;

                if (isAccessibleTable(constraint.getMain())) {
                    row[unique_constraint_catalog] =
                        database.getCatalogName().name;
                    row[unique_constraint_schema] = uniqueName.schema.name;
                    row[unique_constraint_name]   = uniqueName.name;
                }

                row[match_option] = Tokens.T_NONE;
                row[update_rule]  = constraint.getUpdateActionString();
                row[delete_rule]  = constraint.getDeleteActionString();

                t.insertSys(store, row);
            }
        }

        return t;
    }

    /**
     * ENABLED_ROLES<p>
     *
     * <b>Function</b><p>
     *
     * Identify the enabled roles for the current SQL-session.<p>
     *
     * Definition<p>
     *
     * <pre class="SqlCodeExample">
     * CREATE RECURSIVE VIEW ENABLED_ROLES ( ROLE_NAME ) AS
     *      VALUES ( CURRENT_ROLE )
     *      UNION
     *      SELECT RAD.ROLE_NAME
     *        FROM DEFINITION_SCHEMA.ROLE_AUTHORIZATION_DESCRIPTORS RAD
     *        JOIN ENABLED_ROLES R
     *          ON RAD.GRANTEE = R.ROLE_NAME;
     *
     * GRANT SELECT ON TABLE ENABLED_ROLES
     *    TO PUBLIC WITH GRANT OPTION;
     * </pre>
     */

    /**
     * APPLICABLE_ROLES<p>
     *
     * <b>Function</b><p>
     *
     * Identifies the applicable roles for the current user.<p>
     *
     * <b>Definition</b><p>
     *
     * <pre class="SqlCodeExample">
     * CREATE RECURSIVE VIEW APPLICABLE_ROLES ( GRANTEE, ROLE_NAME, IS_GRANTABLE ) AS
     *      ( ( SELECT GRANTEE, ROLE_NAME, IS_GRANTABLE
     *            FROM DEFINITION_SCHEMA.ROLE_AUTHORIZATION_DESCRIPTORS
     *           WHERE ( GRANTEE IN ( CURRENT_USER, 'PUBLIC' )
     *                OR GRANTEE IN ( SELECT ROLE_NAME
     *                                  FROM ENABLED_ROLES ) ) )
     *      UNION
     *      ( SELECT RAD.GRANTEE, RAD.ROLE_NAME, RAD.IS_GRANTABLE
     *          FROM DEFINITION_SCHEMA.ROLE_AUTHORIZATION_DESCRIPTORS RAD
     *          JOIN APPLICABLE_ROLES R
     *            ON RAD.GRANTEE = R.ROLE_NAME ) );
     *
     * GRANT SELECT ON TABLE APPLICABLE_ROLES
     *    TO PUBLIC WITH GRANT OPTION;
     * </pre>
     */
    Table ROLE_COLUMN_GRANTS() throws HsqlException {

        Table t = sysTables[ROLE_COLUMN_GRANTS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[ROLE_COLUMN_GRANTS]);

            addColumn(t, "GRANTOR", SQL_IDENTIFIER);           // not null
            addColumn(t, "GRANTEE", SQL_IDENTIFIER);           // not null
            addColumn(t, "TABLE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TABLE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TABLE_NAME", SQL_IDENTIFIER);        // not null
            addColumn(t, "COLUMN_NAME", SQL_IDENTIFIER);       // not null
            addColumn(t, "PRIVILEGE_TYPE", CHARACTER_DATA);    // not null
            addColumn(t, "IS_GRANTABLE", YES_OR_NO);           // not null

            // order: COLUMN_NAME, PRIVILEGE
            // for unique: GRANTEE, GRANTOR, TABLE_NAME, TABLE_SCHEMA, TABLE_CAT
            // false PK, as TABLE_SCHEMA and/or TABLE_CAT may be null
            t.createPrimaryKey(null, new int[] {
                5, 6, 1, 0, 4, 3, 2
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());
        Session sys = database.sessionManager.newSysSession(
            SqlInvariants.INFORMATION_SCHEMA_HSQLNAME, session.getUser());
        Result rs = sys.executeDirectStatement(
            "SELECT GRANTOR, GRANTEE, TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, PRIVILEGE_TYPE, IS_GRANTABLE "
            + "FROM INFORMATION_SCHEMA.COLUMN_PRIVILEGES "
            + "JOIN INFORMATION_SCHEMA.APPLICABLE_ROLES ON GRANTEE = ROLE_NAME;");

        t.insertSys(store, rs);
        sys.close();

        return t;
    }

    Table ROLE_ROUTINE_GRANTS() throws HsqlException {

        Table t = sysTables[ROLE_ROUTINE_GRANTS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[ROLE_ROUTINE_GRANTS]);

            addColumn(t, "GRANTOR", SQL_IDENTIFIER);          // not null
            addColumn(t, "GRANTEE", SQL_IDENTIFIER);          // not null
            addColumn(t, "SPECIFIC_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "SPECIFIC_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "SPECIFIC_NAME", SQL_IDENTIFIER);    // not null
            addColumn(t, "ROUTINE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "ROUTINE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "ROUTINE_NAME", SQL_IDENTIFIER);
            addColumn(t, "PRIVILEGE_TYPE", CHARACTER_DATA);
            addColumn(t, "IS_GRANTABLE", YES_OR_NO);

            // false PK, as VIEW_CATALOG, VIEW_SCHEMA, TABLE_CATALOG, and/or
            // TABLE_SCHEMA may be NULL
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // Column number mappings
        final int grantor          = 0;
        final int grantee          = 1;
        final int table_name       = 2;
        final int specific_catalog = 3;
        final int specific_schema  = 4;
        final int specific_name    = 5;
        final int routine_catalog  = 6;
        final int routine_schema   = 7;
        final int routine_name     = 8;
        final int privilege_type   = 9;
        final int is_grantable     = 10;

        return t;
    }

    Table ROLE_TABLE_GRANTS() throws HsqlException {

        Table t = sysTables[ROLE_TABLE_GRANTS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[ROLE_TABLE_GRANTS]);

            addColumn(t, "GRANTOR", SQL_IDENTIFIER);           // not null
            addColumn(t, "GRANTEE", SQL_IDENTIFIER);           // not null
            addColumn(t, "TABLE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TABLE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TABLE_NAME", SQL_IDENTIFIER);        // not null
            addColumn(t, "PRIVILEGE_TYPE", CHARACTER_DATA);    // not null
            addColumn(t, "IS_GRANTABLE", YES_OR_NO);           // not null
            addColumn(t, "WITH_HIERARCHY", YES_OR_NO);

            // order:  TABLE_SCHEM, TABLE_NAME, and PRIVILEGE,
            // added for unique:  GRANTEE, GRANTOR,
            // false PK, as TABLE_SCHEM and/or TABLE_CAT may be null
            t.createPrimaryKey(null, new int[] {
                3, 4, 5, 0, 1
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());
        Session sys = database.sessionManager.newSysSession(
            SqlInvariants.INFORMATION_SCHEMA_HSQLNAME, session.getUser());
        Result rs = sys.executeDirectStatement(
            "SELECT GRANTOR, GRANTEE, TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME, PRIVILEGE_TYPE, IS_GRANTABLE, 'NO' "
            + "FROM INFORMATION_SCHEMA.TABLE_PRIVILEGES "
            + "JOIN INFORMATION_SCHEMA.APPLICABLE_ROLES ON GRANTEE = ROLE_NAME;");

        t.insertSys(store, rs);
        sys.close();

        return t;
    }

    Table ROLE_UDT_GRANTS() throws HsqlException {

        Table t = sysTables[ROLE_UDT_GRANTS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[ROLE_UDT_GRANTS]);

            addColumn(t, "GRANTOR", SQL_IDENTIFIER);     // not null
            addColumn(t, "GRANTEE", SQL_IDENTIFIER);     // not null
            addColumn(t, "UDT_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "UDT_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "UDT_NAME", SQL_IDENTIFIER);    // not null
            addColumn(t, "PRIVILEGE_TYPE", CHARACTER_DATA);
            addColumn(t, "IS_GRANTABLE", YES_OR_NO);     // not null
            t.createPrimaryKey();

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // column number mappings
        final int grantor        = 0;
        final int grantee        = 1;
        final int udt_catalog    = 2;
        final int udt_schema     = 3;
        final int udt_name       = 4;
        final int privilege_type = 5;
        final int is_grantable   = 6;

        return t;
    }

    Table ROLE_USAGE_GRANTS() throws HsqlException {

        Table t = sysTables[ROLE_USAGE_GRANTS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[ROLE_USAGE_GRANTS]);

            addColumn(t, "GRANTOR", SQL_IDENTIFIER);        // not null
            addColumn(t, "GRANTEE", SQL_IDENTIFIER);        // not null
            addColumn(t, "OBJECT_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "OBJECT_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "OBJECT_NAME", SQL_IDENTIFIER);    // not null
            addColumn(t, "OBJECT_TYPE", CHARACTER_DATA);    // not null
            addColumn(t, "PRIVILEGE_TYPE", CHARACTER_DATA);
            addColumn(t, "IS_GRANTABLE", YES_OR_NO);        // not null

            // order: COLUMN_NAME, PRIVILEGE
            // for unique: GRANTEE, GRANTOR, TABLE_NAME, TABLE_SCHEM, TABLE_CAT
            // false PK, as TABLE_SCHEM and/or TABLE_CAT may be null
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 3, 4, 5
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());
        Session sys = database.sessionManager.newSysSession(
            SqlInvariants.INFORMATION_SCHEMA_HSQLNAME, session.getUser());
        Result rs = sys.executeDirectStatement(
            "SELECT GRANTOR, GRANTEE, OBJECT_CATALOG, OBJECT_SCHEMA, OBJECT_NAME, OBJECT_TYPE, PRIVILEGE_TYPE, IS_GRANTABLE "
            + "FROM INFORMATION_SCHEMA.USAGE_PRIVILEGES "
            + "JOIN INFORMATION_SCHEMA.APPLICABLE_ROLES ON GRANTEE = ROLE_NAME;");

        t.insertSys(store, rs);
        sys.close();

        return t;
    }

    Table ROUTINE_JAR_USAGE() {
        return null;
    }

    Table ROUTINES() {
        return null;
    }

    /**
     * SCHEMATA<p>
     *
     * <b>Function</b><p>
     *
     * The SCHEMATA view has one row for each accessible schema. <p>
     *
     * <b>Definition</b><p>
     *
     * <pre class="SqlCodeExample">
     * CREATE TABLE SCHEMATA (
     *      CATALOG_NAME INFORMATION_SCHEMA.SQL_IDENTIFIER,
     *      SCHEMA_NAME INFORMATION_SCHEMA.SQL_IDENTIFIER,
     *      SCHEMA_OWNER INFORMATION_SCHEMA.SQL_IDENTIFIER
     *          CONSTRAINT SCHEMA_OWNER_NOT_NULL
     *              NOT NULL,
     *      DEFAULT_CHARACTER_SET_CATALOG INFORMATION_SCHEMA.SQL_IDENTIFIER
     *          CONSTRAINT DEFAULT_CHARACTER_SET_CATALOG_NOT_NULL
     *              NOT NULL,
     *      DEFAULT_CHARACTER_SET_SCHEMA INFORMATION_SCHEMA.SQL_IDENTIFIER
     *          CONSTRAINT DEFAULT_CHARACTER_SET_SCHEMA_NOT_NULL
     *              NOT NULL,
     *      DEFAULT_CHARACTER_SET_NAME INFORMATION_SCHEMA.SQL_IDENTIFIER
     *          CONSTRAINT DEFAULT_CHARACTER_SET_NAME_NOT_NULL
     *              NOT NULL,
     *      SQL_PATH INFORMATION_SCHEMA.CHARACTER_DATA,
     *
     *      CONSTRAINT SCHEMATA_PRIMARY_KEY
     *          PRIMARY KEY ( CATALOG_NAME, SCHEMA_NAME ),
     *      CONSTRAINT SCHEMATA_FOREIGN_KEY_AUTHORIZATIONS
     *          FOREIGN KEY ( SCHEMA_OWNER )
     *              REFERENCES AUTHORIZATIONS,
     *      CONSTRAINT SCHEMATA_FOREIGN_KEY_CATALOG_NAMES
     *          FOREIGN KEY ( CATALOG_NAME )
     *              REFERENCES CATALOG_NAMES
     *      )
     * </pre>
     *
     * <b>Description</b><p>
     *
     * <ol>
     *      <li>The value of CATALOG_NAME is the name of the catalog of the
     *          schema described by this row.<p>
     *
     *      <li>The value of SCHEMA_NAME is the unqualified schema name of
     *          the schema described by this row.<p>
     *
     *      <li>The values of SCHEMA_OWNER are the authorization identifiers
     *          that own the schemata.<p>
     *
     *      <li>The values of DEFAULT_CHARACTER_SET_CATALOG,
     *          DEFAULT_CHARACTER_SET_SCHEMA, and DEFAULT_CHARACTER_SET_NAME
     *          are the catalog name, unqualified schema name, and qualified
     *          identifier, respectively, of the default character set for
     *          columns and domains in the schemata.<p>
     *
     *      <li>Case:<p>
     *          <ul>
     *              <li>If &lt;schema path specification&gt; was specified in
     *                  the &lt;schema definition&gt; that defined the schema
     *                  described by this row and the character representation
     *                  of the &lt;schema path specification&gt; can be
     *                  represented without truncation, then the value of
     *                  SQL_PATH is that character representation.<p>
     *
     *              <li>Otherwise, the value of SQL_PATH is the null value.
     *         </ul>
     * </ol>
     *
     * @throws HsqlException
     * @return Table
     */
    Table SCHEMATA() throws HsqlException {

        Table t = sysTables[SCHEMATA];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SCHEMATA]);

            addColumn(t, "CATALOG_NAME", SQL_IDENTIFIER);
            addColumn(t, "SCHEMA_NAME", SQL_IDENTIFIER);
            addColumn(t, "SCHEMA_OWNER", SQL_IDENTIFIER);
            addColumn(t, "DEFAULT_CHARACTER_SET_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "DEFAULT_CHARACTER_SET_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "DEFAULT_CHARACTER_SET_NAME", SQL_IDENTIFIER);
            addColumn(t, "SQL_PATH", CHARACTER_DATA);

            // order: CATALOG_NAME, SCHEMA_NAME
            // false PK, as rows may have NULL CATALOG_NAME
            t.createPrimaryKey(null, new int[] {
                0, 1
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // Intermediate holders
        Iterator schemas;
        String   schema;
        String   dcsSchema = SqlInvariants.INFORMATION_SCHEMA;
        String   dcsName   = ValuePool.getString("UTF16");
        String   sqlPath   = null;
        Grantee  user      = session.getGrantee();
        Object[] row;

        // column number mappings
        final int schema_catalog                = 0;
        final int schema_name                   = 1;
        final int schema_owner                  = 2;
        final int default_character_set_catalog = 3;
        final int default_character_set_schema  = 4;
        final int default_character_set_name    = 5;
        final int sql_path                      = 6;

        // Initialization
        schemas = database.schemaManager.fullSchemaNamesIterator();

        // Do it.
        while (schemas.hasNext()) {
            schema = (String) schemas.next();

            if (!user.hasSchemaUpdateOrGrantRights(schema)) {
                continue;
            }

            row                 = t.getEmptyRowData();
            row[schema_catalog] = database.getCatalogName().name;
            row[schema_name]    = schema;
            row[schema_owner] =
                database.schemaManager.toSchemaOwner(schema).getNameString();
            row[default_character_set_catalog] =
                database.getCatalogName().name;
            row[default_character_set_schema] = dcsSchema;
            row[default_character_set_name]   = dcsName;
            row[sql_path]                     = sqlPath;

            t.insertSys(store, row);
        }

        return t;
    }

    Table SQL_FEATURES() throws HsqlException {

        Table t = sysTables[SQL_FEATURES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SQL_FEATURES]);

            addColumn(t, "FEATURE_ID", CHARACTER_DATA);
            addColumn(t, "FEATURE_NAME", CHARACTER_DATA);
            addColumn(t, "SUB_FEATURE_ID", CHARACTER_DATA);
            addColumn(t, "SUB_FEATURE_NAME", CHARACTER_DATA);
            addColumn(t, "IS_SUPPORTED", YES_OR_NO);
            addColumn(t, "IS_VERIFIED_BY", CHARACTER_DATA);
            addColumn(t, "COMMENTS", CHARACTER_DATA);
            t.createPrimaryKey(null, new int[] {
                0, 2
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());
        Session sys = database.sessionManager.newSysSession(
            SqlInvariants.INFORMATION_SCHEMA_HSQLNAME, session.getUser());
        Result rs = sys.executeDirectStatement(
            "VALUES"
            + "('B011', 'Embedded Ada', '', '', 'NO', CAST(NULL AS CHARACTER), ''),"
            + "('B012', 'Embedded C', '', '', 'NO', NULL, ''),"
            + "('B013', 'Embedded COBOL', '', '', 'NO', NULL, ''),"
            + "('B014', 'Embedded Fortran', '', '', 'NO', NULL, ''),"
            + "('B015', 'Embedded MUMPS', '', '', 'NO', NULL, ''),"
            + "('B016', 'Embedded Pascal', '', '', 'NO', NULL, ''),"
            + "('B017', 'Embedded PL/I', '', '', 'NO', NULL, ''),"
            + "('B021', 'Direct SQL', '', '', 'YES', NULL, ''),"
            + "('B031', 'Basic dynamic SQL', '', '', 'NO', NULL, ''),"
            + "('B032', 'Extended dynamic SQL', '', '', 'NO', NULL, ''),"
            + "('B032', 'Extended dynamic SQL', '01', 'describe input statement', 'NO', NULL, ''),"
            + "('B033', 'Untyped SQL-invoked function arguments', '', '', 'NO', NULL, ''),"
            + "('B034', 'Dynamic specification of cursor attributes', '', '', 'NO', NULL, ''),"
            + "('B041', 'Extensions to embedded SQL exception declarations', '', '', 'NO', NULL, ''),"
            + "('B051', 'Enhanced execution rights', '', '', 'NO', NULL, ''),"
            + "('B111', 'Module language Ada', '', '', 'NO', NULL, ''),"
            + "('B112', 'Module language C', '', '', 'NO', NULL, ''),"
            + "('B113', 'Module language COBOL', '', '', 'NO', NULL, ''),"
            + "('B114', 'Module language Fortran', '', '', 'NO', NULL, ''),"
            + "('B115', 'Module language MUMPS', '', '', 'NO', NULL, ''),"
            + "('B116', 'Module language Pascal', '', '', 'NO', NULL, ''),"
            + "('B117', 'Module language PL/I', '', '', 'NO', NULL, ''),"
            + "('B121', 'Routine language Ada', '', '', 'NO', NULL, ''),"
            + "('B122', 'Routine language C', '', '', 'NO', NULL, ''),"
            + "('B123', 'Routine language COBOL', '', '', 'NO', NULL, ''),"
            + "('B124', 'Routine language Fortran', '', '', 'NO', NULL, ''),"
            + "('B125', 'Routine language MUMPS', '', '', 'NO', NULL, ''),"
            + "('B126', 'Routine language Pascal', '', '', 'NO', NULL, ''),"
            + "('B127', 'Routine language PL/I', '', '', 'NO', NULL, ''),"
            + "('B128', 'Routine language SQL', '', '', 'NO', NULL, ''),"
            + "('C011', 'Call-Level Interface', '', '', 'NO', NULL, ''),"
            + "('E011', 'Numeric data types', '', '', 'YES', NULL, ''),"
            + "('E011', 'Numeric data types', '01', 'INTEGER and SMALLINT data types', 'YES', NULL, ''),"
            + "('E011', 'Numeric data types', '02', 'REAL, DOUBLE PRECISION, and FLOAT data types', 'YES', NULL, ''),"
            + "('E011', 'Numeric data types', '03', 'DECIMAL and NUMERIC data types', 'YES', NULL, ''),"
            + "('E011', 'Numeric data types', '04', 'Arithmetic operators', 'YES', NULL, ''),"
            + "('E011', 'Numeric data types', '05', 'Numeric comparison', 'YES', NULL, ''),"
            + "('E011', 'Numeric data types', '06', 'Implicit casting among the numeric data types', 'YES', NULL, ''),"
            + "('E021', 'Character data types', '', '', 'YES', NULL, ''),"
            + "('E021', 'Character string types', '01', 'CHARACTER data type', 'YES', NULL, ''),"
            + "('E021', 'Character string types', '02', 'CHARACTER VARYING data type', 'YES', NULL, ''),"
            + "('E021', 'Character string types', '03', 'Character literals', 'YES', NULL, ''),"
            + "('E021', 'Character string types', '04', 'CHARACTER_LENGTH function', 'YES', NULL, ''),"
            + "('E021', 'Character string types', '05', 'OCTET_LENGTH function', 'YES', NULL, ''),"
            + "('E021', 'Character string types', '06', 'SUBSTRING function', 'YES', NULL, ''),"
            + "('E021', 'Character string types', '07', 'Character concatenation', 'YES', NULL, ''),"
            + "('E021', 'Character string types', '08', 'UPPER and LOWER functions', 'YES', NULL, ''),"
            + "('E021', 'Character string types', '09', 'TRIM function', 'YES', NULL, ''),"
            + "('E021', 'Character string types', '10', 'Implicit casting among the character string types', 'YES', NULL, ''),"
            + "('E021', 'Character string types', '11', 'POSITION function', 'YES', NULL, ''),"
            + "('E021', 'Character string types', '12', 'Character comparison', 'YES', NULL, ''),"
            + "('E031', 'Identifiers', '', '', 'YES', NULL, ''),"
            + "('E031', 'Identifiers', '01', 'Delimited identifiers', 'YES', NULL, ''),"
            + "('E031', 'Identifiers', '02', 'Lower case identifiers', 'YES', NULL, ''),"
            + "('E031', 'Identifiers', '03', 'Trailing underscore', 'YES', NULL, ''),"
            + "('E051', 'Basic query specification', '', '', 'YES', NULL, ''),"
            + "('E051', 'Basic query specification', '01', 'SELECT DISTINCT', 'YES', NULL, ''),"
            + "('E051', 'Basic query specification', '02', 'GROUP BY clause', 'YES', NULL, ''),"
            + "('E051', 'Basic query specification', '04', 'GROUP BY can contain columns not in <select list>', 'YES', NULL, ''),"
            + "('E051', 'Basic query specification', '05', 'Select list items can be renamed', 'YES', NULL, ''),"
            + "('E051', 'Basic query specification', '06', 'HAVING clause', 'YES', NULL, ''),"
            + "('E051', 'Basic query specification', '07', 'Qualified * in select list', 'YES', NULL, ''),"
            + "('E051', 'Basic query specification', '08', 'Correlation names in the FROM clause', 'YES', NULL, ''),"
            + "('E051', 'Basic query specification', '09', 'Rename columns in the FROM clause', 'YES', NULL, ''),"
            + "('E061', 'Basic predicates and search conditions', '', '', 'YES', NULL, ''),"
            + "('E061', 'Basic predicates and search conditions', '01', 'Comparison predicate', 'YES', NULL, ''),"
            + "('E061', 'Basic predicates and search conditions', '02', 'BETWEEN predicate', 'YES', NULL, ''),"
            + "('E061', 'Basic predicates and search conditions', '03', 'IN predicate with list of values', 'YES', NULL, ''),"
            + "('E061', 'Basic predicates and search conditions', '04', 'LIKE predicate', 'YES', NULL, ''),"
            + "('E061', 'Basic predicates and search conditions', '05', 'LIKE predicate ESCAPE clause', 'YES', NULL, ''),"
            + "('E061', 'Basic predicates and search conditions', '06', 'NULL predicate', 'YES', NULL, ''),"
            + "('E061', 'Basic predicates and search conditions', '07', 'Quantified comparison predicate', 'YES', NULL, ''),"
            + "('E061', 'Basic predicates and search conditions', '08', 'EXISTS predicate', 'YES', NULL, ''),"
            + "('E061', 'Basic predicates and search conditions', '09', 'Subqueries in comparison predicate', 'YES', NULL, ''),"
            + "('E061', 'Basic predicates and search conditions', '11', 'Subqueries in IN predicate', 'YES', NULL, ''),"
            + "('E061', 'Basic predicates and search conditions', '12', 'Subqueries in quantified comparison predicate', 'YES', NULL, ''),"
            + "('E061', 'Basic predicates and search conditions', '13', 'Correlated subqueries', 'YES', NULL, ''),"
            + "('E061', 'Basic predicates and search conditions', '14', 'Search condition', 'YES', NULL, ''),"
            + "('E071', 'Basic query expressions', '', '', 'YES', NULL, ''),"
            + "('E071', 'Basic query expressions', '01', 'UNION DISTINCT table operator', 'YES', NULL, ''),"
            + "('E071', 'Basic query expressions', '02', 'UNION ALL table operator', 'YES', NULL, ''),"
            + "('E071', 'Basic query expressions', '03', 'EXCEPT DISTINCT table operator', 'YES', NULL, ''),"
            + "('E071', 'Basic query expressions', '05', 'Columns combined via table operators need not have exactly the same data type', 'YES', NULL, ''),"
            + "('E071', 'Basic query expressions', '06', 'Table operators in subqueries', 'YES', NULL, ''),"
            + "('E081', 'Basic Privileges', '', '', 'NO', NULL, ''),"
            + "('E081', 'Basic Privileges', '01', 'SELECT privilege', 'YES', NULL, ''),"
            + "('E081', 'Basic Privileges', '02', 'DELETE privilege', 'YES', NULL, ''),"
            + "('E081', 'Basic Privileges', '03', 'INSERT privilege at the table level', 'YES', NULL, ''),"
            + "('E081', 'Basic Privileges', '04', 'UPDATE privilege at the table level', 'YES', NULL, ''),"
            + "('E081', 'Basic Privileges', '05', 'UPDATE privilege at the column level', 'YES', NULL, ''),"
            + "('E081', 'Basic Privileges', '06', 'REFERENCES privilege at the table level', 'YES', NULL, ''),"
            + "('E081', 'Basic Privileges', '07', 'REFERENCES privilege at the column level', 'YES', NULL, ''),"
            + "('E081', 'Basic Privileges', '08', 'WITH GRANT OPTION', 'NO', NULL, ''),"
            + "('E081', 'Basic Privileges', '09', 'USAGE privilege', 'YES', NULL, ''),"
            + "('E081', 'Basic Privileges', '10', 'EXECUTE privilege', 'YES', NULL, ''),"
            + "('E091', 'Set functions', '', '', 'YES', NULL, ''),"
            + "('E091', 'Set functions', '01', 'AVG', 'YES', NULL, ''),"
            + "('E091', 'Set functions', '02', 'COUNT', 'YES', NULL, ''),"
            + "('E091', 'Set functions', '03', 'MAX', 'YES', NULL, ''),"
            + "('E091', 'Set functions', '04', 'MIN', 'YES', NULL, ''),"
            + "('E091', 'Set functions', '05', 'SUM', 'YES', NULL, ''),"
            + "('E091', 'Set functions', '06', 'ALL quantifier', 'YES', NULL, ''),"
            + "('E091', 'Set functions', '07', 'DISTINCT quantifier', 'YES', NULL, ''),"
            + "('E101', 'Basic data manipulation', '', '', 'YES', NULL, ''),"
            + "('E101', 'Basic data manipulation', '01', 'INSERT statement', 'YES', NULL, ''),"
            + "('E101', 'Basic data manipulation', '03', 'Searched UPDATE statement', 'YES', NULL, ''),"
            + "('E101', 'Basic data manipulation', '04', 'Searched DELETE statement', 'YES', NULL, ''),"
            + "('E111', 'Single row SELECT statement', '', '', 'YES', NULL, ''),"
            + "('E121', 'Basic cursor support', '', '', 'NO', NULL, ''),"
            + "('E121', 'Basic cursor support', '01', 'DECLARE CURSOR', 'NO', NULL, ''),"
            + "('E121', 'Basic cursor support', '02', 'ORDER BY columns need not be in select list', 'YES', NULL, ''),"
            + "('E121', 'Basic cursor support', '03', 'Value expressions in ORDER BY clause', 'YES', NULL, ''),"
            + "('E121', 'Basic cursor support', '04', 'OPEN statement', 'NO', NULL, ''),"
            + "('E121', 'Basic cursor support', '06', 'Positioned UPDATE statement', 'NO', NULL, ''),"
            + "('E121', 'Basic cursor support', '07', 'Positioned DELETE statement', 'NO', NULL, ''),"
            + "('E121', 'Basic cursor support', '08', 'CLOSE statement', 'NO', NULL, ''),"
            + "('E121', 'Basic cursor support', '10', 'FETCH statement implicit NEXT', 'NO', NULL, ''),"
            + "('E121', 'Basic cursor support', '17', 'WITH HOLD cursors', 'NO', NULL, ''),"
            + "('E131', 'Null value support (nulls in lieu of values)', '', '', 'YES', NULL, ''),"
            + "('E141', 'Basic integrity constraints', '', '', 'YES', NULL, ''),"
            + "('E141', 'Basic integrity constraints', '01', 'NOT NULL constraints', 'YES', NULL, ''),"
            + "('E141', 'Basic integrity constraints', '02', 'UNIQUE constraints of NOT NULL columns', 'YES', NULL, ''),"
            + "('E141', 'Basic integrity constraints', '03', 'PRIMARY KEY constraints', 'YES', NULL, ''),"
            + "('E141', 'Basic integrity constraints', '04', 'Basic FOREIGN KEY constraint with the NO ACTION default for both referential delete action and referential update action', 'YES', NULL, ''),"
            + "('E141', 'Basic integrity constraints', '06', 'CHECK constraints', 'YES', NULL, ''),"
            + "('E141', 'Basic integrity constraints', '07', 'Column defaults', 'YES', NULL, ''),"
            + "('E141', 'Basic integrity constraints', '08', 'NOT NULL inferred on PRIMARY KEY', 'YES', NULL, ''),"
            + "('E141', 'Basic integrity constraints', '10', 'Names in a foreign key can be specified in any order', 'YES', NULL, ''),"
            + "('E151', 'Transaction support', '', '', 'YES', NULL, ''),"
            + "('E151', 'Transaction support', '01', 'COMMIT statement', 'YES', NULL, ''),"
            + "('E151', 'Transaction support', '02', 'ROLLBACK statement', 'YES', NULL, ''),"
            + "('E152', 'Basic SET TRANSACTION statement', '', '', 'YES', NULL, ''),"
            + "('E152', 'Basic SET TRANSACTION statement', '01', 'SET TRANSACTION statement: ISOLATION LEVEL SERIALIZABLE clause', 'YES', NULL, ''),"
            + "('E152', 'Basic SET TRANSACTION statement', '02', 'SET TRANSACTION statement: READ ONLY and READ WRITE clauses', 'YES', NULL, ''),"
            + "('E153', 'Updatable queries with subqueries', '', '', 'NO', NULL, ''),"
            + "('E161', 'SQL comments using leading double minus', '', '', 'YES', NULL, ''),"
            + "('E171', 'SQLSTATE support', '', '', 'YES', NULL, ''),"
            + "('E182', 'Module language', '', '', 'NO', NULL, ''),"
            + "('F021', 'Basic information schema', '', '', 'YES', NULL, ''),"
            + "('F021', 'Basic information schema', '01', 'COLUMNS view', 'YES', NULL, ''),"
            + "('F021', 'Basic information schema', '02', 'TABLES view', 'YES', NULL, ''),"
            + "('T655', 'Cyclically dependent routines', '', '', 'NO', NULL, ''),"
            + "('F021', 'Basic information schema', '03', 'VIEWS view', 'YES', NULL, ''),"
            + "('F021', 'Basic information schema', '04', 'TABLE_CONSTRAINTS view', 'YES', NULL, ''),"
            + "('F021', 'Basic information schema', '05', 'REFERENTIAL_CONSTRAINTS view', 'YES', NULL, ''),"
            + "('F021', 'Basic information schema', '06', 'CHECK_CONSTRAINTS view', 'YES', NULL, ''),"
            + "('F031', 'Basic schema manipulation', '', '', 'YES', NULL, ''),"
            + "('F031', 'Basic schema manipulation', '01', 'CREATE TABLE statement to create persistent base tables', 'YES', NULL, ''),"
            + "('F031', 'Basic schema manipulation', '02', 'CREATE VIEW statement', 'YES', NULL, ''),"
            + "('F031', 'Basic schema manipulation', '03', 'GRANT statement', 'YES', NULL, ''),"
            + "('F031', 'Basic schema manipulation', '04', 'ALTER TABLE statement: ADD COLUMN clause', 'YES', NULL, ''),"
            + "('F031', 'Basic schema manipulation', '13', 'DROP TABLE statement: RESTRICT clause', 'YES', NULL, ''),"
            + "('F031', 'Basic schema manipulation', '16', 'DROP VIEW statement: RESTRICT clause', 'YES', NULL, ''),"
            + "('F031', 'Basic schema manipulation', '19', 'REVOKE statement: RESTRICT clause', 'YES', NULL, ''),"
            + "('F032', 'CASCADE drop behavior', '', '', 'YES', NULL, ''),"
            + "('F033', 'ALTER TABLE statement: DROP COLUMN clause', '', '', 'YES', NULL, ''),"
            + "('F034', 'Extended REVOKE statement', '', '', 'YES', NULL, ''),"
            + "('F034', 'Extended REVOKE statement', '01', 'REVOKE statement performed by other than the owner of a schema object', 'YES', NULL, ''),"
            + "('F034', 'Extended REVOKE statement', '02', 'REVOKE statement: GRANT OPTION FOR clause', 'YES', NULL, ''),"
            + "('F034', 'Extended REVOKE statement', '03', 'REVOKE statement to revoke a privilege that the grantee has WITH GRANT OPTION', 'YES', NULL, ''),"
            + "('F041', 'Basic joined table', '', '', 'YES', NULL, ''),"
            + "('F041', 'Basic joined table', '01', 'Inner join (but not necessarily the INNER keyword)', 'YES', NULL, ''),"
            + "('F041', 'Basic joined table', '02', 'INNER keyword', 'YES', NULL, ''),"
            + "('F041', 'Basic joined table', '03', 'LEFT OUTER JOIN', 'YES', NULL, ''),"
            + "('F041', 'Basic joined table', '04', 'RIGHT OUTER JOIN', 'YES', NULL, ''),"
            + "('F041', 'Basic joined table', '05', 'Outer joins can be nested', 'YES', NULL, ''),"
            + "('F041', 'Basic joined table', '07', 'The inner table in a left or right outer join can also be used in an inner join', 'YES', NULL, ''),"
            + "('F041', 'Basic joined table', '08', 'All comparison operators are supported (rather than just =)', 'YES', NULL, ''),"
            + "('F051', 'Basic date and time', '', '', 'YES', NULL, ''),"
            + "('F051', 'Basic date and time', '01', 'DATE data type (including support of DATE literal)', 'YES', NULL, ''),"
            + "('F051', 'Basic date and time', '02', 'TIME data type (including support of TIME literal) with fractional seconds precision of at least 0', 'YES', NULL, ''),"
            + "('F051', 'Basic date and time', '03', 'TIMESTAMP data type (including support of TIMESTAMP literal) with fractional seconds precision of at least 0 and 6', 'YES', NULL, ''),"
            + "('F051', 'Basic date and time', '04', 'Comparison predicate on DATE, TIME, and TIMESTAMP data types', 'YES', NULL, ''),"
            + "('F051', 'Basic date and time', '05', 'Explicit CAST between datetime types and character string types', 'YES', NULL, ''),"
            + "('F051', 'Basic date and time', '06', 'CURRENT_DATE', 'YES', NULL, ''),"
            + "('F051', 'Basic date and time', '07', 'LOCALTIME', 'YES', NULL, ''),"
            + "('F051', 'Basic date and time', '08', 'LOCALTIMESTAMP', 'YES', NULL, ''),"
            + "('F052', 'Intervals and datetime arithmetic', '', '', 'YES', NULL, ''),"
            + "('F053', 'OVERLAPS predicate', '', '', 'YES', NULL, ''),"
            + "('F081', 'UNION and EXCEPT in views', '', '', 'YES', NULL, ''),"
            + "('F111', 'Isolation levels other than SERIALIZABLE', '', '', 'YES', NULL, ''),"
            + "('F111', 'Isolation levels other than SERIALIZABLE', '01', 'READ UNCOMMITTED isolation level', 'YES', NULL, ''),"
            + "('F111', 'Isolation levels other than SERIALIZABLE', '02', 'READ COMMITTED isolation level', 'YES', NULL, ''),"
            + "('F111', 'Isolation levels other than SERIALIZABLE', '03', 'REPEATABLE READ isolation level', 'YES', NULL, ''),"
            + "('F121', 'Basic diagnostics management', '', '', 'NO', NULL, ''),"
            + "('F121', 'Basic diagnostics management', '01', 'GET DIAGNOSTICS statement', 'NO', NULL, ''),"
            + "('F121', 'Basic diagnostics management', '02', 'SET TRANSACTION statement: DIAGNOSTICS SIZE clause', 'NO', NULL, ''),"
            + "('F131', 'Grouped operations', '', '', 'YES', NULL, ''),"
            + "('F131', 'Grouped operations', '01', 'WHERE, GROUP BY, and HAVING clauses supported in queries with grouped views', 'YES', NULL, ''),"
            + "('F131', 'Grouped operations', '02', 'Multiple tables supported in queries with grouped views', 'YES', NULL, ''),"
            + "('F131', 'Grouped operations', '03', 'Set functions supported in queries with grouped views', 'YES', NULL, ''),"
            + "('F131', 'Grouped operations', '04', 'Subqueries with GROUP BY and HAVING clauses and grouped views', 'YES', NULL, ''),"
            + "('F131', 'Grouped operations', '05', 'Single row SELECT with GROUP BY and HAVING clauses and grouped views', 'YES', NULL, ''),"
            + "('F171', 'Multiple schemas per user', '', '', 'YES', NULL, ''),"
            + "('F181', 'Multiple module support', '', '', 'NO', NULL, ''),"
            + "('F191', 'Referential delete actions', '', '', 'YES', NULL, ''),"
            + "('F201', 'CAST function', '', '', 'YES', NULL, ''),"
            + "('F221', 'Explicit defaults', '', '', 'YES', NULL, ''),"
            + "('F222', 'INSERT statement: DEFAULT VALUES clause', '', '', 'YES', NULL, ''),"
            + "('F231', 'Privilege tables', '', '', 'YES', NULL, ''),"
            + "('F231', 'Privilege tables', '01', 'TABLE_PRIVILEGES view', 'YES', NULL, ''),"
            + "('F231', 'Privilege tables', '02', 'COLUMN_PRIVILEGES view', 'YES', NULL, ''),"
            + "('F231', 'Privilege tables', '03', 'USAGE_PRIVILEGES view', 'YES', NULL, ''),"
            + "('F251', 'Domain support', '', '', 'YES', NULL, ''),"
            + "('F261', 'CASE expression', '', '', 'YES', NULL, ''),"
            + "('F261', 'CASE expression', '01', 'Simple CASE', 'YES', NULL, ''),"
            + "('F261', 'CASE expression', '02', 'Searched CASE', 'YES', NULL, ''),"
            + "('F261', 'CASE expression', '03', 'NULLIF', 'YES', NULL, ''),"
            + "('F261', 'CASE expression', '04', 'COALESCE', 'YES', NULL, ''),"
            + "('F262', 'Extended CASE expression', '', '', 'YES', NULL, ''),"
            + "('F263', 'Comma-separated predicates in simple CASE expression', '', '', 'YES', NULL, ''),"
            + "('F271', 'Compound character literals', '', '', 'YES', NULL, ''),"
            + "('F281', 'LIKE enhancements', '', '', 'YES', NULL, ''),"
            + "('F291', 'UNIQUE predicate', '', '', 'YES', NULL, ''),"
            + "('F301', 'CORRESPONDING in query expressions', '', '', 'NO', NULL, ''),"
            + "('F302', 'INTERSECT table operator', '', '', 'YES', NULL, ''),"
            + "('F302', 'INTERSECT table operator', '01', 'INTERSECT DISTINCT table operator', 'YES', NULL, ''),"
            + "('F302', 'INTERSECT table operator', '02', 'INTERSECT ALL table operator', 'YES', NULL, ''),"
            + "('F304', 'EXCEPT ALL table operator', '', '', 'YES', NULL, ''),"
            + "('F311', 'Schema definition statement', '', '', 'NO', NULL, ''),"
            + "('F311', 'Schema definition statement', '01', 'CREATE SCHEMA', 'YES', NULL, ''),"
            + "('F311', 'Schema definition statement', '02', 'CREATE TABLE for persistent base tables', 'YES', NULL, ''),"
            + "('F311', 'Schema definition statement', '03', 'CREATE VIEW', 'YES', NULL, ''),"
            + "('F311', 'Schema definition statement', '04', 'CREATE VIEW: WITH CHECK OPTION', 'NO', NULL, ''),"
            + "('F311', 'Schema definition statement', '05', 'GRANT statement', 'YES', NULL, ''),"
            + "('F312', 'MERGE statement', '', '', 'YES', NULL, ''),"
            + "('F321', 'User authorization', '', '', 'YES', NULL, ''),"
            + "('F341', 'Usage tables', '', '', 'YES', NULL, ''),"
            + "('F361', 'Subprogram support', '', '', 'YES', NULL, ''),"
            + "('F381', 'Extended schema manipulation', '', '', 'YES', NULL, ''),"
            + "('F381', 'Extended schema manipulation', '01', 'ALTER TABLE statement: ALTER COLUMN clause', 'YES', NULL, ''),"
            + "('F381', 'Extended schema manipulation', '02', 'ALTER TABLE statement: ADD CONSTRAINT clause', 'YES', NULL, ''),"
            + "('F381', 'Extended schema manipulation', '03', 'ALTER TABLE statement: DROP CONSTRAINT clause', 'YES', NULL, ''),"
            + "('F391', 'Long identifiers', '', '', 'YES', NULL, ''),"
            + "('F392', 'Unicode escapes in identifiers', '', '', 'YES', NULL, ''),"
            + "('F393', 'Unicode escapes in literals', '', '', 'YES', NULL, ''),"
            + "('F401', 'Extended joined table', '', '', 'YES', NULL, ''),"
            + "('F401', 'Extended joined table', '01', 'NATURAL JOIN', 'YES', NULL, ''),"
            + "('F401', 'Extended joined table', '02', 'FULL OUTER JOIN', 'YES', NULL, ''),"
            + "('F401', 'Extended joined table', '04', 'CROSS JOIN', 'YES', NULL, ''),"
            + "('F402', 'Named column joins for LOBs, arrays, and multisets', '', '', 'NO', NULL, ''),"
            + "('F411', 'Time zone specification', '', '', 'YES', NULL, ''),"
            + "('F421', 'National character', '', '', 'YES', NULL, ''),"
            + "('F431', 'Read-only scrollable cursors', '', '', 'NO', NULL, 'No direct support, but fully supported in JDBC'),"
            + "('F431', 'Read-only scrollable cursors', '01', 'FETCH with explicit NEXT', 'NO', NULL, ''),"
            + "('F431', 'Read-only scrollable cursors', '02', 'FETCH FIRST', 'NO', NULL, ''),"
            + "('F431', 'Read-only scrollable cursors', '03', 'FETCH LAST', 'NO', NULL, ''),"
            + "('F431', 'Read-only scrollable cursors', '04', 'FETCH PRIOR', 'NO', NULL, ''),"
            + "('F431', 'Read-only scrollable cursors', '05', 'FETCH ABSOLUTE', 'NO', NULL, ''),"
            + "('F431', 'Read-only scrollable cursors', '06', 'FETCH RELATIVE', 'NO', NULL, ''),"
            + "('F441', 'Extended set function support', '', '', 'YES', NULL, ''),"
            + "('F442', 'Mixed column references in set functions', '', '', 'YES', NULL, ''),"
            + "('F451', 'Character set definition', '', '', 'NO', NULL, ''),"
            + "('F461', 'Named character sets', '', '', 'YES', NULL, ''),"
            + "('F471', 'Scalar subquery values', '', '', 'YES', NULL, ''),"
            + "('F481', 'Expanded NULL predicate', '', '', 'YES', NULL, ''),"
            + "('F491', 'Constraint management', '', '', 'YES', NULL, ''),"
            + "('F501', 'Features and conformance views', '', '', 'YES', NULL, ''),"
            + "('F501', 'Features and conformance views', '01', 'SQL_FEATURES view', 'YES', NULL, ''),"
            + "('F501', 'Features and conformance views', '02', 'SQL_SIZING view', 'YES', NULL, ''),"
            + "('F501', 'Features and conformance views', '03', 'SQL_LANGUAGES view', 'YES', NULL, ''),"
            + "('F502', 'Enhanced documentation tables', '', '', 'NO', NULL, ''),"
            + "('F502', 'Enhanced documentation tables', '01', 'SQL_SIZING_PROFILES view', 'NO', NULL, ''),"
            + "('F502', 'Enhanced documentation tables', '02', 'SQL_IMPLEMENTATION_INFO view', 'NO', NULL, ''),"
            + "('F502', 'Enhanced documentation tables', '03', 'SQL_PACKAGES view', 'NO', NULL, ''),"
            + "('F521', 'Assertions', '', '', 'NO', NULL, ''),"
            + "('F531', 'Temporary tables', '', '', 'YES', NULL, ''),"
            + "('F555', 'Enhanced seconds precision', '', '', 'YES', NULL, ''),"
            + "('F561', 'Full value expressions', '', '', 'YES', NULL, ''),"
            + "('F571', 'Truth value tests', '', '', 'YES', NULL, ''),"
            + "('F591', 'Derived tables', '', '', 'YES', NULL, ''),"
            + "('F611', 'Indicator data types', '', '', 'NO', NULL, ''),"
            + "('F641', 'Row and table constructors', '', '', 'YES', NULL, ''),"
            + "('F651', 'Catalog name qualifiers', '', '', 'YES', NULL, ''),"
            + "('F661', 'Simple tables', '', '', 'YES', NULL, ''),"
            + "('F671', 'Subqueries in CHECK', '', '', 'NO', NULL, ''),"
            + "('F672', 'Retrospective check constraints', '', '', 'YES', NULL, ''),"
            + "('F691', 'Collation and translation', '', '', 'NO', NULL, ''),"
            + "('F692', 'Enhanced collation support', '', '', 'NO', NULL, ''),"
            + "('F693', 'SQL-session and client module collations', '', '', 'NO', NULL, ''),"
            + "('F695', 'Translation support', '', '', 'NO', NULL, ''),"
            + "('F696', 'Additional translation documentation', '', '', 'NO', NULL, ''),"
            + "('F701', 'Referential update actions', '', '', 'YES', NULL, ''),"
            + "('F711', 'ALTER domain', '', '', 'YES', NULL, ''),"
            + "('F721', 'Deferrable constraints', '', '', 'NO', NULL, ''),"
            + "('F731', 'INSERT column privileges', '', '', 'YES', NULL, ''),"
            + "('F741', 'Referential MATCH types', '', '', 'NO', NULL, ''),"
            + "('F751', 'View CHECK enhancements', '', '', 'NO', NULL, ''),"
            + "('F761', 'Session management', '', '', 'NO', NULL, ''),"
            + "('F771', 'Connection management', '', '', 'NO', NULL, ''),"
            + "('F762', 'CURRENT_CATALOG', '','',  'YES', NULL, ''),"
            + "('F763', 'CURRENT_SCHEMA', '','',  'YES', NULL, ''),"
            + "('F781', 'Self-referencing operations', '', '', 'NO', NULL, ''),"
            + "('F791', 'Insensitive cursors', '', '', 'NO', NULL, ''),"
            + "('F801', 'Full set function', '', '', 'YES', NULL, ''),"
            + "('F811', 'Extended flagging', '', '', 'NO', NULL, ''),"
            + "('F812', 'Basic flagging', '', '', 'NO', NULL, ''),"
            + "('F813', 'Extended flagging', '', '', 'NO', NULL, ''),"
            + "('F821', 'Local table references', '', '', 'NO', NULL, ''),"
            + "('F831', 'Full cursor update', '', '', 'NO', NULL, ''),"
            + "('F831', 'Full cursor update', '01', 'Updatable scrollable cursors', 'NO', NULL, ''),"
            + "('F831', 'Full cursor update', '02', 'Updatable ordered cursors', 'NO', NULL, ''),"
            + "('F850', 'Top-level <order by clause> in <query expression>', '', '', 'YES', NULL, ''),"
            + "('F851', '<order by clause> in subqueries', '', '', 'YES', NULL, ''),"
            + "('F852', 'Top-level <order by clause> in views', '', '', 'YES', NULL, ''),"
            + "('F855', 'Nested <order by clause> in <query expression>', '', '', 'YES', NULL, ''),"
            + "('F856', 'Nested <fetch first clause> in <query expression>', '', '', 'YES', NULL, ''),"
            + "('F857', 'Top-level <fetch first clause> in <query expression>', '', '', 'YES', NULL, ''),"
            + "('F858', '<fetch first clause> in subqueries', '', '', 'YES', NULL, ''),"
            + "('F859', 'Top-level <fetch first clause> in views', '', '', 'YES', NULL, ''),"
            + "('S011', 'Distinct data types', '', '', 'YES', NULL, ''),"
            + "('S011', 'Distinct data types', '01', 'USER_DEFINED_TYPES view', 'YES', NULL, ''),"
            + "('S023', 'Basic structured types', '', '', 'NO', NULL, ''),"
            + "('S024', 'Enhanced structured types', '', '', 'NO', NULL, ''),"
            + "('S025', 'Final structured types', '', '', 'NO', NULL, ''),"
            + "('S026', 'Self-referencing structured types', '', '', 'NO', NULL, ''),"
            + "('S027', 'Create method by specific method name', '', '', 'NO', NULL, ''),"
            + "('S028', 'Permutable UDT options list', '', '', 'NO', NULL, ''),"
            + "('S041', 'Basic reference types', '', '', 'NO', NULL, ''),"
            + "('S043', 'Enhanced reference types', '', '', 'NO', NULL, ''),"
            + "('S051', 'Create table of type', '', '', 'NO', NULL, ''),"
            + "('S071', 'SQL paths in function and type name resolution', '', '', 'YES', NULL, ''),"
            + "('S081', 'Subtables', '', '', 'NO', NULL, ''),"
            + "('S091', 'Basic array support', '', '', 'NO', NULL, ''),"
            + "('S091', 'Basic array support', '01', 'Arrays of built-in data types', 'NO', NULL, ''),"
            + "('S091', 'Basic array support', '02', 'Arrays of distinct types', 'NO', NULL, ''),"
            + "('S091', 'Basic array support', '03', 'Array expressions', 'NO', NULL, ''),"
            + "('S092', 'Arrays of user-defined types', '', '', 'NO', NULL, ''),"
            + "('S094', 'Arrays of reference types', '', '', 'NO', NULL, ''),"
            + "('S095', 'Array constructors by query', '', '', 'NO', NULL, ''),"
            + "('S096', 'Optional array bounds', '', '', 'NO', NULL, ''),"
            + "('S097', 'Array element assignment', '', '', 'NO', NULL, ''),"
            + "('S111', 'ONLY in query expressions', '', '', 'NO', NULL, ''),"
            + "('S151', 'Type predicate', '', '', 'NO', NULL, ''),"
            + "('S161', 'Subtype treatment', '', '', 'NO', NULL, ''),"
            + "('S162', 'Subtype treatment for references', '', '', 'NO', NULL, ''),"
            + "('S201', 'SQL-invoked routines on arrays', '', '', 'NO', NULL, ''),"
            + "('S201', 'SQL-invoked routines on arrays', '01', 'Array parameters', 'NO', NULL, ''),"
            + "('S201', 'SQL-invoked routines on arrays', '02', 'Array as result type of functions', 'NO', NULL, ''),"
            + "('S202', 'SQL-invoked routines on multisets', '', '', 'NO', NULL, ''),"
            + "('S211', 'User-defined cast functions', '', '', 'NO', NULL, ''),"
            + "('S231', 'Structured type locators', '', '', 'NO', NULL, ''),"
            + "('S232', 'Array locators', '', '', 'NO', NULL, ''),"
            + "('S233', 'Multiset locators', '', '', 'NO', NULL, ''),"
            + "('S241', 'Transform functions', '', '', 'NO', NULL, ''),"
            + "('S242', 'Alter transform statement', '', '', 'NO', NULL, ''),"
            + "('S251', 'User-defined orderings', '', '', 'NO', NULL, ''),"
            + "('S261', 'Specific type method', '', '', 'NO', NULL, ''),"
            + "('S271', 'Basic multiset support', '', '', 'NO', NULL, ''),"
            + "('S272', 'Multisets of user-defined types', '', '', 'NO', NULL, ''),"
            + "('S274', 'Multisets of reference types', '', '', 'NO', NULL, ''),"
            + "('S275', 'Advanced multiset support', '', '', 'NO', NULL, ''),"
            + "('S281', 'Nested collection types', '', '', 'NO', NULL, ''),"
            + "('S291', 'Unique constraint on entire row', '', '', 'NO', NULL, ''),"
            + "('T011', 'Timestamp in Information Schema', '', '', 'YES', NULL, ''),"
            + "('T021', 'BINARY and VARBINARY data types', '', '', 'YES', NULL, ''),"
            + "('T022', 'Advanced BINARY and VARBINARY data type support', '', '', 'YES', NULL, ''),"
            + "('T023', 'Compound binary literals', '', '', 'YES', NULL, ''),"
            + "('T024', 'Spaces in binary literals', '', '', 'YES', NULL, ''),"
            + "('T031', 'BOOLEAN data type', '', '', 'YES', NULL, ''),"
            + "('T041', 'Basic LOB data type support', '', '', 'YES', NULL, ''),"
            + "('T041', 'Basic LOB data type support', '01', 'BLOB data type', 'YES', NULL, ''),"
            + "('T041', 'Basic LOB data type support', '02', 'CLOB data type', 'YES', NULL, ''),"
            + "('T041', 'Basic LOB data type support', '03', 'POSITION, LENGTH, LOWER, TRIM, UPPER, and SUBSTRING functions for LOB data types', 'YES', NULL, ''),"
            + "('T041', 'Basic LOB data type support', '04', 'Concatenation of LOB data types', 'YES', NULL, ''),"
            + "('T041', 'Basic LOB data type support', '05', 'LOB locator: non-holdable', 'YES', NULL, ''),"
            + "('T042', 'Extended LOB data type support', '', '', 'NO', NULL, ''),"
            + "('T051', 'Row types', '', '', 'NO', NULL, ''),"
            + "('T052', 'MAX and MIN for row types', '', '', 'NO', NULL, ''),"
            + "('T053', 'Explicit aliases for all-fields reference', '', '', 'NO', NULL, ''),"
            + "('T061', 'UCS support', '', '', 'YES', NULL, ''),"
            + "('T071', 'BIGINT data type', '', '', 'YES', NULL, ''),"
            + "('T111', 'Updatable joins, unions, and columns', '', '', 'NO', NULL, ''),"
            + "('T121', 'WITH (excluding RECURSIVE) in query expression', '', '', 'NO', NULL, ''),"
            + "('T122', 'WITH (excluding RECURSIVE) in subquery', '', '', 'NO', NULL, ''),"
            + "('T131', 'Recursive query', '', '', 'NO', NULL, ''),"
            + "('T132', 'Recursive query in subquery', '', '', 'NO', NULL, ''),"
            + "('T141', 'SIMILAR predicate', '', '', 'YES', NULL, ''),"
            + "('T151', 'DISTINCT predicate', '', '', 'YES', NULL, ''),"
            + "('T152', 'DISTINCT predicate with negation', '', '', 'YES', NULL, ''),"
            + "('T171', 'LIKE clause in table definition', '', '', 'YES', NULL, ''),"
            + "('T172', 'AS subquery clause in table definition', '', '', 'YES', NULL, ''),"
            + "('T173', 'Extended LIKE clause in table definition', '', '', 'NO', NULL, ''),"
            + "('T174', 'Identity columns', '', '', 'YES', NULL, ''),"
            + "('T175', 'Generated columns', '', '', 'NO', NULL, ''),"
            + "('T176', 'Sequence generator support', '', '', 'YES', NULL, ''),"
            + "('T191', 'Referential action RESTRICT', '', '', 'YES', NULL, ''),"
            + "('T201', 'Comparable data types for referential constraints', '', '', 'YES', NULL, ''),"
            + "('T211', 'Basic trigger capability', '', '', 'YES', NULL, ''),"
            + "('T211', 'Basic trigger capability', '01', 'Triggers activated on UPDATE, INSERT, or DELETE of one base table', 'YES', NULL, ''),"
            + "('T211', 'Basic trigger capability', '02', 'BEFORE triggers', 'YES', NULL, ''),"
            + "('T211', 'Basic trigger capability', '03', 'AFTER triggers', 'YES', NULL, ''),"
            + "('T211', 'Basic trigger capability', '04', 'FOR EACH ROW triggers', 'YES', NULL, ''),"
            + "('T211', 'Basic trigger capability', '05', 'Ability to specify a search condition that must be true before the trigger is invoked', 'YES', NULL, ''),"
            + "('T211', 'Basic trigger capability', '06', 'Support for run-time rules for the interaction of triggers and constraints', 'YES', NULL, ''),"
            + "('T211', 'Basic trigger capability', '07', 'TRIGGER privilege', 'YES', NULL, ''),"
            + "('T211', 'Basic trigger capability', '08', 'Multiple triggers for the same event are executed in the order in which they were created in the catalog', 'YES', NULL, ''),"
            + "('T212', 'Enhanced trigger capability', '', '', 'YES', NULL, ''),"
            + "('T231', 'Sensitive cursors', '', '', 'YES', NULL, ''),"
            + "('T241', 'START TRANSACTION statement', '', '', 'YES', NULL, ''),"
            + "('T251', 'SET TRANSACTION statement: LOCAL option', '', '', 'NO', NULL, ''),"
            + "('T261', 'Chained transactions', '', '', 'NO', NULL, ''),"
            + "('T271', 'Savepoints', '', '', 'YES', NULL, ''),"
            + "('T272', 'Enhanced savepoint management', '', '', 'YES', NULL, ''),"
            + "('T281', 'SELECT privilege with column granularity', '', '', 'YES', NULL, ''),"
            + "('T301', 'Functional dependencies', '', '', 'YES', NULL, ''),"
            + "('T312', 'OVERLAY function', '', '', 'YES', NULL, ''),"
            + "('T321', 'Basic SQL-invoked routines', '', '', 'NO', NULL, ''),"
            + "('T321', 'Basic SQL-invoked routines', '01', 'User-defined functions with no overloading', 'NO', NULL, ''),"
            + "('T321', 'Basic SQL-invoked routines', '02', 'User-defined stored procedures with no overloading', 'NO', NULL, ''),"
            + "('T321', 'Basic SQL-invoked routines', '03', 'Function invocation', 'NO', NULL, ''),"
            + "('T321', 'Basic SQL-invoked routines', '04', 'CALL statement', 'NO', NULL, ''),"
            + "('T321', 'Basic SQL-invoked routines', '05', 'RETURN statement', 'NO', NULL, ''),"
            + "('T321', 'Basic SQL-invoked routines', '06', 'ROUTINES view', 'YES', NULL, ''),"
            + "('T321', 'Basic SQL-invoked routines', '07', 'PARAMETERS view', 'YES', NULL, ''),"
            + "('T322', 'Overloading of SQL-invoked functions and procedures', '', '', 'YES', NULL, ''),"
            + "('T323', 'Explicit security for external routines', '', '', 'YES', NULL, ''),"
            + "('T324', 'Explicit security for SQL routines', '', '', 'NO', NULL, ''),"
            + "('T325', 'Qualified SQL parameter references', '', '', 'NO', NULL, ''),"
            + "('T326', 'Table functions', '', '', 'NO', NULL, ''),"
            + "('T331', 'Basic roles', '', '', 'YES', NULL, ''),"
            + "('T332', 'Extended roles', '', '', 'NO', NULL, ''),"
            + "('T351', 'Bracketed SQL comments (/*...*/ comments)', '', '', 'YES', NULL, ''),"
            + "('T401', 'INSERT into a cursor', '', '', 'NO', NULL, ''),"
            + "('T411', 'UPDATE statement: SET ROW option', '', '', 'NO', NULL, ''),"
            + "('T431', 'Extended grouping capabilities', '', '', 'NO', NULL, ''),"
            + "('T432', 'Nested and concatenated GROUPING SETS', '', '', 'NO', NULL, ''),"
            + "('T433', 'Multiargument GROUPING function', '', '', 'NO', NULL, ''),"
            + "('T434', 'GROUP BY DISINCT', '', '', 'NO', NULL, ''),"
            + "('T441', 'ABS and MOD functions', '', '', 'YES', NULL, ''),"
            + "('T461', 'Symmetric BETWEEN predicate', '', '', 'YES', NULL, ''),"
            + "('T471', 'Result sets return value', '', '', 'NO', NULL, ''),"
            + "('T491', 'LATERAL derived table', '', '', 'NO', NULL, ''),"
            + "('T501', 'Enhanced EXISTS predicate', '', '', 'YES', NULL, ''),"
            + "('T511', 'Transaction counts', '', '', 'NO', NULL, ''),"
            + "('T541', 'Updatable table references', '', '', 'NO', NULL, ''),"
            + "('T551', 'Optional key words for default syntax', '', '', 'YES', NULL, ''),"
            + "('T561', 'Holdable locators', '', '', 'NO', NULL, ''),"
            + "('T571', 'Array-returning external SQL-invoked functions', '', '', 'NO', NULL, ''),"
            + "('T572', 'Multiset-returning external SQL-invoked functions', '', '', 'NO', NULL, ''),"
            + "('T581', 'Regular expression substring function', '', '', 'YES', NULL, ''),"
            + "('T591', 'UNIQUE constraints of possibly null columns', '', '', 'YES', NULL, ''),"
            + "('T601', 'Local cursor references', '', '', 'NO', NULL, ''),"
            + "('T611', 'Elementary OLAP operations', '', '', 'NO', NULL, ''),"
            + "('T612', 'Advanced OLAP operations', '', '', 'NO', NULL, ''),"
            + "('T613', 'Sampling', '', '', 'NO', NULL, ''),"
            + "('T621', 'Enhanced numeric functions', '', '', 'NO', NULL, ''),"
            + "('T631', 'IN predicate with one list element', '', '', 'YES', NULL, ''),"
            + "('T641', 'Multiple column assignment', '', '', 'YES', NULL, ''),"
            + "('T651', 'SQL-schema statements in SQL routines', '', '', 'NO', NULL, ''),"
            + "('T652', 'SQL-dynamic statements in SQL routines', '', '', 'NO', NULL, ''),"
            + "('T653', 'SQL-schema statements in external routines', '', '', 'NO', NULL, ''),"
            + "('T654', 'SQL-dynamic statements in external routines', '', '', 'NO', NULL, '');");

        t.insertSys(store, rs);

        return t;
    }

    Table SQL_IMPLEMENTATION_INFO() throws HsqlException {

        Table t = sysTables[SQL_IMPLEMENTATION_INFO];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SQL_IMPLEMENTATION_INFO]);

            addColumn(t, "IMPLEMENTATION_INFO_ID", CHARACTER_DATA);
            addColumn(t, "IMPLEMENTATION_INFO_NAME", CHARACTER_DATA);
            addColumn(t, "INTEGER_VALUE", CARDINAL_NUMBER);
            addColumn(t, "CHARACTER_VALUE", CHARACTER_DATA);
            addColumn(t, "COMMENTS", CHARACTER_DATA);
            t.createPrimaryKey(null, new int[]{ 0 }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());
        Session sys = database.sessionManager.newSysSession(
            SqlInvariants.INFORMATION_SCHEMA_HSQLNAME, session.getUser());

/*
        Result rs = sys.executeDirectStatement(
            "VALUES "
            + ";");

        t.insertSys(store, rs);
*/
        return t;
    }

    Table SQL_PACKAGES() throws HsqlException {

        Table t = sysTables[SQL_PACKAGES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SQL_PACKAGES]);

            addColumn(t, "ID", CHARACTER_DATA);
            addColumn(t, "NAME", CHARACTER_DATA);
            addColumn(t, "IS_SUPPORTED", YES_OR_NO);
            addColumn(t, "IS_VERIFIED_BY", CHARACTER_DATA);
            addColumn(t, "COMMENTS", CHARACTER_DATA);
            t.createPrimaryKey(null, new int[]{ 0 }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());
        Session sys = database.sessionManager.newSysSession(
            SqlInvariants.INFORMATION_SCHEMA_HSQLNAME, session.getUser());
        Result rs = sys.executeDirectStatement(
            "VALUES "
            + "( 'PKG001', 'Enhanced datetime facilities','YES', CAST(NULL AS CHARACTER), '' ),"
            + "( 'PKG002', 'Enhanced integrity management','YES', NULL, '' ),"
            + "( 'PKG004', 'PSM', 'NO', NULL, '' ),"
            + "( 'PKG006', 'Basic object support', 'NO', NULL, '' ),"
            + "( 'PKG007', 'Enhanced object support','NO', NULL, '' ),"
            + "( 'PKG008', 'Active database', 'YES', NULL, '' ),"
            + "( 'PKG010', 'OLAP', 'NO', NULL, '');");

        t.insertSys(store, rs);

        return t;
    }

    Table SQL_PARTS() throws HsqlException {

        Table t = sysTables[SQL_PARTS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SQL_PARTS]);

            addColumn(t, "PART", CHARACTER_DATA);
            addColumn(t, "NAME", CHARACTER_DATA);
            addColumn(t, "IS_SUPPORTED", YES_OR_NO);
            addColumn(t, "IS_VERIFIED_BY", CHARACTER_DATA);
            addColumn(t, "COMMENTS", CHARACTER_DATA);
            t.createPrimaryKey(null, new int[]{ 0 }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());
        Session sys = database.sessionManager.newSysSession(
            SqlInvariants.INFORMATION_SCHEMA_HSQLNAME, session.getUser());
        Result rs = sys.executeDirectStatement(
            "VALUES ( 'ISO9075-1', 'Framework','YES', CAST(NULL AS CHARACTER), '' ),"
            + "( 'ISO9075-2', 'Foundation','YES', NULL, '' ),"
            + "( 'ISO9075-3', 'Call-level interface','YES', NULL, '' ),"
            + "( 'ISO9075-4', 'Persistent Stored Modules', 'NO', NULL, '' ),"
            + "( 'ISO9075-9', 'Management of External Data', 'NO', NULL, '' ),"
            + "( 'ISO9075-10', 'Object Language Bindings,','NO', NULL, '' ),"
            + "( 'ISO9075-11', 'Information and Definition Schemas', 'YES', NULL, '' ),"
            + "( 'ISO9075-13', 'Routines & Types Using the Java Programming', 'NO', NULL, ''),"
            + "( 'ISO9075-14', 'XML-Related Specifications', 'NO', NULL, ''),"
            + ";");

        t.insertSys(store, rs);

        return t;
    }

    Table SQL_SIZING() throws HsqlException {

        Table t = sysTables[SQL_SIZING];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SQL_SIZING]);

            addColumn(t, "SIZING_ID", CARDINAL_NUMBER);
            addColumn(t, "SIZING_NAME", CHARACTER_DATA);
            addColumn(t, "SUPPORTED_VALUE", CARDINAL_NUMBER);
            addColumn(t, "COMMENTS", CHARACTER_DATA);
            t.createPrimaryKey(null, new int[]{ 0 }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());
        Session sys = database.sessionManager.newSysSession(
            SqlInvariants.INFORMATION_SCHEMA_HSQLNAME, session.getUser());
        Result rs = sys.executeDirectStatement(
            "VALUES ( 34, 'MAXIMUM CATALOG NAME LENGTH', 128,'Length in characters' ),"
            + "( 30, 'MAXIMUM COLUMN NAME LENGTH',128, NULL ),"
            + "( 97, 'MAXIMUM COLUMNS IN GROUP BY', 0, 'Limited by memory only' ),"
            + "( 99, 'MAXIMUM COLUMNS IN ORDER BY', 0, 'Limited by memory only' ),"
            + "( 100, 'MAXIMUM COLUMNS IN SELECT', 0,'Limited by memory only' ),"
            + "( 101, 'MAXIMUM COLUMNS IN TABLE', 0, 'Limited by memory only' ),"
            + "( 1, 'MAXIMUM CONCURRENT ACTIVITIES', 0, 'Limited by memory only'),"
            + "( 31, 'MAXIMUM CURSOR NAME LENGTH', 128, NULL),"
            + "( 0, 'MAXIMUM DRIVER CONNECTIONS', 0, 'Limited by memory only' ),"
            + "( 10005, 'MAXIMUM IDENTIFIER LENGTH', 128, NULL),"
            + "( 32, 'MAXIMUM SCHEMA NAME LENGTH', 128, NULL),"
            + "( 20000, 'MAXIMUM STATEMENT OCTETS', 0, 'Limited by memory only'),"
            + "( 20001, 'MAXIMUM STATEMENT OCTETS DATA', 0, 'Limited by memory only'),"
            + "( 20002, 'MAXIMUM STATEMENT OCTETS SCHEMA',0, 'Limited by memory only'),"
            + "( 35, 'MAXIMUM TABLE NAME LENGTH', 128, NULL),"
            + "( 106, 'MAXIMUM TABLES IN SELECT', 0, 'Limited by memory only'),"
            + "( 107, 'MAXIMUM USER NAME LENGTH', 128, NULL ),"
            + "( 25000, 'MAXIMUM CURRENT DEFAULT TRANSFORM GROUP LENGTH', NULL, NULL),"
            + "( 25001, 'MAXIMUM CURRENT TRANSFORM GROUP LENGTH',NULL, NULL),"
            + "( 25002, 'MAXIMUM CURRENT PATH LENGTH', NULL, NULL),"
            + "( 25003, 'MAXIMUM CURRENT ROLE LENGTH', 128, NULL),"
            + "( 25004, 'MAXIMUM SESSION USER LENGTH', 128, NULL),"
            + "( 25005, 'MAXIMUM SYSTEM USER LENGTH', 128, NULL);");

        t.insertSys(store, rs);

        return t;
    }

    Table SQL_SIZING_PROFILES() throws HsqlException {

        Table t = sysTables[SQL_SIZING_PROFILES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SQL_SIZING_PROFILES]);

            addColumn(t, "SIZING_ID", CARDINAL_NUMBER);
            addColumn(t, "SIZING_NAME", CHARACTER_DATA);
            addColumn(t, "PROFILE_ID", CARDINAL_NUMBER);
            addColumn(t, "PROFILE_NAME", CHARACTER_DATA);
            addColumn(t, "REQUIRED_VALUE", CARDINAL_NUMBER);
            addColumn(t, "COMMENTS", CHARACTER_DATA);
            t.createPrimaryKey(null, new int[]{ 0 }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());
        Session sys = database.sessionManager.newSysSession(
            SqlInvariants.INFORMATION_SCHEMA_HSQLNAME, session.getUser());

        /*
                Result rs = sys.executeDirectStatement(
                    "VALUES "
                    + ";");

                t.insertSys(store, rs);
        */
        return t;
    }

    /**
     * The TABLE_CONSTRAINTS table has one row for each table constraint
     * associated with a table.  <p>
     *
     * It effectively contains a representation of the table constraint
     * descriptors. <p>
     *
     * <b>Definition:</b> <p>
     *
     * <pre class="SqlCodeExample">
     * CREATE TABLE SYSTEM_TABLE_CONSTRAINTS (
     *      CONSTRAINT_CATALOG      VARCHAR NULL,
     *      CONSTRAINT_SCHEMA       VARCHAR NULL,
     *      CONSTRAINT_NAME         VARCHAR NOT NULL,
     *      CONSTRAINT_TYPE         VARCHAR NOT NULL,
     *      TABLE_CATALOG           VARCHAR NULL,
     *      TABLE_SCHEMA            VARCHAR NULL,
     *      TABLE_NAME              VARCHAR NOT NULL,
     *      IS_DEFERRABLE           VARCHAR NOT NULL,
     *      INITIALLY_DEFERRED      VARCHAR NOT NULL,
     *
     *      CHECK ( CONSTRAINT_TYPE IN
     *                      ( 'UNIQUE', 'PRIMARY KEY',
     *                        'FOREIGN KEY', 'CHECK' ) ),
     *
     *      CHECK ( ( IS_DEFERRABLE, INITIALLY_DEFERRED ) IN
     *              ( VALUES ( 'NO',  'NO'  ),
     *                       ( 'YES', 'NO'  ),
     *                       ( 'YES', 'YES' ) ) )
     * )
     * </pre>
     *
     * <b>Description:</b> <p>
     *
     * <ol>
     * <li> The values of CONSTRAINT_CATALOG, CONSTRAINT_SCHEMA, and
     *      CONSTRAINT_NAME are the catalog name, unqualified schema
     *      name, and qualified identifier, respectively, of the
     *      constraint being described. If the &lt;table constraint
     *      definition&gt; or &lt;add table constraint definition&gt;
     *      that defined the constraint did not specify a
     *      &lt;constraint name&gt;, then the values of CONSTRAINT_CATALOG,
     *      CONSTRAINT_SCHEMA, and CONSTRAINT_NAME are
     *      implementation-defined. <p>
     *
     * <li> The values of CONSTRAINT_TYPE have the following meanings: <p>
     *  <table border cellpadding="3">
     *  <tr>
     *      <td nowrap>FOREIGN KEY</td>
     *      <td nowrap>The constraint being described is a
     *                 foreign key constraint.</td>
     *  </tr>
     *  <tr>
     *      <td nowrap>UNIQUE</td>
     *      <td nowrap>The constraint being described is a
     *                 unique constraint.</td>
     *  </tr>
     *  <tr>
     *      <td nowrap>PRIMARY KEY</td>
     *      <td nowrap>The constraint being described is a
     *                 primary key constraint.</td>
     *  </tr>
     *  <tr>
     *      <td nowrap>CHECK</td>
     *      <td nowrap>The constraint being described is a
     *                 check constraint.</td>
     *  </tr>
     * </table> <p>
     *
     * <li> The values of TABLE_CATALOG, TABLE_SCHEMA, and TABLE_NAME are
     *      the catalog name, the unqualified schema name, and the
     *      qualified identifier of the name of the table to which the
     *      table constraint being described applies. <p>
     *
     * <li> The values of IS_DEFERRABLE have the following meanings: <p>
     *
     *  <table>
     *      <tr>
     *          <td nowrap>YES</td>
     *          <td nowrap>The table constraint is deferrable.</td>
     *      </tr>
     *      <tr>
     *          <td nowrap>NO</td>
     *          <td nowrap>The table constraint is not deferrable.</td>
     *      </tr>
     *  </table> <p>
     *
     * <li> The values of INITIALLY_DEFERRED have the following meanings: <p>
     *
     *  <table>
     *      <tr>
     *          <td nowrap>YES</td>
     *          <td nowrap>The table constraint is initially deferred.</td>
     *      </tr>
     *      <tr>
     *          <td nowrap>NO</td>
     *          <td nowrap>The table constraint is initially immediate.</td>
     *      </tr>
     *  </table> <p>
     * </ol>
     *
     * @throws HsqlException
     * @return Table
     */
    Table TABLE_CONSTRAINTS() throws HsqlException {

        Table t = sysTables[TABLE_CONSTRAINTS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[TABLE_CONSTRAINTS]);

            addColumn(t, "CONSTRAINT_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "CONSTRAINT_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "CONSTRAINT_NAME", SQL_IDENTIFIER);    // not null
            addColumn(t, "CONSTRAINT_TYPE", CHARACTER_DATA);    // not null
            addColumn(t, "TABLE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TABLE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TABLE_NAME", SQL_IDENTIFIER);         // not null
            addColumn(t, "IS_DEFERRABLE", YES_OR_NO);           // not null
            addColumn(t, "INITIALLY_DEFERRED", YES_OR_NO);      // not null

            // false PK, as CONSTRAINT_CATALOG, CONSTRAINT_SCHEMA,
            // TABLE_CATALOG and/or TABLE_SCHEMA may be null
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 4, 5, 6
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // Intermediate holders
        Iterator     tables;
        Table        table;
        Constraint[] constraints;
        int          constraintCount;
        Constraint   constraint;
        String       cat;
        String       schem;
        Object[]     row;

        // column number mappings
        final int constraint_catalog = 0;
        final int constraint_schema  = 1;
        final int constraint_name    = 2;
        final int constraint_type    = 3;
        final int table_catalog      = 4;
        final int table_schema       = 5;
        final int table_name         = 6;
        final int is_deferable       = 7;
        final int initially_deferred = 8;

        // initialization
        tables =
            database.schemaManager.databaseObjectIterator(SchemaObject.TABLE);
        table = null;    // else compiler complains

        // do it
        while (tables.hasNext()) {
            table = (Table) tables.next();

            // todo requires table level INSERT or UPDATE or DELETE or REFERENCES (not SELECT) right
            if (table.isView() || !isAccessibleTable(table)) {
                continue;
            }

            constraints     = table.getConstraints();
            constraintCount = constraints.length;

            for (int i = 0; i < constraintCount; i++) {
                constraint = constraints[i];
                row        = t.getEmptyRowData();

                switch (constraint.getConstraintType()) {

                    case Constraint.CHECK : {
                        row[constraint_type] = "CHECK";

                        break;
                    }
                    case Constraint.UNIQUE : {
                        row[constraint_type] = "UNIQUE";

                        break;
                    }
                    case Constraint.FOREIGN_KEY : {
                        row[constraint_type] = "FOREIGN KEY";
                        table                = constraint.getRef();

                        break;
                    }
                    case Constraint.PRIMARY_KEY : {
                        row[constraint_type] = "PRIMARY KEY";

                        break;
                    }
                    case Constraint.MAIN :
                    default : {
                        continue;
                    }
                }

                cat                     = database.getCatalogName().name;
                schem                   = table.getSchemaName().name;
                row[constraint_catalog] = cat;
                row[constraint_schema]  = schem;
                row[constraint_name]    = constraint.getName().name;
                row[table_catalog]      = cat;
                row[table_schema]       = schem;
                row[table_name]         = table.getName().name;
                row[is_deferable]       = Tokens.T_NO;
                row[initially_deferred] = Tokens.T_NO;

                t.insertSys(store, row);
            }
        }

        return t;
    }

    Table TRANSLATIONS() throws HsqlException {

        Table t = sysTables[TRANSLATIONS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[TRANSLATIONS]);

            addColumn(t, "TRANSLATION_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TRANSLATION_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TRANSLATION_NAME", SQL_IDENTIFIER);
            addColumn(t, "SOURCE_CHARACTER_SET_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "SOURCE_CHARACTER_SET_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "SOURCE_CHARACTER_SET_NAME", SQL_IDENTIFIER);
            addColumn(t, "TARGET_CHARACTER_SET_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TARGET_CHARACTER_SET_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TARGET_CHARACTER_SET_NAME", SQL_IDENTIFIER);
            addColumn(t, "TRANSLATION_SOURCE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TRANSLATION_SOURCE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TRANSLATION_SOURCE_NAME", SQL_IDENTIFIER);
            t.createPrimaryKey(null, new int[] {
                0, 1, 2
            }, false);

            return t;
        }

        return t;
    }

    Table TRIGGER_COLUMN_USAGE() throws HsqlException {

        Table t = sysTables[TRIGGER_COLUMN_USAGE];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[TRIGGER_COLUMN_USAGE]);

            addColumn(t, "TRIGGER_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TRIGGER_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TRIGGER_NAME", SQL_IDENTIFIER);    // not null
            addColumn(t, "TABLE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TABLE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TABLE_NAME", SQL_IDENTIFIER);      // not null
            addColumn(t, "COLUMN_NAME", SQL_IDENTIFIER);     // not null
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 3, 4, 5, 6
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // column number mappings
        final int trigger_catalog = 0;
        final int trigger_schems  = 1;
        final int trigger_name    = 2;
        final int table_catalog   = 3;
        final int table_schema    = 4;
        final int table_name      = 5;
        final int column_name     = 6;

        // Initialization
        return t;
    }

    Table TRIGGER_ROUTINE_USAGE() throws HsqlException {

        Table t = sysTables[TRIGGER_ROUTINE_USAGE];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[TRIGGER_ROUTINE_USAGE]);

            addColumn(t, "TRIGGER_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TRIGGER_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TRIGGER_NAME", SQL_IDENTIFIER);     // not null
            addColumn(t, "SPECIFIC_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "SPECIFIC_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "SPECIFIC_NAME", SQL_IDENTIFIER);    // not null
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 3, 4, 5
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // column number mappings
        final int trigger_catalog  = 0;
        final int trigger_schems   = 1;
        final int trigger_name     = 2;
        final int specific_catalog = 3;
        final int specific_schema  = 4;
        final int specific_name    = 5;

        // Initialization
        return t;
    }

    Table TRIGGER_SEQUENCE_USAGE() throws HsqlException {

        Table t = sysTables[TRIGGER_SEQUENCE_USAGE];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[TRIGGER_SEQUENCE_USAGE]);

            addColumn(t, "TRIGGER_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TRIGGER_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TRIGGER_NAME", SQL_IDENTIFIER);     // not null
            addColumn(t, "SEQUENCE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "SEQUENCE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "SEQUENCE_NAME", SQL_IDENTIFIER);    // not null
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 3, 4, 5
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // column number mappings
        final int trigger_catalog  = 0;
        final int trigger_schems   = 1;
        final int trigger_name     = 2;
        final int sequence_catalog = 3;
        final int sequence_schema  = 4;
        final int sequence_name    = 5;

        // Initialization
        return t;
    }

    Table TRIGGER_TABLE_USAGE() throws HsqlException {

        Table t = sysTables[TRIGGER_TABLE_USAGE];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[TRIGGER_TABLE_USAGE]);

            addColumn(t, "TRIGGER_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TRIGGER_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TRIGGER_NAME", SQL_IDENTIFIER);    // not null
            addColumn(t, "TABLE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TABLE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TABLE_NAME", SQL_IDENTIFIER);      // not null
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 3, 4, 5
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // column number mappings
        final int trigger_catalog = 0;
        final int trigger_schems  = 1;
        final int trigger_name    = 2;
        final int table_catalog   = 3;
        final int table_schema    = 4;
        final int table_name      = 5;

        // Initialization
        return t;
    }

    Table TRIGGERS() throws HsqlException {

        Table t = sysTables[TRIGGERS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[TRIGGERS]);

            addColumn(t, "TRIGGER_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TRIGGER_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TRIGGER_NAME", SQL_IDENTIFIER);
            addColumn(t, "EVENT_MANIPULATION", SQL_IDENTIFIER);
            addColumn(t, "EVENT_OBJECT_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "EVENT_OBJECT_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "ACTION_ORDER", CHARACTER_DATA);
            addColumn(t, "ACTION_CONDITION", CHARACTER_DATA);
            addColumn(t, "ACTION_STATEMENT", CHARACTER_DATA);
            addColumn(t, "ACTION_ORIENTATION", CHARACTER_DATA);
            addColumn(t, "ACTION_TIMING", CHARACTER_DATA);
            addColumn(t, "ACTION_REFERENCE_OLD_TABLE", SQL_IDENTIFIER);
            addColumn(t, "ACTION_REFERENCE_NEW_TABLE", SQL_IDENTIFIER);
            addColumn(t, "CREATED", TIME_STAMP);
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 3, 4, 5
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // column number mappings
        final int trigger_catalog      = 0;
        final int trigger_schems       = 1;
        final int trigger_name         = 2;
        final int event_manipulation   = 3;
        final int event_object_catalog = 4;
        final int event_object_schema  = 5;

        // Initialization
        return t;
    }

    Table TRIGGERED_UPDATE_COLUMNS() throws HsqlException {

        Table t = sysTables[TRIGGERED_UPDATE_COLUMNS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[TRIGGERED_UPDATE_COLUMNS]);

            addColumn(t, "TRIGGER_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TRIGGER_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TRIGGER_NAME", SQL_IDENTIFIER);            // not null
            addColumn(t, "EVENT_OBJECT_CATALOG", SQL_IDENTIFIER);    // not null
            addColumn(t, "EVENT_OBJECT_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "EVENT_OBJECT_TABLE", SQL_IDENTIFIER);
            addColumn(t, "EVENT_OBJECT_COLUMN", SQL_IDENTIFIER);     // not null
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 3, 4, 5, 6
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // column number mappings
        final int trigger_catalog      = 0;
        final int trigger_schems       = 1;
        final int trigger_name         = 2;
        final int event_object_catalog = 3;
        final int event_object_schema  = 4;
        final int event_object_table   = 5;
        final int event_object_column  = 6;

        // Initialization
        return t;
    }

    Table TYPE_JAR_USAGE() {
        return null;
    }

    /**
     * The USAGE_PRIVILEGES view has one row for each usage privilege
     * descriptor. <p>
     *
     * It effectively contains a representation of the usage privilege
     * descriptors. <p>
     *
     * <b>Definition:</b> <p>
     *
     * <pre class="SqlCodeExample">
     * CREATE TABLE SYSTEM_USAGE_PRIVILEGES (
     *      GRANTOR         VARCHAR NOT NULL,
     *      GRANTEE         VARCHAR NOT NULL,
     *      OBJECT_CATALOG  VARCHAR NULL,
     *      OBJECT_SCHEMA   VARCHAR NULL,
     *      OBJECT_NAME     VARCHAR NOT NULL,
     *      OBJECT_TYPE     VARCHAR NOT NULL
     *
     *          CHECK ( OBJECT_TYPE IN (
     *                      'DOMAIN',
     *                      'CHARACTER SET',
     *                      'COLLATION',
     *                      'TRANSLATION',
     *                      'SEQUENCE' ) ),
     *
     *      IS_GRANTABLE    VARCHAR NOT NULL
     *
     *          CHECK ( IS_GRANTABLE IN ( 'YES', 'NO' ) ),
     *
     *      UNIQUE( GRANTOR, GRANTEE, OBJECT_CATALOG,
     *              OBJECT_SCHEMA, OBJECT_NAME, OBJECT_TYPE )
     * )
     * </pre>
     *
     * <b>Description:</b><p>
     *
     * <ol>
     * <li> The value of GRANTOR is the &lt;authorization identifier&gt; of the
     *      user or role who granted usage privileges on the object of the type
     *      identified by OBJECT_TYPE that is identified by OBJECT_CATALOG,
     *      OBJECT_SCHEMA, and OBJECT_NAME, to the user or role identified by the
     *      value of GRANTEE forthe usage privilege being described. <p>
     *
     * <li> The value of GRANTEE is the &lt;authorization identifier&gt; of some
     *      user or role, or PUBLIC to indicate all users, to whom the usage
     *      privilege being described is granted. <p>
     *
     * <li> The values of OBJECT_CATALOG, OBJECT_SCHEMA, and OBJECT_NAME are the
     *      catalog name, unqualified schema name, and qualified identifier,
     *      respectively, of the object to which the privilege applies. <p>
     *
     * <li> The values of OBJECT_TYPE have the following meanings: <p>
     *
     *      <table border cellpadding="3">
     *          <tr>
     *              <td nowrap>DOMAIN</td>
     *              <td nowrap>The object to which the privilege applies is
     *                         a domain.</td>
     *          <tr>
     *          <tr>
     *              <td nowrap>CHARACTER SET</td>
     *              <td nowrap>The object to which the privilege applies is a
     *                         character set.</td>
     *          <tr>
     *          <tr>
     *              <td nowrap>COLLATION</td>
     *              <td nowrap>The object to which the privilege applies is a
     *                         collation.</td>
     *          <tr>
     *          <tr>
     *              <td nowrap>TRANSLATION</td>
     *              <td nowrap>The object to which the privilege applies is a
     *                         transliteration.</td>
     *          <tr>
     *          <tr>
     *              <td nowrap>SEQUENCE</td>
     *              <td nowrap>The object to which the privilege applies is a
     *                         sequence generator.</td>
     *          <tr>
     *      </table> <p>
     *
     * <li> The values of IS_GRANTABLE have the following meanings: <p>
     *
     *      <table border cellpadding="3">
     *          <tr>
     *              <td nowrap>YES</td>
     *              <td nowrap>The privilege being described was granted
     *                         WITH GRANT OPTION and is thus grantable.</td>
     *          <tr>
     *          <tr>
     *              <td nowrap>NO</td>
     *              <td nowrap>The privilege being described was not granted
     *                  WITH GRANT OPTION and is thus not grantable.</td>
     *          <tr>
     *      </table> <p>
     * <ol>
     *
     * @throws HsqlException
     * @return Table
     */
    Table USAGE_PRIVILEGES() throws HsqlException {

        Table t = sysTables[USAGE_PRIVILEGES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[USAGE_PRIVILEGES]);

            addColumn(t, "GRANTOR", SQL_IDENTIFIER);        // not null
            addColumn(t, "GRANTEE", SQL_IDENTIFIER);        // not null
            addColumn(t, "OBJECT_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "OBJECT_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "OBJECT_NAME", SQL_IDENTIFIER);    // not null
            addColumn(t, "OBJECT_TYPE", CHARACTER_DATA);    // not null
            addColumn(t, "PRIVILEGE_TYPE", CHARACTER_DATA);
            addColumn(t, "IS_GRANTABLE", YES_OR_NO);        // not null

            // order: COLUMN_NAME, PRIVILEGE
            // for unique: GRANTEE, GRANTOR, TABLE_NAME, TABLE_SCHEM, TABLE_CAT
            // false PK, as TABLE_SCHEM and/or TABLE_CAT may be null
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 3, 4, 5, 6, 7
            }, false);

            return t;
        }

        //
        Object[] row;

        //
        final int grantor        = 0;
        final int grantee        = 1;
        final int object_catalog = 2;
        final int object_schema  = 3;
        final int object_name    = 4;
        final int object_type    = 5;
        final int privilege_type = 6;
        final int is_grantable   = 7;
        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());
        Iterator objects =
            new WrapperIterator(database.schemaManager
                .databaseObjectIterator(SchemaObject.SEQUENCE), database
                .schemaManager.databaseObjectIterator(SchemaObject.COLLATION));

        objects = new WrapperIterator(
            objects,
            database.schemaManager.databaseObjectIterator(
                SchemaObject.CHARSET));
        objects = new WrapperIterator(
            objects,
            database.schemaManager.databaseObjectIterator(
                SchemaObject.DOMAIN));

/*
        objects = new WrapperIterator(
            objects,
            database.schemaManager.databaseObjectIterator(SchemaObject.TYPE));
*/
        OrderedHashSet grantees =
            session.getGrantee().getGranteeAndAllRolesWithPublic();

        while (objects.hasNext()) {
            SchemaObject object = (SchemaObject) objects.next();

            for (int i = 0; i < grantees.size(); i++) {
                Grantee granteeObject = (Grantee) grantees.get(i);
                OrderedHashSet rights =
                    granteeObject.getAllDirectPrivileges(object);
                OrderedHashSet grants =
                    granteeObject.getAllGrantedPrivileges(object);

                if (!grants.isEmpty()) {
                    grants.addAll(rights);

                    rights = grants;
                }

                for (int j = 0; j < rights.size(); j++) {
                    Right right          = (Right) rights.get(j);
                    Right grantableRight = right.getGrantableRights();

                    row                 = t.getEmptyRowData();
                    row[grantor]        = right.getGrantor().getName().name;
                    row[grantee]        = right.getGrantee().getName().name;
                    row[object_catalog] = database.getCatalogName().name;
                    row[object_schema]  = object.getSchemaName().name;
                    row[object_name]    = object.getName().name;
                    row[object_type] =
                        SchemaObjectSet.getName(object.getName().type);
                    row[privilege_type] = Tokens.T_USAGE;
                    row[is_grantable] =
                        right.getGrantee() == object.getOwner()
                        || grantableRight.isFull() ? Tokens.T_YES
                                                   : Tokens.T_NO;;

                    try {
                        t.insertSys(store, row);
                    } catch (HsqlException e) {}
                }
            }
        }

        return t;
    }

    Table USER_DEFINED_TYPES() throws HsqlException {

        Table t = sysTables[USER_DEFINED_TYPES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[USER_DEFINED_TYPES]);

            addColumn(t, "USER_DEFINED_TYPE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "USER_DEFINED_TYPE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "USER_DEFINED_TYPE_NAME", SQL_IDENTIFIER);
            addColumn(t, "USER_DEFINED_TYPE_CATEGORY", SQL_IDENTIFIER);
            addColumn(t, "IS_INSTANTIABLE", YES_OR_NO);
            addColumn(t, "IS_FINAL", YES_OR_NO);
            addColumn(t, "ORDERING_FORM", SQL_IDENTIFIER);
            addColumn(t, "ORDERING_CATEGORY", SQL_IDENTIFIER);
            addColumn(t, "ORDERING_ROUTINE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "ORDERING_ROUTINE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "ORDERING_ROUTINE_NAME", SQL_IDENTIFIER);
            addColumn(t, "REFERENCE_TYPE", SQL_IDENTIFIER);
            addColumn(t, "DATA_TYPE", CHARACTER_DATA);
            addColumn(t, "CHARACTER_MAXIMUM_LENGTH", CARDINAL_NUMBER);
            addColumn(t, "CHARACTER_OCTET_LENGTH", CARDINAL_NUMBER);
            addColumn(t, "CHARACTER_SET_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "CHARACTER_SET_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "CHARACTER_SET_NAME", SQL_IDENTIFIER);
            addColumn(t, "COLLATION_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "COLLATION_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "COLLATION_NAME", SQL_IDENTIFIER);
            addColumn(t, "NUMERIC_PRECISION", CARDINAL_NUMBER);
            addColumn(t, "NUMERIC_PRECISION_RADIX", CARDINAL_NUMBER);
            addColumn(t, "NUMERIC_SCALE", CARDINAL_NUMBER);
            addColumn(t, "DATETIME_PRECISION", CARDINAL_NUMBER);
            addColumn(t, "INTERVAL_TYPE", CHARACTER_DATA);
            addColumn(t, "INTERVAL_PRECISION", CARDINAL_NUMBER);
            addColumn(t, "SOURCE_DTD_IDENTIFIER", CHARACTER_DATA);
            addColumn(t, "REF_DTD_IDENTIFIER", CHARACTER_DATA);
            addColumn(t, "DECLARED_DATA_TYPE", CHARACTER_DATA);
            addColumn(t, "DECLARED_NUMERIC_PRECISION", CARDINAL_NUMBER);
            addColumn(t, "DECLARED_NUMERIC_SCALE", CARDINAL_NUMBER);
            addColumn(t, "EXTERNAL_NAME", CHARACTER_DATA);
            addColumn(t, "EXTERNAL_LANGUAGE", CHARACTER_DATA);
            addColumn(t, "JAVA_INTERFACE", CHARACTER_DATA);
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 4, 5, 6
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());
        final int user_defined_type_catalog  = 0;
        final int user_defined_type_schema   = 1;
        final int user_defined_type_name     = 2;
        final int user_defined_type_category = 3;
        final int is_instantiable            = 4;
        final int is_final                   = 5;
        final int ordering_form              = 6;
        final int ordering_category          = 7;
        final int ordering_routine_catalog   = 8;
        final int ordering_routine_schema    = 9;
        final int ordering_routine_name      = 10;
        final int reference_type             = 11;
        final int data_type                  = 12;
        final int character_maximum_length   = 13;
        final int character_octet_length     = 14;
        final int character_set_catalog      = 15;
        final int character_set_schema       = 16;
        final int character_set_name         = 17;
        final int collation_catalog          = 18;
        final int collation_schema           = 19;
        final int collation_name             = 20;
        final int numeric_precision          = 21;
        final int numeric_precision_radix    = 22;
        final int numeric_scale              = 23;
        final int datetime_precision         = 24;
        final int interval_type              = 25;
        final int interval_precision         = 26;
        final int source_dtd_identifier      = 27;
        final int ref_dtd_identifier         = 28;
        final int declared_data_type         = 29;
        final int declared_numeric_precision = 30;
        final int declared_numeric_scale     = 31;
        Iterator it =
            database.schemaManager.databaseObjectIterator(SchemaObject.DOMAIN);

        while (it.hasNext()) {
            Type distinct = (Type) it.next();

            if (!distinct.isDistinctType()) {
                continue;
            }

            Object[] data = t.getEmptyRowData();

            data[user_defined_type_catalog]  = database.getCatalogName().name;
            data[user_defined_type_schema]   = distinct.getSchemaName().name;
            data[user_defined_type_name]     = distinct.getName().name;
            data[data_type]                  = distinct.getFullNameString();
            data[declared_data_type]         = distinct.getFullNameString();
            data[user_defined_type_category] = "DISTINCT";
            data[is_instantiable]            = "YES";
            data[is_final]                   = "YES";
            data[ordering_form]              = "FULL";
            data[source_dtd_identifier]      = distinct.getFullNameString();

            if (distinct.isCharacterType()) {
                data[character_maximum_length] =
                    ValuePool.getLong(distinct.precision);
                data[character_octet_length] =
                    ValuePool.getLong(distinct.precision * 2);
            } else if (distinct.isNumberType()) {
                data[numeric_precision] =
                    ValuePool.getLong(((NumberType) distinct).getPrecision());
                data[declared_numeric_precision] =
                    ValuePool.getLong(((NumberType) distinct).getPrecision());

                if (distinct.typeCode != Types.SQL_DOUBLE) {
                    data[numeric_scale] = ValuePool.getLong(distinct.scale);
                    data[declared_numeric_scale] =
                        ValuePool.getLong(distinct.scale);
                }

                data[numeric_precision_radix] = ValuePool.getLong(2);

                if (distinct.typeCode == Types.SQL_DECIMAL
                        || distinct.typeCode == Types.SQL_NUMERIC) {
                    data[numeric_precision_radix] = ValuePool.getLong(10);
                }
            } else if (distinct.isBooleanType()) {}
            else if (distinct.isDateTimeType()) {
                data[datetime_precision] = ValuePool.getLong(distinct.scale);
            } else if (distinct.isIntervalType()) {
                data[interval_precision] =
                    ValuePool.getLong(distinct.precision);
                data[interval_type]      = distinct.getFullNameString();
                data[datetime_precision] = ValuePool.getLong(distinct.scale);
            } else if (distinct.isBinaryType()) {
                data[character_maximum_length] =
                    ValuePool.getLong(distinct.precision);
                data[character_octet_length] =
                    ValuePool.getLong(distinct.precision);
            } else if (distinct.isBitType()) {
                data[character_maximum_length] =
                    ValuePool.getLong(distinct.precision);
                data[character_octet_length] =
                    ValuePool.getLong(distinct.precision);
            }
        }

        return t;
    }

    /**
     * The VIEW_COLUMN_USAGE table has one row for each column of a
     * table that is explicitly or implicitly referenced in the
     * &lt;query expression&gt; of the view being described. <p>
     *
     * <b>Definition:</b> <p>
     *
     * <pre class="SqlCodeExample">
     * CREATE TABLE SYSTEM_VIEW_COLUMN_USAGE (
     *      VIEW_CATALOG    VARCHAR NULL,
     *      VIEW_SCHEMA     VARCHAR NULL,
     *      VIEW_NAME       VARCHAR NOT NULL,
     *      TABLE_CATALOG   VARCHAR NULL,
     *      TABLE_SCHEMA    VARCHAR NULL,
     *      TABLE_NAME      VARCHAR NOT NULL,
     *      COLUMN_NAME     VARCHAR NOT NULL,
     *      UNIQUE ( VIEW_CATALOG, VIEW_SCHEMA, VIEW_NAME,
     *               TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME,
     *               COLUMN_NAME )
     * )
     * </pre>
     *
     * <b>Description:</b> <p>
     *
     * <ol>
     * <li> The values of VIEW_CATALOG, VIEW_SCHEMA, and VIEW_NAME are the
     *      catalog name, unqualified schema name, and qualified identifier,
     *      respectively, of the view being described. <p>
     *
     * <li> The values of TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME, and
     *      COLUMN_NAME are the catalog name, unqualified schema name,
     *      qualified identifier, and column name, respectively, of a column
     *      of a table that is explicitly or implicitly referenced in the
     *      &lt;query expression&gt; of the view being described.
     * </ol>
     *
     * @throws HsqlException
     * @return Table
     */
    Table VIEW_COLUMN_USAGE() throws HsqlException {

        Table t = sysTables[VIEW_COLUMN_USAGE];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[VIEW_COLUMN_USAGE]);

            addColumn(t, "VIEW_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "VIEW_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "VIEW_NAME", SQL_IDENTIFIER);      // not null
            addColumn(t, "TABLE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TABLE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TABLE_NAME", SQL_IDENTIFIER);     // not null
            addColumn(t, "COLUMN_NAME", SQL_IDENTIFIER);    // not null

            // false PK, as VIEW_CATALOG, VIEW_SCHEMA, TABLE_CATALOG, and/or
            // TABLE_SCHEMA may be NULL
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 3, 4, 5, 6
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // Calculated column values
        String viewCatalog;
        String viewSchema;
        String viewName;

        // Intermediate holders
        Iterator tables;
        View     view;
        Table    table;
        Object[] row;
        Iterator iterator;

        // Column number mappings
        final int view_catalog  = 0;
        final int view_schema   = 1;
        final int view_name     = 2;
        final int table_catalog = 3;
        final int table_schema  = 4;
        final int table_name    = 5;
        final int column_name   = 6;

        // Initialization
        tables =
            database.schemaManager.databaseObjectIterator(SchemaObject.TABLE);

        // Do it.
        while (tables.hasNext()) {
            table = (Table) tables.next();

            if (table.isView()
                    && session.getGrantee().isFullyAccessibleByRole(table)) {

                // fall through
            } else {
                continue;
            }

            viewCatalog = database.getCatalogName().name;
            viewSchema  = table.getSchemaName().name;
            viewName    = table.getName().name;
            view        = (View) table;

            OrderedHashSet references = view.getReferences();

            iterator = references.iterator();

            while (iterator.hasNext()) {
                HsqlName name = (HsqlName) iterator.next();

                if (name.type == SchemaObject.COLUMN) {
                    row                = t.getEmptyRowData();
                    row[view_catalog]  = viewCatalog;
                    row[view_schema]   = viewSchema;
                    row[view_name]     = viewName;
                    row[table_catalog] = viewCatalog;
                    row[table_schema]  = name.parent.schema.name;
                    row[table_name]    = name.parent.name;
                    row[column_name]   = name.name;

                    t.insertSys(store, row);
                }
            }
        }

        return t;
    }

    /**
     * The VIEW_ROUTINE_USAGE table has one row for each SQL-invoked
     * routine identified as the subject routine of either a &lt;routine
     * invocation&gt;, a &lt;method reference&gt;, a &lt;method invocation&gt;,
     * or a &lt;static method invocation&gt; contained in a &lt;view
     * definition&gt;. <p>
     *
     * <b>Definition</b><p>
     *
     * <pre class="SqlCodeExample">
     * CREATE TABLE VIEW_ROUTINE_USAGE (
     *      TABLE_CATALOG       VARCHAR NULL,
     *      TABLE_SCHEMA        VARCHAR NULL,
     *      TABLE_NAME          VARCHAR NOT NULL,
     *      SPECIFIC_CATALOG    VARCHAR NULL,
     *      SPECIFIC_SCHEMA     VARCHAR NULL,
     *      SPECIFIC_NAME       VARCHAR NOT NULL,
     *      UNIQUE( TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME,
     *              SPECIFIC_CATALOG, SPECIFIC_SCHEMA,
     *              SPECIFIC_NAME )
     * )
     * </pre>
     *
     * <b>Description</b><p>
     *
     * <ol>
     * <li> The values of TABLE_CATALOG, TABLE_SCHEMA, and TABLE_NAME are the
     *      catalog name, unqualified schema name, and qualified identifier,
     *      respectively, of the viewed table being described. <p>
     *
     * <li> The values of SPECIFIC_CATALOG, SPECIFIC_SCHEMA, and SPECIFIC_NAME are
     *      the catalog name, unqualified schema name, and qualified identifier,
     *      respectively, of the specific name of R. <p>
     * </ol>
     *
     * @throws HsqlException
     * @return Table
     */
    Table VIEW_ROUTINE_USAGE() throws HsqlException {

        Table t = sysTables[VIEW_ROUTINE_USAGE];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[VIEW_ROUTINE_USAGE]);

            addColumn(t, "TABLE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TABLE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TABLE_NAME", SQL_IDENTIFIER);       // not null
            addColumn(t, "SPECIFIC_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "SPECIFIC_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "SPECIFIC_NAME", SQL_IDENTIFIER);    // not null

            // false PK, as VIEW_CATALOG, VIEW_SCHEMA, TABLE_CATALOG, and/or
            // TABLE_SCHEMA may be NULL
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 3, 4, 5
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // Calculated column values
        String viewCat;
        String viewSchem;
        String viewName;
        String specificSchema;

        // Intermediate holders
        Iterator tables;
        View     view;
        Table    table;
        Object[] row;

//        Function       expression;
        OrderedHashSet collector;
        Method         method;
        OrderedHashSet methodSet;
        Iterator       iterator;

        // Column number mappings
        final int table_catalog    = 0;
        final int table_schema     = 1;
        final int table_name       = 2;
        final int specific_catalog = 3;
        final int specific_schema  = 4;
        final int specific_name    = 5;

        // Initialization
        tables =
            database.schemaManager.databaseObjectIterator(SchemaObject.TABLE);
        collector = new OrderedHashSet();

        // Do it.
        while (tables.hasNext()) {
            collector.clear();

            table = (Table) tables.next();

            if (table.isView()
                    && session.getGrantee().isFullyAccessibleByRole(table)) {

                // fall through
            } else {
                continue;
            }

            viewCat   = database.getCatalogName().name;
            viewSchem = table.getSchemaName().name;
            viewName  = table.getName().name;
            specificSchema =
                database.schemaManager.getDefaultSchemaHsqlName().name;
            view = (View) table;

            view.collectAllFunctionExpressions(collector);

            methodSet = new OrderedHashSet();
            iterator  = collector.iterator();

            while (iterator.hasNext()) {
/*
                expression = (Function) iterator.next();

                String className =
                    expression.getMethod().getDeclaringClass().getName();
                String schema =
                    database.schemaManager.getDefaultSchemaHsqlName().name;
                SchemaObject object =
                    database.schemaManager.getSchemaObject(className, schema,
                        SchemaObject.FUNCTION);

                if (!session.getGrantee().isAccessible(object)) {
                    continue;
                }

                methodSet.add(expression.getMethod());
*/
            }

            iterator = methodSet.iterator();

            while (iterator.hasNext()) {
                method                = (Method) iterator.next();
                row                   = t.getEmptyRowData();
                row[table_catalog]    = viewCat;
                row[table_schema]     = viewSchem;
                row[table_name]       = viewName;
                row[specific_catalog] = database.getCatalogName().name;
                row[specific_schema]  = specificSchema;
                row[specific_name] = DINameSpace.getMethodSpecificName(method);

                t.insertSys(store, row);
            }
        }

        return t;
    }

    /**
     * The VIEW_TABLE_USAGE table has one row for each table identified
     * by a &lt;table name&gt; simply contained in a &lt;table reference&gt;
     * that is contained in the &lt;query expression&gt; of a view. <p>
     *
     * <b>Definition</b><p>
     *
     * <pre class="SqlCodeExample">
     * CREATE TABLE SYSTEM_VIEW_TABLE_USAGE (
     *      VIEW_CATALOG    VARCHAR NULL,
     *      VIEW_SCHEMA     VARCHAR NULL,
     *      VIEW_NAME       VARCHAR NULL,
     *      TABLE_CATALOG   VARCHAR NULL,
     *      TABLE_SCHEMA    VARCHAR NULL,
     *      TABLE_NAME      VARCHAR NULL,
     *      UNIQUE( VIEW_CATALOG, VIEW_SCHEMA, VIEW_NAME,
     *              TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME )
     * )
     * </pre>
     *
     * <b>Description:</b><p>
     *
     * <ol>
     * <li> The values of VIEW_CATALOG, VIEW_SCHEMA, and VIEW_NAME are the
     *      catalog name, unqualified schema name, and qualified identifier,
     *      respectively, of the view being described. <p>
     *
     * <li> The values of TABLE_CATALOG, TABLE_SCHEMA, and TABLE_NAME are the
     *      catalog name, unqualified schema name, and qualified identifier,
     *      respectively, of a table identified by a &lt;table name&gt;
     *      simply contained in a &lt;table reference&gt; that is contained in
     *      the &lt;query expression&gt; of the view being described.
     * </ol>
     *
     * @throws HsqlException
     * @return Table
     */
    Table VIEW_TABLE_USAGE() throws HsqlException {

        Table t = sysTables[VIEW_TABLE_USAGE];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[VIEW_TABLE_USAGE]);

            addColumn(t, "VIEW_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "VIEW_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "VIEW_NAME", SQL_IDENTIFIER);     // not null
            addColumn(t, "TABLE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TABLE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TABLE_NAME", SQL_IDENTIFIER);    // not null

            // false PK, as VIEW_CATALOG, VIEW_SCHEMA, TABLE_CATALOG, and/or
            // TABLE_SCHEMA may be NULL
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 3, 4, 5
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        //
        Session sys = database.sessionManager.newSysSession(
            SqlInvariants.INFORMATION_SCHEMA_HSQLNAME, session.getUser());
        Result rs = sys.executeDirectStatement(
            "select DISTINCT VIEW_CATALOG, VIEW_SCHEMA, "
            + "VIEW_NAME, TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME "
            + "from INFORMATION_SCHEMA.VIEW_COLUMN_USAGE");

        t.insertSys(store, rs);
        sys.close();

        return t;
    }

    /**
     * The VIEWS view contains one row for each VIEW definition. <p>
     *
     * Each row is a description of the query expression that defines its view,
     * with the following columns:
     *
     * <pre class="SqlCodeExample">
     * TABLE_CATALOG    VARCHAR     name of view's defining catalog.
     * TABLE_SCHEMA     VARCHAR     name of view's defining schema.
     * TABLE_NAME       VARCHAR     the simple name of the view.
     * VIEW_DEFINITION  VARCHAR     the character representation of the
     *                              &lt;query expression&gt; contained in the
     *                              corresponding &lt;view descriptor&gt;.
     * CHECK_OPTION     VARCHAR     {"CASCADED" | "LOCAL" | "NONE"}
     * IS_UPDATABLE     VARCHAR     {"YES" | "NO"}
     * INSERTABLE_INTO VARCHAR      {"YES" | "NO"}
     * IS_TRIGGER_UPDATABLE        VARCHAR  {"YES" | "NO"}
     * IS_TRIGGER_DELETEABLE       VARCHAR  {"YES" | "NO"}
     * IS_TRIGGER_INSERTABLE_INTO  VARCHAR  {"YES" | "NO"}
     * </pre> <p>
     *
     * @return a tabular description of the text source of all
     *        <code>View</code> objects accessible to
     *        the user.
     * @throws HsqlException if an error occurs while producing the table
     */
    Table VIEWS() throws HsqlException {

        Table t = sysTables[VIEWS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[VIEWS]);

            addColumn(t, "TABLE_CATALOG", SQL_IDENTIFIER);
            addColumn(t, "TABLE_SCHEMA", SQL_IDENTIFIER);
            addColumn(t, "TABLE_NAME", SQL_IDENTIFIER);               // not null
            addColumn(t, "VIEW_DEFINITION", CHARACTER_DATA);          // not null
            addColumn(t, "CHECK_OPTION", CHARACTER_DATA);             // not null
            addColumn(t, "IS_UPDATABLE", YES_OR_NO);                  // not null
            addColumn(t, "INSERTABLE_INTO", YES_OR_NO);               // not null
            addColumn(t, "IS_TRIGGER_UPDATABLE", YES_OR_NO);          // not null
            addColumn(t, "IS_TRIGGER_DELETABLE", YES_OR_NO);          // not null
            addColumn(t, "IS_TRIGGER_INSERTABLE_INTO", YES_OR_NO);    // not null

            // order TABLE_NAME
            // added for unique: TABLE_SCHEMA, TABLE_CATALOG
            // false PK, as TABLE_SCHEMA and/or TABLE_CATALOG may be null
            t.createPrimaryKey(null, new int[] {
                1, 2, 0
            }, false);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());
        Iterator  tables;
        Table     table;
        Object[]  row;
        final int table_catalog              = 0;
        final int table_schema               = 1;
        final int table_name                 = 2;
        final int view_definition            = 3;
        final int check_option               = 4;
        final int is_updatable               = 5;
        final int insertable_into            = 6;
        final int is_trigger_updatable       = 7;
        final int is_trigger_deletable       = 8;
        final int is_trigger_insertable_into = 9;

        tables = allTables();

        while (tables.hasNext()) {
            table = (Table) tables.next();

            if ((table.getSchemaName() != SqlInvariants
                    .INFORMATION_SCHEMA_HSQLNAME && !table
                        .isView()) || !isAccessibleTable(table)) {
                continue;
            }

            row                = t.getEmptyRowData();
            row[table_catalog] = database.getCatalogName().name;
            row[table_schema]  = table.getSchemaName().name;
            row[table_name]    = table.getName().name;

            String check = Tokens.T_NONE;

            if (table instanceof View) {
                if (session.getGrantee().isFullyAccessibleByRole(table)) {
                    row[view_definition] = ((View) table).getStatement();
                }

                switch (((View) table).getCheckOption()) {

                    case SchemaObject.ViewCheckModes.CHECK_NONE :
                        break;

                    case SchemaObject.ViewCheckModes.CHECK_LOCAL :
                        check = Tokens.T_LOCAL;
                        break;

                    case SchemaObject.ViewCheckModes.CHECK_CASCADE :
                        check = Tokens.T_CASCADED;
                        break;
                }
            }

            row[check_option]         = check;
            row[is_updatable]         = table.isUpdatable() ? Tokens.T_YES
                                                            : Tokens.T_NO;
            row[insertable_into]      = table.isInsertable() ? Tokens.T_YES
                                                             : Tokens.T_NO;
            row[is_trigger_updatable] = null;    // only applies to INSTEAD OF triggers
            row[is_trigger_deletable]       = null;
            row[is_trigger_insertable_into] = null;

            t.insertSys(store, row);
        }

        return t;
    }

//------------------------------------------------------------------------------
// SQL SCHEMATA BASE TABLES

    /**
     * ROLE_AUTHORIZATION_DESCRIPTORS<p>
     *
     * <b>Function</b><p>
     *
     * Contains a representation of the role authorization descriptors.<p>
     * <b>Definition</b>
     *
     * <pre class="SqlCodeExample">
     * CREATE TABLE ROLE_AUTHORIZATION_DESCRIPTORS (
     *      ROLE_NAME INFORMATION_SCHEMA.SQL_IDENTIFIER,
     *      GRANTEE INFORMATION_SCHEMA.SQL_IDENTIFIER,
     *      GRANTOR INFORMATION_SCHEMA.SQL_IDENTIFIER,
     *      IS_GRANTABLE INFORMATION_SCHEMA.CHARACTER_DATA
     *          CONSTRAINT ROLE_AUTHORIZATION_DESCRIPTORS_IS_GRANTABLE_CHECK
     *              CHECK ( IS_GRANTABLE IN
     *                  ( 'YES', 'NO' ) ),
     *          CONSTRAINT ROLE_AUTHORIZATION_DESCRIPTORS_PRIMARY_KEY
     *              PRIMARY KEY ( ROLE_NAME, GRANTEE ),
     *          CONSTRAINT ROLE_AUTHORIZATION_DESCRIPTORS_CHECK_ROLE_NAME
     *              CHECK ( ROLE_NAME IN
     *                  ( SELECT AUTHORIZATION_NAME
     *                      FROM AUTHORIZATIONS
     *                     WHERE AUTHORIZATION_TYPE = 'ROLE' ) ),
     *          CONSTRAINT ROLE_AUTHORIZATION_DESCRIPTORS_FOREIGN_KEY_AUTHORIZATIONS_GRANTOR
     *              FOREIGN KEY ( GRANTOR )
     *                  REFERENCES AUTHORIZATIONS,
     *          CONSTRAINT ROLE_AUTHORIZATION_DESCRIPTORS_FOREIGN_KEY_AUTHORIZATIONS_GRANTEE
     *              FOREIGN KEY ( GRANTEE )
     *                  REFERENCES AUTHORIZATIONS
     *      )
     * </pre>
     *
     * <b>Description</b><p>
     *
     * <ol>
     *      <li>The value of ROLE_NAME is the &lt;role name&gt; of some
     *          &lt;role granted&gt; by the &lt;grant role statement&gt; or
     *          the &lt;role name&gt; of a &lt;role definition&gt;. <p>
     *
     *      <li>The value of GRANTEE is an &lt;authorization identifier&gt;,
     *          possibly PUBLIC, or &lt;role name&gt; specified as a
     *          &lt;grantee&gt; contained in a &lt;grant role statement&gt;,
     *          or the &lt;authorization identifier&gt; of the current
     *          SQLsession when the &lt;role definition&gt; is executed. <p>
     *
     *      <li>The value of GRANTOR is the &lt;authorization identifier&gt;
     *          of the user or role who granted the role identified by
     *          ROLE_NAME to the user or role identified by the value of
     *          GRANTEE. <p>
     *
     *      <li>The values of IS_GRANTABLE have the following meanings:<p>
     *
     *      <table border cellpadding="3">
     *          <tr>
     *              <td nowrap>YES</td>
     *              <td nowrap>The described role is grantable.</td>
     *          <tr>
     *          <tr>
     *              <td nowrap>NO</td>
     *              <td nowrap>The described role is not grantable.</td>
     *          <tr>
     *      </table> <p>
     * </ol>
     *
     * @throws HsqlException
     * @return Table
     */
    Table ROLE_AUTHORIZATION_DESCRIPTORS() throws HsqlException {

        Table t = sysTables[ROLE_AUTHORIZATION_DESCRIPTORS];

        if (t == null) {
            t = createBlankTable(
                sysTableHsqlNames[ROLE_AUTHORIZATION_DESCRIPTORS]);

            addColumn(t, "ROLE_NAME", SQL_IDENTIFIER);    // not null
            addColumn(t, "GRANTEE", SQL_IDENTIFIER);      // not null
            addColumn(t, "GRANTOR", SQL_IDENTIFIER);      // not null
            addColumn(t, "IS_GRANTABLE", YES_OR_NO);      // not null

            // true PK
            t.createPrimaryKey(null, new int[] {
                0, 1
            }, true);

            return t;
        }

        PersistentStore store =
            database.persistentStoreCollection.getStore(t.getPersistenceId());

        // Intermediate holders
        String   grantorName = SqlInvariants.SYSTEM_AUTHORIZATION_NAME;
        Iterator grantees;
        Grantee  granteeObject;
        String   granteeName;
        Iterator roles;
        String   roleName;
        String   isGrantable;
        Object[] row;

        // Column number mappings
        final int role_name    = 0;
        final int grantee      = 1;
        final int grantor      = 2;
        final int is_grantable = 3;

        // Initialization
        grantees = session.getGrantee().visibleGrantees().iterator();

        // Do it.
        while (grantees.hasNext()) {
            granteeObject = (Grantee) grantees.next();
            granteeName   = granteeObject.getNameString();
            roles         = granteeObject.getDirectRoles().iterator();
            isGrantable   = granteeObject.isAdmin() ? Tokens.T_YES
                                                    : Tokens.T_NO;;

            while (roles.hasNext()) {
                Grantee role = (Grantee) roles.next();

                row               = t.getEmptyRowData();
                row[role_name]    = role.getNameString();
                row[grantee]      = granteeName;
                row[grantor]      = grantorName;
                row[is_grantable] = isGrantable;

                t.insertSys(store, row);
            }
        }

        return t;
    }
}
