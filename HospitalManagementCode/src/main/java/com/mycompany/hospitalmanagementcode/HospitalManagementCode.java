package com.mycompany.hospitalmanagementcode;

import java.util.*;

public class HospitalManagementCode {
    private static HospitalSystem system = new HospitalSystem();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n===+++ Hospital Management System +++===");
            System.out.println("1. Register Patient\n2. Search Patient by ID\n3. Update Patient\n4. Delete Patient");
            System.out.println("5. Display All Patients\n6. Allocate Bed to Inpatient\n7. Release Bed\n8. Display Ward Layout");
            System.out.println("9. Display Available Beds\n10. Display Occupied Beds\n11. Reports (Totals & Occupancy)");
            System.out.println("12. Sort Patients by Surname\n13. Sort Patients by ID\n0. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1: registerPatient(); break;
                case 2: searchPatient(); break;
                case 3: updatePatient(); break;
                case 4: deletePatient(); break;
                case 5: system.displayAllPatients(); break;
                case 6: allocateBed(); break;
                case 7: releaseBed(); break;
                case 8: system.displayWardLayout(); break;
                case 9: system.displayAvailableBeds(); break;
                case 10: system.displayOccupiedBeds(); break;
                case 11: showReports(); break;
                case 12: showSortedBySurname(); break;
                case 13: showSortedById(); break;
                case 0: exit = true; System.out.println("Exiting..."); break;
                default: System.out.println("Invalid option.");
            }
        }
        scanner.close();
    }

    private static PatientCategory readCategory() {
        System.out.print("Category (INPATIENT, OUTPATIENT, EMERGENCY): ");
        try { return PatientCategory.valueOf(scanner.nextLine().toUpperCase()); }
        catch (IllegalArgumentException e) { System.out.println("Invalid category."); return null; }
    }

    private static void registerPatient() {
        System.out.print("Patient ID: "); String id = scanner.nextLine();
        System.out.print("First Name: "); String firstName = scanner.nextLine();
        System.out.print("Last Name: "); String lastName = scanner.nextLine();
        System.out.print("Age: "); int age = scanner.nextInt(); scanner.nextLine();
        System.out.print("Gender: "); String gender = scanner.nextLine();
        System.out.print("Medical Condition: "); String condition = scanner.nextLine();
        PatientCategory category = readCategory();
        if (category == null) return;

        Patient patient = category == PatientCategory.INPATIENT
                ? new Inpatient(id, firstName, lastName, age, gender, condition, 0, null)
                : new Patient(id, firstName, lastName, age, gender, condition, category);

        if (system.registerPatient(patient)) {
            System.out.println("Patient registered successfully.");
            if (category == PatientCategory.INPATIENT) {
                System.out.println(system.allocateBedToInpatient(id)
                        ? "Bed allocated automatically."
                        : "Warning: No bed available for inpatient. Please allocate later.");
            }
        } else {
            System.out.println("Registration failed. Duplicate ID or error.");
        }
    }

    private static void searchPatient() {
        System.out.print("Enter Patient ID: ");
        Patient p = system.findPatientById(scanner.nextLine());
        if (p == null) System.out.println("Patient not found."); else p.displayDetails();
    }

    private static void updatePatient() {
        System.out.print("Enter Patient ID to update: "); String id = scanner.nextLine();
        Patient p = system.findPatientById(id);
        if (p == null) { System.out.println("Patient not found."); return; }

        System.out.print("New First Name: "); String firstName = scanner.nextLine();
        System.out.print("New Last Name: "); String lastName = scanner.nextLine();
        System.out.print("New Age: "); int age = scanner.nextInt(); scanner.nextLine();
        System.out.print("New Gender: "); String gender = scanner.nextLine();
        System.out.print("New Medical Condition: "); String condition = scanner.nextLine();
        PatientCategory category = readCategory();
        if (category == null) return;

        if (system.updatePatient(id, firstName, lastName, age, gender, condition, category)) {
            System.out.println("Patient updated.");
            if (category == PatientCategory.INPATIENT) {
                if (p instanceof Inpatient && ((Inpatient) p).getBedNumber() == null) {
                    System.out.println(system.allocateBedToInpatient(id) ? "Bed allocated." : "No bed available.");
                }
            } else if (p instanceof Inpatient && ((Inpatient) p).getBedNumber() != null) {
                system.releaseBedForPatient(id);
                System.out.println("Bed released because patient is no longer inpatient.");
            }
        } else {
            System.out.println("Update failed.");
        }
    }

    private static void deletePatient() {
        System.out.print("Enter Patient ID to delete: ");
        System.out.println(system.deletePatient(scanner.nextLine()) ? "Patient deleted." : "Patient not found.");
    }

    private static void allocateBed() {
        System.out.print("Enter Inpatient ID: ");
        System.out.println(system.allocateBedToInpatient(scanner.nextLine())
                ? "Bed allocated successfully."
                : "Allocation failed. Check if patient exists, is inpatient, already has bed, or no beds available.");
    }

    private static void releaseBed() {
        System.out.print("Enter Inpatient ID: ");
        System.out.println(system.releaseBedForPatient(scanner.nextLine())
                ? "Bed released." : "Release failed. Check patient and bed.");
    }

    private static void showReports() {
        System.out.println("=== Reports ===");
        System.out.println("Total Registered Patients: " + system.getTotalRegisteredPatients());
        System.out.println("Total Occupied Beds: " + system.getTotalOccupiedBeds());
        System.out.printf("Ward Occupancy: %.2f%%\n", system.getWardOccupancyPercentage());
    }

    private static void showSortedBySurname() {
        System.out.println("=== Patients Sorted by Surname ===");
        for (Patient p : system.getPatientsSortedBySurname())
            System.out.println(p.getPatientId() + " - " + p.getFirstName() + " " + p.getLastName());
    }

    private static void showSortedById() {
        System.out.println("=== Patients Sorted by ID ===");
        for (Patient p : system.getPatientsSortedById())
            System.out.println(p.getPatientId() + " - " + p.getFirstName() + " " + p.getLastName());
    }
}










