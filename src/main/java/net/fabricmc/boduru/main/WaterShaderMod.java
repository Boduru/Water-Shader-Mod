package net.fabricmc.boduru.main;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.boduru.shading.*;
import net.fabricmc.boduru.loading.TextureLoader;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL30.GL_CLIP_DISTANCE0;

public class WaterShaderMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("water_shader_mod");

    public static RenderPass renderPass;
    public static ClipPlane clipPlane;
    public static Framebuffers framebuffers;
    public static VanillaShaders vanillaShaders;
    public static CameraSav cameraSav;
    public static TextureLoader textureLoader;

    private static String dudvMapPath = "../src/main/resources/assets/water_shader/textures/dudvmap.png";
    private static boolean isInitialized = false;

    @Override
    public void onInitialize() {
        LOGGER.info("Water Shader Mod Ready!");

        // Initialize static variables
        clipPlane = new ClipPlane();
        framebuffers = new Framebuffers();
        renderPass = RenderPass.getInstance();
        vanillaShaders = VanillaShaders.getInstance();
        cameraSav = new CameraSav();
        textureLoader = new TextureLoader();

        // Initialize clip plane
        clipPlane.setHeight(61.8f);
    }

    public static void InitContext() {
        // Do initialization once at game start
        if (isInitialized) {
            return;
        }

        // Get the window size
        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();

        // Initialize water framebuffers
        framebuffers.setFramebuffersSize(width, height);
        framebuffers.initializeReflectionFrameBuffer(width, height);
        framebuffers.initializeRefractionFrameBuffer(width, height);

        // Load DUDV Map
        long r = textureLoader.loadTexture(dudvMapPath, "dudvmap");
        System.out.println("DUDV Map Texture ID: " + r);

        // Switch to initialized state
        isInitialized = true;
    }

    public static void EnableClipPlane() {
        // Activate Clipping Plane 0
        glEnable(GL_CLIP_DISTANCE0);
    }
}
