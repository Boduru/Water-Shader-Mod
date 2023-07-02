package net.fabricmc.boduru.shading;

/**
 * The render pass class is used to track the current render pass and what effect should be applied.
 * The class is used as a singleton state machine.
 */

public class RenderPass {
    public enum Pass {
        REFLECTION,
        REFRACTION,
        WATER
    }

    private Pass currentPass = Pass.REFLECTION;

    private static RenderPass instance;

    private RenderPass() {}

    public static RenderPass getInstance() {
        if (instance == null)
            instance = new RenderPass();

        return instance;
    }

    /**
     * Switches to the next render pass and loops back to the first pass when the last pass is reached.
     */
    public void nextRenderPass() {
        currentPass = Pass.values()[(currentPass.ordinal() + 1) % Pass.values().length];
    }

    public Pass getCurrentPass() {
        return this.currentPass;
    }
}
