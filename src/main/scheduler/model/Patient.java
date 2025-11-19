package scheduler.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Patient extends User {
    private static String addPatient = "INSERT INTO Patients VALUES (?)";
    public static String getPatient = "SELECT * FROM Patients WHERE Username = ?";

    private Patient(PatientBuilder builder) {
        super(builder);
    }

    private Patient(PatientGetter getter) {
        super(getter);
    }

    @Override
    protected void saveSubclassToDB(Connection con) throws SQLException {
        PreparedStatement statement = con.prepareStatement(addPatient);
        statement.setString(1, this.username);
        statement.executeUpdate();
    }

    public static class PatientBuilder extends UserBuilder<Patient> {
        public PatientBuilder(String username, byte[] salt, byte[] hash) {
            super(username, salt, hash);
        }

        @Override
        public Patient build() {
            return new Patient(this);
        }
    }

    public static class PatientGetter extends UserGetter<Patient> {
        public PatientGetter(String username, String password) {
            super(username, password);
        }

        @Override
        protected Patient createUser() {
            return new Patient(this);
        }
    }
}
