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

    private static void generateReports() {
        List<Internship> internships = db.getInternships();
        if (internships.isEmpty()) {
            System.out.println("No internships available to report.");
            return;
        }

        System.out.println("\n--- Generate Internship Report ---");
        System.out.println("Filter by:");
        System.out.println("1. Status (PENDING / APPROVED / REJECTED)");
        System.out.println("2. Preferred Major");
        System.out.println("3. Internship Level (BASIC / INTERMEDIATE / ADVANCED)");
        System.out.println("4. Show All Internships");
        System.out.print("Choose a filter option: ");
        String choice = sc.nextLine().trim();

        List<Internship> filtered = internships;

        switch (choice) {
            case "1":
                System.out.print("Enter Status (PENDING/APPROVED/REJECTED): ");
                String status = sc.nextLine().trim().toUpperCase();
                filtered = internships.stream()
                        .filter(i -> i.getStatus().name().equalsIgnoreCase(status))
                        .toList();
                break;

            case "2":
                System.out.print("Enter Preferred Major (e.g., CSC, EEE, MAE): ");
                String major = sc.nextLine().trim().toLowerCase();
                filtered = internships.stream()
                        .filter(i -> i.getPreferredMajor().toLowerCase().contains(major))
                        .toList();
                break;

            case "3":
                System.out.print("Enter Internship Level (BASIC/INTERMEDIATE/ADVANCED): ");
                String level = sc.nextLine().trim().toUpperCase();
                filtered = internships.stream()
                        .filter(i -> i.getLevel() != null && i.getLevel().name().equalsIgnoreCase(level))
                        .toList();
                break;

            case "4":
                // no filter
                break;

            default:
                System.out.println("Invalid choice. Returning to menu.");
                return;
        }

        if (filtered.isEmpty()) {
            System.out.println("\nNo internships found for the selected filter.");
            return;
        }

        System.out.println("\n===== Internship Report =====");
        for (Internship i : filtered) {
            System.out.println("ID: " + i.getInternshipID());
            System.out.println("Title: " + i.getInternshipTitle());
            System.out.println("Company: " + i.getCompanyName());
            System.out.println("Level: " + i.getLevel());
            System.out.println("Preferred Major: " + i.getPreferredMajor());
            System.out.println("Status: " + i.getStatus());
            System.out.println("Visible: " + (i.isVisible() ? "Yes" : "No"));
            System.out.println("---------------------------------------");
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
                    generateReports();
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
