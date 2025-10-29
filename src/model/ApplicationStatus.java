package model;
import java.io.Serializable;

/**
 * Enum to represent the status of a student's internship application. [cite: 65]
 * Using an enum prevents errors from typos in strings.
 */
public enum ApplicationStatus implements Serializable {
    PENDING,
    SUCCESSFUL,
    UNSUCCESSFUL
}