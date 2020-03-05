package org.openmrs.module.mirebalaisreports.etl.job;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.microsoft.sqlserver.jdbc.SQLServerBulkCopy;
import com.microsoft.sqlserver.jdbc.SQLServerBulkCopyOptions;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mirebalaisreports.etl.EtlConnectionManager;
import org.openmrs.module.mirebalaisreports.etl.EtlStatusTable;
import org.openmrs.module.mirebalaisreports.etl.StatementParser;
import org.openmrs.module.mirebalaisreports.etl.config.ConfigLoader;
import org.openmrs.module.mirebalaisreports.etl.config.EtlDataSource;
import org.openmrs.module.mirebalaisreports.etl.config.EtlJobConfig;

/**
 * Job that can load into SQL Server table
 */
public class LoadSqlServerJob implements EtlJob {

    private static Log log = LogFactory.getLog(LoadSqlServerJob.class);
    private static boolean refreshInProgress = false;

    //***** CONSTRUCTORS *****

    public LoadSqlServerJob() {}

    /**
     * @see EtlJob
     */
    @Override
    public void execute(EtlJobConfig config) {
        if (!refreshInProgress) {
            refreshInProgress = true;
            try {
                // Pull options off of configuration
                String sds = config.getString("extract", "datasource");
                EtlDataSource sourceDatasource = ConfigLoader.getConfigurationFromFile(sds, EtlDataSource.class);
                String sourceQueryFile = config.getString("extract", "query");
                String sourceQuery = ConfigLoader.getFileContents(sourceQueryFile);
                String targetTable = config.getString("load", "table");
                String targetSchemaFile = config.getString("load", "schema");
                String targetSchema = ConfigLoader.getFileContents(targetSchemaFile);
                String tds = config.getString("load", "datasource");
                EtlDataSource targetDatasource = ConfigLoader.getConfigurationFromFile(tds, EtlDataSource.class);

                String uuid = UUID.randomUUID().toString();

                Connection sourceConnection = null;
                Connection targetConnection = null;

                try {
                    QueryRunner qr = new QueryRunner();
                    sourceConnection = EtlConnectionManager.openConnection(sourceDatasource);
                    targetConnection = EtlConnectionManager.openConnection(targetDatasource);

                    // If the target status table does not exist, create it here
                    EtlStatusTable.createStatusTable(targetConnection);

                    // Initialize the status table with this job execution
                    EtlStatusTable.insertStatus(targetConnection, uuid, targetTable);

                    boolean originalSourceAutoCommit = sourceConnection.getAutoCommit();
                    boolean originalTargetAutocommit = targetConnection.getAutoCommit();

                    try {
                        sourceConnection.setAutoCommit(false); // We intend to rollback changes to source after querying DB
                        targetConnection.setAutoCommit(true);  // We want to commit to target as we go, to query status

                        // First, drop any existing target table
                        EtlStatusTable.updateStatus(targetConnection, uuid, "Dropping existing table");
                        qr.update(targetConnection, "drop table if exists " + targetTable);

                        // Then, recreate the target table
                        EtlStatusTable.updateStatus(targetConnection, uuid, "Creating table");
                        qr.update(targetConnection, targetSchema);

                        // Now execute a bulk import
                        EtlStatusTable.updateStatus(targetConnection, uuid, "Executing import");

                        // Parse the source query into statements
                        List<String> stmts = StatementParser.parseSqlIntoStatements(sourceQuery, ";");
                        log.debug("Parsed extract query into " + stmts.size() + " statements");

                        // Iterate over each statement, and execute.  The final statement is expected to select the data out.
                        for (Iterator<String> sqlIterator = stmts.iterator(); sqlIterator.hasNext();) {
                            String sqlStatement = sqlIterator.next();
                            Statement statement = null;
                            try {
                                log.debug("Executing: " + sqlStatement);
                                StopWatch sw = new StopWatch();
                                sw.start();
                                statement = sourceConnection.createStatement();
                                statement.execute(sqlStatement);
                                log.debug("Statement executed");
                                if (!sqlIterator.hasNext()) {
                                    log.debug("This is the last statement, treat it as the extraction query");
                                    ResultSet resultSet = null;
                                    try {
                                        resultSet = statement.getResultSet();
                                        if (resultSet != null) {

                                            // Skip to the end to get the number of rows that ResultSet contains
                                            resultSet.last();
                                            Integer rowCount = resultSet.getRow();
                                            EtlStatusTable.updateTotalCount(targetConnection, uuid, rowCount);

                                            // Reset back to the beginning to ensure all rows are extracted
                                            resultSet.beforeFirst();

                                            // Pass the ResultSet to bulk copy to SQL Server (TODO: Handle other DBs)
                                            SQLServerBulkCopy bulkCopy = new SQLServerBulkCopy(targetConnection);
                                            SQLServerBulkCopyOptions bco = new SQLServerBulkCopyOptions();
                                            bco.setKeepIdentity(true);
                                            bco.setBatchSize(100);
                                            bco.setBulkCopyTimeout(3600);
                                            bulkCopy.setBulkCopyOptions(bco);
                                            bulkCopy.setDestinationTableName(targetTable);
                                            bulkCopy.writeToServer(resultSet);

                                            // Update the status at the end of the bulk copy
                                            EtlStatusTable.updateCurrentCount(targetConnection, uuid);
                                            EtlStatusTable.updateStatusSuccess(targetConnection, uuid);
                                        }
                                        else {
                                            throw new IllegalStateException("Invalid SQL extraction, no result set found");
                                        }
                                    }
                                    finally {
                                        DbUtils.closeQuietly(resultSet);
                                    }
                                }
                                sw.stop();
                                log.debug("Statement executed in: " + sw.toString());
                            }
                            finally {
                                DbUtils.closeQuietly(statement);
                            }
                        }
                    }
                    catch (Exception e) {
                        EtlStatusTable.updateStatusError(targetConnection, uuid, e);
                    }
                    finally {
                        sourceConnection.rollback();
                        sourceConnection.setAutoCommit(originalSourceAutoCommit);
                        targetConnection.setAutoCommit(originalTargetAutocommit);
                    }
                }
                finally {
                    DbUtils.closeQuietly(targetConnection);
                    DbUtils.closeQuietly(sourceConnection);
                }
            }
            catch (Exception e) {
                throw new IllegalStateException("An error occured during SQL Server Load task", e);
            }
            finally {
                refreshInProgress = false;
            }
        }
    }

}
