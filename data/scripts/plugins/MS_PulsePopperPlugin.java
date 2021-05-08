package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.awt.Color;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.vector.Vector2f;

public class MS_PulsePopperPlugin extends BaseEveryFrameCombatPlugin {
    private CombatEngineAPI engine;
    
    private static final Color FX1 = new Color(165, 255, 240, 255);
    private static final Color FX2 = new Color(75, 250, 135, 255);
    private Vector2f ZERO = new Vector2f(0,0);
    private String sound;
    
    private DamagingProjectileAPI proj;
    
    public MS_PulsePopperPlugin(@NotNull DamagingProjectileAPI proj) {
        this.proj = proj;
    }
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }
        
        //Checks if our script should be removed from the combat engine
	if (proj == null || proj.didDamage() || !Global.getCombatEngine().isEntityInPlay(proj)) {
		Global.getCombatEngine().removePlugin(this);
		return;
	}
        
        float popSmall;
        float popBig;
        float vol;
        switch (proj.getProjectileSpecId()) {
            case "ms_mcepc_blast":
            case "ms_scattercepc_clone":
                popSmall = 15f;
                popBig = 25f;
                sound = "cepc_pop_big";
                vol = 0.65f;
                break;
            case "ms_gigacepc_blast":
            case "ms_cepc_blast":
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
        
        if (proj.isFading() && !proj.didDamage() && Math.random() > 0.5f) {
            engine.addHitParticle(proj.getLocation(), ZERO, popSmall, 1f, 0.15f, FX1);
            engine.addHitParticle(proj.getLocation(), ZERO, popBig, 1f, 0.275f, FX2);
            Global.getSoundPlayer().playSound(sound, 1f, vol, proj.getLocation(), ZERO);
                
            engine.removeEntity(proj);
        }
    }
    
    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }
}
