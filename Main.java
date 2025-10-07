import java.sql.*;
import java.util.*;

// Main entry
public class Main {
    // DB config - demo credentials
    private static final String URL = "jdbc:mysql://localhost:3306/nimbus?useSSL=false&serverTimezone=UTC";
    private static final String USER = "kapil_80042";
    private static final String PASS = "srvApp#2025!";


    public static void main(String[] args) {
        try {
            // Load driver (optional for modern JDBC, but explicit here)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Add connector jar to classpath.");
            return;
        }

        Scanner sc = new Scanner(System.in);
        int choice;
        do {
            System.out.println("\n--- Nimbus JDBC Demo ---");
            System.out.println("1. Part A: Fetch Employees (SELECT)");
            System.out.println("2. Part B: Product CRUD (INSERT/READ/UPDATE/DELETE)");
            System.out.println("3. Part C: Student Management (MVC style)");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");
            choice = readInt(sc);

            switch (choice) {
                case 1:
                    EmployeeFetcher.fetchAllEmployees();
                    break;
                case 2:
                    ProductMenu.run(sc);
                    break;
                case 3:
                    StudentApp.run(sc);
                    break;
                case 4:
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        } while (choice != 4);

        sc.close();
    }

    // safe int reader
    private static int readInt(Scanner sc) {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (Exception e) {
            return -1;
        }
    }

    // ---------------- Part A: Employee fetch ----------------
    static class EmployeeFetcher {
        public static void fetchAllEmployees() {
            String sql = "SELECT EmpID, Name, Salary FROM Employee";
            System.out.println("\n--- Fetching Employees ---");
            try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                System.out.printf("%-6s | %-20s | %-10s%n", "EmpID", "Name", "Salary");
                System.out.println("-------------------------------------------");
                while (rs.next()) {
                    int id = rs.getInt("EmpID");
                    String name = rs.getString("Name");
                    double sal = rs.getDouble("Salary");
                    System.out.printf("%-6d | %-20s | %-10.2f%n", id, name, sal);
                }
            } catch (SQLException e) {
                System.err.println("Error fetching employees: " + e.getMessage());
            }
        }
    }

    // ---------------- Part B: Product CRUD with transactions ----------------
    static class ProductMenu {
        public static void run(Scanner sc) {
            int ch;
            do {
                System.out.println("\n--- Product CRUD Menu ---");
                System.out.println("1. Create product");
                System.out.println("2. Read all products");
                System.out.println("3. Update product (Price & Quantity)");
                System.out.println("4. Delete product");
                System.out.println("5. Back to main menu");
                System.out.print("Enter choice: ");
                ch = readInt(sc);

                switch (ch) {
                    case 1:
                        insertProduct(sc);
                        break;
                    case 2:
                        listProducts();
                        break;
                    case 3:
                        updateProduct(sc);
                        break;
                    case 4:
                        deleteProduct(sc);
                        break;
                    case 5:
                        break;
                    default:
                        System.out.println("Invalid");
                }
            } while (ch != 5);
        }

        private static void insertProduct(Scanner sc) {
            System.out.print("Enter product name: ");
            String name = sc.nextLine().trim();
            System.out.print("Enter price: ");
            double price = Double.parseDouble(sc.nextLine().trim());
            System.out.print("Enter quantity: ");
            int qty = Integer.parseInt(sc.nextLine().trim());

            String sql = "INSERT INTO Product (ProductName, Price, Quantity) VALUES (?, ?, ?)";
            try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = con.prepareStatement(sql)) {

                con.setAutoCommit(false);
                ps.setString(1, name);
                ps.setDouble(2, price);
                ps.setInt(3, qty);
                int rows = ps.executeUpdate();
                con.commit();
                System.out.println("Inserted rows: " + rows);
            } catch (SQLException e) {
                System.err.println("Insert failed: " + e.getMessage());
            }
        }

