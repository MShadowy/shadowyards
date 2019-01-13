package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.Misc;
import data.scripts.ShadowyardsModPlugin;
import db.twiglib.TwigUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;


public class MS_Utils {
    
    static final float SAFE_DISTANCE = 600f;
    static final float DEFAULT_DAMAGE_WINDOW = 3f;
    
    public static float estimateIncomingDamage(ShipAPI ship) {
        return estimateIncomingDamage(ship, DEFAULT_DAMAGE_WINDOW);
    }
    public static float estimateIncomingDamage(ShipAPI ship, float damageWindowSeconds) {
        float accumulator = 0f;

        accumulator += estimateIncomingBeamDamage(ship, damageWindowSeconds);

        for (DamagingProjectileAPI proj : Global.getCombatEngine().getProjectiles()) {

            if(proj.getOwner() == ship.getOwner()) continue; // Ignore friendly projectiles

            Vector2f endPoint = new Vector2f(proj.getVelocity());
            endPoint.scale(damageWindowSeconds);
            Vector2f.add(endPoint, proj.getLocation(), endPoint);

            if((ship.getShield() != null && ship.getShield().isWithinArc(proj.getLocation()))
                    || !CollisionUtils.getCollides(proj.getLocation(), endPoint,
                        new Vector2f(ship.getLocation()), ship.getCollisionRadius()))
                continue;

            accumulator += proj.getDamageAmount() + proj.getEmpAmount();// * Math.max(0, Math.min(1, Math.pow(1 - MathUtils.getDistance(proj, ship) / safeDistance, 2)));
        }

        return accumulator;
    }
    public static float estimateIncomingBeamDamage(ShipAPI ship, float damageWindowSeconds) {
        float accumulator = 0f;

        for (BeamAPI beam : Global.getCombatEngine().getBeams()) {
            if(beam.getDamageTarget() != ship) continue;
            
            float dps = beam.getWeapon().getDerivedStats().getDamageOver30Sec() / 30;
            float emp = beam.getWeapon().getDerivedStats().getEmpPerSecond();

            accumulator += (dps + emp) * damageWindowSeconds;
        }

        return accumulator;
    }
    public static float estimateIncomingMissileDamage(ShipAPI ship) {
        float accumulator = 0f;
        DamagingProjectileAPI missile;

        for (Iterator iter = Global.getCombatEngine().getMissiles().iterator(); iter.hasNext();) {
            missile = (DamagingProjectileAPI) iter.next();

            if(missile.getOwner() == ship.getOwner()) continue; // Ignore friendly missiles

            float safeDistance = SAFE_DISTANCE + ship.getCollisionRadius();
            float threat = missile.getDamageAmount() + missile.getEmpAmount();

            if(ship.getShield() != null && ship.getShield().isWithinArc(missile.getLocation()))
                continue;

            accumulator += threat * Math.max(0, Math.min(1, Math.pow(1 - MathUtils.getDistance(missile, ship) / safeDistance, 2)));
        }

        return accumulator;
    }
    public static float getActualDistance(Vector2f from, CombatEntityAPI target, boolean considerShield)
    {
        if (considerShield && (target instanceof ShipAPI))
        {
            ShipAPI ship = (ShipAPI) target;
            ShieldAPI shield = ship.getShield();
            if (shield != null && shield.isOn() && shield.isWithinArc(from))
            {
                return MathUtils.getDistance(from, shield.getLocation()) - shield.getRadius();
            }
        }
        return MathUtils.getDistance(from, target.getLocation()) - Misc.getTargetingRadius(from, target, false);
    }
    public static List<ShipAPI> getSortedAreaList(Vector2f loc, List<ShipAPI> list)
    {
        List<ShipAPI> out;
        if (ShadowyardsModPlugin.hasTwigLib)
        {
            List<ShipAPI> temp = new ArrayList<>(list);
            Collections.sort(temp, new SortShipsByDistance(loc));

            out = new ArrayList<>(list.size());
            while (temp.size() > 0)
            {
                ShipAPI ship = temp.get(0);
                if (TwigUtils.isMultiShip(ship))
                {
                    out.add(ship);
                    TwigUtils.filterConnections(ship, temp);
                }
                else
                {
                    out.add(ship);
                    temp.remove(0);
                }
            }
        }
        else
        {
            out = new ArrayList<>(list);
            Collections.sort(out, new SortShipsByDistance(loc));
        }
        return out;
    }
    private static class SortShipsByDistance implements Comparator<ShipAPI>
    {
        private final Vector2f loc;

        SortShipsByDistance(Vector2f loc)
        {
            this.loc = loc;
        }

        @Override
        public int compare(ShipAPI s1, ShipAPI s2)
        {
            float dist1;
            if (s1.getShield() != null && s1.getShield().isOn() && s1.getShield().isWithinArc(loc))
            {
                dist1 = MathUtils.getDistance(s1.getLocation(), loc) - s1.getShield().getRadius();
                dist1 *= dist1;
            }
            else
            {
                dist1 = MathUtils.getDistanceSquared(s1.getLocation(), loc);
            }
            float dist2;
            if (s2.getShield() != null && s2.getShield().isOn() && s2.getShield().isWithinArc(loc))
            {
                dist2 = MathUtils.getDistance(s2.getLocation(), loc) - s2.getShield().getRadius();
                dist2 *= dist1;
            }
            else
            {
                dist2 = MathUtils.getDistanceSquared(s2.getLocation(), loc);
            }
            return Float.compare(dist1, dist2);
        }
    }
    
    public static boolean isWithinEmpRange(Vector2f loc, float dist, ShipAPI ship)
    {
        float distSq = dist * dist;
        if (ship.getShield() != null && ship.getShield().isOn() && ship.getShield().isWithinArc(loc))
        {
            if (MathUtils.getDistance(ship.getLocation(), loc) - ship.getShield().getRadius() <= dist)
            {
                return true;
            }
        }

        for (WeaponAPI weapon : ship.getAllWeapons())
        {
            if (!weapon.getSlot().isHidden() && weapon.getSlot().getWeaponType() != WeaponType.DECORATIVE && weapon.getSlot().getWeaponType()
                    != WeaponType.LAUNCH_BAY
                    && weapon.getSlot().getWeaponType() != WeaponType.SYSTEM)
            {
                if (MathUtils.getDistanceSquared(weapon.getLocation(), loc) <= distSq)
                {
                    return true;
                }
            }
        }

        for (ShipEngineAPI engine : ship.getEngineController().getShipEngines())
        {
            if (!engine.isSystemActivated())
            {
                if (MathUtils.getDistanceSquared(engine.getLocation(), loc) <= distSq)
                {
                    return true;
                }
            }
        }

        return false;
    }
}
