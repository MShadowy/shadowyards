package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MS_effectsHook;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.util.vector.Vector2f;

public class MS_TAGSystemEffect implements EveryFrameWeaponEffectPlugin {
    
    private final float damageMalus = 1.5f;
    
    public Map <ShipAPI, Float> TELEMETRY = new HashMap<>();
    private final IntervalUtil interval = new IntervalUtil (2f, 2f);
    private final IntervalUtil anim = new IntervalUtil (0.03f, 0.03f);
    
    /*private boolean runOnce=false, animIsOn=true;
    private ShipAPI theShip;
    private ShipSystemAPI theSystem;
    private AnimationAPI theAnim;
    private float timer=0, animation=0, fade=0, sparkle=0;
    private final float TICK=0.1f, ANIM=0.05f, RANGE=1000;
    private int LENGTH, frame=0;
    
    private final String tagSprite="ms_tagAnim_00";
    private final int tagFrames = 14;
    private final float tagWidth = 256;
    private final float tagHieght = 320;
    
    private void render (SpriteAPI sprite, float width, float height, boolean additive){
        //where the magic happen
        sprite.setAlphaMult(1); 
        sprite.setSize(width, height);
        if (additive){
            sprite.setAdditiveBlend();
        }
        sprite.setAngle(0);
    }*/
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        /*if(!runOnce){
            runOnce=true;
            theShip=weapon.getShip();
            theSystem=theShip.getSystem();
            LENGTH=13 -1;
            theAnim=weapon.getAnimation();
        }*/
        
        if (engine.isPaused()) {
            return;
        }
        
        if (!TELEMETRY.isEmpty()) {
            //malus.advance(amount);
            for (Map.Entry<ShipAPI, Float> entry : TELEMETRY.entrySet()) {
                ShipAPI ship = entry.getKey();
                String id = ship.getFleetMemberId();
                Vector2f loc = ship.getLocation();
                Vector2f vel = ship.getVelocity();
                
                float remaining = entry.getValue() - amount;
                
                if (remaining < 0) {
                    TELEMETRY.clear();
                    
                    ship.getMutableStats().getArmorDamageTakenMult().unmodify(id);
                    ship.getMutableStats().getHullDamageTakenMult().unmodify(id);
                    ship.getMutableStats().getShieldDamageTakenMult().unmodify(id);
                    
                    //animIsOn=false;
                } else {
                    TELEMETRY.put(ship, remaining);
                    //ping Interval
                    interval.advance(amount);
                    if (interval.intervalElapsed()) {
                        for (int i = 0; i < 1; i++) {
                            MS_effectsHook.createPing(loc, vel);
                        }
                    }
                    //render the TAG loop
                    /*animIsOn = true;
                    
                    if (animIsOn || fade>0) {
                        animation+=amount;
                        if(animation>ANIM) {
                            animation-=ANIM;
                            
                            frame++;
                            if(frame>LENGTH){
                                frame=1;
                            }
                            //theAnim.setFrame(frame);
                            render(
                                    Global.getSettings().getSprite("tagAnim", tagSprite+(int)frame),
                                    tagWidth,
                                    tagHieght,
                                    true
                            );
                            
                            if(animIsOn){
                                fade = Math.min(fade+0.02f, 1);
                            } else {
                                fade = Math.max(fade-0.02f,0);
                            }
                            //theAnim.setAlphaMult(fade);
                        }
                    }*/
                    
                    ship.getMutableStats().getArmorDamageTakenMult().modifyMult(id, damageMalus);
                    ship.getMutableStats().getHullDamageTakenMult().modifyMult(id, damageMalus);
                    ship.getMutableStats().getShieldDamageTakenMult().modifyMult(id, damageMalus);
                }
            }
        }
    }
    
    public void putTELEMETRY (ShipAPI ship, float amount) {
        TELEMETRY.put(ship, 20f);
    }
}
