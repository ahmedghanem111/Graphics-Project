package brawhalla;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public abstract class Renderer implements GLEventListener, KeyListener, MouseListener {
    private final GLU glu = new GLU();
    private LevelManager levels;

    // Player
    private float playerX = 0;
    private float playerY = 0;
    private float playerSpeed = 0.2f;
    private Texture playerTexture;
    private boolean facingRight = true;
    private int playerHealth;
    private int playerMaxHealth;
    private int playerScore = 0;
    private int totalScore = 0;
    private boolean flipPlayer = false;

    // Enemy
    private float enemyX = 8;
    private float enemyY = 2;
    private float enemySpeed = 0.03f;
    private Texture enemyTexture;
    private int enemyHealth;
    private int enemyMaxHealth;
    private boolean enemyAlive = true;
    private int enemiesKilled = 0;
    private boolean flipEnemy = false;

    // Time System
    private long levelStartTime;
    private long currentLevelTime;
    private int levelTimeLimit;
    private Timer gameTimer;
    private int timeBonus = 0;

    // Game State
    private GL gl;
    private boolean fullscreen = false;
    private JFrame gameFrame;
    private boolean gameRunning = true;
    private boolean gamePaused = false;

    // Full screen tracking
    private int originalWidth = 1200;
    private int originalHeight = 800;
    private int originalX, originalY;
    private int fullscreenMode = 0; // 0: windowed, 1: borderless, 2: exclusive fullscreen

    public Renderer(int startLevel) {
        levels = new LevelManager(startLevel);
        initStatsForLevel();
        initTimer();
        updateFlipPlayers();
    }

    public Renderer() {
        levels = new LevelManager(1);
        initStatsForLevel();
        initTimer();
        updateFlipPlayers();
    }

    private void initStatsForLevel() {
        LevelManager.LevelStats stats = levels.getCurrentLevelStats();
        playerHealth = stats.playerMaxHealth;
        playerMaxHealth = stats.playerMaxHealth;
        enemyHealth = stats.enemyMaxHealth;
        enemyMaxHealth = stats.enemyMaxHealth;
        levelTimeLimit = stats.timeLimit;

        levelStartTime = System.currentTimeMillis();
        currentLevelTime = 0;
        timeBonus = 0;

        updateFlipPlayers();
    }

    private void updateFlipPlayers() {
        LevelManager.LevelStats stats = levels.getCurrentLevelStats();
        flipPlayer = stats.flipPlayers;
        flipEnemy = stats.flipPlayers;
        System.out.println("Players flipped: " + stats.flipPlayers);
    }

    private void initTimer() {
        gameTimer = new Timer(1000, e -> updateTime());
        gameTimer.start();
    }

    private void updateTime() {
        if (!gameRunning || gamePaused) return;

        long currentTime = System.currentTimeMillis();
        currentLevelTime = (currentTime - levelStartTime) / 1000;

        int timeRemaining = levelTimeLimit - (int)currentLevelTime;
        if (timeRemaining <= 0) {
            timeRemaining = 0;
            timeUp();
        }

        timeBonus = timeRemaining * 10;
    }

    private void timeUp() {
        if (gameRunning) {
            gameRunning = false;
            System.out.println("\n⏰ TIME'S UP! ⏰");

            if (gameFrame != null) {
                gameFrame.setTitle("TIME'S UP! Score: " + (playerScore + timeBonus));
            }

            JOptionPane.showMessageDialog(gameFrame,
                    "Time's Up!\nLevel Score: " + playerScore + "\nTotal: " + (playerScore + timeBonus),
                    "Time's Up!",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public void setGameFrame(JFrame frame) {
        this.gameFrame = frame;
        // حفظ الموقع الأصلي
        originalX = frame.getX();
        originalY = frame.getY();
        updateWindowTitle();
    }

    private void updateWindowTitle() {
        if (gameFrame != null) {
            LevelManager.LevelStats stats = levels.getCurrentLevelStats();
            String title = String.format(
                    "Brawlhalla - Level %d | HP: %d/%d | Enemy: %d/%d | Time: %s | Score: %d | Fullscreen: %s",
                    levels.getLevel(),
                    playerHealth, playerMaxHealth,
                    enemyHealth, enemyMaxHealth,
                    getFormattedTime(),
                    playerScore + timeBonus,
                    fullscreen ? "ON" : "OFF"
            );
            gameFrame.setTitle(title);
        }
    }

    public String getFormattedTime() {
        int timeRemaining = levelTimeLimit - (int)currentLevelTime;
        if (timeRemaining < 0) timeRemaining = 0;

        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void stopGame() {
        gameRunning = false;
        if (gameTimer != null) {
            gameTimer.stop();
        }
        System.out.println("Game stopped");
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        gl = drawable.getGL();
        gl.glClearColor(0f, 0f, 0f, 1f);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        levels.loadLevel(gl, levels.getLevel());
        loadTextures();

        printGameInfo();
    }

    private void loadTextures() {
        try {
            File playerFile = new File("Assets/Player/player.png");
            if (playerFile.exists()) {
                playerTexture = TextureIO.newTexture(playerFile, true);
                System.out.println("✓ Player texture loaded");
            } else {
                System.err.println("✗ Player texture not found");
                playerTexture = null;
            }
        } catch (IOException e) {
            System.err.println("✗ Error loading player texture");
            playerTexture = null;
        }

        try {
            File enemyFile = new File("Assets/Enemy/enemy.png");
            if (enemyFile.exists()) {
                enemyTexture = TextureIO.newTexture(enemyFile, true);
                System.out.println("✓ Enemy texture loaded");
            } else {
                System.err.println("✗ Enemy texture not found");
                enemyTexture = null;
            }
        } catch (IOException e) {
            System.err.println("✗ Error loading enemy texture");
            enemyTexture = null;
        }
    }

    private void printGameInfo() {
        LevelManager.LevelStats stats = levels.getCurrentLevelStats();
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║               BRAWLHALLA - LEVEL " + levels.getLevel() + "                    ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║ Background Flipped:  " + (stats.flipBackground ? "YES ⬆️⬇️" : "NO"));
        System.out.println("║ Players Flipped:     " + (stats.flipPlayers ? "YES 🔄" : "NO"));
        System.out.println("║ Time Limit:          " + levelTimeLimit + " seconds");
        System.out.println("║ Time Bonus:          10 points/second");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        if (!gameRunning) return;

        if (gl == null) gl = drawable.getGL();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        glu.gluLookAt(0, 8, 25, 0, 0, 0, 0, 1, 0);

        levels.draw(gl);

        if (enemyAlive) {
            drawEnemy(gl);
            moveEnemyTowardsPlayer();
        }

        drawPlayer(gl);
        checkCollisions();
        updateWindowTitle();

        drawHUD(gl);
        checkGameStatus();
    }

    private void drawHUD(GL gl) {
        gl.glPushMatrix();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glOrtho(0, 800, 0, 600, -1, 1);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glDisable(GL.GL_TEXTURE_2D);

        LevelManager.LevelStats stats = levels.getCurrentLevelStats();

        drawHealthBar2D(gl, 50, 550, 200, 20,
                (float)playerHealth / playerMaxHealth,
                Color.GREEN, "Player HP");

        drawHealthBar2D(gl, 50, 520, 200, 20,
                enemyAlive ? (float)enemyHealth / enemyMaxHealth : 0,
                Color.RED, "Enemy HP");

        drawTextInfo(gl, 50, 480, "Time: " + getFormattedTime(), Color.WHITE);
        drawTextInfo(gl, 50, 450, "Score: " + (playerScore + timeBonus), Color.CYAN);
        drawTextInfo(gl, 50, 420, "Level: " + levels.getLevel(), Color.YELLOW);

        // Flip status
        String flipStatus = "Flips: ";
        if (stats.flipBackground) flipStatus += "BG ⬆️⬇️ ";
        if (stats.flipPlayers) flipStatus += "Players 🔄 ";
        if (!stats.flipBackground && !stats.flipPlayers) flipStatus += "None";

        drawTextInfo(gl, 50, 390, flipStatus, Color.ORANGE);

        // Full screen status
        drawTextInfo(gl, 50, 360, "Fullscreen: " + (fullscreen ? "ON (F11)" : "OFF (F11)"),
                fullscreen ? Color.MAGENTA : Color.WHITE);

        // Controls hint
        drawTextInfo(gl, 50, 330, "F11: Fullscreen | B: Flip BG | P: Flip Players | Space: Pause", Color.LIGHT_GRAY);

        if (gamePaused) {
            drawPausedScreen(gl);
        }

        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPopMatrix();
        gl.glPopMatrix();
    }

    private void drawHealthBar2D(GL gl, float x, float y, float width, float height,
                                 float percent, Color color, String label) {
        gl.glColor3f(0.2f, 0.2f, 0.2f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + width, y);
        gl.glVertex2f(x + width, y + height);
        gl.glVertex2f(x, y + height);
        gl.glEnd();

        gl.glColor3f(0.4f, 0.4f, 0.4f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x + 2, y + 2);
        gl.glVertex2f(x + width - 2, y + 2);
        gl.glVertex2f(x + width - 2, y + height - 2);
        gl.glVertex2f(x + 2, y + height - 2);
        gl.glEnd();

        float healthWidth = (width - 4) * percent;
        if (healthWidth > 0) {
            gl.glColor3f(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f);
            gl.glBegin(GL.GL_QUADS);
            gl.glVertex2f(x + 2, y + 2);
            gl.glVertex2f(x + 2 + healthWidth, y + 2);
            gl.glVertex2f(x + 2 + healthWidth, y + height - 2);
            gl.glVertex2f(x + 2, y + height - 2);
            gl.glEnd();
        }

        gl.glColor3f(1, 1, 1);
        drawTextInfo(gl, x + 5, y + height/2 - 4, label + ": " + (int)(percent * 100) + "%", Color.WHITE);
    }

    private void drawTextInfo(GL gl, float x, float y, String text, Color color) {
        gl.glColor3f(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f);
        // Simple text rendering - in real app use GLUT or bitmap fonts
    }

    private void drawPausedScreen(GL gl) {
        // Semi-transparent overlay
        gl.glColor4f(0, 0, 0, 0.7f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, 0);
        gl.glVertex2f(800, 0);
        gl.glVertex2f(800, 600);
        gl.glVertex2f(0, 600);
        gl.glEnd();

        gl.glColor3f(1, 1, 0);
        drawTextInfo(gl, 320, 300, "GAME PAUSED", Color.YELLOW);
        drawTextInfo(gl, 280, 270, "Press SPACE to resume", Color.WHITE);
    }

    private void drawPlayer(GL gl) {
        gl.glPushMatrix();
        gl.glTranslatef(playerX, playerY, 0.1f);

        if (flipPlayer) {
            gl.glScalef(-1, -1, 1);
        } else if (!facingRight) {
            gl.glScalef(-1, 1, 1);
        }

        if (playerTexture != null) {
            gl.glEnable(GL.GL_TEXTURE_2D);
            playerTexture.bind();

            gl.glMatrixMode(GL.GL_TEXTURE);
            gl.glPushMatrix();
            gl.glLoadIdentity();

            if (flipPlayer) {
                gl.glScalef(-1, -1, 1);
                gl.glTranslatef(-1, -1, 0);
            } else {
                gl.glScalef(1, -1, 1);
                gl.glTranslatef(0, -1, 0);
            }

            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glColor3f(1, 1, 1);
        } else {
            gl.glDisable(GL.GL_TEXTURE_2D);
            gl.glColor3f(flipPlayer ? 1f : 0f, 0.5f, flipPlayer ? 0f : 1f);
        }

        float playerWidth = 1.5f;
        float playerHeight = 2.5f;

        gl.glBegin(GL.GL_QUADS);
        if (playerTexture != null) {
            gl.glTexCoord2f(0, 0); gl.glVertex2f(-playerWidth/2, -playerHeight/2);
            gl.glTexCoord2f(1, 0); gl.glVertex2f(playerWidth/2, -playerHeight/2);
            gl.glTexCoord2f(1, 1); gl.glVertex2f(playerWidth/2, playerHeight/2);
            gl.glTexCoord2f(0, 1); gl.glVertex2f(-playerWidth/2, playerHeight/2);
        } else {
            gl.glVertex2f(-playerWidth/2, -playerHeight/2);
            gl.glVertex2f(playerWidth/2, -playerHeight/2);
            gl.glVertex2f(playerWidth/2, playerHeight/2);
            gl.glVertex2f(-playerWidth/2, playerHeight/2);
        }
        gl.glEnd();

        if (playerTexture != null) {
            gl.glMatrixMode(GL.GL_TEXTURE);
            gl.glPopMatrix();
            gl.glMatrixMode(GL.GL_MODELVIEW);
        }

        gl.glDisable(GL.GL_TEXTURE_2D);
        drawHealthBar3D(gl, playerHealth, playerMaxHealth,
                0, playerHeight/2 + 0.3f, playerWidth, flipPlayer);
        gl.glPopMatrix();
    }

    private void drawEnemy(GL gl) {
        if (!enemyAlive) return;

        gl.glPushMatrix();
        gl.glTranslatef(enemyX, enemyY, 0.1f);

        if (flipEnemy) {
            gl.glScalef(-1, -1, 1);
        }

        if (enemyTexture != null) {
            gl.glEnable(GL.GL_TEXTURE_2D);
            enemyTexture.bind();

            gl.glMatrixMode(GL.GL_TEXTURE);
            gl.glPushMatrix();
            gl.glLoadIdentity();

            if (flipEnemy) {
                gl.glScalef(-1, -1, 1);
                gl.glTranslatef(-1, -1, 0);
            } else {
                gl.glScalef(1, -1, 1);
                gl.glTranslatef(0, -1, 0);
            }

            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glColor3f(1, 1, 1);
        } else {
            gl.glDisable(GL.GL_TEXTURE_2D);
            gl.glColor3f(flipEnemy ? 0.5f : 1f, 0.3f, flipEnemy ? 0.5f : 0.3f);
        }

        float enemyWidth = 1.8f;
        float enemyHeight = 2.8f;

        gl.glBegin(GL.GL_QUADS);
        if (enemyTexture != null) {
            gl.glTexCoord2f(0, 0); gl.glVertex2f(-enemyWidth/2, -enemyHeight/2);
            gl.glTexCoord2f(1, 0); gl.glVertex2f(enemyWidth/2, -enemyHeight/2);
            gl.glTexCoord2f(1, 1); gl.glVertex2f(enemyWidth/2, enemyHeight/2);
            gl.glTexCoord2f(0, 1); gl.glVertex2f(-enemyWidth/2, enemyHeight/2);
        } else {
            gl.glVertex2f(-enemyWidth/2, -enemyHeight/2);
            gl.glVertex2f(enemyWidth/2, -enemyHeight/2);
            gl.glVertex2f(enemyWidth/2, enemyHeight/2);
            gl.glVertex2f(-enemyWidth/2, enemyHeight/2);
        }
        gl.glEnd();

        if (enemyTexture != null) {
            gl.glMatrixMode(GL.GL_TEXTURE);
            gl.glPopMatrix();
            gl.glMatrixMode(GL.GL_MODELVIEW);
        }

        gl.glDisable(GL.GL_TEXTURE_2D);
        drawHealthBar3D(gl, enemyHealth, enemyMaxHealth,
                0, enemyHeight/2 + 0.3f, enemyWidth, flipEnemy);
        gl.glPopMatrix();
    }

    private void drawHealthBar3D(GL gl, int currentHealth, int maxHealth,
                                 float x, float y, float width,
                                 boolean isFlipped) {
        float healthPercent = (float) currentHealth / maxHealth;
        float barWidth = width;
        float barHeight = 0.2f;

        gl.glColor3f(0.2f, 0.2f, 0.2f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x - barWidth/2 - 0.1f, y - 0.05f);
        gl.glVertex2f(x + barWidth/2 + 0.1f, y - 0.05f);
        gl.glVertex2f(x + barWidth/2 + 0.1f, y + barHeight + 0.05f);
        gl.glVertex2f(x - barWidth/2 - 0.1f, y + barHeight + 0.05f);
        gl.glEnd();

        gl.glColor3f(0.4f, 0.4f, 0.4f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x - barWidth/2, y);
        gl.glVertex2f(x + barWidth/2, y);
        gl.glVertex2f(x + barWidth/2, y + barHeight);
        gl.glVertex2f(x - barWidth/2, y + barHeight);
        gl.glEnd();

        if (isFlipped) {
            if (healthPercent > 0.5) gl.glColor3f(0.5f, 0, 0.5f);
            else if (healthPercent > 0.25) gl.glColor3f(0.7f, 0, 0.7f);
            else gl.glColor3f(1, 0, 1);
        } else {
            if (healthPercent > 0.5) gl.glColor3f(0, 1, 0);
            else if (healthPercent > 0.25) gl.glColor3f(1, 1, 0);
            else gl.glColor3f(1, 0, 0);
        }

        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x - barWidth/2, y);
        gl.glVertex2f(x - barWidth/2 + barWidth * healthPercent, y);
        gl.glVertex2f(x - barWidth/2 + barWidth * healthPercent, y + barHeight);
        gl.glVertex2f(x - barWidth/2, y + barHeight);
        gl.glEnd();
    }

    private void moveEnemyTowardsPlayer() {
        if (!enemyAlive) return;

        float dx = playerX - enemyX;
        float dy = playerY - enemyY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 2.0f) {
            enemySpeed = 0.03f * (levels.getLevel() * 0.5f + 0.5f);
            if (Math.abs(dx) > 0.1f) enemyX += (dx > 0 ? enemySpeed : -enemySpeed);
            if (Math.abs(dy) > 0.1f) enemyY += (dy > 0 ? enemySpeed : -enemySpeed);
        }
    }

    private void checkCollisions() {
        if (!enemyAlive || !gameRunning || gamePaused) return;

        float dx = playerX - enemyX;
        float dy = playerY - enemyY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance < 2.0f) {
            LevelManager.LevelStats stats = levels.getCurrentLevelStats();

            playerHealth -= stats.enemyDamage;
            if (playerHealth < 0) playerHealth = 0;

            enemyHealth -= stats.playerDamage;
            if (enemyHealth < 0) enemyHealth = 0;

            System.out.println("⚔️  Combat! Player: -" + stats.enemyDamage +
                    " | Enemy: -" + stats.playerDamage);

            if (playerHealth <= 0) {
                playerHealth = 0;
                enemyAlive = false;
                playerScore -= 50;
                System.out.println("💀 Player defeated! -50 points");
            }

            if (enemyHealth <= 0) {
                enemyHealth = 0;
                enemyAlive = false;
                enemiesKilled++;
                playerScore += stats.pointsForKill;
                System.out.println("🎯 Enemy killed! +" + stats.pointsForKill + " points");
            }
        }
    }

    private void checkGameStatus() {
        if (!gameRunning || gamePaused) return;

        LevelManager.LevelStats stats = levels.getCurrentLevelStats();

        if (playerHealth <= 0) {
            gameRunning = false;
            totalScore += playerScore + timeBonus;
            System.out.println("\n========================================");
            System.out.println("            GAME OVER!");
            System.out.println("========================================");
            System.out.println("Final Score: " + (playerScore + timeBonus));
            System.out.println("Total Score: " + totalScore);
            System.out.println("========================================\n");

            if (gameFrame != null) {
                gameFrame.setTitle("GAME OVER - Score: " + totalScore);
            }

            JOptionPane.showMessageDialog(gameFrame,
                    "Game Over!\nFinal Score: " + (playerScore + timeBonus) + "\nTotal Score: " + totalScore,
                    "Game Over",
                    JOptionPane.INFORMATION_MESSAGE);

        } else if (!enemyAlive) {
            int finalScore = playerScore + timeBonus;
            totalScore += finalScore;

            System.out.println("\n========================================");
            System.out.println("        LEVEL " + levels.getLevel() + " COMPLETED!");
            System.out.println("========================================");
            System.out.println("Level Score: " + finalScore);
            System.out.println("Total Score: " + totalScore);
            System.out.println("========================================\n");

            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            if (levels.getLevel() < 3) {
                                nextLevel();
                            } else {
                                gameCompleted();
                            }
                        }
                    },
                    3000
            );
            gameRunning = false;
        }
    }

    private void nextLevel() {
        int nextLevel = levels.getLevel() + 1;
        levels.setLevel(nextLevel);
        levels.loadLevel(gl, nextLevel);
        initStatsForLevel();
        resetPositions();
        playerScore = 0;
        gameRunning = true;
        printGameInfo();
    }

    private void gameCompleted() {
        gameRunning = false;
        System.out.println("\n🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉");
        System.out.println("       CONGRATULATIONS!");
        System.out.println("   YOU COMPLETED ALL LEVELS!");
        System.out.println("🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉");
        System.out.println("Final Total Score: " + totalScore);
        System.out.println("🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉\n");

        if (gameFrame != null) {
            gameFrame.setTitle("VICTORY! Final Score: " + totalScore);
        }

        JOptionPane.showMessageDialog(gameFrame,
                "CONGRATULATIONS!\nYou completed all levels!\nFinal Total Score: " + totalScore,
                "VICTORY!",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL gl = drawable.getGL();
        if (height == 0) height = 1;
        levels.screenW = width;
        levels.screenH = height;

        float aspect = (float) width / height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0, aspect, 0.1, 100.0);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameRunning && e.getKeyCode() != KeyEvent.VK_ESCAPE &&
                e.getKeyCode() != KeyEvent.VK_R && e.getKeyCode() != KeyEvent.VK_F11 &&
                e.getKeyCode() != KeyEvent.VK_B && e.getKeyCode() != KeyEvent.VK_P) {
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                if (gameFrame != null) gameFrame.dispose();
                break;
            case KeyEvent.VK_LEFT: case KeyEvent.VK_A:
                playerX -= playerSpeed;
                facingRight = false;
                break;
            case KeyEvent.VK_RIGHT: case KeyEvent.VK_D:
                playerX += playerSpeed;
                facingRight = true;
                break;
            case KeyEvent.VK_UP: case KeyEvent.VK_W:
                playerY += playerSpeed;
                break;
            case KeyEvent.VK_DOWN: case KeyEvent.VK_S:
                playerY -= playerSpeed;
                break;
            case KeyEvent.VK_N:
                if (gl != null && levels.getLevel() < 3) {
                    nextLevel();
                }
                break;
            case KeyEvent.VK_R:
                resetPositions();
                System.out.println("🔄 Positions reset");
                break;
            case KeyEvent.VK_F11:
                toggleFullscreen();
                break;
            case KeyEvent.VK_F:
                toggleFullscreen();
                break;
            case KeyEvent.VK_B:
                levels.toggleFlipBackground();
                System.out.println("Background flip toggled: " + levels.isFlipBackground());
                break;
            case KeyEvent.VK_P:
                levels.toggleFlipPlayers();
                updateFlipPlayers();
                System.out.println("Players flip toggled: " + levels.isFlipPlayers());
                break;
            case KeyEvent.VK_H:
                playerHealth = playerMaxHealth;
                System.out.println("❤️  Player healed to full!");
                break;
            case KeyEvent.VK_T:
                timeBonus += 100;
                System.out.println("⏰ +100 time bonus!");
                break;
            case KeyEvent.VK_SPACE:
                gamePaused = !gamePaused;
                System.out.println(gamePaused ? "⏸️ Game Paused" : "▶️ Game Resumed");
                break;
        }
    }

    private void toggleFullscreen() {
        if (gameFrame == null) return;

        fullscreen = !fullscreen;

        if (fullscreen) {
            // حفظ الإعدادات الأصلية
            originalWidth = gameFrame.getWidth();
            originalHeight = gameFrame.getHeight();
            originalX = gameFrame.getX();
            originalY = gameFrame.getY();

            // إخفاء الإطار
            gameFrame.dispose();

            // إعداد Fullscreen
            gameFrame.setUndecorated(true);
            gameFrame.setResizable(false);

            // الحصول على شاشة العرض
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

            if (gd.isFullScreenSupported()) {
                gd.setFullScreenWindow(gameFrame);
                System.out.println("✅ Fullscreen mode activated");
            } else {
                // بديل إذا لم يكن Fullscreen مدعوماً
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                gameFrame.setBounds(0, 0, screenSize.width, screenSize.height);
                System.out.println("⚠️  Fullscreen not supported, using borderless window");
            }

            gameFrame.setVisible(true);
            gameFrame.requestFocus();

        } else {
            // الخروج من Fullscreen
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            if (gd.isFullScreenSupported()) {
                gd.setFullScreenWindow(null);
            }

            gameFrame.dispose();
            gameFrame.setUndecorated(false);
            gameFrame.setResizable(true);
            gameFrame.setSize(originalWidth, originalHeight);
            gameFrame.setLocation(originalX, originalY);
            gameFrame.setVisible(true);
            gameFrame.requestFocus();

            System.out.println("✅ Windowed mode activated");
        }

        updateWindowTitle();
    }

    private void resetPositions() {
        playerX = 0;
        playerY = 0;
        enemyX = 8;
        enemyY = 2;
        initStatsForLevel();
        enemyAlive = true;
        gameRunning = true;
        gamePaused = false;
        updateWindowTitle();
    }

    // Getters للوصول إلى البيانات من Game.java
    public int getCurrentLevel() {
        return levels.getLevel();
    }

    public int getPlayerScore() {
        return playerScore + timeBonus;
    }

    public int getPlayerHealth() {
        return playerHealth;
    }

    public int getEnemyHealth() {
        return enemyHealth;
    }

    public boolean isEnemyAlive() {
        return enemyAlive;
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    public abstract void setValue(Object aValue, boolean isSelected);

    public abstract Component getComponent();
}