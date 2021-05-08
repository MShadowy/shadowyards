package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.plugins.MS_PulsePopperPlugin;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_scatterGunSpreader implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {
    private final List<DamagingProjectileAPI> subProjectileRegistry = new ArrayList<>();
    
    private static final String DATA_KEY_PREFIX = "MS_ScatterGun_";
    
    protected boolean shouldSplit = true;
    
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if ((projectile == null) || (weapon == null)) {
            return;
        }
 
        final String DATA_KEY = DATA_KEY_PREFIX + weapon.getShip().getId() + "_" + weapon.getSlot().getId();
        LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        if (localData == null) {
            localData = new LocalData();
            engine.getCustomData().put(DATA_KEY, localData);
        }
        final Set<DamagingProjectileAPI> scatterProjectiles = localData.scatterProjectiles;
 
        scatterProjectiles.add(projectile);
    }
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }
 
        final String DATA_KEY = DATA_KEY_PREFIX + weapon.getShip().getId() + "_" + weapon.getSlot().getId();
        LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        if (localData == null) {
            localData = new LocalData();
            engine.getCustomData().put(DATA_KEY, localData);
        }
        final Set<DamagingProjectileAPI> scatterProjectiles = localData.scatterProjectiles;
        
        if (scatterProjectiles.isEmpty()) return;
        
        Iterator<DamagingProjectileAPI> iter = scatterProjectiles.iterator();
        while (iter.hasNext()) {
            DamagingProjectileAPI proj = iter.next();
            if (proj.isExpired() || !Global.getCombatEngine().isEntityInPlay(proj)) {
                iter.remove();
            }
        }
        
        iter = scatterProjectiles.iterator();
        while (iter.hasNext()) {
            DamagingProjectileAPI proj = iter.next();
            String spec = proj.getProjectileSpecId();
            
            if (spec == null) {
                iter.remove();
                continue;
            }
            
            Vector2f loc = proj.getLocation();
            Vector2f vel = proj.getVelocity();

            String clone = spec + "_clone";  //spec + "_clone" means is, if its got the same name in its name (except the "_clone" part) then it must be that weapon.
            String emp = spec + "_emp";  //same with spec + "_emp".

            int shotCount = (1);
            int empCount = (10);
            //just to make sure we spawn the sub projectiles only one time
            if (shouldSplit) {
                for (int j = 0; j < shotCount; j++) {
                    engine.spawnProjectile(proj.getSource(), proj.getWeapon(), clone, loc, proj.getFacing(), proj.getVelocity());
                }
                for (int k = 0; k < empCount; k++) {
                    Vector2f randomVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(20f, 60f));
                    randomVel.x += vel.x;
                    randomVel.y += vel.y;

                    engine.spawnProjectile(proj.getSource(), proj.getWeapon(), emp, loc, proj.getFacing(), randomVel);
                }

                shouldSplit = false;
            }

            for (DamagingProjectileAPI p : CombatUtils.getProjectilesWithinRange(loc, 400f)) {
                if (!subProjectileRegistry.contains(p) && engine.isEntityInPlay(p) && !p.didDamage() && 
                            (clone.equals(p.getProjectileSpecId()) || emp.equals(p.getProjectileSpecId()))) {
                    engine.addPlugin(new MS_PulsePopperPlugin(p));
                    subProjectileRegistry.add(p);
                }
            }

            if (!subProjectileRegistry.isEmpty() && subProjectileRegistry.size() >= 11) {
                engine.removeEntity(proj);
            }
            
            if (!Global.getCombatEngine().isEntityInPlay(proj)) {
                subProjectileRegistry.clear();
                shouldSplit = true;
            }
        }
    }
    
    private static final class LocalData {
        final Set<DamagingProjectileAPI> scatterProjectiles = new LinkedHashSet<>(100);
    }
}