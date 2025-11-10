package model;

import java.io.Serializable;
import java.time.LocalDate; 

/**
 * Represents an Internship opportunity.
 * This is a core "Entity" or "Model" class.
 */
public class Internship implements Serializable {

    // Using LocalDate for dates is cleaner than strings
    private String internshipTitle;
    private String description;
    private InternshipLevel level;
    private String preferredMajor;
    private LocalDate openingDate;
    private LocalDate closingDate;
    private InternshipStatus status;
    private String companyName;
    private String companyRepInCharge; // The ID of the rep who created it
    private int slots;
    private boolean isVisible; // Toggled by Company Rep

    // We also need a unique ID for each internship
    private final int internshipID;
    private static int nextID = 1; // A simple way to auto-increment IDs

    public Internship(String internshipTitle, String description, InternshipLevel level, 
                      String preferredMajor, LocalDate openingDate, LocalDate closingDate, 
                      String companyName, String companyRepInCharge, int slots) {
        
        this.internshipID = nextID++;
        this.internshipTitle = internshipTitle;
        this.description = description;
        this.level = level;
        this.preferredMajor = preferredMajor;
        this.openingDate = openingDate;
        this.closingDate = closingDate;
        this.companyName = companyName;
        this.companyRepInCharge = companyRepInCharge;
        this.slots = slots;
        
        this.status = InternshipStatus.PENDING; // Must be approved by staff
        this.isVisible = false; // Default to off
    }

    // --- Getters ---
    // (We need getters for almost everything so other classes can read the data)
    
    public int getInternshipID() { return internshipID; }
    public String getInternshipTitle() { return internshipTitle; }
    public String getDescription() { return description; }
    public InternshipLevel getLevel() { return level; }
    public String getPreferredMajor() { return preferredMajor; }
    public LocalDate getOpeningDate() { return openingDate; }
    public LocalDate getClosingDate() { return closingDate; }
    public InternshipStatus getStatus() { return status; }
    public String getCompanyName() { return companyName; }
    public String getCompanyRepInCharge() { return companyRepInCharge; }
    public int getSlots() { return slots; }
    public boolean isVisible() { return isVisible; }

    // --- Setters ---
    // (We only create setters for things that need toD change)

    /**
     * Used by Staff to approve or reject an internship.
     * @param status The new status (APPROVED or REJECTED).
     */
    public void setStatus(InternshipStatus status) {
        this.status = status;
    }

    /**
     * Used by Company Rep to toggle visibility.
     * @param visible True to make it visible, false to hide.
     */
    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    // This is a special method to link static and non-static worlds
    // for saving/loading the nextID state.
    public static int getNextID() { return nextID; }
    public static void setNextID(int id) { nextID = id; }

    //Staff UI helper methods

    public boolean isApproved() {
        return status == InternshipStatus.APPROVED;
    }

    public void setApproved(boolean approved) {
        this.status = approved ? InternshipStatus.APPROVED : InternshipStatus.REJECTED;
    }

    public String getTitle() {
        return getInternshipTitle();
    }
    
    public boolean isAcceptingOn(LocalDate date) {
    return (date.isAfter(openingDate) || date.isEqual(openingDate))
        && (date.isBefore(closingDate) || date.isEqual(closingDate));
}
}