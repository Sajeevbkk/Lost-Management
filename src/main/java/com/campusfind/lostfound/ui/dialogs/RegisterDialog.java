package com.campusfind.lostfound.ui.dialogs;

import com.campusfind.lostfound.config.AppTheme;
import com.campusfind.lostfound.model.Role;
import com.campusfind.lostfound.model.User;
import com.campusfind.lostfound.service.AuthService;
import com.campusfind.lostfound.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegisterDialog extends JDialog {

    private final AuthService authService = AuthService.getInstance();
    private final JTextField nameField = UIUtils.createTextField(20);
    private final JTextField emailField = UIUtils.createTextField(20);
    private final JTextField campusIdField = UIUtils.createTextField(20);
    private final JTextField deptField = UIUtils.createTextField(20);
    private final JTextField phoneField = UIUtils.createTextField(20);
    private final JPasswordField passField = UIUtils.createPasswordField(20);
    private final JComboBox<Role> roleCombo = new JComboBox<>(new Role[]{Role.STUDENT, Role.STAFF});
    private final JLabel errorLabel = new JLabel(" ");

    public RegisterDialog(Frame parent) {
        super(parent, "Register Campus Account", true);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppTheme.BG_MAIN);
        setSize(480, 620);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(AppTheme.BG_MAIN);
        mainPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel titleLabel = new JLabel("Create Campus Member Account");
        titleLabel.setFont(AppTheme.FONT_HEADER);
        titleLabel.setForeground(AppTheme.PRIMARY_NAVY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLabel = new JLabel("Join your campus recovery network");
        subLabel.setFont(AppTheme.FONT_REGULAR);
        subLabel.setForeground(AppTheme.TEXT_MUTED);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(subLabel);
        mainPanel.add(Box.createVerticalStrut(14));

        JPanel formCard = UIUtils.createCard();
        formCard.setLayout(new GridLayout(7, 2, 10, 10));
        formCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        formCard.add(new JLabel("Full Name:"));
        formCard.add(nameField);

        formCard.add(new JLabel("College Email:"));
        formCard.add(emailField);

        formCard.add(new JLabel("Campus ID / Roll No:"));
        formCard.add(campusIdField);

        formCard.add(new JLabel("Department / Faculty:"));
        formCard.add(deptField);

        formCard.add(new JLabel("Phone Number:"));
        formCard.add(phoneField);

        formCard.add(new JLabel("Campus Role:"));
        roleCombo.setFont(AppTheme.FONT_REGULAR);
        formCard.add(roleCombo);

        formCard.add(new JLabel("Password:"));
        formCard.add(passField);

        mainPanel.add(formCard);
        mainPanel.add(Box.createVerticalStrut(8));

        errorLabel.setFont(AppTheme.FONT_SMALL);
        errorLabel.setForeground(AppTheme.DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(errorLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        JButton registerBtn = UIUtils.createButton("Create Account", AppTheme.SECONDARY_PURPLE, Color.WHITE);
        registerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        registerBtn.addActionListener(e -> performRegister());

        mainPanel.add(registerBtn);
        mainPanel.add(Box.createVerticalStrut(10));

        JButton backToLogin = UIUtils.createOutlineButton("Back to Sign In", AppTheme.PRIMARY_NAVY);
        backToLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        backToLogin.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));
        backToLogin.addActionListener(e -> {
            dispose();
            new LoginDialog((Frame) getParent()).setVisible(true);
        });
        mainPanel.add(backToLogin);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void performRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String campusId = campusIdField.getText().trim();
        String dept = deptField.getText().trim();
        String phone = phoneField.getText().trim();
        String pass = new String(passField.getPassword());
        Role role = (Role) roleCombo.getSelectedItem();

        if (name.isEmpty() || email.isEmpty() || campusId.isEmpty() || dept.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("All fields are required.");
            return;
        }

        if (!email.contains("@")) {
            errorLabel.setText("Please enter a valid email address.");
            return;
        }

        try {
            authService.register(name, email, campusId, dept, phone, pass, role);
            JOptionPane.showMessageDialog(this,
                    "Account registered successfully! Welcome to CampusFind, " + name + ".",
                    "Registration Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            errorLabel.setText(ex.getMessage());
        }
    }
}
