package com.campusfind.lostfound.ui.views;

import com.campusfind.lostfound.config.AppTheme;
import com.campusfind.lostfound.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AboutView extends JPanel {

    public AboutView() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_MAIN);
        initComponents();
    }

    private void initComponents() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(AppTheme.BG_MAIN);
        scrollContent.setBorder(new EmptyBorder(24, 28, 28, 28));

        // Header Title
        JLabel title = new JLabel("About Smart Lost & Found Management System");
        title.setFont(AppTheme.FONT_HEADER_LARGE);
        title.setForeground(AppTheme.PRIMARY_NAVY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("CampusFind — Pure Java Swing Enterprise Campus Recovery Desktop Application");
        sub.setFont(AppTheme.FONT_REGULAR);
        sub.setForeground(AppTheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        scrollContent.add(title);
        scrollContent.add(Box.createVerticalStrut(4));
        scrollContent.add(sub);
        scrollContent.add(Box.createVerticalStrut(20));

        // Feature 1: Multi-Factor AI Matching Engine
        scrollContent.add(createSectionCard(
                "1. Multi-Factor AI Similarity Engine",
                "CampusFind features an intelligent matching algorithm that cross-examines reported lost belongings against newly cataloged found items in real-time:\n\n" +
                "• Category Hard Gate (30% Baseline): Items must strictly share the same category before computing textual similarity.\n" +
                "• Title Similarity (25% Weight): Normalized Levenshtein distance combined with Jaccard token index to capture typing variations.\n" +
                "• Campus Location Proximity (20% Weight): Spatial proximity based on campus block, seminar rooms, and building landmark tokens.\n" +
                "• Chronological Closeness (15% Weight): Temporal scoring giving maximum weight to items discovered within 0-3 days of loss.\n" +
                "• Attribute & Color Token Overlap (10% Weight): Description keyword overlap excluding common linguistic stop-words."
        ));
        scrollContent.add(Box.createVerticalStrut(16));

        // Feature 2: Two-Tier Verification Pipeline
        scrollContent.add(createSectionCard(
                "2. Two-Tier Confidential Verification Pipeline",
                "To eliminate false claims and prevent theft:\n\n" +
                "• Tier 1 (Confidential Evidence): Claimants submit unlisted identifying details (serial numbers, lock screen wallpapers, inner contents) hidden from public view.\n" +
                "• Tier 2 (Physical Handover Signoff): Administrators conduct in-person physical verification against official college RFID smart cards and record an immutable signoff in the system audit log."
        ));
        scrollContent.add(Box.createVerticalStrut(16));

        // Feature 3: Demo Profiles Guide
        scrollContent.add(createSectionCard(
                "3. Included Campus Demo Accounts",
                "The system initializes with realistic university demo accounts accessible via 1-click switcher or sign-in:\n\n" +
                "• Campus Admin: Prof. Arvind Swaminathan (admin@college.edu / Admin@123) - Security & Student Affairs\n" +
                "• Student: Rahul Sharma (rahul.sharma@college.edu / Student@123) - Computer Science (CS-2024-042)\n" +
                "• Student: Priya Patel (priya.patel@college.edu / Student@123) - Electronics & Comm (EC-2024-118)\n" +
                "• Faculty: Dr. Anil Verma (anil.verma@college.edu / Staff@123) - Mechanical Engineering (STF-ME-102)\n" +
                "• Student: Sneha Reddy (sneha.reddy@college.edu / Student@123) - Information Technology (IT-2024-089)"
        ));

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createSectionCard(String heading, String body) {
        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(8, 8));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hLbl = new JLabel(heading);
        hLbl.setFont(AppTheme.FONT_SUBTITLE);
        hLbl.setForeground(AppTheme.PRIMARY_NAVY);

        JTextArea bArea = new JTextArea(body);
        bArea.setFont(AppTheme.FONT_REGULAR);
        bArea.setEditable(false);
        bArea.setLineWrap(true);
        bArea.setWrapStyleWord(true);
        bArea.setBackground(AppTheme.BG_CARD);

        card.add(hLbl, BorderLayout.NORTH);
        card.add(bArea, BorderLayout.CENTER);
        return card;
    }
}
