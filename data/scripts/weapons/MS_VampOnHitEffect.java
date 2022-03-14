package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import data.scripts.plugins.MS_VampDrainPlugin;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.vector.Vector2f;

public class MS_VampOnHitEffect implements OnHitEffectPlugin {
    private DamagingProjectileAPI proj;
    
    public MS_VampOnHitEffect (@NotNull DamagingProjectileAPI proj) {
        this.proj = proj;
    }
    
    @Override
    public void onHit(DamagingProjectileAPI proj, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI apply, CombatEngineAPI engine) {
        ShipAPI source = proj.getSource();
        
        if (target instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) target;
            
            if (!ship.isAlive() || ship.isAlly() || ship.getOwner() == source.getOwner()) { } else {
                //engine.addPlugin(new MS_VampDrainPlugin(ship));
            }
        }
    }
}
