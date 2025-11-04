package view;

import controller.Database;
import model.Application;
import model.ApplicationStatus;
import model.CareerCenterStaff;
import model.CompanyRepresentative;
import model.Internship;
import model.InternshipLevel;
import model.InternshipStatus;
import model.WithdrawalStatus;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Boundary class for the Career Center Staff workflows.
 */
public final class StaffUI {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final Database DB = Database.getInstance();

    private StaffUI() {
    }

    public static void showStaffMenu(CareerCenterStaff staff) {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n=== Career Center Staff Dashboard ===");
            System.out.println("Hello, " + staff.getName() + " (" + staff.getStaffDepartment() + ")");
            System.out.println("1. Review Company Representative Registrations");
            System.out.println("2. Review Internship Submissions");
            System.out.println("3. Manage Withdrawal Requests");
            System.out.println("4. Generate Internship Reports");
            System.out.println("5. Change Password");
            System.out.println("6. Logout");
            int choice = readInt("Select an option: ");

            switch (choice) {
                case 1 -> reviewCompanyRepresentatives();
                case 2 -> reviewInternships();
                case 3 -> manageWithdrawals();
                case 4 -> generateReports();
                case 5 -> PasswordUI.changePassword(staff);
                case 6 -> exit = true;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void reviewCompanyRepresentatives() {
        List<CompanyRepresentative> pending = DB.getPendingCompanyRepresentatives();
        if (pending.isEmpty()) {
            System.out.println("There are no pending representative registrations.");
            return;
        }

        boolean back = false;
        while (!back) {
            System.out.println("\n--- Pending Company Representatives ---");
            for (int i = 0; i < pending.size(); i++) {
                CompanyRepresentative rep = pending.get(i);
                System.out.println((i + 1) + ". " + rep.getName() + " (" + rep.getUserID() + ") - "
                        + rep.getCompanyName() + ", " + rep.getDepartment() + ", " + rep.getPosition());
            }
            System.out.println("0. Back");
            int choice = readInt("Select an entry to review: ");
            if (choice == 0) {
                back = true;
            } else if (choice > 0 && choice <= pending.size()) {
                CompanyRepresentative rep = pending.get(choice - 1);
                handleRepresentativeDecision(rep);
                pending = DB.getPendingCompanyRepresentatives();
                if (pending.isEmpty()) {
                    System.out.println("All pending representatives have been processed.");
                    back = true;
                }
            } else {
                System.out.println("Invalid selection.");
            }
        }
    }

    private static void handleRepresentativeDecision(CompanyRepresentative rep) {
        System.out.println("\n--- Representative Details ---");
        System.out.println("Name     : " + rep.getName());
        System.out.println("Email    : " + rep.getUserID());
        System.out.println("Company  : " + rep.getCompanyName());
        System.out.println("Department: " + rep.getDepartment());
        System.out.println("Position : " + rep.getPosition());

        System.out.println("Approve this representative? (A=Approve, R=Reject, Other=Back)");
        String input = SCANNER.nextLine().trim().toUpperCase();
        if (input.equals("A")) {
            rep.setApproved(true);
            System.out.println("Representative approved. They may now log in.");
        } else if (input.equals("R")) {
            removeRepresentative(rep);
            System.out.println("Representative rejected and removed from the system.");
        } else {
            System.out.println("No changes made.");
        }
    }

    private static void removeRepresentative(CompanyRepresentative rep) {
        List<Internship> internships = new ArrayList<>(DB.getInternshipsByCompanyRep(rep.getUserID()));
        for (Internship internship : internships) {
            removeInternshipWithApplications(internship);
        }
        DB.removeUser(rep);
    }

    private static void reviewInternships() {
        List<Internship> pending = DB.getInternships().stream()
                .filter(internship -> internship.getStatus() == InternshipStatus.PENDING)
                .sorted(Comparator.comparing(Internship::getInternshipTitle, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        if (pending.isEmpty()) {
            System.out.println("There are no pending internship submissions.");
            return;
        }

        boolean back = false;
        while (!back) {
            System.out.println("\n--- Pending Internships ---");
            for (int i = 0; i < pending.size(); i++) {
                Internship internship = pending.get(i);
                System.out.println((i + 1) + ". [" + internship.getInternshipID() + "] " + internship.getInternshipTitle()
                        + " by " + internship.getCompanyName());
            }
            System.out.println("0. Back");
            int choice = readInt("Select an internship to review: ");
            if (choice == 0) {
                back = true;
            } else if (choice > 0 && choice <= pending.size()) {
                Internship internship = pending.get(choice - 1);
                handleInternshipDecision(internship);
                pending = DB.getInternships().stream()
                        .filter(i -> i.getStatus() == InternshipStatus.PENDING)
                        .sorted(Comparator.comparing(Internship::getInternshipTitle, String.CASE_INSENSITIVE_ORDER))
                        .collect(Collectors.toList());
                if (pending.isEmpty()) {
                    System.out.println("All pending internships have been processed.");
                    back = true;
                }
            } else {
                System.out.println("Invalid selection.");
            }
        }
    }

    private static void handleInternshipDecision(Internship internship) {
        System.out.println("\n--- Internship Submission ---");
        System.out.println("Title       : " + internship.getInternshipTitle());
        System.out.println("Company     : " + internship.getCompanyName());
        System.out.println("Level       : " + internship.getLevel());
        System.out.println("Major       : " + internship.getPreferredMajor());
        System.out.println("Slots       : " + internship.getSlots());
        System.out.println("Opening Date: " + internship.getOpeningDate());
        System.out.println("Closing Date: " + internship.getClosingDate());
        System.out.println("Description : " + internship.getDescription());

        System.out.println("Approve this internship? (A=Approve, R=Reject, Other=Back)");
        String input = SCANNER.nextLine().trim().toUpperCase();
        if (input.equals("A")) {
            internship.setStatus(InternshipStatus.APPROVED);
            System.out.println("Internship approved. Company representative may now publish it.");
        } else if (input.equals("R")) {
            internship.setStatus(InternshipStatus.REJECTED);
            internship.setVisible(false);
            System.out.println("Internship rejected.");
        } else {
            System.out.println("No changes made.");
        }
    }

    private static void manageWithdrawals() {
        List<Application> requests = DB.getApplications().stream()
                .filter(app -> app.getWithdrawalStatus() == WithdrawalStatus.REQUESTED)
                .collect(Collectors.toList());
        if (requests.isEmpty()) {
            System.out.println("There are no pending withdrawal requests.");
            return;
        }

        boolean back = false;
        while (!back) {
            System.out.println("\n--- Pending Withdrawal Requests ---");
            for (int i = 0; i < requests.size(); i++) {
                Application app = requests.get(i);
                Internship internship = DB.findInternshipById(app.getInternshipID());
                System.out.println((i + 1) + ". Application " + app.getApplicationID()
                        + " by student " + app.getStudentID()
                        + " for " + (internship == null ? "[Deleted Internship]" : internship.getInternshipTitle()));
            }
            System.out.println("0. Back");
            int choice = readInt("Select a request to review: ");
            if (choice == 0) {
                back = true;
            } else if (choice > 0 && choice <= requests.size()) {
                Application application = requests.get(choice - 1);
                handleWithdrawalDecision(application);
                requests = DB.getApplications().stream()
                        .filter(app -> app.getWithdrawalStatus() == WithdrawalStatus.REQUESTED)
                        .collect(Collectors.toList());
                if (requests.isEmpty()) {
                    System.out.println("All withdrawal requests have been processed.");
                    back = true;
                }
            } else {
                System.out.println("Invalid selection.");
            }
        }
    }

    private static void handleWithdrawalDecision(Application application) {
        Internship internship = DB.findInternshipById(application.getInternshipID());
        System.out.println("\n--- Withdrawal Request ---");
        System.out.println("Application ID : " + application.getApplicationID());
        System.out.println("Student ID     : " + application.getStudentID());
        System.out.println("Current Status : " + application.getStatus());
        System.out.println("Accepted       : " + (application.isAccepted() ? "Yes" : "No"));
        System.out.println("Internship     : " + (internship == null ? "[Deleted]" : internship.getInternshipTitle()));

        System.out.println("Approve this withdrawal? (A=Approve, R=Reject, Other=Back)");
        String input = SCANNER.nextLine().trim().toUpperCase();
        if (input.equals("A")) {
            application.setWithdrawalStatus(WithdrawalStatus.APPROVED);
            application.setAccepted(false);
            application.setStatus(ApplicationStatus.UNSUCCESSFUL);
            if (internship != null) {
                DB.updateInternshipFilledStatus(internship);
            }
            System.out.println("Withdrawal approved.");
        } else if (input.equals("R")) {
            application.setWithdrawalStatus(WithdrawalStatus.REJECTED);
            System.out.println("Withdrawal rejected.");
        } else {
            System.out.println("No changes made.");
        }
    }

    private static void generateReports() {
        System.out.println("\n--- Internship Report Filters ---");
        InternshipStatus status = promptStatus();
        InternshipLevel level = promptLevel();
        System.out.print("Filter by preferred major (leave blank for all): ");
        String major = SCANNER.nextLine().trim();
        if (major.isEmpty()) {
            major = null;
        }
        System.out.print("Filter by company keyword (leave blank for all): ");
        String companyKeyword = SCANNER.nextLine().trim();
        if (companyKeyword.isEmpty()) {
            companyKeyword = null;
        }
        LocalDate closingBefore = promptDate("Filter by closing date before (YYYY-MM-DD) or leave blank: ");

        final String majorFilter = major;
        final String companyFilter = companyKeyword;
        final LocalDate closingFilter = closingBefore;

        List<Internship> internships = DB.getInternships().stream()
                .filter(i -> status == null || i.getStatus() == status)
                .filter(i -> level == null || i.getLevel() == level)
                .filter(i -> majorFilter == null || i.getPreferredMajor().equalsIgnoreCase(majorFilter))
                .filter(i -> companyFilter == null || i.getCompanyName().toLowerCase().contains(companyFilter.toLowerCase()))
                .filter(i -> closingFilter == null || (i.getClosingDate() != null && !i.getClosingDate().isAfter(closingFilter)))
                .sorted(Comparator.comparing(Internship::getInternshipTitle, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        if (internships.isEmpty()) {
            System.out.println("No internships matched the selected filters.");
            return;
        }

        System.out.println("\n=== Internship Report ===");
        for (Internship internship : internships) {
            List<Application> apps = DB.getApplicationsByInternship(internship.getInternshipID());
            long pendingCount = apps.stream().filter(app -> app.getStatus() == ApplicationStatus.PENDING).count();
            long successfulCount = apps.stream().filter(app -> app.getStatus() == ApplicationStatus.SUCCESSFUL).count();
            long unsuccessfulCount = apps.stream().filter(app -> app.getStatus() == ApplicationStatus.UNSUCCESSFUL).count();
            long acceptedCount = DB.countAcceptedForInternship(internship.getInternshipID());
            long withdrawalPending = apps.stream().filter(app -> app.getWithdrawalStatus() == WithdrawalStatus.REQUESTED).count();

            System.out.println("[" + internship.getInternshipID() + "] " + internship.getInternshipTitle());
            System.out.println("    Company     : " + internship.getCompanyName());
            System.out.println("    Status      : " + internship.getStatus());
            System.out.println("    Level       : " + internship.getLevel());
            System.out.println("    Preferred Major: " + internship.getPreferredMajor());
            System.out.println("    Slots       : " + internship.getSlots());
            System.out.println("    Closing Date: " + internship.getClosingDate());
            System.out.println("    Visible     : " + (internship.isVisible() ? "Yes" : "No"));
            System.out.println("    Accepted    : " + acceptedCount);
            System.out.println("    Applications: total=" + apps.size()
                    + ", pending=" + pendingCount
                    + ", successful=" + successfulCount
                    + ", unsuccessful=" + unsuccessfulCount);
            System.out.println("    Withdrawal Requests Pending: " + withdrawalPending);
        }

        Map<InternshipStatus, Long> byStatus = internships.stream()
                .collect(Collectors.groupingBy(Internship::getStatus, Collectors.counting()));
        System.out.println("\nSummary by Status:");
        byStatus.forEach((s, count) -> System.out.println("  " + s + ": " + count));
    }

    private static InternshipStatus promptStatus() {
        System.out.print("Filter by status (Pending/Approved/Rejected/Filled or blank): ");
        String input = SCANNER.nextLine().trim();
        if (input.isEmpty()) {
            return null;
        }
        try {
            return InternshipStatus.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid status. Ignoring filter.");
            return null;
        }
    }

    private static InternshipLevel promptLevel() {
        System.out.print("Filter by level (Basic/Intermediate/Advanced or blank): ");
        String input = SCANNER.nextLine().trim();
        if (input.isEmpty()) {
            return null;
        }
        try {
            return InternshipLevel.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid level. Ignoring filter.");
            return null;
        }
    }

    private static LocalDate promptDate(String message) {
        System.out.print(message);
        String input = SCANNER.nextLine().trim();
        if (input.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date. Ignoring filter.");
            return null;
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine();
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static void removeInternshipWithApplications(Internship internship) {
        List<Application> applications = new ArrayList<>(DB.getApplicationsByInternship(internship.getInternshipID()));
        for (Application application : applications) {
            DB.removeApplication(application);
        }
        DB.removeInternship(internship);
    }
}
