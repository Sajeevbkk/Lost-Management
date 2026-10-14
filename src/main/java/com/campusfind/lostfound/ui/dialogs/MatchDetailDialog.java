package com.campusfind.lostfound.ui.dialogs;

import com.campusfind.lostfound.config.AppTheme;
import com.campusfind.lostfound.model.MatchResult;
import com.campusfind.lostfound.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MatchDetailDialog extends JDialog {

    private final MatchResult match;

    public MatchDetailDialog(Frame parent, MatchResult match) {
        super(parent, "AI Similarity Breakdown: " + match.getMatchScore() + "% Match", true);
        this.match = match;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppTheme.BG_MAIN);
        setSize(540, 580);
        setLocationRelativeTo(getParent());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(AppTheme.BG_MAIN);
        mainPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Score Header Card
        JPanel headerCard = UIUtils.createCard();
        headerCard.setLayout(new BorderLayout(10, 10));
        headerCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel scoreTitle = new JLabel("Match Confidence: " + match.getMatchScore() + "%");
        scoreTitle.setFont(AppTheme.FONT_HEADER_LARGE);
        scoreTitle.setForeground(AppTheme.PRIMARY_NAVY);

        Color tierColor = "HIGH".equalsIgnoreCase(match.getMatchTier()) ? AppTheme.SUCCESS :
                ("MEDIUM".equalsIgnoreCase(match.getMatchTier()) ? AppTheme.WARNING : AppTheme.DANGER);
        JLabel tierBadge = UIUtils.createBadge(match.getMatchTier() + " CONFIDENCE", new Color(tierColor.getRed(), tierColor.getGreen(), tierColor.getBlue(), 30), tierColor);

        headerCard.add(scoreTitle, BorderLayout.WEST);
        headerCard.add(tierBadge, BorderLayout.EAST);

        mainPanel.add(headerCard);
        mainPanel.add(Box.createVerticalStrut(14));

        // Comparison Items Card
        JPanel compCard = UIUtils.createCard();
        compCard.setLayout(new GridLayout(3, 2, 8, 6));
        compCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        compCard.add(new JLabel("Reported Lost Item:"));
        JLabel lostLbl = new JLabel(match.getLostItem().getItemName());
        lostLbl.setFont(AppTheme.FONT_BOLD);
        compCard.add(lostLbl);

        compCard.add(new JLabel("Discovered Found Item:"));
        JLabel foundLbl = new JLabel(match.getFoundItem().getItemName());
        foundLbl.setFont(AppTheme.FONT_BOLD);
        foundLbl.setForeground(AppTheme.SECONDARY_PURPLE);
        compCard.add(foundLbl);

        compCard.add(new JLabel("Storage Desk:"));
        compCard.add(new JLabel(match.getFoundItem().getStorageLocation()));

        mainPanel.add(compCard);
        mainPanel.add(Box.createVerticalStrut(14));

        // Multi-Factor Breakdown Meters
        JPanel metersCard = UIUtils.createCard();
        metersCard.setLayout(new GridLayout(4, 2, 10, 8));
        metersCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        metersCard.add(new JLabel("Title Similarity (Levenshtein + Jaccard): " + match.getNameSimilarity() + "%"));
        metersCard.add(createProgressBar(match.getNameSimilarity(), AppTheme.PRIMARY_NAVY));

        metersCard.add(new JLabel("Location Proximity: " + match.getLocationSimilarity() + "%"));
        metersCard.add(createProgressBar(match.getLocationSimilarity(), AppTheme.SECONDARY_PURPLE));

        metersCard.add(new JLabel("Date Consistency Closeness: " + match.getDateProximity() + "%"));
        metersCard.add(createProgressBar(match.getDateProximity(), AppTheme.INFO));

        metersCard.add(new JLabel("Color & Attribute Overlap: " + match.getFeatureSimilarity() + "%"));
        metersCard.add(createProgressBar(match.getFeatureSimilarity(), AppTheme.SUCCESS));

        mainPanel.add(metersCard);
        mainPanel.add(Box.createVerticalStrut(14));

        // Reason Explanation Card
        JPanel reasonCard = UIUtils.createCard();
        reasonCard.setLayout(new BorderLayout(4, 4));
        reasonCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel reasonLbl = new JLabel("Engine Diagnostic Analysis:");
        reasonLbl.setFont(AppTheme.FONT_BOLD);

        JTextArea reasonText = new JTextArea(match.getMatchReason());
        reasonText.setFont(AppTheme.FONT_REGULAR);
        reasonText.setEditable(false);
        reasonText.setLineWrap(true);
        reasonText.setWrapStyleWord(true);
        reasonText.setBackground(new Color(0xF8, 0xFA, 0xFC));
        reasonText.setBorder(new EmptyBorder(8, 8, 8, 8));

        reasonCard.add(reasonLbl, BorderLayout.NORTH);
        reasonCard.add(new JScrollPane(reasonText), BorderLayout.CENTER);

        mainPanel.add(new JScrollPane(reasonCard));
        mainPanel.add(Box.createVerticalStrut(14));

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(AppTheme.BG_MAIN);
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton claimBtn = UIUtils.createButton("Claim This Matched Item", AppTheme.SECONDARY_PURPLE, Color.WHITE);
        claimBtn.addActionListener(e -> {
            dispose();
            new ClaimItemDialog((Frame) getParent(), match.getFoundItem()).setVisible(true);
        });
        actionPanel.add(claimBtn);

        JButton closeBtn = UIUtils.createOutlineButton("Close", AppTheme.TEXT_MUTED);
        closeBtn.addActionListener(e -> dispose());
        actionPanel.add(closeBtn);

        mainPanel.add(actionPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JProgressBar createProgressBar(int value, Color color) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(value);
        bar.setStringPainted(true);
        bar.setForeground(color);
        bar.setBackground(new Color(0xEE, 0xF2, 0xF6));
        bar.setFont(AppTheme.FONT_SMALL);
        return bar;
    }
}
