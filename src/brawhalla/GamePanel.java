package brawhalla;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.media.opengl.GLCanvas;
import com.sun.opengl.util.FPSAnimator;

public class GamePanel extends JPanel {
    ArenaEscape frame;
    private GLCanvas gameCanvas;
    private FPSAnimator animator;
    private Renderer renderer;
    private JButton backButton;

    // HUD Components
    private JLabel levelLabel;
    private JLabel scoreLabel;
    private JLabel playerHealthLabel;
    private JLabel enemyHealthLabel;
    private JLabel timeLabel;

    public GamePanel(ArenaEscape f) {
        this.frame = f;
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        // ====== HUD PANEL (Top) ======
        JPanel hudPanel = new JPanel(new GridLayout(1, 6, 5, 0));
        hudPanel.setBackground(Color.BLACK);
        hudPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        levelLabel = new JLabel("Level: 1", SwingConstants.CENTER);
        styleHudLabel(levelLabel, Color.YELLOW);
        hudPanel.add(levelLabel);

        scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        styleHudLabel(scoreLabel, Color.CYAN);
        hudPanel.add(scoreLabel);

        playerHealthLabel = new JLabel("P1 HP: 100", SwingConstants.CENTER);
        styleHudLabel(playerHealthLabel, Color.GREEN);
        hudPanel.add(playerHealthLabel);

        enemyHealthLabel = new JLabel("E1 HP: 100", SwingConstants.CENTER);
        styleHudLabel(enemyHealthLabel, Color.RED);
        hudPanel.add(enemyHealthLabel);

        timeLabel = new JLabel("Time: 00:00", SwingConstants.CENTER);
        styleHudLabel(timeLabel, Color.ORANGE);
        hudPanel.add(timeLabel);

        JLabel fullscreenLabel = new JLabel("F11: Fullscreen", SwingConstants.CENTER);
        styleHudLabel(fullscreenLabel, Color.MAGENTA);
        hudPanel.add(fullscreenLabel);

        // ====== CONTROL PANEL (Bottom) ======
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(Color.DARK_GRAY);

        JLabel controls = new JLabel(
                "<html><center><font color='white' size='3'>" +
                        "WASD/Arrows: Move | F11: Fullscreen | B: Flip BG | P: Flip Players<br>" +
                        "Space: Pause | R: Reset | N: Next Level | ESC: Exit" +
                        "</font></center></html>",
                SwingConstants.CENTER
        );

        backButton = new JButton("Back to Menu");
        styleBackButton(backButton);
        backButton.addActionListener(e -> stopGameAndReturn());

        controlPanel.add(controls, BorderLayout.CENTER);
        controlPanel.add(backButton, BorderLayout.EAST);

        // ====== MAIN PANEL ======
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(hudPanel, BorderLayout.NORTH);
        topPanel.add(controlPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
    }

    public void startGame(int selectedLevel) {
        stopGame();

        gameCanvas = new GLCanvas();
        renderer = new Renderer(selectedLevel) {
            public void setValue(Object aValue, boolean isSelected) {}
            public Component getComponent() { return null; }
        };

        // تمرير الـRenderer للـGamePanel لتحديث الـHUD
        renderer.setGamePanel(this);

        gameCanvas.addGLEventListener(renderer);
        gameCanvas.addKeyListener(renderer);
        gameCanvas.setFocusable(true);

        animator = new FPSAnimator(gameCanvas, 60);
        animator.start();

        add(gameCanvas, BorderLayout.CENTER);
        revalidate();
        repaint();

        gameCanvas.requestFocus();

        // Timer لتحديث الـHUD
        Timer hudTimer = new Timer(100, e -> updateHUD());
        hudTimer.start();

        gameCanvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    stopGameAndReturn();
                }
            }
        });
    }

    private void updateHUD() {
        if (renderer != null) {
            levelLabel.setText("Level: " + renderer.getCurrentLevel());
            scoreLabel.setText("Score: " + renderer.getPlayerScore());
            playerHealthLabel.setText("P1 HP: " + renderer.getPlayerHealth());
            enemyHealthLabel.setText(renderer.isEnemyAlive() ?
                    "E1 HP: " + renderer.getEnemyHealth() :
                    "E1: DEAD");
            timeLabel.setText("Time: " + renderer.getFormattedTime());
        }
    }

    private void styleHudLabel(JLabel label, Color color) {
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(color);
        label.setOpaque(true);
        label.setBackground(Color.BLACK);
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
    }

    private void styleBackButton(JButton b) {
        b.setFont(new Font("Arial", Font.BOLD, 16));
        b.setBackground(new Color(20,20,30));
        b.setForeground(Color.CYAN);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(Color.CYAN, 2));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void stopGame() {
        if (animator != null) {
            animator.stop();
            animator = null;
        }
        if (gameCanvas != null) {
            remove(gameCanvas);
            gameCanvas = null;
        }
    }

    public void stopGameAndReturn() {
        stopGame();
        frame.showScreen("menu");
    }

    // Getters للـRenderer
    public void setGameInfo(int level, int score, int playerHealth, int enemyHealth, String time) {
        SwingUtilities.invokeLater(() -> {
            levelLabel.setText("Level: " + level);
            scoreLabel.setText("Score: " + score);
            playerHealthLabel.setText("P1 HP: " + playerHealth);
            enemyHealthLabel.setText("E1 HP: " + enemyHealth);
            timeLabel.setText("Time: " + time);
        });
    }
}