package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.vector.Vector2f;

public class MS_TanithSparkHandler extends BaseEveryFrameCombatPlugin {
    private final List<DamagingProjectileAPI> alreadyRegisteredProjectiles = new ArrayList<>();
    private static final String PROJ_ID = "ms_tanithZap";
    private static final String SPARK_ID = "ms_dSpark";
    
    private static final String SPARK_BURST_SOUND = "explosion_from_damage";
    private static final Color SPARK_BURST_COLOR = Color.CYAN;
    private static final int NUM_SPARKS = 10;
    
    private static final Vector2f NULLVEL = new Vector2f(0, 0);
    
    private DamagingProjectileAPI proj;
    
    private final List sparks = new ArrayList(16);
    
    private CombatEngineAPI engine;
    
    public void particleBurst(DamagingProjectileAPI proj)
    {
        // To avoid collision issues, remove projectile before spawning sparks
        // First, need to grab some variables from it
        engine = Global.getCombatEngine();
        ShipAPI ship = proj.getSource();
        WeaponAPI weapon = proj.getWeapon();
        Vector2f loc = proj.getLocation(), vel = proj.getVelocity();
        engine.removeEntity(proj);

        // Create the sparks and give them the source's position/velocity
        for (int x = 0; x < NUM_SPARKS; x++)
        {
            // Spawn the spark projectile and register it with the master
            // Instead of using the kludgy custom data class from the earlier version,
            // we'll let the sparks handle their own behavior internally, so this just spawns the sparks
            engine.spawnProjectile(ship, weapon, SPARK_ID, loc, (float) x * (360f / (float) NUM_SPARKS), NULLVEL);
        }

        // Spawn an explosion and play the sound
        engine.spawnExplosion(loc, NULLVEL, SPARK_BURST_COLOR, 7.5f, 1f);
        Global.getSoundPlayer().playSound(SPARK_BURST_SOUND, 1f, 1f, loc, vel);
    }
    
    public MS_TanithSparkHandler(@NotNull DamagingProjectileAPI proj) {
        this.proj = proj;
    }
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine.isPaused() || engine == null) return;
        
        for (DamagingProjectileAPI proj : engine.getProjectiles()) {
            if (!PROJ_ID.equals(proj.getProjectileSpecId())) continue;
            
            if (proj.didDamage() || proj.isFading()) {
                particleBurst(proj);
            }
        }
    }   
    
    @Override
    public void init(CombatEngineAPI engine)
    {
        // Do some cleanup at the start of each battle
        this.engine = engine;
        sparks.clear();
    }
}
