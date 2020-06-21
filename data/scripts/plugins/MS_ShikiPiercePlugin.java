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
import java.util.WeakHashMap;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class MS_ShikiPiercePlugin extends BaseEveryFrameCombatPlugin {
    private static CombatEngineAPI engine;
    private static final Map<String, CollisionClass> ORIGINAL_COLLISSION_CLASSES = new HashMap<>();

    // Sound to play while piercing a target's armor (should be loopable!)
    private static final String PIERCE_SOUND = "explosion_missile"; // TEMPORARY
    // Projectile ID (String), pierces shields (boolean)
    // Keep track of the original collision class (used for shield hits)
    private static final Color COLOR1 = new Color(165, 215, 145, 150);
    private static final Color COLOR2 = new Color(155, 255, 155, 150);
    private static final Color COLOR3 = new Color(115, 185, 165, 150);
    
    private static final Vector2f ZERO = new Vector2f();
    
    private static final Set<String> PROJ_IDS = new HashSet();
    
    private float MAX_DAMAGE;
    private float DAMAGE_PER_TICK;
    private float EMP_PER_TICK;
    private float DAMAGE_TOTAL;
    
    private final IntervalUtil interval = new IntervalUtil(0.05f, 0.05f);
    private boolean runOnce = false;
    
    private final Map<DamagingProjectileAPI, Float> projectileTrailIDs = new WeakHashMap<>();

    private final Map<DamagingProjectileAPI, Float> projectileTrailIDs2 = new WeakHashMap<>();

    static {
        PROJ_IDS.add("ms_microLanceBlast");
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
            ORIGINAL_COLLISSION_CLASSES.clear();
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
            if (!ORIGINAL_COLLISSION_CLASSES.containsKey(spec)) {
                ORIGINAL_COLLISSION_CLASSES.put(spec, proj.getCollisionClass());
            }

            // We'll do collision checks manually
            proj.setCollisionClass(CollisionClass.NONE);
            //Spawn random hit particles I guess
            //target a vector directly behind the proj
            //Vector2f dir;
            Vector2f point = new Vector2f(-30f, 0f);
            VectorUtils.rotate(point, proj.getFacing(), point);
            Vector2f.add(point, proj.getLocation(), point);
            
            if (projectileTrailIDs.get(proj) == null)
            {
                projectileTrailIDs.put(proj, MagicTrailPlugin.getUniqueID());
            }
            
            if (projectileTrailIDs2.get(proj) == null)
            {
                projectileTrailIDs2.put(proj, MagicTrailPlugin.getUniqueID());
            }
            // Then, actually spawn a trail
            MagicTrailPlugin.AddTrailMemberAdvanced(proj, projectileTrailIDs.get(proj), Global.getSettings().getSprite("sra_trails",
                        "rhpcb_proj_trail"), proj.getLocation(), 0f, 0f, proj.getFacing() - 180f, 
                    0f, 0f, 16f, 0f, COLOR2, COLOR3, 0.6f, 0f, 0.1f, 0.3f, GL_SRC_ALPHA, GL_ONE, 
                    128, 500, new Vector2f(0,0), null);
            
            MagicTrailPlugin.AddTrailMemberAdvanced(proj, projectileTrailIDs2.get(proj), Global.getSettings().getSprite("sra_trails",
                        "rhpcb_secondary_proj_trail"), proj.getLocation(), 0f, 0f, proj.getFacing() - 180f, 
                    0f, 0f, 24f, 0f, COLOR3, new Color(60, 90, 120), 0.4f, 0f, 0.15f, 0.35f, GL_SRC_ALPHA, GL_ONE, 
                    128, 500, new Vector2f(0,0), null);

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
                    proj.setCollisionClass(ORIGINAL_COLLISSION_CLASSES.get(spec));
                    // since the shot tends to bash through shields and do hull damage on overloads, make it fade out and set damage to 0
                    if (proj.didDamage()) {
                        proj.setDamageAmount(0);
                    }
                } // Check if the projectile is inside the entity's bounds
                else if (CollisionUtils.isPointWithinBounds(proj.getLocation(), entity)) {
                    // Calculate projectile speed
                    float speed = proj.getVelocity().length();

                    if (interval.intervalElapsed()) {
                        DAMAGE_TOTAL = DAMAGE_TOTAL + DAMAGE_PER_TICK;
                        float DAMAGE_REMAINING = MAX_DAMAGE - DAMAGE_TOTAL;
                        
                        //only do damage if DAMAGE_REMAINING is a positive value
                        if (DAMAGE_REMAINING > 0) {
                            engine.applyDamage(entity, proj.getLocation(), DAMAGE_PER_TICK, proj.getDamageType(), EMP_PER_TICK, true, true, proj.getSource());
                        }
                        
                        //in case the proj hits a shield we want it to apply the correct amount of damage
                        proj.setDamageAmount(DAMAGE_REMAINING);
                        
                        // Render the hit
                        engine.spawnExplosion(proj.getLocation(), entity.getVelocity(), COLOR1, speed * amount * 2f, .5f);
                        // Play piercing sound (only one sound active per projectile)
                        Global.getSoundPlayer().playLoop(PIERCE_SOUND, proj, 1f, 1f, proj.getLocation(), entity.getVelocity());
                        
                        if (DAMAGE_REMAINING <= 0) {
                            proj.setCollisionClass(ORIGINAL_COLLISSION_CLASSES.get(spec));
                            proj.isFading();
                        }
                    }
                }
            }
            
            //reset the damage
            if (proj.isFading() && proj.getDamageAmount() < MAX_DAMAGE) {
                proj.setDamageAmount(MAX_DAMAGE);
                DAMAGE_TOTAL = 0;
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