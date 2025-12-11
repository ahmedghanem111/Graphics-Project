package brawhalla;

import javax.media.opengl.GL;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class LevelManager {
    private int currentLevel = 1;
    private ArrayList<PlatForms> platforms = new ArrayList<>();
    private ArrayList<LevelLine> levelLines = new ArrayList<>();
    public int screenW = 800, screenH = 600;

    private Texture backgroundTexture;
    private boolean flipBackground = false;

    // إحصائيات كل مستوى
    public static class LevelStats {
        public int playerMaxHealth = 100;
        public int enemyMaxHealth = 100;
        public int enemyDamage = 10;
        public int playerDamage = 20;
        public int pointsForKill = 100;
        public int pointsForWin = 500;
        public int timeLimit = 180;
        public boolean flipBackground = false;
        public boolean flipPlayers = false;

        public LevelStats(int playerHealth, int enemyHealth, int enemyDmg,
                          int playerDmg, int killPoints, int winPoints,
                          int timeLimit, boolean flipBg, boolean flipPlayers) {
            this.playerMaxHealth = playerHealth;
            this.enemyMaxHealth = enemyHealth;
            this.enemyDamage = enemyDmg;
            this.playerDamage = playerDmg;
            this.pointsForKill = killPoints;
            this.pointsForWin = winPoints;
            this.timeLimit = timeLimit;
            this.flipBackground = flipBg;
            this.flipPlayers = flipPlayers;
        }
    }

    private HashMap<Integer, LevelStats> levelStats = new HashMap<>();

    public LevelManager() {
        this.currentLevel = 1;
        initLevelStats();
    }

    public LevelManager(int startLevel) {
        this.currentLevel = startLevel;
        initLevelStats();
    }

    private void initLevelStats() {
        levelStats.put(1, new LevelStats(100, 80, 8, 15, 100, 500, 180, true, false));
        levelStats.put(2, new LevelStats(120, 120, 12, 18, 150, 750, 150, false, true));
        levelStats.put(3, new LevelStats(150, 200, 15, 25, 250, 1000, 120, true, true));
    }

    public LevelStats getCurrentLevelStats() {
        return levelStats.get(currentLevel);
    }

    public int getLevel() {
        return currentLevel;
    }

    public void setLevel(int level) {
        if (level >= 1 && level <= 3) {
            this.currentLevel = level;
            LevelStats stats = getCurrentLevelStats();
            flipBackground = stats.flipBackground;
        }
    }

    public boolean isFlipBackground() {
        return flipBackground;
    }

    public boolean isFlipPlayers() {
        return getCurrentLevelStats().flipPlayers;
    }

    public void toggleFlipBackground() {
        flipBackground = !flipBackground;
        System.out.println("Background flip toggled: " + flipBackground);
    }

    public void toggleFlipPlayers() {
        LevelStats stats = getCurrentLevelStats();
        stats.flipPlayers = !stats.flipPlayers;
        System.out.println("Players flip toggled: " + stats.flipPlayers);
    }

    public void nextLevel(GL gl) {
        currentLevel++;

        if (currentLevel > 3) currentLevel = 1;
        loadLevel(gl, currentLevel);
    }

    public void loadLevel(GL gl, int level) {
        currentLevel = level;
        LevelStats stats = getCurrentLevelStats();
        flipBackground = stats.flipBackground;

        platforms.clear();
        platforms = getPlatforms();
        levelLines.clear();
        levelLines = getLevelLines();

        // طباعة معلومات التصحيح
        printPlatformInfo();

        loadBackgroundTexture(gl);

        for (PlatForms p : platforms) {
            p.loadTextures(gl);
        }

        System.out.println("\n=== LEVEL " + currentLevel + " LOADED ===");
        System.out.println("Flip Background: " + stats.flipBackground);
        System.out.println("Flip Players: " + stats.flipPlayers);
        System.out.println("==========================\n");
    }
    public void loadBackgroundTexture(GL gl) {
        try {
            String bgPath = getBackgroundTexturePath();
            File bgFile = new File(bgPath);
            if (bgFile.exists()) {
                backgroundTexture = TextureIO.newTexture(bgFile, true);
                System.out.println("✓ Background loaded: " + bgPath +
                        " (" + backgroundTexture.getImageWidth() +
                        "x" + backgroundTexture.getImageHeight() + ")");
            } else {
                System.err.println("✗ Background not found: " + bgPath);
                backgroundTexture = null;
            }
        } catch (Exception e) {
            System.err.println("✗ Error loading background: " + e.getMessage());
            backgroundTexture = null;
        }
    }

    private String getBackgroundTexturePath() {
        switch(currentLevel) {
            case 1: return "Assets/Levels/level3/SnowBack2.png";
            case 2: return "Assets/Levels/level1/DesertBack4.png";
            case 3: return "Assets/Levels/Boss level/BossBack1.png";
            default: return "Assets/Levels/level1/SnowBack.png";
        }
    }

    public void draw(GL gl) {
        drawBackground(gl);

        // رسم خطوط المستوى
        for (LevelLine line : levelLines) {
            line.draw(gl);
        }

        // رسم المنصات
        if (platforms != null) {
            System.out.println("Drawing " + platforms.size() + " platforms...");
            for (PlatForms p : platforms) {
                if (p != null) {
                    p.render(gl);
                }
            }
        } else {
            System.err.println("ERROR: platforms list is null in draw()!");
        }
    }

    public void drawBackground(GL gl) {
        try {
            // Get screen size dynamically
            java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            screenW = screenSize.width;
            screenH = screenSize.height;

            gl.glDisable(GL.GL_DEPTH_TEST);
            gl.glMatrixMode(GL.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            gl.glOrtho(0, screenW, screenH, 0, -1, 1);
            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glPushMatrix();
            gl.glLoadIdentity();

            if (backgroundTexture != null) {
                gl.glEnable(GL.GL_TEXTURE_2D);
                backgroundTexture.bind();

                // إعدادات بسيطة وآمنة
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);

                gl.glColor3f(1, 1, 1);
            } else {
                gl.glDisable(GL.GL_TEXTURE_2D);
                gl.glColor3f(0.8f, 0.7f, 0.4f);
            }

            gl.glBegin(GL.GL_QUADS);
            gl.glTexCoord2f(0, 0); gl.glVertex2f(0, 0);
            gl.glTexCoord2f(1, 0); gl.glVertex2f(screenW, 0);
            gl.glTexCoord2f(1, 1); gl.glVertex2f(screenW, screenH);
            gl.glTexCoord2f(0, 1); gl.glVertex2f(0, screenH);
            gl.glEnd();

            gl.glPopMatrix();
            gl.glMatrixMode(GL.GL_PROJECTION);
            gl.glPopMatrix();
            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glEnable(GL.GL_DEPTH_TEST);
            gl.glDisable(GL.GL_TEXTURE_2D);

        } catch (Exception e) {
            System.err.println("Error drawing background: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public ArrayList<PlatForms> getPlatforms() {
        ArrayList<PlatForms> list = new ArrayList<>();
        switch (currentLevel) {
            case 1:
                // **غير الأبعاد هنا**: جعل width أكبر
                list.add(new PlatForms(0, -2, 30, 4, "Assets/Levels/level3/PlatForm.png")); // كان 25 -> 30
                list.add(new PlatForms(-15, 4, 20, 3, "Assets/Levels/level3/PlatForm.png")); // كان -12 -> -15, 18 -> 20
                list.add(new PlatForms(15, 4, 20, 3, "Assets/Levels/level3/PlatForm.png")); // كان 12 -> 15, 18 -> 20
                break;

            case 2:
                list.add(new PlatForms(0, -2, 35, 6, "Assets/Levels/level1/platform3.png")); // كان 30 -> 35
                list.add(new PlatForms(-12, 2, 35, 5, "Assets/Levels/level1/platform3.png")); // كان -15 -> -18, 20 -> 22
                list.add(new PlatForms(12, 2, 35, 5, "Assets/Levels/level1/platform3.png")); // كان 15 -> 18, 20 -> 22
                break;

            case 3:
                list.add(new PlatForms(0, -2, 35, 5, "Assets/Levels/Boss level/PlatForm.png")); // كان 30 -> 35
                list.add(new PlatForms(-20, 4, 18, 3, "Assets/Levels/Boss level/PlatForm.png")); // كان -18 -> -20
                list.add(new PlatForms(20, 4, 18, 3, "Assets/Levels/Boss level/PlatForm.png")); // كان 18 -> 20
                break;
        }
        return list;
    }
    public ArrayList<LevelLine> getLevelLines() {
        ArrayList<LevelLine> list = new ArrayList<>();
        switch (currentLevel) {
            case 1: list.add(new LevelLine(-50, -12, 100, 0.8f, 0.3f, 0.4f, 0.5f));
                break;
            case 2: list.add(new LevelLine(-50, -12, 100, 0.8f, 0.9f, 0.7f, 0.3f));
                break;
            case 3: list.add(new LevelLine(-50, -12, 100, 0.8f, 0.2f, 0.1f, 0.3f));
                break;
        }
        return list;
    }
    public void printPlatformInfo() {
        ArrayList<PlatForms> platforms = getPlatforms();
        System.out.println("\n=== PLATFORMS INFO for Level " + currentLevel + " ===");
        System.out.println("Number of platforms: " + platforms.size());

        for (int i = 0; i < platforms.size(); i++) {
            PlatForms p = platforms.get(i);
            System.out.println("Platform " + i + ":");
            System.out.println("  Position: (" + p.x + ", " + p.y + ")");
            System.out.println("  Size: " + p.w + " x " + p.h);
            System.out.println("  Texture: " + p.imagePath);
        }
        System.out.println("====================================\n");
    }
}