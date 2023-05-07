package net.fabricmc.boduru.shading;

/**
 * The render pass class is used to track the current render pass and what effect should be applied.
 * The class is used as a singleton state machine.
 */

public class RenderPass {
    private boolean drawWater = false;

    private static RenderPass Instance;

    private RenderPass() {}

    public static RenderPass getInstance() {
        if (Instance == null)
            Instance = new RenderPass();

        return Instance;
    }

    public void setDrawWater(boolean drawWater) {
        this.drawWater = drawWater;
    }

    public boolean doDrawWater() {
        return this.drawWater;
    }
}
