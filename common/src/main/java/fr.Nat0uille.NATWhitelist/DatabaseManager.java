package fr.Nat0uille.NATWhitelist;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {

    public enum DatabaseType {
        H2, MYSQL, MARIADB
    }

    private HikariDataSource dataSource;
    private DatabaseType type;

    public boolean connectH2(String filePath) {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:h2:" + filePath + ";DB_CLOSE_ON_EXIT=FALSE");
            config.setDriverClassName("org.h2.Driver");
            config.setMaximumPoolSize(10);
            config.setAutoCommit(true);

            this.dataSource = new HikariDataSource(config);
            this.type = DatabaseType.H2;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean connectMySQL(String host, int port, String database, String username, String password) {
        try {
            HikariConfig config = new HikariConfig();
            String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";

            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setMaximumPoolSize(10);

            this.dataSource = new HikariDataSource(config);
            this.type = DatabaseType.MYSQL;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean connectMariaDB(String host, int port, String database, String username, String password) {
        try {
            HikariConfig config = new HikariConfig();
            String jdbcUrl = "jdbc:mariadb://" + host + ":" + port + "/" + database + "?autoReconnect=true";

            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("org.mariadb.jdbc.Driver");
            config.setMaximumPoolSize(10);

            this.dataSource = new HikariDataSource(config);
            this.type = DatabaseType.MARIADB;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    public DatabaseType getType() {
        return type;
    }

    public Connection getConnection() throws SQLException {
        if (!isConnected()) {
            throw new SQLException("Non connecté");
        }
        return dataSource.getConnection();
    }

    public boolean execute(String sql) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insert(String table, Map<String, Object> data) {
        if (data == null || data.isEmpty()) return false;

        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!columns.isEmpty()) {
                columns.append(", ");
                values.append(", ");
            }
            columns.append(entry.getKey());
            values.append("?");
            params.add(entry.getValue());
        }

        String sql = "INSERT INTO " + table + " (" + columns + ") VALUES (" + values + ")";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int update(String table, Map<String, Object> data, String whereClause, Object... whereParams) {
        if (data == null || data.isEmpty()) return -1;

        StringBuilder setClause = new StringBuilder();
        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!setClause.isEmpty()) {
                setClause.append(", ");
            }
            setClause.append(entry.getKey()).append(" = ?");
            params.add(entry.getValue());
        }

        for (Object param : whereParams) {
            params.add(param);
        }

        String sql = "UPDATE " + table + " SET " + setClause;
        if (whereClause != null && !whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int delete(String table, String whereClause, Object... whereParams) {
        String sql = "DELETE FROM " + table;
        if (whereClause != null && !whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < whereParams.length; i++) {
                stmt.setObject(i + 1, whereParams[i]);
            }
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public List<Map<String, Object>> select(String table, String whereClause, Object... whereParams) {
        String sql = "SELECT * FROM " + table;
        if (whereClause != null && !whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }

        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < whereParams.length; i++) {
                stmt.setObject(i + 1, whereParams[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnName(i), rs.getObject(i));
                    }
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            try {
                // Pour H2, exécuter SHUTDOWN COMPACT FALSE pour éviter le compactage lors de la fermeture
                if (type == DatabaseType.H2) {
                    try (Connection conn = dataSource.getConnection();
                         Statement stmt = conn.createStatement()) {
                        stmt.execute("SHUTDOWN");
                    } catch (SQLException e) {
                        // Ignorer les erreurs de fermeture
                    }
                }
            } catch (Exception e) {
                // Ignorer les erreurs
            } finally {
                // Fermer le pool HikariCP
                dataSource.close();
            }
        }
    }
}
