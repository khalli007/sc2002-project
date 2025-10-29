package model;
import java.io.Serializable;

/**
 * Represents a Company Representative user.
 * This class demonstrates INHERITANCE by extending User.
 */
public class CompanyRepresentative extends User implements Serializable {
    
    private String companyName;
    private String department;
    private String position;
    // The spec says their ID is their email [cite: 38]
    // The spec also says their status is pending until approved [cite: 74]
    private boolean isApproved;

    /**
     * Constructor for a CompanyRepresentative.
     * @param email Their email, used as their userID [cite: 38]
     * @param name Rep's name.
     * @param password Rep's password.
     * @param companyName The company they work for. [cite: 44]
     * @param department Their department. [cite: 44]
     * @param position Their position. [cite: 44]
     */
    public CompanyRepresentative(String email, String name, String password, 
                                 String companyName, String department, String position) {
        super(email, name, password); // Email is the userID
        this.companyName = companyName;
        this.department = department;
        this.position = position;
        this.isApproved = false; // Default to not approved [cite: 74]
    }

    // --- Getters ---

    public String getCompanyName() {
        return companyName;
    }

    public String getDepartment() {
        return department;
    }

    public String getPosition() {
        return position;
    }

    public boolean isApproved() {
        return isApproved;
    }

    // --- Setter ---

    /**
     * Used by CareerCenterStaff to approve this representative. [cite: 102]
     * @param approved True to approve, false to reject.
     */
    public void setApproved(boolean approved) {
        isApproved = approved;
    }
}