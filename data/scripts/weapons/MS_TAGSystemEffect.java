package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MS_effectsHook;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_TAGSystemEffect extends BaseEveryFrameCombatPlugin {
    private final List<ShipAPI> Register = new ArrayList<>();
    private final float damageMalus = 1.5f;
    private float animTimer = 0;
    private boolean animate;
    private final int FPS = 14;
    
    private final String tagSprite = "ms_tagAnim_";
    protected float frame = 0;
    
    public Map <ShipAPI, Float> TELEMETRY = new HashMap<>();
    private final IntervalUtil pulse = new IntervalUtil (2f, 2f);
    private final IntervalUtil anim = new IntervalUtil (0.03f, 0.03f);
    private final IntervalUtil debuffDuration = new IntervalUtil (15f, 15f);
    
    private final ShipAPI ship;
    
    private CombatEngineAPI engine;
    
    public MS_TAGSystemEffect(@NotNull ShipAPI ship) {
        this.ship = ship;
    }
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }
        
        String id = ship.getFleetMemberId();
        
        if (!debuffDuration.intervalElapsed()) {
            ship.getMutableStats().getArmorDamageTakenMult().modifyMult(id, damageMalus);
            ship.getMutableStats().getHullDamageTakenMult().modifyMult(id, damageMalus);
            ship.getMutableStats().getShieldDamageTakenMult().modifyMult(id, damageMalus);
            
            if (!Register.contains(ship)) {
                Register.add(ship);
            }
        }
        
        if (Register.contains(ship)) {
            debuffDuration.advance(amount);
            
            //in here we do the thing to adjust the range of hostile weapons pointing in this direction
            /*List<ShipAPI> buffees = CombatUtils.getShipsWithinRange(ship.getLocation(), 1000);
            for (ShipAPI s : buffees) {
                if (s.getOwner() == ship.getOwner()) continue;
                
                List<WeaponAPI> weaps = s.getAllWeapons();
                for (WeaponAPI w : weaps) {
                    if (w.getCurrAngle() < VectorUtils.getAngle(w.getLocation(), ship.getLocation())) {
                        
                    }
                }
            }*/
        }
        
        if (debuffDuration.intervalElapsed() || !ship.isAlive() || ship.isHulk() || !Global.getCombatEngine().isEntityInPlay(ship)) {
            ship.getMutableStats().getArmorDamageTakenMult().unmodify(id);
            ship.getMutableStats().getHullDamageTakenMult().unmodify(id);
            ship.getMutableStats().getShieldDamageTakenMult().unmodify(id);
            
            Global.getCombatEngine().removePlugin(this);
            Register.remove(ship);
            frame = 0;
        }
    }

    @Override
    public void renderInWorldCoords(ViewportAPI vapi) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }
        
        if (Register.contains(ship)) {
            Vector2f loc = ship.getLocation();
            Vector2f vel = ship.getVelocity();
            
            float amount = engine.getElapsedInLastFrame();
            
            if (!engine.isPaused()) {
                animTimer += amount;
                if (animTimer >= 1/FPS) {
                    animTimer -= 1/FPS;
                    animate = true;
                } else animate = false;
            }

            pulse.advance(amount);
            if (pulse.intervalElapsed()) {
                for (int i = 0; i < 1; i++) {
                    MS_effectsHook.createPing(loc, vel);
                }
            }
            
            anim.advance(animTimer);
            
            if (!engine.isPaused() && animate) {
                if (anim.intervalElapsed()) {
                    frame += 1;
                }
                
                if (frame > 14) frame = 1;
            }
            
            if (frame >= 1) {
                SpriteAPI sprite = Global.getSettings().getSprite("tagAnim", (tagSprite + (int) frame));
                
                if (!engine.isPaused()) {
                    sprite.setAlphaMult(MathUtils.getRandomNumberInRange(0.9f, 1f));
                } else {
                    float tAlf = sprite.getAlphaMult();
                    sprite.setAlphaMult(tAlf);
                }
                sprite.setSize(256, 320);
                sprite.setAdditiveBlend();
                sprite.renderAtCenter(loc.x, loc.y);
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }
}
