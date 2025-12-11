package brawhalla;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel {
    ArenaEscape frame;
    JLabel scoreLabel, healthLabel;
    JProgressBar scoreBar, healthBar;
    JButton back;

    public GamePanel(ArenaEscape f) {
        this.frame = f;
        setLayout(null);
        setBackground(Color.DARK_GRAY);

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        scoreLabel.setForeground(Color.CYAN);
        add(scoreLabel);

        scoreBar = new JProgressBar(0, 100);
        scoreBar.setValue(0);
        scoreBar.setStringPainted(false);
        add(scoreBar);

        healthLabel = new JLabel("Health: 100");
        healthLabel.setFont(new Font("Arial", Font.BOLD, 20));
        healthLabel.setForeground(Color.RED);
        add(healthLabel);

        healthBar = new JProgressBar(0, 100);
        healthBar.setValue(100);
        healthBar.setStringPainted(false);
        add(healthBar);

        back = new JButton("Back");
        styleBackButton(back);
        back.addActionListener(e -> frame.showScreen("menu"));
        add(back);

        updateLayout();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateLayout();
            }
        });
    }

    private void styleBackButton(JButton b) {
        b.setFont(new Font("Arial", Font.BOLD, 24));
        b.setBackground(new Color(20,20,30));
        b.setForeground(Color.CYAN);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(Color.CYAN,2));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void updateLayout() {
        int w = getWidth();
        int h = getHeight();
        scoreLabel.setBounds(20, 20, 180, 24);
        scoreBar.setBounds(20, 48, 140, 12);
        healthLabel.setBounds(20, 74, 180, 24);
        healthBar.setBounds(20, 102, 140, 12);
        back.setBounds(20, h - 80, 140, 44);
    }
}
