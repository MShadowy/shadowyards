package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_ArmorPiercePlugin extends BaseEveryFrameCombatPlugin {

    private static CombatEngineAPI engine;
    private static final Map<String, CollisionClass> originalCollisionClasses = new HashMap<>();
    //private static final Map<HullSize, Float> mag = new HashMap<>();

    // Sound to play while piercing a target's armor (should be loopable!)
    private static final String PIERCE_SOUND = "explosion_missile"; // TEMPORARY
    // Projectile ID (String), pierces shields (boolean)
    // Keep track of the original collision class (used for shield hits)
    private static final Color COLOR1 = new Color(165, 215, 145, 150);
    private static final Color COLOR2 = new Color(155, 255, 155, 150);
    
    private static final Vector2f ZERO = new Vector2f();
    
    private static final Set<String> PROJ_IDS = new HashSet();

    static {
        // Add all projectiles that should pierce armor here
        // Format: Projectile ID (String), pierces shields (boolean)
        PROJ_IDS.add("ms_rhpcblast");
        //PROJ_IDS.add("ms_rhpc_glow");
    }
    
    /*static {
        mag.put(HullSize.FIGHTER, 1.0f);
        mag.put(HullSize.FRIGATE, 0.7f);
        mag.put(HullSize.DESTROYER, 0.5f);
        mag.put(HullSize.CRUISER, 0.3f);
        mag.put(HullSize.CAPITAL_SHIP, 0.25f);
    }*/

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
            originalCollisionClasses.clear();
        }

        if (engine.isPaused()) {
            return;
        }

        // Scan all shots on the map for armor piercing projectiles
        for (DamagingProjectileAPI proj : engine.getProjectiles()) {
            String spec = proj.getProjectileSpecId();

            // Is this projectile armor piercing?
            if (!PROJ_IDS.contains(spec)) {
                continue;
            }
            
            // Register the original collision class (used for shield hits)
            if (!originalCollisionClasses.containsKey(spec)) {
                originalCollisionClasses.put(spec, proj.getCollisionClass());
            }

            // We'll do collision checks manually
            proj.setCollisionClass(CollisionClass.NONE);
            //Spawn random hit particles I guess
            Vector2f spawn = MathUtils.getRandomPointInCircle(proj.getLocation(), proj.getCollisionRadius() * 2);
            float size = MathUtils.getRandomNumberInRange(20f, 10f);
            float sharpDur = MathUtils.getRandomNumberInRange(0.2f, 0.6f);
            float smoothDur = MathUtils.getRandomNumberInRange(0.1f, 0.4f);
            
            if (Math.random() > 0.08 && !engine.isPaused()) {
                engine.addHitParticle(spawn, ZERO, size, MathUtils.getRandomNumberInRange(1f, 2f), sharpDur, COLOR2);
            }
            if (Math.random() > 0.04 && !engine.isPaused()) {
                engine.addSmoothParticle(spawn, ZERO, size * 2, MathUtils.getRandomNumberInRange(0.5f, 1f), smoothDur, COLOR1);
            }

            // Find nearby ships, missiles and asteroids
            List<CombatEntityAPI> toCheck = new ArrayList<>();
            toCheck.addAll(CombatUtils.getShipsWithinRange(proj.getLocation(), proj.getCollisionRadius() + 5f));
            toCheck.addAll(CombatUtils.getMissilesWithinRange(proj.getLocation(), proj.getCollisionRadius() + 5f));
            toCheck.addAll(CombatUtils.getAsteroidsWithinRange(proj.getLocation(), proj.getCollisionRadius() + 5f));

            // Don't include the ship that fired this projectile!
            toCheck.remove(proj.getSource());
            for (CombatEntityAPI entity : toCheck) {
                // Check for an active phase cloak
                if (entity.getCollisionClass() == CollisionClass.NONE) {
                    continue;
                }

                // Check for a shield hit
                if ((entity.getShield() != null && entity.getShield().isOn() && entity.getShield().isWithinArc(proj.getLocation()))) {
                    // If we hit a shield, enable collision
                    proj.setCollisionClass(originalCollisionClasses.get(spec));
                } // Check if the projectile is inside the entity's bounds
                else if (CollisionUtils.isPointWithinBounds(proj.getLocation(), entity)) {
                    // Calculate projectile speed
                    float speed = proj.getVelocity().length();

                    // Change how damamge is applied -- preferably a set % based on hull size as opposed to collission radius
                    // Damage per frame is based on how long it would take
                    // the projectile to travel through the entity
                    float modifier = 1.0f / ((entity.getCollisionRadius() * 2f) / speed);
                    float damage = (proj.getDamageAmount() * amount) * modifier;
                    float emp = (proj.getEmpAmount() * amount) * modifier;

                    // Apply damage and slow the projectile
                    // Note: BALLISTIC_AS_BEAM projectiles won't be slowed!
                    engine.applyDamage(entity, proj.getLocation(), damage, proj.getDamageType(), emp, true, true, proj.getSource());
                    //proj.getVelocity().scale(1.0f - (amount * 1.5f));

                    // Render the hit
                    engine.spawnExplosion(proj.getLocation(), entity.getVelocity(), COLOR1, speed * amount * 2f, .5f);

                    // Play piercing sound (only one sound active per projectile)
                    Global.getSoundPlayer().playLoop(PIERCE_SOUND, proj, 1f, 1f, proj.getLocation(), entity.getVelocity());
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
        if (engine == null) {
            return;
        }
        
        for (DamagingProjectileAPI proj : engine.getProjectiles()) {
            String spec = proj.getProjectileSpecId();
            
            if (!PROJ_IDS.contains(spec)) {
                continue;
            }
            
            Vector2f Here = new Vector2f(0,0) ;
            Here.x = proj.getLocation().x;
            Here.y = proj.getLocation().y;
                    
            SpriteAPI sprite = Global.getSettings().getSprite("flare", "nidhoggr_ALF");
                    
            if (!engine.isPaused()) {
                sprite.setAlphaMult(MathUtils.getRandomNumberInRange(0.9f, 1f));
            } else {
                float tAlf = sprite.getAlphaMult();
                sprite.setAlphaMult(tAlf);
            }
            sprite.setSize(800, 100);
            sprite.setAdditiveBlend();
            sprite.renderAtCenter(Here.x, Here.y);
        }
    }
}
