package org.openmrs.module.mirebalaisreports.etl;

import java.sql.Connection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

/**
 * Methods for working with the ETL Status Table
 */
public class EtlStatusTable {

    public static final String TABLE_NAME = "etl_status";

    public static boolean checkTableExists(Connection c, String tableName) throws Exception {
        QueryRunner qr = new QueryRunner();
        StringBuilder query = new StringBuilder();
        query.append("select count(*) from information_schema.tables ");
        query.append("where table_type = 'BASE TABLE' ");
        query.append("and table_name = '" + tableName + "'");
        Integer num = (Integer)qr.query(c, query.toString(), new ScalarHandler());
        return num > 0;
    }

    public static void createStatusTable(Connection c) throws Exception {
        if (!checkTableExists(c, TABLE_NAME)) {
            StringBuilder sb = new StringBuilder();
            sb.append("create table " + TABLE_NAME + " (");
            sb.append("  uuid            CHAR(36) NOT NULL, ");
            sb.append("  num             INT NOT NULL, ");
            sb.append("  table_name      VARCHAR(100) NOT NULL, ");
            sb.append("  total_expected  INT, ");
            sb.append("  total_loaded    INT, ");
            sb.append("  started         DATETIME NOT NULL, ");
            sb.append("  completed       DATETIME, ");
            sb.append("  status          VARCHAR(1000) NOT NULL,");
            sb.append("  error_message   VARCHAR(1000)");
            sb.append(")");
            executeUpdate(c, sb.toString());
        }
    }

    public static String insertStatus(Connection c, String uuid, String tableName) throws Exception {
        executeUpdate(c, "update " + TABLE_NAME + " set num = num+1 where table_name = ?", new Object[] {tableName});
        String stmt = "insert into " + TABLE_NAME + " (uuid, num, table_name, started, status) values (?,?,?,?,?)";
        executeUpdate(c, stmt, new Object[] { uuid, 1, tableName, new Date(), "Refresh initiated" });
        return uuid;
    }

    public static void updateTotalCount(Connection c, String uuid, Integer totalCount) throws Exception {
        String stmt = "update " + TABLE_NAME + " set total_expected = ? where uuid = ?";
        executeUpdate(c, stmt, new Object[] { totalCount, uuid });
    }

    public static void updateCurrentCount(Connection c, String uuid) throws Exception {
        String tableName = getTableForUuid(c, uuid);
        Integer num = getCurrentCountInTable(c, tableName);
        String stmt = "update " + TABLE_NAME + " set total_loaded = ? where uuid = ?";
        executeUpdate(c, stmt, new Object[] { num, uuid });
    }

    public static void updateStatus(Connection c, String uuid, String message) throws Exception {
        String stmt = "update " + TABLE_NAME + " set status = ? where uuid = ?";
        executeUpdate(c, stmt, new Object[] { message, uuid });
    }

    public static void updateStatusSuccess(Connection c, String uuid) throws Exception {
        String stmt = "update " + TABLE_NAME + " set status = ?, completed = ? where uuid = ?";
        executeUpdate(c, stmt, new Object[] { "Import Completed Sucessfully", new Date(), uuid });
    }

    public static void updateStatusError(Connection c, String uuid, Exception e) throws Exception {
        e.printStackTrace();
        String stmt = "update " + TABLE_NAME + " set status = ?, completed = ?, error_message = ? where uuid = ?";
        executeUpdate(c, stmt, new Object[] { "Import Failed", new Date(), e.getMessage(), uuid });
    }

    protected static void executeUpdate(Connection c, String stmt, Object...params) throws Exception {
        QueryRunner qr = new QueryRunner();
        qr.update(c, stmt, params);
    }

    public static Integer getCurrentCountInTable(Connection c, String tableName) throws Exception {
        QueryRunner qr = new QueryRunner();
        Integer num = (Integer)qr.query(c, "select count(*) from " + tableName, new ScalarHandler());
        return num;
    }

    public static String getTableForUuid(Connection c, String uuid) throws Exception {
        QueryRunner qr = new QueryRunner();
        String sql = "select table_name from " + TABLE_NAME + " where uuid = ?";
        return qr.query(c, sql, new ScalarHandler<String>(), uuid);
    }

    public static Map<String, EtlStatus> getLatestEtlStatuses(Connection c) throws Exception {
        Map<String, EtlStatus> ret = new LinkedHashMap<String, EtlStatus>();
        StringBuilder sql = new StringBuilder();
        sql.append("select uuid, num, table_name, started, completed, total_expected, total_loaded, status, error_message ");
        sql.append("from " + TABLE_NAME + " ");
        sql.append("where table_name = ? and num = 1 ");
        QueryRunner qr = new QueryRunner();
        List<Map<String, Object>> l = qr.query(c, sql.toString(), new MapListHandler());
        if (l != null) {
            for (Map<String, Object> m : l) {
                if (m != null) {
                    EtlStatus status = new EtlStatus();
                    status.setUuid((String) m.get("uuid"));
                    status.setNum((Integer) m.get("num"));
                    status.setTableName((String) m.get("table_name"));
                    status.setTotalExpected((Integer)m.get("total_expected"));
                    status.setTotalLoaded((Integer)m.get("total_loaded"));
                    status.setStarted((Date) m.get("started"));
                    status.setCompleted((Date) m.get("completed"));
                    status.setStatus((String) m.get("status"));
                    status.setErrorMessage((String) m.get("error_message"));
                    ret.put(status.getTableName(), status);
                }
            }
        }
        return ret;
    }
}
