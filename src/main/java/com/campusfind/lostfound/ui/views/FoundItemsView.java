package com.campusfind.lostfound.ui.views;

import com.campusfind.lostfound.config.AppTheme;
import com.campusfind.lostfound.model.Category;
import com.campusfind.lostfound.model.FoundItem;
import com.campusfind.lostfound.model.FoundStatus;
import com.campusfind.lostfound.service.ItemService;
import com.campusfind.lostfound.ui.MainFrame;
import com.campusfind.lostfound.ui.dialogs.ClaimItemDialog;
import com.campusfind.lostfound.ui.dialogs.ItemDetailDialog;
import com.campusfind.lostfound.ui.dialogs.ReportItemDialog;
import com.campusfind.lostfound.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FoundItemsView extends JPanel {

    private final MainFrame mainFrame;
    private final ItemService itemService = ItemService.getInstance();

    private final JTextField searchField = UIUtils.createTextField(16);
    private final JComboBox<Object> categoryCombo = new JComboBox<>();
    private final JComboBox<Object> statusCombo = new JComboBox<>();
    private final JTextField locationField = UIUtils.createTextField(14);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Item Title", "Category", "Date Found", "Location Discovered", "Safe Pickup Desk", "Status"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private List<FoundItem> currentList = new ArrayList<>();

    public FoundItemsView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_MAIN);
        initComponents();

        itemService.addChangeListener(this::loadItems);
    }

    private void initComponents() {
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(AppTheme.BG_MAIN);
        topContainer.setBorder(new EmptyBorder(20, 24, 12, 24));

        // Header Title Row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(AppTheme.BG_MAIN);

        JLabel title = new JLabel("Cataloged Discovered Belongings");
        title.setFont(AppTheme.FONT_HEADER);
        title.setForeground(AppTheme.PRIMARY_NAVY);

        JButton reportBtn = UIUtils.createButton("+ Report Found Item", AppTheme.SECONDARY_PURPLE, Color.WHITE);
        reportBtn.addActionListener(e -> new ReportItemDialog(mainFrame, true).setVisible(true));

        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(reportBtn, BorderLayout.EAST);
        topContainer.add(titleRow);
        topContainer.add(Box.createVerticalStrut(14));

        // Filter Bar Card
        JPanel filterCard = UIUtils.createCard();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));

        categoryCombo.addItem("All Categories");
        for (Category c : Category.values()) categoryCombo.addItem(c);

        statusCombo.addItem("All Statuses");
        for (FoundStatus s : FoundStatus.values()) statusCombo.addItem(s);

        filterCard.add(new JLabel("Keyword:"));
        filterCard.add(searchField);
        filterCard.add(new JLabel("Category:"));
        filterCard.add(categoryCombo);
        filterCard.add(new JLabel("Status:"));
        filterCard.add(statusCombo);
        filterCard.add(new JLabel("Location / Desk:"));
        filterCard.add(locationField);

        JButton filterBtn = UIUtils.createButton("Filter", AppTheme.PRIMARY_NAVY, Color.WHITE);
        filterBtn.addActionListener(e -> applyFilter());

        JButton resetBtn = UIUtils.createOutlineButton("Reset", AppTheme.TEXT_MUTED);
        resetBtn.addActionListener(e -> resetFilter());

        filterCard.add(filterBtn);
        filterCard.add(resetBtn);

        topContainer.add(filterCard);
        add(topContainer, BorderLayout.NORTH);

        // Table Panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(AppTheme.BG_MAIN);
        centerPanel.setBorder(new EmptyBorder(0, 24, 14, 24));

        UIUtils.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(95);
        table.getColumnModel().getColumn(4).setPreferredWidth(160);
        table.getColumnModel().getColumn(5).setPreferredWidth(180);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    showSelectedItem();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Row
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottomRow.setBackground(AppTheme.BG_MAIN);

        JButton viewBtn = UIUtils.createOutlineButton("View Details", AppTheme.PRIMARY_NAVY);
        viewBtn.addActionListener(e -> showSelectedItem());

        JButton claimBtn = UIUtils.createButton("Claim Ownership of Selected Item", AppTheme.SECONDARY_PURPLE, Color.WHITE);
        claimBtn.addActionListener(e -> claimSelectedItem());

        bottomRow.add(viewBtn);
        bottomRow.add(claimBtn);

        centerPanel.add(bottomRow, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        loadItems();
    }

    private void applyFilter() {
        String query = searchField.getText().trim();
        Category cat = (categoryCombo.getSelectedIndex() > 0) ? (Category) categoryCombo.getSelectedItem() : null;
        FoundStatus stat = (statusCombo.getSelectedIndex() > 0) ? (FoundStatus) statusCombo.getSelectedItem() : null;
        String loc = locationField.getText().trim();

        currentList = itemService.searchFoundItems(query, cat, stat, loc);
        updateTable(currentList);
    }

    private void resetFilter() {
        searchField.setText("");
        categoryCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
        locationField.setText("");
        loadItems();
    }

    public void loadItems() {
        currentList = itemService.getAllFoundItems();
        updateTable(currentList);
    }

    private void updateTable(List<FoundItem> list) {
        tableModel.setRowCount(0);
        for (FoundItem item : list) {
            tableModel.addRow(new Object[]{
                    "#" + item.getFoundId(),
                    item.getItemName(),
                    item.getCategory().getDisplayName(),
                    UIUtils.formatDate(item.getDateFound()),
                    item.getLocationFound(),
                    item.getStorageLocation(),
                    item.getStatus().name()
            });
        }
    }

    private void showSelectedItem() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table to view details.", "Select Item", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        FoundItem selected = currentList.get(row);
        new ItemDetailDialog(mainFrame, selected).setVisible(true);
    }

    private void claimSelectedItem() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a found item to claim.", "Select Item", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        FoundItem selected = currentList.get(row);
        if (selected.getStatus() == FoundStatus.RETURNED) {
            JOptionPane.showMessageDialog(this, "This item has already been successfully returned to its owner.", "Already Returned", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new ClaimItemDialog(mainFrame, selected).setVisible(true);
    }
}
