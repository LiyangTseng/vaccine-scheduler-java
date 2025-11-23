package scheduler.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Date;

import scheduler.db.ConnectionManager;

public class Reservation {
    private final Integer id;
    private final Date date;
    private final String caregiver;
    private String patient;
    private String vaccine;

    private Reservation(ReservationBuilder builder) {
        this.id = builder.id;
        this.date = builder.date;
        this.caregiver = builder.caregiver;
        this.patient = builder.patient;
        this.vaccine = builder.vaccine;
    }

    private Reservation(ReservationGetter getter) {
        this.id = getter.id;
        this.date = getter.date;
        this.caregiver = getter.caregiver;
        this.patient = getter.patient;
        this.vaccine = getter.vaccine;
    }

    // Getters
    public Integer getId() {
        return this.id;
    }

    public Date getDate() {
        return this.date;
    }

    public String getCaregiver() {
        return this.caregiver;
    }

    public String getPatient() {
        return this.patient;
    }

    public String getVaccine() {
        return this.vaccine;
    }

    public static List<Reservation> getAllReservationsByCaregiver(String caregiver) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String getAllReservations = "SELECT * FROM Reservations WHERE CaregiverName = ?";

        try {
            PreparedStatement statement = con.prepareStatement(getAllReservations);
            statement.setString(1, caregiver);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                reservations.add(
                        new Reservation.ReservationBuilder(
                            resultSet.getInt("Id"),
                            resultSet.getDate("Time"),
                            resultSet.getString("CaregiverName"),
                            resultSet.getString("PatientName"),
                            resultSet.getString("VaccineName")
                        ).build()
                );
            }
            return reservations;
        } catch (SQLException e) {
            throw e;
        } finally {
            cm.closeConnection();
        }
    }

    public static List<Reservation> getAllReservationsByPatient(String patient) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String getAllReservations = "SELECT * FROM Reservations WHERE PatientName = ?";

        try {
            PreparedStatement statement = con.prepareStatement(getAllReservations);
            statement.setString(1, patient);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                reservations.add(
                        new Reservation.ReservationBuilder(
                            resultSet.getInt("Id"),
                            resultSet.getDate("Time"),
                            resultSet.getString("CaregiverName"),
                            resultSet.getString("PatientName"),
                            resultSet.getString("VaccineName")
                        ).build()
                );
            }
            return reservations;
        } catch (SQLException e) {
            throw e;
        } finally {
            cm.closeConnection();
        }
    }

    public void saveToDB() throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String addReservation = "INSERT INTO Reservations VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = con.prepareStatement(addReservation);
            statement.setInt(1, this.id);
            statement.setDate(2, this.date);
            statement.setString(3, this.caregiver);
            statement.setString(4, this.patient);
            statement.setString(5, this.vaccine);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    public static class ReservationBuilder {
        private final Integer id;
        private final Date date;
        private final String caregiver;
        private String patient;
        private String vaccine;

        public ReservationBuilder(Integer id, Date date, String caregiver, String patient, String vaccine) {
            this.id = id;
            this.date = date;
            this.caregiver = caregiver;
            this.patient = patient;
            this.vaccine = vaccine;
        }

        public Reservation build() throws SQLException {
            return new Reservation(this);
        }
    }

    public static class ReservationGetter {
        private Integer id;
        private final Date date;
        private final String caregiver;
        private String patient;
        private String vaccine;

        public ReservationGetter(Date date, String caregiver) {
            this.date = date;
            this.caregiver = caregiver;
        }

        public Reservation get() throws SQLException {
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();

            String getReservation = "SELECT Id, Time, CaregiverName, PatientName, VaccineName FROM Reservations WHERE Time = ? AND CaregiverName = ?";
            try {
                PreparedStatement statement = con.prepareStatement(getReservation);
                statement.setString(1, this.date.toString());
                statement.setString(2, this.caregiver);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    this.id = resultSet.getInt("Id");
                    this.patient = resultSet.getString("PatientName");
                    this.vaccine = resultSet.getString("VaccineName");
                    cm.closeConnection();
                    return new Reservation(this);
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
