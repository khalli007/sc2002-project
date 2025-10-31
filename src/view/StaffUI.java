package view;

import controller.Database;
import model.CareerCenterStaff;
import model.CompanyRepresentative;
import model.Internship;

import java.util.List;
import java.util.Scanner;

/**
 * StaffUI - menu and features for Career Center Staff users.
 */
public class StaffUI {
    private static final Scanner sc = new Scanner(System.in);
    private static final Database db = Database.getInstance();
    private static void approveCompanyReps() {
        List<CompanyRepresentative> pending = db.getPendingCompanyReps();
        if (pending.isEmpty()) {
            System.out.println("No pending company representatives.");
            return;
        }

        for (int i = 0; i < pending.size(); i++) {
            System.out.println((i + 1) + ". " + pending.get(i).getName() + " (" + pending.get(i).getCompanyName() + ")");
        }

        System.out.print("Enter number to approve/reject (or blank to return): ");
        String input = sc.nextLine();
        if (input.isEmpty()) return;

        try {
            int index = Integer.parseInt(input) - 1;
            CompanyRepresentative rep = pending.get(index);

            System.out.print("Approve (A) or Reject (R)? ");
            String action = sc.nextLine().trim().toUpperCase();

            if (action.equals("A")) {
                db.approveCompanyRep(rep);
                System.out.println(rep.getName() + " approved.");
            } else if (action.equals("R")) {
                db.rejectCompanyRep(rep);
                System.out.println(rep.getName() + " rejected.");
            } else {
                System.out.println("Invalid input.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input, returning to menu.");
        }
    }

    private static void approveInternships() {
        List<Internship> pending = db.getPendingInternships();
        if (pending.isEmpty()) {
            System.out.println("No pending internships.");
            return;
        }

        for (int i = 0; i < pending.size(); i++) {
            System.out.println((i + 1) + ". " + pending.get(i).getTitle() + " (" + pending.get(i).getCompanyName() + ")");
        }

        System.out.print("Enter number to approve/reject (or blank to return): ");
        String input = sc.nextLine();
        if (input.isEmpty()) return;

        try {
            int index = Integer.parseInt(input) - 1;
            Internship internship = pending.get(index);

            System.out.print("Approve (A) or Reject (R)? ");
            String action = sc.nextLine().trim().toUpperCase();

            if (action.equals("A")) {
                db.approveInternship(internship);
                System.out.println("Internship approved.");
            } else if (action.equals("R")) {
                db.rejectInternship(internship);
                System.out.println("Internship rejected.");
            } else {
                System.out.println("Invalid input.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input, returning to menu.");
        }
    }
    public static void showMenu(CareerCenterStaff staff) {
        while (true) {
            System.out.println("\n===== Career Center Staff Menu =====");
            System.out.println("1. Approve/Reject Company Representatives");
            System.out.println("2. Approve/Reject Internship Opportunities");
            System.out.println("3. Generate Reports");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    approveCompanyReps();
                    break;
                case "2":
                    approveInternships();
                    break;
                case "3":
                    System.out.println("Report generation not implemented yet.");
                    break;
                case "4":
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

}
