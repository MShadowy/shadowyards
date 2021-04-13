package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import data.scripts.ShadowyardsModPlugin;
import data.scripts.hullmods.TEM_LatticeShield;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class MS_REISOnHitEffect implements OnHitEffectPlugin {
    
    private static final Map<ShipAPI.HullSize, Float> EMP = new HashMap<>();
    
    static {
        EMP.put(HullSize.FIGHTER, 125f);
        EMP.put(HullSize.FRIGATE, 225f);
        EMP.put(HullSize.DESTROYER, 450f);
        EMP.put(HullSize.CRUISER, 650f);
        EMP.put(HullSize.CAPITAL_SHIP, 850f);
    }
    
    // Sound to play while piercing a target's armor (should be loopable!)
    private static final String PIERCE_SOUND = "ms_enginekill_impact"; // TEMPORARY
    // Projectile ID (String), pierces shields (boolean)
    private static final Color EFFECT_COLOR = new Color(100, 31, 104, 150);
    private static final Color EFFECT_COLOR_CORE = new Color(193, 78, 186, 255);
    //private static final float ENGINE_KILL_RADIUS_SQUARED = 30f * 30f;
    
    @Override
    public void onHit(DamagingProjectileAPI proj, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI apply, CombatEngineAPI engine) {
        if (point == null) {
            return;
        }
        
        if (target instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) target;
            Vector2f projLocation = proj.getLocation();
            List<ShipEngineControllerAPI.ShipEngineAPI> shipEngines = ship.getEngineController().getShipEngines();
            float emp = 100;
            
            if ((shieldHit || ship.getVariant().getHullMods().contains("tem_latticeshield") && ((!ShadowyardsModPlugin.templarsExist || TEM_LatticeShield.shieldLevel(ship) > 0f) || !ship.getVariant().getHullMods().contains("tem_latticeshield")))) {            
                //in here we set up a curve for dealing with the odds of a REIS hit piercing shields to disable engines
                if (MathUtils.getRandomNumberInRange(0, 1) < (0.15 + ship.getFluxLevel())) {
                    for (ShipEngineControllerAPI.ShipEngineAPI e : shipEngines) {
                        if (e.isDisabled() == false) {
                            Vector2f eLoc = e.getLocation();
                        
                            engine.addSmoothParticle(point, new Vector2f(), 300f, 1f, 0.75f, new Color(100, 255, 200, 255));
                            engine.spawnEmpArc(proj.getSource(), point, null, new SimpleEntity(eLoc), DamageType.ENERGY, 0, 0, 100000f, null, 20f, EFFECT_COLOR, EFFECT_COLOR_CORE);
                            
                            engine.applyDamage(target, eLoc, 0, DamageType.OTHER, EMP.get(ship.getHullSize()), false, false, proj.getSource());
                        }
                    }
                }
            } else {
                for (ShipEngineControllerAPI.ShipEngineAPI e : shipEngines) {
                    if (e.isDisabled() == false) {
                        Vector2f eLoc = e.getLocation();
                        
                        engine.addSmoothParticle(point, new Vector2f(), 300f, 1f, 0.75f, new Color(100, 255, 200, 255));
                        engine.spawnEmpArc(proj.getSource(), point, null, new SimpleEntity(eLoc), DamageType.ENERGY, 0, 0, 100000f, null, 20f, EFFECT_COLOR, EFFECT_COLOR_CORE);
                        
                        engine.applyDamage(target, eLoc, 0, DamageType.OTHER, EMP.get(ship.getHullSize()), false, false, proj.getSource());
                    }
                }
            }
            
            Global.getSoundPlayer().playSound(PIERCE_SOUND, 1f, 1f, projLocation, target.getVelocity());
        }
    }
}
