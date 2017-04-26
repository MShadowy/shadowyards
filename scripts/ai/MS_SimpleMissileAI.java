package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_SimpleMissileAI implements MissileAIPlugin, GuidedMissileAI {
    
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f(0f,0f);
    private boolean overshoot = false;
    private boolean runOnce = false;
    //data
    private final float flightSpeed;
    private final float maxSearchRange = 2000;
    private final float searchCone = 10;
    //wider cone for cancelling the ship target if it is way out the attack cone
    private final float cancellingCone = 15;
    private final static float damping = 0.1f;

    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public MS_SimpleMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
        flightSpeed = missile.getMaxSpeed();
    }
    
    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////
    
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
        
        //cancelling IF: skip the AI if the game is paused, the missile is way off course, engineless or without a target or target is phased out
        if (Global.getCombatEngine().isPaused() || overshoot || missile.isFading() || missile.isFizzling() || target == null) {return;}
       
        //fiding lead point to aim to
        //public static Vector2f getBestInterceptPoint(Vector2f point, float speed,Vector2f targetLoc, Vector2f targetVel)
        lead = AIUtils.getBestInterceptPoint(missile.getLocation(), flightSpeed, target.getLocation(), target.getVelocity());
        
        if(lead == null) {
            return; //just in case to makes sure a corect lead has been calculated
        }
           
        //aimAngle = angle between the missile facing and the lead direction
        float aimAngle = MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(missile.getLocation(), lead));

        //if the missile overshoot the target, just shut the AI
        if (Math.abs(aimAngle) > 90) {
            if (aimAngle < 0) {
                overshoot = true;
                return;                
            }
        } else {
            //if the lead is forward, turn the missile toward the lead accelerating
            missile.giveCommand(ShipCommand.ACCELERATE);            
                if (aimAngle < 0) {
                    missile.giveCommand(ShipCommand.TURN_RIGHT);
                } else {
                    missile.giveCommand(ShipCommand.TURN_LEFT);
                }  
        }        
        // Damp angular velocity if the missile aim is getting close to the targeted angle
        if (Math.abs(aimAngle) < Math.abs(missile.getAngularVelocity()) * damping)
        {
            missile.setAngularVelocity(aimAngle / damping);
        }
    }
    
    //////////////////////
    //    TARGETTING    //
    //////////////////////
    
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
                if (MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(missile.getLocation(), tmp.getLocation())) > searchCone)
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
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        this.target = target;
    }
    
    public void init(CombatEngineAPI engine) {}
}
