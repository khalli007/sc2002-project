package view;

import java.util.Scanner;
import controller.Database;
import model.CareerCenterStaff;
import model.CompanyRepresentative;
import model.Student;
import model.User;

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
            int choice = readInt("Choose an option: ");

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

        if (user instanceof Student student) {
            StudentUI.showStudentMenu(student);
        } else if (user instanceof CareerCenterStaff staff) {
            StaffUI.showStaffMenu(staff);
        } else if (user instanceof CompanyRepresentative rep) {
            CompanyRepUI.showCompanyRepMenu(rep);
        }

        System.out.println("Logging out " + user.getName() + "...");
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
        db.addUser(newRep);
        
        System.out.println("\nRegistration successful!");
        System.out.println("Your account is now pending approval from Career Center Staff.");
        System.out.println("You will be able to log in once your account is approved.");
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine();
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}