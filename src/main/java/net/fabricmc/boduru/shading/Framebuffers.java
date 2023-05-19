package net.fabricmc.boduru.shading;

import net.fabricmc.boduru.main.WaterShaderMod;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Framebuffers {
    private int FRAMEBUFFER_WIDTH = 1920;
    private int FRAMEBUFFER_HEIGHT = 1080;

    public static int frameCount = 0;

    private int reflectionFrameBuffer;
    private int reflectionTexture;
    private int reflectionDepthBuffer;

    private int refractionFrameBuffer;
    private int refractionTexture;
    private int refractionDepthTexture;

    private int worldFrameBuffer;
    private int worldTexture;
    private int worldDepthBuffer;

    public void setFramebuffersSize(int width, int height) {
        FRAMEBUFFER_WIDTH = width;
        FRAMEBUFFER_HEIGHT = height;
    }

    public void resizeTextures(int width, int height) {
        setFramebuffersSize(width, height);

        // Reflection
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, reflectionTexture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        // Refraction
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, refractionTexture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        // World
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, worldTexture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        // Depth
        GL11.glBindTexture(GL30.GL_RENDERBUFFER, reflectionDepthBuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT);
        GL11.glBindTexture(GL30.GL_RENDERBUFFER, 0);

        GL11.glBindTexture(GL30.GL_RENDERBUFFER, refractionDepthTexture);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT);
        GL11.glBindTexture(GL30.GL_RENDERBUFFER, 0);

        GL11.glBindTexture(GL30.GL_RENDERBUFFER, worldDepthBuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT);
        GL11.glBindTexture(GL30.GL_RENDERBUFFER, 0);

    }

    public void unbindCurrentFrameBuffer(int displayWidth, int displayHeight) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(0, 0, displayWidth, displayHeight);
    }

    public int getReflectionFBO() {
        return reflectionFrameBuffer;
    }

    public int getRefractionFBO() {
        return refractionFrameBuffer;
    }

    public int getReflectionTexture() {
        return reflectionTexture;
    }

    public int getRefractionTexture() {
        return refractionTexture;
    }

    public void initializeWorldFrameBuffer(int displayWidth, int displayHeight) {
        worldFrameBuffer = createFrameBuffer();
        worldTexture = createTextureAttachment(FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT);
        worldDepthBuffer = createDepthBufferAttachment(FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT);

        final boolean complete = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) == GL30.GL_FRAMEBUFFER_COMPLETE;
        WaterShaderMod.LOGGER.info("World Framebuffer complete: " + complete);

        unbindCurrentFrameBuffer(displayWidth, displayHeight);
    }

    public void initializeReflectionFrameBuffer(int displayWidth, int displayHeight) {
        reflectionFrameBuffer = createFrameBuffer();
        reflectionTexture = createTextureAttachment(FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT);
        reflectionDepthBuffer = createDepthBufferAttachment(FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT);

        final boolean complete = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) == GL30.GL_FRAMEBUFFER_COMPLETE;
        WaterShaderMod.LOGGER.info("Reflection Framebuffer complete: " + complete);

        unbindCurrentFrameBuffer(displayWidth, displayHeight);
    }

    public void initializeRefractionFrameBuffer(int displayWidth, int displayHeight) {
        refractionFrameBuffer = createFrameBuffer();
        refractionTexture = createTextureAttachment(FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT);
        refractionDepthTexture = createDepthTextureAttachment(FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT);

        final boolean complete = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) == GL30.GL_FRAMEBUFFER_COMPLETE;
        WaterShaderMod.LOGGER.info("Refraction Framebuffer complete: " + complete);

        unbindCurrentFrameBuffer(displayWidth, displayHeight);
    }

    private void bindFrameBuffer(int frameBuffer, int width, int height){
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
        GL11.glViewport(0, 0, width, height);
    }

    private int createFrameBuffer() {
        int frameBuffer = GL30.glGenFramebuffers();
        // generate name for frame buffer
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
        // create the framebuffer
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        // indicate that we will always render to color attachment 0
        return frameBuffer;
    }

    private int createTextureAttachment(int width, int height) {
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height,
                0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL30.GL_GENERATE_MIPMAP, GL11.GL_FALSE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, texture, 0);

        return texture;
    }

    private int createDepthTextureAttachment(int width, int height){
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, width, height,
                0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, texture, 0);

        return texture;
    }

    private int createDepthBufferAttachment(int width, int height) {
        int depthBuffer = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, width, height);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);

        return depthBuffer;
    }

    public static void CopyFrameBufferTexture(int width, int height, int fboIn, int fboOut) {
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, fboIn);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, fboOut);
        GL30.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
        GL30.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_NEAREST);
    }

    public static void SaveImage(int width, int height) {
        //First off, we need to access the pixel data of the Display. Without this, we don't know what to save!
        GL11.glReadBuffer(GL11.GL_FRONT);
        int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        //Save the screen image
        File file = new File("/Users/jimpavan/Documents/projects/Minecraft/Water Shader Mod/output.png"); // The file to save to.
        String format = "png"; // Example: "PNG" or "JPG"
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for(int x = 0; x < width; x++)
        {
            for(int y = 0; y < height; y++)
            {
                int i = (x + (width * y)) * bpp;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }

        try {
            ImageIO.write(image, format, file);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void free() {
        GL30.glDeleteFramebuffers(reflectionFrameBuffer);
        GL11.glDeleteTextures(reflectionTexture);
        GL30.glDeleteRenderbuffers(reflectionDepthBuffer);
        GL30.glDeleteFramebuffers(refractionFrameBuffer);
        GL11.glDeleteTextures(refractionTexture);
        GL11.glDeleteTextures(refractionDepthTexture);

        GL30.glDeleteFramebuffers(worldFrameBuffer);
        GL11.glDeleteTextures(worldTexture);
    }
}
