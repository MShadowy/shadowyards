package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import data.scripts.util.CustomMissileTargetingTool;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public final class WindingRocketAI implements MissileAIPlugin, GuidedMissileAI {
    
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    // Our current target (can be null)
    private CombatEntityAPI target;
    
    private Vector2f lead = new Vector2f(), offset=new Vector2f();
    private boolean launch = true, overshoot=false;     
    private float eccm=1.5f, timer=0, check=0.1f;
    //data
    private final float MAX_SPEED, MAX_RANGE, MAX_SEARCH_RANGE = 2000, SEARCH_CONE = 180, DAMPING = 0.1f;   

    public WindingRocketAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
        MAX_SPEED = missile.getMaxSpeed();
        MAX_RANGE = missile.getWeapon().getRange();
        if (missile.getSource().getVariant().getHullMods().contains("eccm")){
            eccm=1;
        }
    }

    @Override
    public void advance(float amount) {
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }

        // Apparently commands still work while fizzling
        if (Global.getCombatEngine().isPaused() || missile.isFading() || missile.isFizzling()) {
            return;
        }

        // This missile should always be accelerating
        missile.giveCommand(ShipCommand.ACCELERATE);

        if (launch){
            launch=false;
            setTarget(CustomMissileTargetingTool.assignTarget(missile,0,false,true,MAX_RANGE,MAX_SEARCH_RANGE,SEARCH_CONE));
            if(target!=null){
                //pick a random point inside the ship
                offset=MathUtils.getRandomPointInCircle(new Vector2f(), target.getCollisionRadius()*eccm/4);
                if(!CollisionUtils.isPointWithinBounds(new Vector2f(offset.x+target.getLocation().x,offset.y+target.getLocation().y), target)){
                    offset=new Vector2f();
                }
            }
            return;
        } else if (target==null || overshoot || target.getCollisionClass()==CollisionClass.NONE){
            return;
        }

        timer+=amount;
        //finding lead point to aim to        
        if(launch || timer>=check){
            launch=false;
            timer -=check;
            check = Math.min(
                    0.5f,
                    Math.max(
                            0.05f,
                            1.5f*MathUtils.getDistanceSquared(missile.getLocation(), target.getLocation())/6000000)
            );
            lead = AIUtils.getBestInterceptPoint(
                    missile.getLocation(),
                    MAX_SPEED*eccm,
                    target.getLocation(),
                    target.getVelocity()
            );
            if (lead == null ) {
                lead = target.getLocation(); 
            }
            
            Vector2f.add(lead, offset, lead);
        }
        
        //best angle for interception        
        float aimAngle = MathUtils.getShortestRotation(
                missile.getFacing(),
                VectorUtils.getAngle(
                        missile.getLocation(),
                        lead
                )
        );
        
        if (aimAngle < 0) {
            missile.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            missile.giveCommand(ShipCommand.TURN_LEFT);
        }  
        
        // Damp angular velocity if the missile aim is getting close to the targeted angle
        if (Math.abs(aimAngle) < Math.abs(missile.getAngularVelocity()) * DAMPING) {
            missile.setAngularVelocity(aimAngle / DAMPING);
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
}
