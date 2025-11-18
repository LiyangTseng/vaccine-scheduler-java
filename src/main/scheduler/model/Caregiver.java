package scheduler.model;

import scheduler.db.ConnectionManager;
import java.sql.*;

public class Caregiver extends User{
    public static String addCaregiver = "INSERT INTO Caregivers VALUES (? , ?, ?)";
    public static String getCaregiver = "SELECT Salt, Hash FROM Caregivers WHERE Username = ?";

    private Caregiver(CaregiverBuilder builder) {
        super(builder);
    }

    private Caregiver(CaregiverGetter getter) {
        super(getter);
    }

    public void uploadAvailability(Date d) throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String addAvailability = "INSERT INTO Availabilities VALUES (? , ?)";
        try {
            PreparedStatement statement = con.prepareStatement(addAvailability);
            statement.setDate(1, d);
            statement.setString(2, this.username);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    public static class CaregiverBuilder extends UserBuilder<Caregiver> {
        public CaregiverBuilder(String username, byte[] salt, byte[] hash) {
            super(username, salt, hash);
        }

        @Override
        public Caregiver build() {
            return new Caregiver(this);
        }
    }

    public static class CaregiverGetter extends UserGetter<Caregiver> {

        public CaregiverGetter(String username, String password) {
            super(username, password);
        }

        @Override
        protected Caregiver createUser() {
            return new Caregiver(this);
        }
    }
}
