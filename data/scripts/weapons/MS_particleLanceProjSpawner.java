
package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;


public class MS_particleLanceProjSpawner implements EveryFrameWeaponEffectPlugin {
    
    protected int shot = 0;
    private boolean restart = true;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) return;
        
        Vector2f targetPoint = new Vector2f();
        BeamAPI beamCheck = null;
        for (BeamAPI b : weapon.getBeams()) {
            targetPoint = b.getTo();
            
            beamCheck = b;
        }
        
        ShipAPI ship = weapon.getShip();
        
        if (restart) {
            shot = 0;
        }
        
        if (weapon.isFiring() && beamCheck != null) {
            restart = false;
            if (beamCheck.didDamageThisFrame()  && shot == 0) {
                shot++;
                for (int i = 0; i < 1; i++) {
                    engine.spawnProjectile(ship, weapon, "ms_lanceLeader", targetPoint, VectorUtils.getAngle(targetPoint, beamCheck.getDamageTarget().getLocation()), beamCheck.getDamageTarget().getLocation());
                }
            }
        } else if (!restart) {
            restart = true;
        }
    }
}
