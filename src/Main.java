import services.DocumentManager;
import models.Document;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        DocumentManager manager = new DocumentManager();

        while (true) {
            System.out.println("\n===== DOCUMENT VERSION CONTROL SYSTEM =====");
            System.out.println("1. Add Document");
            System.out.println("2. List Documents");
            System.out.println("3. Edit Document");
            System.out.println("4. View Version History");
            System.out.println("5. Restore Version");
            System.out.println("6. Show Diff Between Versions");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter document type (TXT/PDF/DOCX/PPT): ");
                    String type = sc.nextLine();
                    System.out.print("Enter file path (with name and extension): ");
                    String path = sc.nextLine();
                    manager.createDocument(type, path);
                    break;

                case 2:
                    manager.listDocuments();
                    break;

                case 3:
                    System.out.print("Enter file path to edit: ");
                    String ePath = sc.nextLine();
                    manager.editDocument(ePath);
                    break;

                case 4:
                    System.out.print("Enter file path to view versions: ");
                    String vPath = sc.nextLine();
                    manager.viewVersionHistory(vPath);
                    break;

                case 5:
                    System.out.print("Enter file path to restore: ");
                    String rPath = sc.nextLine();
                    System.out.print("Enter version number to restore: ");
                    int rVersion = sc.nextInt();
                    sc.nextLine();
                    manager.restoreVersion(rPath, rVersion);
                    break;

                case 6:
                    System.out.print("Enter file path: ");
                    String dPath = sc.nextLine();
                    System.out.print("Enter first version number: ");
                    int v1 = sc.nextInt();
                    System.out.print("Enter second version number: ");
                    int v2 = sc.nextInt();
                    sc.nextLine();
                    manager.showDiff(dPath, v1, v2);
                    break;

                case 7:
                    System.out.println("Exiting...");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}