        private static void listProducts() {
            String sql = "SELECT ProductID, ProductName, Price, Quantity FROM Product";
            try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                System.out.printf("%-8s | %-20s | %-8s | %-8s%n", "ProductID", "Name", "Price", "Qty");
                System.out.println("-----------------------------------------------------");
                while (rs.next()) {
                    System.out.printf("%-8d | %-20s | %-8.2f | %-8d%n",
                            rs.getInt("ProductID"),
                            rs.getString("ProductName"),
                            rs.getDouble("Price"),
                            rs.getInt("Quantity"));
                }
            } catch (SQLException e) {
                System.err.println("Read failed: " + e.getMessage());
            }
        }

        private static void updateProduct(Scanner sc) {
            System.out.print("Enter ProductID to update: ");
            int id = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Enter new price: ");
            double price = Double.parseDouble(sc.nextLine().trim());
            System.out.print("Enter new quantity: ");
            int qty = Integer.parseInt(sc.nextLine().trim());

            String sql = "UPDATE Product SET Price = ?, Quantity = ? WHERE ProductID = ?";
            try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = con.prepareStatement(sql)) {

                con.setAutoCommit(false);
                ps.setDouble(1, price);
                ps.setInt(2, qty);
                ps.setInt(3, id);
                int rows = ps.executeUpdate();
                if (rows == 1) {
                    con.commit();
                    System.out.println("Update successful.");
                } else {
                    con.rollback();
                    System.out.println("Update affected " + rows + " rows. Rolled back.");
                }
            } catch (SQLException e) {
                System.err.println("Update error: " + e.getMessage());
            }
        }

        private static void deleteProduct(Scanner sc) {
            System.out.print("Enter ProductID to delete: ");
            int id = Integer.parseInt(sc.nextLine().trim());

            String sql = "DELETE FROM Product WHERE ProductID = ?";
            try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = con.prepareStatement(sql)) {

                con.setAutoCommit(false);
                ps.setInt(1, id);
                int rows = ps.executeUpdate();
                if (rows == 1) {
                    con.commit();
                    System.out.println("Delete successful.");
                } else {
                    con.rollback();
                    System.out.println("Delete affected " + rows + " rows. Rolled back.");
                }
            } catch (SQLException e) {
                System.err.println("Delete failed: " + e.getMessage());
            }
        }
    }

    // ---------------- Part C: Student MVC ----------------
    // Model
    static class Student {
        private int studentID;
        private String name;
        private String department;
        private double marks;

        public Student(int studentID, String name, String department, double marks) {
            this.studentID = studentID;
            this.name = name;
            this.department = department;
            this.marks = marks;
        }

        public int getStudentID() { return studentID; }
        public String getName() { return name; }
        public String getDepartment() { return department; }
        public double getMarks() { return marks; }
    }

    // Controller / DAO
    static class StudentDAO {
        public void addStudent(Student s) throws SQLException {
            String sql = "INSERT INTO Student (StudentID, Name, Department, Marks) VALUES (?, ?, ?, ?)";
            try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, s.getStudentID());
                ps.setString(2, s.getName());
                ps.setString(3, s.getDepartment());
                ps.setDouble(4, s.getMarks());
                ps.executeUpdate();
            }
        }

        public List<Student> getAllStudents() throws SQLException {
            List<Student> list = new ArrayList<>();
            String sql = "SELECT StudentID, Name, Department, Marks FROM Student";
            try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(new Student(
                            rs.getInt("StudentID"),
                            rs.getString("Name"),
                            rs.getString("Department"),
                            rs.getDouble("Marks")
                    ));
                }
            }
            return list;
        }

        public boolean updateMarks(int id, double newMarks) throws SQLException {
            String sql = "UPDATE Student SET Marks = ? WHERE StudentID = ?";
            try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setDouble(1, newMarks);
                ps.setInt(2, id);
                return ps.executeUpdate() == 1;
            }
        }

        public boolean deleteStudent(int id) throws SQLException {
            String sql = "DELETE FROM Student WHERE StudentID = ?";
            try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() == 1;
            }
        }
    }

    // View (console UI)
    static class StudentApp {
        public static void run(Scanner sc) {
            StudentDAO dao = new StudentDAO();
            int choice;
            do {
                System.out.println("\n--- Student Management ---");
                System.out.println("1. Add Student");
                System.out.println("2. View Students");
                System.out.println("3. Update Student Marks");
                System.out.println("4. Delete Student");
                System.out.println("5. Back to main menu");
                System.out.print("Enter choice: ");
                choice = readInt(sc);

                try {
                    switch (choice) {
                        case 1:
                            System.out.print("Enter ID: ");
                            int id = Integer.parseInt(sc.nextLine().trim());
                            System.out.print("Enter Name: ");
                            String name = sc.nextLine().trim();
                            System.out.print("Enter Department: ");
                            String dept = sc.nextLine().trim();
                            System.out.print("Enter Marks: ");
                            double marks = Double.parseDouble(sc.nextLine().trim());
                            dao.addStudent(new Student(id, name, dept, marks));
                            System.out.println("Student added.");
                            break;
                        case 2:
                            List<Student> list = dao.getAllStudents();
                            System.out.printf("%-8s | %-15s | %-10s | %-6s%n", "StudentID", "Name", "Department", "Marks");
                            System.out.println("-------------------------------------------------");
                            for (Student s : list) {
                                System.out.printf("%-8d | %-15s | %-10s | %-6.2f%n", s.getStudentID(), s.getName(), s.getDepartment(), s.getMarks());
                            }
                            break;
                        case 3:
                            System.out.print("Enter Student ID: ");
                            int sid = Integer.parseInt(sc.nextLine().trim());
                            System.out.print("Enter new marks: ");
                            double nm = Double.parseDouble(sc.nextLine().trim());
                            boolean upd = dao.updateMarks(sid, nm);
                            System.out.println(upd ? "Updated." : "No record updated.");
                            break;
                        case 4:
                            System.out.print("Enter Student ID to delete: ");
                            int did = Integer.parseInt(sc.nextLine().trim());
                            boolean del = dao.deleteStudent(did);
                            System.out.println(del ? "Deleted." : "No record deleted.");
                            break;
                        case 5:
                            break;
                        default:
                            System.out.println("Invalid");
                    }
                } catch (SQLException e) {
                    System.err.println("DB error: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Input error: " + e.getMessage());
                }
            } while (choice != 5);
        }
    }
}
