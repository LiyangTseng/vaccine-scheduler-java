package scheduler.model;

import java.util.ArrayList;
import java.util.List;

import scheduler.db.ConnectionManager;
import java.sql.*;

public class Caregiver extends User{
    private static String addCaregiver = "INSERT INTO Caregivers VALUES (?)";
    public static String getCaregiver = "SELECT * FROM Caregivers WHERE Username = ?";

    private Caregiver(CaregiverBuilder builder) {
        super(builder);
    }

    private Caregiver(CaregiverGetter getter) {
        super(getter);
    }

    public void uploadAvailability(Date d) throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        try {
            PreparedStatement statement = con.prepareStatement(Availability.addAvailability);
            statement.setDate(1, d);
            statement.setString(2, this.username);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    public static List<String> getAllCaregivers() throws SQLException {
        List<String> caregivers = new ArrayList<>();

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String getCaregivers = "SELECT * FROM Caregivers";

        try {
            PreparedStatement statement = con.prepareStatement(getCaregivers);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                caregivers.add(resultSet.getString("Username"));
            }
            return caregivers;
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    @Override
    protected void saveSubclassToDB(Connection con) throws SQLException {
        PreparedStatement statement = con.prepareStatement(addCaregiver);
        statement.setString(1, this.username);
        statement.executeUpdate();
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
