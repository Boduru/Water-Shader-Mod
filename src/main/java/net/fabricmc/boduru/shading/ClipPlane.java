package net.fabricmc.boduru.shading;

/**
 * The clip plane is used to define the height of the clip plane.
 * Vertex culling is done using the clip plane as reference for which vertex to discard.
 */

public class ClipPlane {
    private float y;

    public ClipPlane(float clipY) {
        y = clipY;
    }

    public float getY() {
        return y;
    }
}
