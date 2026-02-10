import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AdminGUI.java — Admin Dashboard
 * Implements ALL admin requirements from the spec:
 *   - CRUD end users
 *   - Assign lecturers to academic leaders
 *   - Define APU grading system
 *   - Create new classes for modules
 */
public class AdminGUI extends JFrame {
    private AdminStaff logic;
    private String adminName;

    public AdminGUI(String id, String name) {
        this.adminName = name;
        this.logic = new AdminStaff(id);
        setTitle("AFS Admin Dashboard - " + name);
        setSize(950, 680);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        showMainMenu();
        setVisible(true);
    }

    // ==========================================
    // MAIN MENU
    // ==========================================
    private void showMainMenu() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new GridLayout(8, 1, 10, 10));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 200, 20, 200));

        JButton btnUsers      = new JButton("Manage Users (CRUD)");
        JButton btnAssignLead = new JButton("Assign Lecturer to Academic Leader");
        JButton btnGrading    = new JButton("Define Grading System");
        JButton btnClasses    = new JButton("Create Classes for Modules");
        JButton btnModules    = new JButton("View All Modules");
        JButton btnProfile    = new JButton("Update My Profile");
        JButton btnLogout     = new JButton("Logout");

        btnUsers.addActionListener(e -> showManageUsers());
        btnAssignLead.addActionListener(e -> showAssignLecturerToLeader());
        btnGrading.addActionListener(e -> showGradingSystem());
        btnClasses.addActionListener(e -> showManageClasses());
        btnModules.addActionListener(e -> showAllModules());
        btnProfile.addActionListener(e -> showUpdateProfile());
        btnLogout.addActionListener(e -> logout());

        menuPanel.add(new JLabel("Welcome, " + adminName, SwingConstants.CENTER));
        menuPanel.add(btnUsers);
        menuPanel.add(btnAssignLead);
        menuPanel.add(btnGrading);
        menuPanel.add(btnClasses);
        menuPanel.add(btnModules);
        menuPanel.add(btnProfile);
        menuPanel.add(btnLogout);

        add(menuPanel, BorderLayout.CENTER);
        revalidate(); repaint();
    }

    // ==========================================
    // MANAGE USERS
    // ==========================================
    private void showManageUsers() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Manage Users", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        String[] cols = {"User ID", "Role", "Name", "Gender", "Email", "Phone", "Age"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        refreshUserTable(model);

        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton btnCreate = new JButton("Create User");
        JButton btnEdit   = new JButton("Edit Selected");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnReset  = new JButton("Reset Password");
        JButton btnBack   = new JButton("Back");

        btnCreate.addActionListener(e -> { showCreateUserDialog(model); });

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a user."); return; }
            String[] d = { (String)model.getValueAt(row,0), (String)model.getValueAt(row,1),
                           (String)model.getValueAt(row,2), (String)model.getValueAt(row,3),
                           (String)model.getValueAt(row,4), (String)model.getValueAt(row,5),
                           (String)model.getValueAt(row,6) };
            showEditUserDialog(d, model);
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a user."); return; }
            String targetID = (String) model.getValueAt(row, 0);
            String targetName = (String) model.getValueAt(row, 2);
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete user: " + targetName + " (" + targetID + ")?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (logic.deleteUser(targetID)) {
                    JOptionPane.showMessageDialog(this, "User deleted.");
                    refreshUserTable(model);
                } else {
                    JOptionPane.showMessageDialog(this, "Cannot delete your own account.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnReset.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a user."); return; }
            String targetID = (String) model.getValueAt(row, 0);
            JPasswordField newPwd = new JPasswordField();
            int opt = JOptionPane.showConfirmDialog(this,
                new Object[]{"New Password for " + targetID + ":", newPwd},
                "Reset Password", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                String pwd = new String(newPwd.getPassword()).trim();
                if (logic.resetPassword(targetID, pwd)) {
                    JOptionPane.showMessageDialog(this, "Password reset successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Reset failed. Check User ID.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnBack.addActionListener(e -> showMainMenu());

        btnPanel.add(btnCreate); btnPanel.add(btnEdit);
        btnPanel.add(btnDelete); btnPanel.add(btnReset); btnPanel.add(btnBack);
        add(btnPanel, BorderLayout.SOUTH);
        revalidate(); repaint();
    }

    private void refreshUserTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (String[] u : logic.getAllUsers()) {
            model.addRow(new Object[]{u[0], u[1], u[3], u[4], u[5], u[6], u[7]});
        }
    }

    private void showCreateUserDialog(DefaultTableModel model) {
        String[]  roles   = {"Student", "Lecturer", "Academic Leader"};
        String[]  genders = {"Male", "Female"};
        JComboBox<String> cmbRole   = new JComboBox<>(roles);
        JComboBox<String> cmbGender = new JComboBox<>(genders);
        JTextField txtName  = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtPhone = new JTextField();
        JTextField txtAge   = new JTextField();
        JPasswordField txtPass = new JPasswordField();

        int opt = JOptionPane.showConfirmDialog(this,
            new Object[]{"Role:", cmbRole, "Name:", txtName, "Gender:", cmbGender,
                         "Email:", txtEmail, "Phone:", txtPhone, "Age:", txtAge, "Password:", txtPass},
            "Create New User", JOptionPane.OK_CANCEL_OPTION);

        if (opt == JOptionPane.OK_OPTION) {
            String newID = logic.createUser(
                (String)cmbRole.getSelectedItem(), txtName.getText().trim(),
                (String)cmbGender.getSelectedItem(), txtEmail.getText().trim(),
                txtPhone.getText().trim(), txtAge.getText().trim(),
                new String(txtPass.getPassword()));
            if (newID != null) {
                JOptionPane.showMessageDialog(this, "User created! ID: " + newID);
                refreshUserTable(model);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed. Ensure email is valid, unique, and no fields are empty.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditUserDialog(String[] d, DefaultTableModel model) {
        String[] genders = {"Male", "Female"};
        JTextField txtName  = new JTextField(d[2]);
        JTextField txtEmail = new JTextField(d[4]);
        JTextField txtPhone = new JTextField(d[5]);
        JTextField txtAge   = new JTextField(d[6]);
        JComboBox<String> cmbGender = new JComboBox<>(genders);
        cmbGender.setSelectedItem(d[3]);

        int opt = JOptionPane.showConfirmDialog(this,
            new Object[]{"Name:", txtName, "Gender:", cmbGender,
                         "Email:", txtEmail, "Phone:", txtPhone, "Age:", txtAge},
            "Edit User: " + d[0], JOptionPane.OK_CANCEL_OPTION);

        if (opt == JOptionPane.OK_OPTION) {
            if (logic.updateUser(d[0], txtName.getText().trim(),
                    (String)cmbGender.getSelectedItem(), txtEmail.getText().trim(),
                    txtPhone.getText().trim(), txtAge.getText().trim())) {
                JOptionPane.showMessageDialog(this, "User updated.");
                refreshUserTable(model);
            } else {
                JOptionPane.showMessageDialog(this, "Update failed. Check email is valid.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==========================================
    // ASSIGN LECTURER TO ACADEMIC LEADER
    // ==========================================
    private void showAssignLecturerToLeader() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Assign Lecturers to Academic Leaders", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        String[] cols = {"Academic Leader ID", "Lecturer ID"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (String[] row : logic.getAllLeaderAssignments()) model.addRow(row);

        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton btnAssign = new JButton("Assign");
        JButton btnBack   = new JButton("Back");

        btnAssign.addActionListener(e -> {
            // Build dropdowns from users.txt
            List<String> leaders   = new ArrayList<>();
            List<String> lecturers = new ArrayList<>();
            for (String[] u : logic.getAllUsers()) {
                if (u[1].equals("Academic Leader")) leaders.add(u[0] + " - " + u[3]);
                if (u[1].equals("Lecturer"))        lecturers.add(u[0] + " - " + u[3]);
            }
            if (leaders.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No Academic Leaders found. Create one first.");
                return;
            }
            if (lecturers.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No Lecturers found. Create one first.");
                return;
            }

            JComboBox<String> cmbLeader   = new JComboBox<>(leaders.toArray(new String[0]));
            JComboBox<String> cmbLecturer = new JComboBox<>(lecturers.toArray(new String[0]));

            int opt = JOptionPane.showConfirmDialog(this,
                new Object[]{"Academic Leader:", cmbLeader, "Lecturer:", cmbLecturer},
                "Assign Lecturer to Leader", JOptionPane.OK_CANCEL_OPTION);

            if (opt == JOptionPane.OK_OPTION) {
                String leaderID   = ((String)cmbLeader.getSelectedItem()).split(" - ")[0];
                String lecturerID = ((String)cmbLecturer.getSelectedItem()).split(" - ")[0];

                if (logic.assignLecturerToLeader(leaderID, lecturerID)) {
                    JOptionPane.showMessageDialog(this, "Lecturer assigned to Academic Leader!");
                    model.setRowCount(0);
                    for (String[] row : logic.getAllLeaderAssignments()) model.addRow(row);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Assignment failed. This pairing may already exist or IDs are invalid.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnBack.addActionListener(e -> showMainMenu());
        btnPanel.add(btnAssign); btnPanel.add(btnBack);
        add(btnPanel, BorderLayout.SOUTH);
        revalidate(); repaint();
    }

    // ==========================================
    // GRADING SYSTEM
    // ==========================================
    private void showGradingSystem() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Define APU Grading System", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        String[] cols = {"Grade", "Min Mark", "Max Mark"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for (String[] g : logic.getGradingSystem()) model.addRow(new Object[]{g[0], g[1], g[2]});

        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton btnAdd    = new JButton("Add Row");
        JButton btnDelete = new JButton("Delete Row");
        JButton btnSave   = new JButton("Save");
        JButton btnBack   = new JButton("Back");

        btnAdd.addActionListener(e -> model.addRow(new Object[]{"", "", ""}));

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
            model.removeRow(row);
        });

        btnSave.addActionListener(e -> {
            List<String> lines = new ArrayList<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                String grade = model.getValueAt(i,0).toString().trim();
                String min   = model.getValueAt(i,1).toString().trim();
                String max   = model.getValueAt(i,2).toString().trim();
                if (grade.isEmpty() || min.isEmpty() || max.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Row " + (i+1) + " has empty fields.");
                    return;
                }
                try {
                    double lo = Double.parseDouble(min), hi = Double.parseDouble(max);
                    if (lo > hi) { JOptionPane.showMessageDialog(this, "Row " + (i+1) + ": Min > Max."); return; }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Row " + (i+1) + ": Min/Max must be numbers."); return;
                }
                lines.add(grade + "|" + min + "|" + max);
            }
            if (logic.saveGradingSystem(lines)) JOptionPane.showMessageDialog(this, "Grading system saved!");
        });

        btnBack.addActionListener(e -> showMainMenu());
        btnPanel.add(btnAdd); btnPanel.add(btnDelete); btnPanel.add(btnSave); btnPanel.add(btnBack);
        add(btnPanel, BorderLayout.SOUTH);
        revalidate(); repaint();
    }

    // ==========================================
    // CREATE / MANAGE CLASSES
    // ==========================================
    private void showManageClasses() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Create Classes for Modules", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        String[] cols = {"Class ID", "Class Name", "Module ID"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (String[] cls : logic.getAllClasses()) model.addRow(new Object[]{cls[0], cls[1], cls[2]});

        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton btnCreate = new JButton("Create Class");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnBack   = new JButton("Back");

        btnCreate.addActionListener(e -> {
            List<String[]> modules = logic.getAllModules();
            if (modules.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "No modules exist yet. An Academic Leader must create modules first.");
                return;
            }
            String[] modOptions = new String[modules.size()];
            for (int i = 0; i < modules.size(); i++)
                modOptions[i] = modules.get(i)[0] + " - " + modules.get(i)[1];

            JTextField txtName = new JTextField();
            JComboBox<String> cmbModule = new JComboBox<>(modOptions);

            int opt = JOptionPane.showConfirmDialog(this,
                new Object[]{"Class Name:", txtName, "Module:", cmbModule},
                "Create Class", JOptionPane.OK_CANCEL_OPTION);

            if (opt == JOptionPane.OK_OPTION) {
                String moduleID = ((String)cmbModule.getSelectedItem()).split(" - ")[0];
                String classID  = logic.createClass(txtName.getText().trim(), moduleID);
                if (classID != null) {
                    JOptionPane.showMessageDialog(this, "Class created! ID: " + classID);
                    model.setRowCount(0);
                    for (String[] cls : logic.getAllClasses()) model.addRow(new Object[]{cls[0],cls[1],cls[2]});
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed. Class name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select a class first."); return; }
            String classID = (String) model.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete class " + classID + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                logic.deleteClass(classID);
                model.removeRow(row);
            }
        });

        btnBack.addActionListener(e -> showMainMenu());
        btnPanel.add(btnCreate); btnPanel.add(btnDelete); btnPanel.add(btnBack);
        add(btnPanel, BorderLayout.SOUTH);
        revalidate(); repaint();
    }

    // ==========================================
    // VIEW ALL MODULES
    // ==========================================
    private void showAllModules() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JLabel title = new JLabel("All Modules", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        String[] cols = {"Module ID", "Module Name", "Leader ID"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        List<String[]> modules = logic.getAllModules();
        if (modules.isEmpty()) model.addRow(new Object[]{"No modules found", "", ""});
        else for (String[] m : modules) model.addRow(new Object[]{m[0], m[1], m[2]});

        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);

        JButton btnBack = new JButton("Back");
        btnBack.addActionListener(e -> showMainMenu());
        JPanel p = new JPanel(new FlowLayout()); p.add(btnBack);
        add(p, BorderLayout.SOUTH);
        revalidate(); repaint();
    }

    // ==========================================
    // UPDATE OWN PROFILE
    // ==========================================
    private void showUpdateProfile() {
        JTextField txtName  = new JTextField(logic.getName());
        JTextField txtEmail = new JTextField(logic.getEmail());
        JTextField txtPhone = new JTextField(logic.getPhone());
        JTextField txtAge   = new JTextField(logic.getAge());

        int opt = JOptionPane.showConfirmDialog(this,
            new Object[]{"Name:", txtName, "Email:", txtEmail, "Phone:", txtPhone, "Age:", txtAge},
            "Update Profile", JOptionPane.OK_CANCEL_OPTION);

        if (opt == JOptionPane.OK_OPTION) {
            if (logic.updateProfile(txtName.getText().trim(), txtEmail.getText().trim(),
                                    txtPhone.getText().trim(), txtAge.getText().trim())) {
                JOptionPane.showMessageDialog(this, "Profile updated!");
            } else {
                JOptionPane.showMessageDialog(this,
                    "Update failed. Check email is valid.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==========================================
    // LOGOUT
    // ==========================================
    private void logout() {
        new LoginGUI().setVisible(true);
        this.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminGUI("ADM01", "System Admin"));
    }
}