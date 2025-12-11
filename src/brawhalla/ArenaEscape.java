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
        setTitle("Arena Escape - Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        menuPanel = new MenuPanel(this);
        instructionsPanel = new InstructionsPanel(this);
        highscorePanel = new HighscorePanel(this);
        gamePanel = new GamePanel(this);

        container.add(menuPanel, "menu");
        container.add(instructionsPanel, "instructions");
        container.add(highscorePanel, "highscore");
        container.add(gamePanel, "game");

        add(container);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setVisible(true);

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

    public void showScreen(String name) {
        card.show(container, name);

        if (name.equals("menu")) {
            menuPanel.playSound();
        } else {
            menuPanel.stopSound();
        }
    }

    public void startGame(int selectedLevel) {
        showScreen("game");

        SwingUtilities.invokeLater(() -> {
            gamePanel.startGame(
                    selectedLevel,
                    menuPanel.isMultiplayer(),
                    menuPanel.getPlayer1Name(),
                    menuPanel.getPlayer2Name()
            );
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ArenaEscape::new);
    }
}