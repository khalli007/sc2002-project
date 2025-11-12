package view;

import controller.Database;
import model.User;
import java.util.Scanner;

/**
 * PasswordUI - allows any user to change their password.
 */
public class PasswordUI {
    private static final Scanner sc = new Scanner(System.in);

    public static void changePassword(User user) {
        System.out.println("\n--- Change Password ---");
        System.out.print("Enter current password: ");
        String current = sc.nextLine();

        // Verify current password
        if (!user.getPassword().equals(current)) {
            System.out.println("Incorrect current password. Failed to change password.");
            return;
        }

        System.out.print("Enter new password: ");
        String newPass = sc.nextLine();

        System.out.print("Confirm new password: ");
        String confirm = sc.nextLine();

        if (!newPass.equals(confirm)) {
            System.out.println("Passwords do not match. Failed to change password.");
            return;
        }

        user.setPassword(newPass);
        Database.getInstance().saveData();
        System.out.println("Password changed successfully! Please log in again to continue.");
    }
}
