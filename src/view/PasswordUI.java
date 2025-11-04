package view;

import model.User;

import java.util.Scanner;

/**
 * Shared helper for handling password changes across different user roles.
 */
public final class PasswordUI {

    private static final Scanner SCANNER = new Scanner(System.in);

    private PasswordUI() {
    }

    public static void changePassword(User user) {
        System.out.println("\n--- Change Password ---");
        System.out.print("Enter current password: ");
        String current = SCANNER.nextLine();
        if (!user.getPassword().equals(current)) {
            System.out.println("Current password is incorrect.");
            return;
        }

        System.out.print("Enter new password: ");
        String newPassword = SCANNER.nextLine();
        if (newPassword.isBlank()) {
            System.out.println("Password cannot be blank.");
            return;
        }

        System.out.print("Confirm new password: ");
        String confirm = SCANNER.nextLine();
        if (!newPassword.equals(confirm)) {
            System.out.println("Passwords do not match.");
            return;
        }

        user.setPassword(newPassword);
        System.out.println("Password updated successfully. Please use the new password on your next login.");
    }
}
