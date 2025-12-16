
// TeacherDashboard.java
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class TeacherDashboard extends JFrame {

    private DataStore.Teacher teacher;

    // ===== ICONS (same style as AdminDashboard & StudentDashboard) =====
    private final ImageIcon icLogo = loadScaledIcon("Pic/Teacher.png", 180, 120);

    private final ImageIcon icAttendance = loadScaledIcon("Pic/markattence.png", 90, 90);
    private final ImageIcon icMarks = loadScaledIcon("Pic/enter marks.png", 90, 90);
    private final ImageIcon icStudents = loadScaledIcon("Pic/Studnet.png", 90, 90);
    private final ImageIcon icClasses = loadScaledIcon("Pic/view class.png", 90, 90);
    private final ImageIcon icPerformance = loadScaledIcon("Pic/Pro.png", 90, 90);
    private final ImageIcon icStudentAttendance = loadScaledIcon("Pic/markattence.png", 90, 90);
    private final ImageIcon icPassword = loadScaledIcon("Pic/Ch.png", 90, 90);
    private final ImageIcon icLogout = loadScaledIcon("Pic/logout.jpg", 90, 90);

    public TeacherDashboard(String teacherId) {

        teacher = DataStore.getTeacherById(teacherId);
        if (teacher == null) {
            JOptionPane.showMessageDialog(null, "Teacher not found");
            return;
        }

        setTitle("Teacher Dashboard - " + teacher.name);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== TOP PANEL (Logo + Title) =====
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel lblTitle = new JLabel("Teacher Dashboard", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        top.add(lblTitle, BorderLayout.NORTH);

        if (icLogo != null) {
            JLabel lblLogo = new JLabel(icLogo, SwingConstants.CENTER);
            lblLogo.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
            top.add(lblLogo, BorderLayout.SOUTH);
        }

        add(top, BorderLayout.NORTH);

        // ===== BUTTON GRID =====
        JPanel panel = new JPanel(new GridLayout(0, 4, 18, 18));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        panel.add(createBtn("Mark Attendance", icAttendance, e -> markAttendance()));
        panel.add(createBtn("Enter Marks", icMarks, e -> enterMarks()));
        panel.add(createBtn("View My Students", icStudents, e -> viewMyStudents()));
        panel.add(createBtn("View Assigned Classes", icClasses, e -> viewAssigned()));
        panel.add(createBtn("Student Performance", icPerformance, e -> viewStudentPerformance()));
        panel.add(createBtn("Student Attendance", icStudentAttendance, e -> viewStudentAttendance()));
        panel.add(createBtn("Change Password", icPassword, e -> changePassword()));

        // ===== LOGOUT BUTTON =====
        JButton logout = createBtn("Logout", icLogout, e -> {
            new LoginScreen();
            dispose();
        });

        add(panel, BorderLayout.CENTER);
        add(logout, BorderLayout.SOUTH);

        setVisible(true);
    }

    // ========= Reusable Tile Button ==========
    private JButton createBtn(String text, ImageIcon icon, java.awt.event.ActionListener al) {
        JButton b = new JButton("<html><center><b>" + text + "</b></center></html>");
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));

        if (icon != null) {
            b.setIcon(icon);
            b.setHorizontalTextPosition(SwingConstants.CENTER);
            b.setVerticalTextPosition(SwingConstants.BOTTOM);
        }

        b.setPreferredSize(new Dimension(240, 140));
        b.addActionListener(al);

        // Hover effect
        Color normal = b.getBackground();
        Color hover = new Color(230, 230, 230);

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(hover);
                b.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(normal);
                b.setBorder(UIManager.getBorder("Button.border"));
            }
        });

        return b;
    }

    // ========= ICON LOADER (same for all dashboards) ==========
    private ImageIcon loadScaledIcon(String path, int w, int h) {
        try {
            java.net.URL u = getClass().getClassLoader().getResource(path);
            if (u != null) {
                ImageIcon original = new ImageIcon(u);
                Image scaled = original.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            } else {
                System.out.println("Missing icon: " + path);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    // ===================== Mark Attendance =====================
    private void markAttendance() {
        // Get classes assigned to this teacher
        String[] classNames = teacher.assignedClassSubjects.keySet().toArray(new String[0]);
        if (classNames.length == 0) {
            JOptionPane.showMessageDialog(this, "No assigned classes");
            return;
        }

        // Teacher selects class
        String cls = (String) JOptionPane.showInputDialog(
                this, "Select Class:", "Mark Attendance",
                JOptionPane.QUESTION_MESSAGE, null, classNames, classNames[0]);
        if (cls == null)
            return;

        DataStore.ClassRoom cr = DataStore.getClassByName(cls);
        if (cr == null || cr.studentIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students in this class");
            return;
        }

        String date = DataStore.todayDate(); // format "yyyy-MM-dd" or your preferred format

        // Iterate students
        for (String sid : cr.studentIds) {
            DataStore.Student s = DataStore.getStudentById(sid);
            if (s == null)
                continue;

            String currentStatus = s.attendance.get(date); // get today's attendance if exists

            // If attendance already marked, ask to overwrite
            if (currentStatus != null) {
                int overwrite = JOptionPane.showConfirmDialog(
                        this,
                        "Attendance for " + s.name + " (" + s.id + ") already exists for today: " + currentStatus
                                + "\nOverwrite?",
                        "Already Exists",
                        JOptionPane.YES_NO_OPTION);
                if (overwrite != JOptionPane.YES_OPTION)
                    continue;
            }

            // Ask teacher to mark present or absent
            int res = JOptionPane.showOptionDialog(
                    this,
                    "Mark attendance for " + s.name + " (" + s.id + "):",
                    "Attendance",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[] { "Present", "Absent" },
                    "Present");

            if (res == JOptionPane.CLOSED_OPTION)
                continue;

            String val = (res == 0) ? "P" : "A";

            // Save attendance
            DataStore.markAttendanceForStudent(s.id, date, val);
        }

        JOptionPane.showMessageDialog(this, "Attendance saved for " + date);
    }

    // ===================== Enter Marks =====================
    private void enterMarks() {
        String[] classNames = teacher.assignedClassSubjects.keySet().toArray(new String[0]);
        if (classNames.length == 0) {
            JOptionPane.showMessageDialog(this, "No assigned classes");
            return;
        }

        String cls = (String) JOptionPane.showInputDialog(this, "Select Class:", "Enter Marks",
                JOptionPane.QUESTION_MESSAGE, null, classNames, null);
        if (cls == null)
            return;

        List<String> subs = teacher.assignedClassSubjects.getOrDefault(cls, new ArrayList<>());
        if (subs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No subjects assigned for this class");
            return;
        }

        String sub = (String) JOptionPane.showInputDialog(this, "Select Subject:", "Subject",
                JOptionPane.QUESTION_MESSAGE, null, subs.toArray(), null);
        if (sub == null)
            return;

        DataStore.ClassRoom cr = DataStore.getClassByName(cls);
        if (cr == null || cr.studentIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students in class");
            return;
        }

        for (String sid : cr.studentIds) {
            DataStore.Student s = DataStore.getStudentById(sid);
            if (s == null)
                continue;

            boolean valid = false;
            while (!valid) {
                String input = JOptionPane.showInputDialog(this,
                        "Enter marks (0-100) for " + s.name + " in " + sub + " (Cancel skips):");
                if (input == null)
                    break; // cancel, skip student

                try {
                    int m = Integer.parseInt(input.trim());
                    if (m < 0 || m > 100) {
                        JOptionPane.showMessageDialog(this, "Invalid mark! Must be between 0 and 100.");
                    } else {
                        DataStore.setMarkForStudent(s.id, sub, m);
                        valid = true; // exit loop
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input! Please enter an integer.");
                }
            }
        }

        JOptionPane.showMessageDialog(this, "Marks entry complete");
    }

    // ===================== View My Students (JTable) =====================
    private void viewMyStudents() {
        // Get assigned classes for the teacher
        String[] classNames = teacher.assignedClassSubjects.keySet().toArray(new String[0]);
        if (classNames.length == 0) {
            JOptionPane.showMessageDialog(this, "No assigned classes");
            return;
        }

        // Select a class
        String cls = (String) JOptionPane.showInputDialog(
                this, "Select Class:", "View Students",
                JOptionPane.QUESTION_MESSAGE, null, classNames, classNames[0]);
        if (cls == null)
            return;

        DataStore.ClassRoom cr = DataStore.getClassByName(cls);
        if (cr == null || cr.studentIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students in this class");
            return;
        }

        // Prepare data for JTable
        String[] cols = { "ID", "Name", "Attendance %", "Marks" };
        Object[][] data = new Object[cr.studentIds.size()][4];
        int i = 0;
        for (String sid : cr.studentIds) {
            DataStore.Student s = DataStore.getStudentById(sid);
            if (s == null)
                continue;

            data[i][0] = s.id;
            data[i][1] = s.name;
            data[i][2] = String.format("%.2f%%", s.getAttendancePercentage());

            if (s.marks.isEmpty()) {
                data[i][3] = "No Marks";
            } else {
                StringBuilder marksStr = new StringBuilder();
                for (Map.Entry<String, Integer> entry : s.marks.entrySet()) {
                    marksStr.append(entry.getKey()).append(":").append(entry.getValue()).append(", ");
                }
                // Remove trailing comma
                data[i][3] = marksStr.substring(0, marksStr.length() - 2);
            }
            i++;
        }

        // JTable
        JTable table = new JTable(data, cols);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(table);

        // Print button
        JButton printBtn = new JButton("🖨 Print Students");
        printBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        printBtn.addActionListener(e -> {
            try {
                table.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Print Failed: " + ex.getMessage());
            }
        });
        JPanel btnPanel = new JPanel();
        btnPanel.add(printBtn);

        // Main Panel
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JLabel title = new JLabel("Students of Class: " + cls, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        // Show dialog
        JDialog dialog = new JDialog(this, "Students of Class", true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void viewAssigned() {
        String[] cols = { "Class", "Subjects" };
        Object[][] data;

        if (teacher.assignedClassSubjects.isEmpty()) {
            data = new Object[][] { { "No assigned classes", "" } };
        } else {
            data = new Object[teacher.assignedClassSubjects.size()][2];
            int i = 0;
            for (Map.Entry<String, List<String>> en : teacher.assignedClassSubjects.entrySet()) {
                data[i][0] = en.getKey();
                data[i][1] = String.join(", ", en.getValue());
                i++;
            }
        }

        JTable table = new JTable(data, cols);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JScrollPane scroll = new JScrollPane(table);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JLabel title = new JLabel("Assigned Classes", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        JDialog dialog = new JDialog(this, "Assigned Classes", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void viewStudentPerformance() {
        String[] opts = { "View Marks (by student ID)", "View Attendance (by student ID)", "Back" };
        while (true) {
            int sel = JOptionPane.showOptionDialog(this, "Student Performance", "Performance",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);

            if (sel == 0) { // Marks
                String id = JOptionPane.showInputDialog(this, "Enter Student ID:");
                if (id == null || id.trim().isEmpty())
                    continue;

                DataStore.Student s = DataStore.getStudentById(id.trim());
                if (s == null) {
                    JOptionPane.showMessageDialog(this, "Student not found");
                    continue;
                }

                String[] cols = { "Subject", "Marks" };
                Object[][] data;
                if (s.marks.isEmpty()) {
                    data = new Object[][] { { "No marks recorded", "" } };
                } else {
                    data = new Object[s.marks.size()][2];
                    int i = 0;
                    for (Map.Entry<String, Integer> e : s.marks.entrySet()) {
                        data[i][0] = e.getKey();
                        data[i][1] = e.getValue();
                        i++;
                    }
                }

                JTable table = new JTable(data, cols);
                table.setRowHeight(25);
                table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
                table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                JScrollPane scroll = new JScrollPane(table);

                JPanel panel = new JPanel(new BorderLayout(10, 10));
                JLabel title = new JLabel("Marks for " + s.name + " (" + s.id + ")", SwingConstants.CENTER);
                title.setFont(new Font("Segoe UI", Font.BOLD, 18));
                panel.add(title, BorderLayout.NORTH);
                panel.add(scroll, BorderLayout.CENTER);

                JDialog dialog = new JDialog(this, "Student Marks", true);
                dialog.setSize(500, 400);
                dialog.setLocationRelativeTo(this);
                dialog.add(panel);
                dialog.setVisible(true);

            } else if (sel == 1) { // Attendance
                String id = JOptionPane.showInputDialog(this, "Enter Student ID:");
                if (id == null || id.trim().isEmpty())
                    continue;

                DataStore.Student s = DataStore.getStudentById(id.trim());
                if (s == null) {
                    JOptionPane.showMessageDialog(this, "Student not found");
                    continue;
                }

                String[] cols = { "Date", "Status" };
                Object[][] data;
                if (s.attendance.isEmpty()) {
                    data = new Object[][] { { "No attendance recorded", "" } };
                } else {
                    data = new Object[s.attendance.size()][2];
                    int i = 0;
                    for (Map.Entry<String, String> e : s.attendance.entrySet()) {
                        data[i][0] = e.getKey();
                        data[i][1] = e.getValue();
                        i++;
                    }
                }

                JTable table = new JTable(data, cols);
                table.setRowHeight(25);
                table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
                table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                JScrollPane scroll = new JScrollPane(table);

                JPanel panel = new JPanel(new BorderLayout(10, 10));
                JLabel title = new JLabel("Attendance for " + s.name + " (" + s.id + ")", SwingConstants.CENTER);
                title.setFont(new Font("Segoe UI", Font.BOLD, 18));
                JLabel perc = new JLabel("Attendance %: " + String.format("%.2f%%", s.getAttendancePercentage()),
                        SwingConstants.CENTER);
                perc.setFont(new Font("Segoe UI", Font.PLAIN, 14));

                panel.add(title, BorderLayout.NORTH);
                panel.add(perc, BorderLayout.BEFORE_FIRST_LINE);
                panel.add(scroll, BorderLayout.CENTER);

                JDialog dialog = new JDialog(this, "Student Attendance", true);
                dialog.setSize(500, 400);
                dialog.setLocationRelativeTo(this);
                dialog.add(panel);
                dialog.setVisible(true);

            } else {
                break;
            }
        }
    }

    private void viewStudentAttendance() {
        String id = JOptionPane.showInputDialog(this, "Enter Student ID:");
        if (id == null || id.trim().isEmpty())
            return;

        DataStore.Student s = DataStore.getStudentById(id.trim());
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Student not found");
            return;
        }

        String[] cols = { "Date", "Status" };
        Object[][] data;
        if (s.attendance.isEmpty()) {
            data = new Object[][] { { "No attendance recorded", "" } };
        } else {
            data = new Object[s.attendance.size()][2];
            int i = 0;
            for (Map.Entry<String, String> e : s.attendance.entrySet()) {
                data[i][0] = e.getKey();
                data[i][1] = e.getValue();
                i++;
            }
        }

        JTable table = new JTable(data, cols);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(table);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JLabel title = new JLabel("Attendance for " + s.name + " (" + s.id + ")", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel perc = new JLabel("Attendance %: " + String.format("%.2f%%", s.getAttendancePercentage()),
                SwingConstants.CENTER);
        perc.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(title, BorderLayout.NORTH);
        panel.add(perc, BorderLayout.BEFORE_FIRST_LINE);
        panel.add(scroll, BorderLayout.CENTER);

        JDialog dialog = new JDialog(this, "Student Attendance History", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    // ===================== Change Password =====================
    private void changePassword() {
        JPasswordField p1 = new JPasswordField();
        JPasswordField p2 = new JPasswordField();
        Object[] f = { "New Password:", p1, "Confirm New Password:", p2 };
        int res = JOptionPane.showConfirmDialog(this, f, "Change Password", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION)
            return;

        String a = new String(p1.getPassword());
        String b = new String(p2.getPassword());
        if (a.isEmpty() || b.isEmpty() || !a.equals(b)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match or empty");
            return;
        }

        teacher.password = a;
        DataStore.saveData();
        JOptionPane.showMessageDialog(this, "Password changed");
    }
}
