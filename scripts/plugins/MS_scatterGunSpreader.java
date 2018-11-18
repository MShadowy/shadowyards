package data.scripts.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_scatterGunSpreader extends BaseEveryFrameCombatPlugin {
    
    private static final Set<String> SHOTGUNPROJ_IDS = new HashSet<>(1);
    
    private CombatEngineAPI engine;

    static {
        //add Projectile IDs here.
        SHOTGUNPROJ_IDS.add("ms_scattercepc");
    }
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }

        List<DamagingProjectileAPI> projectiles = engine.getProjectiles();
        int size = projectiles.size();
        for (int i = 0; i < size; i++) {
            DamagingProjectileAPI proj = projectiles.get(i);
            String spec = proj.getProjectileSpecId();
            
            if (SHOTGUNPROJ_IDS.contains(spec)) {
                Vector2f loc = proj.getLocation();
                Vector2f vel = proj.getVelocity();
                int shotCount = (20);
                for (int j = 0; j < shotCount; j++) {
                    Vector2f randomVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(20f, 60f));
                    randomVel.x += vel.x;
                    randomVel.y += vel.y;
                    //spec + "_clone" means is, if its got the same name in its name (except the "_clone" part) then it must be that weapon.
                    engine.spawnProjectile(proj.getSource(), proj.getWeapon(), spec + "_clone", loc, proj.getFacing(), randomVel);
                }
                engine.removeEntity(proj);
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }
}
