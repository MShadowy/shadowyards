package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;
import data.scripts.ShadowyardsModPlugin;
import data.scripts.hullmods.TEM_LatticeShield;

public class MS_PolarizerOnHitEffect implements OnHitEffectPlugin
{
    // statics
    public static final float empRadius = 250f;
    
    //code
    //@Override
    //public void onHit (DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, CombatEngineAPI engine)
    //{
        // Check if we hit a ship (not its shield)
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, CombatEngineAPI engine) {
        if (point == null) {
            return;
        }
        if (target instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) target;

            if ((shieldHit || ship.getVariant().getHullMods().contains("tem_latticeshield") && ((!ShadowyardsModPlugin.templarsExist || TEM_LatticeShield.shieldLevel(ship) > 0f) || !ship.getVariant().getHullMods().contains("tem_latticeshield")))) {            
            } else {
                engine.addSmoothParticle(point, new Vector2f(), 300f, 1f, 0.75f, new Color(100, 255, 200, 255));
                float emp = projectile.getEmpAmount() * 0.15f;
                float dam = projectile.getDamageAmount() * 0.2f;
                for (int x = 0; x < 6; x++) {
                    engine.spawnEmpArc(projectile.getSource(), point, projectile.getDamageTarget(), projectile.getDamageTarget(), DamageType.ENERGY, dam, emp, 100000f, null, 20f, new Color(100, 255, 200, 255), new Color(200, 255, 255, 255));
                }
                Global.getSoundPlayer().playSound("ms_hemp_shot_impact", 1f, 1f, point, new Vector2f());
            }
        }
    }
}