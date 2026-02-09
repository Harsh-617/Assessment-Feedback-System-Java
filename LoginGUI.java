import javax.swing.*;
import java.awt.*;

/**
 * LoginGUI.java - Login Interface
 * Entry point for users to access the system
 */
public class LoginGUI extends JFrame {
    
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    
    public LoginGUI() {
        setTitle("AFS - Assessment Feedback System");
        setSize(450, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        
        // Title Panel
        JPanel titlePanel = new JPanel();
        JLabel lblTitle = new JLabel("Assessment Feedback System");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(lblTitle);
        add(titlePanel, BorderLayout.NORTH);
        
        // Login Form Panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        JLabel lblEmail = new JLabel("Email:");
        txtEmail = new JTextField();
        
        JLabel lblPassword = new JLabel("Password:");
        txtPassword = new JPasswordField();
        
        formPanel.add(lblEmail);
        formPanel.add(txtEmail);
        formPanel.add(lblPassword);
        formPanel.add(txtPassword);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnLogin = new JButton("Login");
        JButton btnExit = new JButton("Exit");
        
        btnLogin.addActionListener(e -> handleLogin());
        btnExit.addActionListener(e -> System.exit(0));
        
        // Add Enter key support
        txtPassword.addActionListener(e -> handleLogin());
        
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnExit);
        add(buttonPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both email and password!", 
                "Login Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Attempt login
        boolean success = Main.loginUser(email, password);
        
        if (success) {
            // Login successful - Main.loginUser will open appropriate dashboard
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Invalid email or password!", 
                "Login Failed", 
                JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginGUI());
    }
}