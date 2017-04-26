package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_MimirAnimateMainSequence implements EveryFrameWeaponEffectPlugin {
    
    private WeaponAPI theWeapon;
    private WeaponAPI theMuzzle;
    private AnimationAPI theCharge;
    private AnimationAPI theSparkles;
    private AnimationAPI theHeat;
    private AnimationAPI theFlash;
    private ShipAPI ship;
    
    private final List <WeaponAPI> theFlare = new ArrayList<>();
    private final IntervalUtil anim = new IntervalUtil (0.03f, 0.03f);
    private int frameC = 0;
    private int maxFrameC = 0;
    private int frameZ = 0;
    private int maxFrameZ = 0;
    private int frameM = 0;
    private int maxFrameM = 0;
    private float heat;    
    private float charge;
    private float lastCharge=0;
    private float pFacing=0;
    
    private static final float CHARGEDOWN_RATIO=10f;
    private static final float COOLING_RATIO=0.25f;
    
    private boolean runOnce=false;
    private boolean fired=false;
    private boolean firing = false;
    private boolean chargeUp = false;
    private boolean chargeDown = false;
    private boolean reset=true;
    
    @Override
    public void advance (float amount, CombatEngineAPI engine, WeaponAPI weapon) {
                
        if(engine.isPaused()) return;  
        
        if (!runOnce || ship == null) {            
            runOnce=true;
            theFlare.clear();
            ship=weapon.getShip();
            //get the weapon, all the sprites, sizes, and set the frames to the visible ones
            for (WeaponAPI w : ship.getAllWeapons()) {
                switch(w.getSlot().getId()) {
                    case "MAIN":
                        theWeapon = w;
                        break;
                    case "CHARGE":
                        theCharge = w.getAnimation();
                        maxFrameC = theCharge.getNumFrames()-1;
                        break;
                    case "HEAT":
                        w.getAnimation().setFrame(1);
                        theHeat = w.getAnimation();
                        theHeat.setAlphaMult(0);
                        break;
                    case "ZAP":
                        theSparkles = w.getAnimation();
                        maxFrameZ = theSparkles.getNumFrames();
                        break;
                    case "MUZZLE":
                        theMuzzle = w;
                        theFlash = w.getAnimation();
                        maxFrameM = theFlash.getNumFrames();
                        break;
                }
            }
            return;
        }
        
        ///////////////////
        //   ANIMATION   //
        ///////////////////
        
        float newCharge = weapon.getChargeLevel();
        //skip the script if the weapon isn't firing or cooling
        if (newCharge>0 || firing){
                  
            anim.advance(amount);
            
            if (newCharge>lastCharge){
                firing = true;
                chargeUp=true;
                chargeDown=false;
                if (reset){
                    reset=false;
                    charge=1;
                    frameC=0;
                    frameZ=0;
                }
            } else {
                chargeUp=false;
                chargeDown=true;
                reset=true;
            }            
            lastCharge=newCharge;
            
            if(newCharge==0 && heat==0 && charge==0){
                firing=false;
            }
            
            //lightning effect after firing
            if (frameZ!=0 && anim.intervalElapsed()){
                frameZ++;
                if (frameZ==maxFrameZ){
                    frameZ=0;
                }
            }
            //the barrel get hot only if the gun fired
            heat = Math.max(heat-amount*COOLING_RATIO,0);
            //fading of the chargeup light after firing or in case of overload/venting
            if(chargeDown && charge>0){
                charge = Math.min(
                        1,
                        Math.max(
                                0,
                                charge - charge*amount*CHARGEDOWN_RATIO + (((float)Math.random()-0.5f)/20)
                        )
                );
            }
            //animate the charge
            if (chargeUp && frameC!=maxFrameC && anim.intervalElapsed()){
                charge=1;
                frameC++;
            }
            
            //FIRING!
            if (newCharge==1){
                if (theWeapon.getAmmo()==0){
                    fired=true;
                    heat=1;
                    frameZ=1;
                    engine.addSmoothParticle(
                            theWeapon.getLocation(),
                            ship.getVelocity(),
                            MathUtils.getRandomNumberInRange(125, 150),
                            0.25f,
                            1,
                            new Color (50,255,100)
                    );                
                    engine.addHitParticle(
                            theWeapon.getLocation(),
                            ship.getVelocity(),
                            MathUtils.getRandomNumberInRange(50, 75),
                            1f,
                            0.1f,
                            new Color (200,255,200)
                    );
                    
                    //projectile replacement
                    for (DamagingProjectileAPI p : engine.getProjectiles()){
                        if ( p.getWeapon() == weapon ) {
                            pFacing = p.getFacing();
                            engine.removeEntity(p);
                            break;
                        }
                    }
                    Vector2f muzzle_location = new Vector2f (theWeapon.getLocation());
                    Vector2f ship_velocity = new Vector2f(theWeapon.getShip().getVelocity());
                    
                    //SimpleEntity
                    //public CombatEntityAPI spawnProjectile(ShipAPI ship, WeaponAPI weapon, String weaponId, Vector2f point, float angle, Vector2f shipVelocity)
                    engine.spawnProjectile(ship, weapon, "ms_rhpbc_replacement", muzzle_location, pFacing, ship_velocity);
                }
            }
            
            //animate the muzzle
            if((fired || frameM!=0) && anim.intervalElapsed()){               
                fired=false;
                frameM++;
                if (frameM==maxFrameM){
                    frameM=0;
                }                
            }
            
            //apply the animation and heat to the decos
            theCharge.setFrame(frameC);
            theCharge.setAlphaMult(charge);
            theFlash.setFrame(frameM);
            theHeat.setAlphaMult(heat);
            theHeat.setFrame(1);
            theSparkles.setFrame(frameZ);
        }    
    }
}