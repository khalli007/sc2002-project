package view;

import java.util.Scanner;
import controller.Database;
import model.User;
import model.CareerCenterStaff;
import model.CompanyRepresentative;
import model.Student;
import view.StaffUI;
import view.CompanyRepUI;
import view.StudentUI;

/**
 * Handles the main login and registration UI for all users.
 * This is a "Boundary" class.
 */
public class LoginUI {

    private static final Scanner sc = new Scanner(System.in);
    private static final Database db = Database.getInstance();

    /**
     * The main application loop. Shows the login menu until the user quits.
     */
    public static void showLoginMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Login");
            System.out.println("2. Register as Company Representative");
            System.out.println("3. Quit");
            System.out.print("Choose an option: ");

            int choice = -1;
            try {
                choice = sc.nextInt();
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                sc.nextLine(); // Clear the bad input
                continue; // Skip the rest of the loop and start over
            }
            sc.nextLine(); // Consume the newline left-over

            switch (choice) {
                case 1:
                    handleLogin();
                    break;
                case 2:
                    handleRegister();
                    break;
                case 3:
                    running = false; // This will exit the while-loop
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Handles the user login process.
     */
    private static void handleLogin() {
        System.out.println("\n--- Login ---");
        System.out.print("Enter User ID (Email for Reps, NTU ID for Staff, Student ID for Students): ");
        String userID = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        User user = db.authenticateUser(userID, password);

        if (user == null) {
            // Check if the user exists but is an unapproved rep
            User potentialUser = db.findUserById(userID);
            if (potentialUser instanceof CompanyRepresentative && !((CompanyRepresentative) potentialUser).isApproved()) {
                System.out.println("Login failed. Your account is still pending approval from Career Center Staff.");
            } else {
                System.out.println("Login failed. Invalid User ID or Password.");
            }
            return; // Go back to main menu
        }

        // Login Successful!
        System.out.println("Login successful. Welcome, " + user.getName() + "!");

        // --- POLYMORPHISM IN ACTION ---
        // We don't know what kind of user it is, so we check.
        // This will direct the user to their specific menu.
        // We will create these other UI classes next.
        
        if (user instanceof CareerCenterStaff) {
            StaffUI.showMenu((CareerCenterStaff) user);
        } 
        /*
        else if (user instanceof CompanyRepresentative) {
            CompanyRepUI.showMenu((CompanyRepresentative) user);
        } else if (user instanceof Student) {
            StudentUI.showMenu((Student) user);
        } else {
            System.out.println("Unknown user type. Cannot proceed.");
        }
        */
    }

    /**
     * Handles the new Company Representative registration process.
     */
    private static void handleRegister() {
        System.out.println("\n--- Company Representative Registration ---");
        
        System.out.print("Enter your Name: ");
        String name = sc.nextLine();
        System.out.print("Enter your Email (this will be your User ID): ");
        String email = sc.nextLine();
        
        // Check if user ID already exists
        if (db.findUserById(email) != null) {
            System.out.println("Registration failed. This email is already in use.");
            return;
        }

        System.out.print("Enter your Company Name: ");
        String company = sc.nextLine();
        System.out.print("Enter your Department: ");
        String dept = sc.nextLine();
        System.out.print("Enter your Position: ");
        String pos = sc.nextLine();
        
        // Per spec, default password is "password"
        String password = "password"; 

        // Create the new rep
        CompanyRepresentative newRep = new CompanyRepresentative(email, name, password, company, dept, pos);
        
        // Add them to the main user list in the database
        db.getUsers().add(newRep);
        
        System.out.println("\nRegistration successful!");
        System.out.println("Your account is now pending approval from Career Center Staff.");
        System.out.println("You will be able to log in once your account is approved.");
    }
}