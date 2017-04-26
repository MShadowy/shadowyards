//by Tartiflette, this script allow for to create fake vector thrusters with decorative weapons.
//feel free to use it, credit is appreciated but not mandatory
//Modified by MShadowy with permission
package data.scripts.weapons;
 
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
 
public class MS_thrustPlugin implements EveryFrameWeaponEffectPlugin {
   
    private float target = 0.0f;
    private boolean runOnce = false;
    //private static float arc = 0.0f;
    //private static float arcFacing = 0.0f;
    private static ShipAPI ship;
    private static ShipEngineControllerAPI engines;
    private static ShipEngineAPI bindEngine;
    private static ShipSystemAPI shipSystem;
    private static int shipSystemAccDir = 0;
    private static int shipSystemAcc = 0;
    private float checkDrive = 5f * 5f;
    private final float tinX = 6;
    private final float smallX = 10;
    private final float midX = 18;
    private final float bigX = 26;
    private float throttle = 100f;
    private float lengthMult;
    private final float widthMult = 1.25f;
    float maxOutput;
    
    float aim;
    
    private final Color NoColor = new Color(0, 0, 0, 0);
    private Color NormalColor = new Color(255, 185, 155, 255);
    
    private static final Map<ShipAPI.HullSize, Float> tinY = new HashMap<>();

    static {
        tinY.put(ShipAPI.HullSize.FIGHTER, 15f);
        tinY.put(ShipAPI.HullSize.FRIGATE, 25f);
        tinY.put(ShipAPI.HullSize.DESTROYER, 35f);
        tinY.put(ShipAPI.HullSize.CRUISER, 55f);
        tinY.put(ShipAPI.HullSize.CAPITAL_SHIP, 75f);
    }

    private static final Map<ShipAPI.HullSize, Float> smallY = new HashMap<>();

    static {
        smallY.put(ShipAPI.HullSize.FIGHTER, 50f);
        smallY.put(ShipAPI.HullSize.FRIGATE, 60f);
        smallY.put(ShipAPI.HullSize.DESTROYER, 80f);
        smallY.put(ShipAPI.HullSize.CRUISER, 90f);
        smallY.put(ShipAPI.HullSize.CAPITAL_SHIP, 100f);
    }

    private static final Map<ShipAPI.HullSize, Float> midY = new HashMap<>();

    static {
        midY.put(ShipAPI.HullSize.FIGHTER, 0f);
        midY.put(ShipAPI.HullSize.FRIGATE, 80f);
        midY.put(ShipAPI.HullSize.DESTROYER, 110f);
        midY.put(ShipAPI.HullSize.CRUISER, 140f);
        midY.put(ShipAPI.HullSize.CAPITAL_SHIP, 180f);
    }

    private static final Map<ShipAPI.HullSize, Float> bigY = new HashMap<>();

    static {
        bigY.put(ShipAPI.HullSize.FIGHTER, 0f);
        bigY.put(ShipAPI.HullSize.FRIGATE, 100f);
        bigY.put(ShipAPI.HullSize.DESTROYER, 140f);
        bigY.put(ShipAPI.HullSize.CRUISER, 180f);
        bigY.put(ShipAPI.HullSize.CAPITAL_SHIP, 260f);
    }
    
    private static final Set<String> accShipSystems = new HashSet<>();

    static {
        accShipSystems.add("ms_RRSDrive");
    }

    private static final Set<String> accBackShipSystems = new HashSet<>();

    static {
        accBackShipSystems.add("ms_woopDrive");
    }
    
    private final Map<Integer, SpriteAPI> THRUST = new HashMap<>();
    {
        THRUST.put(1, Global.getSettings().getSprite("thrusts", "SRA_Vectored_Thrust01"));
        THRUST.put(2, Global.getSettings().getSprite("thrusts", "SRA_Vectored_Thrust02"));
        THRUST.put(3, Global.getSettings().getSprite("thrusts", "SRA_Vectored_Thrust03"));
    }
    
