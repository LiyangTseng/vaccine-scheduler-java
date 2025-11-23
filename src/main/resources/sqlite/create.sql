CREATE TABLE Users (
    Username varchar(255) PRIMARY KEY,
    Salt BINARY(16),
    Hash BINARY(16)
);

CREATE TABLE Caregivers (
    Username varchar(255) PRIMARY KEY REFERENCES Users(Username)
);

CREATE TABLE Patients (
    Username varchar(255) PRIMARY KEY REFERENCES Users(Username)
);

CREATE TABLE Availabilities (
    Time date,
    Username varchar(255) REFERENCES Caregivers,
    PRIMARY KEY (Time, Username)
);

-- "appointments"
CREATE TABLE Reservations (
    Id int,
    Time date,
    CaregiverName varchar(255) REFERENCES Caregivers(Username),
    PatientName varchar(255) REFERENCES Patients(Username),
    VaccineName varchar(255) REFERENCES Vaccines(Name),
    PRIMARY KEY (Time, CaregiverName)
);

CREATE TABLE Vaccines (
    Name varchar(255),
    Doses int,
    PRIMARY KEY (Name)
);