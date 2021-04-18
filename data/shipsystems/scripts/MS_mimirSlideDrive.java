package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import static java.lang.Math.PI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

public class MS_mimirSlideDrive extends BaseShipSystemScript {
    
    //Animation stuff for the effects
    
    private AnimationAPI theInvert;
    private AnimationAPI theLight;
    
    private final List <WeaponAPI> theBlack = new ArrayList<>();
    private final IntervalUtil anim = new IntervalUtil (0.03f, 0.03f);
    private int frameL = 0;
    private int maxFrameL = 0;
    private float invert;
    
    //tracking stuff for the system itself
    private static final String DATA_KEY = "ms_RRSDrive";
    
    public static float effectLevel(ShipAPI ship) {
        final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
        if (localData == null) {
            return 0f;
        }

        final Map<ShipAPI, Float> acting = localData.acting;

        if (acting.containsKey(ship)) {
            return acting.get(ship);
        } else {
            return 0f;
        }
    }
    //booleans for various states
    private boolean runOnce = false;
    private boolean started = false;
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }
        
        if (!engine.getCustomData().containsKey(DATA_KEY)) {
            engine.getCustomData().put(DATA_KEY, new LocalData());
        }
        
        ShipAPI ship = (ShipAPI) stats.getEntity();
        List<WeaponAPI> allWeapons = ship.getAllWeapons();
        if (!runOnce) {
            runOnce=true;
            theBlack.clear();
            for ( WeaponAPI w : allWeapons ) {
                switch(w.getSlot().getId()) {
                    case "INVERT":
                        w.getAnimation().setFrame(1);
                        theInvert = w.getAnimation();
                        theInvert.setAlphaMult(0);
                        break;
                    case "LIGHTER":
                        theLight = w.getAnimation();
                        maxFrameL = theLight.getNumFrames();
                        break;
                }
            }
            return;
        }
                
        float amount = engine.getElapsedInLastFrame();
        
        if (engine.isPaused()) {
            return;
        }
        
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final Map<ShipAPI, Float> acting = localData.acting;
        
        if (ship.isAlive() && ship.getVelocity() != null) {
            if (effectLevel > 0f) {
                acting.put(ship, effectLevel);
                
                anim.advance(amount);
                
                if (anim.intervalElapsed() && state != State.OUT) {
                    frameL = MathUtils.getRandomNumberInRange(0, maxFrameL-1);
                }
                
                invert = MathUtils.getRandomNumberInRange(0.9f, 1f);
                if (state == State.OUT) {
                    invert = Math.max(invert -(float)Math.cos(PI * effectLevel), 0);
                    frameL = 0;
                }
                
                theInvert.setAlphaMult(invert);
                theInvert.setFrame(1);
                theLight.setFrame(frameL);
            }
            
            switch (state) {
                case IN:
                    {
                        if (!started) {
                            started = true;
                        }
                        float speed = ship.getVelocity().length();
                        if (speed <= 0.1f) {
                            ship.getVelocity().set(VectorUtils.getDirectionalVector(ship.getLocation(), ship.getVelocity()));
                        }
                        if (speed < 900f) {
                            ship.getVelocity().normalise();
                            ship.getVelocity().scale(speed + amount * 3600f);
                        }
                        break;
                    }
                case ACTIVE:
                    {
                        float speed = ship.getVelocity().length();
                        if (speed <= 0.1f) {
                            ship.getVelocity().set(VectorUtils.getDirectionalVector(ship.getLocation(), ship.getVelocity()));
                        }
                        if (speed < 900f) {
                            ship.getVelocity().normalise();
                            ship.getVelocity().scale(speed + amount * 3600f);
                        }
                        stats.getArmorDamageTakenMult().modifyPercent(id, 0.5f);
                        stats.getHullDamageTakenMult().modifyPercent(id, 0.5f);
                        break;
                    }
                default:
                    {
                        float speed = ship.getVelocity().length();
                        if (speed > ship.getMutableStats().getMaxSpeed().getModifiedValue()) {
                            ship.getVelocity().normalise();
                            ship.getVelocity().scale(speed - amount * 3600f);
                        }
                        break;
                    }
            }
        }
    }
    
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
	ShipAPI ship = (ShipAPI) stats.getEntity();
        started = false;
        //inversion = false;
        
        if (ship != null) {
            if (!Global.getCombatEngine().getCustomData().containsKey(DATA_KEY)) {
                Global.getCombatEngine().getCustomData().put(DATA_KEY, new LocalData());
            }
            final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
            if (localData != null) {
                final Map<ShipAPI, Float> acting = localData.acting;

                acting.remove(ship);
            }
        }
        
        stats.getArmorDamageTakenMult().unmodify();
        stats.getHullDamageTakenMult().unmodify();
    }
	
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
	if (index == 0) {
            return new StatusData("vector locked", false);
	}
        if (index == 1) {            
            return new StatusData("armor plates locked", false);
        }
	return null;
    }

    private static final class LocalData {

        final Map<ShipAPI, Float> acting = new HashMap<>(50);
    }
}
