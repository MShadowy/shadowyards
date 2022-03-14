package data.scripts.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.jetbrains.annotations.NotNull;

public class MS_VampVFXPlugin extends BaseEveryFrameCombatPlugin {
    private DamagingProjectileAPI proj;
    
    public MS_VampVFXPlugin (@NotNull DamagingProjectileAPI proj) {
        this.proj = proj;
    }
    
    public void advance(float f, CombatEngineAPI engine, WeaponAPI weapon) {
        
    }
}