enum PatientCategory { INPATIENT, OUTPATIENT, EMERGENCY }

class Patient {
    private String patientId, firstName, lastName, gender, medicalCondition;
    private int age;
    private PatientCategory category;

    public Patient(String patientId, String firstName, String lastName, int age,
                   String gender, String medicalCondition, PatientCategory category) {
        this.patientId = patientId; this.firstName = firstName; this.lastName = lastName;
        this.age = age; this.gender = gender; this.medicalCondition = medicalCondition; this.category = category;
    }

    public String getPatientId() { return patientId; }
    public void setPatientId(String v) { patientId = v; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { firstName = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { lastName = v; }
    public int getAge() { return age; }
    public void setAge(int v) { age = v; }
    public String getGender() { return gender; }
    public void setGender(String v) { gender = v; }
    public String getMedicalCondition() { return medicalCondition; }
    public void setMedicalCondition(String v) { medicalCondition = v; }
    public PatientCategory getCategory() { return category; }
    public void setCategory(PatientCategory v) { category = v; }
    public String getSurname() { return lastName; }

    public void displayDetails() {
        System.out.println("Patient ID: " + patientId);
        System.out.println("Name: " + firstName + " " + lastName);
        System.out.println("Age: " + age);
        System.out.println("Gender: " + gender);
        System.out.println("Medical Condition: " + medicalCondition);
        System.out.println("Category: " + category);
    }
}

class Inpatient extends Patient {
    private int wardNumber;
    private String bedNumber;

    public Inpatient(String patientId, String firstName, String lastName, int age,
                     String gender, String medicalCondition, int wardNumber, String bedNumber) {
        super(patientId, firstName, lastName, age, gender, medicalCondition, PatientCategory.INPATIENT);
        this.wardNumber = wardNumber; this.bedNumber = bedNumber;
    }

    public int getWardNumber() { return wardNumber; }
    public void setWardNumber(int v) { wardNumber = v; }
    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String v) { bedNumber = v; }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.println("Ward Number: " + wardNumber);
        System.out.println("Bed Number: " + (bedNumber == null ? "Not allocated" : bedNumber));
    }
}

class Bed {
    private String bedId;
    private boolean occupied;
    private Inpatient patient;

    public Bed(String bedId) { this.bedId = bedId; }

    public String getBedId() { return bedId; }
    public boolean isOccupied() { return occupied; }
    public Inpatient getPatient() { return patient; }

    public boolean allocate(Inpatient p) {
        if (occupied) return false;
        occupied = true; patient = p; return true;
    }

    public void release() { occupied = false; patient = null; }

    @Override
    public String toString() { return bedId + (occupied ? " [Occupied]" : " [Available]"); }
}

class Ward {
    private static final int ROWS = 4, COLS = 5;
    private Bed[][] beds = new Bed[ROWS][COLS];
    private List<Bed> allBeds = new ArrayList<>();

