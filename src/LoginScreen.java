import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginScreen extends JFrame {

    public LoginScreen() {
        setTitle("School Management System - Login");
        setIconImage(new ImageIcon("Pic/logo.png").getImage());
        setSize(800, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("School Management System", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));

        ImageIcon logoIcon = new ImageIcon("Pic/logo.png");
        Image logoImg = logoIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(logoImg), SwingConstants.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(logoLabel, BorderLayout.CENTER);
        root.add(topPanel, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBorder(BorderFactory.createTitledBorder("Select Your Role"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        JButton btnAdmin = styledButton("Admin", "Pic/admin.png");
        JButton btnTeacher = styledButton("Teacher", "Pic/Teacher.png");
        JButton btnStudent = styledButton("Student", "Pic/Studnet.png");

        gbc.gridx = 0;
        center.add(btnAdmin, gbc);
        gbc.gridx = 1;
        center.add(btnTeacher, gbc);
        gbc.gridx = 2;
        center.add(btnStudent, gbc);

        root.add(center, BorderLayout.CENTER);
        add(root);

        // =================== BUTTON ACTIONS ====================

        // ADMIN LOGIN (no ID)
        btnAdmin.addActionListener(e -> {
            showLoginDialog("Admin", "Pic/admin.png", false, null, DataStore.adminPassword, () -> {
                new AdminDashboard();
                dispose();
            });
        });

        // TEACHER LOGIN
        btnTeacher.addActionListener(e -> {
            showLoginDialog("Teacher", "Pic/Teacher.png", true, "Enter Teacher ID:", null, id -> {
                DataStore.Teacher t = DataStore.getTeacherById(id.trim());
                if (t == null) {
                    JOptionPane.showMessageDialog(this, "Invalid Teacher ID!", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                return true;
            }, id -> {
                DataStore.Teacher t = DataStore.getTeacherById(id.trim());
                new TeacherDashboard(id.trim());
                dispose();
            });
        });

        // STUDENT LOGIN
        btnStudent.addActionListener(e -> {
            showLoginDialog("Student", "Pic/Studnet.png", true, "Enter Student ID:", null, id -> {
                DataStore.Student s = DataStore.getStudentById(id.trim());
                if (s == null) {
                    JOptionPane.showMessageDialog(this, "Invalid Student ID!", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                return true;
            }, id -> {
                DataStore.Student s = DataStore.getStudentById(id.trim());
                new StudentDashboard(id.trim());
                dispose();
            });
        });

        setVisible(true);
    }

    // ================= STYLED BUTTON =================
    private JButton styledButton(String text, String iconPath) {
        ImageIcon icon = new ImageIcon(iconPath);
        Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        icon = new ImageIcon(img);

        JButton b = new JButton(text, icon);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setPreferredSize(new Dimension(180, 140));
        b.setFocusPainted(false);
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);

        Color normal = b.getBackground();
        Color hover = new Color(135, 206, 250);

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            public void mouseExited(MouseEvent e) { b.setBackground(normal); }
        });

        return b;
    }

    // ================= CUSTOM LOGIN DIALOG =================
    private void showLoginDialog(String role, String iconPath, boolean askID, String idPrompt,
                                 String correctPassword, Runnable onSuccess) {
        showLoginDialog(role, iconPath, askID, idPrompt, correctPassword, null, id -> onSuccess.run());
    }

    private void showLoginDialog(String role, String iconPath, boolean askID, String idPrompt,
                                 String password, java.util.function.Function<String, Boolean> idValidator,
                                 java.util.function.Consumer<String> onSuccess) {

        JDialog dialog = new JDialog(this, role + " Login", true);
        dialog.setSize(420, askID ? 220 : 170);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setLocationRelativeTo(this);

        // Role Icon
        JPanel leftPanel = new JPanel();
        ImageIcon ic = new ImageIcon(iconPath);
        Image im = ic.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        leftPanel.add(new JLabel(new ImageIcon(im)));

        // Input Fields
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField idField = new JTextField(15);
        JPasswordField pwdField = new JPasswordField(15);

        if (askID) {
            gbc.gridx = 0; gbc.gridy = 0;
            inputPanel.add(new JLabel(idPrompt), gbc);
            gbc.gridx = 1; gbc.gridy = 0;
            inputPanel.add(idField, gbc);
        }

        gbc.gridx = 0; gbc.gridy = askID ? 1 : 0;
        inputPanel.add(new JLabel("Enter Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = askID ? 1 : 0;
        inputPanel.add(pwdField, gbc);

        // Buttons
        JPanel btnPanel = new JPanel();
        JButton okBtn = new JButton("Login");
        JButton cancelBtn = new JButton("Cancel");
        okBtn.setPreferredSize(new Dimension(80, 25));
        cancelBtn.setPreferredSize(new Dimension(80, 25));
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);

        dialog.add(leftPanel, BorderLayout.WEST);
        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        // ENTER key submits
        idField.addActionListener(e -> okBtn.doClick());
        pwdField.addActionListener(e -> okBtn.doClick());

        okBtn.addActionListener(e -> {
            String id = idField.getText();
            String pass = new String(pwdField.getPassword());

            if (askID) {
                if (id.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "ID cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!idValidator.apply(id)) return;
            }

            // Check password
            String correctPass = askID ? null : password; // For admin
            if (!askID) correctPass = password;
            else {
                // For Teacher/Student get actual password
                if (role.equals("Teacher")) correctPass = DataStore.getTeacherById(id.trim()).password;
                else if (role.equals("Student")) correctPass = DataStore.getStudentById(id.trim()).password;
            }

            if (pass.equals(correctPass)) {
                dialog.dispose();
                onSuccess.accept(id);
            } else {
                JOptionPane.showMessageDialog(dialog, "Incorrect password!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        new LoginScreen();
    }
}
