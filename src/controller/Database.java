package controller;

import java.io.*; // For file input/output
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import model.*; // Import all our model classes

/**
 * Manages all application data using a Singleton pattern.
 * This class handles loading, saving, and providing access to all users,
 * internships, and applications.
 * This is our main "Control" class.
 */
public class Database {

    // --- Singleton Instance ---
    private static Database instance = null;

    /**
     * Private constructor to prevent anyone else from creating a new instance.
     * This is part of the Singleton pattern.
     */
    private Database() {
        // Initialize all our data lists
        users = new ArrayList<>();
        internships = new ArrayList<>();
        applications = new ArrayList<>();
    }

    /**
     * The public, static way to get the one and only Database instance.
     * @return The single instance of the Database.
     */
    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    // --- Data Storage ---
    // We will hold all our application data in these lists.
    private List<User> users;
    private List<Internship> internships;
    private List<Application> applications;
    
    // File paths for saving data
    private static final String USERS_FILE = "users.dat";
    private static final String INTERNSHIPS_FILE = "internships.dat";
    private static final String APPLICATIONS_FILE = "applications.dat";
    private static final String ID_COUNTERS_FILE = "id_counters.dat"; // To save our static 'nextID'

    // --- Public Getters ---
    // These methods let other parts of our app see the data.
    
    public List<User> getUsers() {
        return users;
    }
    
    public List<Student> getStudents() {
        // We use Java Streams to filter the list
        return users.stream()
                .filter(user -> user instanceof Student)
                .map(user -> (Student) user)
                .collect(Collectors.toList());
    }

    public List<CompanyRepresentative> getCompanyRepresentatives() {
        return users.stream()
                .filter(user -> user instanceof CompanyRepresentative)
                .map(user -> (CompanyRepresentative) user)
                .collect(Collectors.toList());
    }

    public List<CareerCenterStaff> getStaff() {
        return users.stream()
                .filter(user -> user instanceof CareerCenterStaff)
                .map(user -> (CareerCenterStaff) user)
                .collect(Collectors.toList());
    }

    public List<Internship> getInternships() {
        return internships;
    }

    public List<Application> getApplications() {
        return applications;
    }

    // --- Data Persistence (Save/Load) ---

