package com.campusfind.lostfound.ui;

import com.campusfind.lostfound.config.AppTheme;
import com.campusfind.lostfound.model.User;
import com.campusfind.lostfound.service.AuthService;
import com.campusfind.lostfound.ui.dialogs.LoginDialog;
import com.campusfind.lostfound.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SidebarPanel extends JPanel {

    private final MainFrame mainFrame;
    private final AuthService authService = AuthService.getInstance();
    private final Map<String, JButton> navButtons = new HashMap<>();
    private String activeView = "home";

    private final JLabel lblUserName = new JLabel("Not Signed In");
    private final JLabel lblUserRole = new JLabel("Guest");
    private final JButton authActionBtn = new JButton("Sign In");

    public SidebarPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_SIDEBAR);
        setPreferredSize(new Dimension(240, 700));

        initComponents();

        authService.addAuthListener(this::updateUserProfile);
        updateUserProfile();
    }

    private void initComponents() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(AppTheme.BG_SIDEBAR);
        contentPanel.setBorder(new EmptyBorder(20, 16, 20, 16));

        // Brand Logo & Title
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setBackground(AppTheme.BG_SIDEBAR);
        brandPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brandIcon = new JLabel("🔍");
        brandIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JPanel brandText = new JPanel();
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        brandText.setBackground(AppTheme.BG_SIDEBAR);

        JLabel titleLbl = new JLabel("CampusFind");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 19));
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel("Smart Lost & Found");
        subLbl.setFont(AppTheme.FONT_SMALL);
        subLbl.setForeground(new Color(0x94, 0xA3, 0xB8));

        brandText.add(titleLbl);
        brandText.add(subLbl);

        brandPanel.add(brandIcon);
        brandPanel.add(brandText);

        contentPanel.add(brandPanel);
        contentPanel.add(Box.createVerticalStrut(28));

        // Navigation Items
        addNavItem(contentPanel, "home", "Home Dashboard");
        addNavItem(contentPanel, "lost", "Lost Belongings");
        addNavItem(contentPanel, "found", "Discovered Items");
        addNavItem(contentPanel, "matches", "Smart AI Matches");
        addNavItem(contentPanel, "user-dashboard", "My Reports & Claims");
        addNavItem(contentPanel, "admin-dashboard", "Admin Console 🛡");
        addNavItem(contentPanel, "about", "About & Docs");

        add(contentPanel, BorderLayout.CENTER);

        // Bottom User Profile Card
        JPanel bottomProfile = new JPanel(new BorderLayout(8, 6));
        bottomProfile.setBackground(new Color(0x1E, 0x29, 0x3B));
        bottomProfile.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel userText = new JPanel();
        userText.setLayout(new BoxLayout(userText, BoxLayout.Y_AXIS));
        userText.setBackground(new Color(0x1E, 0x29, 0x3B));

        lblUserName.setFont(AppTheme.FONT_BOLD);
        lblUserName.setForeground(Color.WHITE);

        lblUserRole.setFont(AppTheme.FONT_SMALL);
        lblUserRole.setForeground(new Color(0x94, 0xA3, 0xB8));

        userText.add(lblUserName);
        userText.add(Box.createVerticalStrut(2));
        userText.add(lblUserRole);

        authActionBtn.setFont(AppTheme.FONT_SMALL);
        authActionBtn.setForeground(Color.WHITE);
        authActionBtn.setBackground(AppTheme.BG_SIDEBAR_ACTIVE);
        authActionBtn.setFocusPainted(false);
        authActionBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        authActionBtn.addActionListener(e -> {
            if (authService.isLoggedIn()) {
                authService.logout();
            } else {
                new LoginDialog(mainFrame).setVisible(true);
            }
        });

        bottomProfile.add(userText, BorderLayout.CENTER);
        bottomProfile.add(authActionBtn, BorderLayout.EAST);

        add(bottomProfile, BorderLayout.SOUTH);
    }

    private void addNavItem(JPanel container, String viewKey, String label) {
        JButton btn = new JButton(label);
        btn.setFont(AppTheme.FONT_BOLD);
        btn.setForeground(new Color(0xCB, 0xD5, 0xE1)); // Slate 300
        btn.setBackground(AppTheme.BG_SIDEBAR);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addActionListener(e -> {
            mainFrame.showView(viewKey);
            setActiveView(viewKey);
        });

        navButtons.put(viewKey, btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(4));
    }

    public void setActiveView(String viewKey) {
        this.activeView = viewKey;
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equalsIgnoreCase(viewKey)) {
                btn.setBackground(AppTheme.BG_SIDEBAR_ACTIVE);
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(AppTheme.BG_SIDEBAR);
                btn.setForeground(new Color(0xCB, 0xD5, 0xE1));
            }
        }
    }

    private void updateUserProfile() {
        User user = authService.getCurrentUser();
        if (user != null) {
            lblUserName.setText(user.getFullName());
            lblUserRole.setText(user.getCampusId() + " (" + user.getRole().name() + ")");
            authActionBtn.setText("Sign Out");
            authActionBtn.setBackground(new Color(0x47, 0x55, 0x69));
        } else {
            lblUserName.setText("Guest Member");
            lblUserRole.setText("Not Logged In");
            authActionBtn.setText("Sign In");
            authActionBtn.setBackground(AppTheme.BG_SIDEBAR_ACTIVE);
        }
        revalidate();
        repaint();
    }
}
