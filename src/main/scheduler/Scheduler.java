package scheduler;

import scheduler.db.ConnectionManager;
import scheduler.model.Availability;
import scheduler.model.Caregiver;
import scheduler.model.Patient;
import scheduler.model.Reservation;
import scheduler.model.User;
import scheduler.model.Vaccine;
import scheduler.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.sql.Date;

public class Scheduler {

    // objects to keep track of the currently logged-in user
    // Note: it is always true that at most one of currentCaregiver and currentPatient is not null
    //       since only one user can be logged-in at a time
    private static Caregiver currentCaregiver = null;
    private static Patient currentPatient = null;
    private static Integer appointment_id = 1; // start with 1

    public static void main(String[] args) {
        // printing greetings text
        System.out.println();
        System.out.println("Welcome to the COVID-19 Vaccine Reservation Scheduling Application!");
        System.out.println("*** Please enter one of the following commands ***");
        System.out.println("> create_patient <username> <password>");
        System.out.println("> create_caregiver <username> <password>");
        System.out.println("> login_patient <username> <password>");
        System.out.println("> login_caregiver <username> <password>");
        System.out.println("> search_caregiver_schedule <date>");
        System.out.println("> reserve <date> <vaccine>");
        System.out.println("> upload_availability <date>");
        System.out.println("> cancel <appointment_id>");  // TODO: implement cancel (extra credit)
        System.out.println("> add_doses <vaccine> <number>");
        System.out.println("> show_appointments");  // TODO: implement show_appointments (Part 2)
        System.out.println("> logout");
        System.out.println("> quit");
        System.out.println();

        // read input from user
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            String response = "";
            try {
                response = r.readLine();
            } catch (IOException e) {
                System.out.println("Please try again!");
            }
            // split the user input by spaces
            String[] tokens = response.split(" ");
            // check if input exists
            if (tokens.length == 0) {
                System.out.println("Please try again!");
                continue;
            }
            // determine which operation to perform
            String operation = tokens[0];
            if (operation.equals("create_patient")) {
                createPatient(tokens);
            } else if (operation.equals("create_caregiver")) {
                createCaregiver(tokens);
            } else if (operation.equals("login_patient")) {
                loginPatient(tokens);
            } else if (operation.equals("login_caregiver")) {
                loginCaregiver(tokens);
            } else if (operation.equals("search_caregiver_schedule")) {
                searchCaregiverSchedule(tokens);
            } else if (operation.equals("reserve")) {
                reserve(tokens);
            } else if (operation.equals("upload_availability")) {
                uploadAvailability(tokens);
            } else if (operation.equals("cancel")) {
                cancel(tokens);
            } else if (operation.equals("add_doses")) {
                addDoses(tokens);
            } else if (operation.equals("show_appointments")) {
                showAppointments(tokens);
            } else if (operation.equals("logout")) {
                logout(tokens);
            } else if (operation.equals("quit")) {
                System.out.println("Bye!");
                return;
            } else {
                System.out.println("Invalid operation name!");
            }
        }
    }

    private static void createPatient(String[] tokens) {
        // create_patient <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Failed to create patient: Require format `create_patient <username> <password>`.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        // check 2: check if the username has been taken already
        if (usernameExists(username, Patient.getPatient)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the patient
        try {
            Patient patient = new Patient.PatientBuilder(username, salt, hash).build();
            // save to patient information to our database
            patient.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create patient: " + e);
        }
    }

    private static void createCaregiver(String[] tokens) {
        // create_caregiver <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Failed to create caregiver: Require format `create_caregiver <username> <password>`");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        // check 2: check if the username has been taken already
        if (usernameExists(username, Caregiver.getCaregiver)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the caregiver
        try {
            Caregiver caregiver = new Caregiver.CaregiverBuilder(username, salt, hash).build();
            // save to caregiver information to our database
            caregiver.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create caregiver: " + e);
        }
    }

    private static boolean usernameExists(String username, String selectQuery) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        try {
            PreparedStatement statement = con.prepareStatement(selectQuery);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username \n" + e);
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void loginPatient(String[] tokens) {
        // login_patient <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login failed: Required format `login_patient <username> <password>`");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Patient patient = null;
        try {
            if (usernameExists(username, Patient.getPatient)) {
                patient = new Patient.PatientGetter(username, password).get(User.getUser);
            }
        } catch (SQLException e) {
            System.out.println("Login failed: " + e);
        }
        // check if the login was successful
        if (patient == null) {
            System.out.println("Login failed: patient not found.");
        } else {
            System.out.println("Logged in as: " + username);
            currentPatient = patient;
        }
    }

    private static void loginCaregiver(String[] tokens) {
        // login_caregiver <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login failed: Required format `login_caregiver <username> <password>`");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Caregiver caregiver = null;
        try {
            if (usernameExists(username, Caregiver.getCaregiver)) {
                caregiver = new Caregiver.CaregiverGetter(username, password).get(User.getUser);
            }
        } catch (SQLException e) {
            System.out.println("Login failed: " + e);
        }
        // check if the login was successful
        if (caregiver == null) {
            System.out.println("Login failed: caregiver not found");
        } else {
            System.out.println("Logged in as: " + username);
            currentCaregiver = caregiver;
        }
    }

    private static void searchCaregiverSchedule(String[] tokens) {
        // search_caregiver_schedule <date>
        // check 1: check if any user is logged in
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first");
            return;
        }

        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("search failed: Required format `search_caregiver_schedule <date>`");
            return;
        }

        Date date;
        try {
            date = Date.valueOf(tokens[1]);
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
            return;
        }

        // get all caregiver names
        List<String> caregivers;
        try {
            caregivers = Caregiver.getAllCaregivers();
        } catch (SQLException e) {
            System.out.println("Error occurred when retrieving all caregivers: " + e);
            return;
        }
        // query the availabilities of the existing caregivers
        System.out.println("Caregivers:");
        boolean found_available_caregivers = false;
        Availability availability = null;
        try {
            for (int i=0; i<caregivers.size(); ++i) {
                availability = new Availability.AvailabilityGetter(caregivers.get(i), date).get();
                if (availability != null) {
                    System.out.println(availability.getCaregiverName());
                    found_available_caregivers = true;
                }
            }
        } catch (Exception e) {
            System.out.println("Please try again");
        }
        if (!found_available_caregivers) {
            System.out.println("No caregivers available");
        }

        // get all vaccines
        List<String> vaccines;
        System.out.println("Vaccines:");
        try {
            vaccines = Vaccine.getAllVaccines();
        } catch (SQLException e) {
            System.out.println("Error occurred when retrieving all vaccines: " + e);
            return;
        }

        if (vaccines.size() == 0) {
            System.out.println("No vaccines available");
            return;
        }
        for (int i=0; i<vaccines.size(); ++i) {
            System.out.println(vaccines.get(i));
        }
    }

    private static void reserve(String[] tokens) {
        // reserve <date> <vaccine>
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first");
            return;
        }

        if (currentPatient == null) {
            System.out.println("Please login as a patient");
            return;
        }

        if (tokens.length != 3) {
            System.out.println("reserve failed: Required format `reserve <date> <vaccine>`");
            return;
        }
        Date date;
        try {
            date = Date.valueOf(tokens[1]);
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
            return;
        }
        String vaccineName = tokens[2];
        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when adding doses: " + e);
        }

        if (vaccine == null || vaccine.getAvailableDoses() == 0) {
            System.out.println("Not enough available doses");
            return;
        }

        // get all caregiver names
        List<String> caregivers;
        try {
            caregivers = Caregiver.getAllCaregivers();
        } catch (SQLException e) {
            System.out.println("Error occurred when retrieving all caregivers: " + e);
            return;
        }
        // sort by alphabetical order
        Collections.sort(caregivers);
        // query the availabilities of the existing caregivers
        boolean found_available_caregivers = false;
        Availability availability = null;
        try {
            for (int i=0; i<caregivers.size(); ++i) {
                availability = new Availability.AvailabilityGetter(caregivers.get(i), date).get();
                if (availability != null) {
                    found_available_caregivers = true;
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Please try again");
        }
        if (!found_available_caregivers) {
            System.out.println("No caregiver is available");
            return;
        }
        String caregiver = availability.getCaregiverName();

        // make appointment
        Reservation reservation = null;
        try {
            reservation = new Reservation.ReservationBuilder(
                appointment_id, date, caregiver, currentPatient.getUsername(), vaccineName
            ).build();
            reservation.saveToDB();
        } catch (Exception e) {
            System.out.println("Please try again! Error occured: " + e);
        }

        System.out.println("Appointment ID " + appointment_id + ", Caregiver username " + caregiver);

        appointment_id += 1;

        // remove availability once successfully booked reservation
        // TODO
    }

    private static void uploadAvailability(String[] tokens) {
        // upload_availability <date>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        String date = tokens[1];
        try {
            Date d = Date.valueOf(date);
            currentCaregiver.uploadAvailability(d);
            System.out.println("Availability uploaded!");
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        } catch (SQLException e) {
            System.out.println("Error occurred when uploading availability: " + e);
        }
    }

    private static void cancel(String[] tokens) {
        // TODO: Extra credit
    }

    private static void addDoses(String[] tokens) {
        // add_doses <vaccine> <number>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String vaccineName = tokens[1];
        int doses = Integer.parseInt(tokens[2]);
        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when adding doses");
        }
        // check 3: if getter returns null, it means that we need to create the vaccine and insert it into the Vaccines
        //          table
        if (vaccine == null) {
            try {
                vaccine = new Vaccine.VaccineBuilder(vaccineName, doses).build();
                vaccine.saveToDB();
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
            }
        } else {
            // if the vaccine is not null, meaning that the vaccine already exists in our table
            try {
                vaccine.increaseAvailableDoses(doses);
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
            }
        }
        System.out.println("Doses updated!");
    }

    private static void showAppointments(String[] tokens) {
        // show_appointments
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first");
            return;
        }

        List<Reservation> reservations = null;
        if (currentCaregiver != null) {
            try {
                reservations = Reservation.getAllReservationsByCaregiver(currentCaregiver.getUsername());
            } catch (Exception e) {
                System.out.println("Please try again. Error occured: " + e);
            }
        } else {
            try {
                reservations = Reservation.getAllReservationsByPatient(currentPatient.getUsername());
            } catch (Exception e) {
                System.out.println("Please try again. Error occured: " + e);
            }
        }
        if (reservations.size() == 0) {
            System.out.println("No appointments scheduled");
            return;
        }

        for (int i=0; i<reservations.size(); i++) {
            Reservation r = reservations.get(i);
            String output = String.format(
                "%d %s %s %s",
                r.getId(),
                r.getVaccine(),
                r.getDate().toString(),
                currentCaregiver != null? r.getPatient(): r.getCaregiver()
            );
            System.out.println(output);
        }
    }

    private static void logout(String[] tokens) {
        // logout
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first");
            return;
        }

        try {
            if (currentCaregiver != null) {
                currentCaregiver = null;
            }
            if (currentPatient != null) {
                currentPatient = null;
            }
            System.out.println("Successfully logged out");
        } catch (Exception e) {
            System.out.println("Please try again");
        }
    }
}
