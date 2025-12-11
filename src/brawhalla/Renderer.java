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

// لا حذف الـ abstract هنا
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
    private boolean gameOverShown = false;

    // أضف enum في أعلى Renderer.java
    private enum GameState {
        PLAYING,
        LEVEL_COMPLETE,
        GAME_OVER,
        VICTORY,
        PAUSED
    }

    // الجاذبية
    private float playerVelocityY = 0;
    private final float GRAVITY = -0.015f;
    private final float JUMP_FORCE = 0.3f;
    private boolean isJumping = false;
    private boolean isOnGround = false;

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

    // جاذبية العدو
    private float enemyVelocityY = 0;
    private boolean enemyOnGround = false;

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
    private int fullscreenMode = 0;

    // Cooldown for hits
    private long lastPlayerHitTime = 0;
    private long lastEnemyHitTime = 0;
    private final long HIT_COOLDOWN = 1000;
    private GameState currentState = GameState.PLAYING;


    public Renderer(int startLevel) {
        levels = new LevelManager(startLevel);
        initStatsForLevel();
        initTimer();
        updateFlipPlayers();
        initializePositions();
    }

    public Renderer() {
        levels = new LevelManager(1);
        initStatsForLevel();
        initTimer();
        updateFlipPlayers();
        initializePositions();
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

    private void initializePositions() {
        resetPositions(); // استخدم نفس الدالة
        System.out.println("Player initialized at: " + playerX + ", " + playerY);
        System.out.println("Enemy initialized at: " + enemyX + ", " + enemyY);
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
        this.originalX = frame.getX();
        this.originalY = frame.getY();
        updateWindowTitle();
    }

    // في Renderer.java أضف:
    private GamePanel gamePanel;



    // وفي updateWindowTitle() أضف:
    private void updateWindowTitle() {
        // إذا كان فيه gamePanel، حدث الـHUD
        if (gamePanel != null) {
            gamePanel.setGameInfo(
                    levels.getLevel(),
                    playerScore + timeBonus,
                    playerHealth,
                    enemyHealth,
                    getFormattedTime()
            );
        }
    }

    public String getFormattedTime() {
        int timeRemaining = levelTimeLimit - (int) currentLevelTime;
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
        initializePositions();

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
        if (currentState != GameState.PLAYING && currentState != GameState.PAUSED) {
            return; // لا ترسم إذا اللعبة مش شغالة
        }

        if (gl == null) gl = drawable.getGL();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        glu.gluLookAt(0, 8, 25, 0, 0, 0, 0, 1, 0);

        levels.draw(gl);

        if (currentState == GameState.PLAYING) {
            applyGravity();
            applyEnemyGravity();

            if (enemyAlive) {
                drawEnemy(gl);
                moveEnemyTowardsPlayer();
            }

            drawPlayer(gl);
            checkCollisions();
            checkGameStatus(); // **هون بس**
            updateWindowTitle();
        }

        drawHUD(gl);
    }

    // دالة جديدة لمؤشرات التصحيح
    private void drawDebugMarkers() {
        gl.glDisable(GL.GL_TEXTURE_2D);

        java.util.ArrayList<PlatForms> platforms = levels.getPlatforms();
        if (platforms != null) {
            for (PlatForms p : platforms) {
                if (p != null) {
                    // النقطة الحمراء في وسط المنصة
                    gl.glColor3f(1, 0, 0);
                    gl.glPointSize(15.0f);
                    gl.glBegin(GL.GL_POINTS);
                    gl.glVertex3f(p.x, p.y + p.h/2, 1.0f); // Z = 1 علشان تظهر فوق
                    gl.glEnd();

                    // الإطار الأخضر حول المنصة
                    gl.glColor3f(0, 1, 0);
                    gl.glLineWidth(3.0f);
                    gl.glBegin(GL.GL_LINE_LOOP);
                    gl.glVertex3f(p.x - p.w/2, p.y, 1.0f);
                    gl.glVertex3f(p.x + p.w/2, p.y, 1.0f);
                    gl.glVertex3f(p.x + p.w/2, p.y + p.h, 1.0f);
                    gl.glVertex3f(p.x - p.w/2, p.y + p.h, 1.0f);
                    gl.glEnd();
                }
            }
        }
        gl.glEnable(GL.GL_TEXTURE_2D);
    }

    private void applyGravity() {
        if (gamePaused || !gameRunning) return;

        playerVelocityY += GRAVITY;
        playerY += playerVelocityY;

        checkPlayerPlatformCollision();

        // **سقوط تحت الأرض**
        if (playerY < -20.0f) {
            // أوقف اللعبة أولاً
            gameRunning = false;

            // بعدين اظهر الرسالة (مرة واحدة)
            if (!gameOverShown) {
                gameOverShown = true;

                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(gameFrame,
                            "💀 GAME OVER!\nYou fell!\nScore: " + (playerScore + timeBonus),
                            "Fatal Fall",
                            JOptionPane.ERROR_MESSAGE);

                    if (gameFrame != null) {
                        gameFrame.dispose();
                    }
                });
            }
            return; // توقف هنا
        }
    }

    // أضف في أعلى الكلاس:

    private void applyEnemyGravity() {
        if (gamePaused || !enemyAlive) return;

        enemyVelocityY += GRAVITY;
        enemyY += enemyVelocityY;

        checkEnemyPlatformCollision();

        if (enemyY < -10) {
            enemyY = -10;
            enemyVelocityY = 0;
        }
    }

    private void checkPlayerPlatformCollision() {
        isOnGround = false;
        boolean wasOnGround = isOnGround;

        java.util.ArrayList<PlatForms> platforms = levels.getPlatforms();
        if (platforms == null) return;

        float playerBottom = playerY - 1.25f; // أسفل اللاعب (افترض طوله 2.5)

        for (PlatForms platform : platforms) {
            if (platform == null) continue;

            float platformLeft = platform.x - platform.w / 2;
            float platformRight = platform.x + platform.w / 2;
            float platformTop = platform.y + platform.h;

            // تحقق إذا كان اللاعب فوق المنصة مباشرة
            boolean isWithinWidth = playerX >= platformLeft - 0.5f && playerX <= platformRight + 0.5f;
            boolean isAbovePlatform = playerBottom <= platformTop;
            boolean isFalling = playerVelocityY <= 0;
            float distanceToPlatform = platformTop - playerBottom;

            if (isWithinWidth && isAbovePlatform && isFalling && distanceToPlatform >= 0 && distanceToPlatform < 0.8f) {
                // هبط على المنصة
                playerY = platformTop + 1.25f; // ضعه فوق المنصة
                playerVelocityY = 0;
                isOnGround = true;

                if (!wasOnGround) {
                    System.out.println("✓ Landed safely on platform");
                }
                break;
            }
        }

        // إذا كان يسقط ولم يهبط على منصة
        if (!isOnGround && playerVelocityY < -0.1f) {
            System.out.println("⚠️ Falling! y=" + playerY + ", velocity=" + playerVelocityY);
        }
    }
    private void checkEnemyPlatformCollision() {
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

        drawHealthBar2D(gl, 50, 550, 200, 20,
                (float) playerHealth / playerMaxHealth,
                Color.GREEN, "Player HP");

        drawHealthBar2D(gl, 50, 520, 200, 20,
                enemyAlive ? (float) enemyHealth / enemyMaxHealth : 0,
                Color.RED, "Enemy HP");

        drawTextInfo(gl, 50, 480, "Time: " + getFormattedTime(), Color.WHITE);
        drawTextInfo(gl, 50, 450, "Score: " + (playerScore + timeBonus), Color.CYAN);
        drawTextInfo(gl, 50, 420, "Level: " + levels.getLevel(), Color.YELLOW);

        String flipStatus = "Flips: ";
        LevelManager.LevelStats stats = levels.getCurrentLevelStats();
        if (stats.flipBackground) flipStatus += "BG ⬆️⬇️ ";
        if (stats.flipPlayers) flipStatus += "Players 🔄 ";
        if (!stats.flipBackground && !stats.flipPlayers) flipStatus += "None";

        drawTextInfo(gl, 50, 390, flipStatus, Color.ORANGE);
        drawTextInfo(gl, 50, 360, "Fullscreen: " + (fullscreen ? "ON (F11)" : "OFF (F11)"),
                fullscreen ? Color.MAGENTA : Color.WHITE);

        // ============ هنا ضيف المؤشرات الجديدة ============

        // 1. مؤشر السقوط
        if (!isOnGround && playerY < 0 && playerVelocityY < -0.1f) {
            drawTextInfo(gl, 350, 320, "⚠️ FALLING!", Color.RED);

            float fallSpeed = Math.abs(playerVelocityY);
            if (fallSpeed > 0.5f) {
                drawTextInfo(gl, 330, 300, "HIGH VELOCITY!", Color.ORANGE);
            }
        }

        // 2. مؤشر الصحة المنخفضة
        if (playerHealth < 30 && playerHealth > 0) {
            drawTextInfo(gl, 350, 280, "💀 LOW HEALTH!", new Color(255, 50, 50));
        }

        // 3. مؤشر إذا اللاعب تحت الأرض
        if (playerY < -5) {
            drawTextInfo(gl, 350, 260, "⬇️ IN PIT!", new Color(255, 100, 0));
        }

        // 4. مؤشر النط (Jump cooldown)
        if (!isOnGround && playerVelocityY > 0) {
            drawTextInfo(gl, 350, 240, "⬆️ JUMPING!", new Color(0, 200, 255));
        }

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
            gl.glColor3f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
            gl.glBegin(GL.GL_QUADS);
            gl.glVertex2f(x + 2, y + 2);
            gl.glVertex2f(x + 2 + healthWidth, y + 2);
            gl.glVertex2f(x + 2 + healthWidth, y + height - 2);
            gl.glVertex2f(x + 2, y + height - 2);
            gl.glEnd();
        }

        gl.glColor3f(1, 1, 1);
        drawTextInfo(gl, x + 5, y + height / 2 - 4, label + ": " + (int) (percent * 100) + "%", Color.WHITE);
    }

    private void drawTextInfo(GL gl, float x, float y, String text, Color color) {
        gl.glColor3f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
    }

    private void drawPausedScreen(GL gl) {
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

        if (!facingRight) {
            gl.glScalef(-1, 1, 1);
        }

        if (playerTexture != null) {
            gl.glEnable(GL.GL_TEXTURE_2D);
            playerTexture.bind();

            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);

            gl.glColor3f(1, 1, 1);
        } else {
            gl.glDisable(GL.GL_TEXTURE_2D);
            gl.glColor3f(0f, 0.5f, 1f);
        }

        float playerWidth = 1.5f;
        float playerHeight = 2.5f;

        gl.glBegin(GL.GL_QUADS);
        if (playerTexture != null) {
            gl.glTexCoord2f(0, 0);
            gl.glVertex2f(-playerWidth / 2, -playerHeight / 2);
            gl.glTexCoord2f(1, 0);
            gl.glVertex2f(playerWidth / 2, -playerHeight / 2);
            gl.glTexCoord2f(1, 1);
            gl.glVertex2f(playerWidth / 2, playerHeight / 2);
            gl.glTexCoord2f(0, 1);
            gl.glVertex2f(-playerWidth / 2, playerHeight / 2);
        } else {
            gl.glVertex2f(-playerWidth / 2, -playerHeight / 2);
            gl.glVertex2f(playerWidth / 2, -playerHeight / 2);
            gl.glVertex2f(playerWidth / 2, playerHeight / 2);
            gl.glVertex2f(-playerWidth / 2, playerHeight / 2);
        }
        gl.glEnd();

        if (playerTexture != null) {
            gl.glDisable(GL.GL_TEXTURE_2D);
        }

        drawHealthBar3D(gl, playerHealth, playerMaxHealth,
                0, playerHeight / 2 + 0.3f, playerWidth, false);
        gl.glPopMatrix();
    }

    private void drawEnemy(GL gl) {
        if (!enemyAlive) return;

        gl.glPushMatrix();
        gl.glTranslatef(enemyX, enemyY, 0.1f);

        if (enemyTexture != null) {
            gl.glEnable(GL.GL_TEXTURE_2D);
            enemyTexture.bind();

            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);

            gl.glColor3f(1, 1, 1);
        } else {
            gl.glDisable(GL.GL_TEXTURE_2D);
            gl.glColor3f(1f, 0.3f, 0.3f);
        }

        float enemyWidth = 1.8f;
        float enemyHeight = 2.8f;

        gl.glBegin(GL.GL_QUADS);
        if (enemyTexture != null) {
            gl.glTexCoord2f(0, 0);
            gl.glVertex2f(-enemyWidth / 2, -enemyHeight / 2);
            gl.glTexCoord2f(1, 0);
            gl.glVertex2f(enemyWidth / 2, -enemyHeight / 2);
            gl.glTexCoord2f(1, 1);
            gl.glVertex2f(enemyWidth / 2, enemyHeight / 2);
            gl.glTexCoord2f(0, 1);
            gl.glVertex2f(-enemyWidth / 2, enemyHeight / 2);
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

        drawHealthBar3D(gl, enemyHealth, enemyMaxHealth,
                0, enemyHeight / 2 + 0.3f, enemyWidth, false);
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
        gl.glVertex2f(x - barWidth / 2 - 0.1f, y - 0.05f);
        gl.glVertex2f(x + barWidth / 2 + 0.1f, y - 0.05f);
        gl.glVertex2f(x + barWidth / 2 + 0.1f, y + barHeight + 0.05f);
        gl.glVertex2f(x - barWidth / 2 - 0.1f, y + barHeight + 0.05f);
        gl.glEnd();

        gl.glColor3f(0.4f, 0.4f, 0.4f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x - barWidth / 2, y);
        gl.glVertex2f(x + barWidth / 2, y);
        gl.glVertex2f(x + barWidth / 2, y + barHeight);
        gl.glVertex2f(x - barWidth / 2, y + barHeight);
        gl.glEnd();

        if (healthPercent > 0.5) gl.glColor3f(0, 1, 0);
        else if (healthPercent > 0.25) gl.glColor3f(1, 1, 0);
        else gl.glColor3f(1, 0, 0);

        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x - barWidth / 2, y);
        gl.glVertex2f(x - barWidth / 2 + barWidth * healthPercent, y);
        gl.glVertex2f(x - barWidth / 2 + barWidth * healthPercent, y + barHeight);
        gl.glVertex2f(x - barWidth / 2, y + barHeight);
        gl.glEnd();
    }

    private void moveEnemyTowardsPlayer() {
        if (!enemyAlive || gamePaused || !gameRunning) return; // **أضف !gameRunning**

        float dx = playerX - enemyX;
        float dy = playerY - enemyY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 2.0f && enemyOnGround && gameRunning) { // **تأكد من gameRunning**
            enemySpeed = 0.03f * (levels.getLevel() * 0.5f + 0.5f);

            // حركة أفقية
            if (Math.abs(dx) > 0.1f) {
                enemyX += (dx > 0 ? enemySpeed : -enemySpeed);
            }

            // نط نحو اللاعب
            if (Math.abs(dy) > 0.5f && enemyY < playerY && Math.random() > 0.98) {
                enemyVelocityY = JUMP_FORCE * 0.8f;
            }
        }
    }

    private void checkCollisions() {
        if (!enemyAlive || !gameRunning || gamePaused) return;

        float dx = playerX - enemyX;
        float dy = playerY - enemyY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        long currentTime = System.currentTimeMillis();

        if (distance < 2.0f && gameRunning) { // **تأكد من gameRunning**
            LevelManager.LevelStats stats = levels.getCurrentLevelStats();

            // **أضف cooldown checks**
            boolean canEnemyHit = (currentTime - lastEnemyHitTime) > HIT_COOLDOWN;
            boolean canPlayerHit = (currentTime - lastPlayerHitTime) > HIT_COOLDOWN;

            if (canEnemyHit) {
                playerHealth -= stats.enemyDamage;
                if (playerHealth < 0) playerHealth = 0;
                lastEnemyHitTime = currentTime;

                System.out.println("⚔️ Enemy hit Player! -" + stats.enemyDamage + " HP");

                // دفع للخلف
                float pushBack = 0.5f;
                playerX += (dx > 0 ? pushBack : -pushBack);
                playerVelocityY = JUMP_FORCE * 0.5f;
            }

            if (canPlayerHit) {
                enemyHealth -= stats.playerDamage;
                if (enemyHealth < 0) enemyHealth = 0;
                lastPlayerHitTime = currentTime;

                System.out.println("⚔️ Player hit Enemy! -" + stats.playerDamage + " HP");

                // دفع العدو للخلف
                float pushBack = 0.5f;
                enemyX += (dx > 0 ? -pushBack : pushBack);
                enemyVelocityY = JUMP_FORCE * 0.5f;
            }

            // **تحقق من الموت**
            if (playerHealth <= 0) {
                playerHealth = 0;
                enemyAlive = false;
                playerScore -= 50;
                System.out.println("💀 Player defeated! -50 points");
            }

            if (enemyHealth <= 0 && enemyAlive) { // **أضف && enemyAlive**
                enemyHealth = 0;
                enemyAlive = false;
                enemiesKilled++;
                playerScore += stats.pointsForKill;
                System.out.println("🎯 Enemy killed! +" + stats.pointsForKill + " points");

                // **لا تستدعي checkGameStatus() هنا**
                // هيتم استدعاؤها تلقائياً في display()
            }
        }
    }

    private void checkGameStatus() {
        if (!gameRunning || gamePaused) return;

        LevelManager.LevelStats stats = levels.getCurrentLevelStats();

        // 1. تحقق من موت اللاعب
        if (playerHealth <= 0) {
            playerHealth = 0;
            gameRunning = false;

            // انتظري شوية وبعدين اعرض Game Over
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(null,
                                        "💀 GAME OVER!\n" +
                                                "Score: " + (playerScore + timeBonus),
                                        "Game Over",
                                        JOptionPane.INFORMATION_MESSAGE);

                                // العودة للمنيو
                                if (gameFrame != null) {
                                    gameFrame.dispose();
                                }
                            });
                        }
                    },
                    500 // انتظر نصف ثانية
            );
            return;
        }

        // 2. تحقق من موت العدو (Level Complete)
        if (!enemyAlive && enemyHealth <= 0) {
            int finalScore = playerScore + timeBonus + stats.pointsForWin;

            // أوقفي اللعبة مؤقتاً
            gameRunning = false;

            // انتظري شوية وبعدين اعرض Level Complete
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(() -> {
                                int choice = JOptionPane.showOptionDialog(null,
                                        "🎉 LEVEL " + levels.getLevel() + " COMPLETED!\n" +
                                                "Score: " + finalScore + "\n\n" +
                                                "Continue to next level?",
                                        "Level Complete",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE,
                                        null,
                                        new String[]{"Next Level", "Menu"},
                                        "Next Level");

                                if (choice == 0) { // Next Level
                                    nextLevel();
                                } else { // Menu
                                    if (gameFrame != null) {
                                        gameFrame.dispose();
                                    }
                                }
                            });
                        }
                    },
                    1000 // انتظر ثانية
            );
        }
    }

    // دالة جديدة لعرض Game Over
    private void showGameOverDialog() {
        if (gameFrame != null && gameFrame.isVisible()) {
            JOptionPane.showMessageDialog(gameFrame,
                    "💀 GAME OVER!\n" +
                            "Final Score: " + (playerScore + timeBonus) + "\n" +
                            "Total Score: " + totalScore,
                    "Game Over",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // دالة جديدة لعرض Level Complete
    private void showLevelCompleteDialog(int levelScore, int totalScore) {
        if (gameFrame != null && gameFrame.isVisible()) {
            int choice = JOptionPane.showOptionDialog(gameFrame,
                    "🎉 LEVEL " + levels.getLevel() + " COMPLETED!\n" +
                            "Level Score: " + levelScore + "\n" +
                            "Total Score: " + totalScore + "\n\n" +
                            "Continue to next level?",
                    "Level Complete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"Next Level", "Back to Menu"},
                    "Next Level");

            if (choice == 0) { // Next Level
                nextLevel();
            } else { // Back to Menu
                if (gameFrame != null) {
                    gameFrame.dispose();
                }
            }
        }
    }

    private void nextLevel() {
        int nextLevel = levels.getLevel() + 1;

        if (nextLevel > 3) {
            // لو ده آخر مستوى
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "🎉 CONGRATULATIONS!\n" +
                                "You completed all levels!\n" +
                                "Final Score: " + (playerScore + timeBonus),
                        "VICTORY!",
                        JOptionPane.INFORMATION_MESSAGE);
            });
            return;
        }

        // غير المستوى
        levels.setLevel(nextLevel);

        // أعد تعيين كل حاجة
        LevelManager.LevelStats stats = levels.getCurrentLevelStats();
        playerHealth = stats.playerMaxHealth;
        playerMaxHealth = stats.playerMaxHealth;
        enemyHealth = stats.enemyMaxHealth;
        enemyMaxHealth = stats.enemyMaxHealth;
        enemyAlive = true; // مهم جداً!

        // أعد تعيين المواقع
        playerX = 0;
        playerY = 5;
        enemyX = 8;
        enemyY = 5;

        // أعد تعيين السرعات
        playerVelocityY = 0;
        enemyVelocityY = 0;

        // أعد تعيين النتيجة للمستوى الجديد
        playerScore = 0;
        timeBonus = 0;
        levelStartTime = System.currentTimeMillis();

        // شغلي اللعبة تاني
        gameRunning = true;
        gamePaused = false;

        System.out.println("✅ Level " + nextLevel + " started!");
    }


    private void gameCompleted() {
        gameRunning = false;
        System.out.println("\n🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉");
        System.out.println("       CONGRATULATIONS!");
        System.out.println("   YOU COMPLETED ALL LEVELS!");
        System.out.println("🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉");
        System.out.println("Final Total Score: " + totalScore);
        System.out.println("🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉\n");

        SwingUtilities.invokeLater(() -> {
            if (gameFrame != null && gameFrame.isVisible()) {
                JOptionPane.showMessageDialog(gameFrame,
                        "🎉 CONGRATULATIONS!\n" +
                                "You completed all levels!\n" +
                                "Final Total Score: " + totalScore,
                        "VICTORY!",
                        JOptionPane.INFORMATION_MESSAGE);

                // العودة للمنيو
                if (gameFrame != null) {
                    gameFrame.dispose();
                }
            }
        });
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
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }

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
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                playerX -= playerSpeed;
                facingRight = false;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                playerX += playerSpeed;
                facingRight = true;
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                if (isOnGround && !gamePaused) {
                    playerVelocityY = JUMP_FORCE;
                    isOnGround = false;
                }
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
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
            originalWidth = gameFrame.getWidth();
            originalHeight = gameFrame.getHeight();
            originalX = gameFrame.getX();
            originalY = gameFrame.getY();

            gameFrame.dispose();
            gameFrame.setUndecorated(true);
            gameFrame.setResizable(false);

            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

            if (gd.isFullScreenSupported()) {
                gd.setFullScreenWindow(gameFrame);
                System.out.println("✅ Fullscreen mode activated");
            } else {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                gameFrame.setBounds(0, 0, screenSize.width, screenSize.height);
                System.out.println("⚠️  Fullscreen not supported, using borderless window");
            }

            gameFrame.setVisible(true);
            gameFrame.requestFocus();

        } else {
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
        java.util.ArrayList<PlatForms> platforms = levels.getPlatforms();

        if (platforms != null && !platforms.isEmpty()) {
            // اللاعب على المنصة الأولى
            PlatForms firstPlatform = platforms.get(0);
            if (firstPlatform != null) {
                playerX = firstPlatform.x;
                playerY = firstPlatform.y + firstPlatform.h + 1.25f;
                System.out.println("✓ Player respawned on platform 1");
            }

            // العدو على آخر منصة
            int lastPlatformIndex = platforms.size() - 1;
            PlatForms lastPlatform = platforms.get(lastPlatformIndex);
            if (lastPlatform != null) {
                enemyX = lastPlatform.x;
                enemyY = lastPlatform.y + lastPlatform.h + 1.25f;
                System.out.println("✓ Enemy respawned on platform " + (lastPlatformIndex + 1));
            }
        } else {
            playerX = 0;
            playerY = 5.0f;
            enemyX = 10;
            enemyY = 5.0f;
            System.out.println("⚠️ No platforms found, using default positions");
        }

        playerVelocityY = 0;
        isOnGround = true;
        enemyVelocityY = 0;
        enemyOnGround = true;
    }
    private float getPlatformTop(float xPos) {
        java.util.ArrayList<PlatForms> platforms = levels.getPlatforms();
        if (platforms == null || platforms.isEmpty()) {
            System.err.println("WARNING: No platforms found, returning default height");
            return 0.0f;
        }

        float highestTop = -10.0f;
        boolean foundPlatform = false;

        for (PlatForms platform : platforms) {
            if (platform == null) continue;

            float platformLeft = platform.x - platform.w / 2;
            float platformRight = platform.x + platform.w / 2;
            float platformTop = platform.y + platform.h; // **هنا الخطأ السابق**

            // تحقق إذا كان xPos فوق المنصة
            if (xPos >= platformLeft && xPos <= platformRight) {
                if (platformTop > highestTop) {
                    highestTop = platformTop;
                    foundPlatform = true;
                    System.out.println("DEBUG: Found platform for x=" + xPos +
                            ", top=" + platformTop +
                            ", platform at (" + platform.x + "," + platform.y +
                            ") size " + platform.w + "x" + platform.h);
                }
            }
        }

        if (!foundPlatform) {
            System.err.println("WARNING: No platform under position x=" + xPos);
        }

        return highestTop;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    // إضافة الـgetters المطلوبة
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
    // في Renderer.java أضف:

    public void setGamePanel(GamePanel panel) {
        this.gamePanel = panel;
    }

    // وفي updateWindowTitle() أضف:

}