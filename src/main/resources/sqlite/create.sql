CREATE TABLE Users (
    Username varchar(255) PRIMARY KEY,
    Salt BINARY(16),
    Hash BINARY(16)
);

CREATE TABLE Caregivers (
    Username varchar(255) PRIMARY KEY REFERENCES Users(Username),
    Salt BINARY(16),
    Hash BINARY(16)
);

CREATE TABLE Patients (
    Username varchar(255) PRIMARY KEY REFERENCES Users(Username),
    Salt BINARY(16),
    Hash BINARY(16)
);

CREATE TABLE Availabilities (
    Time date,
    Username varchar(255) REFERENCES Caregivers,
    PRIMARY KEY (Time, Username)
);

CREATE TABLE Vaccines (
    Name varchar(255),
    Doses int,
    PRIMARY KEY (Name)
);

-- CREATE TABLE Reservations {

-- }