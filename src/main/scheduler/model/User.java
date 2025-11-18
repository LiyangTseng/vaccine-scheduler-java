package scheduler.model;

import scheduler.db.ConnectionManager;
import scheduler.util.Util;

import java.sql.*;
import java.util.Arrays;

public abstract class User {
    protected final String username;
    protected final byte[] salt;
    protected final byte[] hash;

    protected User(UserBuilder<?> builder) {
        this.username = builder.username;
        this.salt = builder.salt;
        this.hash = builder.hash;
    }

    protected User(UserGetter getter) {
        this.username = getter.username;
        this.salt = getter.salt;
        this.hash = getter.hash;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getHash() {
        return hash;
    }

    public void saveToDB(String insertQuery) throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        try {
            PreparedStatement statement = con.prepareStatement(insertQuery);
            statement.setString(1, this.username);
            statement.setBytes(2, this.salt);
            statement.setBytes(3, this.hash);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    public abstract static class UserBuilder<T extends User> {
        private final String username;
        private final byte[] salt;
        private final byte[] hash;

        public UserBuilder(String username, byte[] salt, byte[] hash) {
            this.username = username;
            this.salt = salt;
            this.hash = hash;
        }

        public abstract T build();
    }

    public abstract static class UserGetter<T extends User> {
        private final String username;
        private final String password;
        private byte[] salt;
        private byte[] hash;

        public UserGetter(String username, String password) {
            this.username = username;
            this.password = password;
        }

        // Subclasses implement this to specify what object to return
        protected abstract T createUser();

        public T get(String selectQuery) throws SQLException {
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();

            try {
                PreparedStatement statement = con.prepareStatement(selectQuery);
                statement.setString(1, this.username);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    byte[] salt = resultSet.getBytes("Salt");
                    // we need to call Util.trim() to get rid of the paddings,
                    // try to remove the use of Util.trim() and you'll see :)
                    byte[] hash = Util.trim(resultSet.getBytes("Hash"));
                    // check if the password matches
                    byte[] calculatedHash = Util.generateHash(password, salt);
                    if (!Arrays.equals(hash, calculatedHash)) {
                        return null;
                    } else {
                        this.salt = salt;
                        this.hash = hash;
                        cm.closeConnection();
                        return createUser();
                    }
                }
                return null;
            } catch (SQLException e) {
                throw new SQLException();
            } finally {
                cm.closeConnection();
            }
        }
    }

}
