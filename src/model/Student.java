package model;
import java.io.Serializable;

/**
 * Represents a Student user.
 * This class demonstrates INHERITANCE by extending User.
 */
public class Student extends User implements Serializable {
    
    private String major;
    private int yearOfStudy;
    // We will add a list of applications later (e.g., List<Application> myApplications)

    /**
     * Constructor for a Student.
     * @param userID Student's ID (e.g., "U2310001A") [cite: 37]
     * @param name Student's name.
     * @param password Student's password.
     * @param major Student's major (e.g., "Computer Science") [cite: 43, 59]
     * @param yearOfStudy Student's year (1-4) [cite: 43, 58]
     */
    public Student(String userID, String name, String password, String major, int yearOfStudy) {
        // 'super' calls the constructor of the parent (User) class
        super(userID, name, password); 
        this.major = major;
        this.yearOfStudy = yearOfStudy;
    }

    // --- Getters ---

    public String getMajor() {
        return major;
    }

    public int getYearOfStudy() {
        return yearOfStudy;
    }
}