package brawhalla;

import com.sun.opengl.util.FPSAnimator;

import javax.media.opengl.GLCanvas;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Game extends JFrame {
    private int startLevel;
    private Renderer renderer;
    private JLabel scoreLabel;
    private JLabel playerHealthLabel;
    private JLabel enemyHealthLabel;
    private JLabel levelLabel;
    private JLabel timeLabel;
    private JLabel fullscreenLabel;

    public Game(int startLevel) {
        this.startLevel = startLevel;
        setupWindow();
    }

    private void setupWindow() {
        setTitle("Brawlhalla - Level " + startLevel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Panel أعلى للمعلومات مع 6 أعمدة
        JPanel infoPanel = new JPanel(new GridLayout(1, 6, 5, 0));
        infoPanel.setBackground(Color.BLACK);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Level
        levelLabel = new JLabel("Level: " + startLevel, SwingConstants.CENTER);
        styleInfoLabel(levelLabel, Color.YELLOW);
        infoPanel.add(levelLabel);

        // Score
        scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        styleInfoLabel(scoreLabel, Color.CYAN);
        infoPanel.add(scoreLabel);

        // Player Health
        playerHealthLabel = new JLabel("P1 HP: 100", SwingConstants.CENTER);
        styleInfoLabel(playerHealthLabel, Color.GREEN);
        infoPanel.add(playerHealthLabel);

        // Enemy Health
        enemyHealthLabel = new JLabel("E1 HP: 100", SwingConstants.CENTER);
        styleInfoLabel(enemyHealthLabel, Color.RED);
        infoPanel.add(enemyHealthLabel);

        // Time
        timeLabel = new JLabel("Time: 00:00", SwingConstants.CENTER);
        styleInfoLabel(timeLabel, Color.ORANGE);
        infoPanel.add(timeLabel);

        // Fullscreen Status
        fullscreenLabel = new JLabel("F11: Fullscreen", SwingConstants.CENTER);
        styleInfoLabel(fullscreenLabel, Color.MAGENTA);
        infoPanel.add(fullscreenLabel);

        // Panel للتحكم
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(Color.DARK_GRAY);

        JLabel controls = new JLabel(
                "<html><center><font color='white' size='3'>" +
                        "WASD/Arrows: Move | F11: Fullscreen | B: Flip BG | P: Flip Players<br>" +
                        "Space: Pause | R: Reset | N: Next Level | ESC: Exit" +
                        "</font></center></html>",
                SwingConstants.CENTER
        );

        JButton backBtn = new JButton("Back to Menu");
        backBtn.setFont(new Font("Arial", Font.BOLD, 12));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBackground(new Color(50, 50, 70));
        backBtn.setFocusable(false);
        backBtn.addActionListener(e -> dispose());

        controlPanel.add(controls, BorderLayout.CENTER);
        controlPanel.add(backBtn, BorderLayout.EAST);

        // GLCanvas
        // GLCanvas
        GLCanvas canvas = new GLCanvas();
        renderer = new Renderer(startLevel) {
            public void setValue(Object aValue, boolean isSelected) {
                // يمكن تجاهل هذا
            }

            public Component getComponent() {
                return null;
            }
        };
        canvas.addGLEventListener(renderer);
        canvas.addKeyListener(renderer);
        canvas.addMouseListener(renderer);
        canvas.setFocusable(true);

        // Panel أعلى
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(infoPanel, BorderLayout.NORTH);
        topPanel.add(controlPanel, BorderLayout.SOUTH);

        // إضافة الكومبوننتس
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);

        // Timer لتحديث المعلومات
        Timer updateTimer = new Timer(100, e -> updateGameInfo());
        updateTimer.start();

        setSize(1200, 800);
        setLocationRelativeTo(null);
        setVisible(true);
        canvas.requestFocus();

        FPSAnimator animator = new FPSAnimator(canvas, 60);
        animator.start();

        // إعداد Fullscreen listener
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F11) {
                    toggleFullscreen();
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                animator.stop();
                updateTimer.stop();
                renderer.stopGame();
            }
        });

        // تمرير الإطار إلى الـ Renderer
        renderer.setGameFrame(this);
    }

    private void styleInfoLabel(JLabel label, Color color) {
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(color);
        label.setOpaque(true);
        label.setBackground(Color.BLACK);
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
    }

    private void updateGameInfo() {
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

    private void toggleFullscreen() {
        // هذا الآن يتم التعامل معه في الـ Renderer
        // نطلب من المستخدم استخدام F11 من داخل اللعبة
        Toolkit.getDefaultToolkit().beep();
    }
}