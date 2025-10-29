package model;
import java.io.Serializable;

/**
 * Abstract base class for all users in the system.
 * Implements Serializable to allow user objects to be saved to a file.
 * This class demonstrates ABSTRACTION.
 */
public abstract class User implements Serializable {
    
    // Using 'protected' so child classes can access them, 'private' would also work
    protected String userID;
    protected String name;
    protected String password; // Default is "password" [cite: 40]

    /**
     * Constructor for the User class.
     * @param userID The user's unique identifier.
     * @param name The user's name.
     * @param password The user's password.
     */
    public User(String userID, String name, String password) {
        this.userID = userID;
        this.name = name;
        this.password = password;
    }

    // --- Getters ---
    
    public String getUserID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    // --- Setter ---

    /**
     * Allows the user to change their password[cite: 41].
     * @param newPassword The new password.
     */
    public void setPassword(String newPassword) {
        this.password = newPassword;
    }
}