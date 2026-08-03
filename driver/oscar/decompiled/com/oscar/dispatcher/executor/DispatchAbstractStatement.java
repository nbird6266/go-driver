/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.dispatcher.executor;

import com.oscar.Driver;
import com.oscar.dispatcher.core.ConnectionMangerV2;
import com.oscar.dispatcher.entity.DispatchConnection;
import com.oscar.dispatcher.entity.FakeConnection;
import com.oscar.dispatcher.entity.LsnVo;
import com.oscar.dispatcher.executor.command.StatementCreateCommand;
import com.oscar.dispatcher.oscarParser.sql.OscarParser;
import com.oscar.dispatcher.parser.statement.BeginStatement;
import com.oscar.dispatcher.parser.statement.EndStatement;
import com.oscar.dispatcher.parser.statement.Statement;
import com.oscar.jdbc.OscarJdbc2Connection;
import com.oscar.util.OSQLException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class DispatchAbstractStatement {
    public static final int DISPATCH_TYPE_MAIN = 0;
    public static final int DISPATCH_TYPE_SLAVE = 1;
    public static final int DISPATCH_TYPE_ALL = 2;
    public static final int DISPATCH_TYPE_ALL_CURRENT = 3;
    protected DispatchConnection dispatchConnection;
    protected java.sql.Statement mainStatement;
    protected java.sql.Statement slaveStatement;
    protected Boolean replaceProcessingEnabled = null;
    protected Integer maxFieldSize = null;
    protected Integer fetchSize = null;
    protected Integer maxrows = null;
    protected Integer fetchdirection = null;
    protected Integer timeout = null;
    protected String cursor = null;
    StatementCreateCommand<? extends java.sql.Statement> createCommand = null;
    String strategyValue = null;
    protected boolean logFlag = Driver.getLogLevel() >= 2;
    private boolean slaveConnectionDisable = false;
    protected volatile int dispatchType = 0;
    protected volatile int currentDispatchType = 0;

    public java.sql.Statement getMainStatement() throws SQLException {
        if (this.mainStatement == null) {
            this.mainStatement = this.createCommand.getStatement(this.dispatchConnection.getMainConn());
            this.initStatement(this.mainStatement);
        }
        return this.mainStatement;
    }

    public List<java.sql.Statement> getAllStatement() throws SQLException {
        ArrayList<java.sql.Statement> result = new ArrayList<java.sql.Statement>();
        result.add(this.getMainStatement());
        java.sql.Statement slaveStatement = this.getSlaveStatement();
        if (slaveStatement != null) {
            result.add(this.getSlaveStatement());
        }
        return result;
    }

    public java.sql.Statement getSlaveStatement() throws SQLException {
        if (this.slaveConnectionDisable) {
            return null;
        }
        if (this.slaveStatement == null) {
            this.slaveStatement = this.createSlaveStatement();
        } else if (this.slaveStatement.getConnection() == null || this.slaveStatement.getConnection().isClosed()) {
            java.sql.Statement newStatement = this.createSlaveStatement();
            try {
                this.bind(this.slaveStatement, newStatement);
            }
            catch (Exception e) {
                throw new SQLException(e.toString());
            }
            this.slaveStatement = newStatement;
        }
        return this.slaveStatement;
    }

    private java.sql.Statement createSlaveStatement() throws SQLException {
        String key = "slave" + ConnectionMangerV2.lastSlaveID.get(this.dispatchConnection.url).incrementAndGet() % this.dispatchConnection.slaveCount;
        Connection conn = this.dispatchConnection.getSlaveConnection(key);
        if (conn instanceof FakeConnection) {
            this.slaveConnectionDisable = true;
            return null;
        }
        java.sql.Statement slaveStatement = this.createCommand.getStatement(conn);
        this.initStatement(slaveStatement);
        return slaveStatement;
    }

    protected abstract void initStatement(java.sql.Statement var1) throws SQLException;

    public List<java.sql.Statement> getAllCurrentStatement() throws SQLException {
        ArrayList<java.sql.Statement> result = new ArrayList<java.sql.Statement>();
        if (this.mainStatement != null) {
            result.add(this.mainStatement);
        }
        if (this.slaveStatement != null) {
            result.add(this.slaveStatement);
        }
        return result;
    }

    protected int getExecuteType(String sql) {
        String[] directToMainArray;
        int result = 0;
        if (this.dispatchConnection.slaveCount == 0) {
            this.currentDispatchType = 0;
            if (this.logFlag) {
                Driver.writeLog("session: " + this.dispatchConnection.getMainConn().sessionID + ", " + DispatchAbstractStatement.class + ", getExecuteType(String sql), sql:" + sql + ", slaveCount=0 ");
            }
            return this.currentDispatchType;
        }
        String[] directToSlaveArray = this.dispatchConnection.getMainConn().getDirectToSlaveArray();
        if (directToSlaveArray != null && directToSlaveArray.length != 0) {
            for (String dirSQL : directToSlaveArray) {
                if ("".equals(dirSQL) || sql == null || sql.indexOf(dirSQL) < 0) continue;
                this.currentDispatchType = 1;
                if (this.logFlag) {
                    Driver.writeLog("session: " + this.dispatchConnection.getMainConn().sessionID + ", " + DispatchAbstractStatement.class + ", getExecuteType(String sql), sql:" + sql + ",dirSlaveSQL= " + dirSQL);
                }
                return this.currentDispatchType;
            }
        }
        if ((directToMainArray = this.dispatchConnection.getMainConn().getDirectToMainArray()) != null && directToMainArray.length != 0) {
            for (String dirSQL : directToMainArray) {
                if ("".equals(dirSQL) || sql == null || sql.indexOf(dirSQL) < 0) continue;
                this.currentDispatchType = 0;
                if (this.logFlag) {
                    Driver.writeLog("session: " + this.dispatchConnection.getMainConn().sessionID + ", " + DispatchAbstractStatement.class + ", getExecuteType(String sql), sql:" + sql + ",dirSQL= " + dirSQL);
                }
                return this.currentDispatchType;
            }
        }
        if (this.strategyValue == null) {
            this.strategyValue = this.dispatchConnection.props.getProperty("TRANSACTIONDISPATCHSTRATEGY", "2");
        }
        OscarParser oscarParser = new OscarParser(sql, this.strategyValue);
        try {
            Statement sqlStmt = oscarParser.doParse(this.dispatchConnection);
            this.strategyValue = oscarParser.getStrategyValue();
            if (sqlStmt instanceof BeginStatement || sqlStmt instanceof EndStatement) {
                // empty if block
            }
            String sqlType = null;
            int type = sqlStmt.getSQLType();
            switch (type) {
                case 0: {
                    result = 0;
                    sqlType = "in transaction";
                    this.add();
                    break;
                }
                case 2: {
                    result = 1;
                    result = this.changeExecuteType();
                    sqlType = "select";
                    break;
                }
                case 5: {
                    result = 2;
                    sqlType = "set or reset";
                    this.add();
                    break;
                }
                default: {
                    result = 0;
                    sqlType = "insert or delete or update";
                    this.add();
                }
            }
            if (this.logFlag) {
                Driver.writeLog("session: " + this.dispatchConnection.getMainConn().sessionID + ", " + DispatchAbstractStatement.class + ", getExecuteType(String sql), sql:" + sql + ", sqlType:" + sqlType);
            }
        }
        catch (Exception e) {
            Driver.writeLog("prase " + e.getMessage());
        }
        this.currentDispatchType = result;
        return result;
    }

    protected int getExecuteType() {
        return this.dispatchType;
    }

    protected int getCurrentExecuteType() {
        return this.currentDispatchType;
    }

    protected <R> R executeTemplet(ExecuteCommand<R> command, int dispatchType) throws SQLException {
        if (this.slaveConnectionDisable && dispatchType == 1) {
            Driver.writeLog("slave connection init false " + command.getFunctionName());
            dispatchType = 0;
        }
        if (this.currentDispatchType != dispatchType) {
            this.currentDispatchType = dispatchType;
        }
        R r = null;
        StringBuilder sql = null;
        java.sql.Statement st = null;
        if (this.logFlag) {
            sql = new StringBuilder();
            sql.append("Main session: ").append(this.dispatchConnection.getMainConn().sessionID).append(", ").append(DispatchAbstractStatement.class).append(", executeTemplet()" + command.getFunctionName() + " ,dispatchType :").append(dispatchType + " ");
        }
        switch (dispatchType) {
            case 0: {
                st = this.getMainStatement();
                if (this.logFlag) {
                    sql.append(", send to master session:").append(((OscarJdbc2Connection)st.getConnection()).sessionID).append(" , url:").append(((OscarJdbc2Connection)st.getConnection()).getURL() + " st" + st);
                    Driver.writeLog(sql.toString());
                }
                r = command.execute(st);
                break;
            }
            case 1: {
                try {
                    st = this.getSlaveStatement();
                    if (st == null || this.dispatchConnection.getMainConn().getSlaveDelayTime() > 0L && st.getConnection() instanceof OscarJdbc2Connection && System.currentTimeMillis() < ((OscarJdbc2Connection)st.getConnection()).getSleepEndTime()) {
                        st = this.getMainStatement();
                        if (this.logFlag) {
                            sql.append(", error ,send to master session:").append(((OscarJdbc2Connection)st.getConnection()).sessionID).append(" , url:").append(((OscarJdbc2Connection)st.getConnection()).getURL() + " st" + st);
                            Driver.writeLog(sql.toString());
                        }
                        r = command.execute(st);
                        this.currentDispatchType = 0;
                        break;
                    }
                    if (this.logFlag) {
                        sql.append(", send to slave session:").append(((OscarJdbc2Connection)st.getConnection()).sessionID).append(" , url:").append(((OscarJdbc2Connection)st.getConnection()).getURL() + " st" + st);
                        Driver.writeLog(sql.toString());
                    }
                    if (Boolean.valueOf(this.dispatchConnection.props.getProperty("USESLAVESYNCREAD", "false")).booleanValue() && ((OscarJdbc2Connection)st.getConnection()).getProtocolVersion().getProtocolType() >= 3 && command.isExecuteFunction()) {
                        LsnVo lv = this.dispatchConnection.getLsnVo();
                        long slaveCurrentLsn = lv.sendLsn(st);
                        r = command.execute(st);
                        if (slaveCurrentLsn <= 0L) break;
                        ((OscarJdbc2Connection)st.getConnection()).setLsnValue(slaveCurrentLsn);
                        break;
                    }
                    r = command.execute(st);
                    break;
                }
                catch (SQLException e) {
                    if (this.logFlag) {
                        Driver.writeLog(e);
                    }
                    if (this.expectionHandler(e, st, this.getMainStatement())) {
                        try {
                            if (this.dispatchConnection.getMainConn().getSlaveDelayTime() > 0L && st.getConnection() instanceof OscarJdbc2Connection) {
                                ((OscarJdbc2Connection)st.getConnection()).setSleepEndTime(System.currentTimeMillis() + this.dispatchConnection.getMainConn().getSlaveDelayTime());
                            }
                            st = this.getMainStatement();
                            if (this.logFlag) {
                                sql.append(", ===========error============== ,send to master session:").append(((OscarJdbc2Connection)st.getConnection()).sessionID).append(" , url:").append(((OscarJdbc2Connection)st.getConnection()).getURL() + " st" + st);
                                Driver.writeLog(sql.toString());
                            }
                            r = command.execute(st);
                            this.currentDispatchType = 0;
                            this.dispatchType = 0;
                            if (!e.getSQLState().equals("0A502") || !"2".equals(this.strategyValue)) break;
                            this.dispatchConnection.setHasUpdate(true);
                            break;
                        }
                        catch (SQLException ei) {
                            if (this.logFlag) {
                                Driver.writeLog("============failare swtich to main=========" + ei.getLocalizedMessage());
                                ei.printStackTrace();
                            }
                            throw ei;
                        }
                        catch (Exception ei) {
                            if (this.logFlag) {
                                Driver.writeLog("============failare swtich to main 111 =========" + ei.getLocalizedMessage());
                                ei.printStackTrace();
                            }
                            throw new OSQLException("failare swtich to main", "08001", 121, ei);
                        }
                    }
                    if (this.logFlag) {
                        Driver.writeLog("============failare swtich to main=========currentDispatchType :" + this.currentDispatchType + " dispatchType: " + dispatchType + " this.dispatchType:" + this.dispatchType);
                    }
                    throw e;
                }
            }
            case 2: {
                List<java.sql.Statement> list = this.getAllStatement();
                if (this.logFlag) {
                    sql.append(", send to all: ");
                    for (java.sql.Statement t : this.getAllStatement()) {
                        sql.append("session : ").append(((OscarJdbc2Connection)t.getConnection()).sessionID).append(" , url:").append(((OscarJdbc2Connection)t.getConnection()).getURL() + " st" + st).append(" , ");
                    }
                    Driver.writeLog(sql.toString());
                }
                for (java.sql.Statement t : list) {
                    r = command.execute(t);
                }
                break;
            }
            case 3: {
                List<java.sql.Statement> lis = this.getAllCurrentStatement();
                if (this.logFlag) {
                    sql.append(", send to current:");
                    for (java.sql.Statement t : lis) {
                        sql.append("session : ").append(((OscarJdbc2Connection)t.getConnection()).sessionID).append(" , url:").append(((OscarJdbc2Connection)t.getConnection()).getURL() + " st" + st).append(" , ");
                    }
                    Driver.writeLog(sql.toString());
                }
                for (java.sql.Statement t : lis) {
                    r = command.execute(t);
                }
                break;
            }
        }
        return r;
    }

    protected boolean getErrorCode(SQLException e) {
        boolean flag = false;
        if (e.getSQLState().equals("0A502") || e.getSQLState().equals("08003") || e.getSQLState().equals("08001")) {
            flag = true;
        }
        return flag;
    }

    protected boolean isDisconnected(SQLException e) {
        return "08003".equals(e.getSQLState());
    }

    private Integer changeExecuteType() {
        int result = 1;
        String rate = this.dispatchConnection.props.getProperty("HOSTLOADRATE");
        if (Integer.valueOf(rate) > 0) {
            String url = this.dispatchConnection.url;
            ConnectionMangerV2.totalAcount.get(url).incrementAndGet();
            if (ConnectionMangerV2.hostAcount.get(url).get() > 0) {
                BigDecimal hostRate = new BigDecimal(ConnectionMangerV2.hostAcount.get(url).get()).divide(new BigDecimal(ConnectionMangerV2.totalAcount.get(url).get()), 2, 1);
                if (hostRate.multiply(new BigDecimal(100)).compareTo(new BigDecimal(rate)) == -1) {
                    result = 0;
                    ConnectionMangerV2.hostAcount.get(url).incrementAndGet();
                }
            } else {
                result = 0;
                ConnectionMangerV2.hostAcount.get(url).incrementAndGet();
            }
        }
        return result;
    }

    public void add() {
        if (Integer.valueOf(this.dispatchConnection.props.getProperty("HOSTLOADRATE")) > 0) {
            String url = this.dispatchConnection.url;
            ConnectionMangerV2.totalAcount.get(url).incrementAndGet();
            ConnectionMangerV2.hostAcount.get(url).incrementAndGet();
        }
    }

    protected StatementCreateCommand<? extends java.sql.Statement> getCommand() {
        return this.createCommand;
    }

    protected abstract boolean expectionHandler(SQLException var1, java.sql.Statement var2, java.sql.Statement var3) throws SQLException;

    protected void bind(java.sql.Statement slave, java.sql.Statement master) throws Exception {
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    protected static interface ExecuteCommand<R> {
        public boolean isExecuteFunction();

        public String getFunctionName();

        public R execute(java.sql.Statement var1) throws SQLException;
    }
}

