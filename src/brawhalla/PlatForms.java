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
            System.out.println("Loading platform texture from: " + textureFile.getAbsolutePath());

            if (textureFile.exists()) {
                System.out.println("✓ File exists!");
                texture = TextureIO.newTexture(textureFile, true);
                textureLoaded = true;
                System.out.println("✓ Loaded platform texture: " + imagePath);
                System.out.println("  Texture size: " + texture.getImageWidth() + "x" + texture.getImageHeight());
            } else {
                System.err.println("✗ Platform image not found: " + imagePath);
                System.err.println("  Looking at: " + textureFile.getAbsolutePath());
                texture = null;
                textureLoaded = false;
            }
        } catch (IOException e) {
            System.err.println("✗ Error loading platform texture: " + imagePath);
            e.printStackTrace();
            texture = null;
            textureLoaded = false;
        } catch (Exception e) {
            System.err.println("✗ Unexpected error: " + e.getMessage());
            texture = null;
            textureLoaded = false;
        }
    }

    public void render(GL gl) {
        gl.glPushMatrix();
        gl.glTranslatef(x, y, z);

        // محاولة تحميل الـtexture إذا لم يتم تحميله
        if (!textureLoaded) {
            loadTextures(gl);
        }

        // إذا الـtexture متحمل، استخدمه
        if (texture != null && textureLoaded) {
            gl.glEnable(GL.GL_TEXTURE_2D);
            texture.bind();

            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);

            gl.glColor3f(1.0f, 1.0f, 1.0f);

            // **التعديل المهم هنا**: استخدم texture coordinates صحيحة
            // 0,0 = أسفل يسار، 1,1 = أعلى يمين
            gl.glBegin(GL.GL_QUADS);

            // أسفل يسار
            gl.glTexCoord2f(0.0f, 0.0f);
            gl.glVertex3f(-w/2, 0, 0.5f);

            // أسفل يمين
            gl.glTexCoord2f(1.0f, 0.0f);
            gl.glVertex3f(w/2, 0, 0.5f);

            // أعلى يمين
            gl.glTexCoord2f(1.0f, 1.0f);
            gl.glVertex3f(w/2, h, 0.5f);

            // أعلى يسار
            gl.glTexCoord2f(0.0f, 1.0f);
            gl.glVertex3f(-w/2, h, 0.5f);

            gl.glEnd();

            gl.glDisable(GL.GL_TEXTURE_2D);

        } else {
            // إذا الـtexture مش متحمل، استخدم ألوان بديلة
            gl.glDisable(GL.GL_TEXTURE_2D);

            // ألوان حسب المستوى
            if (imagePath.contains("level3") || imagePath.contains("Snow")) {
                gl.glColor3f(0.3f, 0.5f, 0.9f); // أزرق داكن
            } else if (imagePath.contains("level1") || imagePath.contains("Desert")) {
                gl.glColor3f(0.9f, 0.7f, 0.2f); // ذهبي
            } else if (imagePath.contains("Boss")) {
                gl.glColor3f(0.7f, 0.2f, 0.7f); // بنفسجي داكن
            } else {
                gl.glColor3f(0.5f, 0.5f, 0.5f); // رمادي داكن
            }

            // رسم المنصة بدون texture
            gl.glBegin(GL.GL_QUADS);
            gl.glVertex3f(-w/2, 0, 0.5f);
            gl.glVertex3f(w/2, 0, 0.5f);
            gl.glVertex3f(w/2, h, 0.5f);
            gl.glVertex3f(-w/2, h, 0.5f);
            gl.glEnd();
        }

        gl.glPopMatrix();
    }
}