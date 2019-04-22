package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import java.util.Collections;
import java.util.List;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_SynapticAI implements MissileAIPlugin, GuidedMissileAI {
    private CombatEntityAPI target;
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private Vector2f lead = new Vector2f(0f,0f);
    private boolean runOnce = false;
    
    private final float maxSearchRange = 600;
    private final float cancellingCone = 60;
    private final float launchAngle = 3;
    
    private final static float damping = 0.1f;
    
    public MS_SynapticAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
    }
    
    @Override
    public void advance(float amount) {
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        
        // assign a target only once
        if (!runOnce)
        {
            setTarget(assignTarget(missile));
            runOnce=true;
        }
        
        if (Global.getCombatEngine().isPaused() || missile.isFading() || missile.isFizzling() || target == null) {return;}
        
        
    }
    
    public CombatEntityAPI assignTarget(MissileAPI missile)
    {
        ShipAPI source = missile.getSource();
        ShipAPI currentTarget = source.getShipTarget();
        
        if (currentTarget != null && 
                !currentTarget.isFighter() && 
                !currentTarget.isDrone() && 
                currentTarget.isAlive() && 
                currentTarget.getOwner()!=missile.getOwner() && 
                //current target is in the attack cone
                MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(missile.getLocation(), currentTarget.getLocation())) < cancellingCone){
            //return the ship's target if it's valid
            return (CombatEntityAPI)currentTarget;
        } else {
            //search for the closest enemy in the cone of attack
            ShipAPI closest = null;
            float distance, closestDistance = Float.MAX_VALUE;
            //grab all nearby enemies
            for (ShipAPI tmp : AIUtils.getNearbyEnemies(missile, maxSearchRange))
            {
                //rule out ships out of the missile attack cone
                if (MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(missile.getLocation(), tmp.getLocation())) > 300)
                {
                    continue;
                }
                //sort closest enemy
                distance = MathUtils.getDistance(tmp, missile.getLocation());  
                if (distance < closestDistance)
                {
                    closest = tmp;
                    closestDistance = distance;
                }
            }
            //return the closest enemy
            return closest;
        }
    }
    
    @Override
    public CombatEntityAPI getTarget(){
        return target;
    }
    
    @Override
    public final void setTarget(CombatEntityAPI target)
    {
        this.target = target;
    }
    
    public void init(CombatEngineAPI engine) {}
}
