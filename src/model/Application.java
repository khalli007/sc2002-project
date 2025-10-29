package model;
import java.io.Serializable;

/**
 * Represents the link between a Student and an Internship they applied for.
 * This is an "association" class.
 */
public class Application implements Serializable {

    private final int applicationID;
    private static int nextID = 1;

    private final String studentID; // The student who applied
    private final int internshipID; // The internship they applied for
    private ApplicationStatus status; // Status of this specific application [cite: 65]

    public Application(String studentID, int internshipID) {
        this.applicationID = nextID++;
        this.studentID = studentID;
        this.internshipID = internshipID;
        this.status = ApplicationStatus.PENDING; // Default status [cite: 65]
    }

    // --- Getters ---
    public int getApplicationID() { return applicationID; }
    public String getStudentID() { return studentID; }
    public int getInternshipID() { return internshipID; }
    public ApplicationStatus getStatus() { return status; }

    // --- Setter ---
    /**
     * Used by Company Rep to approve or reject the application. [cite: 93]
     * @param status The new status (SUCCESSFUL or UNSUCCESSFUL).
     */
    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
    
    // For saving/loading the static ID counter
    public static int getNextID() { return nextID; }
    public static void setNextID(int id) { nextID = id; }
}