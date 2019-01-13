package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.Collections;
import java.util.List;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;



public class MS_BlackcapAI implements MissileAIPlugin, GuidedMissileAI {
    
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f(0f,0f);
    //data
    private final float flightSpeed;
    private final float maxSearchRange = 1000;
    private final static float damping = 0.1f;
    private IntervalUtil timer = new IntervalUtil(0.1f, 0f);
    
    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public static ShipAPI findBestTarget(MissileAPI missile) {
        ShipAPI source = missile.getSource();
        if (source != null && source.getShipTarget() != null && !source.getShipTarget().isHulk()) {
            return source.getShipTarget();
        }

        return AIUtils.getNearestEnemy(missile);
    }
    
    public MS_BlackcapAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
        flightSpeed = missile.getMaxSpeed();
        
        // Support for 'fire at target by clicking on them' behavior
        List<ShipAPI> directTargets = CombatUtils.getShipsWithinRange(launchingShip.getMouseTarget(), 100f);
        if (!directTargets.isEmpty()) {
            Collections.sort(directTargets, new CollectionUtils.SortEntitiesByDistance(launchingShip.getMouseTarget()));
            for (ShipAPI tmp : directTargets) {
                if (!tmp.isHulk() && tmp.getOwner() != launchingShip.getOwner()) {
                    setTarget(tmp);
                    break;
                }
            }
        }

        // Otherwise, use default Scatter targeting AI
        if (target == null) {
            setTarget(findBestTarget(missile));
        }
    }
    
    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////
    
    @Override
    public void advance (float amount) {
        // Apparently commands still work while fizzling
        if (missile.isFading() || missile.isFizzling()) {
            return;
        }
        
        timer.advance(amount);
        
        // If our current target is lost, assign a new one
        if (target == null // unset
                || (target instanceof ShipAPI && ((ShipAPI) target).isHulk()) // dead
                || (missile.getOwner() == target.getOwner()) // friendly
                || !Global.getCombatEngine().isEntityInPlay(target)) // completely removed
        {
            setTarget(findBestTarget(missile));
            return;
        }
        
        //cancelling IF: skip the AI if the game is paused, the missile is way off course, engineless or without a target or target is phased out
        if (Global.getCombatEngine().isPaused() || missile.isFading() || missile.isFizzling() || target == null) {return;}
        
        //fiding lead point to aim to
        //public static Vector2f getBestInterceptPoint(Vector2f point, float speed,Vector2f targetLoc, Vector2f targetVel)
        lead = AIUtils.getBestInterceptPoint(missile.getLocation(), flightSpeed, target.getLocation(), target.getVelocity());
        
        if(lead == null) {
            return; //just in case to makes sure a corect lead has been calculated
        }
        
        //aimAngle = angle between the missile facing and the lead direction
        float aimAngle = MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(missile.getLocation(), lead));
        
        //if the missile overshoots or is otherwise way off course, screw acceleration and just turn
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
    }
    
    //////////////////////
    //    TARGETTING    //
    //////////////////////
    
    public CombatEntityAPI assignTarget(MissileAPI missile) {
        ShipAPI source = missile.getSource();
        ShipAPI currentTarget = source.getShipTarget();
        
        if (currentTarget != null && currentTarget.isAlive() && currentTarget.getOwner() != missile.getOwner()) {
            return (CombatEntityAPI)currentTarget;
        } else {
            //search for the closest enemy in the cone of attack
            ShipAPI closest = null;
            float distance, closestDistance = Float.MAX_VALUE;
            //grab all nearby enemies
            for (ShipAPI tmp : AIUtils.getNearbyEnemies(missile, maxSearchRange))
            {
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
