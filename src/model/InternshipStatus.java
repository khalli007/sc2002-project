package model;
import java.io.Serializable;

/**
 * Enum to represent the status of an internship opportunity. [cite: 84]
 */
public enum InternshipStatus implements Serializable {
    PENDING,  // Submitted by company, awaiting staff approval [cite: 89]
    APPROVED, // Approved by staff, visible to students [cite: 90]
    REJECTED, // Rejected by staff
    FILLED    // All slots are confirmed by students [cite: 97]
}