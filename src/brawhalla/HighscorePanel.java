package brawhalla;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class HighscorePanel extends JPanel {
    ArenaEscape frame;
    JLabel playerLabel;
    JLabel scoreLabel;
    JButton back;
    boolean multiplayer = false;

    public HighscorePanel(ArenaEscape f) {
        this.frame = f;
        setLayout(null);
        setBackground(Color.BLACK);

        playerLabel = new JLabel("Player: ");
        playerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        playerLabel.setForeground(Color.CYAN);
        add(playerLabel);

        scoreLabel = new JLabel("Highscore: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 32));
        scoreLabel.setForeground(Color.WHITE);
        add(scoreLabel);

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

    public void setPlayers(String players, boolean multiplayer) {
        this.multiplayer = multiplayer;
        playerLabel.setText("Player: " + players);
    }

    public void updateLayout() {
        int w = getWidth();
        int h = getHeight();
        playerLabel.setBounds(w - 280, 20, 260, 28);
        scoreLabel.setBounds(30, 20, 400, 36);
        back.setBounds(20, h - 80, 140, 44);
    }
}
