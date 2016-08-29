package com.fr.bi.data;

import com.fr.base.FRContext;
import com.fr.bi.common.inter.Traversal;
import com.fr.bi.stable.constant.DBConstant;
import com.fr.bi.stable.data.db.BIDataValue;
import com.fr.bi.stable.data.db.ICubeFieldSource;
import com.fr.bi.stable.data.db.SQLStatement;
import com.fr.bi.stable.dbdealer.*;
import com.fr.bi.stable.utils.code.BILogger;
import com.fr.data.core.db.DBUtils;
import com.fr.data.core.db.dialect.Dialect;
import com.fr.data.core.db.dialect.DialectFactory;
import com.fr.general.DateUtils;
import com.fr.stable.StringUtils;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * This class created on 2016/8/11.
 *
 * @author Connery
 * @since Advanced FineBI Analysis 1.0
 */
public abstract class DBExtractorImpl implements DBExtractor {

    private int dealWithResultSet(ResultSet rs,
                                  ICubeFieldSource[] columns,
                                  Traversal<BIDataValue> traversal,
                                  boolean needCharSetConvert,
                                  String originalCharSetName,
                                  String newCharSetName, int row) throws SQLException {
        @SuppressWarnings("rawtypes")
        DBDealer[] dealers = createDBDealer(needCharSetConvert, originalCharSetName, newCharSetName, columns);
        int ilen = dealers.length;
        while (rs.next()) {
            for (int i = 0; i < ilen; i++) {
                Object value = dealers[i].dealWithResultSet(rs);
                traversal.actionPerformed(new BIDataValue(row, i, value));
            }
            row++;
        }
        return row;
    }

    @SuppressWarnings("rawtypes")
    private DBDealer[] createDBDealer(boolean needCharSetConvert, String originalCharSetName,
                                      String newCharSetName, ICubeFieldSource[] columns) {
        List<DBDealer> res = new ArrayList<DBDealer>();
        for (int i = 0, ilen = columns.length; i < ilen; i++) {
            ICubeFieldSource field = columns[i];
            if (field.isUsable()) {
                DBDealer object = null;
                int rsColumn = i + 1;
                switch (field.getFieldType()) {
                    case DBConstant.COLUMN.DATE: {
                        switch (field.getClassType()) {
                           case DBConstant.CLASS.DATE : {
                               object = new DateDealer(rsColumn);
                               break;
                           }
                            case DBConstant.CLASS.TIME : {
                                object = new TimeDealer(rsColumn);
                                break;
                            }
                            case DBConstant.CLASS.TIMESTAMP : {
                                object = new TimestampDealer(rsColumn);
                                break;
                            }
                        }
                        break;
                    }
                    case DBConstant.COLUMN.NUMBER: {
                        switch (field.getClassType()) {
                            case DBConstant.CLASS.INTEGER:
                            case DBConstant.CLASS.LONG: {
                                object = new LongDealer(rsColumn);
                                break;
                            }
                            case DBConstant.CLASS.DOUBLE:
                            default: {
                                object = new DoubleDealer(rsColumn);
                            }
                        }
                        break;
                    }
                    case DBConstant.COLUMN.STRING:
                    default: {
                        if (needCharSetConvert) {
                            object = new StringDealerWithCharSet(rsColumn, originalCharSetName, newCharSetName);
                        } else {
                            object = new StringDealer(rsColumn);
                        }
                    }
                }
                res.add(object);
            }
        }
        return res.toArray(new DBDealer[res.size()]);
    }

    /**
     * 执行sql语句，获取数据
     *
     * @param traversal
     */
    @Override
    public int runSQL(SQLStatement sql, ICubeFieldSource[] columns, Traversal<BIDataValue> traversal, int row) {
        com.fr.data.impl.Connection connection = sql.getConn();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            long t = System.currentTimeMillis();
            conn = sql.getSqlConn();
            String originalCharSetName = connection.getOriginalCharsetName();
            String newCharSetName = connection.getNewCharsetName();
            boolean needCharSetConvert = StringUtils.isNotBlank(originalCharSetName)
                    && StringUtils.isNotBlank(newCharSetName);
            Dialect dialect = DialectFactory.generateDialect(conn, connection.getDriver());
            String sqlString = createSqlString(dialect, columns);
            sql.setSelect(sqlString);
            String query = dealWithSqlCharSet(sql.toString(), connection);
            BILogger.getLogger().info("Start Query sql:" + query);
            stmt = createStatement(conn, dialect);
            try {
                rs = stmt.executeQuery(query);
            } catch (Exception e) {
                DBUtils.closeStatement(stmt);
                sql.setSelect("");
                query = dealWithSqlCharSet(sql.toString(), connection);
                stmt = createStatement(conn, dialect);
                rs = stmt.executeQuery(query);
                BILogger.getLogger().error("sql: " + sql.toString() + " execute failed!");
            }
            BILogger.getLogger().info("sql: " + sql.toString() + " execute cost:" + DateUtils.timeCostFrom(t));
            row = dealWithResultSet(rs, columns, traversal, needCharSetConvert, originalCharSetName, newCharSetName, row);
        } catch (Throwable e) {
            BILogger.getLogger().error("sql: " + sql.toString() + " execute failed!");
            throw new RuntimeException(e);
        } finally {
            DBUtils.closeResultSet(rs);
            DBUtils.closeStatement(stmt);
            DBUtils.closeConnection(conn);
        }
        return row;
    }

    private String createSqlString(Dialect dialect, ICubeFieldSource[] columns) {
        StringBuffer sb = new StringBuffer();
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].isUsable()) {
                list.add(dialect.column2SQL(columns[i].getFieldName()));
            }
        }
        for (int i = 0; i < list.size(); i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    private String dealWithSqlCharSet(String sql, com.fr.data.impl.Connection database) {
        if (StringUtils.isNotBlank(database.getOriginalCharsetName()) && StringUtils.isNotBlank(database.getNewCharsetName())) {
            try {
                return new String(sql.getBytes(database.getNewCharsetName()), database.getOriginalCharsetName());
            } catch (UnsupportedEncodingException e) {
                FRContext.getLogger().error(e.getMessage(), e);
            }
        }
        return sql;
    }

    public abstract Statement createStatement(Connection conn, Dialect dialect) throws SQLException;


}
