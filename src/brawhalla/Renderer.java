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

    // Player 1
    private float player1X = -3;
    private float player1Y = 0;
    private float playerSpeed = 0.2f;
    private Texture player1Texture;
    private boolean player1FacingRight = true;
    private int player1Health;
    private int player1MaxHealth;
    private int player1Score = 0;
    private String player1Name = "Player1";

    // Player 2
    private float player2X = 3;
    private float player2Y = 0;
    private Texture player2Texture;
    private boolean player2FacingRight = true;
    private int player2Health;
    private int player2MaxHealth;
    private int player2Score = 0;
    private String player2Name = "Player2";

    private boolean multiplayer = false;

    // Enemy
    private float enemyX = 8;
    private float enemyY = 2;
    private float enemySpeed = 0.03f;
    private Texture enemyTexture;
    private int enemyHealth;
    private int enemyMaxHealth;
    private boolean enemyAlive = true;

    // الجاذبية
    private float player1VelocityY = 0;
    private float player2VelocityY = 0;
    private final float GRAVITY = -0.015f;
    private final float JUMP_FORCE = 0.3f;
    private boolean player1OnGround = false;
    private boolean player2OnGround = false;
    private float enemyVelocityY = 0;
    private boolean enemyOnGround = false;

    // الوقت
    private long levelStartTime;
    private long currentLevelTime;
    private int levelTimeLimit;
    private Timer gameTimer;
    private int timeBonus = 0;

    // Game State
    private GL gl;
    private boolean gameRunning = true;
    private boolean gamePaused = false;
    private JFrame gameFrame;

    // التحكم
    private boolean player1Left = false, player1Right = false, player1Up = false;
    private boolean player2Left = false, player2Right = false, player2Up = false;

    // Constructor
    public Renderer(int startLevel, boolean multiplayer, String player1Name, String player2Name) {
        this.multiplayer = multiplayer;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        levels = new LevelManager(startLevel);
        initStatsForLevel();
        initTimer();
        initializePositions();
    }

    public Renderer(int startLevel) {
        this(startLevel, false, "Player1", "Player2");
    }

    public Renderer() {
        this(1, false, "Player1", "Player2");
    }

    private void initStatsForLevel() {
        LevelManager.LevelStats stats = levels.getCurrentLevelStats();
        player1Health = stats.playerMaxHealth;
        player1MaxHealth = stats.playerMaxHealth;
        player2Health = stats.playerMaxHealth;
        player2MaxHealth = stats.playerMaxHealth;
        enemyHealth = stats.enemyMaxHealth;
        enemyMaxHealth = stats.enemyMaxHealth;
        levelTimeLimit = stats.timeLimit;

        levelStartTime = System.currentTimeMillis();
        currentLevelTime = 0;
        timeBonus = 0;
    }

    private void initTimer() {
        gameTimer = new Timer(1000, e -> updateTime());
        gameTimer.start();
    }

    private void updateTime() {
        if (!gameRunning || gamePaused) return;

        long currentTime = System.currentTimeMillis();
        currentLevelTime = (currentTime - levelStartTime) / 1000;

        int timeRemaining = levelTimeLimit - (int) currentLevelTime;
        if (timeRemaining <= 0) {
            timeRemaining = 0;
            timeUp();
        }

        timeBonus = timeRemaining * 10;
    }

    private void timeUp() {
        if (gameRunning) {
            gameRunning = false;
            JOptionPane.showMessageDialog(gameFrame, "Time's Up!\nFinal Score: " + getTotalScore(), "Game Over", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void setGameFrame(JFrame frame) {
        this.gameFrame = frame;
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
        initializePositions();

        System.out.println("Game initialized. Mode: " + (multiplayer ? "Multiplayer" : "Single Player"));
        System.out.println("Player 1: " + player1Name + ", Player 2: " + player2Name);
    }

    private void loadTextures() {
        // Player 1 texture
        try {
            File player1File = new File("Assets/player1.png");
            if (player1File.exists()) {
                player1Texture = TextureIO.newTexture(player1File, true);
                System.out.println("✓ Player 1 texture loaded from: " + player1File.getAbsolutePath());
            } else {
                System.err.println("✗ Player 1 texture not found at: Assets/player1.png");
                player1Texture = null;
            }
        } catch (Exception e) {
            System.err.println("Error loading player1 texture: " + e.getMessage());
            player1Texture = null;
        }

        // Player 2 texture
        try {
            File player2File = new File("Assets/player2.png");
            if (player2File.exists()) {
                player2Texture = TextureIO.newTexture(player2File, true);
                System.out.println("✓ Player 2 texture loaded from: " + player2File.getAbsolutePath());
            } else {
                System.err.println("✗ Player 2 texture not found at: Assets/player2.png");
                player2Texture = null;
            }
        } catch (Exception e) {
            System.err.println("Error loading player2 texture: " + e.getMessage());
            player2Texture = null;
        }

        // Enemy texture (use player2 if no enemy texture)
        try {
            File enemyFile = new File("Assets/player2.png"); // استخدام player2 للعدو
            if (enemyFile.exists()) {
                enemyTexture = TextureIO.newTexture(enemyFile, true);
                System.out.println("✓ Enemy texture loaded (using player2)");
            } else {
                enemyTexture = null;
            }
        } catch (Exception e) {
            System.err.println("Error loading enemy texture: " + e.getMessage());
            enemyTexture = null;
        }
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        if (gl == null) gl = drawable.getGL();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        glu.gluLookAt(0, 8, 25, 0, 0, 0, 0, 1, 0);

        levels.draw(gl);

        if (gameRunning && !gamePaused) {
            applyGravity();
            applyEnemyGravity();
            handlePlayerMovement();
            levels.drawPlatforms(gl);

            if (enemyAlive) {
                drawEnemy(gl);
                moveEnemyTowardsPlayer();
            }
            drawPlayer1(gl);
            if (multiplayer) {
                drawPlayer2(gl);
            }
            checkCollisions();
            checkGameStatus();
        }
    }

    private void handlePlayerMovement() {
        if (player1Left) {
            player1X -= playerSpeed;
            player1FacingRight = false;
        }
        if (player1Right) {
            player1X += playerSpeed;
            player1FacingRight = true;
        }
        if (player1Up && player1OnGround) {
            player1VelocityY = JUMP_FORCE;
            player1OnGround = false;
        }

        if (multiplayer) {
            if (player2Left) {
                player2X -= playerSpeed;
                player2FacingRight = false;
            }
            if (player2Right) {
                player2X += playerSpeed;
                player2FacingRight = true;
            }
            if (player2Up && player2OnGround) {
                player2VelocityY = JUMP_FORCE;
                player2OnGround = false;
            }
        }
    }

    private void applyGravity() {
        player1VelocityY += GRAVITY;
        player1Y += player1VelocityY;
        checkPlayer1PlatformCollision();

        if (multiplayer) {
            player2VelocityY += GRAVITY;
            player2Y += player2VelocityY;
            checkPlayer2PlatformCollision();
        }
    }

    private void checkPlayer1PlatformCollision() {
        java.util.ArrayList<PlatForms> platforms = levels.getPlatforms();
        if (platforms == null) return;

        player1OnGround = false;
        float playerBottom = player1Y - 1.25f;

        for (PlatForms platform : platforms) {
            if (platform == null) continue;

            float platformLeft = platform.x - platform.w / 2;
            float platformRight = platform.x + platform.w / 2;
            float platformTop = platform.y + platform.h;

            boolean isWithinWidth = player1X >= platformLeft - 0.5f && player1X <= platformRight + 0.5f;
            boolean isAbovePlatform = playerBottom <= platformTop;
            boolean isFalling = player1VelocityY <= 0;
            float distanceToPlatform = platformTop - playerBottom;

            if (isWithinWidth && isAbovePlatform && isFalling && distanceToPlatform >= 0 && distanceToPlatform < 0.8f) {
                player1Y = platformTop + 1.25f;
                player1VelocityY = 0;
                player1OnGround = true;
                break;
            }
        }
    }

    private void checkPlayer2PlatformCollision() {
        java.util.ArrayList<PlatForms> platforms = levels.getPlatforms();
        if (platforms == null) return;

        player2OnGround = false;
        float playerBottom = player2Y - 1.25f;

        for (PlatForms platform : platforms) {
            if (platform == null) continue;

            float platformLeft = platform.x - platform.w / 2;
            float platformRight = platform.x + platform.w / 2;
            float platformTop = platform.y + platform.h;

            boolean isWithinWidth = player2X >= platformLeft - 0.5f && player2X <= platformRight + 0.5f;
            boolean isAbovePlatform = playerBottom <= platformTop;
            boolean isFalling = player2VelocityY <= 0;
            float distanceToPlatform = platformTop - playerBottom;

            if (isWithinWidth && isAbovePlatform && isFalling && distanceToPlatform >= 0 && distanceToPlatform < 0.8f) {
                player2Y = platformTop + 1.25f;
                player2VelocityY = 0;
                player2OnGround = true;
                break;
            }
        }
    }

    private void applyEnemyGravity() {
        if (!enemyAlive) return;

        enemyVelocityY += GRAVITY;
        enemyY += enemyVelocityY;

        enemyOnGround = false;
        for (PlatForms platform : levels.getPlatforms()) {
            float platformLeft = platform.x - platform.w / 2;
            float platformRight = platform.x + platform.w / 2;
            float platformTop = platform.y + platform.h;

            if (enemyX > platformLeft - 0.5f && enemyX < platformRight + 0.5f) {
                if (enemyY <= platformTop && enemyY > platformTop - 0.5f && enemyVelocityY <= 0) {
                    enemyY = platformTop;
                    enemyVelocityY = 0;
                    enemyOnGround = true;
                    break;
                }
            }
        }
    }

    private void drawPlayer1(GL gl) {
        drawPlayer(gl, player1X, player1Y, player1FacingRight, player1Texture, Color.GREEN);
    }

    private void drawPlayer2(GL gl) {
        drawPlayer(gl, player2X, player2Y, player2FacingRight, player2Texture, Color.BLUE);
    }

    private void drawPlayer(GL gl, float x, float y, boolean facingRight, Texture texture, Color color) {
        gl.glPushMatrix();
        gl.glTranslatef(x, y, 0.1f);

        if (!facingRight) {
            gl.glScalef(-1, 1, 1);
        }

        if (texture != null) {
            gl.glEnable(GL.GL_TEXTURE_2D);
            texture.bind();
            gl.glColor3f(1, 1, 1);
        } else {
            gl.glDisable(GL.GL_TEXTURE_2D);
            gl.glColor3f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
        }

        float playerWidth = 1.5f;
        float playerHeight = 2.5f;

        gl.glBegin(GL.GL_QUADS);
        if (texture != null) {
            gl.glTexCoord2f(0, 0); gl.glVertex2f(-playerWidth / 2, -playerHeight / 2);
            gl.glTexCoord2f(1, 0); gl.glVertex2f(playerWidth / 2, -playerHeight / 2);
            gl.glTexCoord2f(1, 1); gl.glVertex2f(playerWidth / 2, playerHeight / 2);
            gl.glTexCoord2f(0, 1); gl.glVertex2f(-playerWidth / 2, playerHeight / 2);
        } else {
            gl.glVertex2f(-playerWidth / 2, -playerHeight / 2);
            gl.glVertex2f(playerWidth / 2, -playerHeight / 2);
            gl.glVertex2f(playerWidth / 2, playerHeight / 2);
            gl.glVertex2f(-playerWidth / 2, playerHeight / 2);
        }
        gl.glEnd();

        if (texture != null) {
            gl.glDisable(GL.GL_TEXTURE_2D);
        }
        gl.glPopMatrix();
    }

    private void drawEnemy(GL gl) {
        if (!enemyAlive) return;

        gl.glPushMatrix();
        gl.glTranslatef(enemyX, enemyY, 0.1f);

        if (enemyTexture != null) {
            gl.glEnable(GL.GL_TEXTURE_2D);
            enemyTexture.bind();
            gl.glColor3f(1, 1, 1);
        } else {
            gl.glDisable(GL.GL_TEXTURE_2D);
            gl.glColor3f(1f, 0.3f, 0.3f);
        }

        float enemyWidth = 1.8f;
        float enemyHeight = 2.8f;

        gl.glBegin(GL.GL_QUADS);
        if (enemyTexture != null) {
            gl.glTexCoord2f(0, 0); gl.glVertex2f(-enemyWidth / 2, -enemyHeight / 2);
            gl.glTexCoord2f(1, 0); gl.glVertex2f(enemyWidth / 2, -enemyHeight / 2);
            gl.glTexCoord2f(1, 1); gl.glVertex2f(enemyWidth / 2, enemyHeight / 2);
            gl.glTexCoord2f(0, 1); gl.glVertex2f(-enemyWidth / 2, enemyHeight / 2);
        } else {
            gl.glVertex2f(-enemyWidth / 2, -enemyHeight / 2);
            gl.glVertex2f(enemyWidth / 2, -enemyHeight / 2);
            gl.glVertex2f(enemyWidth / 2, enemyHeight / 2);
            gl.glVertex2f(-enemyWidth / 2, enemyHeight / 2);
        }
        gl.glEnd();

        if (enemyTexture != null) {
            gl.glDisable(GL.GL_TEXTURE_2D);
        }
        gl.glPopMatrix();
    }

    private void moveEnemyTowardsPlayer() {
        if (!enemyAlive || !gameRunning) return;

        float targetX = player1X;
        float targetY = player1Y;

        if (multiplayer && Math.random() > 0.5) {
            targetX = player2X;
            targetY = player2Y;
        }

        float dx = targetX - enemyX;
        float dy = targetY - enemyY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 2.0f && enemyOnGround) {
            enemySpeed = 0.03f * (levels.getLevel() * 0.5f + 0.5f);

            if (Math.abs(dx) > 0.1f) {
                enemyX += (dx > 0 ? enemySpeed : -enemySpeed);
            }

            if (Math.abs(dy) > 0.5f && enemyY < targetY && Math.random() > 0.98) {
                enemyVelocityY = JUMP_FORCE * 0.8f;
            }
        }
    }

    private void checkCollisions() {
        if (!enemyAlive || !gameRunning) return;

        LevelManager.LevelStats stats = levels.getCurrentLevelStats();

        // Check player1 collision with enemy
        float dist1 = (float) Math.sqrt(Math.pow(player1X - enemyX, 2) + Math.pow(player1Y - enemyY, 2));
        if (dist1 < 2.0f) {
            player1Health -= stats.enemyDamage;
            enemyHealth -= stats.playerDamage;
            player1Score += 10;

            if (player1Health < 0) player1Health = 0;
            if (enemyHealth < 0) enemyHealth = 0;

            // Push back
            player1X += (player1X < enemyX ? -0.5f : 0.5f);
            player1VelocityY = JUMP_FORCE * 0.3f;
        }

        // Check player2 collision with enemy (if multiplayer)
        if (multiplayer) {
            float dist2 = (float) Math.sqrt(Math.pow(player2X - enemyX, 2) + Math.pow(player2Y - enemyY, 2));
            if (dist2 < 2.0f) {
                player2Health -= stats.enemyDamage;
                enemyHealth -= stats.playerDamage;
                player2Score += 10;

                if (player2Health < 0) player2Health = 0;
                if (enemyHealth < 0) enemyHealth = 0;

                // Push back
                player2X += (player2X < enemyX ? -0.5f : 0.5f);
                player2VelocityY = JUMP_FORCE * 0.3f;
            }
        }
    }

    private void checkGameStatus() {
        if (!gameRunning) return;

        // Check if players are dead
        if (player1Health <= 0 && (!multiplayer || player2Health <= 0)) {
            gameRunning = false;
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(gameFrame,
                        "Game Over!\n" +
                                player1Name + " Score: " + player1Score + "\n" +
                                (multiplayer ? player2Name + " Score: " + player2Score + "\n" : "") +
                                "Total Score: " + getTotalScore(),
                        "Game Over",
                        JOptionPane.ERROR_MESSAGE);
            });
            return;
        }

        // Check if enemy is dead
        if (enemyHealth <= 0 && enemyAlive) {
            enemyAlive = false;
            LevelManager.LevelStats stats = levels.getCurrentLevelStats();
            player1Score += stats.pointsForKill;
            if (multiplayer) player2Score += stats.pointsForKill;

            gameRunning = false;

            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(gameFrame,
                        "Level Complete!\n" +
                                player1Name + " Score: " + player1Score + "\n" +
                                (multiplayer ? player2Name + " Score: " + player2Score + "\n" : "") +
                                "Total Score: " + getTotalScore() + "\n" +
                                "Time Bonus: " + timeBonus,
                        "Congratulations",
                        JOptionPane.INFORMATION_MESSAGE);
            });
        }
    }

    private void initializePositions() {
        player1X = -3;
        player1Y = 5;
        player2X = 3;
        player2Y = 5;
        enemyX = 8;
        enemyY = 5;
        player1VelocityY = 0;
        player2VelocityY = 0;
        enemyVelocityY = 0;
        player1OnGround = true;
        player2OnGround = true;
        enemyOnGround = true;
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
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                if (gameFrame != null) gameFrame.dispose();
                break;
            // Player 1 Controls (WASD)
            case KeyEvent.VK_A:
                player1Left = true;
                break;
            case KeyEvent.VK_D:
                player1Right = true;
                break;
            case KeyEvent.VK_W:
                player1Up = true;
                break;
            case KeyEvent.VK_S:
                player1Y -= playerSpeed;
                break;
            // Player 2 Controls (Arrow Keys)
            case KeyEvent.VK_LEFT:
                if (multiplayer) player2Left = true;
                break;
            case KeyEvent.VK_RIGHT:
                if (multiplayer) player2Right = true;
                break;
            case KeyEvent.VK_UP:
                if (multiplayer) player2Up = true;
                break;
            case KeyEvent.VK_DOWN:
                if (multiplayer) player2Y -= playerSpeed;
                break;
            // Game Controls
            case KeyEvent.VK_SPACE:
                gamePaused = !gamePaused;
                break;
            case KeyEvent.VK_F11:
                if (gameFrame != null) {
                    if ((gameFrame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
                        gameFrame.setExtendedState(JFrame.NORMAL);
                    } else {
                        gameFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    }
                }
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:
                player1Left = false;
                break;
            case KeyEvent.VK_D:
                player1Right = false;
                break;
            case KeyEvent.VK_W:
                player1Up = false;
                break;
            case KeyEvent.VK_LEFT:
                player2Left = false;
                break;
            case KeyEvent.VK_RIGHT:
                player2Right = false;
                break;
            case KeyEvent.VK_UP:
                player2Up = false;
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}

    // ====== GETTERS ======
    public int getCurrentLevel() {
        return levels.getLevel();
    }

    public int getPlayer1Score() {
        return player1Score;
    }

    public int getPlayer2Score() {
        return player2Score;
    }

    public int getTotalScore() {
        return player1Score + player2Score + timeBonus;
    }

    public int getPlayer1Health() {
        return player1Health;
    }

    public int getPlayer2Health() {
        return player2Health;
    }

    public int getEnemyHealth() {
        return enemyHealth;
    }

    public boolean isEnemyAlive() {
        return enemyAlive;
    }

    public String getFormattedTime() {
        int timeRemaining = levelTimeLimit - (int) currentLevelTime;
        if (timeRemaining < 0) timeRemaining = 0;
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public boolean isMultiplayer() {
        return multiplayer;
    }

    public void stopGame() {
        gameRunning = false;
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }
    public int getPlayerScore() {
        return getTotalScore();  // أو return player1Score; حسب ما تريد
    }


    public int getPlayerHealth() {
        return getPlayer1Health(); // أو أي قيمة افتراضية
    }
}

