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



public class MS_Barrago_S1_AI implements MissileAIPlugin, GuidedMissileAI {
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f(0f,0f);
    private boolean runOnce = false;
    //data
    //private final float flightSpeed;
    private final float maxSearchRange = 9999;
    private final float searchCone = 180;
    private final float cancellingCone = 60;
    private final float launchAngle = 3;
    
    private final static float damping = 0.1f;
    
    //Data collection
    public MS_Barrago_S1_AI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
    }
    
    /*Main Loop:
    The Missile AI looks for the ships target, or selects the best target according
    to a set critera (favoring, in order Cruisers, Destroyers, Capital Ships then Frigates 
    sorted by nearest detected)and at set intervals compares the angle difference between 
    missile facing and target location; if the difference is less than the set value, 
    the missile starts the separation sequence, however that works*/
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
        
        //finding lead point to aim to; since this stage won't be doing the acfual intercept we only concern ourselves with the s2 speed
        //public static Vector2f getBestInterceptPoint(Vector2f point, float speed,Vector2f targetLoc, Vector2f targetVel)
        lead = AIUtils.getBestInterceptPoint(missile.getLocation(), 600, target.getLocation(), target.getVelocity());
        
        if(lead == null) {
            return; //just in case to makes sure a corect lead has been calculated
        }
           
        //aimAngle = angle between the missile facing and the lead direction
        float aimAngle = MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(missile.getLocation(), lead));
        
        //if the missile overshoot the target, just shut the AI
        if (Math.abs(aimAngle) > 90) {
            if (aimAngle < 0) {
                 missile.giveCommand(ShipCommand.TURN_RIGHT);
                } else {
                    missile.giveCommand(ShipCommand.TURN_LEFT);
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
        
        //Okay, so here we get to the meat of it
        //If the missile is within 5 degrees of the target when the check goes off we spawn the second stage
        //Then we pass the targeting data to the new missile, spawn the spent first stage
        //And finally crush the first stage
        if (Math.abs(aimAngle) < launchAngle) {
            Vector2f loc = missile.getLocation();
            Vector2f vel = missile.getVelocity();
            for (int i = 0; i < 1; i++) {
                engine.spawnProjectile(missile.getSource(), missile.getWeapon(), "ms_barrago_lrm_s2", loc, missile.getFacing(), vel);
                
                Global.getSoundPlayer().playSound("barrago_stage_two_fire", MathUtils.getRandomNumberInRange(0.9f, 1.1f), 0.5f, missile.getLocation(), vel);
            }
            engine.removeEntity(missile);
        }
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
    public CombatEntityAPI getTarget()
    {
        return target;
    }

    @Override
    public final void setTarget(CombatEntityAPI target)
    {
        this.target = target;
    }
    
    public void init(CombatEngineAPI engine) {}
}