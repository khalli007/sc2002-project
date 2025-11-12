package view;

import controller.Database;
import model.CompanyRepresentative;
import model.Internship;
import model.Application;
import model.ApplicationStatus;
import model.InternshipStatus;
import model.Student;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import model.InternshipLevel;
import view.PasswordUI;

public class CompanyRepUI {
    private static final Scanner sc = new Scanner(System.in);
    private static final Database db = Database.getInstance();

    public static void showMenu(CompanyRepresentative rep) {
        while (true) {
            System.out.println("\n===== Company Representative Menu =====");
            System.out.println("1. Post a new Internship");
            System.out.println("2. View My Internships");
            System.out.println("3. Manage Applications");
            System.out.println("4. Toggle Visibility");
            System.out.println("5. Change Password");
            System.out.println("6. Logout");
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
                    manageApplications(rep);
                    break;
                case "4":
                    toggleVisibility(rep);
                    break;
                case "5":
                    PasswordUI.changePassword(rep);
                    return;
                case "6":
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void postInternship(CompanyRepresentative rep) {
        long currentCount = 0;
        for (Internship i : db.getInternships()) {
            if (i.getCompanyRepInCharge().equals(rep.getUserID())) {
                currentCount++;
            }
        }

        if (currentCount >= 5) {
            System.out.println("You have reached the maximum of 5 internships.");
            return;
        }

        System.out.println("\n--- Post New Internship ---");
        System.out.print("Enter Internship Title: ");
        String title = sc.nextLine();
        System.out.print("Enter Description: ");
        String desc = sc.nextLine();
        System.out.print("Preferred Major: ");
        String major = sc.nextLine();

        System.out.println("Select Internship Level:");
        System.out.println("1. Basic");
        System.out.println("2. Intermediate");
        System.out.println("3. Advanced");
        System.out.print("Enter choice: ");
        String levelInput = sc.nextLine();
        InternshipLevel level;

        if (levelInput.equals("1")) {
            level = InternshipLevel.BASIC;
        } else if (levelInput.equals("2")) {
            level = InternshipLevel.INTERMEDIATE;
        } else if (levelInput.equals("3")) {
            level = InternshipLevel.ADVANCED;
        } else {
            System.out.println("Invalid level. Defaulting to BASIC.");
            level = InternshipLevel.BASIC;
        }

        System.out.print("Number of slots: ");
        int slots = Integer.parseInt(sc.nextLine());

        LocalDate openDate = LocalDate.now();
        LocalDate closeDate = openDate.plusMonths(1);

        Internship internship = new Internship(
                title, desc, level, major,
                openDate, closeDate,
                rep.getCompanyName(), rep.getUserID(), slots
        );

        db.getInternships().add(internship);
        System.out.println("Internship posted successfully. Pending staff approval.");
        db.saveData();
    }

    private static void viewInternships(CompanyRepresentative rep) {
        System.out.println("\n--- My Internships ---");
        for (Internship i : db.getInternships()) {
            if (i.getCompanyRepInCharge().equals(rep.getUserID())) {
                System.out.println(i.getInternshipTitle() + " (" + i.getStatus() + ")");
            }
        }
    }

    private static void manageApplications(CompanyRepresentative rep) {
        System.out.println("\n--- Manage Applications ---");

        List<Internship> myInternships = new ArrayList<>();
        for (Internship i : db.getInternships()) {
            if (i.getCompanyRepInCharge().equals(rep.getUserID())) {
                myInternships.add(i);
            }
        }

        if (myInternships.isEmpty()) {
            System.out.println("You have no internships posted.");
            return;
        }

        for (int i = 0; i < myInternships.size(); i++) {
            Internship in = myInternships.get(i);
            System.out.println((i + 1) + ". " + in.getInternshipTitle() + " (" + in.getStatus() + ")");
        }

        System.out.print("Select an internship number: ");
        String input = sc.nextLine();
        if (input.isEmpty()) return;

        int idx;
        try {
            idx = Integer.parseInt(input) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        if (idx < 0 || idx >= myInternships.size()) {
            System.out.println("Invalid internship number.");
            return;
        }

        Internship chosen = myInternships.get(idx);

        List<Application> apps = new ArrayList<>();
        for (Application a : db.getApplications()) {
            if (a.getInternshipID() == chosen.getInternshipID()) {
                apps.add(a);
            }
        }

        if (apps.isEmpty()) {
            System.out.println("No applications for this internship yet.");
            return;
        }

        System.out.println("\nApplications for " + chosen.getInternshipTitle() + ":");
        for (Application a : apps) {
            Student s = (Student) db.findUserById(a.getStudentID());
            System.out.println("Application ID: " + a.getApplicationID() +
                    " | Student: " + s.getName() +
                    " | Status: " + a.getStatus());
        }

        System.out.print("Enter Application ID to approve/reject (or blank to go back): ");
        String choice = sc.nextLine();
        if (choice.isEmpty()) return;

        int appID;
        try {
            appID = Integer.parseInt(choice);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        Application target = null;
        for (Application a : apps) {
            if (a.getApplicationID() == appID) {
                target = a;
                break;
            }
        }

        if (target == null) {
            System.out.println("Invalid Application ID.");
            return;
        }

        System.out.print("Approve (A) or Reject (R)? ");
        String action = sc.nextLine().trim().toUpperCase();

        if (action.equals("A")) {
            target.setStatus(ApplicationStatus.SUCCESSFUL);
            System.out.println("Application approved.");

            int acceptedCount = 0;
            for (Application a : db.getApplications()) {
                if (a.getInternshipID() == chosen.getInternshipID() &&
                    a.getStatus() == ApplicationStatus.SUCCESSFUL) {
                    acceptedCount++;
                }
            }

            if (acceptedCount >= chosen.getSlots()) {
                chosen.setStatus(InternshipStatus.FILLED);
                System.out.println("Internship marked as FILLED (slots full).");
            }

        } else if (action.equals("R")) {
            target.setStatus(ApplicationStatus.UNSUCCESSFUL);
            System.out.println("Application rejected.");
        } else {
            System.out.println("Invalid input.");
        }

        db.saveData();
    }

    private static void toggleVisibility(CompanyRepresentative rep) {
        System.out.println("\n--- Toggle Internship Visibility ---");

        List<Internship> myInternships = new ArrayList<>();
        for (Internship i : db.getInternships()) {
            if (i.getCompanyRepInCharge().equals(rep.getUserID())) {
                myInternships.add(i);
            }
        }

        if (myInternships.isEmpty()) {
            System.out.println("You don’t have any internships posted yet.");
            return;
        }

        for (int i = 0; i < myInternships.size(); i++) {
            Internship in = myInternships.get(i);
            System.out.println((i + 1) + ". " + in.getInternshipTitle() +
                    " — Visible to students: " + (in.isVisible() ? "Yes" : "No"));
        }

        System.out.print("Enter the number of the internship to toggle: ");
        String input = sc.nextLine();
        if (input.isEmpty()) {
            System.out.println("No input given. Returning to menu...");
            return;
        }

        int index;
        try {
            index = Integer.parseInt(input) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
            return;
        }

        if (index < 0 || index >= myInternships.size()) {
            System.out.println("That number doesn’t match any of your internships.");
            return;
        }

        Internship chosen = myInternships.get(index);
        chosen.setVisible(!chosen.isVisible());

        System.out.println("Internship \"" + chosen.getInternshipTitle()
                + "\" is now " + (chosen.isVisible() ? "VISIBLE to students." : "HIDDEN from students."));

        db.saveData();
    }
}
