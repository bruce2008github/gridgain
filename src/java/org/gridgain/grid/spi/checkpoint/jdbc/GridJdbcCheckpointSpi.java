// Copyright (C) GridGain Systems Licensed under GPLv3, http://www.gnu.org/licenses/gpl.html

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.spi.checkpoint.jdbc;

import org.gridgain.grid.logger.*;
import org.gridgain.grid.resources.*;
import org.gridgain.grid.spi.*;
import org.gridgain.grid.spi.checkpoint.*;
import org.gridgain.grid.typedef.*;
import org.gridgain.grid.typedef.internal.*;
import javax.sql.*;
import java.sql.*;
import java.text.*;

/**
 * This class defines JDBC checkpoint SPI implementation. All checkpoints are
 * stored in the database table and available from all nodes in the grid. Note that every
 * node must have access to the database. The reason of having it is because a job state
 * can be saved on one node and loaded on another (e.g., if a job gets
 * preempted on a different node after node failure).
 * <h1 class="header">Configuration</h1>
 * <h2 class="header">Mandatory</h2>
 * This SPI has following mandatory configuration parameters.
 * <ul>
 * <li>DataSource (see {@link #setDataSource(DataSource)}).</li>
 * </ul>
 * <h2 class="header">Optional</h2>
 * This SPI has following optional configuration parameters:
 * <ul>
 * <li>Checkpoint table name (see {@link #setCheckpointTableName(String)}).</li>
 * <li>Checkpoint key field name (see {@link #setKeyFieldName(String)}). </li>
 * <li>Checkpoint key field type (see {@link #setKeyFieldType(String)}). </li>
 * <li>Checkpoint value field name (see {@link #setValueFieldName(String)}).</li>
 * <li>Checkpoint value field type (see {@link #setValueFieldType(String)}).</li>
 * <li>Checkpoint expiration date field name (see {@link #setExpireDateFieldName(String)}).</li>
 * <li>Checkpoint expiration date field type (see {@link #setExpireDateFieldType(String)}).</li>
 * <li>Number of retries in case of any failure (see {@link #setNumberOfRetries(int)}).</li>
 * <li>User name (see {@link #setUser(String)}).</li>
 * <li>Password (see {@link #setPassword(String)}).</li>
 * </ul>
 * <h2 class="header">Apache DBCP</h2>
 * <a href="http://commons.apache.org/dbcp/">Apache DBCP</a> project provides various wrappers
 * for data sources and connection pools. You can use these wrappers as Spring beans to configure
 * this SPI from Spring configuration file. Refer to {@code Apache DBCP} project for more information.
 * <p>
 * <h2 class="header">Java Example</h2>
 * {@link GridJdbcCheckpointSpi} can be configured as follows:
 * <pre name="code" class="java">
 * GridConfigurationAdapter cfg = new GridConfigurationAdapter();
 *
 * GridJdbcCheckpointSpi checkpointSpi = new GridJdbcCheckpointSpi();
 *
 * javax.sql.DataSource ds = ... // Set datasource.
 *
 * // Set jdbc checkpoint SPI parameters.
 * checkpointSpi.setDataSource(ds);
 * checkpointSpi.setUser("test");
 * checkpointSpi.setPassword("test");
 *
 * // Override default checkpoint SPI.
 * cfg.setCheckpointSpi(checkpointSpi);
 *
 * // Starts grid.
 * G.start(cfg);
 * </pre>
 *
 * <h2 class="header">Spring Example</h2>
 * {@link GridJdbcCheckpointSpi} can be configured from Spring XML configuration file:
 * <pre name="code" class="xml">
 * &lt;bean id="grid.custom.cfg" class="org.gridgain.grid.GridConfigurationAdapter" singleton="true"&gt;
 *     ...
 *     &lt;property name="checkpointSpi"&gt;
 *         &lt;bean class="org.gridgain.grid.spi.checkpoint.jdbc.GridJdbcCheckpointSpi"&gt;
 *             &lt;property name="dataSrc"&gt;&lt;ref bean="anyPooledDataSourceBean" /&gt;&lt;/property&gt;
 *             &lt;property name="checkpointTableName" value="GRID_CHECKPOINTS" /&gt;
 *             &lt;property name="user" value="test" /&gt;
 *             &lt;property name="password" value="test" /&gt;
 *         &lt;/bean&gt;
 *     &lt;/property&gt;
 *     ...
 * &lt;/bean&gt;
 * </pre>
 * <p>
 * <img src="http://www.gridgain.com/images/spring-small.png">
 * <br>
 * For information about Spring framework visit <a href="http://www.springframework.org/">www.springframework.org</a>
 *
 * @author 2012 Copyright (C) GridGain Systems
 * @version 4.0.0c.22032012
 */
@SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed", "JDBCExecuteWithNonConstantString"})
@GridSpiInfo(
    author = "GridGain Systems",
    url = "www.gridgain.com",
    email = "support@gridgain.com",
    version = "4.0.0c.22032012")
@GridSpiMultipleInstancesSupport(true)
public class GridJdbcCheckpointSpi extends GridSpiAdapter implements GridCheckpointSpi, GridJdbcCheckpointSpiMBean {
    /** Default number of retries in case of errors (value is {@code 2}). */
    public static final int DFLT_NUMBER_OF_RETRIES = 2;

    /** Default expiration date field type (value is {@code DATETIME}). */
    public static final String DFLT_EXPIRE_DATE_FIELD_TYPE = "DATETIME";

    /** Default expiration date field name (value is {@code EXPIRE_DATE}). */
    public static final String DFLT_EXPIRE_DATE_FIELD_NAME = "EXPIRE_DATE";

    /** Default checkpoint value field type (value is {@code BLOB}). */
    public static final String DFLT_VALUE_FIELD_TYPE = "BLOB";

    /** Default checkpoint value field name (value is {@code VALUE}). */
    public static final String DFLT_VALUE_FIELD_NAME = "VALUE";

    /** Default checkpoint key field type (value is {@code VARCHAR(256)}). */
    public static final String DFLT_KEY_FIELD_TYPE = "VARCHAR(256)";

    /** Default checkpoint key field name (value is {@code NAME}). */
    public static final String DFLT_KEY_FIELD_NAME = "NAME";

    /** Default checkpoint table name (value is {@code CHECKPOINTS}). */
    public static final String DFLT_CHECKPOINT_TABLE_NAME = "CHECKPOINTS";

    /** Non-expirable timeout. */
    private static final long NON_EXPIRABLE_TIMEOUT = 0;

    /**
     * Template arguments:
     * {@code 0} - checkpoint table name
     * {@code 1} - key field name
     * {@code 2} - key field type
     * {@code 3} - value field name
     * {@code 4} - value field type
     * {@code 5} - create date field name
     * {@code 6} - create date field type
     */
    private static final String CREATE_TABLE_SQL = "CREATE TABLE {0} ({1} {2} PRIMARY KEY, {3} {4} , {5} {6} NULL)";

    /** */
    private static final String CHECK_TABLE_EXISTS_SQL = "SELECT 0 FROM {0} WHERE 0 <> 0";

    /** */
    private static final String CHECK_EXISTS_SQL = "SELECT 0 FROM {0} WHERE {1} = ?";

    /** */
    private static final String UPDATE_SQL = "UPDATE {0} SET {1} = ?, {2} = ? WHERE {3} = ?";

    /** */
    private static final String INSERT_SQL = "INSERT INTO {0} ({1}, {2}, {3}) VALUES (?, ?, ?)";

    /** */
    private static final String DELETE_SQL = "DELETE FROM {0} WHERE {1} = ?";

