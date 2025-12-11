package brawhalla;

import javax.media.opengl.GL;

public class LevelLine {

    float x, y;      // position
    float width;     // طول الخط
    float height;    // سمك الخط
    float r, g, b;   // اللون

    public LevelLine(float x, float y, float width, float height,
                     float r, float g, float b) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public void draw(GL gl) {
        gl.glDisable(GL.GL_TEXTURE_2D);

        gl.glColor3f(r, g, b);

        gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(x, y, 0);
        gl.glVertex3f(x + width, y, 0);
        gl.glVertex3f(x + width, y + height, 0);
        gl.glVertex3f(x, y + height, 0);
        gl.glEnd();

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glColor3f(1,1,1); // reset
    }
}