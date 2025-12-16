import java.awt.*;
import java.awt.print.PrinterException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class AdminDashboard extends JFrame {

    // ===== Load + Resize Icons =====
    private final ImageIcon icStudentManage = loadScaledIcon("Pic/Mange student.png", 90, 90); // Manage Students
    private final ImageIcon icStudentShow = loadScaledIcon("Pic/Studnet.png", 90, 90); // Show Students
    private final ImageIcon icTeacher = loadScaledIcon("Pic/Manage Teacher.png", 90, 90);
    private final ImageIcon icLeaving = loadScaledIcon("Pic/42828.jpg", 90, 90);

    private final ImageIcon icSubject = loadScaledIcon("Pic/mange subject.png", 90, 90);
    private final ImageIcon icAssign = loadScaledIcon("Pic/Assign suject.png", 90, 90);
    private final ImageIcon icFee = loadScaledIcon("Pic/set fee.png", 90, 90);
    private final ImageIcon icReport = loadScaledIcon("Pic/R.png", 90, 90);
    private final ImageIcon icAnalysis = loadScaledIcon("Pic/Analyse.png", 90, 90);
    private final ImageIcon icPassword = loadScaledIcon("Pic/pass.png", 90, 90);
    private final ImageIcon icLogout = loadScaledIcon("Pic/logout.jpg", 90, 90);
    private final ImageIcon icShowTeacher = loadScaledIcon("Pic/Teacher.png", 90, 90); // new icon for showing teachers

    private final ImageIcon icLogo = loadScaledIcon("Pic/admin.png", 170, 115); // Updated admin logo

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel (Title + Logo)
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel titleLbl = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        top.add(titleLbl, BorderLayout.NORTH);

        if (icLogo != null) {
            JLabel logo = new JLabel(icLogo, SwingConstants.CENTER);
            logo.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
            top.add(logo, BorderLayout.SOUTH);
        }

        add(top, BorderLayout.NORTH);

        // Tiles Grid
        JPanel tiles = new JPanel(new GridLayout(0, 4, 18, 18));
        tiles.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        tiles.add(createTile("Manage Students", icStudentManage, e -> manageStudents()));
        tiles.add(createTile("Manage Teachers", icTeacher, e -> manageTeachers()));
        tiles.add(createTile("Manage Subjects", icSubject, e -> manageSubjects()));
        tiles.add(createTile("Assign Subjects", icAssign, e -> assignSubjects()));

        tiles.add(createTile("Set Class Fee", icFee, e -> setClassFee()));
        tiles.add(createTile("Pay Fee", icFee, e -> payFee()));
        tiles.add(createTile("View Reports", icReport, e -> viewReports()));
        tiles.add(createTile("Analysis", icAnalysis, e -> showAnalysis()));

        tiles.add(createTile("Show Students", icStudentShow, e -> showAllStudents()));
        tiles.add(createTile("Show Teachers", icShowTeacher, e -> showAllTeachers()));
        tiles.add(createTile("Leaving Certificate", icLeaving, e -> leavingCertificate()));

        tiles.add(createTile("Change Password", icPassword, e -> changePassword()));
        tiles.add(createTile("Logout", icLogout, e -> {
            new LoginScreen();
            dispose();
        }));

        add(tiles, BorderLayout.CENTER);
        setVisible(true);
    }

    // ========= Icon Loader + Auto Size ==========
    private ImageIcon loadScaledIcon(String path, int w, int h) {
        try {
            java.net.URL u = getClass().getResource(path);
            if (u != null) {
                ImageIcon original = new ImageIcon(u);
                Image scaled = original.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    // ========= Tile (button) Creator with Hover Effect ==========
    private JButton createTile(String text, ImageIcon icon, java.awt.event.ActionListener al) {
        JButton b = new JButton("<html><center><b>" + text + "</b></center></html>");
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));

        if (icon != null) {
            b.setIcon(icon);
            b.setHorizontalTextPosition(SwingConstants.RIGHT); // icon on left
            b.setVerticalTextPosition(SwingConstants.CENTER); // center vertically
        }

        b.setPreferredSize(new Dimension(240, 140));
        b.setFocusPainted(false);
        b.setBackground(UIManager.getColor("Button.background"));

        // ---- Hover Effect ----
        Color normalBG = b.getBackground();
        Color hoverBG = new Color(230, 230, 230);

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(hoverBG);
                b.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(normalBG);
                b.setBorder(UIManager.getBorder("Button.border"));
            }
        });

        b.addActionListener(al);
        return b;
    }

    // ================= LEAVING CERTIFICATE MODULE =================
    private void leavingCertificate() {

        String id = JOptionPane.showInputDialog(this, "Enter Student ID:");
        if (id == null || id.trim().isEmpty())
            return;

        DataStore.Student s = DataStore.getStudentById(id.trim());
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Student not found");
            return;
        }

        DataStore.ClassRoom c = DataStore.getClassByName(s.klass);
        double pending = c.fee - s.feePaid;

        // ---------- STUDENT INFO ----------
        StringBuilder info = new StringBuilder();
        info.append("<html><h2>Student Details</h2>")
                .append("<b>Name:</b> ").append(s.name).append("<br>")
                .append("<b>ID:</b> ").append(s.id).append("<br>")
                .append("<b>Class:</b> ").append(s.klass).append("<br>")
                .append("<b>Fee Paid:</b> ").append(s.feePaid).append("<br>")
                .append("<b>Pending Fee:</b> ").append(pending).append("<br><br>");

        // ---------- CHECK DUES ----------
        if (pending > 0) {
            info.append("<font color='red'><b>Pay dues first to issue Leaving Certificate</b></font></html>");
            JOptionPane.showMessageDialog(this, info.toString(), "Leaving Certificate",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        info.append("<font color='green'><b>Eligible for Leaving Certificate</b></font></html>");

        int choice = JOptionPane.showConfirmDialog(this,
                info.toString(),
                "Leaving Certificate",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);

        if (choice != JOptionPane.OK_OPTION)
            return;

        // ---------- CERTIFICATE TEXT ----------
        StringBuilder cert = new StringBuilder();
        cert.append("=====================================\n");
        cert.append("        SCHOOL LEAVING CERTIFICATE\n");
        cert.append("=====================================\n\n");
        cert.append("This is to certify that:\n\n");
        cert.append("Student Name : ").append(s.name).append("\n");
        cert.append("Student ID   : ").append(s.id).append("\n");
        cert.append("Class        : ").append(s.klass).append("\n\n");

        cert.append("Academic Record:\n");
        if (s.marks.isEmpty()) {
            cert.append("No marks record available.\n");
        } else {
            for (Map.Entry<String, Integer> m : s.marks.entrySet()) {
                cert.append("• ").append(m.getKey())
                        .append(" : ").append(m.getValue()).append("\n");
            }
        }

        cert.append("\nAll school dues are cleared.\n");
        cert.append("This certificate is issued by the system.\n\n");

        cert.append("Date: ").append(new Date()).append("\n");
        cert.append("Authorized Signature: ____________\n");
        cert.append("=====================================\n");

        JTextArea ta = new JTextArea(cert.toString());
        ta.setFont(new Font("Monospaced", Font.PLAIN, 14));
        ta.setEditable(false);

        int print = JOptionPane.showConfirmDialog(this,
                new JScrollPane(ta),
                "Leaving Certificate Preview",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);

        if (print == JOptionPane.OK_OPTION) {
            try {
                ta.print();
                JOptionPane.showMessageDialog(this, "Certificate Printed Successfully!");

                // ---------- DELETE STUDENT ----------
                DataStore.deleteStudent(s.id);
                DataStore.saveData();
                JOptionPane.showMessageDialog(this,
                        "Student removed from system after issuing Leaving Certificate.");

            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Printing failed");
            }
        }
    }

    // ========= Header Panel for Dialogs ==========
    private JPanel dialogHeader(ImageIcon icon, String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.setOpaque(false);

        if (icon != null)
            left.add(new JLabel(icon));
        left.add(lbl);

        p.add(left, BorderLayout.NORTH);
        return p;
    }

    // ----- Add your existing manageStudents, manageTeachers, etc. methods here
    // -----

    // ---------------- Manage Students ----------------
    private void manageStudents() {
        String[] options = { "Add Student", "Update Student", "Delete Student", "Back" };
        while (true) {
            JPanel header = dialogHeader(icStudentManage, "Manage Students");
            int sel = JOptionPane.showOptionDialog(this, header, "Manage Students", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (sel == 0)
                addStudentDialog();
            else if (sel == 1)
                updateStudentDialog();
            else if (sel == 2)
                deleteStudentDialog();
            else
                break;
        }
    }

    // ---------------- Manage Students ----------------

    private void addStudentDialog() {
        String generatedId = DataStore.generateUniqueStudentId();
        JTextField idField = new JTextField(generatedId);
        idField.setEditable(false);

        JTextField nameField = new JTextField();
        JTextField contactField = new JTextField();
        String[] classNames = DataStore.classes.stream().map(c -> c.name).toArray(String[]::new);
        JComboBox<String> clsBox = new JComboBox<>(classNames);
        JPasswordField passFld = new JPasswordField("password");

        Object[] fields = { "ID (integer):", idField, "Name:", nameField, "Contact:", contactField, "Class:", clsBox,
                "Password:", passFld };
        int res = JOptionPane.showConfirmDialog(this, fields, "Add Student", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION)
            return;
        String id = idField.getText().trim();
        String nm = nameField.getText().trim();
        String contact = contactField.getText().trim();
        String cls = (String) clsBox.getSelectedItem();
        String pass = new String(passFld.getPassword());

        if (id.isEmpty() || nm.isEmpty() || contact.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required");
            return;
        }
        try {
            Integer.parseInt(id);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "ID must be an integer");
            return;
        }
        if (DataStore.studentIdExists(id)) {
            JOptionPane.showMessageDialog(this, "Student ID already exists");
            return;
        }

        DataStore.Student s = new DataStore.Student(id, nm, contact, cls, pass);
        DataStore.addStudent(s);
        JOptionPane.showMessageDialog(this, "Student added successfully");
    }

    private void updateStudentDialog() {
        String id = JOptionPane.showInputDialog(this, "Enter Student ID to update:");
        if (id == null || id.trim().isEmpty())
            return;
        DataStore.Student s = DataStore.getStudentById(id.trim());
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Student not found");
            return;
        }

        JTextField idField = new JTextField(s.id);
        idField.setEditable(false); // ID cannot change
        ;
        JTextField nameField = new JTextField(s.name);
        JTextField contactField = new JTextField(s.contact);
        String[] classNames = DataStore.classes.stream().map(c -> c.name).toArray(String[]::new);
        JComboBox<String> clsBox = new JComboBox<>(classNames);
        clsBox.setSelectedItem(s.klass);
        JPasswordField passFld = new JPasswordField(s.password);

        Object[] fields = { "ID (integer):", idField, "Name:", nameField, "Contact:", contactField, "Class:", clsBox,
                "Password:", passFld };
        int res = JOptionPane.showConfirmDialog(this, fields, "Update Student", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION)
            return;
        String newId = idField.getText().trim();
        String nm = nameField.getText().trim();
        String contact = contactField.getText().trim();
        String cls = (String) clsBox.getSelectedItem();
        String pass = new String(passFld.getPassword());

        if (newId.isEmpty() || nm.isEmpty() || contact.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields required");
            return;
        }
        try {
            Integer.parseInt(newId);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "ID must be integer");
            return;
        }
        // if changing to an ID that exists (and not same student) -> invalid
        if (!newId.equals(s.id) && DataStore.studentIdExists(newId)) {
            JOptionPane.showMessageDialog(this, "ID already exists");
            return;
        }

        DataStore.Student ns = new DataStore.Student(newId, nm, contact, cls, pass);
        ns.feePaid = s.feePaid;
        ns.attendance = new LinkedHashMap<>(s.attendance);
        ns.marks = new LinkedHashMap<>(s.marks);
        boolean ok = DataStore.updateStudent(s.id, ns);
        if (ok)
            JOptionPane.showMessageDialog(this, "Student updated");
        else
            JOptionPane.showMessageDialog(this, "Failed to update student");
    }

    private void deleteStudentDialog() {
        String id = JOptionPane.showInputDialog(this, "Enter Student ID to delete:");
        if (id == null || id.trim().isEmpty())
            return;
        DataStore.Student s = DataStore.getStudentById(id.trim());
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Student not found");
            return;
        }
        int conf = JOptionPane.showConfirmDialog(this, "Delete student " + s.name + " (ID: " + s.id + ")?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION)
            return;
        boolean ok = DataStore.deleteStudent(id.trim());
        if (ok)
            JOptionPane.showMessageDialog(this, "Student deleted");
        else
            JOptionPane.showMessageDialog(this, "Failed to delete student");
    }

    private void printFeeReceipt(DataStore.Student s, DataStore.ClassRoom cr) {
        StringBuilder sb = new StringBuilder();
        sb.append("🏫 School Name / Logo\n");
        sb.append("Fee Receipt\n");
        sb.append("Date: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
                .append("\n\n");
        sb.append("Student: ").append(s.name).append("\n");
        sb.append("Class: ").append(s.klass).append("\n");
        sb.append("Contact: ").append(s.contact).append("\n\n");
        sb.append("Fee History:\n");
        sb.append(String.format("%-20s %-10s %-10s %-20s\n", "Date", "Paid", "Class Fee", "Note"));
        for (DataStore.Student.FeeRecord fr : s.feeHistory) {
            sb.append(String.format("%-20s %-10.2f %-10.2f %-20s\n", fr.date, fr.amount, fr.classFeeAtTime, fr.note));
        }

        sb.append("\nTotal Paid: ").append(s.feePaid).append("\n");
        sb.append("Pending: ").append(cr.fee - s.feePaid).append("\n");

        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Fee Receipt", JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------------- Manage Teachers ----------------
    private void manageTeachers() {
        String[] options = { "Add Teacher", "Update Teacher", "Delete Teacher", "Back" };
        while (true) {
            JPanel header = dialogHeader(icTeacher, "Manage Teachers");
            int sel = JOptionPane.showOptionDialog(this, header, "Manage Teachers", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (sel == 0)
                addTeacherDialog();
            else if (sel == 1)
                updateTeacherDialog();
            else if (sel == 2)
                deleteTeacherDialog();
            else
                break;
        }
    }

    private void addTeacherDialog() {
        // System will generate unique ID for teacher
        String generatedId = DataStore.generateUniqueTeacherId();

        JTextField idField = new JTextField(generatedId);
        idField.setEditable(false); // admin cannot edit id
        JTextField nameField = new JTextField();
        JTextField contactField = new JTextField();
        JPasswordField passFld = new JPasswordField("password");

        Object[] fields = { "Teacher ID (auto):", idField, "Name:", nameField, "Contact:", contactField, "Password:",
                passFld };
        int res = JOptionPane.showConfirmDialog(this, fields, "Add Teacher", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION)
            return;

        String id = idField.getText().trim();
        String nm = nameField.getText().trim();
        String contact = contactField.getText().trim();
        String pass = new String(passFld.getPassword());

        if (nm.isEmpty() || contact.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields required");
            return;
        }

        // NOTE: generated id should be unique by design; still check
        if (DataStore.teacherIdExists(id)) {
            JOptionPane.showMessageDialog(this, "Generated Teacher ID already exists, try again");
            return;
        }

        DataStore.Teacher t = new DataStore.Teacher(id, nm, contact, pass);
        DataStore.addTeacher(t);
        JOptionPane.showMessageDialog(this, "Teacher added successfully (ID: " + id + ")");
    }

    private void updateTeacherDialog() {
        String id = JOptionPane.showInputDialog(this, "Enter Teacher ID to update:");
        if (id == null || id.trim().isEmpty())
            return;
        DataStore.Teacher t = DataStore.getTeacherById(id.trim());
        if (t == null) {
            JOptionPane.showMessageDialog(this, "Teacher not found");
            return;
        }

        // ID shown but not editable
        JTextField idFld = new JTextField(t.id);
        idFld.setEditable(false);
        JTextField nameFld = new JTextField(t.name);
        JTextField contactFld = new JTextField(t.contact);
        JPasswordField passFld = new JPasswordField(t.password);
        Object[] fields = { "Teacher ID (cannot change):", idFld, "Name:", nameFld, "Contact:", contactFld, "Password:",
                passFld };
        int res = JOptionPane.showConfirmDialog(this, fields, "Update Teacher", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION)
            return;
        String nid = idFld.getText().trim(); // same as t.id
        String nm = nameFld.getText().trim();
        String contact = contactFld.getText().trim();
        String pass = new String(passFld.getPassword());
        if (nm.isEmpty() || contact.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields required");
            return;
        }

        // Keep same ID in update - do not change teacher id
        DataStore.Teacher nt = new DataStore.Teacher(nid, nm, contact, pass);
        nt.assignedClassSubjects = new LinkedHashMap<>(t.assignedClassSubjects);
        boolean ok = DataStore.updateTeacher(t.id, nt);
        if (ok)
            JOptionPane.showMessageDialog(this, "Teacher updated");
        else
            JOptionPane.showMessageDialog(this, "Failed to update teacher");
    }

    private void deleteTeacherDialog() {
        String id = JOptionPane.showInputDialog(this, "Enter Teacher ID to delete:");
        if (id == null || id.trim().isEmpty())
            return;
        DataStore.Teacher t = DataStore.getTeacherById(id.trim());
        if (t == null) {
            JOptionPane.showMessageDialog(this, "Teacher not found");
            return;
        }
        int conf = JOptionPane.showConfirmDialog(this,
                "Delete teacher " + t.name + " (ID: " + t.id + ")? This removes assignments.", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION)
            return;
        boolean ok = DataStore.deleteTeacher(id.trim());
        if (ok)
            JOptionPane.showMessageDialog(this, "Teacher deleted");
        else
            JOptionPane.showMessageDialog(this, "Failed to delete teacher");
    }

    // ---------------- Manage Subjects ----------------
    private void manageSubjects() {
        String[] options = { "Add New Subject", "Update Subject", "Delete Subject", "Back" };
        while (true) {
            JPanel header = dialogHeader(icSubject, "Manage Subjects");
            int sel = JOptionPane.showOptionDialog(this, header, "Subjects", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (sel == 0)
                addSubjectDialog();
            else if (sel == 1)
                updateSubjectDialog();
            else if (sel == 2)
                deleteSubjectDialog();
            else
                break;
        }
    }

    private void addSubjectDialog() {
        String cls = (String) JOptionPane.showInputDialog(this, "Select Class:", "Add Subject",
                JOptionPane.QUESTION_MESSAGE, null, DataStore.classes.stream().map(c -> c.name).toArray(String[]::new),
                null);
        if (cls == null)
            return;
        String subj = JOptionPane.showInputDialog(this, "Enter subject name to add to class " + cls + ":");
        if (subj == null || subj.trim().isEmpty())
            return;
        boolean ok = DataStore.addSubjectToClass(cls, subj.trim());
        if (ok)
            JOptionPane.showMessageDialog(this, "Subject added to class");
        else
            JOptionPane.showMessageDialog(this, "Failed to add subject (duplicate?)");
    }

    private void updateSubjectDialog() {
        String cls = (String) JOptionPane.showInputDialog(this, "Select Class:", "Update Subject",
                JOptionPane.QUESTION_MESSAGE, null, DataStore.classes.stream().map(c -> c.name).toArray(String[]::new),
                null);
        if (cls == null)
            return;
        DataStore.ClassRoom c = DataStore.getClassByName(cls);
        if (c == null || c.subjects.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No subjects in this class");
            return;
        }
        String old = (String) JOptionPane.showInputDialog(this, "Select subject to update:", "Update",
                JOptionPane.QUESTION_MESSAGE, null, c.subjects.toArray(), null);
        if (old == null)
            return;
        String neu = JOptionPane.showInputDialog(this, "Enter new name for subject '" + old + "':");
        if (neu == null || neu.trim().isEmpty())
            return;
        boolean ok = DataStore.updateSubjectInClass(cls, old, neu.trim());
        if (ok)
            JOptionPane.showMessageDialog(this, "Subject updated");
        else
            JOptionPane.showMessageDialog(this, "Failed to update subject (duplicate or conflict)");
    }

    private void deleteSubjectDialog() {
        String cls = (String) JOptionPane.showInputDialog(this, "Select Class:", "Delete Subject",
                JOptionPane.QUESTION_MESSAGE, null, DataStore.classes.stream().map(c -> c.name).toArray(String[]::new),
                null);
        if (cls == null)
            return;
        DataStore.ClassRoom c = DataStore.getClassByName(cls);
        if (c == null || c.subjects.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No subjects in this class");
            return;
        }
        String subj = (String) JOptionPane.showInputDialog(this, "Select subject to delete:", "Delete",
                JOptionPane.QUESTION_MESSAGE, null, c.subjects.toArray(), null);
        if (subj == null)
            return;
        int conf = JOptionPane.showConfirmDialog(this,
                "Delete subject '" + subj + "' from class " + cls + "? This will remove marks and assignments.",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION)
            return;
        boolean ok = DataStore.deleteSubjectFromClass(cls, subj);
        if (ok)
            JOptionPane.showMessageDialog(this, "Subject deleted");
        else
            JOptionPane.showMessageDialog(this, "Failed to delete subject");
    }

    // ---------------- Assign Subjects ----------------
    private void assignSubjects() {
        String[] options = { "Assign Subject to Teacher", "Update Assigned Subjects", "Back" };
        while (true) {
            JPanel header = dialogHeader(icAssign, "Assign Subjects");
            int sel = JOptionPane.showOptionDialog(this, header, "Assign Subjects", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (sel == 0)
                assignSubjectDialog();
            else if (sel == 1)
                updateAssignedDialog();
            else
                break;
        }
    }

    private void assignSubjectDialog() {
        String tid = JOptionPane.showInputDialog(this, "Enter Teacher ID to assign:");
        if (tid == null || tid.trim().isEmpty())
            return;
        DataStore.Teacher t = DataStore.getTeacherById(tid.trim());
        if (t == null) {
            JOptionPane.showMessageDialog(this, "Teacher not found");
            return;
        }
        String cls = (String) JOptionPane.showInputDialog(this, "Select Class:", "Assign", JOptionPane.QUESTION_MESSAGE,
                null, DataStore.classes.stream().map(c -> c.name).toArray(String[]::new), null);
        if (cls == null)
            return;
        DataStore.ClassRoom cr = DataStore.getClassByName(cls);
        if (cr == null || cr.subjects.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No subjects in class");
            return;
        }
        // show check list
        JList<String> list = new JList<>(cr.subjects.toArray(new String[0]));
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane sp = new JScrollPane(list);
        int res = JOptionPane.showConfirmDialog(this, sp,
                "Select subjects to assign (note: each class-subject can only have one teacher)",
                JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION)
            return;
        List<String> chosen = list.getSelectedValuesList();
        if (chosen.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No subjects selected");
            return;
        }
        // check teacher limit (max 3 total subjects)
        int already = t.totalAssignedSubjects();
        int willAdd = 0;
        for (String s : chosen) {
            List<String> cur = t.assignedClassSubjects.getOrDefault(cls, new ArrayList<>());
            if (!cur.contains(s))
                willAdd++;
        }
        if (already + willAdd > 3) {
            JOptionPane.showMessageDialog(this,
                    "Teacher cannot have more than 3 subjects in total. Already: " + already);
            return;
        }
        // ensure chosen not assigned elsewhere
        List<String> toAssign = new ArrayList<>();
        for (String s : chosen) {
            if (!DataStore.isSubjectAssignedToAnyTeacher(cls, s)
                    || (t.assignedClassSubjects.getOrDefault(cls, new ArrayList<>()).contains(s))) {
                toAssign.add(s);
            }
        }
        if (toAssign.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selected subjects are already assigned to other teachers");
            return;
        }
        boolean ok = DataStore.assignSubjectsToTeacher(tid.trim(), cls, toAssign);
        if (ok)
            JOptionPane.showMessageDialog(this, "Assigned: " + String.join(", ", toAssign));
        else
            JOptionPane.showMessageDialog(this, "Failed to assign (conflict)");
    }

    private void updateAssignedDialog() {
        String tid = JOptionPane.showInputDialog(this, "Enter Teacher ID to update assignments:");
        if (tid == null || tid.trim().isEmpty())
            return;
        DataStore.Teacher t = DataStore.getTeacherById(tid.trim());
        if (t == null) {
            JOptionPane.showMessageDialog(this, "Teacher not found");
            return;
        }
        String[] classNames = t.assignedClassSubjects.keySet().toArray(new String[0]);
        if (classNames.length == 0) {
            JOptionPane.showMessageDialog(this, "This teacher has no assigned classes");
            return;
        }
        String cls = (String) JOptionPane.showInputDialog(this, "Select class to update for this teacher:",
                "Update Assigned", JOptionPane.QUESTION_MESSAGE, null, classNames, null);
        if (cls == null)
            return;
        DataStore.ClassRoom cr = DataStore.getClassByName(cls);
        if (cr == null) {
            JOptionPane.showMessageDialog(this, "Class not found");
            return;
        }
        // build list of available subjects (class subjects). Preselect current ones.
        JList<String> list = new JList<>(cr.subjects.toArray(new String[0]));
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        List<String> curList = t.assignedClassSubjects.getOrDefault(cls, new ArrayList<>());
        int[] selIdx = curList.stream().mapToInt(s -> {
            for (int i = 0; i < cr.subjects.size(); i++)
                if (cr.subjects.get(i).equals(s))
                    return i;
            return -1;
        }).filter(i -> i >= 0).toArray();
        list.setSelectedIndices(selIdx);
        int res = JOptionPane.showConfirmDialog(this, new JScrollPane(list),
                "Select new assigned subjects (max 3 per teacher total)", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION)
            return;
        List<String> chosen = list.getSelectedValuesList();
        // check teacher total limit
        int otherCount = t.totalAssignedSubjects()
                - t.assignedClassSubjects.getOrDefault(cls, new ArrayList<>()).size();
        if (otherCount + chosen.size() > 3) {
            JOptionPane.showMessageDialog(this,
                    "Total assigned subjects cannot exceed 3. Current other count: " + otherCount);
            return;
        }
        // validate exclusivity vs other teachers
        boolean ok = DataStore.updateTeacherAssignedSubjects(tid.trim(), cls, chosen);
        if (ok)
            JOptionPane.showMessageDialog(this, "Assigned subjects updated");
        else
            JOptionPane.showMessageDialog(this, "Failed to update assignments (conflict with other teacher)");
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

        DataStore.adminPassword = a; // Save new admin password in DataStore
        DataStore.saveData(); // persist changes
        JOptionPane.showMessageDialog(this, "Password changed successfully");
    }

    // ---------------- Fee ----------------
    private void setClassFee() {
        String cls = (String) JOptionPane.showInputDialog(this, "Select Class:", "Set Class Fee",
                JOptionPane.QUESTION_MESSAGE, null, DataStore.classes.stream().map(c -> c.name).toArray(String[]::new),
                null);
        if (cls == null)
            return;
        DataStore.ClassRoom cr = DataStore.getClassByName(cls);
        String feeStr = JOptionPane.showInputDialog(this,
                "Enter monthly fee for class " + cls + " (current: " + cr.fee + "):");
        if (feeStr == null)
            return;
        try {
            double f = Double.parseDouble(feeStr);
            if (f < 0) {
                JOptionPane.showMessageDialog(this, "Fee cannot be negative");
                return;
            }
            DataStore.setClassFee(cls, f);
            JOptionPane.showMessageDialog(this, "Fee set");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid fee");
        }
    }

    private void payFee() {

        // 1. Ask for Student ID
        String sid = JOptionPane.showInputDialog(this, "Enter Student ID:");
        if (sid == null || sid.trim().isEmpty())
            return;

        DataStore.Student s = DataStore.getStudentById(sid.trim());
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Student not found");
            return;
        }

        DataStore.ClassRoom c = DataStore.getClassByName(s.klass);
        if (c == null) {
            JOptionPane.showMessageDialog(this, "Class fee not set for this class.");
            return;
        }

        double pending = c.fee - s.feePaid;
        if (pending <= 0) {
            JOptionPane.showMessageDialog(this, "No pending fee. All fees are paid.");
            return;
        }

        // 2. Student Info (Beautiful & Clear)
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JLabel lblStudent = new JLabel(
                "<html><h2>Name: " + s.name + "</h2>"
                        + "ID: " + s.id + "<br>"
                        + "Class: " + s.klass + "<br><br>"
                        + "<b>Total Fee:</b> " + c.fee + "<br>"
                        + "<b>Paid:</b> " + s.feePaid + "<br>"
                        + "<b>Pending:</b> " + pending + "</html>");
        lblStudent.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        panel.add(lblStudent, BorderLayout.NORTH);

        // 3. Fee History Table
        String[] columns = { "Date", "Paid Amount", "Total Fee", "Pending Fee", "Note" };
        Object[][] data = new Object[s.feeHistory.size()][5];
        for (int i = 0; i < s.feeHistory.size(); i++) {
            DataStore.Student.FeeRecord fr = s.feeHistory.get(i);
            double pendingAtThatTime = fr.classFeeAtTime - getTotalPaidUpTo(s, i);
            data[i][0] = fr.date;
            data[i][1] = fr.amount;
            data[i][2] = fr.classFeeAtTime;
            data[i][3] = pendingAtThatTime;
            data[i][4] = fr.note;
        }

        JTable table = new JTable(data, columns);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setEnabled(false);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // 4. Options
        String[] options = { "Pay Fee", "Print History", "Cancel" };
        int choice = JOptionPane.showOptionDialog(
                this, panel, "Fee History",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (choice == 0) { // Pay Fee
            while (true) {
                String amtStr = JOptionPane.showInputDialog(this, "Enter Amount (Pending: " + (int) pending + "):");
                if (amtStr == null)
                    return; // Cancel pressed

                amtStr = amtStr.trim();
                if (amtStr.isEmpty())
                    continue; // empty input, ask again

                try {
                    int amt = Integer.parseInt(amtStr);

                    if (amt <= 0) {
                        JOptionPane.showMessageDialog(this, "Amount must be greater than 0.");
                        continue; // ask again
                    }

                    if (amt > pending) {
                        JOptionPane.showMessageDialog(this,
                                "Amount cannot exceed pending fee (" + (int) pending + ").");
                        continue; // ask again
                    }

                    // ✅ Valid input, save record
                    s.feeHistory.add(new DataStore.Student.FeeRecord(amt, c.fee, "Payment"));
                    s.feePaid += amt;
                    DataStore.saveData();
                    JOptionPane.showMessageDialog(this, "Fee Paid Successfully!");
                    printSinglePaymentReceipt(s, c);
                    break; // exit loop

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input! Please enter an integer.");
                    // loop continues to ask again
                }
            }
        }

        // ★ Option 2: PRINT FULL HISTORY
        else if (choice == 1) {
            printFullFeeReceipt(s, c);
        }
    }

    // Calculate total paid up to index
    private double getTotalPaidUpTo(DataStore.Student s, int index) {
        double total = 0;
        for (int i = 0; i <= index; i++) {
            total += s.feeHistory.get(i).amount;
        }
        return total;
    }

    // ---------------- SINGLE PAYMENT RECEIPT ----------------
    private void printSinglePaymentReceipt(DataStore.Student s, DataStore.ClassRoom c) {
        if (s.feeHistory.isEmpty())
            return;

        DataStore.Student.FeeRecord fr = s.feeHistory.get(s.feeHistory.size() - 1);
        double pending = fr.classFeeAtTime - s.feePaid;

        // Build receipt text
        StringBuilder sb = new StringBuilder();
        sb.append("=========== SCHOOL RECEIPT ===========\n\n");
        sb.append("Student Name : ").append(s.name).append("\n");
        sb.append("Student ID   : ").append(s.id).append("\n");
        sb.append("Class        : ").append(s.klass).append("\n");
        sb.append("Receipt No   : ").append(s.feeHistory.size()).append("\n");
        sb.append("Date         : ").append(fr.date).append("\n\n");

        sb.append("--------------------------------------\n");
        sb.append(String.format("%-15s %-10s %-10s %-10s\n", "Date", "Paid", "Total", "Pending"));
        sb.append("--------------------------------------\n");
        sb.append(String.format("%-15s %-10.2f %-10.2f %-10.2f\n",
                fr.date, fr.amount, fr.classFeeAtTime, pending));
        sb.append("--------------------------------------\n");
        sb.append("Total Paid  : ").append(s.feePaid).append("\n");
        sb.append("Pending Fee : ").append(pending).append("\n");
        sb.append("======================================\n");

        // JTextArea for preview
        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(new Font("Monospaced", Font.PLAIN, 14));
        ta.setEditable(false);

        // Show in a dialog
        int option = JOptionPane.showConfirmDialog(this, new JScrollPane(ta),
                "Single Payment Receipt Preview", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            // Print
            try {
                boolean done = ta.print();
                if (done) {
                    JOptionPane.showMessageDialog(this, "Receipt Printed Successfully!");
                }
            } catch (PrinterException ex) {
                ex.printStackTrace();
            }
        }
    }

    // ---------------- FULL FEE HISTORY RECEIPT ----------------
    private void printFullFeeReceipt(DataStore.Student s, DataStore.ClassRoom c) {
        if (s.feeHistory.isEmpty())
            return;

        StringBuilder sb = new StringBuilder();
        sb.append("=========== FULL FEE HISTORY ===========\n\n");
        sb.append("Student Name : ").append(s.name).append("\n");
        sb.append("Student ID   : ").append(s.id).append("\n");
        sb.append("Class        : ").append(s.klass).append("\n");
        sb.append("Report Date  : ").append(new java.util.Date()).append("\n");
        sb.append("Total Fee    : ").append(c.fee).append("\n\n");

        sb.append("--------------------------------------------------\n");
        sb.append(String.format("%-15s %-10s %-10s %-10s %-10s\n", "Date", "Paid", "Total", "Pending", "Note"));
        sb.append("--------------------------------------------------\n");

        double totalPaid = 0;
        for (DataStore.Student.FeeRecord fr : s.feeHistory) {
            totalPaid += fr.amount;
            double pending = fr.classFeeAtTime - totalPaid;
            sb.append(String.format("%-15s %-10.2f %-10.2f %-10.2f %-10s\n",
                    fr.date, fr.amount, fr.classFeeAtTime, pending, fr.note));
        }

        sb.append("--------------------------------------------------\n");
        sb.append("Total Paid       : ").append(totalPaid).append("\n");
        sb.append("Remaining Fee    : ").append(c.fee - totalPaid).append("\n");
        sb.append("==============================================\n");

        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(new Font("Monospaced", Font.PLAIN, 14));
        ta.setEditable(false);

        // Show in a dialog
        int option = JOptionPane.showConfirmDialog(this, new JScrollPane(ta),
                "Full Fee History Preview", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            // Print
            try {
                boolean done = ta.print();
                if (done) {
                    JOptionPane.showMessageDialog(this, "Full Receipt Printed Successfully!");
                }
            } catch (PrinterException ex) {
                ex.printStackTrace();
            }
        }
    }

    // -- Reports ----------------
    private void viewReports() {
        String[] opts = { "Student Marks (search by ID)", "Attendance History (search by ID)", "Back" };
        while (true) {
            JPanel header = dialogHeader(icReport, "Reports");
            int sel = JOptionPane.showOptionDialog(this, header, "Reports", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
            if (sel == 0)
                reportStudentMarks();
            else if (sel == 1)
                reportAttendanceHistory();
            else
                break;
        }
    }

    private void reportAttendanceHistory() {
        String id = JOptionPane.showInputDialog(this, "Enter Student ID:");
        if (id == null || id.trim().isEmpty())
            return;

        DataStore.Student s = DataStore.getStudentById(id.trim());
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Student not found");
            return;
        }

        // ===== TOP STUDENT INFO =====
        JPanel infoPanel = buildStudentInfoPanel(s, true);

        // ===== ATTENDANCE TABLE =====
        String[] cols = { "Date", "Status" };
        Object[][] data;

        if (s.attendance.isEmpty()) {
            data = new Object[][] { { "No Attendance Found", "" } };
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
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane tableScroll = new JScrollPane(table);

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel title = new JLabel("STUDENT ATTENDANCE REPORT", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        panel.add(title, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.WEST);
        panel.add(tableScroll, BorderLayout.CENTER);

        JDialog dialog = new JDialog(this, "Attendance Report", true);
        dialog.setSize(650, 500);
        dialog.setLocationRelativeTo(this);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void reportStudentMarks() {
        String id = JOptionPane.showInputDialog(this, "Enter Student ID:");
        if (id == null || id.trim().isEmpty())
            return;

        DataStore.Student s = DataStore.getStudentById(id.trim());
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Student not found");
            return;
        }

        // ===== TOP STUDENT INFO =====
        JPanel infoPanel = buildStudentInfoPanel(s, false);

        // ===== MARKS TABLE =====
        String[] cols = { "Subject", "Marks" };
        Object[][] data;

        if (s.marks.isEmpty()) {
            data = new Object[][] { { "No Marks Found", "" } };
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
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane tableScroll = new JScrollPane(table);

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel title = new JLabel("STUDENT MARKS REPORT", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        panel.add(title, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.WEST);
        panel.add(tableScroll, BorderLayout.CENTER);

        JDialog dialog = new JDialog(this, "Marks Report", true);
        dialog.setSize(650, 500);
        dialog.setLocationRelativeTo(this);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    // ---------- BEAUTIFUL STUDENT INFO PANEL CREATOR ----------
    private JPanel buildStudentInfoPanel(DataStore.Student s, boolean isAttendance) {

        JPanel info = new JPanel(new GridLayout(0, 1, 5, 5));
        info.setBorder(BorderFactory.createTitledBorder("Student Info"));

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font valueFont = new Font("Segoe UI", Font.PLAIN, 14);

        info.add(makeInfoRow("Name", s.name, labelFont, valueFont));
        info.add(makeInfoRow("ID", s.id, labelFont, valueFont));
        info.add(makeInfoRow("Class", s.klass, labelFont, valueFont));

        if (isAttendance)
            info.add(makeInfoRow("Attendance %", String.format("%.2f%%", s.getAttendancePercentage()), labelFont,
                    valueFont));
        else
            info.add(makeInfoRow("Fee Paid", String.valueOf(s.feePaid), labelFont, valueFont));

        return info;
    }

    private JPanel makeInfoRow(String label, String value, Font lf, Font vf) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel l = new JLabel(label + " : ");
        l.setFont(lf);
        JLabel v = new JLabel(value);
        v.setFont(vf);
        row.add(l);
        row.add(v);
        return row;
    }

    // ---------------- Analysis ----------------
    // ---------------- Analysis ----------------
    private void showAnalysis() {

        // ===== TOP INFO PANEL (Totals) =====
        JPanel info = new JPanel(new GridLayout(2, 1, 5, 5));
        info.setBorder(BorderFactory.createTitledBorder("Summary"));

        info.add(new JLabel("Total Teachers: " + DataStore.teachers.size()));
        info.add(new JLabel("Total Students: " + DataStore.students.size()));

        // ===== TABLE DATA (Students Per Class) =====
        String[] cols = { "Class Name", "Students Count" };
        Object[][] data = new Object[DataStore.classes.size()][2];

        int i = 0;
        for (DataStore.ClassRoom c : DataStore.classes) {
            data[i][0] = c.name;
            data[i][1] = c.studentIds.size();
            i++;
        }

        JTable table = new JTable(data, cols);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setRowHeight(25);

        JScrollPane scroll = new JScrollPane(table);

        // ===== MAIN PANEL =====
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel title = new JLabel("SYSTEM ANALYSIS", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        panel.add(title, BorderLayout.NORTH);
        panel.add(info, BorderLayout.WEST);
        panel.add(scroll, BorderLayout.CENTER);

        // ===== DIALOG =====
        JDialog dialog = new JDialog(this, "Analysis Report", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    // ---------------- Main Students Menu ----------------
    private void showStudentsMenu() {
        // Dialog with two options
        Object[] options = { "Show All Students", "Show Class Students" };
        int choice = JOptionPane.showOptionDialog(this,
                "Choose an option:",
                "Students Menu",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            showAllStudents(); // All students table
        } else if (choice == 1) {
            showStudentsByClass(); // Students filtered by class
        }
    }

    // ---------------- SHOW ALL STUDENTS WITH SEARCH & SORT ----------------
    private void showAllStudents() {
        String[] cols = { "ID", "Name", "Class", "Pending Fee", "Attendance %" };
        Object[][] data = getAllStudentsData();

        JTable table = new JTable(data, cols);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(table);

        // Search field
        JTextField searchField = new JTextField(20);
        JLabel searchLabel = new JLabel("Search by Name or ID: ");
        JPanel searchPanel = new JPanel();
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        // Sort Combo
        String[] sortOptions = { "Sort by Class Asc", "Sort by Class Desc" };
        JComboBox<String> sortCombo = new JComboBox<>(sortOptions);
        searchPanel.add(sortCombo);

        // Filter logic
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            private void filter() {
                String text = searchField.getText();
                if (text.trim().length() == 0)
                    sorter.setRowFilter(null);
                else
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0, 1)); // search in ID and Name
            }
        });

        // Sort logic
        sortCombo.addActionListener(e -> {
            if (sortCombo.getSelectedIndex() == 0) {
                sorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(2, SortOrder.ASCENDING)));
            } else {
                sorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(2, SortOrder.DESCENDING)));
            }
        });

        // Print Button
        JButton printBtn = new JButton("🖨 Print All Students");
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
        JLabel title = new JLabel("ALL STUDENTS", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        panel.add(title, BorderLayout.NORTH);
        panel.add(searchPanel, BorderLayout.BEFORE_FIRST_LINE);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        // Dialog
        JDialog dialog = new JDialog(this, "All Students", true);
        dialog.setSize(850, 500);
        dialog.setLocationRelativeTo(this);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    // ---------------- SHOW CLASS STUDENTS WITH SEARCH ----------------
    private void showStudentsByClass() {
        if (DataStore.classes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No classes available!");
            return;
        }

        String[] classNames = DataStore.classes.stream().map(c -> c.name).toArray(String[]::new);

        String selectedClass = (String) JOptionPane.showInputDialog(
                this, "Select Class:", "Choose Class",
                JOptionPane.QUESTION_MESSAGE, null, classNames, classNames[0]);
        if (selectedClass == null)
            return;

        java.util.List<DataStore.Student> filtered = new java.util.ArrayList<>();
        for (DataStore.Student s : DataStore.students) {
            if (s.klass.equals(selectedClass))
                filtered.add(s);
        }

        String[] cols = { "ID", "Name", "Pending Fee", "Attendance %" };
        Object[][] data;
        if (filtered.isEmpty()) {
            data = new Object[][] { { "No Students Found", "", "", "" } };
        } else {
            data = new Object[filtered.size()][4];
            int i = 0;
            for (DataStore.Student s : filtered) {
                data[i][0] = s.id;
                data[i][1] = s.name;
                data[i][2] = s.feePaid; // pending fee
                data[i][3] = String.format("%.2f%%", s.getAttendancePercentage());
                i++;
            }
        }

        JTable table = new JTable(data, cols);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(table);

        // Search field
        JTextField searchField = new JTextField(20);
        JLabel searchLabel = new JLabel("Search by Name or ID: ");
        JPanel searchPanel = new JPanel();
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            private void filter() {
                String text = searchField.getText();
                if (text.trim().length() == 0)
                    sorter.setRowFilter(null);
                else
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0, 1));
            }
        });

        // Print
        JButton printBtn = new JButton("🖨 Print Class Students");
        printBtn.addActionListener(e -> {
            try {
                table.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Print Failed: " + ex.getMessage());
            }
        });
        JPanel btnPanel = new JPanel();
        btnPanel.add(printBtn);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JLabel title = new JLabel("STUDENTS OF CLASS: " + selectedClass, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        panel.add(title, BorderLayout.NORTH);
        panel.add(searchPanel, BorderLayout.BEFORE_FIRST_LINE);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(this, "Class Students", true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    // Helper method to get all students data
    private Object[][] getAllStudentsData() {
        if (DataStore.students.isEmpty())
            return new Object[][] { { "No Students", "", "", "", "" } };
        Object[][] data = new Object[DataStore.students.size()][5];
        int i = 0;
        for (DataStore.Student s : DataStore.students) {
            data[i][0] = s.id;
            data[i][1] = s.name;
            data[i][2] = s.klass;
            data[i][3] = s.feePaid; // pending fee
            data[i][4] = String.format("%.2f%%", s.getAttendancePercentage());
            i++;
        }
        return data;
    }

    // ---------------- Show All Teachers ----------------
    private void showAllTeachers() {

        // Columns
        String[] cols = { "ID", "Name", "Assigned Classes & Subjects" };

        // Data
        Object[][] data;
        if (DataStore.teachers.isEmpty()) {
            data = new Object[][] { { "No Teachers", "", "" } };
        } else {
            data = new Object[DataStore.teachers.size()][3];
            int i = 0;
            for (DataStore.Teacher t : DataStore.teachers) {
                data[i][0] = t.id;
                data[i][1] = t.name;

                if (t.assignedClassSubjects == null || t.assignedClassSubjects.isEmpty()) {
                    data[i][2] = "None";
                } else {
                    List<String> pairs = new ArrayList<>();
                    for (Map.Entry<String, List<String>> e : t.assignedClassSubjects.entrySet()) {
                        pairs.add(e.getKey() + ": " + String.join(",", e.getValue()));
                    }
                    data[i][2] = String.join(" ; ", pairs);
                }
                i++;
            }
        }

        JTable table = new JTable(data, cols);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(table);

        // Search field
        JTextField searchField = new JTextField(20);
        JLabel searchLabel = new JLabel("Search by Name or ID: ");
        JPanel searchPanel = new JPanel();
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        // Filter
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            private void filter() {
                String text = searchField.getText();
                if (text.trim().length() == 0)
                    sorter.setRowFilter(null);
                else
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0, 1)); // search ID and Name
            }
        });

        // Print button
        JButton printBtn = new JButton("🖨 Print All Teachers");
        printBtn.addActionListener(e -> {
            try {
                table.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Print Failed: " + ex.getMessage());
            }
        });
        JPanel btnPanel = new JPanel();
        btnPanel.add(printBtn);

        // Main panel
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JLabel title = new JLabel("ALL TEACHERS", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        panel.add(title, BorderLayout.NORTH);
        panel.add(searchPanel, BorderLayout.BEFORE_FIRST_LINE);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        // Show dialog
        JDialog dialog = new JDialog(this, "All Teachers", true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {

        // Launch admin dashboard
        SwingUtilities.invokeLater(AdminDashboard::new);
    }
}
