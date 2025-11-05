package view;

import controller.Database;
import model.CompanyRepresentative;
import model.Internship;
import java.time.LocalDate;
import java.util.Scanner;

public class CompanyRepUI {
    private static final Scanner sc = new Scanner(System.in);
    private static final Database db = Database.getInstance();

    public static void showMenu(CompanyRepresentative rep) {
        while (true) {
            System.out.println("\n===== Company Representative Menu =====");
            System.out.println("1. Post a new Internship");
            System.out.println("2. View My Internships");
            System.out.println("3. Logout");
            System.out.print("Enter choice: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    postInternship(rep);
                    break;
                case "2":
                    viewInternships(rep);
                    break;
                case "3":
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void postInternship(CompanyRepresentative rep) {
        System.out.println("\n--- Post New Internship ---");
        System.out.print("Enter Internship Title: ");
        String title = sc.nextLine();
        System.out.print("Enter Description: ");
        String desc = sc.nextLine();
        System.out.print("Preferred Major: ");
        String major = sc.nextLine();
        System.out.print("Number of slots: ");
        int slots = Integer.parseInt(sc.nextLine());

        // For now, we can skip level and use dummy dates
        LocalDate openDate = LocalDate.now();
        LocalDate closeDate = openDate.plusMonths(1);

        Internship internship = new Internship(
                title, desc, null, major,
                openDate, closeDate,
                rep.getCompanyName(), rep.getUserID(), slots
        );

        db.getInternships().add(internship);
        System.out.println("Internship posted successfully. Pending staff approval.");
    }

    private static void viewInternships(CompanyRepresentative rep) {
        System.out.println("\n--- My Internships ---");
        db.getInternships().stream()
                .filter(i -> i.getCompanyRepInCharge().equals(rep.getUserID()))
                .forEach(i -> System.out.println(
                        i.getInternshipTitle() + " (" + i.getStatus() + ")"));
    }
}