    /**
     * Saves all application data to .dat files using Object Serialization.
     */
    public void saveData() {
        // We use a try-with-resources block to automatically close the files
        try (ObjectOutputStream oosUsers = new ObjectOutputStream(new FileOutputStream(USERS_FILE));
             ObjectOutputStream oosInternships = new ObjectOutputStream(new FileOutputStream(INTERNSHIPS_FILE));
             ObjectOutputStream oosApplications = new ObjectOutputStream(new FileOutputStream(APPLICATIONS_FILE));
             ObjectOutputStream oosIds = new ObjectOutputStream(new FileOutputStream(ID_COUNTERS_FILE))) {

            // Write the lists to their files
            oosUsers.writeObject(users);
            oosInternships.writeObject(internships);
            oosApplications.writeObject(applications);
            
            // Save the static ID counters
            oosIds.writeInt(Internship.getNextID());
            oosIds.writeInt(Application.getNextID());
            
            System.out.println("Data saved successfully.");

        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    /**
     * Loads all application data from .dat files.
     * If files don't exist (first time running), it loads initial CSVs.
     */
    @SuppressWarnings("unchecked")
    public void loadData() {
        // Check if the save files exist
        if (new File(USERS_FILE).exists()) {
            try (ObjectInputStream oisUsers = new ObjectInputStream(new FileInputStream(USERS_FILE));
                 ObjectInputStream oisInternships = new ObjectInputStream(new FileInputStream(INTERNSHIPS_FILE));
                 ObjectInputStream oisApplications = new ObjectInputStream(new FileInputStream(APPLICATIONS_FILE));
                 ObjectInputStream oisIds = new ObjectInputStream(new FileInputStream(ID_COUNTERS_FILE))) {

                // Read the objects back in the SAME order we wrote them
                users = (List<User>) oisUsers.readObject();
                internships = (List<Internship>) oisInternships.readObject();
                applications = (List<Application>) oisApplications.readObject();
                
                // Restore the static ID counters
                Internship.setNextID(oisIds.readInt());
                Application.setNextID(oisIds.readInt());

                System.out.println("Data loaded successfully.");

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error loading data: " + e.getMessage());
                // If loading fails, fall back to initial data
                loadInitialCsvData();
            }
        } else {
            // No save files found, this is the first run
            System.out.println("No save data found. Loading initial CSV data...");
            loadInitialCsvData();
            saveData(); // Save the initial data so we have it for next time
        }
    }

    /**
     * Loads the initial user data from the sample CSV files.
     * Per the spec, Company Rep list is empty.
     */
    private void loadInitialCsvData() {
        users = new ArrayList<>(); // Start fresh
        
        // --- Load Staff List ---
        // We use Files.lines in a try-catch to read the CSV
        try {
            List<String> staffLines = Files.readAllLines(Paths.get("sample_staff_list.csv"));
            // Skip the header row (i = 1)
            for (int i = 1; i < staffLines.size(); i++) {
                String[] data = staffLines.get(i).split(",");
                if (data.length == 5) {
                    // StaffID,Name,Role,Department,Email
                    // Create a new staff object and add it to our user list
                    CareerCenterStaff staff = new CareerCenterStaff(
                            data[0], // StaffID
                            data[1], // Name
                            "password", // Default password
                            data[3]  // Department
                    );
                    users.add(staff);
                }
            }
            System.out.println("Loaded " + getStaff().size() + " staff members.");
        } catch (IOException e) {
            System.out.println("Error reading sample_staff_list.csv: " + e.getMessage());
        }

        // --- Load Student List ---
        try {
            List<String> studentLines = Files.readAllLines(Paths.get("sample_student_list.csv"));
            // Skip the header row (i = 1)
            for (int i = 1; i < studentLines.size(); i++) {
                String[] data = studentLines.get(i).split(",");
                if (data.length == 5) {
                    // StudentID,Name,Major,Year,Email
                    // Create a new student object and add it to our user list
                    Student student = new Student(
                            data[0], // StudentID
                            data[1], // Name
                            "password", // Default password
                            data[2], // Major
                            Integer.parseInt(data[3]) // Year
                    );
                    users.add(student);
                }
            }
            System.out.println("Loaded " + getStudents().size() + " students.");
        } catch (IOException e) {
            System.out.println("Error reading sample_student_list.csv: " + e.getMessage());
        }
        
        // Company Rep list starts empty per spec
        System.out.println("Company Representative list is empty.");
    }
    
    // --- Helper Methods ---

    /**
     * Finds a user by their User ID.
     * @param userID The ID to search for.
     * @return The User object if found, or null if not found.
     */
    public User findUserById(String userID) {
        for (User user : users) {
            if (user.getUserID().equals(userID)) {
                return user;
            }
        }
        return null; // Not found
    }
    public Internship findInternshipById(int id) {
    for (Internship i : internships) {
        if (i.getInternshipID() == id)
            return i;
    }
    return null;
    }

    public List<Application> getApplicationsByStudent(String studentID) {
        List<Application> result = new ArrayList<>();
        for (Application a : applications) {
            if (a.getStudentID().equals(studentID))
                result.add(a);
        }
        return result;
    }

    public void addApplication(Application app) {
        applications.add(app);
    }

    public boolean hasStudentAcceptedPlacement(String studentID) {
        for (Application a : applications) {
            if (a.getStudentID().equals(studentID) && a.isAccepted()) return true;
        }
        return false;
    }

    public long countActiveApplicationsForStudent(String studentID) {
        return applications.stream()
                .filter(a -> a.getStudentID().equals(studentID))
                .filter(a -> a.getWithdrawalStatus() != WithdrawalStatus.APPROVED)
                .count();
    }

    public long countAcceptedForInternship(int internshipID) {
        return applications.stream()
                .filter(a -> a.getInternshipID() == internshipID && a.isAccepted())
                .count();
    }


    /**
     * Attempts to authenticate a user.
     * @param userID The User ID.
     * @param password The password.
     * @return The User object if login is successful, or null if it fails.
     */
    public User authenticateUser(String userID, String password) {
        User user = findUserById(userID);
        
        if (user != null && user.getPassword().equals(password)) {
            // Special check for Company Rep: must be approved
            if (user instanceof CompanyRepresentative) {
                if (((CompanyRepresentative) user).isApproved()) {
                    return user; // Approved rep, login success
                } else {
                    return null; // Rep not approved, login fail
                }
            }
            return user; // Student or Staff, login success
        }
        return null; // Wrong ID or password
    }

    // --- Approval Methods for Staff ---
    

    public List<CompanyRepresentative> getPendingCompanyReps() {
        List<CompanyRepresentative> pending = new ArrayList<>();
        for (User u : users) {
            if (u instanceof CompanyRepresentative rep && !rep.isApproved()) {
                pending.add(rep);
            }
        }
        return pending;
    }

    /** Approves a company representative */
    public void approveCompanyRep(CompanyRepresentative rep) {
        rep.setApproved(true);
        saveData();
    }

    /** Rejects a company representative (removes them from the system) */
    public void rejectCompanyRep(CompanyRepresentative rep) {
        users.remove(rep);
        saveData();
    }

    /** Returns all internships that are not yet approved */
    public List<Internship> getPendingInternships() {
        List<Internship> pending = new ArrayList<>();
        for (Internship i : internships) {
            if (!i.isApproved()) {
                pending.add(i);
            }
        }
        return pending;
    }

    /** Approves an internship */
    public void approveInternship(Internship internship) {
        internship.setApproved(true);
        internship.setVisible(true); // make visible to students
        saveData();
    }


    /** Rejects an internship (removes it) */
    public void rejectInternship(Internship internship) {
        internships.remove(internship);
        saveData();
    }

}