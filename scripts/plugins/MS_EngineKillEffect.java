package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_EngineKillEffect extends BaseEveryFrameCombatPlugin {

    // Sound to play while piercing a target's armor (should be loopable!)
    private static final String PIERCE_SOUND = "ms_enginekill_impact"; // TEMPORARY
    // Projectile ID (String), pierces shields (boolean)
    private static final Color effectColor = new Color(165, 215, 145, 150);
    private static final Color effectColorCore = new Color(255, 255, 255, 255);
    private static final float ENGINE_KILL_RADIUS_SQUARED = 30f * 30f;

    private static final Set<String> PROJ_IDS = new HashSet<>();

    static {
        // Add all the enginekiller
        PROJ_IDS.add("ms_enginekill");
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();
        
        // Scan all shots on the map for armor piercing projectiles
        for (DamagingProjectileAPI proj : engine.getProjectiles()) {
            Vector2f projLocation = proj.getLocation();
            String spec = proj.getProjectileSpecId();
            
            if (engine.isPaused()) {
                continue;
            }

            // Is this projectile armor piercing?
            if (!PROJ_IDS.contains(spec)) {
                continue;
            }

            // We'll do collision checks manually
            proj.setCollisionClass(CollisionClass.NONE);

            // Find nearby ships
            List<ShipAPI> toCheck = CombatUtils.getShipsWithinRange(projLocation, proj.getCollisionRadius() + 5f);
            // Don't include the ship that fired this projectile!
            toCheck.remove(proj.getSource());

            for (CombatEntityAPI entity : toCheck) {
                // Check for an active phase cloak
                if (entity.getCollisionClass() == CollisionClass.NONE) {
                            continue;
                } // Check if the projectile is inside the entity's bounds

                if (CollisionUtils.isPointWithinBounds(projLocation, entity)) {
                    if (entity instanceof ShipAPI) {
                        ShipAPI ship = (ShipAPI) entity;
                        List<ShipEngineAPI> shipEngines = ship.getEngineController().getShipEngines();

                        for (ShipEngineAPI shipEngine : shipEngines) {
                            // Apply the engine kill effect
                            if (shipEngine.isDisabled() == false) {
                                Vector2f shipEngineLocation = shipEngine.getLocation();
                                float distanceSq = MathUtils.getDistanceSquared(shipEngineLocation, projLocation);

                                if (distanceSq <= ENGINE_KILL_RADIUS_SQUARED) {
                                    shipEngine.disable();
                                    engine.spawnEmpArc(proj.getSource(), projLocation, entity, entity,
                                            DamageType.OTHER, 0, 0, 50f,
                                            null, 15f, effectColor, effectColorCore);
                                }
                            }
                        }
                    }

                    // Do other stuff relating to hitting the ship, such as applying damage per second equal to the projectile hit damage
                    engine.applyDamage(entity, projLocation, amount * proj.getDamageAmount(), proj.getDamageType(), amount * proj.getEmpAmount(), true, false, proj.getSource());

                    // Play piercing sound (only one sound active per projectile)
                    Global.getSoundPlayer().playSound(PIERCE_SOUND, 1f, 1f, projLocation, entity.getVelocity());
                }
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
    }
    
    @Override
    public void renderInUICoords(ViewportAPI viewport) {
    }
     
    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {  
    }
}
