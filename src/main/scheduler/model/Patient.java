package scheduler.model;

public class Patient extends User {
    public static String addPatient = "INSERT INTO Patients VALUES (? , ?, ?)";
    public static String getPatient = "SELECT Salt, Hash FROM Patients WHERE Username = ?";

    private Patient(PatientBuilder builder) {
        super(builder);
    }

    private Patient(PatientGetter getter) {
        super(getter);
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
