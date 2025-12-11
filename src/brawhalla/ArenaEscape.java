package brawhalla;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ArenaEscape extends JFrame {

    CardLayout card = new CardLayout();
    JPanel container = new JPanel(card);

    public MenuPanel menuPanel;
    public InstructionsPanel instructionsPanel;
    public HighscorePanel highscorePanel;
    public GamePanel gamePanel;

    public ArenaEscape() {

        // ===== BASIC FRAME SETTINGS =====
        setTitle("Arena Escape - Main Menu");
        setSize(1500, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ===== PANELS INITIALIZATION =====
        menuPanel = new MenuPanel(this);
        instructionsPanel = new InstructionsPanel(this);
        highscorePanel = new HighscorePanel(this);

        // Add all screens to container
        container.add(menuPanel, "menu");
        container.add(instructionsPanel, "instructions");
        container.add(highscorePanel, "highscore");
        gamePanel = new GamePanel(this);
        container.add(gamePanel, "game");

        add(container);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setVisible(true);

        // ===== RESIZE LISTENER (for dynamic backgrounds + UI updates) =====
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {

                // Resize background of menu
                menuPanel.resizeBackground();
                menuPanel.revalidate();
                menuPanel.repaint();

                // Adjust layouts of other panels
                instructionsPanel.updateBackPosition();
                highscorePanel.updateLayout();
            }
        });


    }

    // ===== SCREEN SWITCHER =====
    public void showScreen(String name) {
        card.show(container, name);


        // Update highscore display depending on solo/multiplayer
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

        // Run the game on EDT (safe Swing Thread)
        SwingUtilities.invokeLater(() -> {
            Game game3D = new Game(selectedLevel);
            game3D.setVisible(true);
        });
    }
    


    // ===== ENTRY POINT =====
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ArenaEscape::new);
    }
}
