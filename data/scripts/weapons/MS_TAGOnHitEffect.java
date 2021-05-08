package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;

public class MS_TAGOnHitEffect implements OnHitEffectPlugin {
    
    @Override
    public void onHit (DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI apply, CombatEngineAPI engine) {
        ShipAPI source = projectile.getSource();
        
        if (target instanceof ShipAPI && !shieldHit) {
            ShipAPI ship = (ShipAPI) target;
            
            if (!ship.isAlive() || ship.isAlly() || ship.getOwner() == source.getOwner()) { } else {
                engine.addPlugin(new MS_TAGSystemEffect(ship));
            }
        }
    }
}
