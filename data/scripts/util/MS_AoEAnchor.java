package data.scripts.util;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_AoEAnchor {
    private static final Vector2f zeroVector = new Vector2f();
    
    public static Vector2f getLocalPosition(
        Vector2f worldPosition,
        CombatEntityAPI relativeTo
    ) {
        return getLocalPosition(worldPosition, relativeTo.getLocation(), relativeTo.getFacing());
    }
    
    public static Vector2f getLocalPosition(
        Vector2f worldPosition,
        Vector2f relativeToPosition,
        float relativeToFacing
    ) {
        Vector2f localPosition = Vector2f.sub(worldPosition, relativeToPosition, new Vector2f());
        VectorUtils.rotateAroundPivot(
            localPosition,
            new Vector2f(),
            -relativeToFacing
        );

        return localPosition;
    }
    
    private Vector2f relativePosition;
    private float relativeFacing;
    private CombatEntityAPI relativeTo;
    
    public MS_AoEAnchor(
        Vector2f relativePosition,
        float relativeFacing,
        CombatEntityAPI relativeTo
    ) {
        this.relativePosition = relativePosition;
        this.relativeFacing = relativeFacing;
        this.relativeTo = relativeTo;
    }

    public MS_AoEAnchor(
        Vector2f worldPosition,
        CombatEntityAPI relativeTo
    ) {
        this.relativePosition = worldPosition;
        this.relativeFacing = 0F;
        this.relativeTo = relativeTo;
    }

    public MS_AoEAnchor(
        Vector2f worldPosition,
        float facing
    ) {
        this.relativePosition = worldPosition;
        this.relativeFacing = facing;
        this.relativeTo = null;
    }

    public MS_AoEAnchor(
        Vector2f worldPosition
    ) {
        this.relativePosition = worldPosition;
        this.relativeFacing = 0F;
        this.relativeTo = null;
    }

    public CombatEntityAPI getEntity()
    {
        return relativeTo;
    }

    public Vector2f getLocation()
    {
        return getLocation(new Vector2f(), 0F);
    }

    public Vector2f getLocation(
        float timeOffset
    ) {
        return getLocation(new Vector2f(), timeOffset);
    }

    public Vector2f getLocation(
        Vector2f output
    ) {
        return getLocation(output, 0F);
    }

    public Vector2f getLocation(
        Vector2f output,
        float timeOffset
    ) {
        if (null == relativeTo) {
            output.set(relativePosition);
            return output;
        }

        VectorUtils.rotateAroundPivot(
            relativePosition,
            zeroVector,
            relativeTo.getFacing(),
            output
        );

        output.x += relativeTo.getLocation().x + relativeTo.getVelocity().x * timeOffset;
        output.y += relativeTo.getLocation().y + relativeTo.getVelocity().y * timeOffset;

        return output;
    }

    public float getFacing() {
        if (null == relativeTo) {
            return relativeFacing;
        }

        return MathUtils.clampAngle(relativeTo.getFacing() + relativeFacing);
    }
}
