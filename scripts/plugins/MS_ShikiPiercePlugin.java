package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
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
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class MS_ShikiPiercePlugin extends BaseEveryFrameCombatPlugin {
    private static CombatEngineAPI engine;
    private static final Map<String, CollisionClass> originalCollisionClasses = new HashMap<>();

    // Sound to play while piercing a target's armor (should be loopable!)
    private static final String PIERCE_SOUND = "explosion_missile"; // TEMPORARY
    // Projectile ID (String), pierces shields (boolean)
    // Keep track of the original collision class (used for shield hits)
    private static final Color COLOR1 = new Color(165, 215, 145, 150);
    private static final Color COLOR2 = new Color(155, 255, 155, 150);
    
    private static final Vector2f ZERO = new Vector2f();
    
    private static final Set<String> PROJ_IDS = new HashSet();
    
    private float MAX_DAMAGE;
    private float DAMAGE_PER_TICK;
    private float EMP_PER_TICK;
    private float DAMAGE_TOTAL;
    
    private final IntervalUtil interval = new IntervalUtil(0.05f, 0.05f);
    private boolean runOnce = false;

    static {
        PROJ_IDS.add("ms_microLanceBlast");
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
            originalCollisionClasses.clear();
        }

        if (engine.isPaused()) {
            return;
        }
        
        interval.advance(amount);

        // Scan all shots on the map for armor piercing projectiles
        for (DamagingProjectileAPI proj : engine.getProjectiles()) {
            String spec = proj.getProjectileSpecId();

            // Is this projectile armor piercing?
            if (!PROJ_IDS.contains(spec)) {
                continue;
            }
            
            if (!runOnce) {
                runOnce = true;
                
                MAX_DAMAGE = (proj.getDamageAmount());
                DAMAGE_PER_TICK = MAX_DAMAGE * 0.5f;
                EMP_PER_TICK = proj.getEmpAmount() * 0.5f;
            }
            
            // Register the original collision class (used for shield hits)
            if (!originalCollisionClasses.containsKey(spec)) {
                originalCollisionClasses.put(spec, proj.getCollisionClass());
            }

            // We'll do collision checks manually
            proj.setCollisionClass(CollisionClass.NONE);
            //Spawn random hit particles I guess
            //target a vector directly behind the proj
            //Vector2f dir;
            Vector2f point = new Vector2f(-30f, 0f);
            VectorUtils.rotate(point, proj.getFacing(), point);
            Vector2f.add(point, proj.getLocation(), point);
            
            Vector2f spawn = MathUtils.getRandomPointInCircle(proj.getLocation(), proj.getCollisionRadius());
            float size = MathUtils.getRandomNumberInRange(20f, 10f);
            float sharpDur = MathUtils.getRandomNumberInRange(0.2f, 0.6f);
            float smoothDur = MathUtils.getRandomNumberInRange(0.1f, 0.4f);
            
            if (Math.random() > 0.24 && !engine.isPaused()) {
                engine.addHitParticle(spawn, ZERO, size, MathUtils.getRandomNumberInRange(1f, 2f), sharpDur, COLOR2);
            }
            if (Math.random() > 0.16 && !engine.isPaused()) {
                engine.addSmoothParticle(spawn, ZERO, size * 2, MathUtils.getRandomNumberInRange(0.5f, 1f), smoothDur, COLOR1);
            }
            if (Math.random() > 0.5 && !engine.isPaused()) {
                engine.spawnEmpArc(proj.getSource(), spawn, null, new SimpleEntity(point), 
                        DamageType.ENERGY,
                        0.0f,
                        0.0f,
                        10000f,
                        null,
                        4f,
                        COLOR1,
                        COLOR1);
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

                    if (interval.intervalElapsed()) {
                        DAMAGE_TOTAL = DAMAGE_TOTAL + DAMAGE_PER_TICK;
                        float DAMAGE_REMAINING = MAX_DAMAGE - DAMAGE_TOTAL;
                        
                        engine.applyDamage(entity, proj.getLocation(), DAMAGE_PER_TICK, proj.getDamageType(), EMP_PER_TICK, true, true, proj.getSource());
                        proj.setDamageAmount(DAMAGE_REMAINING);
                        
                        // Render the hit
                        engine.spawnExplosion(proj.getLocation(), entity.getVelocity(), COLOR1, speed * amount * 2f, .5f);
                        // Play piercing sound (only one sound active per projectile)
                        Global.getSoundPlayer().playLoop(PIERCE_SOUND, proj, 1f, 1f, proj.getLocation(), entity.getVelocity());
                        
                        if (DAMAGE_REMAINING <= 0) {
                            DAMAGE_PER_TICK = 0;
                            proj.setCollisionClass(originalCollisionClasses.get(spec));
                            proj.isFading();
                        }
                    }
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
            sprite.setSize(400, 50);
            sprite.setAdditiveBlend();
            sprite.renderAtCenter(Here.x, Here.y);
        }
    }
}
