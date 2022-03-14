package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.plugins.MS_VampDrainPlugin;
import data.scripts.plugins.MS_VampVFXPlugin;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.lazywizard.lazylib.combat.CombatUtils;

public class MS_VampMaster implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {
    final Set<ShipAPI> effectedShips = new LinkedHashSet<>(100);
    
    private static final String DATA_KEY_PREFIX = "MS_Vamp_";
    private static final String EFFECT_KEY_PREFIX = "MS_Drained_";
    
    private final IntervalUtil effectTime = new IntervalUtil(15f, 15f);
    
    @Override
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if ((proj == null) || (weapon == null)) {
            return;
        }
        
        final String DATA_KEY = DATA_KEY_PREFIX + weapon.getShip().getId() + "_" + weapon.getSlot().getId();
        LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        if (localData == null) {
            localData = new LocalData();
            engine.getCustomData().put(DATA_KEY, localData);
        }
        final Set<DamagingProjectileAPI> vProjs = localData.vampireProjectiles;
        
        vProjs.add(proj);
        engine.addPlugin(new MS_VampVFXPlugin(proj));
    }
    
    @Override
    public void advance(float f, CombatEngineAPI engine, WeaponAPI weapon) {
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
        final Set<DamagingProjectileAPI> vProjs = localData.vampireProjectiles;
        
        if (vProjs.isEmpty()) return;
        
        Iterator<DamagingProjectileAPI> iter = vProjs.iterator();
        while (iter.hasNext()) {
            DamagingProjectileAPI proj = iter.next();
            if (proj.isExpired() || !Global.getCombatEngine().isEntityInPlay(proj)) {
                iter.remove();
            }
        }
        
        iter = vProjs.iterator();
        while (iter.hasNext()) {
            DamagingProjectileAPI proj = iter.next();
            String spec = proj.getProjectileSpecId();
            ShipAPI source = weapon.getShip();
            
            if (spec == null) {
                iter.remove();
                continue;
            }
            
            if (proj.didDamage()) {
                CombatEntityAPI target = proj.getDamageTarget();
                
                if (target instanceof ShipAPI) {
                    ShipAPI ship = (ShipAPI) target;
                    
                    if (!ship.isAlive() || ship.isAlly() || ship.getOwner() == source.getOwner()) { } else
                    {
                        engine.addPlugin(new MS_VampDrainPlugin(ship));
                        //engine.addPlugin(new MS_VampBuffPlugin(ship));
                        
                        if (!effectedShips.contains(ship)) effectedShips.add(ship);
                    }
                }
            }
        }
        
        if (!effectedShips.isEmpty())
        {
            //so in here we tally the number of effected ships, apply the drain visual effect to the ships,
            //add the supplementary effects to the weapon, and finally time the effect duration and remove 
            //the plugins/effects as they time out
        }
        
        for (DamagingProjectileAPI proj : CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 400f)) {
            onFire(proj, weapon, engine);
        }
    }
    
    private static final class LocalData {
        final Set<DamagingProjectileAPI> vampireProjectiles = new LinkedHashSet<>(100);
    }
}
