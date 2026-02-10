import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class StudentGUI extends JFrame {
    private Student currentStudent;

    // Constructor: Opens the Dashboard for a logged-in student
    public StudentGUI(Student student) {
        this.currentStudent = student;

        setTitle("AFS - Student Dashboard: " + student.getName());
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2, 10, 10));

        // Buttons per "Student" Role requirements (Page 2 & 14)
        JButton btnProfile = new JButton("Update Profile");
        JButton btnRegisterClasses = new JButton("Register for Classes");
        JButton btnViewResults = new JButton("View Results");
        JButton btnSubmitComments = new JButton("Submit Comments");
        JButton btnLogout = new JButton("Logout");

        add(btnProfile);
        add(btnRegisterClasses);
        add(btnViewResults);
        add(btnSubmitComments);
        add(btnLogout);

        // --- Action Listeners ---

        btnProfile.addActionListener(e -> showUpdateProfileDialog());
        btnRegisterClasses.addActionListener(e -> showRegisterClassDialog());
        btnViewResults.addActionListener(e -> showResultsWindow());
        btnSubmitComments.addActionListener(e -> showCommentDialog());
        
        btnLogout.addActionListener(e -> {
            this.dispose();
            new LoginGUI();
        });

        setVisible(true);
    }


    // GUI: UPDATE PROFILE
    private void showUpdateProfileDialog() {
        // Simple inputs for update
        JTextField txtName = new JTextField(currentStudent.getName());
        JTextField txtEmail = new JTextField(); // Ideally pre-fill
        JTextField txtPhone = new JTextField();
        JTextField txtAge = new JTextField();
        
        Object[] message = {
            "Name:", txtName,
            "Email:", txtEmail,
            "Phone:", txtPhone,
            "Age:", txtAge
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Update Profile", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            if(Helpers.validateEmail(txtEmail.getText())) {
                currentStudent.updateProfile("newPass123", txtName.getText(), "Male", txtEmail.getText(), txtPhone.getText(), txtAge.getText());
                JOptionPane.showMessageDialog(this, "Profile Updated.");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Email.");
            }
        }
    }

    // GUI: REGISTER FOR CLASS
    private void showRegisterClassDialog() {
        // In a real app, you would load Class IDs from classes.txt into a ComboBox
        // Here we use a text field for simplicity per assignment requirements
        String classID = JOptionPane.showInputDialog(this, "Enter Class ID (e.g., CLS01):");
        if (classID != null && !classID.trim().isEmpty()) {
            boolean success = currentStudent.registerForClasses(classID.trim());
            if (success) {
                JOptionPane.showMessageDialog(this, "Successfully Enrolled!");
            } else {
                JOptionPane.showMessageDialog(this, "Already enrolled or Error.");
            }
        }
    }

    // GUI: SUBMIT COMMENTS
    private void showCommentDialog() {
        JTextField txtLecID = new JTextField();
        JTextArea txtComment = new JTextArea(5, 20);
        
        Object[] message = {
            "Lecturer ID:", txtLecID,
            "Comment:", new JScrollPane(txtComment)
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Submit Feedback", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            currentStudent.submitComments(txtLecID.getText(), txtComment.getText());
            JOptionPane.showMessageDialog(this, "Feedback submitted.");
        }
    }

    // GUI: VIEW RESULTS (JTable)
    private void showResultsWindow() {
        JFrame resFrame = new JFrame("My Results");
        resFrame.setSize(500, 300);
        
        String[] columns = {"Module ID", "Assessment ID", "Marks", "Feedback"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        
        List<String[]> data = currentStudent.viewResults();
        for (String[] row : data) {
            // results.txt format: studentID|moduleID|assessmentID|marks|feedback
            // We want indices 1, 2, 3, 4
            model.addRow(new Object[]{row[1], row[2], row[3], row[4]});
        }
        
        JTable table = new JTable(model);
        resFrame.add(new JScrollPane(table));
        resFrame.setVisible(true);
    }
}