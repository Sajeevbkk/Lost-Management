package com.campusfind.lostfound.ui;

import com.campusfind.lostfound.config.AppTheme;
import com.campusfind.lostfound.model.User;
import com.campusfind.lostfound.service.AuthService;
import com.campusfind.lostfound.service.NotificationService;
import com.campusfind.lostfound.ui.dialogs.LoginDialog;
import com.campusfind.lostfound.ui.dialogs.NotificationsDialog;
import com.campusfind.lostfound.ui.dialogs.RegisterDialog;
import com.campusfind.lostfound.ui.dialogs.ReportItemDialog;
import com.campusfind.lostfound.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TopBarPanel extends JPanel {

    private final MainFrame mainFrame;
    private final AuthService authService = AuthService.getInstance();
    private final NotificationService notificationService = NotificationService.getInstance();

    private final JButton notifBellBtn = new JButton("🔔 Alerts (0)");
    private final JComboBox<String> demoSwitcherCombo = new JComboBox<>(new String[]{
            "Quick Demo Switcher...",
            "Rahul Sharma (CS Student)",
            "Priya Patel (EC Student)",
            "Dr. Anil Verma (ME Faculty)",
            "Sneha Reddy (IT Student)",
            "Prof. Arvind (Campus Admin)"
    });

    private final JPanel authArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

    public TopBarPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER));
        setPreferredSize(new Dimension(800, 58));

        initComponents();

        authService.addAuthListener(this::updateState);
        notificationService.addNotificationListener(this::updateNotifBadge);

        updateState();
        updateNotifBadge();
    }

    private void initComponents() {
        // Left Side: Fast "+ Report Item" shortcut
        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        leftSide.setBackground(Color.WHITE);

        JButton fastReportBtn = UIUtils.createButton("+ Report Belonging", AppTheme.PRIMARY_NAVY, Color.WHITE);
        fastReportBtn.setFont(AppTheme.FONT_SMALL);
        fastReportBtn.addActionListener(e -> new ReportItemDialog(mainFrame, false).setVisible(true));

        leftSide.add(fastReportBtn);
        add(leftSide, BorderLayout.WEST);

        // Right Side: Demo Switcher + Notification Bell + Auth Buttons
        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        rightSide.setBackground(Color.WHITE);

        // Demo User Switcher
        demoSwitcherCombo.setFont(AppTheme.FONT_SMALL);
        demoSwitcherCombo.addActionListener(e -> {
            int idx = demoSwitcherCombo.getSelectedIndex();
            if (idx == 1) authService.quickSwitchUser("rahul.sharma@college.edu");
            else if (idx == 2) authService.quickSwitchUser("priya.patel@college.edu");
            else if (idx == 3) authService.quickSwitchUser("anil.verma@college.edu");
            else if (idx == 4) authService.quickSwitchUser("sneha.reddy@college.edu");
            else if (idx == 5) authService.quickSwitchUser("admin@college.edu");
        });
        rightSide.add(demoSwitcherCombo);

        // Notification Bell
        notifBellBtn.setFont(AppTheme.FONT_BOLD);
        notifBellBtn.setBackground(new Color(0xEE, 0xF2, 0xFF));
        notifBellBtn.setForeground(AppTheme.PRIMARY_NAVY);
        notifBellBtn.setFocusPainted(false);
        notifBellBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        notifBellBtn.addActionListener(e -> new NotificationsDialog(mainFrame).setVisible(true));
        rightSide.add(notifBellBtn);

        // Auth Buttons
        authArea.setBackground(Color.WHITE);
        rightSide.add(authArea);

        add(rightSide, BorderLayout.EAST);
    }

    private void updateState() {
        authArea.removeAll();
        User current = authService.getCurrentUser();
        if (current == null) {
            JButton loginBtn = UIUtils.createOutlineButton("Sign In", AppTheme.PRIMARY_NAVY);
            loginBtn.setFont(AppTheme.FONT_SMALL);
            loginBtn.addActionListener(e -> new LoginDialog(mainFrame).setVisible(true));

            JButton regBtn = UIUtils.createButton("Register", AppTheme.SECONDARY_PURPLE, Color.WHITE);
            regBtn.setFont(AppTheme.FONT_SMALL);
            regBtn.addActionListener(e -> new RegisterDialog(mainFrame).setVisible(true));

            authArea.add(loginBtn);
            authArea.add(regBtn);
        } else {
            JLabel badge = UIUtils.createBadge(current.getRole().name(),
                    current.getRole().name().equals("ADMIN") ? AppTheme.DANGER_LIGHT : AppTheme.PURPLE_LIGHT,
                    current.getRole().name().equals("ADMIN") ? AppTheme.DANGER : AppTheme.SECONDARY_PURPLE);
            authArea.add(badge);
        }

        updateNotifBadge();
        authArea.revalidate();
        authArea.repaint();
    }

    private void updateNotifBadge() {
        User current = authService.getCurrentUser();
        if (current != null) {
            int unread = notificationService.getUnreadCount(current.getUserId());
            if (unread > 0) {
                notifBellBtn.setText("🔔 Alerts (" + unread + ")");
                notifBellBtn.setBackground(new Color(0xFE, 0xF2, 0xF2));
                notifBellBtn.setForeground(AppTheme.DANGER);
            } else {
                notifBellBtn.setText("🔔 Alerts");
                notifBellBtn.setBackground(new Color(0xF1, 0xF5, 0xF9));
                notifBellBtn.setForeground(AppTheme.TEXT_MUTED);
            }
        } else {
            notifBellBtn.setText("🔔 Alerts");
            notifBellBtn.setBackground(new Color(0xF1, 0xF5, 0xF9));
            notifBellBtn.setForeground(AppTheme.TEXT_MUTED);
        }
    }
}