    /** */
    private static final String SELECT_SQL = "SELECT {0} FROM {1} WHERE {2} = ? AND ({3} IS NULL OR {3} > ?)";

    /** */
    private static final String SELECT_EXPIRED_SQL = "SELECT {0} FROM {1} WHERE {2} IS NOT NULL AND {2} <= ?";

    /** */
    private static final String DELETE_EXPIRED_SQL = "DELETE FROM {0} WHERE {1} IS NOT NULL AND {1} <= ?";

    /** */
    @GridLoggerResource
    private GridLogger log;

    /** */
    private DataSource dataSrc;

    /** */
    private String user;

    /** */
    private String password;

    /** */
    private int retryNum = DFLT_NUMBER_OF_RETRIES;

    /** */
    private String tblName = DFLT_CHECKPOINT_TABLE_NAME;

    /** */
    private String keyName = DFLT_KEY_FIELD_NAME;

    /** */
    private String keyType = DFLT_KEY_FIELD_TYPE;

    /** */
    private String valName = DFLT_VALUE_FIELD_NAME;

    /** */
    private String valType = DFLT_VALUE_FIELD_TYPE;

    /** */
    private String expDateName = DFLT_EXPIRE_DATE_FIELD_NAME;

    /** */
    private String expDateType = DFLT_EXPIRE_DATE_FIELD_TYPE;

    /** */
    private String crtTblSql;

    /** */
    private String chkTblExistsSql;

    /** */
    private String chkExistsSql;

    /** */
    private String updateSql;

    /** */
    private String insSql;

    /** */
    private String delSql;

    /** */
    private String selSql;

    /** */
    private String delExpSql;

    /** */
    private String selExpSql;

    /** Listener. */
    private GridCheckpointListener lsnr;

    /** {@inheritDoc} */
    @Override public int getNumberOfRetries() {
        return retryNum;
    }

    /** {@inheritDoc} */
    @Override public String getDataSourceInfo() {
        return dataSrc.toString();
    }

    /** {@inheritDoc} */
    @Override public String getUser() {
        return user;
    }

    /** {@inheritDoc} */
    @Override public String getPassword() {
        return password;
    }

    /** {@inheritDoc} */
    @Override public String getCheckpointTableName() {
        return tblName;
    }

    /** {@inheritDoc} */
    @Override public String getKeyFieldName() {
        return keyName;
    }

    /** {@inheritDoc} */
    @Override public String getKeyFieldType() {
        return keyType;
    }

    /** {@inheritDoc} */
    @Override public String getValueFieldName() {
        return valName;
    }

    /** {@inheritDoc} */
    @Override public String getValueFieldType() {
        return valType;
    }

    /** {@inheritDoc} */
    @Override public String getExpireDateFieldName() {
        return expDateName;
    }

    /** {@inheritDoc} */
    @Override public String getExpireDateFieldType() {
        return expDateType;
    }

    /**
     * Sets DataSource to use for database access. This parameter is mandatory and must be
     * provided for this SPI to be able to start.
     * <p>
     * <a href="http://commons.apache.org/dbcp/">Apache DBCP</a> project provides various wrappers
     * for data sources and connection pools. You can use these wrappers as Spring beans to configure
     * this SPI from Spring configuration file. Refer to {@code Apache DBCP} project for more information.
     *
     * @param dataSrc DataSource object to set.
     */
    @GridSpiConfiguration(optional = false)
    public void setDataSource(DataSource dataSrc) {
        this.dataSrc = dataSrc;
    }

    /**
     * Sets number of retries in case of any database errors. By default
     * the value is {@link #DFLT_NUMBER_OF_RETRIES}.
     *
     * @param retryNum Number of retries in case of any database errors.
     */
    @GridSpiConfiguration(optional = true)
    public void setNumberOfRetries(int retryNum) {
        this.retryNum = retryNum;
    }