    public Ward() {
        int id = 1;
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) {
                Bed bed = new Bed(String.format("B%02d", id++));
                beds[r][c] = bed; allBeds.add(bed);
            }
    }

    public boolean allocateBed(Inpatient patient) {
        for (Bed bed : allBeds) {
            if (!bed.isOccupied()) {
                bed.allocate(patient);
                patient.setBedNumber(bed.getBedId());
                patient.setWardNumber(1);
                return true;
            }
        }
        return false;
    }

    public boolean releaseBed(String bedId) {
        for (Bed bed : allBeds) {
            if (bed.getBedId().equalsIgnoreCase(bedId) && bed.isOccupied()) { bed.release(); return true; }
        }
        return false;
    }

    public void displayLayout() {
        System.out.println("=== Ward Layout (4x5) ===");
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Bed bed = beds[r][c];
                System.out.print(bed.getBedId() + "[" + (bed.isOccupied() ? "X" : "O") + "] ");
            }
            System.out.println();
        }
    }

    public List<Bed> getAvailableBeds() {
        List<Bed> a = new ArrayList<>();
        for (Bed b : allBeds) if (!b.isOccupied()) a.add(b);
        return a;
    }

    public List<Bed> getOccupiedBeds() {
        List<Bed> o = new ArrayList<>();
        for (Bed b : allBeds) if (b.isOccupied()) o.add(b);
        return o;
    }

    public int getTotalBeds() { return allBeds.size(); }
    public int getOccupiedCount() { return getOccupiedBeds().size(); }
    public double getOccupancyPercentage() { return (double) getOccupiedCount() / getTotalBeds() * 100; }
    public boolean hasAvailableBeds() { return !getAvailableBeds().isEmpty(); }
}

class HospitalSystem {
    private List<Patient> patients = new ArrayList<>();
    private Ward ward = new Ward();

    public boolean registerPatient(Patient patient) {
        if (findPatientById(patient.getPatientId()) != null) return false;
        patients.add(patient); return true;
    }

    public Patient findPatientById(String id) {
        for (Patient p : patients) if (p.getPatientId().equalsIgnoreCase(id)) return p;
        return null;
    }

    public boolean updatePatient(String id, String firstName, String lastName, int age,
                                 String gender, String medicalCondition, PatientCategory category) {
        Patient p = findPatientById(id);
        if (p == null) return false;
        p.setFirstName(firstName); p.setLastName(lastName); p.setAge(age);
        p.setGender(gender); p.setMedicalCondition(medicalCondition); p.setCategory(category);
        return true;
    }

    public boolean deletePatient(String id) {
        Patient p = findPatientById(id);
        if (p == null) return false;
        if (p instanceof Inpatient) {
            String bedId = ((Inpatient) p).getBedNumber();
            if (bedId != null && !bedId.isEmpty()) ward.releaseBed(bedId);
        }
        patients.remove(p);
        return true;
    }

    public List<Patient> getAllPatients() { return new ArrayList<>(patients); }

    public void displayAllPatients() {
        if (patients.isEmpty()) { System.out.println("No patients registered."); return; }
        System.out.println("=== All Registered Patients ===");
        for (Patient p : patients) { p.displayDetails(); System.out.println("-------------------"); }
    }

    public boolean allocateBedToInpatient(String id) {
        Patient p = findPatientById(id);
        if (!(p instanceof Inpatient) || p.getCategory() != PatientCategory.INPATIENT) return false;
        Inpatient ip = (Inpatient) p;
        if (ip.getBedNumber() != null && !ip.getBedNumber().isEmpty()) return false;
        return ward.allocateBed(ip);
    }

    public boolean releaseBedForPatient(String id) {
        Patient p = findPatientById(id);
        if (!(p instanceof Inpatient) || p.getCategory() != PatientCategory.INPATIENT) return false;
        Inpatient ip = (Inpatient) p;
        String bedId = ip.getBedNumber();
        if (bedId == null || bedId.isEmpty()) return false;
        boolean released = ward.releaseBed(bedId);
        if (released) { ip.setBedNumber(null); ip.setWardNumber(0); }
        return released;
    }

    public Ward getWard() { return ward; }

    public void displayAvailableBeds() {
        List<Bed> a = ward.getAvailableBeds();
        if (a.isEmpty()) { System.out.println("No available beds."); return; }
        System.out.println("Available beds:");
        for (Bed b : a) System.out.println(b.getBedId());
    }

    public void displayOccupiedBeds() {
        List<Bed> o = ward.getOccupiedBeds();
        if (o.isEmpty()) { System.out.println("No occupied beds."); return; }
        System.out.println("Occupied beds:");
        for (Bed b : o) System.out.println(b.getBedId() + " - Patient: " + b.getPatient().getFirstName() + " " + b.getPatient().getLastName());
    }

    public int getTotalRegisteredPatients() { return patients.size(); }
    public int getTotalOccupiedBeds() { return ward.getOccupiedCount(); }
    public double getWardOccupancyPercentage() { return ward.getOccupancyPercentage(); }
    public void displayWardLayout() { ward.displayLayout(); }

    public List<Patient> getPatientsSortedBySurname() {
        List<Patient> s = new ArrayList<>(patients);
        s.sort(Comparator.comparing(Patient::getSurname));
        return s;
    }

    public List<Patient> getPatientsSortedById() {
        List<Patient> s = new ArrayList<>(patients);
        s.sort(Comparator.comparing(Patient::getPatientId));
        return s;
    }
}

