package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.awt.Color;
import java.util.List;
import org.lwjgl.util.vector.Vector2f;



public class MS_PulsePopperPlugin extends BaseEveryFrameCombatPlugin {
    private CombatEngineAPI engine;
    
    private static final Color FX1 = new Color(245, 255, 245, 255);
    private static final Color FX2 = new Color(100, 215, 100, 255);
    private Vector2f ZERO = new Vector2f(0,0);
    private String sound;
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }
        
        List<DamagingProjectileAPI> projs = engine.getProjectiles();
        int size = projs.size();
        for (int i = 0; i < size; i++) {
            DamagingProjectileAPI proj = projs.get(i);
            if (proj.isFading() && proj.getProjectileSpecId() != null && (proj.getProjectileSpecId().startsWith("ms_cepc") 
                    || proj.getProjectileSpecId().startsWith("ms_mcepc") || proj.getProjectileSpecId().startsWith("ms_scatter")) 
                    && !proj.didDamage() && Math.random() > 0.5f) {
                float popSmall;
                float popBig;
                float vol;
                switch (proj.getProjectileSpecId()) {
                    case "ms_mcepc_blast":
                        popSmall = 15f;
                        popBig = 25f;
                        sound = "cepc_pop_big";
                        vol = 0.65f;
                        break;
                    case "ms_cepc_blast":
                    case "ms_scattercepc_clone":
                        popSmall = 12f;
                        popBig = 17f;
                        sound = "cepc_pop";
                        vol = 0.5f;
                        break;
                    default:
                        popSmall = 6f;
                        popBig = 11f;
                        sound = "cepc_pop_small";
                        vol = 0.4f;
                        break;
                }
                
                engine.addHitParticle(proj.getLocation(), ZERO, popSmall, 1f, 0.15f, FX1);
                engine.addHitParticle(proj.getLocation(), ZERO, popBig, 1f, 0.275f, FX2);
                Global.getSoundPlayer().playSound(sound, 1f, vol, proj.getLocation(), ZERO);
                
                engine.removeEntity(proj);
            } 
        }
    }
    
    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }
}
