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
    private boolean accepted; // True once the student confirms the placement
    private WithdrawalStatus withdrawalStatus; // Tracks withdrawal lifecycle

    public Application(String studentID, int internshipID) {
        this.applicationID = nextID++;
        this.studentID = studentID;
        this.internshipID = internshipID;
        this.status = ApplicationStatus.PENDING; // Default status [cite: 65]
        this.accepted = false;
        this.withdrawalStatus = WithdrawalStatus.NONE;
    }

    // --- Getters ---
    public int getApplicationID() { return applicationID; }
    public String getStudentID() { return studentID; }
    public int getInternshipID() { return internshipID; }
    public ApplicationStatus getStatus() { return status; }
    public boolean isAccepted() { return accepted; }
    public WithdrawalStatus getWithdrawalStatus() { return withdrawalStatus; }

    // --- Setter ---
    /**
     * Used by Company Rep to approve or reject the application. [cite: 93]
     * @param status The new status (SUCCESSFUL or UNSUCCESSFUL).
     */
    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    /**
     * Marks the application as accepted by the student.
     * @param accepted True if the student accepts the offer.
     */
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    /**
     * Updates the withdrawal status (used by staff during approvals).
     * @param status New withdrawal status.
     */
    public void setWithdrawalStatus(WithdrawalStatus status) {
        this.withdrawalStatus = status;
    }

    /**
     * Convenience helper to check if the application is still active from the
     * student's perspective (i.e., pending review or awaiting confirmation).
     * @return True if application counts towards the student's concurrent cap.
     */
    public boolean isActive() {
        if (withdrawalStatus == WithdrawalStatus.APPROVED) {
            return false;
        }
        return status == ApplicationStatus.PENDING
                || (status == ApplicationStatus.SUCCESSFUL && !accepted);
    }
    
    // For saving/loading the static ID counter
    public static int getNextID() { return nextID; }
    public static void setNextID(int id) { nextID = id; }
}