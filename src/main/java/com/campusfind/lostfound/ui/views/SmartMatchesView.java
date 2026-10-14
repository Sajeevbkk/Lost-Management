package com.campusfind.lostfound.ui.views;

import com.campusfind.lostfound.config.AppTheme;
import com.campusfind.lostfound.model.MatchResult;
import com.campusfind.lostfound.model.User;
import com.campusfind.lostfound.service.AuthService;
import com.campusfind.lostfound.service.ItemService;
import com.campusfind.lostfound.service.MatchingService;
import com.campusfind.lostfound.ui.MainFrame;
import com.campusfind.lostfound.ui.dialogs.ClaimItemDialog;
import com.campusfind.lostfound.ui.dialogs.MatchDetailDialog;
import com.campusfind.lostfound.util.UIUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class SmartMatchesView extends JPanel {

    private final MainFrame mainFrame;
    private final MatchingService matchingService = MatchingService.getInstance();
    private final AuthService authService = AuthService.getInstance();
    private final ItemService itemService = ItemService.getInstance();

    private final JPanel cardsContainer = new JPanel();
    private final JRadioButton rbAllMatches = new JRadioButton("All Campus Potential Matches", true);
    private final JRadioButton rbMyMatches = new JRadioButton("Matches for My Lost Reports", false);

    public SmartMatchesView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_MAIN);
        initComponents();

        itemService.addChangeListener(this::loadMatches);
        authService.addAuthListener(this::loadMatches);
    }

    private void initComponents() {
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(AppTheme.BG_MAIN);
        topContainer.setBorder(new EmptyBorder(20, 24, 12, 24));

        // Header Title Row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(AppTheme.BG_MAIN);

        JLabel title = new JLabel("Smart AI Multi-Factor Matching Engine");
        title.setFont(AppTheme.FONT_HEADER);
        title.setForeground(AppTheme.PRIMARY_NAVY);

        JButton scanBtn = UIUtils.createButton("Rescan Matching Algorithms", AppTheme.PRIMARY_NAVY, Color.WHITE);
        scanBtn.addActionListener(e -> {
            loadMatches();
            JOptionPane.showMessageDialog(this, "Match scan completed! Analyzed all reported items.", "Scan Finished", JOptionPane.INFORMATION_MESSAGE);
        });

        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(scanBtn, BorderLayout.EAST);
        topContainer.add(titleRow);
        topContainer.add(Box.createVerticalStrut(10));

        // Description & Filter bar
        JPanel filterCard = UIUtils.createCard();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 4));

        ButtonGroup group = new ButtonGroup();
        group.add(rbAllMatches);
        group.add(rbMyMatches);

        rbAllMatches.setFont(AppTheme.FONT_BOLD);
        rbAllMatches.setBackground(AppTheme.BG_CARD);
        rbMyMatches.setFont(AppTheme.FONT_BOLD);
        rbMyMatches.setBackground(AppTheme.BG_CARD);

        rbAllMatches.addActionListener(e -> loadMatches());
        rbMyMatches.addActionListener(e -> loadMatches());

        filterCard.add(new JLabel("Scope:"));
        filterCard.add(rbAllMatches);
        filterCard.add(rbMyMatches);

        topContainer.add(filterCard);
        add(topContainer, BorderLayout.NORTH);

        // Cards Scroll Area
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setBackground(AppTheme.BG_MAIN);
        cardsContainer.setBorder(new EmptyBorder(0, 24, 24, 24));

        JScrollPane scrollPane = new JScrollPane(cardsContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);

        loadMatches();
    }

    public void loadMatches() {
        cardsContainer.removeAll();

        List<MatchResult> list;
        if (rbMyMatches.isSelected()) {
            User current = authService.getCurrentUser();
            list = matchingService.getMatchesForUser(current);
        } else {
            list = matchingService.getAllActiveMatches();
        }

        if (list.isEmpty()) {
            JPanel emptyCard = UIUtils.createCard();
            emptyCard.setLayout(new FlowLayout(FlowLayout.CENTER));
            JLabel emptyLbl = new JLabel("No potential matches with >= 60% confidence detected at this time.");
            emptyLbl.setFont(AppTheme.FONT_REGULAR);
            emptyLbl.setForeground(AppTheme.TEXT_MUTED);
            emptyCard.add(emptyLbl);
            cardsContainer.add(emptyCard);
        } else {
            for (MatchResult match : list) {
                cardsContainer.add(createMatchCard(match));
                cardsContainer.add(Box.createVerticalStrut(12));
            }
        }

        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private JPanel createMatchCard(MatchResult match) {
        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(14, 10));

        // Top Bar: Score & Category
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(AppTheme.BG_CARD);

        Color tierColor = "HIGH".equalsIgnoreCase(match.getMatchTier()) ? AppTheme.SUCCESS : AppTheme.WARNING;
        JLabel scoreBadge = UIUtils.createBadge(match.getMatchScore() + "% SIMILARITY (" + match.getMatchTier() + " CONFIDENCE)",
                new Color(tierColor.getRed(), tierColor.getGreen(), tierColor.getBlue(), 25), tierColor);

        JLabel catLbl = new JLabel(match.getLostItem().getCategory().getDisplayName());
        catLbl.setFont(AppTheme.FONT_BOLD);
        catLbl.setForeground(AppTheme.TEXT_MUTED);

        topBar.add(scoreBadge, BorderLayout.WEST);
        topBar.add(catLbl, BorderLayout.EAST);
        card.add(topBar, BorderLayout.NORTH);

        // Center: Side-by-side comparison
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        centerGrid.setBackground(AppTheme.BG_CARD);

        // Lost Item Box
        JPanel lostBox = new JPanel();
        lostBox.setLayout(new BoxLayout(lostBox, BoxLayout.Y_AXIS));
        lostBox.setBackground(new Color(0xFE, 0xF2, 0xF2));
        lostBox.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xFE, 0xCA, 0xCA), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel lHeader = new JLabel("REPORTED MISSING");
        lHeader.setFont(AppTheme.FONT_BADGE);
        lHeader.setForeground(AppTheme.DANGER);

        JLabel lTitle = new JLabel(match.getLostItem().getItemName());
        lTitle.setFont(AppTheme.FONT_BOLD);

        JLabel lLoc = new JLabel("Location: " + match.getLostItem().getLocationLost());
        lLoc.setFont(AppTheme.FONT_SMALL);

        JLabel lDate = new JLabel("Date Lost: " + UIUtils.formatDate(match.getLostItem().getDateLost()));
        lDate.setFont(AppTheme.FONT_SMALL);

        lostBox.add(lHeader);
        lostBox.add(Box.createVerticalStrut(4));
        lostBox.add(lTitle);
        lostBox.add(Box.createVerticalStrut(2));
        lostBox.add(lLoc);
        lostBox.add(lDate);

        // Found Item Box
        JPanel foundBox = new JPanel();
        foundBox.setLayout(new BoxLayout(foundBox, BoxLayout.Y_AXIS));
        foundBox.setBackground(new Color(0xEC, 0xFD, 0xF5));
        foundBox.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xA7, 0xF3, 0xD0), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel fHeader = new JLabel("DISCOVERED ITEM");
        fHeader.setFont(AppTheme.FONT_BADGE);
        fHeader.setForeground(AppTheme.SUCCESS);

        JLabel fTitle = new JLabel(match.getFoundItem().getItemName());
        fTitle.setFont(AppTheme.FONT_BOLD);

        JLabel fLoc = new JLabel("Found At: " + match.getFoundItem().getLocationFound());
        fLoc.setFont(AppTheme.FONT_SMALL);

        JLabel fDesk = new JLabel("Stored At: " + match.getFoundItem().getStorageLocation());
        fDesk.setFont(AppTheme.FONT_SMALL);

        foundBox.add(fHeader);
        foundBox.add(Box.createVerticalStrut(4));
        foundBox.add(fTitle);
        foundBox.add(Box.createVerticalStrut(2));
        foundBox.add(fLoc);
        foundBox.add(fDesk);

        centerGrid.add(lostBox);
        centerGrid.add(foundBox);

        // Bottom: Reason & Actions
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        bottomPanel.setBackground(AppTheme.BG_CARD);

        JLabel reasonLbl = new JLabel("<html><b>Reasoning:</b> " + match.getMatchReason() + "</html>");
        reasonLbl.setFont(AppTheme.FONT_REGULAR);
        reasonLbl.setForeground(AppTheme.TEXT_MUTED);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(AppTheme.BG_CARD);

        JButton diagBtn = UIUtils.createOutlineButton("Diagnostic Breakdown", AppTheme.PRIMARY_NAVY);
        diagBtn.addActionListener(e -> new MatchDetailDialog(mainFrame, match).setVisible(true));

        JButton claimBtn = UIUtils.createButton("Claim This Item", AppTheme.SECONDARY_PURPLE, Color.WHITE);
        claimBtn.addActionListener(e -> new ClaimItemDialog(mainFrame, match.getFoundItem()).setVisible(true));

        btnRow.add(diagBtn);
        btnRow.add(claimBtn);

        bottomPanel.add(reasonLbl, BorderLayout.CENTER);
        bottomPanel.add(btnRow, BorderLayout.SOUTH);

        card.add(centerGrid, BorderLayout.CENTER);
        card.add(bottomPanel, BorderLayout.SOUTH);

        return card;
    }
}
