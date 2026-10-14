package com.campusfind.lostfound.ui.dialogs;

import com.campusfind.lostfound.config.AppTheme;
import com.campusfind.lostfound.model.Notification;
import com.campusfind.lostfound.model.User;
import com.campusfind.lostfound.service.AuthService;
import com.campusfind.lostfound.service.NotificationService;
import com.campusfind.lostfound.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class NotificationsDialog extends JDialog {

    private final AuthService authService = AuthService.getInstance();
    private final NotificationService notificationService = NotificationService.getInstance();
    private final JPanel listContainer = new JPanel();

    public NotificationsDialog(Frame parent) {
        super(parent, "Campus Notifications & Match Alerts", true);
        initComponents();
        loadNotifications();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppTheme.BG_MAIN);
        setSize(520, 580);
        setLocationRelativeTo(getParent());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(AppTheme.BG_MAIN);
        topPanel.setBorder(new EmptyBorder(16, 20, 12, 20));

        JLabel title = new JLabel("Campus Alerts & Notifications");
        title.setFont(AppTheme.FONT_HEADER);
        title.setForeground(AppTheme.PRIMARY_NAVY);

        JButton markAllBtn = UIUtils.createOutlineButton("Mark All as Read", AppTheme.PRIMARY_NAVY);
        markAllBtn.addActionListener(e -> {
            User current = authService.getCurrentUser();
            if (current != null) {
                notificationService.markAllAsRead(current.getUserId());
                loadNotifications();
            }
        });

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(markAllBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(AppTheme.BG_MAIN);
        listContainer.setBorder(new EmptyBorder(8, 16, 16, 16));

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadNotifications() {
        listContainer.removeAll();
        User current = authService.getCurrentUser();
        if (current == null) {
            JLabel emptyLbl = new JLabel("Please sign in to view your notifications.");
            emptyLbl.setFont(AppTheme.FONT_REGULAR);
            listContainer.add(emptyLbl);
            listContainer.revalidate();
            listContainer.repaint();
            return;
        }

        List<Notification> list = notificationService.getNotificationsForUser(current.getUserId());
        if (list.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBackground(AppTheme.BG_MAIN);
            JLabel emptyLbl = new JLabel("No alerts or notifications at this time.");
            emptyLbl.setFont(AppTheme.FONT_REGULAR);
            emptyLbl.setForeground(AppTheme.TEXT_MUTED);
            emptyPanel.add(emptyLbl);
            listContainer.add(emptyPanel);
        } else {
            for (Notification n : list) {
                listContainer.add(createNotificationCard(n));
                listContainer.add(Box.createVerticalStrut(8));
            }
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel createNotificationCard(Notification n) {
        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(8, 6));

        if (!n.getIsRead()) {
            card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(AppTheme.PRIMARY_NAVY, 1, true),
                    new EmptyBorder(12, 12, 12, 12)
            ));
            card.setBackground(new Color(0xF0, 0xF9, 0xFF));
        }

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel titleLbl = new JLabel(n.getTitle());
        titleLbl.setFont(AppTheme.FONT_BOLD);
        titleLbl.setForeground(n.getIsRead() ? AppTheme.TEXT_DARK : AppTheme.PRIMARY_NAVY);

        JLabel timeLbl = new JLabel(UIUtils.formatDateTime(n.getCreatedAt()));
        timeLbl.setFont(AppTheme.FONT_SMALL);
        timeLbl.setForeground(AppTheme.TEXT_MUTED);

        header.add(titleLbl, BorderLayout.WEST);
        header.add(timeLbl, BorderLayout.EAST);

        JTextArea msgArea = new JTextArea(n.getMessage());
        msgArea.setFont(AppTheme.FONT_REGULAR);
        msgArea.setEditable(false);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setOpaque(false);

        card.add(header, BorderLayout.NORTH);
        card.add(msgArea, BorderLayout.CENTER);

        if (!n.getIsRead()) {
            JButton readBtn = new JButton("Mark Read");
            readBtn.setFont(AppTheme.FONT_SMALL);
            readBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            readBtn.addActionListener(e -> {
                notificationService.markAsRead(n.getNotificationId());
                loadNotifications();
            });
            JPanel actionP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            actionP.setOpaque(false);
            actionP.add(readBtn);
            card.add(actionP, BorderLayout.SOUTH);
        }

        return card;
    }
}
