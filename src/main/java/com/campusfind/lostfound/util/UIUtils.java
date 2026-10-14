package com.campusfind.lostfound.util;

import com.campusfind.lostfound.config.AppTheme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UIUtils {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public static String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DATE_FMT);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DATE_TIME_FMT);
    }

    public static JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(AppTheme.FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(bg.darker(), 1, true), new EmptyBorder(8, 16, 8, 16)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Color originalBg = bg;
        Color hoverBg = bg.brighter();

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(originalBg);
            }
        });

        return btn;
    }

    public static JButton createOutlineButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(AppTheme.FONT_BOLD);
        btn.setBackground(Color.WHITE);
        btn.setForeground(color);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(color, 1, true), new EmptyBorder(8, 16, 8, 16)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 18));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(Color.WHITE);
                }
            }
        });

        return btn;
    }

    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(AppTheme.BG_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(AppTheme.BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return card;
    }

    public static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel("  " + text + "  ");
        badge.setFont(AppTheme.FONT_BADGE);
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setBorder(new LineBorder(fg, 1, true));
        return badge;
    }

    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(AppTheme.FONT_REGULAR);
        tf.setBorder(new CompoundBorder(
                new LineBorder(AppTheme.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    public static JPasswordField createPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(AppTheme.FONT_REGULAR);
        pf.setBorder(new CompoundBorder(
                new LineBorder(AppTheme.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return pf;
    }

    public static JTextArea createTextArea(int rows, int cols) {
        JTextArea ta = new JTextArea(rows, cols);
        ta.setFont(AppTheme.FONT_REGULAR);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(new EmptyBorder(6, 8, 6, 8));
        return ta;
    }

    public static void styleTable(JTable table) {
        table.setFont(AppTheme.FONT_REGULAR);
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(AppTheme.BORDER);
        table.setSelectionBackground(new Color(0xEB, 0xF2, 0xFE));
        table.setSelectionForeground(AppTheme.TEXT_DARK);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(AppTheme.FONT_BOLD);
        header.setBackground(new Color(0xF1, 0xF5, 0xF9));
        header.setForeground(AppTheme.TEXT_DARK);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER));

        // Center / left align cell padding
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSelected, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, isSelected, hasFocus, r, c);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return comp;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }
}