    /**
     * Sets checkpoint database user name. Note that authentication will be
     * performed only if both, {@code user} and {@code password} are set.
     *
     * @param user Checkpoint database user name to set.
     * @see #setPassword(String)
     */
    @GridSpiConfiguration(optional = true)
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Sets checkpoint database password. Note that authentication will be
     * performed only if both, {@code user} and {@code password} are set.
     *
     * @param password Checkpoint database password to set.
     * @see #setUser(String)
     */
    @GridSpiConfiguration(optional = true)
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets checkpoint table name. By default {@link #DFLT_CHECKPOINT_TABLE_NAME} is used.
     *
     * @param tblName Checkpoint table name to set.
     */
    @GridSpiConfiguration(optional = true)
    public void setCheckpointTableName(String tblName) {
        this.tblName = tblName;
    }

    /**
     * Sets checkpoint key field name. By default,
     * {@link #DFLT_KEY_FIELD_NAME} is used. Note that you may also want to
     * change key field type (see {@link #setKeyFieldType(String)}).
     *
     * @param keyName Checkpoint key field name to set.
     */
    @GridSpiConfiguration(optional = true)
    public void setKeyFieldName(String keyName) {
        this.keyName = keyName;
    }

    /**
     * Sets checkpoint key field type. The field should have
     * corresponding SQL string type ({@code VARCHAR}, for example).
     * By default {@link #DFLT_EXPIRE_DATE_FIELD_TYPE} is used.
     *
     * @param keyType Checkpoint key field type to set.
     */
    @GridSpiConfiguration(optional = true)
    public void setKeyFieldType(String keyType) {
        this.keyType = keyType;
    }

    /**
     * Sets checkpoint value field name. By default {@link #DFLT_VALUE_FIELD_NAME}
     * is used. Note that you may also want to change the value type
     * (see {@link #setValueFieldType(String)}).
     *
     * @param valName Checkpoint value field name to set.
     */
    @GridSpiConfiguration(optional = true)
    public void setValueFieldName(String valName) {
        this.valName = valName;
    }

    /**
     * Sets checkpoint value field type. Note, that the field should have corresponding
     * SQL {@code BLOB} type, and the default value of {@link #DFLT_VALUE_FIELD_TYPE}, which is
     * {@code BLOB}, won't work for all databases. For example, if using {@code HSQL DB},
     * then the type should be {@code longvarbinary}.
     *
     * @param valType Checkpoint value field type to set.
     */
    @GridSpiConfiguration(optional = true)
    public void setValueFieldType(String valType) {
        this.valType = valType;
    }

    /**
     * Sets checkpoint expiration date field name. By default
     * {@link #DFLT_EXPIRE_DATE_FIELD_NAME} is used. Note that you may also
     * want to change the expiration date field type
     * (see {@link #setExpireDateFieldType(String)}).
     *
     * @param expDateName Checkpoint expiration date field name to set.
     */
    @GridSpiConfiguration(optional = true)
    public void setExpireDateFieldName(String expDateName) {
        this.expDateName = expDateName;
    }

    /**
     * Sets checkpoint expiration date field type. By default
     * {@link #DFLT_EXPIRE_DATE_FIELD_TYPE} is used. The field should have
     * corresponding SQL {@code DATETIME} type.
     *
     * @param expDateType Checkpoint expiration date field type to set.
     */
    @GridSpiConfiguration(optional = true)
    public void setExpireDateFieldType(String expDateType) {
        this.expDateType = expDateType;
    }

    /**
     *
     * @param pattern Message pattern.
     * @param s Array of pattern parameters.
     * @return Formatted message.
     */
    private String sql(String pattern, Object... s) {
        return MessageFormat.format(pattern, s);
    }

    /** {@inheritDoc} */
    @Override public void spiStart(String gridName) throws GridSpiException {
        // Start SPI start stopwatch.
        startStopwatch();

        assertParameter(dataSrc != null, "dataSrc != null");
        assertParameter(!F.isEmpty(tblName), "!F.isEmpty(tblName)");
        assertParameter(!F.isEmpty(keyName), "!F.isEmpty(keyName)");
        assertParameter(!F.isEmpty(keyType), "!F.isEmpty(keyType)");
        assertParameter(!F.isEmpty(valName), "!F.isEmpty(valName)");
        assertParameter(!F.isEmpty(valType), "!F.isEmpty(valType)");
        assertParameter(!F.isEmpty(expDateName), "!F.isEmpty(expDateName)");
        assertParameter(!F.isEmpty(expDateType), "!F.isEmpty(expDateType)");

        // Fill SQL template strings.
        crtTblSql = sql(CREATE_TABLE_SQL, tblName, keyName, keyType, valName, valType, expDateName, expDateType);
        chkTblExistsSql = sql(CHECK_TABLE_EXISTS_SQL, tblName);
        chkExistsSql = sql(CHECK_EXISTS_SQL, tblName, keyName);
        updateSql = sql(UPDATE_SQL, tblName, valName, expDateName, keyName);
        insSql = sql(INSERT_SQL, tblName, keyName, valName, expDateName);
        delSql = sql(DELETE_SQL, tblName, keyName, expDateName);
        selSql = sql(SELECT_SQL, valName, tblName, keyName, expDateName);
        delExpSql = sql(DELETE_EXPIRED_SQL, tblName, expDateName);
        selExpSql = sql(SELECT_EXPIRED_SQL, keyName, tblName, expDateName);

        Connection conn = null;

        try {
            conn = getConnection();

            // Check checkpoint table exists.
            int errCnt = 0;

            while (true) {
                try {
                    if (!isCheckpointTableExists(conn)) {
                        createCheckpointTable(conn);
                    }

                    conn.commit();

                    break;
                }
                catch (SQLException e) {
                    U.rollbackConnection(conn, log);

                    if(++errCnt >= retryNum) {
                        throw new GridSpiException("Failed to create checkpoint table: " + tblName, e);
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("Failed to create checkpoint table as it may already exist (will try again): " +
                          tblName);
                    }
                }
            }
        }
        catch (SQLException e) {
            throw new GridSpiException("Failed to start jdbc checkpoint SPI: " + tblName, e);
        }
        finally {
            U.close(conn, log);
        }

        // Ack ok start.
        if (log.isDebugEnabled()) {
            log.debug(startInfo());
        }
    }

    /** {@inheritDoc} */
    @Override public void spiStop() throws GridSpiException {
        // Ack ok stop.
        if (log.isDebugEnabled()) {
            log.debug(stopInfo());
        }
    }

    /** {@inheritDoc} */
    @Override protected void onContextDestroyed0() {
        if (dataSrc != null) {
            Connection conn = null;

            try {
                conn = getConnection();

                removeExpiredCheckpoints(conn);

                conn.commit();
            }
            catch (SQLException e) {
                U.rollbackConnection(conn, log);

                U.error(log, "Failed to remove expired checkpoints from: " + tblName, e);
                //throw new GridSpiException("Failed to remove expired checkpoints from: " + tblName, e);
            }
            finally {
                U.close(conn, log);
            }
        }
    }

    /** {@inheritDoc} */
    @Override public byte[] loadCheckpoint(String key) throws GridSpiException {
        Connection conn = null;

        PreparedStatement st = null;

        ResultSet rs = null;

        try {
            conn = getConnection();

            st = conn.prepareStatement(selSql);

            st.setString(1, key);
            st.setTime(2, new Time(System.currentTimeMillis()));

            rs = st.executeQuery();

            return rs.next() ? rs.getBytes(1) : null;
        }
        catch (SQLException e) {
            throw new GridSpiException("Failed to load checkpoint [tblName=" + tblName + ", key=" + key + ']', e);
        }
        finally {
            U.close(rs, log);
            U.close(st, log);
            U.close(conn, log);
        }
    }

    /** {@inheritDoc} */
    @Override public boolean removeCheckpoint(String key) {
        Connection conn = null;

        PreparedStatement st = null;

        boolean removed = false;

        try {
            conn = getConnection();

            st = conn.prepareStatement(delSql);

            st.setString(1, key);

            if (st.executeUpdate() > 0) {
                removed = true;

                GridCheckpointListener tmp = lsnr;

                if (tmp != null) {
                    tmp.onCheckpointRemoved(key);
                }
            }

            conn.commit();
        }
        catch (SQLException e) {
            U.rollbackConnection(conn, log);

            U.error(log, "Failed to remove checkpoint [tblName=" + tblName + ", key=" + key + ']', e);

            return false;
        }
        finally {
            U.close(st, log);
            U.close(conn, log);
        }

        return removed;
    }

    /** {@inheritDoc} */
    @Override public boolean saveCheckpoint(String key, byte[] state, long timeout, boolean overwrite)
        throws GridSpiException {
        Time expTime = null;

        if (timeout != NON_EXPIRABLE_TIMEOUT) {
            expTime = new Time(System.currentTimeMillis() + timeout);
        }

        Connection conn = null;

        try {
            conn = getConnection();

            int errCnt = 0;

            while (true) {
                if (errCnt >= retryNum) {
                    throw new GridSpiException("Failed to save checkpoint after pre-configured number of " +
                        "retries [tblName=" + tblName + ", key=" + key + ", retryNum=" + retryNum + ']');
                }

                try {
                    if (!isCheckpointExists(conn, key)) {
                        if (createCheckpoint(conn, key, state, expTime) == 0) {
                            ++errCnt;

                            U.warn(log, "Failed to create checkpoint (will try again) [tblName=" +
                                tblName + ", key=" + key + ']');

                            continue;
                        }
                    }
                    else {
                        if (!overwrite) {
                            return false;
                        }
                        if (updateCheckpoint(conn, key, state, expTime) == 0) {
                            ++errCnt;

                            U.warn(log, "Failed to update checkpoint as it may be deleted (will try create) [" +
                                "tblName=" + tblName + ", key=" + key + ']');

                            continue;
                        }
                    }

                    conn.commit();

                    return true;
                }
                catch (SQLException e) {
                    U.rollbackConnection(conn, log);

                    if(++errCnt >= retryNum) {
                        throw new GridSpiException("Failed to save checkpoint [tblName=" + tblName + ", key=" + key +
                            ']', e);
                    }

                    U.warn(log, "Failed to save checkpoint (will try again) [tblName=" + tblName + ", key=" +
                        key + ']');
                }
            }
        }
        catch (SQLException e) {
            throw new GridSpiException("Failed to save checkpoint [tblName=" + tblName +", key=" + key + ']', e);
        }
        finally {
            U.close(conn, log);
        }
    }

    /**
     * Checks specified checkpoint existing.
     *
     * @param conn Active jdbc connection.
     * @param key Checkpoint key.
     * @return {@code true} if specified checkpoint exists in the checkpoint table.
     * @throws SQLException Thrown in case of any errors.
     */
    private boolean isCheckpointExists(Connection conn, String key) throws SQLException {
        PreparedStatement st = null;

        ResultSet rs = null;

        try {
            st = conn.prepareStatement(chkExistsSql);

            st.setString(1, key);

            rs = st.executeQuery();

            return rs.next();
        }
        finally {
            U.close(rs, log);
            U.close(st, log);
        }
    }

    /**
     * Creates checkpoint.
     *
     * @param conn Active database connection.
     * @param key Checkpoint key.
     * @param state Checkpoint data.
     * @param expTime Checkpoint expire time.
     * @return Number of rows affected by query.
     * @throws SQLException Thrown in case of any errors.
     */
    private int createCheckpoint(Connection conn, String key, byte[] state, Time expTime) throws SQLException {
        PreparedStatement st = null;

        try {
            st = conn.prepareStatement(insSql);

            st.setString(1, key);
            st.setBytes(2, state);
            st.setTime(3, expTime);

            return st.executeUpdate();
        }
        finally {
            U.close(st, log);
        }
    }

    /**
     * Updates checkpoint data.
     *
     * @param conn Active database connection.
     * @param key Checkpoint key.
     * @param state Checkpoint data.
     * @param expTime Checkpoint expire time.
     * @return Number of rows affected by query.
     * @throws SQLException Thrown in case of any errors.
     */
    private int updateCheckpoint(Connection conn, String key, byte[] state, Time expTime) throws SQLException {
        PreparedStatement st = null;

        try {
            st = conn.prepareStatement(updateSql);

            st.setBytes(1, state);
            st.setTime(2, expTime);
            st.setString(3, key);

            return st.executeUpdate();
        }
        finally {
            U.close(st, log);
        }
    }

    /**
     * Get connection from the data source.
     *
     * @return JDBC connection.
     * @throws SQLException Thrown in case of any errors.
     */
    private Connection getConnection() throws SQLException {
        Connection conn = user != null && password != null ?
            dataSrc.getConnection(user, password) :
            dataSrc.getConnection();

        conn.setAutoCommit(false);

        return conn;
    }

    /**
     * This method accomplishes RDBMS-independent table exists check.
     *
     * @param conn Active database connection.
     * @return {@code true} if specified table exists, {@code false} otherwise.
     */
    private boolean isCheckpointTableExists(Connection conn) {
        Statement st = null;

        ResultSet rs = null;

        try {
            st = conn.createStatement();

            rs = st.executeQuery(chkTblExistsSql);

            return true; // if table does exist, no rows will ever be returned
        }
        catch (SQLException ignored) {
            return false; // if table does not exist, an exception will be thrown
        }
        finally {
            U.close(rs, log);
            U.close(st, log);
        }
    }

    /**
     * Creates checkpoint table.
     *
     * @param conn Active database connection.
     * @throws SQLException Thrown in case of any errors.
     */
    private void createCheckpointTable(Connection conn) throws SQLException {
        Statement st = null;

        try {
            st = conn.createStatement();

            st.executeUpdate(crtTblSql);

            if (log.isDebugEnabled()) {
                log.debug("Successfully created checkpoint table: " + tblName);
            }
        }
        finally {
            U.close(st, log);
        }
    }

    /**
     * Removes expired checkpoints from the checkpoint table.
     *
     * @param conn Active database connection.
     * @return Number of removed expired checkpoints.
     * @throws SQLException Thrown in case of any errors.
     */
    private int removeExpiredCheckpoints(Connection conn) throws SQLException {
        int delCnt = 0;

        PreparedStatement selSt = null;
        PreparedStatement delSt = null;

        ResultSet rs = null;

        Time time = new Time(System.currentTimeMillis());

        GridCheckpointListener tmp = lsnr;

        try {
            if (tmp != null) {
                selSt = conn.prepareStatement(selExpSql);

                selSt.setTime(1, time);

                rs = selSt.executeQuery();

                while (rs.next()) {
                    tmp.onCheckpointRemoved(rs.getString(1));
                }
            }

            delSt = conn.prepareStatement(delExpSql);

            delSt.setTime(1, time);

            delCnt = delSt.executeUpdate();
        }
        finally {
            U.close(rs, log);
            U.close(selSt, log);
            U.close(delSt, log);
        }

        if (log.isDebugEnabled()) {
            log.debug("Successfully removed expired checkpoints from: " + tblName );
        }

        return delCnt;
    }

    /** {@inheritDoc} */
    @Override public void setCheckpointListener(GridCheckpointListener lsnr) {
        this.lsnr = lsnr;
    }
}
