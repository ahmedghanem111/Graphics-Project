package texture;
import com.sun.opengl.util.BufferUtil;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Image loading class that converts BufferedImages into a data
 * structure that can be easily passed to OpenGL.
 * @author Pepijn Van Eeckhoudt
 */

public class TextureReader {
  public static Texture readTexture(String filename) throws IOException {
    return readTexture(filename, false);
  }

  public static Texture readTexture(String filename, boolean storeAlphaChannel) throws IOException {
    BufferedImage bufferedImage;
    if (filename.endsWith(".bmp")) {
      bufferedImage = texture.BitmapLoader.loadBitmap(filename);
    } else {
      bufferedImage = readImage(filename);
    }
    return readPixels(bufferedImage, storeAlphaChannel);
  }

  private static BufferedImage readImage(String resourceName) throws IOException {
    return ImageIO.read(texture.ResourceRetriever.getResourceAsStream(resourceName));
  }

    public static int loadTexture(GL gl, String filename) throws IOException {


        Texture tex = readTexture(filename, true); // قراءة الصورة

        int[] textureIDs = new int[1];
        gl.glGenTextures(1, textureIDs, 0);      // إنشاء ID

        int id = textureIDs[0];

        gl.glBindTexture(GL.GL_TEXTURE_2D, id);

        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);

        gl.glTexImage2D(
                GL.GL_TEXTURE_2D,
                0,
                GL.GL_RGBA,
                tex.getWidth(),
                tex.getHeight(),
                0,
                GL.GL_RGBA,
                GL.GL_UNSIGNED_BYTE,
                tex.getPixels()
        );

        return id;
    }


    private static Texture readPixels(BufferedImage img, boolean storeAlphaChannel) {
    int[] packedPixels = new int[img.getWidth() * img.getHeight()];

    PixelGrabber pixelgrabber = new PixelGrabber(img, 0, 0, img.getWidth(), img.getHeight(), packedPixels, 0, img.getWidth());
    try {
      pixelgrabber.grabPixels();
    } catch (InterruptedException e) {
      throw new RuntimeException();
    }

    int bytesPerPixel = storeAlphaChannel ? 4 : 3;
    ByteBuffer unpackedPixels = BufferUtil.newByteBuffer(packedPixels.length * bytesPerPixel);

    for (int row = img.getHeight() - 1; row >= 0; row--) {
      for (int col = 0; col < img.getWidth(); col++) {
	int packedPixel = packedPixels[row * img.getWidth() + col];
	unpackedPixels.put((byte) ((packedPixel >> 16) & 0xFF));
	unpackedPixels.put((byte) ((packedPixel >> 8) & 0xFF));
	unpackedPixels.put((byte) ((packedPixel >> 0) & 0xFF));
	if (storeAlphaChannel) {
	  unpackedPixels.put((byte) ((packedPixel >> 24) & 0xFF));
	}
      }
    }

    unpackedPixels.flip();


    return new Texture(unpackedPixels, img.getWidth(), img.getHeight());
  }

  public static class Texture 
  {
    private ByteBuffer pixels;
    private int width;
    private int  height;

    public Texture( ByteBuffer pixels, int width, int height ) 
      {
	this.height = height;
	this.pixels = pixels;
	this.width = width;
      }

    public int getWidth() 
      {
	return width;
      }
    
    public int getHeight() 
      {
	return height;
      }

    public ByteBuffer getPixels() 
      {
	return pixels;
      }

  }
}
