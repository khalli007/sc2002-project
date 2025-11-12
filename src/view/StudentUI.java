package view;

import controller.Database;
import model.Student;
import model.Internship;
import model.InternshipLevel;
import model.InternshipStatus;
import model.Application;
import model.ApplicationStatus;
import model.WithdrawalStatus;
import view.PasswordUI;
import java.util.List;
import java.util.Scanner;

/**
 * StudentUI - Handles all menus and interactions for Student users.
 */
public class StudentUI {
    private static final Scanner sc = new Scanner(System.in);
    private static final Database db = Database.getInstance();

    public static void showMenu(Student student) {
        while (true) {
            System.out.println("\n===== Student Menu =====");
            System.out.println("Welcome, " + student.getName() + " (" + student.getMajor() + ", Year " + student.getYear() + ")");
            System.out.println("1. View Available Internships");
            System.out.println("2. Apply for Internship");
            System.out.println("3. View My Applications");
            System.out.println("4. Change Password");
            System.out.println("5. Logout");
            System.out.print("Enter choice: ");

            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    viewInternships(student);
                    break;
                case "2":
                    applyInternship(student);
                    break;
                case "3":
                    viewApplications(student);
                    break;
                case "4":
                    PasswordUI.changePassword(student);
                    return;
                case "5":
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void viewInternships(Student student) {
        System.out.println("\n--- Available Internships ---");
        List<Internship> internships = db.getInternships();
        boolean found = false;

        for (Internship i : internships) {
            if (i.getStatus() == InternshipStatus.APPROVED && i.isVisible()) {
                boolean levelAllowed = (student.getYear() >= 3) || (student.getYear() <= 2 && i.getLevel() == InternshipLevel.BASIC);
                boolean majorMatch = i.getPreferredMajor().equalsIgnoreCase(student.getMajor());

                if (levelAllowed && majorMatch) {
                    System.out.println("[" + i.getInternshipID() + "] " + i.getInternshipTitle() + " (" + i.getCompanyName() + ") - Level: " + i.getLevel());
                    found = true;
                }
            }
        }

        if (!found) {
            System.out.println("No internships available currently.");
        }
    }

    private static void applyInternship(Student student) {
        System.out.println("\n--- Apply for Internship ---");

        long activeApps = db.getApplicationsByStudent(student.getUserID()).stream()
                .filter(a -> a.getWithdrawalStatus() != WithdrawalStatus.APPROVED)
                .count();

        if (activeApps >= 3) {
            System.out.println("You already have 3 active applications.");
            return;
        }

        System.out.print("Enter Internship ID to apply: ");
        String input = sc.nextLine();
        int internshipID;

        try {
            internshipID = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        Internship internship = db.findInternshipById(internshipID);
        if (internship == null) {
            System.out.println("Invalid Internship ID.");
            return;
        }

        switch (internship.getStatus()) {
            case APPROVED:
                if (!internship.isVisible()) {
                    System.out.println("This internship is not open for applications.");
                    return;
                }
                break;
            case FILLED:
                System.out.println("This internship is already filled.");
                return;
            default:
                System.out.println("This internship is not open for applications.");
                return;
        }

        if (student.getYear() <= 2 && internship.getLevel() != InternshipLevel.BASIC) {
            System.out.println("You can only apply for Basic-level internships.");
            return;
        }

        boolean alreadyApplied = false;
        for (Application a : db.getApplicationsByStudent(student.getUserID())) {
            if (a.getInternshipID() == internshipID && a.getWithdrawalStatus() != WithdrawalStatus.APPROVED) {
                alreadyApplied = true;
                break;
            }
        }

        if (alreadyApplied) {
            System.out.println("You have already applied for this internship.");
            return;
        }

        Application newApp = new Application(student.getUserID(), internshipID);
        db.addApplication(newApp);
        System.out.println("Application submitted successfully! Status: PENDING");
        db.saveData();
    }

    private static void viewApplications(Student student) {
        System.out.println("\n--- My Internship Applications ---");
        List<Application> apps = db.getApplicationsByStudent(student.getUserID());

        if (apps.isEmpty()) {
            System.out.println("You have not applied for any internships yet.");
            return;
        }

        for (Application app : apps) {
            Internship i = db.findInternshipById(app.getInternshipID());
            String internshipInfo = (i == null) ? "[Internship deleted]" : i.getInternshipTitle() + " (" + i.getCompanyName() + ")";
            System.out.println("Application ID: " + app.getApplicationID());
            System.out.println("    Internship: " + internshipInfo);
            System.out.println("    Status    : " + app.getStatus());
            System.out.println("    Withdrawal: " + app.getWithdrawalStatus());
            System.out.println("    Accepted  : " + (app.isAccepted() ? "Yes" : "No"));
            System.out.println("-----------------------------------");
        }

        System.out.println("1. Accept Successful Internship");
        System.out.println("2. Request Withdrawal");
        System.out.println("3. Back");
        System.out.print("Enter choice: ");

        String choice = sc.nextLine();
        switch (choice) {
            case "1":
                acceptInternship(student);
                break;
            case "2":
                requestWithdrawal(student);
                break;
            case "3":
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void acceptInternship(Student student) {
        List<Application> allApps = db.getApplicationsByStudent(student.getUserID());
        boolean found = false;

        for (Application app : allApps) {
            if (app.getStatus() == ApplicationStatus.SUCCESSFUL && !app.isAccepted()) {
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("You have no successful applications to accept.");
            return;
        }

        System.out.print("Enter Application ID to accept: ");
        String input = sc.nextLine();
        int appID;

        try {
            appID = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        Application chosen = null;
        for (Application a : allApps) {
            if (a.getApplicationID() == appID && a.getStatus() == ApplicationStatus.SUCCESSFUL) {
                chosen = a;
                break;
            }
        }

        if (chosen == null) {
            System.out.println("Invalid Application ID.");
            return;
        }

        chosen.setAccepted(true);
        chosen.setWithdrawalStatus(WithdrawalStatus.NONE);
        System.out.println("Internship accepted successfully!");

        for (Application other : allApps) {
            if (other.getApplicationID() != chosen.getApplicationID()) {
                other.setStatus(ApplicationStatus.UNSUCCESSFUL);
                other.setWithdrawalStatus(WithdrawalStatus.APPROVED);
                other.setAccepted(false);
            }
        }

        db.saveData();
    }

    private static void requestWithdrawal(Student student) {
        List<Application> apps = db.getApplicationsByStudent(student.getUserID());

        System.out.print("Enter Application ID to request withdrawal: ");
        String input = sc.nextLine();
        int appID;

        try {
            appID = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        Application selected = null;
        for (Application a : apps) {
            if (a.getApplicationID() == appID) {
                selected = a;
                break;
            }
        }

        if (selected == null) {
            System.out.println("Application not found.");
            return;
        }

        switch (selected.getWithdrawalStatus()) {
            case APPROVED:
                System.out.println("This application has already been withdrawn.");
                return;
            case REQUESTED:
                System.out.println("You have already requested withdrawal for this application.");
                return;
            default:
                selected.setWithdrawalStatus(WithdrawalStatus.REQUESTED);
                System.out.println("Withdrawal request submitted (awaiting staff approval).");
                db.saveData();
                break;
        }
    }
}