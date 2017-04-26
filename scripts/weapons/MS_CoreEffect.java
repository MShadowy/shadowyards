package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;

public class MS_CoreEffect implements EveryFrameWeaponEffectPlugin {

    private boolean runOnce=false;
    private boolean off = false;
    private float time = 0f;
    
    //frequency of the pulse: in pulsation per second
    private final float FREQUENCY = 0.25f;
    //amplitude of the pulsation: 0 = always full, 1 = from extinct to full brightness
    private final float AMPLITUDE = 0.25f;
    //amount of flickering: 0 = none, 2 = completely random
    private final float FLICKERING = 0.25f;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(engine.isPaused() || off) return;
        
        if (!runOnce){        
        weapon.getSprite().setAdditiveBlend();
        runOnce=true;
        }
        
        if (weapon.getShip().isAlive()){
            //if the ship is alive, animate the light
            time += amount;
            Color glow = new Color(
                    1f,
                    1f,
                    1f,
                    //alpha pulse with a little random
                    (float) ((Math.cos((Math.random()*FLICKERING)+time*(2*Math.PI*FREQUENCY))*(1-(Math.random()*FLICKERING)))*(AMPLITUDE/2))+(1-(AMPLITUDE/2))
            );        
            weapon.getSprite().setColor(glow);
        } else {
            //shut the light and the script
            weapon.getSprite().setColor(new Color (0,0,0,0));
            off=true;
        }        
    }
}