    /*
    Thruster behavior is defined from the Weapon Slot name in a selected ships hull file.
    
    Vectoring thrusters are broken into two components, an optional CAP which puts a
    rotating cover in place and the not optional THRUST which emits the flames.
    These are also separated by size: Tiny, Small, Medium and Large (T, S, M, and L)
    which are placed as prefixes to THRUST and CAP--e.g LTHRUST or MCAP, etc
    
    Following the THRUST or CAP distinctions come behaviors and modifiers.
    Behaviors are _MAIN, _RETRO, and _MR.
    
    MAIN: focused on pushing the ship forward, and depending on position turning in certain
    directions/conditions, assumed to be to the aft
    RETRO: very similar to main, these thrusters handle deceleration and reverse movement,
    conversely assumed to be on the front end of a ship
    MR: These are a hybrid position which perform the functions of both MAIN and RETRO thruster types.
    
    These are modified by positional notes--_CL(Centerline), _PORT, and _STBD for _MAIN
    and _RETRO. _MR thrusters are always presumed to be either on port ot starboard
    sides, notation being either _P or _S for side, followed by F, or A for Fore or 
    Aft respectively.
    
    For example "THRUST_MR_PF" is a Portside Forward located hybrid thruster, while "THRUST_MAIN_CL"
    represents a thruster meant to push the ship forward, located at or very near the center of mass.
    
    EXAMPLE WEAPON SLOT
        {
            "angle": 95,
            "arc": 110,
            "id": "MTHRUST_MR_PA_02",
            "locations": [
               -59.5,
               22
            ],
            "mount": "TURRET",
            "size": "SMALL",
            "type": "DECORATIVE"
        },
    
    Note 1 - Port is left and Starboard is right relative to ship facing, Fore is to
    the Front and Aft is the Back
    Note 2 - In terms of coordinates, Port and Fore are positive; Starboard and Aft
    are negative
    
    -MShadowy
    */
   
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {        
        final SpriteAPI theSprite = weapon.getSprite();
        
        // Don't bother with any checks if the game is paused
        if (engine.isPaused()) {
            return;
        }
       
        if (!runOnce) {
            ship = weapon.getShip();
            engines = ship.getEngineController();
            
            // get nearest engine to weapon before anything
            List<ShipEngineAPI> shipEngines = ship.getEngineController().getShipEngines();
            for (ShipEngineAPI shipEngine : shipEngines) {
                Vector2f shipEngineLocation = shipEngine.getLocation();
                float distanceSq = MathUtils.getDistanceSquared(shipEngineLocation, weapon.getLocation());
                if (distanceSq <= checkDrive) {
                    bindEngine = shipEngine;
                    checkDrive = distanceSq;
                }
            }
            
            
            // check if the ship have listed engine mod ship system
            shipSystem = ship.getSystem();
            if (shipSystem != null) {
                if (accShipSystems.contains(ship.getSystem().getId())) {
                    shipSystemAccDir = 1;
                }
                if (accBackShipSystems.contains(ship.getSystem().getId())) {
                    shipSystemAccDir = -1;
                }
            }
            
            //Get the base maximum speed of the vessel
            if (ship.getVariant().getHullMods().contains("safetyoverrides")){
                NormalColor=new Color(255, 200, 225, 255);
            }
            runOnce=true;
        }
        
        //the maximum increase in throttle per frame
        float maxThrottleDelta = 0.85f;
        
        //the rate at which it returns to idle
        float maxReturnToIdleDelta = 2f;
        
        //the idle throttle
        float idleThrottle = 26.5f;

        // check the ship system
        if (shipSystemAccDir != 0 && shipSystem.isActive()) {
            shipSystemAcc = shipSystemAccDir;
        } else {
            shipSystemAcc = 0;
        }
        if (ship.getTravelDrive().isActive()) {
            shipSystemAcc = 1;
        }
        
        if (bindEngine.isDisabled() || !ship.isAlive()) {
            float angle = weapon.getCurrAngle();

            weapon.setCurrAngle(angle);
            if (weapon.getSlot().getId().contains("THRUST")) {
                theSprite.setColor(NoColor);
            }
        } else if (!bindEngine.isDisabled()) {
            theSprite.setColor(NormalColor);
            float angle = weapon.getCurrAngle();
            float arcFacing = weapon.getArcFacing();
            float arc = weapon.getArc();
            float shipFacing = ship.getFacing();

            if (!engine.isPaused() && weapon.getSlot().getId().contains("THRUST") && Math.random()>0.1f){
                weapon.getAnimation().setFrame(MathUtils.getRandomNumberInRange(1, 3));
            }
            
            //here we define the behavior based on positional modifiers
            //main
            if (weapon.getSlot().getId().contains("MAIN_CL")) {
                //centerline main engines; goes forward and does some left or right -while going forward-
                if (engines.isAccelerating() && (engines.isStrafingLeft() || engines.isTurningRight())
                        && (!engines.isAcceleratingBackwards() || !engines.isDecelerating() || !engines.isStrafingRight()
                        || !engines.isTurningLeft())) {
                    aim = shipFacing + arcFacing + arc * 0.25f;
                    if (weapon.getSlot().getId().contains("THRUST")) {
                        maxOutput = 0.75f;
                        if (throttle < 75f) {
                            throttle += maxThrottleDelta;
                        }
                    }
                } else {
                    if (engines.isAccelerating() && (engines.isStrafingRight() || engines.isTurningLeft())
                            && (!engines.isAcceleratingBackwards() || !engines.isDecelerating() || !engines.isStrafingLeft()
                            || !engines.isTurningRight())) {
                        aim = shipFacing + arcFacing - arc * 0.25f;
                        if (weapon.getSlot().getId().contains("THRUST")) {
                            maxOutput = 0.75f;
                            if (throttle < 75f) {
                                throttle += maxThrottleDelta;
                            }
                        }
                    } else if (engines.isAccelerating() && (!engines.isStrafingRight() || !engines.isStrafingRight()
                            || !engines.isStrafingLeft() || !engines.isTurningLeft() || !engines.isAcceleratingBackwards() 
                            || !engines.isDecelerating()) || shipSystemAcc == 1 ) {
                        //assumed to be aiming at or very close to 180° (within ~15-20°) 
                        aim = shipFacing + arcFacing;
                        if (weapon.getSlot().getId().contains("THRUST")) {
                            maxOutput = 0.9f;
                            if (throttle <= 99f) {
                                throttle += maxThrottleDelta;
                            }
                        }
                    } else //idle
                    {
                        aim = shipFacing + arcFacing;
                        if (weapon.getSlot().getId().contains("THRUST")) {
                            maxOutput = 0.3f;
                            
                            if (throttle >= idleThrottle){
                                throttle -= maxReturnToIdleDelta;
                            }
                        }
                    }
                }
            }
            
            if (weapon.getSlot().getId().contains("MAIN_STBD")) {
                //starboard side main engines; goes forward, helps turn right or strafe left
                //if only turning or strafing
                if (!engines.isAccelerating() && (engines.isStrafingLeft() || engines.isTurningRight())
                        || (engines.isDecelerating() || engines.isAcceleratingBackwards()) && (engines.isStrafingLeft()
                        || engines.isTurningRight()|| engines.isStrafingLeft() && engines.isTurningRight())) {
                    aim = shipFacing - 90f;
                    if (weapon.getSlot().getId().contains("THRUST")) {
                        maxOutput = 0.8f;
                        if (throttle <= 99f) {
                            throttle += maxThrottleDelta;
                        }
                    }
                } else {
                    //if turning/strafing while accelerating
                    if (engines.isAccelerating() && (engines.isStrafingLeft() || engines.isTurningRight())) {
                        aim = shipFacing + arcFacing - arc * 0.25f;
                        if (weapon.getSlot().getId().contains("THRUST")) {
                            maxOutput = 0.5f;
                            if (throttle < 60f) {
                                throttle += maxThrottleDelta;
                            }
                        }
                    } //going straight forward
                    else if (engines.isAccelerating() || shipSystemAcc == 1) {
                        aim = shipFacing + arcFacing - arc * 0.5f;
                        if (weapon.getSlot().getId().contains("THRUST")) {
                            maxOutput = 0.8f;
                            if (throttle <= 99f) {
                                throttle += maxThrottleDelta;
                            }
                        }
                    } else //idle
                    {
                        aim = shipFacing + arcFacing;
                        if (weapon.getSlot().getId().contains("THRUST")) {
                            maxOutput = 0.3f;
                            
                            if (throttle >= idleThrottle){
                                throttle -= maxReturnToIdleDelta;
                            }
                        }
                    }
                }
            }
            
            if (weapon.getSlot().getId().contains("MAIN_PORT")) {
                //port main engines; goes forward, helps turn left or strafe right
                //if only turning or strafing
                if (!engines.isAccelerating() && (engines.isStrafingRight() || engines.isTurningLeft())
                        || (engines.isDecelerating() || engines.isAcceleratingBackwards()) && (engines.isStrafingRight()
                        || engines.isTurningLeft() || engines.isStrafingRight() && engines.isTurningLeft())) {
                    aim = shipFacing + 90f;
                    if (weapon.getSlot().getId().contains("THRUST")) {
                        maxOutput = 0.8f;
                        if (throttle <= 99f) {
                            throttle += maxThrottleDelta;
                        }
                    }
                } else {
                    //if turning/strafing while accelerating
                    if (engines.isAccelerating() && (engines.isStrafingRight() || engines.isTurningLeft())) {
                        aim = shipFacing + arcFacing + arc * 0.25f;
                        if (weapon.getSlot().getId().contains("THRUST")) {
                            maxOutput = 0.5f;
                            if (throttle < 60f) {
                                throttle += maxThrottleDelta;
                            }
                        }
                    } //going straight forward
                    else if (engines.isAccelerating() || shipSystemAcc == 1) {
                        aim = shipFacing + arcFacing + arc * 0.5f;
                        if (weapon.getSlot().getId().contains("THRUST")) {
                            maxOutput = 0.8f;
                            if (throttle <= 99f) {
                                throttle += maxThrottleDelta;
                            }
                        }
                    } else //idle
                    {
                        aim = shipFacing + arcFacing;
                        if (weapon.getSlot().getId().contains("THRUST")) {
                            maxOutput = 0.3f;
                            
                            if (throttle >= idleThrottle){
                                throttle -= maxReturnToIdleDelta;
                            }
                        }
                    }
                }
            }
            
            //retro
            if (weapon.getSlot().getId().contains("RETRO_CL")) {
                //centerline retro engines; reverses thrust and does some left or right -while reversing-
                if ((engines.isAcceleratingBackwards() || engines.isDecelerating()) && (engines.isStrafingLeft() || engines.isTurningRight())
                        && (!engines.isAccelerating() || !engines.isStrafingRight() || !engines.isTurningLeft())) {
                    aim = shipFacing + arcFacing + arc * 0.25f;
                    if (weapon.getSlot().getId().contains("THRUST")) {
                        maxOutput = 0.75f;
                        if (throttle < 75f) {
                            throttle += maxThrottleDelta;
                        }
                    }
                } else {
                    if ((engines.isAcceleratingBackwards() || engines.isDecelerating()) && (engines.isStrafingRight() || engines.isTurningLeft())
                            && (!engines.isAccelerating() || !engines.isStrafingLeft() || !engines.isTurningRight())) {
                        aim = shipFacing + arcFacing - arc * 0.25f;
                        if (weapon.getSlot().getId().contains("THRUST")) {
                            maxOutput = 0.75f;
                            if (throttle < 75f) {
                                throttle += maxThrottleDelta;
                            }
                        }
                    } else if ((engines.isAcceleratingBackwards() || engines.isDecelerating()) 
                            && (!engines.isStrafingRight() || !engines.isStrafingRight() || !engines.isStrafingLeft() || !engines.isTurningLeft() 
                            || !engines.isAccelerating()) || shipSystemAcc == -1 ) {
                        //assumed to be aiming at or very close to 0° (within ~15-20°) {
                        aim = shipFacing + arcFacing;
                        if (weapon.getSlot().getId().contains("THRUST")) {
                            maxOutput = 0.9f;
                            if (throttle <= 99f) {
                                throttle += maxThrottleDelta;
                            }
                        }
                    } else {
                        aim = shipFacing + arcFacing;
                        if (weapon.getSlot().getId().contains("THRUST")) {
                            maxOutput = 0.3f;
                            
                            if (throttle >= idleThrottle){
                                throttle -= maxReturnToIdleDelta;
                            }
                        }
                    }
                }
            }
            
            if (weapon.getSlot().getId().contains("RETRO_STBD")) {
                //starboard side retro engines; reverse thrust, helps turn or strafe left
                //if only turning or strafing
                if ((!engines.isAcceleratingBackwards() || !engines.isDecelerating()) && (engines.isStrafingLeft() 
                        || engines.isTurningLeft()) || engines.isAccelerating() && (engines.isStrafingLeft() 
                        || engines.isTurningLeft() || engines.isStrafingLeft() && engines.isTurningLeft())) {
                    aim = shipFacing - 90f;
                    if (weapon.getSlot().getId().contains("THRUST")) {
                        maxOutput = 0.8f;
                        if (throttle <= 99f) {
                            throttle += maxThrottleDelta;
                        }
                    }
                } else {
                    //if turning/strafing while decelerating
                    if ((engines.isAcceleratingBackwards() || engines.isDecelerating()) 
                            && (engines.isStrafingLeft() || engines.isTurningLeft() || engines.isStrafingLeft()
                            && engines.isTurningLeft())) {
                        aim = shipFacing + arcFacing + arc * 0.25f;
                        if (weapon.getSlot().getId().contains("THRUST")) {
                            maxOutput = 0.5f;
                            if (throttle < 60f) {
                                throttle += maxThrottleDelta;
                            }
                        }
                    } else { //going reverse
                        if ((engines.isAcceleratingBackwards() || engines.isDecelerating())
                                && (!engines.isStrafingLeft() || !engines.isTurningLeft())
                                || shipSystemAcc == -1 ) {
                            aim = shipFacing;
                            if (weapon.getSlot().getId().contains("THRUST")) {
                                maxOutput = 0.8f;
                                if (throttle <= 99f) {
                                    throttle += maxThrottleDelta;
                                }
                            }
                        } else { //idle
                            aim = shipFacing + arcFacing;
                            if (weapon.getSlot().getId().contains("THRUST")) {
                                maxOutput = 0.3f;
                            
                                if (throttle >= idleThrottle){
                                    throttle -= maxReturnToIdleDelta;
                                }
                            }
                        }
                    }
                }
            }
            
            if (weapon.getSlot().getId().contains("RETRO_PORT")) {
                //port side retro engines; reverse thrust, helps turn or strafe right
                //if only turning or strafing
                if ((!engines.isAcceleratingBackwards() || !engines.isDecelerating()) && (engines.isStrafingRight() 
                        || engines.isTurningRight()) || engines.isAccelerating() && (engines.isStrafingRight() 
                        || engines.isTurningRight() || engines.isStrafingRight() && engines.isTurningRight())) {
                    aim = shipFacing + 90f;
                    if (weapon.getSlot().getId().contains("THRUST")) {
                        maxOutput = 0.8f;
                        if (throttle <= 99f) {
                            throttle += maxThrottleDelta;
                        }
                    }
                } else {
                    //if turning/strafing while decelerating
                    if ((engines.isAcceleratingBackwards() || engines.isDecelerating()) 
                            && (engines.isStrafingRight() || engines.isTurningRight() || engines.isStrafingRight()
                            && engines.isTurningRight())) {
                        aim = shipFacing + arcFacing - arc * 0.25f;
                        if (weapon.getSlot().getId().contains("THRUST")) {
                            maxOutput = 0.5f;
                            if (throttle < 60f) {
                                throttle += maxThrottleDelta;
                            }
                        }
                    } else { //going reverse
                        if ((engines.isAcceleratingBackwards() || engines.isDecelerating()) 
                                && (!engines.isStrafingRight()|| !engines.isTurningRight())
                                || shipSystemAcc == -1 ) {
                            aim = shipFacing;
                            if (weapon.getSlot().getId().contains("THRUST")) {
                                maxOutput = 0.8f;
                                if (throttle <= 99f) {
                                    throttle += maxThrottleDelta;
                                }
                            }
                        } else {//idle
                            aim = shipFacing + arcFacing;
                            if (weapon.getSlot().getId().contains("THRUST")) {
                                maxOutput = 0.3f;
                            
                                if (throttle >= idleThrottle){
                                    throttle -= maxReturnToIdleDelta;
                                }
                            }
                        }
                    }
                }
            }
                
            //MR thrusters represent an edge case which can be both a main and retro thruster
            //Have Front and Aft as positions mainly so we get them to do turns/strafing correctly
            if (weapon.getSlot().getId().contains("MR_SF")) {
                //MR thrusters prioritize movement, only devoting fully to turning when doing nothing else
                if ((!engines.isAccelerating() || !engines.isAcceleratingBackwards() || !engines.isDecelerating())
                        && (engines.isStrafingLeft() || engines.isTurningLeft() || engines.isStrafingLeft()
                        && engines.isTurningLeft())) {
                    aim = shipFacing - 90f;
                    if (weapon.getSlot().getId().contains("THRUST")) {
                        maxOutput = 0.8f;
                        if (throttle <= 99f) {
                            throttle += maxThrottleDelta;
                        }
                    }
                } else {
                    if (engines.isAccelerating() && (engines.isStrafingLeft() || engines.isTurningLeft()
                            || engines.isStrafingLeft() && engines.isTurningLeft())) {
                        aim = shipFacing + arcFacing + arc * 0.25f;
                    } else if ((engines.isAcceleratingBackwards()|| engines.isDecelerating())
                            && (engines.isStrafingLeft() || engines.isTurningLeft()
                            || engines.isStrafingLeft() && engines.isTurningLeft())) {
                        aim = shipFacing + arcFacing - arc * 0.25f;
                    } else {
                        if (engines.isAccelerating() && (!engines.isStrafingLeft() || !engines.isTurningLeft()
                                || !engines.isStrafingLeft() && !engines.isTurningLeft())
                                || shipSystemAcc == 1 ) {
                            aim = shipFacing +  arcFacing + arc * 0.5f;
                            if (weapon.getSlot().getId().contains("THRUST")) {
                                maxOutput = 0.8f;
                                if (throttle <= 99f) {
                                    throttle += maxThrottleDelta;
                                }
                            }
                        } else if ((engines.isAcceleratingBackwards() || engines.isDecelerating()) 
                                && (!engines.isStrafingLeft() || !engines.isTurningLeft()
                                || !engines.isStrafingLeft() && !engines.isTurningLeft())
                                || shipSystemAcc == -1 ) {
                            aim = shipFacing;
                            if (weapon.getSlot().getId().contains("THRUST")) {
                                maxOutput = 0.8f;
                                if (throttle <= 99f) {
                                    throttle += maxThrottleDelta;
                                }
                            }
                        } else {
                            aim = shipFacing + arcFacing;
                            if (weapon.getSlot().getId().contains("THRUST")) {
                                maxOutput = 0.3f;
                            
                                if (throttle >= idleThrottle){
                                    throttle -= maxReturnToIdleDelta;
                                }
                            }
                        }
                    }
                }
            }
            
            if (weapon.getSlot().getId().contains("MR_PF")) {
                //MR thrusters prioritize movement, only devoting fully to turning when doing nothing else
                if ((!engines.isAccelerating() || !engines.isAcceleratingBackwards() || !engines.isDecelerating())
                        && (engines.isStrafingRight() || engines.isTurningRight() || engines.isStrafingRight()
                        && engines.isTurningRight())) {
                    aim = shipFacing - 90f;
                    if (weapon.getSlot().getId().contains("THRUST")) {
                        maxOutput = 0.8f;
                        if (throttle <= 99f) {
                            throttle += maxThrottleDelta;
                        }
                    }
                } else {
                    if (engines.isAccelerating() && (engines.isStrafingRight() || engines.isTurningRight()
                            || engines.isStrafingRight() && engines.isTurningRight())) {
                        aim = shipFacing + arcFacing - arc * 0.25f;
                        if (weapon.getSlot().getId().contains("THRUST")) {
                            maxOutput = 0.5f;
                            if (throttle < 60f) {
                                throttle += maxThrottleDelta;
                            }
                        }
                    } else if ((engines.isAcceleratingBackwards()|| engines.isDecelerating())
                            && (engines.isStrafingRight() || engines.isTurningRight()
                            || engines.isStrafingRight() && engines.isTurningRight())) {
                        aim = shipFacing + arcFacing + arc * 0.25f;
                        if (weapon.getSlot().getId().contains("THRUST")) {
                            maxOutput = 0.5f;
                            if (throttle < 60f) {
                                throttle += maxThrottleDelta;
                            }
                        }
                    } else {
                        if (engines.isAccelerating() && (!engines.isStrafingRight() || !engines.isTurningRight()
                                || !engines.isStrafingRight() && !engines.isTurningRight())
                                || shipSystemAcc == 1 ) {
                            aim = shipFacing + arcFacing - arc * 0.5f;
                            if (weapon.getSlot().getId().contains("THRUST")) {
                                maxOutput = 0.8f;
                                if (throttle <= 99f) {
                                    throttle += maxThrottleDelta;
                                }
                            }
                        } else if ((engines.isAcceleratingBackwards() || engines.isDecelerating()) 
                                && (!engines.isStrafingRight() || !engines.isTurningRight()
                                || !engines.isStrafingRight() && !engines.isTurningRight())
                                || shipSystemAcc == -1 ) {
                            aim = shipFacing;
                            if (weapon.getSlot().getId().contains("THRUST")) {
                                maxOutput = 0.8f;
                                if (throttle <= 99f) {
                                    throttle += maxThrottleDelta;
                                }
                            }
                        } else {
                            aim = shipFacing + arcFacing;
                            if (weapon.getSlot().getId().contains("THRUST")) {
                                maxOutput = 0.3f;
                            
                                if (throttle >= idleThrottle){
                                    throttle -= maxReturnToIdleDelta;
                                }
                            }
                        }
                    }
                }
            }
            
            if (weapon.getSlot().getId().contains("MR_SA")) {
                //MR thrusters prioritize movement, only devoting fully to turning when doing nothing else
                if ((!engines.isAccelerating() || !engines.isAcceleratingBackwards() || !engines.isDecelerating())
                        && (engines.isStrafingLeft() || engines.isTurningRight() || engines.isStrafingLeft()
                        && engines.isTurningRight())) {
                    aim = shipFacing - 90f;
                    if (weapon.getSlot().getId().contains("THRUST")) {
                        maxOutput = 0.8f;
                        if (throttle <= 99f) {
                            throttle += maxThrottleDelta;
                        }
                    }
                } else {
                    if (engines.isAccelerating() && (engines.isStrafingLeft() || engines.isTurningRight()
                            || engines.isStrafingLeft() && engines.isTurningRight())) {
                        aim = shipFacing + arcFacing - arc * 0.25f;
                        if (weapon.getSlot().getId().contains("THRUST")) {
                            maxOutput = 0.5f;
                            if (throttle < 60f) {
                                throttle += maxThrottleDelta;
                            }
                        }
                    } else if ((engines.isAcceleratingBackwards()|| engines.isDecelerating())
                            && (engines.isStrafingLeft() || engines.isTurningRight()
                            || engines.isStrafingLeft() && engines.isTurningRight())) {
                        aim = shipFacing + arcFacing + arc * 0.25f;
                        if (weapon.getSlot().getId().contains("THRUST")) {
                            maxOutput = 0.5f;
                            if (throttle < 60f) {
                                throttle += maxThrottleDelta;
                            }
                        }
                    } else {
                        if (engines.isAccelerating() && (!engines.isStrafingLeft() || !engines.isTurningRight()
                                || !engines.isStrafingLeft() && !engines.isTurningRight())
                                || shipSystemAcc == 1 ) {
                            aim = shipFacing + arcFacing - arc * 0.5f;
                            if (weapon.getSlot().getId().contains("THRUST")) {
                                maxOutput = 0.8f;
                                if (throttle <= 99f) {
                                    throttle += maxThrottleDelta;
                                }
                            }
                        } else if ((engines.isAcceleratingBackwards() || engines.isDecelerating()) 
                                && (!engines.isStrafingLeft() || !engines.isTurningRight()
                                || !engines.isStrafingLeft() && !engines.isTurningRight())
                                || shipSystemAcc == -1 ) {
                            aim = shipFacing + arcFacing + arc * 0.5f;
                            if (weapon.getSlot().getId().contains("THRUST")) {
                                maxOutput = 0.8f;
                                if (throttle <= 99f) {
                                    throttle += maxThrottleDelta;
                                }
                            }
                        } else {
                            aim = shipFacing + arcFacing;
                            if (weapon.getSlot().getId().contains("THRUST")) {
                                maxOutput = 0.3f;
                            
                                if (throttle >= idleThrottle){
                                    throttle -= maxReturnToIdleDelta;
                                }
                            }
                        }
                    }
                }
            }
            
            if (weapon.getSlot().getId().contains("MR_PA")) {
                //MR thrusters prioritize movement, only devoting fully to turning when doing nothing else
                if ((!engines.isAccelerating() || !engines.isAcceleratingBackwards() || !engines.isDecelerating())
                        && (engines.isStrafingRight() || engines.isTurningLeft() || engines.isStrafingRight()
                        && engines.isTurningLeft())) {
                    aim = shipFacing + 90f;
                    if (weapon.getSlot().getId().contains("THRUST")) {
                        maxOutput = 0.8f;
                        if (throttle <= 99f) {
                            throttle += maxThrottleDelta;
                        }
                    }
                } else {
                    if (engines.isAccelerating() && (engines.isStrafingRight() || engines.isTurningLeft()
                            || engines.isStrafingRight() && engines.isTurningLeft())) {
                        aim = shipFacing + arcFacing + arc * 0.25f;
                        if (weapon.getSlot().getId().contains("THRUST")) {
                            maxOutput = 0.5f;
                            if (throttle < 60f) {
                                throttle += maxThrottleDelta;
                            }
                        }
                    } else if ((engines.isAcceleratingBackwards()|| engines.isDecelerating())
                            && (engines.isStrafingRight() || engines.isTurningLeft()
                            || engines.isStrafingRight() && engines.isTurningLeft())) {
                        aim = shipFacing + arcFacing - arc * 0.25f;
                        if (weapon.getSlot().getId().contains("THRUST")) {
                            maxOutput = 0.5f;
                            if (throttle < 60f) {
                                throttle += maxThrottleDelta;
                            }
                        }
                    } else {
                        if (engines.isAccelerating() && (!engines.isStrafingRight() || !engines.isTurningLeft()
                                || !engines.isStrafingRight() && !engines.isTurningLeft())
                                || shipSystemAcc == 1 ) {
                            aim = shipFacing + arcFacing + arc * 0.5f;
                            if (weapon.getSlot().getId().contains("THRUST")) {
                                maxOutput = 0.8f;
                                if (throttle <= 99f) {
                                    throttle += maxThrottleDelta;
                                }
                            }
                        } else if ((engines.isAcceleratingBackwards() || engines.isDecelerating()) 
                                && (!engines.isStrafingRight() || !engines.isTurningLeft()
                                || !engines.isStrafingRight() && !engines.isTurningLeft())
                                || shipSystemAcc == -1 ) {
                            aim = shipFacing + arcFacing - arc * 0.5f;
                            if (weapon.getSlot().getId().contains("THRUST")) {
                                maxOutput = 0.8f;
                                if (throttle <= 99f) {
                                    throttle += maxThrottleDelta;
                                }
                            }
                        } else {
                            aim = shipFacing + arcFacing;
                            if (weapon.getSlot().getId().contains("THRUST")) {
                                maxOutput = 0.3f;
                            
                                if (throttle >= idleThrottle){
                                    throttle -= maxReturnToIdleDelta;
                                }
                            }
                        }
                    }
                }
            }
            
            target = angle + MathUtils.getShortestRotation(angle, aim);
            int size;
            if (weapon.getSlot().getId().contains("LCAP") || weapon.getSlot().getId().contains("LTHRUST")) {
                size = 24;
            } else  if (weapon.getSlot().getId().contains("MCAP") || weapon.getSlot().getId().contains("MTHRUST")) {
                size = 12;
            } else  if (weapon.getSlot().getId().contains("SCAP") || weapon.getSlot().getId().contains("STHRUST")) {
                size = 6;
            } else {
                size = 3;
            }

            //TURN TO THE TARGET  
            weapon.setCurrAngle(angle + (amount * size * ((target - angle))));
                
            if (engines.isAccelerating() && throttle >= 99f) {
                throttle = 100f;
            }
            
            if ((!engines.isAccelerating() || !engines.isAcceleratingBackwards() || !engines.isDecelerating()) && throttle <= idleThrottle) {
                throttle = idleThrottle;
            }
            
            float thrust = throttle / 100;
            
            if (ship.getVariant().getHullMods().contains("safetyoverrides")){
                if (shipSystemAcc != 0 || ship.getTravelDrive().isActive()) {
                    lengthMult=4f;
                } else {
                    lengthMult=2.25f;
                }
            } else {
                if (shipSystemAcc != 0 || ship.getTravelDrive().isActive()) {
                    lengthMult=3.25f;
                } else {
                    lengthMult=1.65f;
                }
            }
            
            //set the sprite transparency
            float alphaMult;
            if ((lengthMult - 0.5f) > 1f){
                    alphaMult = 0.85f;
            } else {
                    alphaMult = lengthMult - 0.5f;
            }
        
            float offset = Math.abs(MathUtils.getShortestRotation(weapon.getCurrAngle(), target));
            float randScale = MathUtils.getRandomNumberInRange(0.9f, 1.1f);
            if (weapon.getSlot().getId().contains("TTHRUST")) {
                    weapon.getSprite().setHeight(tinY.get(ship.getHullSize()) * 10 / (offset + 10) * thrust * lengthMult * randScale );
                    weapon.getSprite().setWidth(tinX * 10 / (offset + 10) * maxOutput * widthMult * randScale);
                    weapon.getSprite().setCenter(tinX / 2 * 10 / (offset + 10) * maxOutput * widthMult * randScale, tinY.get(ship.getHullSize()) / 2 * 10 / (offset + 10) * thrust * lengthMult * randScale);
                    weapon.getAnimation().setAlphaMult(alphaMult);
            }
            if (weapon.getSlot().getId().contains("STHRUST")) {
                    weapon.getSprite().setHeight(smallY.get(ship.getHullSize()) * 10 / (offset + 10) * thrust * lengthMult * randScale );
                    weapon.getSprite().setWidth(smallX * 10 / (offset + 10) * maxOutput * widthMult * randScale);
                    weapon.getSprite().setCenter(smallX / 2 * 10 / (offset + 10) * maxOutput * widthMult * randScale, smallY.get(ship.getHullSize()) / 2 * 10 / (offset + 10) * thrust * lengthMult * randScale);
                    weapon.getAnimation().setAlphaMult(alphaMult);
            }
            if (weapon.getSlot().getId().contains("MTHRUST")) {
                    weapon.getSprite().setHeight(midY.get(ship.getHullSize()) * 10 / (offset + 10) * thrust * lengthMult * randScale );
                    weapon.getSprite().setWidth(midX * 10 / (offset + 10) * maxOutput * widthMult * randScale);
                    weapon.getSprite().setCenter(midX / 2 * 10 / (offset + 10) * maxOutput * widthMult * randScale, midY.get(ship.getHullSize()) / 2 * 10 / (offset + 10) * thrust * lengthMult * randScale);
                    weapon.getAnimation().setAlphaMult(alphaMult);
            }
            if (weapon.getSlot().getId().contains("LTHRUST")) {
                    weapon.getSprite().setHeight(bigY.get(ship.getHullSize()) * 10 / (offset + 10) * thrust * lengthMult * randScale);
                    weapon.getSprite().setWidth(bigX * 10 / (offset + 10) * maxOutput * widthMult * randScale);
                    weapon.getSprite().setCenter(bigX / 2 * 10 / (offset + 10) * maxOutput * widthMult * randScale, bigY.get(ship.getHullSize()) / 2 * 10 / (offset + 10) * thrust * lengthMult * randScale);
                    weapon.getAnimation().setAlphaMult(alphaMult);
            }
            
        }
    }
}