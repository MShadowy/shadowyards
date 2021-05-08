package data.scripts.plugins;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class MS_PandoraZapMaster extends BaseEveryFrameCombatPlugin {

    // Sound to play while piercing a target's armor (should be loopable!)
    // Projectile ID (String), pierces shields (boolean)
    private static final Set<String> PROJ_IDS = new HashSet<>();

    static {
        // Add all projectiles that should zap here
        PROJ_IDS.add("ms_pandora_zap");
    }
    
    private CombatEngineAPI engine;
    
    @Override
    public void advance(float amount, List events) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }

        // Scan all shots on the map for zapping projectiles
        DamagingProjectileAPI[] projectiles = engine.getProjectiles().toArray(new DamagingProjectileAPI[engine.getProjectiles().size()]);
        for (DamagingProjectileAPI proj : projectiles) {
            String spec = proj.getProjectileSpecId();

            // Is this projectile a zapper?
            if (!PROJ_IDS.contains(spec)) {
                continue;
            }

            if (engine.getProjectiles().contains(proj) && proj.isFading() == true) {
                Vector2f point = proj.getLocation();

                engine.spawnEmpArc(proj.getSource(), proj.getWeapon().getLocation(), null, new SimpleEntity(point),
                        DamageType.ENERGY,
                        0.0f,
                        0.0f, // emp 
                        100000f, // max range 
                        null,
                        20f, // thickness
                        new Color(125, 155, 115, 40),
                        new Color(165, 215, 145, 40)
                );

                engine.removeEntity(proj);
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }
}
