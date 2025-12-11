package brawhalla;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.*;

public class MenuPanel extends JPanel {
    ArenaEscape frame;
    JTextField player1Field;
    JTextField player2Field;
    JLabel playerDisplay;
    JComboBox<String> modeCombo;
    Image bgImage;
    String[] levels = {"Level 1", "Level 2", "Boss Level"};
    int currentLevelIndex = 0;
    JButton levelButton;
    private int selectedLevel = 1;
    private Clip bgClip;
    private boolean soundEnabled = true;
    private JButton soundButton;

    public MenuPanel(ArenaEscape f) {
        this.frame = f;
        setLayout(new BorderLayout());

        // تحميل الخلفية
        try {
            File imageFile = new File("Assets/brawhalla-promo.jpg");
            if (imageFile.exists()) {
                bgImage = ImageIO.read(imageFile);
            }
        } catch (Exception ex) {
            bgImage = null;
        }

        // ====== OVERLAY PANEL ======
        JPanel overlay = new JPanel();
        overlay.setOpaque(false);
        overlay.setLayout(new BoxLayout(overlay, BoxLayout.Y_AXIS));
        add(overlay, BorderLayout.CENTER);

        overlay.add(Box.createVerticalStrut(80));

        // TITLE
        JLabel title = new JLabel("ARENA ESCAPE");
        title.setFont(new Font("Arial", Font.BOLD, 55));
        title.setForeground(new Color(220, 40, 40));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        overlay.add(title);

        overlay.add(Box.createVerticalStrut(40));

        // PLAYER 1
        JLabel nameLabel = new JLabel("Player 1 Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 30));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        overlay.add(nameLabel);

        overlay.add(Box.createVerticalStrut(10));

        player1Field = new JTextField(15);
        player1Field.setMaximumSize(new Dimension(450, 40));
        player1Field.setFont(new Font("Arial", Font.PLAIN, 28));
        player1Field.setText("Player1");
        overlay.add(player1Field);

        overlay.add(Box.createVerticalStrut(20));

        // PLAYER 2
        JLabel name2Label = new JLabel("Player 2 Name:");
        name2Label.setFont(new Font("Arial", Font.BOLD, 30));
        name2Label.setForeground(Color.WHITE);
        name2Label.setAlignmentX(Component.CENTER_ALIGNMENT);
        name2Label.setVisible(false);
        overlay.add(name2Label);

        player2Field = new JTextField(15);
        player2Field.setMaximumSize(new Dimension(450, 40));
        player2Field.setFont(new Font("Arial", Font.PLAIN, 28));
        player2Field.setText("Player2");
        player2Field.setVisible(false);
        overlay.add(player2Field);

        overlay.add(Box.createVerticalStrut(25));

        // MODE SELECTION
        JPanel modePanel = new JPanel();
        modePanel.setOpaque(false);
        JLabel modeLabel = new JLabel("Mode: ");
        modeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        modeLabel.setForeground(Color.WHITE);
        modeCombo = new JComboBox<>(new String[]{"Solo", "Multiplayer"});
        modeCombo.setPreferredSize(new Dimension(220, 40));
        modeCombo.setFont(new Font("Arial", Font.BOLD, 22));
        modeCombo.addActionListener(e -> {
            boolean multi = "Multiplayer".equals(modeCombo.getSelectedItem());
            player2Field.setVisible(multi);
            name2Label.setVisible(multi);
            revalidate();
            repaint();
        });
        modePanel.add(modeLabel);
        modePanel.add(modeCombo);
        modePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        overlay.add(modePanel);

        overlay.add(Box.createVerticalStrut(30));

        // LEVEL SELECTOR
        JPanel levelPanel = new JPanel();
        levelPanel.setOpaque(false);
        levelPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton leftArrow = createArrowButton("«");
        leftArrow.addActionListener(e -> changeLevel(-1));

        levelButton = new JButton(levels[currentLevelIndex]);
        levelButton.setFont(new Font("Arial", Font.BOLD, 36));
        levelButton.setForeground(Color.WHITE);
        levelButton.setBackground(new Color(60, 0, 0));
        levelButton.setFocusPainted(false);
        levelButton.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
        levelButton.setPreferredSize(new Dimension(350, 60));
        levelPanel.add(leftArrow);
        levelPanel.add(Box.createHorizontalStrut(20));
        levelPanel.add(levelButton);
        levelPanel.add(Box.createHorizontalStrut(20));

        JButton rightArrow = createArrowButton("»");
        rightArrow.addActionListener(e -> changeLevel(1));
        levelPanel.add(rightArrow);
        overlay.add(levelPanel);

        overlay.add(Box.createVerticalStrut(35));

        // MAIN BUTTONS
        JButton startButton = createMainButton("Start Game", new Color(100, 0, 0));
        startButton.addActionListener(e -> {
            if (!validatePlayers()) return;
            updatePlayerDisplay();
            frame.startGame(selectedLevel); // **هنا بيتغير: بدل ما ينشئ Game جديد**
        });
        overlay.add(startButton);

        overlay.add(Box.createVerticalStrut(20));

        JButton instructionsButton = createMainButton("Instructions", new Color(100, 0, 0));
        instructionsButton.addActionListener(e -> frame.showScreen("instructions"));
        overlay.add(instructionsButton);

        overlay.add(Box.createVerticalStrut(20));

        JButton highscoreButton = createMainButton("Highscore", new Color(100, 0, 0));
        highscoreButton.addActionListener(e -> frame.showScreen("highscore"));
        overlay.add(highscoreButton);

        overlay.add(Box.createVerticalStrut(20));

        JButton exitButton = createMainButton("Exit", new Color(100, 0, 0));
        exitButton.addActionListener(e -> System.exit(0));
        overlay.add(exitButton);
        // ===== SOUND CONTROL =====
        overlay.add(Box.createVerticalStrut(15));

        soundButton = new JButton("Sound: ON");
        styleSoundButton(soundButton);
        soundButton.addActionListener(e -> toggleSound());
        soundButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        overlay.add(soundButton);

        overlay.add(Box.createVerticalStrut(10));
        // PLAYER DISPLAY
        playerDisplay = new JLabel("");
        playerDisplay.setFont(new Font("Arial", Font.BOLD, 20));
        playerDisplay.setForeground(Color.WHITE);
        playerDisplay.setHorizontalAlignment(SwingConstants.RIGHT);
        add(playerDisplay, BorderLayout.NORTH);

        SwingUtilities.invokeLater(() -> {
            playSound(); // هنا حط الباث بتاعك
        });
        selectedLevel = currentLevelIndex + 1;
    }

    // ===== UTILITY METHODS =====
    private JButton createArrowButton(String t) {
        JButton b = new JButton(t);
        b.setFont(new Font("Arial", Font.BOLD, 35));
        b.setBackground(new Color(40, 0, 0));
        b.setForeground(Color.RED);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton createMainButton(String text, Color bg) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 32));
        button.setPreferredSize(new Dimension(400, 60));
        button.setMaximumSize(new Dimension(400, 60));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bg.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bg);
            }
        });
        return button;
    }

    private void changeLevel(int dir) {
        currentLevelIndex += dir;
        if (currentLevelIndex < 0) currentLevelIndex = levels.length - 1;
        if (currentLevelIndex >= levels.length) currentLevelIndex = 0;
        selectedLevel = currentLevelIndex + 1;
        levelButton.setText(levels[currentLevelIndex]);
    }

    private boolean validatePlayers() {
        if (player1Field.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Player 1 name!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (isMultiplayer() && player2Field.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Player 2 name!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            Graphics2D g2 = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(0, 0, new Color(0, 0, 0, 180),
                    0, getHeight(), new Color(0, 0, 0, 100));
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void updatePlayerDisplay() {
        String p1 = getPlayer1Name();
        if (isMultiplayer()) {
            playerDisplay.setText("Players: " + p1 + " & " + getPlayer2Name());
        } else {
            playerDisplay.setText("Player: " + p1);
        }
    }

    public boolean isMultiplayer() {
        return "Multiplayer".equals(modeCombo.getSelectedItem());
    }

    public String getPlayer1Name() {
        String text = player1Field.getText().trim();
        return text.isEmpty() ? "Player1" : text;
    }

    public String getPlayer2Name() {
        String text = player2Field.getText().trim();
        return text.isEmpty() ? "Player2" : text;
    }

    public int getSelectedLevel() {
        return selectedLevel;
    }

    public void resizeBackground() {
        if (bgImage != null) {
            bgImage = bgImage.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
        }
    }
    // ===== SOUND METHODS =====
    public void playSound() {
        String soundFileName="Assets/Levels/sounds/background.wav";
        try {
            if (bgClip != null && bgClip.isRunning()) {
                bgClip.stop();
            }
            File soundFile = new File(soundFileName);          // حدد ملف الصوت
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile); // اقرأ الصوت
            bgClip = AudioSystem.getClip();                 // اعمل Clip لتشغيله
            bgClip.open(audioIn); // افتح الصوت في الـ Clip
            bgClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgClip.start();                                      // شغله
        } catch (Exception e) {
            e.printStackTrace();                               // لو فيه خطأ، اطبعه
        }
    }
    private void toggleSound() {
        soundEnabled = !soundEnabled;

        if (bgClip != null) {
            if (soundEnabled) {
                bgClip.start();
                bgClip.loop(Clip.LOOP_CONTINUOUSLY);
                soundButton.setText("Sound: ON");
                System.out.println("Sound ON");
            } else {
                bgClip.stop();
                soundButton.setText("Sound: OFF");
                System.out.println("Sound OFF");
            }
        }
    }
    public void stopSound() {
        if (bgClip != null && bgClip.isRunning()) {
            bgClip.stop();
            System.out.println("Menu music stopped");
        }
    }
    private void styleSoundButton(JButton b) {
        b.setFont(new Font("Arial", Font.BOLD, 18));
        b.setPreferredSize(new Dimension(200, 40));
        b.setMaximumSize(new Dimension(200, 40));
        b.setBackground(new Color(0, 60, 0));
        b.setForeground(Color.GREEN);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(0, 80, 0));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(new Color(0, 60, 0));
            }
        });
    }
}