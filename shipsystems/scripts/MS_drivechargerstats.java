package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_drivechargerstats implements ShipSystemStatsScript {
    
    private Vector2f vel=new Vector2f();
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        stats.getMaxSpeed().modifyFlat(id, 300*effectLevel);
        stats.getMaxTurnRate().modifyMult(id, 10*effectLevel);
        stats.getTurnAcceleration().modifyPercent(id, 2000f * effectLevel);
        stats.getAcceleration().modifyPercent(id, 2000f * effectLevel);
        stats.getDeceleration().modifyPercent(id, 2000f * effectLevel);
        
        if(!Global.getCombatEngine().isPaused()){
            ShipAPI ship = (ShipAPI) stats.getEntity();
            if(vel!=new Vector2f() || effectLevel!=0){
                //acceleration change of the ship
                Vector2f offset=new Vector2f();
                Vector2f.sub(vel, ship.getVelocity(), offset);
 
                //acceleration relative to the ship's orientation
                VectorUtils.rotate(offset, -ship.getFacing()+90, offset);
               
                //tenth forward acceleration
                offset=new Vector2f(0,offset.y*0.1f);
               
                //rotate back to the world's referentiel
                VectorUtils.rotate(offset, ship.getFacing()-90, offset);
                
                //apply the velocity change
                Vector2f.add(ship.getVelocity(),offset,ship.getVelocity());
            }
 
            vel=new Vector2f(ship.getVelocity());
        }
        
    }
    
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("lateral thrusters online", false);
        } else if (index == 1) {
            return new StatusData("releasing stored drive plasma", false);
        }
        return null;
    }
}
