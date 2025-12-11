package brawhalla;

import javax.media.opengl.GL;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;
import java.io.File;
import java.io.IOException;

public class PlatForms {
    float x, y, z = 0, w, h;
    String imagePath;
    Texture texture;
    boolean textureLoaded = false;

    public PlatForms(float x, float y, float width, float height, String imagePath) {
        this.x = x;
        this.y = y;
        this.w = width;
        this.h = height;
        this.imagePath = imagePath;
    }

    public void loadTextures(GL gl) {
        if (textureLoaded) return;

        try {
            File textureFile = new File(imagePath);
            if (textureFile.exists()) {
                texture = TextureIO.newTexture(textureFile, true);
                textureLoaded = true;
                System.out.println("✓ Loaded platform: " + imagePath);
            } else {
                System.err.println("✗ Platform image not found: " + imagePath);
                System.err.println("  Looking at: " + textureFile.getAbsolutePath());
                texture = null;
            }
        } catch (IOException e) {
            System.err.println("✗ Error loading platform: " + imagePath);
            e.printStackTrace();
            texture = null;
        }
    }

    public void render(GL gl) {
        gl.glPushMatrix();
        gl.glTranslatef(x, y, z);

        if (texture != null && textureLoaded) {
            gl.glEnable(GL.GL_TEXTURE_2D);
            texture.bind();
            gl.glColor4f(1f, 1f, 1f, 1f);

            // طريقة قلب الصورة بالمصفوفة
            gl.glMatrixMode(GL.GL_TEXTURE);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            gl.glScalef(1, -1, 1); // قلب على المحور Y
            gl.glTranslatef(0, -1, 0);
            gl.glMatrixMode(GL.GL_MODELVIEW);
        } else {
            gl.glDisable(GL.GL_TEXTURE_2D);
            // ألوان بديلة حسب نوع المنصة
            if (imagePath.contains("level1")) {
                gl.glColor3f(0.9f, 0.7f, 0.3f); // لون صحراوي
            } else if (imagePath.contains("level2")) {
                gl.glColor3f(0.6f, 0.7f, 0.8f); // لون مدينة
            } else if (imagePath.contains("Boss")) {
                gl.glColor3f(0.6f, 0.4f, 0.6f); // لون داكن
            } else {
                gl.glColor3f(0.7f, 0.7f, 0.7f); // لون رمادي افتراضي
            }
        }

        // رسم المنصة
        gl.glBegin(GL.GL_QUADS);
        if (texture != null && textureLoaded) {
            // إحداثيات طبيعية بعد القلب
            gl.glTexCoord2f(0, 0); gl.glVertex2f(-w/2, 0);
            gl.glTexCoord2f(1, 0); gl.glVertex2f(w/2, 0);
            gl.glTexCoord2f(1, 1); gl.glVertex2f(w/2, h);
            gl.glTexCoord2f(0, 1); gl.glVertex2f(-w/2, h);
        } else {
            gl.glVertex2f(-w/2, 0);
            gl.glVertex2f(w/2, 0);
            gl.glVertex2f(w/2, h);
            gl.glVertex2f(-w/2, h);
        }
        gl.glEnd();

        // استعادة مصفوفة الـ texture
        if (texture != null && textureLoaded) {
            gl.glMatrixMode(GL.GL_TEXTURE);
            gl.glPopMatrix();
            gl.glMatrixMode(GL.GL_MODELVIEW);
        }

        gl.glDisable(GL.GL_TEXTURE_2D);

        // رسم حدود للمنصة
        gl.glColor3f(0.2f, 0.2f, 0.2f);
        gl.glLineWidth(2.0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(-w/2, 0);
        gl.glVertex2f(w/2, 0);
        gl.glVertex2f(w/2, h);
        gl.glVertex2f(-w/2, h);
        gl.glEnd();

        gl.glPopMatrix();
    }
}