package com.campusfind.lostfound.ui.dialogs;

import com.campusfind.lostfound.config.AppTheme;
import com.campusfind.lostfound.model.User;
import com.campusfind.lostfound.service.AuthService;
import com.campusfind.lostfound.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginDialog extends JDialog {

    private final AuthService authService = AuthService.getInstance();
    private final JTextField emailField = UIUtils.createTextField(24);
    private final JPasswordField passwordField = UIUtils.createPasswordField(24);
    private final JLabel errorLabel = new JLabel(" ");
    private boolean succeeded = false;

    public LoginDialog(Frame parent) {
        super(parent, "Sign In to CampusFind", true);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppTheme.BG_MAIN);
        setSize(460, 520);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(AppTheme.BG_MAIN);
        mainPanel.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header Title & Subtitle
        JLabel titleLabel = new JLabel("Campus Authentication");
        titleLabel.setFont(AppTheme.FONT_HEADER_LARGE);
        titleLabel.setForeground(AppTheme.PRIMARY_NAVY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Sign in with your university credentials or demo accounts");
        subtitleLabel.setFont(AppTheme.FONT_REGULAR);
        subtitleLabel.setForeground(AppTheme.TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Input Form Card
        JPanel formCard = UIUtils.createCard();
        formCard.setLayout(new GridLayout(4, 1, 6, 6));
        formCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel emailLabel = new JLabel("College Email Address");
        emailLabel.setFont(AppTheme.FONT_BOLD);
        emailLabel.setForeground(AppTheme.TEXT_DARK);

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(AppTheme.FONT_BOLD);
        passLabel.setForeground(AppTheme.TEXT_DARK);

        formCard.add(emailLabel);
        formCard.add(emailField);
        formCard.add(passLabel);
        formCard.add(passwordField);

        mainPanel.add(formCard);
        mainPanel.add(Box.createVerticalStrut(8));

        // Error message label
        errorLabel.setFont(AppTheme.FONT_SMALL);
        errorLabel.setForeground(AppTheme.DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(errorLabel);
        mainPanel.add(Box.createVerticalStrut(8));

        // Login Action Button
        JButton loginBtn = UIUtils.createButton("Sign In", AppTheme.PRIMARY_NAVY, Color.WHITE);
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        loginBtn.addActionListener(e -> performLogin());

        // Quick Demo Accounts Panel
        JPanel demoPanel = UIUtils.createCard();
        demoPanel.setLayout(new BoxLayout(demoPanel, BoxLayout.Y_AXIS));
        demoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel demoHeader = new JLabel("Quick 1-Click Demo Profiles:");
        demoHeader.setFont(AppTheme.FONT_BOLD);
        demoHeader.setForeground(AppTheme.TEXT_MUTED);

        JPanel demoBtns = new JPanel(new GridLayout(2, 2, 6, 6));
        demoBtns.setBackground(AppTheme.BG_CARD);

        JButton btnAdmin = createDemoButton("Admin (Security)", "admin@college.edu", "Admin@123");
        JButton btnRahul = createDemoButton("Rahul (Student)", "rahul.sharma@college.edu", "Student@123");
        JButton btnPriya = createDemoButton("Priya (Student)", "priya.patel@college.edu", "Student@123");
        JButton btnAnil = createDemoButton("Dr. Anil (Faculty)", "anil.verma@college.edu", "Staff@123");

        demoBtns.add(btnAdmin);
        demoBtns.add(btnRahul);
        demoBtns.add(btnPriya);
        demoBtns.add(btnAnil);

        demoPanel.add(demoHeader);
        demoPanel.add(Box.createVerticalStrut(8));
        demoPanel.add(demoBtns);

        mainPanel.add(loginBtn);
        mainPanel.add(Box.createVerticalStrut(16));
        mainPanel.add(demoPanel);
        mainPanel.add(Box.createVerticalStrut(12));

        // Switch to Register Link
        JPanel bottomLinkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomLinkPanel.setBackground(AppTheme.BG_MAIN);
        bottomLinkPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel noAccLabel = new JLabel("New to CampusFind? ");
        noAccLabel.setFont(AppTheme.FONT_REGULAR);
        noAccLabel.setForeground(AppTheme.TEXT_MUTED);

        JButton regLinkBtn = new JButton("Create an Account");
        regLinkBtn.setFont(AppTheme.FONT_BOLD);
        regLinkBtn.setForeground(AppTheme.SECONDARY_PURPLE);
        regLinkBtn.setBorderPainted(false);
        regLinkBtn.setContentAreaFilled(false);
        regLinkBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        regLinkBtn.addActionListener(e -> {
            dispose();
            new RegisterDialog((Frame) getParent()).setVisible(true);
        });

        bottomLinkPanel.add(noAccLabel);
        bottomLinkPanel.add(regLinkBtn);
        mainPanel.add(bottomLinkPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JButton createDemoButton(String label, String email, String password) {
        JButton btn = new JButton(label);
        btn.setFont(AppTheme.FONT_SMALL);
        btn.setBackground(new Color(0xF1, 0xF5, 0xF9));
        btn.setForeground(AppTheme.PRIMARY_NAVY);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            emailField.setText(email);
            passwordField.setText(password);
            performLogin();
        });
        return btn;
    }

    private void performLogin() {
        String email = emailField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (email.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please enter both email and password.");
            return;
        }

        boolean ok = authService.login(email, pass);
        if (ok) {
            succeeded = true;
            dispose();
        } else {
            errorLabel.setText("Invalid credentials. Try demo accounts above.");
        }
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}
