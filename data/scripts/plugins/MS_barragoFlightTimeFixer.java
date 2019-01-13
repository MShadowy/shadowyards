package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_barragoFlightTimeFixer extends BaseEveryFrameCombatPlugin {
    private CombatEngineAPI engine;
    private CombatEntityAPI target;
    static private final String SOUNDER = "barrago_stage_two_fire";
    private static final String FAKE = "ms_barrago_lrm_fake";
    private static final String TO_REMOVE = "ms_barrago_jets";
    private static final String REAL = "ms_barrago_lrm_s2";
    private static final String SHATTER = "ms_barrago_lrm_shatter";
    
    private final Vector2f ZERO = new Vector2f(0,0);
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }
        
        List<MissileAPI> missiles = engine.getMissiles();
        int size = missiles.size();
        for (int i = 0; i < size; i++) {
            MissileAPI miss = missiles.get(i);
            String spec = miss.getProjectileSpecId();
            
            if (spec.contains(FAKE)) {
                Vector2f loc = miss.getLocation();
                Vector2f vel = miss.getVelocity();
                target = miss.getDamageTarget();
                
                for (int j = 0; j < 1; j++) {
                    engine.spawnProjectile(miss.getSource(), miss.getWeapon(), REAL, loc, miss.getFacing(), vel);
                    MissileAPI miss2 = missiles.get(i);
                    String spec2 = miss2.getProjectileSpecId();
                    Global.getSoundPlayer().playSound(SOUNDER, MathUtils.getRandomNumberInRange(0.9f, 1.1f), 0.6f, miss.getLocation(), ZERO);
                    
                    if (spec2.contains(REAL) && target == null) {
                        setTarget(target);
                        break;
                    }
                }
                
                engine.removeEntity(miss);
            }
        }
    }
    
    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }

    public final void setTarget(CombatEntityAPI target)
    {
        this.target = target;
    }
}
