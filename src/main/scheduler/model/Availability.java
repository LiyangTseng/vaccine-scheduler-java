package scheduler.model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import scheduler.db.ConnectionManager;

public class Availability {
    public static String addAvailability = "INSERT INTO Availabilities VALUES (? , ?)";

    private final String caregiver;
    private final Date date;

    private Availability(AvailabilityBuilder builder) {
        this.caregiver = builder.caregiver;
        this.date = builder.date;
    }

    private Availability(AvailabilityGetter getter) {
        this.caregiver = getter.caregiver;
        this.date = getter.date;
    }

    // Getters
    public String getCaregiverName() {
        return this.caregiver;
    }

    public Date getDate() {
        return this.date;
    }

    public static class AvailabilityBuilder {
        private final String caregiver;
        private final Date date;

        public AvailabilityBuilder(String caregiver, Date date) {
            this.caregiver = caregiver;
            this.date = date;
        }

        public Availability build() throws SQLException {
            return new Availability(this);
        }
    }

    public void removeAvailability() throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String deleteAvailability = "DELETE FROM Availabilities WHERE Username = ? AND Time = ?";

        try {
            PreparedStatement statement = con.prepareStatement(deleteAvailability);
            statement.setString(1, this.caregiver);
            statement.setDate(2, this.date);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    public static class AvailabilityGetter {
        private final String caregiver;
        private final Date date;

        public AvailabilityGetter(String caregiver, Date date) {
            this.caregiver = caregiver;
            this.date = date;
        }

        public Availability get() throws SQLException{
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();

            String getAvailability = "SELECT * FROM Availabilities WHERE Username = ? AND Time = ?";
            try {
                PreparedStatement statement = con.prepareStatement(getAvailability);
                statement.setString(1, this.caregiver);
                statement.setDate(2, this.date);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    cm.closeConnection();
                    return new Availability(this);
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
