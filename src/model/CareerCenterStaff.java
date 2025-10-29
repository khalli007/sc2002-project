package model;

import java.io.Serializable;

/**
 * Represents a Career Center Staff user.
 * This class demonstrates INHERITANCE by extending User.
 */
public class CareerCenterStaff extends User implements Serializable {
    
    private String staffDepartment;

    /**
     * Constructor for a CareerCenterStaff.
     * @param userID Staff's ID (e.g., "sng001")
     * @param name Staff's name.
     * @param password Staff's password.
     * @param staffDepartment The staff's department.
     */
    public CareerCenterStaff(String userID, String name, String password, String staffDepartment) {
        super(userID, name, password);
        this.staffDepartment = staffDepartment;
    }

    // --- Getter ---

    public String getStaffDepartment() {
        return staffDepartment;
    }
}