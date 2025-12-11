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
    private JLabel player1HealthLabel;
    private JLabel player2HealthLabel;
    private JLabel enemyHealthLabel;
    private JLabel timeLabel;
    private JLabel modeLabel;

    public GamePanel(ArenaEscape f) {
        this.frame = f;
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        // ====== HUD PANEL ======
        JPanel hudPanel = new JPanel(new GridLayout(1, 8, 5, 0));
        hudPanel.setBackground(Color.BLACK);
        hudPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        levelLabel = new JLabel("Level: 1", SwingConstants.CENTER);
        styleHudLabel(levelLabel, Color.YELLOW);
        hudPanel.add(levelLabel);

        scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        styleHudLabel(scoreLabel, Color.CYAN);
        hudPanel.add(scoreLabel);

        player1HealthLabel = new JLabel("P1 HP: 100", SwingConstants.CENTER);
        styleHudLabel(player1HealthLabel, Color.GREEN);
        hudPanel.add(player1HealthLabel);

        player2HealthLabel = new JLabel("P2 HP: 100", SwingConstants.CENTER);
        styleHudLabel(player2HealthLabel, Color.BLUE);
        hudPanel.add(player2HealthLabel);

        enemyHealthLabel = new JLabel("E1 HP: 100", SwingConstants.CENTER);
        styleHudLabel(enemyHealthLabel, Color.RED);
        hudPanel.add(enemyHealthLabel);

        timeLabel = new JLabel("Time: 00:00", SwingConstants.CENTER);
        styleHudLabel(timeLabel, Color.ORANGE);
        hudPanel.add(timeLabel);

        modeLabel = new JLabel("Mode: Solo", SwingConstants.CENTER);
        styleHudLabel(modeLabel, Color.MAGENTA);
        hudPanel.add(modeLabel);

        JLabel fullscreenLabel = new JLabel("F11: Fullscreen", SwingConstants.CENTER);
        styleHudLabel(fullscreenLabel, Color.WHITE);
        hudPanel.add(fullscreenLabel);

        // ====== CONTROL PANEL ======
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(Color.DARK_GRAY);

        JLabel controls = new JLabel(
                "<html><center><font color='white' size='3'>" +
                        "Player 1: WASD | Player 2: Arrow Keys | F11: Fullscreen<br>" +
                        "Space: Pause | ESC: Exit to Menu" +
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

    public void startGame(int selectedLevel, boolean multiplayer, String player1Name, String player2Name) {
        stopGame();

        gameCanvas = new GLCanvas();
        renderer = new Renderer(selectedLevel, multiplayer, player1Name, player2Name) {
            public void setValue(Object aValue, boolean isSelected) {}
            public Component getComponent() { return null; }
        };

        JFrame tempFrame = new JFrame();
        renderer.setGameFrame(tempFrame);

        gameCanvas.addGLEventListener(renderer);
        gameCanvas.addKeyListener(renderer);
        gameCanvas.setFocusable(true);

        animator = new FPSAnimator(gameCanvas, 60);
        animator.start();

        add(gameCanvas, BorderLayout.CENTER);
        revalidate();
        repaint();

        gameCanvas.requestFocus();

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
            scoreLabel.setText("Score: " + renderer.getTotalScore());

            // استخدم getPlayer1Health() و getPlayer2Health()
            player1HealthLabel.setText("P1 HP: " + renderer.getPlayer1Health());

            if (renderer.isMultiplayer()) {
                player2HealthLabel.setText("P2 HP: " + renderer.getPlayer2Health());
                player2HealthLabel.setVisible(true);
                modeLabel.setText("Mode: Multi");
            } else {
                player2HealthLabel.setVisible(false);
                modeLabel.setText("Mode: Solo");
            }

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
        if (renderer != null) {
            renderer.stopGame();
        }
    }

    public void stopGameAndReturn() {
        stopGame();
        frame.showScreen("menu");
    }
}