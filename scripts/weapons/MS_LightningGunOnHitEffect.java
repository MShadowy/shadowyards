package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;

public class MS_LightningGunOnHitEffect implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, CombatEngineAPI engine) {
        Vector2f origin = projectile.getWeapon().getLocation();
        Vector2f slope = new Vector2f();
        slope.x = (point.x - origin.x);
        slope.y = (point.y - origin.y);
        slope.normalise();
        origin.x = origin.x + slope.x * 5f;
        origin.y = origin.y + slope.y * 5f;

        float distance = MathUtils.getDistance(origin, point);
        float range = projectile.getWeapon().getRange() * 2f;
        if (distance > range) {
            point.x = origin.x + (point.x - origin.x) * range / distance;
            point.y = origin.y + (point.y - origin.y) * range / distance;
        }

        int brightness = (int) (255f * Math.max(Math.min((range - distance) / distance, 0f), 1f));

        Global.getSoundPlayer().playSound("disabled_large", (float) Math.random() * 0.2f + 0.9f, 0.4f * Math.max(Math.min((range - distance) / distance, 0f), 1f), point, new Vector2f());

        engine.spawnEmpArc(projectile.getSource(), origin, null, new SimpleEntity(point),
                DamageType.ENERGY,
                0.0f,
                0.0f, // emp 
                100000f, // max range 
                null,
                20f * Math.max(Math.min((range - distance) / distance, 0f), 1f), // thickness
                new Color(125, 155, 115, brightness),
                new Color(165, 215, 145, brightness)
        );
    }
}
