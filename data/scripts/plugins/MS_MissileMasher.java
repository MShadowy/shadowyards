package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.CollectionUtils.CollectionFilter;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;


public class MS_MissileMasher extends BaseEveryFrameCombatPlugin  {
    
    private CombatEngineAPI engine;
    
    private static final float SECONDS_TO_LOOK_AHEAD = 1f;
    private final static Set<String> PROJ_IDS = new HashSet();
    static {
        PROJ_IDS.add("ms_wavebeam");
        //PROJ_IDS.add("ms_rhpcblast");
    }
    
    private final CollectionFilter<MissileAPI> filterMisses = new CollectionFilter<MissileAPI>()
    {
        @Override
        public boolean accept(MissileAPI missile)
        {
            // Exclude missiles and our own side's shots
            if (missile instanceof DamagingProjectileAPI)
            {
                return false;
            }

            // Only include shots that are on a collision path with us
            // Also ensure they aren't travelling AWAY from us ;)
            return (CollisionUtils.getCollides(missile.getLocation(), Vector2f.add(missile.getLocation(), (Vector2f) new Vector2f(missile.getVelocity()).scale(
                    SECONDS_TO_LOOK_AHEAD), null), proj.getLocation(), proj.getCollisionRadius())
                    && Math.abs(MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(missile.getLocation(), proj.getLocation()))) <= 90f);
        }
    };
    private DamagingProjectileAPI proj;
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }
        
        for (DamagingProjectileAPI proj : engine.getProjectiles()) {
            String spec = proj.getProjectileSpecId();

            // Check if missiles will get mashed
            if (!PROJ_IDS.contains(spec)) {
                continue;
            }
            
            float missileCrunchRadius = Math.max(proj.getCollisionRadius(), 20f);
            
            List<MissileAPI> missiles = CombatUtils.getMissilesWithinRange(proj.getLocation(), missileCrunchRadius);
            missiles = CollectionUtils.filter(missiles, filterMisses);
            
            for (MissileAPI missile : missiles) {
                Vector2f mLoc = missile.getLocation();
                
                if (!missiles.isEmpty()) {
                    engine.applyDamage(missile, mLoc, 99999.0f, DamageType.ENERGY, 0.0f, true, false, null);
                }
            }
        }
    }
}
