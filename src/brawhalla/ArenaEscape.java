package brawhalla;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class    ArenaEscape extends JFrame {

    CardLayout card = new CardLayout();
    JPanel container = new JPanel(card);

    public MenuPanel menuPanel;
    public InstructionsPanel instructionsPanel;
    public HighscorePanel highscorePanel;
    public GamePanel gamePanel;

    public ArenaEscape() {

        // ===== BASIC FRAME SETTINGS =====
        setTitle("Arena Escape - Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ===== PANELS INITIALIZATION =====
        menuPanel = new MenuPanel(this);
        instructionsPanel = new InstructionsPanel(this);
        highscorePanel = new HighscorePanel(this);
        gamePanel = new GamePanel(this); // **غير هنا: أنشئه مع الباقي**

        // Add all screens to container
        container.add(menuPanel, "menu");
        container.add(instructionsPanel, "instructions");
        container.add(highscorePanel, "highscore");
        container.add(gamePanel, "game"); // **أضفه هنا**

        add(container);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setVisible(true);

        // ===== RESIZE LISTENER =====
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                menuPanel.resizeBackground();
                menuPanel.revalidate();
                menuPanel.repaint();
                instructionsPanel.updateBackPosition();
                highscorePanel.updateLayout();
            }
        });
    }

    // ===== SCREEN SWITCHER =====
    public void showScreen(String name) {
        card.show(container, name);

        // إذا رجعنا للمنيو، أوقف اللعبة
        if (name.equals("menu")) {
            gamePanel.stopGameAndReturn();
            // أعد تشغيل الصوت
            menuPanel.playSound();
        }
        // إذا خرجنا من المنيو، أوقف الصوت
        if (name.equals("game") || name.equals("instructions") || name.equals("highscore")) {
            menuPanel.stopSound();
        }

        // Update highscore display
        if (name.equals("highscore")) {
            String p1 = menuPanel.getPlayer1Name();
            String p2 = menuPanel.getPlayer2Name();
            boolean multiplayer = menuPanel.isMultiplayer();

            highscorePanel.setPlayers(
                    multiplayer ? p1 + " & " + p2 : p1,
                    multiplayer
            );
        }
    }

    // ===== START GAME =====
    public void startGame(int selectedLevel) {
        // انتقل لشاشة اللعبة أولاً
        showScreen("game");

        // ثم ابدأ اللعبة
        SwingUtilities.invokeLater(() -> {
            gamePanel.startGame(selectedLevel);
        });
    }

    // ===== ENTRY POINT =====
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ArenaEscape::new);
    }
}