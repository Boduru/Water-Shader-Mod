package net.fabricmc.boduru.shading;

import net.fabricmc.boduru.main.WaterShaderMod;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;

public class Framebuffers {
    private int framebufferWidth = 1920;
    private int framebufferHeight = 1080;

    private int reflectionFrameBuffer;
    private int reflectionTexture;
    private int reflectionDepthBuffer;

    private int refractionFrameBuffer;
    private int refractionTexture;
    private int refractionDepthTexture;

    public void setFramebuffersSize(int width, int height) {
        framebufferWidth = width;
        framebufferHeight = height;
    }

    public void resizeTextures(int width, int height) {
        setFramebuffersSize(width, height);

        // Reflection
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, reflectionTexture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, framebufferWidth, framebufferHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        // Refraction
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, refractionTexture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, framebufferWidth, framebufferHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        // Depth
        GL11.glBindTexture(GL30.GL_RENDERBUFFER, reflectionDepthBuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, framebufferWidth, framebufferHeight);
        GL11.glBindTexture(GL30.GL_RENDERBUFFER, 0);

        GL11.glBindTexture(GL30.GL_RENDERBUFFER, refractionDepthTexture);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, framebufferWidth, framebufferHeight);
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

    public void initializeReflectionFrameBuffer(int displayWidth, int displayHeight) {
        reflectionFrameBuffer = createFrameBuffer();
        reflectionTexture = createTextureAttachment(framebufferWidth, framebufferHeight);
        reflectionDepthBuffer = createDepthBufferAttachment(framebufferWidth, framebufferHeight);

        final boolean complete = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) == GL30.GL_FRAMEBUFFER_COMPLETE;
        WaterShaderMod.LOGGER.info("Reflection Framebuffer complete: " + complete);

        unbindCurrentFrameBuffer(displayWidth, displayHeight);
    }

    public void initializeRefractionFrameBuffer(int displayWidth, int displayHeight) {
        refractionFrameBuffer = createFrameBuffer();
        refractionTexture = createTextureAttachment(framebufferWidth, framebufferHeight);
        refractionDepthTexture = createDepthTextureAttachment(framebufferWidth, framebufferHeight);

        final boolean complete = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) == GL30.GL_FRAMEBUFFER_COMPLETE;
        WaterShaderMod.LOGGER.info("Refraction Framebuffer complete: " + complete);

        unbindCurrentFrameBuffer(displayWidth, displayHeight);
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
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP, GL11.GL_FALSE);
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
        GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
    }

    public void free() {
        GL30.glDeleteFramebuffers(reflectionFrameBuffer);
        GL11.glDeleteTextures(reflectionTexture);
        GL30.glDeleteRenderbuffers(reflectionDepthBuffer);
        GL30.glDeleteFramebuffers(refractionFrameBuffer);
        GL11.glDeleteTextures(refractionTexture);
        GL11.glDeleteTextures(refractionDepthTexture);
    }
}
