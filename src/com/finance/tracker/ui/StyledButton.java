package com.finance.tracker.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StyledButton extends JButton {
    private final Color normalBg;
    private final Color hoverBg;
    private final Color pressedBg;
    private final Color borderColor;
    private boolean isHovered = false;
    private boolean isPressed = false;

    public StyledButton(String text, Color normalBg, Color hoverBg, Color pressedBg, Color textColor, Color borderColor) {
        super(text);
        this.normalBg = normalBg;
        this.hoverBg = hoverBg;
        this.pressedBg = pressedBg;
        this.borderColor = borderColor;

        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(textColor);
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                isPressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public static StyledButton primary(String text) {
        return new StyledButton(
                text,
                new Color(30, 41, 59),      // Dark Slate normal
                new Color(51, 65, 85),      // Slate hover
                new Color(15, 23, 42),      // Deep Slate pressed
                Color.WHITE,                // Crisp white text
                null                        // No border needed
        );
    }

    public static StyledButton secondary(String text) {
        return new StyledButton(
                text,
                Color.WHITE,                // White normal
                new Color(241, 245, 249),  // Light slate hover
                new Color(226, 232, 240),  // Slightly darker pressed
                new Color(30, 41, 59),      // Dark slate text
                new Color(203, 213, 225)   // Light gray border
        );
    }

    public static StyledButton danger(String text) {
        return new StyledButton(
                text,
                new Color(254, 242, 242),  // Soft red normal
                new Color(254, 226, 226),  // Red hover
                new Color(252, 165, 165),  // Pressed
                new Color(185, 28, 28),     // Crimson text
                new Color(252, 165, 165)   // Light red border
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background
        Color bg = isPressed ? pressedBg : (isHovered ? hoverBg : normalBg);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

        // Draw border if set
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
