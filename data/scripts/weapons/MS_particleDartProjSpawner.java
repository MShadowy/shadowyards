package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.VectorUtils;
//import com.fs.starfarer.api.util.IntervalUtil;
//import java.util.List;
//import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_particleDartProjSpawner implements EveryFrameWeaponEffectPlugin {
    
    //private final IntervalUtil startFire = new IntervalUtil(0.2f, 0.2f);
    
    //protected float timer = 0;
    protected int shot = 0;
    private boolean restart = true;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) return;
        
        /*Vector2f point = new Vector2f(weapon.getLocation());
        Vector2f offset = new Vector2f();
        if (weapon.getSlot().isTurret()) {
            offset = new Vector2f(12.5f, 0f);
        } else if (weapon.getSlot().isHardpoint()) {
            offset = new Vector2f(21.5f, 0f);
        }
        VectorUtils.rotate(offset, weapon.getCurrAngle(), offset);
        Vector2f.add(offset, point, point);*/
        
        //float range = weapon.getRange();
        
        /*Vector2f targetPoint = new Vector2f(weapon.getLocation());
        Vector2f targetOffset = new Vector2f();
        if (weapon.getSlot().isTurret()) {
            targetOffset = new Vector2f(range, 0f);
        } else if (weapon.getSlot().isHardpoint()) {
            targetOffset = new Vector2f(range, 0f);
        }
        VectorUtils.rotate(targetOffset, weapon.getCurrAngle(), targetOffset);
        Vector2f.add(targetOffset, targetPoint, targetPoint);*/
        
        Vector2f targetPoint = new Vector2f();
        BeamAPI beamCheck = null;
        for (BeamAPI b : weapon.getBeams()) {
            targetPoint = b.getTo();
            
            beamCheck = b;
        }
        ShipAPI ship = weapon.getShip();
        
        if (restart) {
            //timer = 0;
            shot = 0;
        }
        
        if (weapon.isFiring() && beamCheck != null) {
            //timer += amount;
            //startFire.advance(timer);
            restart = false;
            if (/*startFire.intervalElapsed() && weapon.getChargeLevel() < 1f && timer < 0.25f*/ beamCheck.didDamageThisFrame() && shot == 0) {
                shot++;
                for (int i = 0; i < 1; i++) {
                    engine.spawnProjectile(ship, weapon, "ms_dartLeader", targetPoint, VectorUtils.getAngle(targetPoint, beamCheck.getDamageTarget().getLocation()), beamCheck.getDamageTarget().getLocation());
                }
            }
        } else if (!restart) {
            restart = true;
        }
    }
}
