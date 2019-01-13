package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.util.vector.Vector2f;

public class MS_TAGOnHitEffect implements OnHitEffectPlugin {
    
    static Map <ShipAPI, Float> affected = new HashMap<>();
    static final Float debuffDuration = 10f;
    
    @Override
    public void onHit (DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, CombatEngineAPI engine) {
        ShipAPI source = projectile.getSource();
        
        if (target instanceof ShipAPI && !shieldHit) {
            ShipAPI ship = (ShipAPI) target;
            String id = ship.getFleetMemberId();
            
            //((MS_TAGSystemEffect) projectile.getWeapon().getEffectPlugin()).putTELEMETRY(ship);
            if (!ship.isAlive() || ship.isAlly() || ship.getOwner() == source.getOwner()) {
                return;
            }
            
            if(affected.containsKey(ship)) {
                ((MS_TAGSystemEffect) projectile.getWeapon().getEffectPlugin()).putTELEMETRY(ship, affected.get(ship) + debuffDuration);
            } else {
                ((MS_TAGSystemEffect) projectile.getWeapon().getEffectPlugin()).putTELEMETRY(ship, debuffDuration);
            }
        }
    }
}
