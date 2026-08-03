/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.Config;
import com.oscar.Driver;
import com.oscar.core.BaseConnection;
import com.oscar.core.BaseResultSet;
import com.oscar.core.Encoding;
import com.oscar.core.Field;
import com.oscar.jdbc.OscarJdbc2Connection;
import com.oscar.jdbc.OscarResultSet;
import com.oscar.util.OSQLException;
import com.oscar.util.TableNameParser;
import com.oscar.util.converter.NumberConverter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class OscarDatabaseMetaData
implements DatabaseMetaData {
    private static final String keywords = "abort,absolute,access,accessed,action,add,admin,after,aggregate,all,alter,analyse,analyze,analyzer,and,any,archive,archivelog,array,as,asc,assertion,assignment,async,at,attributes,audit,auditfile,authid,authorization,auto_increment,autoextend,backup,backward,basicanalyzer,batchsize,before,begin,between,bigint,binary,binlog,bit,block,body,boolean,both,bpchar,buffer_pool,build,bulk,by,cache,call,called,cascade,cascaded,case,cast,cdc,chain,char,character,characteristics,cheat,check,check_constraints,checkpoint,chineseanalyzer,chunk,cjkanalyzer,class,clean,close,cluster,coalesce,codepage,collate,column,columns,comment,commit,committed,compile,complete,compress,configuration,connect,connect_by_iscycle,connect_by_isleaf,connect_by_root,constraint,constraints,constructor,content,context,controlfile,conversion,convert,copy,create,cross,csv,current,current_time,current_timestamp,current_user,cursor,cycle,data,database,databaselink,datafile,datafiletype,dateformat,day,dba,deallocate,debug,dec,decimal,declare,decode,decrypt,default,deferrable,deferred,definer,delete,delimiter,delimiters,demand,dense_rank,desc,description,deterministic,directory,disable,distinct,do,document,domain,double,drop,dump,each,else,enable,encoding,encrypt,encrypted,end,errors,escalation,escape,events,except,exchange,excluding,exclusive,exec,execute,exists,explain,export,external,externally,extract,false,fast,fetch,fieldterminator,file,filesize,fill,fire_triggers,first,firstrow,flashback,float,following,for,force,foreign,foreignkey_constraints,forever,formatfile,forward,freelists,freepools,from,full,fulltext,function,g,get,getaconststr,getclobval,global,global_name,globally,grant,greatest,group,guarantee,handler,hash,having,header,heap,hour,identified,if,ignore,ilike,immediate,immutable,implicit,import,import_polcol,in,including,increment,index,indexes,inherits,init,initial,initialized,initially,initrans,inner,inout,input,insensitive,insert,instead,int,integer,intersect,interval,into,invalidate,invisible,invoker,is,isnull,isolation,join,k,keep,keepidentity,keepnulls,key,kill,kilobytes_per_batch,kstore,lancompiler,language,last,lastrow,lc_collate,lc_ctype,leading,leak,least,left,less,level,lifetime,like,limit,list,listen,load,lob,local,localtime,localtimestamp,location,lock,log,logfile,logging,logical,long,loop,lsn,m,maintain_index,maintenance,match,matched,materialized,max,maxerrors,maxextends,maxsize,maxtrans,maxvalue,member,merge,min,minextends,minus,minute,minvalue,mod,mode,modify,month,mount,move,multicolumn,multiple,name,names,national,natural,nchar,never,new,next,nextval,no,noarchivelog,noaudit,nocache,nocompress,nocopy,nocycle,node,noguarantee,nologging,nomaxvalue,nominvalue,nomount,none,norecompute,normal,not,nothing,notify,notnull,novalidate,null,nullif,nulls,number,numeric,nvarchar2,nvl,nvl2,object,of,off,offline,offset,oids,old,on,online,only,open,operator,optimize,optimize_kscache,option,or,oracle,order,ordinality,organization,oscar,out,outer,over,overflow,overlaps,overlay,owner,package,pagesize,parameter,paraminfo,partial,partition,partitions,passing,password,path,pctfree,pctincrease,pctthreshold,pctused,pctversion,pendant,percent,petention,pfile,pipelined,placing,pls_integer,port,position,preceding,precision,prepare,preserve,preval,primary,prior,priority,privileges,procedural,procedure,public,purge,qu,query,quick,quote,range,raw,read,reads,real,rebuild,recheck,recovery,recycle,recyclebin,references,referencing,refresh,reject,relative,remove,rename,repeatable,replace,reset,resize,restart,restore,restrict,resume,retention,return,returning,returns,reuse,reverse,revoke,rewrite,right,role,rollback,row,rowdescription,rowid,rows,rows_per_batch,rowterminator,rowtype,rule,savepoint,scan,schema,scroll,second,security,segment,select,sequence,serializable,session,set,setof,share,show,shrink,shrinklog,shutdown,siblings,silently,similar,simple,single,singlerow,size,smallint,some,specification,split,stable,standalone,standardanalyzer,start,startfile,startpos,starttime,startup,statement,static,statistics,stdin,stdout,stopfile,stoppos,stoptime,stopwords,storage,store,strict,subpartition,subpartitions,substring,successful,suspend,switchover,sync,synonym,sys_connect_by_path,sysaux,sysid,system,table,tablespace,tablock,temp,tempfile,template,temporary,than,then,time,times,timestamp,timezone,tinyint,to,toast,top,trace,trail,trailing,transaction,transactional,treat,trigger,triggers,trim,true,truncate,trusted,tuple,type,unbounded,uncommitted,undo,unencrypted,union,unique,unknown,unlimited,unlisten,unlock,unmaintenance,until,unusable,up,update,updatelabel,updatexml,usage,use,user,using,vacuum,valid,validate,validation,validator,value,values,varbinary,varchar,varchar2,varying,verbose,version,view,visible,volatile,weight,when,whenever,where,window,with,without,work,write,xml,xmlattributes,xmlconcat,xmlelement,xmlforest,xmlparse,xmlpi,xmlroot,xmlserialize,xmltable,year,yes,zone,convert,";
    protected OscarJdbc2Connection connection;
    private int NAMEDATALEN = 0;
    private int INDEX_MAX_KEYS = 0;
    protected Encoding encoding;
    private static String databaseName = "OSCAR";
    private static String databaseVersion;
    private static final String JDBC_NAME = "OSCAR JDBC DRIVER";
    private static final String JDBC_VERSION = "3.0";
    private static final int JDBC_MAJOR_VERSION = 3;
    private static final int JDBC_MINOR_VERSION = 0;
    private Map<String, Integer> dbProps;
    private static final Hashtable tableTypeClauses;
    private static final String[] defaultTableTypes;
    protected static String[] TYPE_NAME;
    protected static int[] DATA_TYPE;

    public OscarDatabaseMetaData(OscarJdbc2Connection conn) {
        this.connection = conn;
        this.encoding = conn.getEncoding();
        this.dbProps = new HashMap<String, Integer>();
    }

    public boolean allProceduresAreCallable() throws SQLException {
        return false;
    }

    public boolean allTablesAreSelectable() throws SQLException {
        return false;
    }

    public String getURL() throws SQLException {
        return this.connection.getURL();
    }

    public String getUserName() throws SQLException {
        return this.connection.getUserName();
    }

    public boolean isReadOnly() throws SQLException {
        boolean isReadOnly = this.connection.isReadOnly();
        return isReadOnly;
    }

    public boolean nullsAreSortedHigh() throws SQLException {
        return this.getValue("nullsAreSortedLoworHigh") == 1;
    }

    public boolean nullsAreSortedLow() throws SQLException {
        return this.getValue("nullsAreSortedLoworHigh") == 0;
    }

    public boolean nullsAreSortedAtStart() throws SQLException {
        return this.getValue("nullsAreSortedAtStartorEnd") == 1;
    }

    public boolean nullsAreSortedAtEnd() throws SQLException {
        return this.getValue("nullsAreSortedAtStartorEnd") == 2;
    }

    public String getDatabaseProductName() throws SQLException {
        return databaseName;
    }

    public String getDatabaseProductVersion() throws SQLException {
        if (databaseVersion == null) {
            try {
                BaseResultSet resultSet = this.connection.execSQL("select version();");
                if (!resultSet.next()) {
                    throw new OSQLException("OSCAR-00107", "08001", 107);
                }
                this.extractVersionNumber(resultSet.getString(1));
                if (resultSet != null) {
                    resultSet.close();
                }
            }
            catch (Exception e) {
                return "7.1";
            }
        }
        return databaseVersion;
    }

    private void extractVersionNumber(String fullVersionString) {
        databaseVersion = fullVersionString;
    }

    public String getDriverName() throws SQLException {
        return JDBC_NAME;
    }

    public String getDriverVersion() throws SQLException {
        return Driver.getVersion();
    }

    public int getDriverMajorVersion() {
        return this.connection.getDriver().getMajorVersion();
    }

    public int getDriverMinorVersion() {
        return this.connection.getDriver().getMinorVersion();
    }

    public boolean usesLocalFiles() throws SQLException {
        return this.getValue("usesLocalFiles") != 0;
    }

    public boolean usesLocalFilePerTable() throws SQLException {
        return this.getValue("usesLocalFilePerTable") != 0;
    }

    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return false;
    }

    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return this.getValue("storesUpperCaseIdentifiers") != 0;
    }

    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return this.getValue("storesLowerCaseIdentifiers") != 0;
    }

    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return this.getValue("storesMixedCaseIdentifiers") != 0;
    }

    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return true;
    }

    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return this.getValue("storesUpperCaseQuotedIdentifiers") != 0;
    }

    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return this.getValue("storesLowerCaseQuotedIdentifiers") != 0;
    }

    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return this.getValue("storesMixedCaseQuotedIdentifiers") != 0;
    }

    public String getIdentifierQuoteString() throws SQLException {
        return "\"";
    }

    public String getSQLKeywords() throws SQLException {
        return keywords;
    }

    public String getNumericFunctions() throws SQLException {
        return "ABS,ACOS,ASIN,ATAN,ATAN2,CEILING,COS,COT,DEGREES,EXP,FLOOR,LOG,LOG10,MOD,PI,POWER,RADIANS,RAND,ROUND,SIGN,SIN,SQRT,TAN,TRUNCATE";
    }

    public String getStringFunctions() throws SQLException {
        return "ASCII,CHAR,CONCAT,DIFFERENCE,INSERT_TEXT,LCASE,LEFT,LENGTH,LOCATE,LTRIM,REPEAT,REPLACE,RIGHT,RTRIM,SOUNDEX,SPACE,SUBSTRING,UCASE";
    }

    public String getSystemFunctions() throws SQLException {
        return "DATABASE,IFNULL,USER";
    }

    public String getTimeDateFunctions() throws SQLException {
        return "CURDATE,CURTIME,DAYNAME,DAYOFMONTH,DAYOFWEEK,DAYOFYEAR,HOUR,MINUTE,MONTH,MONTHNAME,NOW,QUARTER,SECOND,TIMESTAMPADD,TIMESTAMPDIFF,WEEK,YEAR";
    }

    public String getSearchStringEscape() throws SQLException {
        return "\\";
    }

    public String getExtraNameCharacters() throws SQLException {
        return "";
    }

    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return true;
    }

    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return true;
    }

    public boolean supportsColumnAliasing() throws SQLException {
        return true;
    }

    public boolean nullPlusNonNullIsNull() throws SQLException {
        return true;
    }

    public boolean supportsConvert() throws SQLException {
        return true;
    }

    public boolean supportsConvert(int fromType, int toType) throws SQLException {
        switch (fromType) {
            case -7: 
            case -6: 
            case -5: 
            case 2: 
            case 3: 
            case 4: 
            case 5: 
            case 6: 
            case 7: 
            case 8: 
            case 16: {
                switch (toType) {
                    case -7: 
                    case -6: 
                    case -5: 
                    case -1: 
                    case 1: 
                    case 2: 
                    case 3: 
                    case 4: 
                    case 5: 
                    case 6: 
                    case 7: 
                    case 8: 
                    case 12: 
                    case 16: {
                        return true;
                    }
                }
                return false;
            }
            case -1: 
            case 1: 
            case 12: {
                switch (toType) {
                    case -7: 
                    case -6: 
                    case -5: 
                    case -1: 
                    case 1: 
                    case 2: 
                    case 3: 
                    case 4: 
                    case 5: 
                    case 6: 
                    case 7: 
                    case 8: 
                    case 12: 
                    case 16: 
                    case 91: 
                    case 92: 
                    case 93: {
                        return true;
                    }
                }
                return false;
            }
            case -4: 
            case -3: 
            case -2: {
                switch (toType) {
                    case -4: 
                    case -3: 
                    case -2: 
                    case -1: 
                    case 1: 
                    case 12: {
                        return true;
                    }
                }
                return false;
            }
            case 91: {
                switch (toType) {
                    case -1: 
                    case 1: 
                    case 12: 
                    case 91: 
                    case 93: {
                        return true;
                    }
                }
                return false;
            }
            case 92: {
                switch (toType) {
                    case -1: 
                    case 1: 
                    case 12: 
                    case 92: 
                    case 93: {
                        return true;
                    }
                }
                return false;
            }
            case 93: {
                switch (toType) {
                    case -1: 
                    case 1: 
                    case 12: 
                    case 91: 
                    case 92: 
                    case 93: {
                        return true;
                    }
                }
                return false;
            }
            case 2004: {
                switch (toType) {
                    case 2004: {
                        return true;
                    }
                }
                return false;
            }
            case 2005: {
                switch (toType) {
                    case 2005: {
                        return true;
                    }
                }
                return false;
            }
            case 2003: {
                switch (toType) {
                    case 2003: {
                        return true;
                    }
                }
                return false;
            }
            case 2006: {
                switch (toType) {
                    case 2006: {
                        return true;
                    }
                }
                return false;
            }
            case 70: {
                switch (toType) {
                    case -1: 
                    case 1: 
                    case 12: 
                    case 70: {
                        return true;
                    }
                }
                return false;
            }
            case 2002: {
                switch (toType) {
                    case 2002: {
                        return true;
                    }
                }
                return false;
            }
            case 1111: {
                switch (toType) {
                    case -1: 
                    case 1: 
                    case 12: {
                        return true;
                    }
                }
                return false;
            }
            case 0: {
                return true;
            }
            case 2000: {
                return true;
            }
        }
        return false;
    }

    public boolean supportsTableCorrelationNames() throws SQLException {
        return true;
    }

    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return false;
    }

    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return true;
    }

    public boolean supportsOrderByUnrelated() throws SQLException {
        return true;
    }

    public boolean supportsGroupBy() throws SQLException {
        return true;
    }

    public boolean supportsGroupByUnrelated() throws SQLException {
        return false;
    }

    public boolean supportsGroupByBeyondSelect() throws SQLException {
        return false;
    }

    public boolean supportsLikeEscapeClause() throws SQLException {
        return true;
    }

    public boolean supportsMultipleResultSets() throws SQLException {
        return true;
    }

    public boolean supportsMultipleTransactions() throws SQLException {
        return true;
    }

    public boolean supportsNonNullableColumns() throws SQLException {
        return true;
    }

    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return true;
    }

    public boolean supportsCoreSQLGrammar() throws SQLException {
        return false;
    }

    public boolean supportsExtendedSQLGrammar() throws SQLException {
        return false;
    }

    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return true;
    }

    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        return false;
    }

    public boolean supportsANSI92FullSQL() throws SQLException {
        return false;
    }

    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        return false;
    }

    public boolean supportsOuterJoins() throws SQLException {
        return true;
    }

    public boolean supportsFullOuterJoins() throws SQLException {
        return true;
    }

    public boolean supportsLimitedOuterJoins() throws SQLException {
        return true;
    }

    public String getSchemaTerm() throws SQLException {
        return "schema";
    }

    public String getProcedureTerm() throws SQLException {
        return "procedure";
    }

    public String getCatalogTerm() throws SQLException {
        return "database";
    }

    public boolean isCatalogAtStart() throws SQLException {
        return this.getValue("isCatalogAtStart") != 0;
    }

    public String getCatalogSeparator() throws SQLException {
        return ".";
    }

    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return true;
    }

    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return true;
    }

    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return true;
    }

    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return true;
    }

    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return true;
    }

    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return false;
    }

    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return false;
    }

    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return false;
    }

    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return false;
    }

    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return false;
    }

    public boolean supportsPositionedDelete() throws SQLException {
        return true;
    }

    public boolean supportsPositionedUpdate() throws SQLException {
        return true;
    }

    public boolean supportsSelectForUpdate() throws SQLException {
        return true;
    }

    public boolean supportsStoredProcedures() throws SQLException {
        return true;
    }

    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return true;
    }

    public boolean supportsSubqueriesInExists() throws SQLException {
        return true;
    }

    public boolean supportsSubqueriesInIns() throws SQLException {
        return true;
    }

    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return false;
    }

    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return true;
    }

    public boolean supportsUnion() throws SQLException {
        return true;
    }

    public boolean supportsUnionAll() throws SQLException {
        return true;
    }

    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return true;
    }

    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return true;
    }

    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return true;
    }

    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return true;
    }

    public int getMaxBinaryLiteralLength() throws SQLException {
        return this.getValue("MaxBinaryLiteralLength");
    }

    public int getMaxCharLiteralLength() throws SQLException {
        return this.getValue("MaxCharLiteralLength");
    }

    public int getMaxColumnNameLength() throws SQLException {
        return this.getValue("MaxColumnNameLength");
    }

    public int getMaxColumnsInGroupBy() throws SQLException {
        return this.getValue("MaxColumnsInGroupBy");
    }

    public int getMaxColumnsInIndex() throws SQLException {
        return this.getValue("MaxColumnsInIndex");
    }

    public int getMaxColumnsInOrderBy() throws SQLException {
        return this.getValue("MaxColumnsInOrderBy");
    }

    public int getMaxColumnsInSelect() throws SQLException {
        return this.getValue("MaxColumnsInSelect");
    }

    public int getMaxColumnsInTable() throws SQLException {
        return this.getValue("MaxColumnsInTable");
    }

    public int getMaxConnections() throws SQLException {
        return 0;
    }

    public int getMaxCursorNameLength() throws SQLException {
        return this.getValue("MaxCursorNameLength");
    }

    public int getMaxIndexLength() throws SQLException {
        return this.getValue("MaxIndexLength");
    }

    public int getMaxSchemaNameLength() throws SQLException {
        return this.getValue("MaxSchemaNameLength");
    }

    public int getMaxProcedureNameLength() throws SQLException {
        return this.getValue("MaxProcedureNameLength");
    }

    public int getMaxCatalogNameLength() throws SQLException {
        return this.getValue("MaxCatalogNameLength");
    }

    public int getMaxRowSize() throws SQLException {
        return this.getValue("MaxRowSize");
    }

    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return false;
    }

    public int getMaxStatementLength() throws SQLException {
        return this.getValue("MaxStatementLength");
    }

    public int getMaxStatements() throws SQLException {
        return this.getValue("MaxStatements");
    }

    public int getMaxTableNameLength() throws SQLException {
        return this.getValue("MaxTableNameLength");
    }

    public int getMaxTablesInSelect() throws SQLException {
        return this.getValue("MaxTablesInSelect");
    }

    public int getMaxUserNameLength() throws SQLException {
        return this.getValue("MaxUserNameLength");
    }

    public int getDefaultTransactionIsolation() throws SQLException {
        return 2;
    }

    public boolean supportsTransactions() throws SQLException {
        return true;
    }

    public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
        return level == 8 || level == 2;
    }

    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return true;
    }

    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return false;
    }

    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return this.getValue("dataDefinitionCausesTransactionCommit") != 0;
    }

    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return this.getValue("dataDefinitionIgnoredInTransactions") != 0;
    }

    public ResultSet getProcedures(String catalog, String schemaPattern, String procedurePattern) throws SQLException {
        StringBuffer sql = new StringBuffer(50);
        sql.append("SELECT PROCEDURE_CAT,PROCEDURE_SCHEM,PROCEDURE_NAME,NUM_INPUT_PARAMS,NUM_OUTPUT_PARAMS,NUM_RESULT_SETS,REMARKS,PROCEDURE_TYPE FROM V_SYS_PROCEDURE");
        if (procedurePattern == null) {
            procedurePattern = "%";
        }
        sql.append(" WHERE PROCEDURE_NAME LIKE ").append(TableNameParser.orgStringToQueryString(procedurePattern));
        if (schemaPattern != null && schemaPattern.trim().length() > 0) {
            sql.append(" AND PROCEDURE_SCHEM LIKE ").append(TableNameParser.orgStringToQueryString(schemaPattern));
        }
        if (catalog != null && catalog.trim().length() > 0) {
            sql.append(" AND PROCEDURE_CAT LIKE ").append(TableNameParser.orgStringToQueryString(catalog));
        }
        sql.append(" ORDER BY PROCEDURE_SCHEM");
        return this.resetColumnName(this.connection.execSQL(sql.toString()));
    }

    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedurePattern, String columnPattern) throws SQLException {
        StringBuffer sql = new StringBuffer(150);
        sql.append("SELECT A.PROCEDURE_CAT,A.PROCEDURE_SCHEM,A.PROCEDURE_NAME,A.COLUMN_NAME,");
        sql.append("A.COLUMN_TYPE,A.DATA_TYPE,A.TYPE_NAME,");
        sql.append("A.COLUMN_SIZE AS \"PRECISION\",A.BUFFER_LENGTH AS LENGTH,");
        sql.append("A.DECIMAL_DIGITS AS SCALE,A.NUM_PREC_RADIX AS RADIX,A.NULLABLE,A.REMARKS");
        sql.append(" FROM V_SYS_PROCEDURE_PARAMS A");
        sql.append(" WHERE A.ORDINAL_POSITION >0 AND A.PROCEDURE_NAME LIKE ");
        sql.append(TableNameParser.orgStringToQueryString(procedurePattern));
        if (columnPattern != null && columnPattern.trim().length() > 0) {
            sql.append(" AND A.COLUMN_NAME LIKE ").append(TableNameParser.orgStringToQueryString(columnPattern));
        }
        if (schemaPattern != null && schemaPattern.trim().length() > 0) {
            sql.append(" AND A.PROCEDURE_SCHEM LIKE ").append(TableNameParser.orgStringToQueryString(schemaPattern));
        }
        if (catalog != null && catalog.trim().length() > 0) {
            sql.append(" AND A.PROCEDURE_CAT LIKE ").append(TableNameParser.orgStringToQueryString(catalog));
        }
        sql.append(" ORDER BY A.PROCEDURE_SCHEM,A.PROCEDURE_NAME,A.ORDINAL_POSITION");
        return this.resetColumnName(this.connection.execSQL(sql.toString()));
    }

    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
        StringBuffer sql = new StringBuffer(50);
        sql.append("SELECT * FROM V_SYS_TABLES");
        if (tableNamePattern == null) {
            tableNamePattern = "%";
        }
        sql.append(" WHERE TABLE_NAME LIKE ").append(TableNameParser.orgStringToQueryString(tableNamePattern));
        if (schemaPattern != null) {
            sql.append(" AND TABLE_SCHEM LIKE ");
            if (schemaPattern.trim().equals("")) {
                sql.append("NULL");
            } else {
                sql.append(TableNameParser.orgStringToQueryString(schemaPattern));
            }
        } else if (Config.COMPATABLE_DBMS == 2) {
            sql.append(" AND TABLE_SCHEM LIKE ").append(TableNameParser.orgStringToQueryString(this.getUserName()));
        }
        if (catalog != null && catalog.trim().length() != 0) {
            sql.append(" AND TABLE_CAT LIKE ").append(TableNameParser.orgStringToQueryString(catalog));
        }
        if (types != null && types.length > 0) {
            int i;
            sql.append(" AND (");
            for (i = 0; i < types.length - 1; ++i) {
                sql.append(" TABLE_TYPE LIKE ").append(TableNameParser.orgStringToQueryString(types[i])).append(" OR");
            }
            sql.append(" TABLE_TYPE LIKE ").append(TableNameParser.orgStringToQueryString(types[i])).append(")");
        }
        sql.append(" ORDER BY TABLE_TYPE,TABLE_SCHEM,TABLE_NAME");
        return this.resetColumnName(this.connection.execSQL(sql.toString()));
    }

    public ResultSet getSchemas() throws SQLException {
        String sql = "SELECT * FROM V_SYS_SCHEMAS";
        return this.resetColumnName(this.connection.execSQL(sql));
    }

    public ResultSet getCatalogs() throws SQLException {
        String sql = " SELECT (CURRENT_DATABASE())::character varying(64) AS TABLE_CAT;";
        return this.resetColumnName(this.connection.execSQL(sql));
    }

    public ResultSet getTableTypes() throws SQLException {
        String sql = "SELECT * FROM V_SYS_TABLE_TYPES";
        return this.resetColumnName(this.connection.execSQL(sql));
    }

    public ResultSet getColumns(String catalogPattern, String schemaPattern, String tablePattern, String columnPattern) throws SQLException {
        StringBuffer sql = new StringBuffer(200);
        sql.append("SELECT A.TABLE_CAT, A.TABLE_SCHEM, A.TABLE_NAME, A.COLUMN_NAME,");
        sql.append(" B.SQL_CONCISE_TYPE as DATA_TYPE, B.TYPE_NAME, A.COLUMN_SIZE, A.BUFFER_LENGTH,");
        sql.append(" A.DECIMAL_DIGITS, A.NUM_PREC_RADIX, A.NULLABLE, A.REMARKS, A.COLUMN_DEF,");
        sql.append(" B.SQL_DATA_TYPE, B.SQL_DATETIME_SUB, A.CHAR_OCTET_LENGTH,");
        sql.append(" A.ORDINAL_POSITION, A.IS_NULLABLE, A.SCOPE_CATALOG AS SCOPE_CATLOG, A.SCOPE_SHEMA, A.SCOPE_TABLE, A.SOURCE_DATA_TYPE");
        sql.append(" FROM INFO_SCHEM.V_SYS_COLUMNS A LEFT JOIN INFO_SCHEM.V_SYS_TYPE_INFO B ON A.DATA_TYPE = B.DATA_TYPE");
        sql.append(" WHERE A.ORDINAL_POSITION > 0");
        if (tablePattern != null && tablePattern.trim().length() > 0) {
            sql.append(" AND TABLE_NAME LIKE ").append(TableNameParser.orgStringToQueryString(tablePattern));
        }
        if (columnPattern != null && columnPattern.trim().length() > 0) {
            sql.append(" AND COLUMN_NAME LIKE ").append(TableNameParser.orgStringToQueryString(columnPattern));
        }
        if (schemaPattern != null && schemaPattern.trim().length() > 0) {
            sql.append(" AND A.TABLE_SCHEM LIKE ").append(TableNameParser.orgStringToQueryString(schemaPattern));
        }
        if (catalogPattern != null && catalogPattern.trim().length() > 0) {
            sql.append(" AND A.TABLE_CAT LIKE ").append(TableNameParser.orgStringToQueryString(catalogPattern));
        }
        sql.append(" ORDER BY A.TABLE_SCHEM,A.TABLE_NAME,A.ORDINAL_POSITION");
        return this.resetColumnName(this.connection.execSQL(sql.toString()));
    }

    public ResultSet getColumnPrivileges(String catalogPattern, String schemaPattern, String table, String columnNamePattern) throws SQLException {
        StringBuffer sql = new StringBuffer(50);
        sql.append("SELECT TABLE_CAT,TABLE_SCHEM,TABLE_NAME,COLUMN_NAME,GRANTOR,GRANTEE,PRIVILEGE,IS_GRANTABLE FROM V_SYS_COLUMN_PRIVILEGES");
        sql.append(" WHERE TABLE_NAME LIKE ");
        if (table == null || table.trim().length() == 0) {
            sql.append("'%'");
        } else {
            sql.append(TableNameParser.orgStringToQueryString(table));
        }
        sql.append(" AND COLUMN_NAME LIKE ");
        if (columnNamePattern == null || columnNamePattern.trim().length() == 0) {
            sql.append("'%'");
        } else {
            sql.append(TableNameParser.orgStringToQueryString(columnNamePattern));
        }
        if (schemaPattern != null && schemaPattern.trim().length() > 0) {
            sql.append(" AND TABLE_SCHEM LIKE ").append(TableNameParser.orgStringToQueryString(schemaPattern));
        }
        if (catalogPattern != null && catalogPattern.trim().length() > 0) {
            sql.append(" AND TABLE_CAT LIKE ").append(TableNameParser.orgStringToQueryString(catalogPattern));
        }
        sql.append(" ORDER BY COLUMN_NAME,PRIVILEGE");
        return this.resetColumnName(this.connection.execSQL(sql.toString()));
    }

    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        StringBuffer sql = new StringBuffer(150);
        sql.append("SELECT TABLE_CAT,TABLE_SCHEM,TABLE_NAME,GRANTOR,GRANTEE,");
        sql.append("PRIVILEGE,IS_GRANTABLE FROM V_SYS_TABLE_PRIVILEGES");
        sql.append(" WHERE TABLE_NAME LIKE ");
        if (tableNamePattern == null) {
            sql.append("'%'");
        } else {
            sql.append(TableNameParser.orgStringToQueryString(tableNamePattern));
        }
        if (schemaPattern != null && schemaPattern.trim().length() > 0) {
            sql.append(" AND TABLE_SCHEM LIKE ").append(TableNameParser.orgStringToQueryString(schemaPattern));
        }
        if (catalog != null && catalog.trim().length() > 0) {
            sql.append(" AND TABLE_CAT LIKE ").append(TableNameParser.orgStringToQueryString(catalog));
        }
        sql.append(" ORDER BY TABLE_SCHEM,TABLE_NAME,PRIVILEGE");
        return this.resetColumnName(this.connection.execSQL(sql.toString()));
    }

    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException {
        StringBuffer sql = new StringBuffer(50);
        sql.append("SELECT SCOPE, A.COLUMN_NAME, B.DATA_TYPE, B.TYPE_NAME, ");
        sql.append(" A.COLUMN_SIZE, A.BUFFER_LENGTH, A.DECIMAL_DIGITS, A.PSEUDO_COLUMN");
        sql.append(" FROM V_SYS_BESTROWIDENTIFIER A, V_SYS_TYPE_INFO B, V_SYS_TABLES C");
        sql.append(" WHERE A.DATA_TYPE = B.DATA_TYPE");
        sql.append(" AND TABLE_NAME LIKE ");
        if (table == null) {
            sql.append("'%'");
        } else {
            sql.append(TableNameParser.orgStringToQueryString(table));
        }
        sql.append(" AND SCOPE =").append(scope);
        sql.append(" AND TABLE_TYPE IN ('TABLE', 'SYSTEM TABLE')");
        if (schema != null && schema.trim().length() > 0) {
            sql.append(" AND TABLE_SCHEM LIKE ").append(TableNameParser.orgStringToQueryString(schema));
        }
        if (catalog != null && catalog.trim().length() > 0) {
            sql.append(" AND TABLE_CAT LIKE ").append(TableNameParser.orgStringToQueryString(catalog));
        }
        sql.append(" ORDER BY SCOPE");
        return this.resetColumnName(this.connection.execSQL(sql.toString()));
    }

    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
        StringBuffer sql = new StringBuffer(50);
        sql.append("SELECT SCOPE, A.COLUMN_NAME, B.DATA_TYPE, B.TYPE_NAME, ");
        sql.append(" A.COLUMN_SIZE, A.BUFFER_LENGTH, A.DECIMAL_DIGITS, A.PSEUDO_COLUMN");
        sql.append(" FROM V_SYS_VERSIONCOLUMNS A, V_SYS_TYPE_INFO B, V_SYS_TABLES C");
        sql.append(" WHERE A.DATA_TYPE = B.DATA_TYPE");
        sql.append(" AND TABLE_NAME LIKE ").append(TableNameParser.orgStringToQueryString(table));
        sql.append(" AND TABLE_TYPE IN ('TABLE', 'SYSTEM TABLE')");
        if (schema != null && schema.trim().length() > 0) {
            sql.append(" AND TABLE_SCHEM LIKE ").append(TableNameParser.orgStringToQueryString(schema));
        }
        if (catalog != null && catalog.trim().length() > 0) {
            sql.append(" AND TABLE_CAT LIKE ").append(TableNameParser.orgStringToQueryString(catalog));
        }
        return this.resetColumnName(this.connection.execSQL(sql.toString()));
    }

    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
        StringBuffer sql = new StringBuffer(50);
        sql.append("SELECT * FROM V_SYS_PRIMARY_KEYS");
        sql.append(" WHERE TABLE_NAME LIKE ");
        if (table == null || table.trim().length() == 0) {
            sql.append("'%'");
        } else {
            sql.append(TableNameParser.orgStringToQueryString(table));
        }
        if (schema != null && schema.trim().length() > 0) {
            sql.append(" AND TABLE_SCHEM LIKE ").append(TableNameParser.orgStringToQueryString(schema));
        }
        if (catalog != null && catalog.trim().length() > 0) {
            sql.append(" AND TABLE_CAT LIKE ").append(TableNameParser.orgStringToQueryString(catalog));
        }
        sql.append(" ORDER BY KEY_SEQ");
        return this.resetColumnName(this.connection.execSQL(sql.toString()));
    }

    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
        StringBuffer sql = new StringBuffer(50);
        sql.append("SELECT * FROM V_SYS_FOREIGN_KEYS");
        sql.append(" WHERE FKTABLE_NAME LIKE ");
        if (table == null) {
            sql.append("'%'");
        } else {
            sql.append(TableNameParser.orgStringToQueryString(table));
        }
        if (schema != null && schema.trim().length() > 0) {
            sql.append(" AND FKTABLE_SCHEM LIKE ").append(TableNameParser.orgStringToQueryString(schema));
        }
        if (catalog != null && catalog.trim().length() > 0) {
            sql.append(" AND FKTABLE_CAT LIKE ").append(TableNameParser.orgStringToQueryString(catalog));
        }
        sql.append(" ORDER BY KEY_SEQ");
        return this.resetColumnName(this.connection.execSQL(sql.toString()));
    }

    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
        StringBuffer sql = new StringBuffer(50);
        sql.append("SELECT * FROM V_SYS_FOREIGN_KEYS");
        sql.append(" WHERE PKTABLE_NAME LIKE ");
        if (table == null) {
            sql.append("'%'");
        } else {
            sql.append(TableNameParser.orgStringToQueryString(table));
        }
        if (schema != null && schema.trim().length() > 0) {
            sql.append(" AND PKTABLE_SCHEM LIKE ").append(TableNameParser.orgStringToQueryString(schema));
        }
        if (catalog != null && catalog.trim().length() > 0) {
            sql.append(" AND PKTABLE_CAT LIKE ").append(TableNameParser.orgStringToQueryString(catalog));
        }
        sql.append(" ORDER BY KEY_SEQ");
        return this.resetColumnName(this.connection.execSQL(sql.toString()));
    }

    public ResultSet getCrossReference(String primaryCatalog, String primarySchema, String primaryTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
        StringBuffer sql = new StringBuffer(50);
        sql.append("SELECT * FROM V_SYS_FOREIGN_KEYS");
        sql.append(" WHERE PKTABLE_NAME LIKE ");
        if (primaryTable == null) {
            sql.append("'%'");
        } else {
            sql.append(TableNameParser.orgStringToQueryString(primaryTable));
        }
        sql.append(" AND FKTABLE_NAME LIKE ");
        if (foreignTable == null) {
            sql.append("'%'");
        } else {
            sql.append(TableNameParser.orgStringToQueryString(foreignTable));
        }
        if (primarySchema != null && primarySchema.trim().length() > 0) {
            sql.append(" AND PKTABLE_SCHEM LIKE ").append(TableNameParser.orgStringToQueryString(primarySchema));
        }
        if (primaryCatalog != null && primaryCatalog.trim().length() > 0) {
            sql.append(" AND PKTABLE_CAT LIKE ").append(TableNameParser.orgStringToQueryString(primaryCatalog));
        }
        if (foreignSchema != null && foreignSchema.trim().length() > 0) {
            sql.append(" AND FKTABLE_SCHEM LIKE ").append(TableNameParser.orgStringToQueryString(foreignSchema));
        }
        if (foreignCatalog != null && foreignCatalog.trim().length() > 0) {
            sql.append(" AND FKTABLE_CAT LIKE ").append(TableNameParser.orgStringToQueryString(foreignCatalog));
        }
        sql.append(" ORDER BY KEY_SEQ");
        return this.resetColumnName(this.connection.execSQL(sql.toString()));
    }

    public ResultSet getTypeInfo() throws SQLException {
        String sql = "SELECT TYPE_NAME,SQL_CONCISE_TYPE AS DATA_TYPE,\"PRECISION\",LITERAL_PREFIX,LITERAL_SUFFIX,CREATE_PARAMS,NULLABLE,CASE_SENSITIVE,SEARCHABLE,UNSIGNED_ATTRIBUTE,FIXED_PREC_SCALE,AUTO_INCREMENT,LOCAL_TYPE_NAME,MINIMUM_SCALE,MAXIMUM_SCALE,SQL_DATA_TYPE,SQL_DATETIME_SUB,NUM_PREC_RADIX FROM V_SYS_TYPE_INFO ORDER BY DATA_TYPE";
        return this.resetColumnName(this.connection.execSQL(sql));
    }

    public ResultSet getIndexInfo(String catalogPattern, String schemaPattern, String table, boolean unique, boolean approximate) throws SQLException {
        if (table == null) {
            throw new OSQLException("OSCAR-00904", "88888", 904);
        }
        StringBuffer sql = new StringBuffer(50);
        sql.append("SELECT * FROM V_SYS_INDEX_STATISTICS");
        sql.append(" WHERE TABLE_NAME LIKE ").append(TableNameParser.orgStringToQueryString(table));
        if (unique) {
            sql.append(" AND (NON_UNIQUE = 0 OR NON_UNIQUE IS NULL)");
        }
        if (schemaPattern != null && schemaPattern.trim().length() > 0) {
            sql.append(" AND TABLE_SCHEM LIKE ").append(TableNameParser.orgStringToQueryString(schemaPattern));
        }
        if (catalogPattern != null && catalogPattern.trim().length() > 0) {
            sql.append(" AND TABLE_CAT LIKE ").append(TableNameParser.orgStringToQueryString(catalogPattern));
        }
        sql.append(" ORDER BY NON_UNIQUE, TYPE, INDEX_NAME, ORDINAL_POSITION");
        BaseResultSet rs = this.connection.execSQL(sql.toString());
        this.resetColumnName(rs);
        return (ResultSet)Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{ResultSet.class}, new ResultSetInvocationHandler((OscarResultSet)rs));
    }

    protected BaseResultSet resetColumnName(BaseResultSet brs) {
        Field[] fields = brs.getFields();
        for (int i = 0; i < fields.length; ++i) {
            fields[i].resetName();
        }
        return brs;
    }

    public boolean supportsResultSetType(int type) throws SQLException {
        return type != 1005;
    }

    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
        if (type == 1005) {
            return false;
        }
        if (concurrency == 1008) {
            return true;
        }
        return true;
    }

    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        return true;
    }

    public boolean ownDeletesAreVisible(int type) throws SQLException {
        return type != 1003;
    }

    public boolean ownInsertsAreVisible(int type) throws SQLException {
        return true;
    }

    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        return true;
    }

    public boolean othersDeletesAreVisible(int type) throws SQLException {
        return type != 1003;
    }

    public boolean othersInsertsAreVisible(int type) throws SQLException {
        return true;
    }

    public boolean updatesAreDetected(int type) throws SQLException {
        return false;
    }

    public boolean deletesAreDetected(int i) throws SQLException {
        return false;
    }

    public boolean insertsAreDetected(int type) throws SQLException {
        return false;
    }

    public boolean supportsBatchUpdates() throws SQLException {
        return true;
    }

    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException {
        StringBuffer sql = new StringBuffer(200);
        sql.append("SELECT NULL AS TYPE_CAT, NSPNAME AS TYPE_SCHEM, NAME AS TYPE_NAME, NULL AS CLASS_NAME, '2002' AS DATA_TYPE, '2002' AS BASE_TYPE\uff0c NULL AS REMARKS  ").append("FROM INFO_SCHEM.SYS_PL_OBJ T LEFT JOIN INFO_SCHEM.V_SYS_NAMESPACE N  ON N.OID = TYPNAMESPACE WHERE 1 = 1");
        String[] localObject = new String[1];
        String[] arrayOfString = new String[1];
        if (typeNamePattern != null && typeNamePattern.trim().length() != 0) {
            if (TableNameParser.parse(typeNamePattern, localObject, arrayOfString)) {
                sql.append(" AND NAME LIKE ").append(TableNameParser.orgStringToQueryString(arrayOfString[0]));
                sql.append(" AND N.NSPNAME LIKE ").append(TableNameParser.orgStringToQueryString(localObject[0]));
            } else {
                sql.append(" AND NAME LIKE ").append(TableNameParser.orgStringToQueryString(arrayOfString[0]));
            }
        }
        if (localObject[0] == null && schemaPattern != null && schemaPattern.trim().length() != 0) {
            sql.append(" AND N.NSPNAME LIKE ").append(TableNameParser.orgStringToQueryString(schemaPattern));
        }
        sql.append(" ORDER BY TYPE_SCHEM, TYPE_NAME");
        return this.resetColumnName(this.connection.execSQL(sql.toString()));
    }

    public Connection getConnection() throws SQLException {
        return this.connection;
    }

    public boolean rowChangesAreDetected(int type) throws SQLException {
        return false;
    }

    public boolean rowChangesAreVisible(int type) throws SQLException {
        return false;
    }

    public boolean supportsSavepoints() throws SQLException {
        return true;
    }

    public boolean supportsNamedParameters() throws SQLException {
        return true;
    }

    public boolean supportsMultipleOpenResults() throws SQLException {
        return true;
    }

    public boolean supportsGetGeneratedKeys() throws SQLException {
        return true;
    }

    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException {
        return this.resetColumnName(this.connection.execSQL("select * from v_sys_super_types"));
    }

    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        Field[] fields = new Field[]{new Field((BaseConnection)this.connection, "", 1042, 32, 0, "TABLE_CAT", "", "", 0), new Field((BaseConnection)this.connection, "", 1042, 32, 0, "TABLE_SCHEM", "", "", 0), new Field((BaseConnection)this.connection, "", 1042, 32, 0, "TABLE_NAME", "", "", 0), new Field((BaseConnection)this.connection, "", 1042, 32, 0, "SUPERTABLE_NAME", "", "", 0)};
        return this.connection.getDefaultStatement().createResultSet(fields, new ArrayList(), null, 0, 0L);
    }

    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException {
        Field[] fields = new Field[]{new Field((BaseConnection)this.connection, "", 1042, 32, 0, "TYPE_CAT", "", "", 0), new Field((BaseConnection)this.connection, "", 1042, 32, 0, "TYPE_SCHEM", "", "", 0), new Field((BaseConnection)this.connection, "", 1042, 32, 0, "TYPE_NAME", "", "", 0), new Field((BaseConnection)this.connection, "", 1042, 32, 0, "ATTR_NAME", "", "", 0), new Field((BaseConnection)this.connection, "", 21, 32, 0, "DATA_TYPE", "", "", 0), new Field((BaseConnection)this.connection, "", 1042, 32, 0, "ATTR_TYPE_NAME", "", "", 0), new Field((BaseConnection)this.connection, "", 23, 32, 0, "ATTR_SIZE", "", "", 0), new Field((BaseConnection)this.connection, "", 23, 32, 0, "DECIMAL_DIGITS", "", "", 0), new Field((BaseConnection)this.connection, "", 23, 32, 0, "NUM_PREC_RADIX", "", "", 0), new Field((BaseConnection)this.connection, "", 23, 32, 0, "NULLABLE", "", "", 0), new Field((BaseConnection)this.connection, "", 1042, 32, 0, "REMARKS", "", "", 0), new Field((BaseConnection)this.connection, "", 1042, 32, 0, "ATTR_DEF", "", "", 0), new Field((BaseConnection)this.connection, "", 23, 32, 0, "SQL_DATA_TYPE", "", "", 0), new Field((BaseConnection)this.connection, "", 23, 32, 0, "SQL_DATETIME_SUB", "", "", 0), new Field((BaseConnection)this.connection, "", 23, 32, 0, "CHAR_OCTET_LENGTH", "", "", 0), new Field((BaseConnection)this.connection, "", 23, 32, 0, "ORDINAL_POSITION", "", "", 0), new Field((BaseConnection)this.connection, "", 1042, 32, 0, "IS_NULLABLE", "", "", 0), new Field((BaseConnection)this.connection, "", 1042, 32, 0, "SCOPE_CATALOG", "", "", 0), new Field((BaseConnection)this.connection, "", 1042, 32, 0, "SCOPE_SCHEMA", "", "", 0), new Field((BaseConnection)this.connection, "", 1042, 32, 0, "SCOPE_TABLE", "", "", 0), new Field((BaseConnection)this.connection, "", 21, 32, 0, "SOURCE_DATA_TYPE", "", "", 0)};
        return this.connection.getDefaultStatement().createResultSet(fields, new ArrayList(), null, 0, 0L);
    }

    public boolean supportsResultSetHoldability(int holdability) throws SQLException {
        return true;
    }

    public int getResultSetHoldability() throws SQLException {
        return 2;
    }

    public int getDatabaseMajorVersion() throws SQLException {
        return this.connection.getVersion().getDBMajorVersion();
    }

    public int getDatabaseMinorVersion() throws SQLException {
        return this.connection.getVersion().getDBMinorVersion();
    }

    public int getJDBCMajorVersion() throws SQLException {
        return 3;
    }

    public int getJDBCMinorVersion() throws SQLException {
        return 0;
    }

    public int getSQLStateType() throws SQLException {
        return 1;
    }

    public boolean locatorsUpdateCopy() throws SQLException {
        return false;
    }

    public boolean supportsStatementPooling() throws SQLException {
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Integer getValue(String columnName) throws SQLException {
        if (this.dbProps.get(columnName) == null) {
            PreparedStatement psmt;
            block7: {
                psmt = null;
                ResultSet rs = null;
                try {
                    psmt = this.connection.prepareStatement("select NAME, VALUE  from v_sys_limits");
                    psmt.setFetchSize(0);
                    rs = psmt.executeQuery();
                    while (rs.next()) {
                        if (rs.getString(1).equalsIgnoreCase("MaxKstoreDPDataSize")) continue;
                        this.dbProps.put(rs.getString(1), rs.getInt(2));
                    }
                    Object var5_4 = null;
                    if (rs == null) break block7;
                }
                catch (Throwable throwable) {
                    Object var5_5 = null;
                    if (rs != null) {
                        rs.close();
                    }
                    if (psmt != null) {
                        psmt.close();
                    }
                    throw throwable;
                }
                rs.close();
            }
            if (psmt != null) {
                psmt.close();
            }
        }
        return this.dbProps.get(columnName);
    }

    static {
        tableTypeClauses = new Hashtable();
        Hashtable<String, String> ht = new Hashtable<String, String>();
        tableTypeClauses.put("TABLE", ht);
        ht.put("SCHEMAS", "c.relkind = 'r' AND n.nspname NOT LIKE 'pg\\\\_%'");
        ht.put("NOSCHEMAS", "c.relkind = 'r' AND c.relname NOT LIKE 'pg\\\\_%'");
        ht = new Hashtable();
        tableTypeClauses.put("VIEW", ht);
        ht.put("SCHEMAS", "c.relkind = 'v' AND n.nspname <> 'INFO_SCHEM'");
        ht.put("NOSCHEMAS", "c.relkind = 'v' AND c.relname NOT LIKE 'pg\\\\_%'");
        ht = new Hashtable();
        tableTypeClauses.put("INDEX", ht);
        ht.put("SCHEMAS", "c.relkind = 'i' AND n.nspname NOT LIKE 'pg\\\\_%'");
        ht.put("NOSCHEMAS", "c.relkind = 'i' AND c.relname NOT LIKE 'pg\\\\_%'");
        ht = new Hashtable();
        tableTypeClauses.put("SEQUENCE", ht);
        ht.put("SCHEMAS", "c.relkind = 'S'");
        ht.put("NOSCHEMAS", "c.relkind = 'S'");
        ht = new Hashtable();
        tableTypeClauses.put("SYSTEM TABLE", ht);
        ht.put("SCHEMAS", "c.relkind = 'r' AND n.nspname = 'INFO_SCHEM'");
        ht.put("NOSCHEMAS", "c.relkind = 'r' AND c.relname LIKE 'pg\\\\_%' AND c.relname NOT LIKE 'pg\\\\_toast\\\\_%' AND c.relname NOT LIKE 'pg\\\\_temp\\\\_%'");
        ht = new Hashtable();
        tableTypeClauses.put("SYSTEM TOAST TABLE", ht);
        ht.put("SCHEMAS", "c.relkind = 'r' AND n.nspname = 'sys_toast'");
        ht.put("NOSCHEMAS", "c.relkind = 'r' AND c.relname LIKE 'pg\\\\_toast\\\\_%'");
        ht = new Hashtable();
        tableTypeClauses.put("SYSTEM TOAST INDEX", ht);
        ht.put("SCHEMAS", "c.relkind = 'i' AND n.nspname = 'sys_toast'");
        ht.put("NOSCHEMAS", "c.relkind = 'i' AND c.relname LIKE 'pg\\\\_toast\\\\_%'");
        ht = new Hashtable();
        tableTypeClauses.put("SYSTEM VIEW", ht);
        ht.put("SCHEMAS", "c.relkind = 'v' AND n.nspname = 'INFO_SCHEM' ");
        ht.put("NOSCHEMAS", "c.relkind = 'v' AND c.relname LIKE 'pg\\\\_%'");
        ht = new Hashtable();
        tableTypeClauses.put("SYSTEM INDEX", ht);
        ht.put("SCHEMAS", "c.relkind = 'i' AND n.nspname = 'INFO_SCHEM'");
        ht.put("NOSCHEMAS", "c.relkind = 'v' AND c.relname LIKE 'pg\\\\_%' AND c.relname NOT LIKE 'pg\\\\_toast\\\\_%' AND c.relname NOT LIKE 'pg\\\\_temp\\\\_%'");
        ht = new Hashtable();
        tableTypeClauses.put("TEMPORARY TABLE", ht);
        ht.put("SCHEMAS", "c.relkind = 'r' AND n.nspname LIKE 'pg\\\\_temp\\\\_%' ");
        ht.put("NOSCHEMAS", "c.relkind = 'r' AND c.relname LIKE 'pg\\\\_temp\\\\_%' ");
        ht = new Hashtable();
        tableTypeClauses.put("TEMPORARY INDEX", ht);
        ht.put("SCHEMAS", "c.relkind = 'i' AND n.nspname LIKE 'pg\\\\_temp\\\\_%' ");
        ht.put("NOSCHEMAS", "c.relkind = 'i' AND c.relname LIKE 'pg\\\\_temp\\\\_%' ");
        defaultTableTypes = new String[]{"TABLE", "VIEW", "INDEX", "SEQUENCE", "TEMPORARY TABLE"};
        TYPE_NAME = new String[]{"ARRAY", "BIGINT", "BINARY", "BIT", "BLOB", "BOOLEAN", "CHAR", "CLOB", "DATALINK", "DATE", "DECIMAL", "DISTINCT", "DOUBLE", "FLOAT", "INTEGER", "JAVA_OBJECT", "LONBINARY", "LONGVARCHAR", "NULL", "NUMERIC", "OTHER", "REAL", "REF", "SMALLINT", "STRUCT", "TIME", "TIMESTAMP", "TINYINT", "VARBINARY", "VARCHAR"};
        DATA_TYPE = new int[]{2003, -5, -2, -7, 2004, 16, 1, 2005, 70, 91, 3, 2001, 8, 6, 4, 2000, -4, -1, 0, 2, 1111, 7, 2006, 5, 2002, 92, 93, -6, -3, 12};
    }

    private class ResultSetInvocationHandler
    implements InvocationHandler {
        private final int indexNonunique = 3;
        private final OscarResultSet resultSet;

        ResultSetInvocationHandler(OscarResultSet resultSet) {
            this.resultSet = resultSet;
            try {
                this.convertValue(this.resultSet);
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (args != null && args.length == 1 && ("NON_UNIQUE".equalsIgnoreCase(args[0].toString()) || String.valueOf(3).equals(args[0].toString())) && "getBoolean".equals(method.getName()) && this.resultSet.this_row[3] == null) {
                return true;
            }
            Object res = method.invoke(this.resultSet, args);
            return res;
        }

        private void convertValue(BaseResultSet rs) throws NumberFormatException, SQLException {
            List tuples = rs.getTuples();
            for (byte[][] tup : tuples) {
                if (tup[3] == null) continue;
                int nonunique = -1;
                nonunique = OscarDatabaseMetaData.this.connection.compatibleOldProtocol || OscarDatabaseMetaData.this.connection.netDataByStr ? Integer.parseInt(OscarDatabaseMetaData.this.encoding.decode(tup[3])) : NumberConverter.convertBytesToInt(tup[3]);
                if (nonunique < 0) continue;
                tup[3] = OscarDatabaseMetaData.this.encoding.encode(String.valueOf(nonunique == 1));
            }
            rs.getFields()[3].setOID(16);
        }
    }
}

