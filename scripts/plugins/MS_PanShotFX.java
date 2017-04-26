package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.lwjgl.util.vector.Vector2f;

public class MS_PanShotFX extends BaseEveryFrameCombatPlugin {

    //How many frames between each explosion
    private static final float explosionNum = 0.3f;
    private static final float explosionDur = .5f;
    //Effects color (should match the projectile color)
    private static final Color effectColor = new Color(165, 215, 145, 150);

    private static final Set<String> shots = new HashSet<>();

    static {
        shots.add("ms_pandora_wave");
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        for (DamagingProjectileAPI proj : Global.getCombatEngine().getProjectiles()) {
            Vector2f spawn = proj.getLocation();
            Vector2f explosionVelocity = (Vector2f) new Vector2f(proj.getVelocity()).scale(0.9f);

            if (!shots.contains(proj.getProjectileSpecId())) {
                continue;
            }

            for (int x = 0; x < explosionNum; x++) {
                Global.getCombatEngine().spawnExplosion(spawn, explosionVelocity, effectColor, 2f, explosionDur);
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
