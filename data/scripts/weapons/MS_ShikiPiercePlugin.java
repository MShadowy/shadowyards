package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import static com.fs.starfarer.api.impl.campaign.skills.RangedSpecialization.MAX_CHANCE_PERCENT;
import static com.fs.starfarer.api.impl.campaign.skills.RangedSpecialization.MAX_RANGE;
import static com.fs.starfarer.api.impl.campaign.skills.RangedSpecialization.MIN_RANGE;
import com.fs.starfarer.api.impl.campaign.skills.RangedSpecialization.RangedSpecDamageDealtMod;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.plugins.MS_APVisualEffectPlugin;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

//import static org.lwjgl.opengl.GL11.GL_ONE;
//import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class MS_ShikiPiercePlugin implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

    private static final String DATA_KEY_PREFIX = "MS_HullPierceWeapon_";
    
    private static final String PROJ_ID = "ms_microLanceBlast";
    private static final int MAX_HITS = 4; // For base 1000 damage per hit

    // Calculate time to cover 50 px
    // Might want to reduce the base distance — your call
    // Doesn't account for stuff that speeds up the projectile
    private static final float BASE_PIERCE_DISTANCE = 50f;
    private static final float BASE_SPEED = 1000f;
    private static final float DAMAGE_COOLDOWN = BASE_PIERCE_DISTANCE / BASE_SPEED;

    // Sound to play while piercing a target's armor (should be loopable!)
    private static final String SHIELD_HIT_SOUND = "hit_shield_heavy_energy";
    private static final String PIERCE_SOUND = "explosion_missile"; // TEMPORARY
    private static final Color COLOR1 = new Color(165, 215, 145, 150);

    //*******************
    // RENDERING & SOUNDS
    //********************

    // Deals all remaining damage
    private void hitShield(MS_MicroLanceShot shot,
                DamagingProjectileAPI proj, CombatEntityAPI entity,
                float speed, float amount) {
        
        float damage = shot.damagePerHit;
        damage *= getRangedSpecDamageMult(proj);
        float empDamage = shot.empPerHit;
        
        Global.getCombatEngine().applyDamage(entity, proj.getLocation(),
                    damage,
                    proj.getDamageType(),
                    empDamage,
                    false, false, proj.getSource());

        // Render the hit
        Global.getCombatEngine().spawnExplosion(proj.getLocation(), entity.getVelocity(), COLOR1, speed * amount, 1f);
        // Play piercing sound (only one sound active per projectile)
        Global.getSoundPlayer().playLoop(SHIELD_HIT_SOUND, proj, 1f, 1f, proj.getLocation(), entity.getVelocity());
    }

    // Deals damage equal to hitsToBreak
    private void hitShieldPierced(MS_MicroLanceShot shot,
                DamagingProjectileAPI proj, CombatEntityAPI entity,
                int hitsToBreak, float speed, float amount) {
        float damage = shot.damagePerHit;
        damage *= getRangedSpecDamageMult(proj);
        float empDamage = shot.empPerHit;

        Global.getCombatEngine().applyDamage(entity, proj.getLocation(),
                    damage,
                    proj.getDamageType(),
                    empDamage,
                    false, false, proj.getSource());

        // Render the hit
        Global.getCombatEngine().spawnExplosion(proj.getLocation(), entity.getVelocity(), COLOR1, speed * amount, 1f);
        // Play piercing sound (only one sound active per projectile)
        Global.getSoundPlayer().playLoop(SHIELD_HIT_SOUND, proj, 1f, 1f, proj.getLocation(), entity.getVelocity());
    }

    // Deals one hit
    private void hitHullPierced(MS_MicroLanceShot shot,
                DamagingProjectileAPI proj, CombatEntityAPI entity,
                float speed, float amount) {
        float damage = shot.damagePerHit;
        damage *= getRangedSpecDamageMult(proj);
        float empDamage = shot.empPerHit;

        // Deal damage first, then check if it counts as a hit
        Global.getCombatEngine().applyDamage(entity, proj.getLocation(),
                    damage,
                    proj.getDamageType(),
                    empDamage,
                    true, false, proj.getSource());

        // Render the hit
        Global.getCombatEngine().spawnExplosion(proj.getLocation(), entity.getVelocity(), COLOR1, speed * amount * 2f, .5f);
        // Play piercing sound (only one sound active per projectile)
        Global.getSoundPlayer().playLoop(PIERCE_SOUND, proj, 1f, 1f, proj.getLocation(), entity.getVelocity());

    }

    // Special case handling of Ranged Specialization skill
    private float getRangedSpecDamageMult(DamagingProjectileAPI proj) {
        ShipAPI source = proj.getSource();
        if (source == null) return 1f;

        if (!source.hasListenerOfClass(RangedSpecDamageDealtMod.class)) {
            return 1f;
        }

        float dist = Misc.getDistance(proj.getLocation(), source.getLocation());
        float f = (dist - MIN_RANGE) / (MAX_RANGE - MIN_RANGE);
        if (f < 0) f = 0;
        if (f > 1) f = 1;

        float chancePercent = (int) Math.round(MAX_CHANCE_PERCENT * f);
        if (chancePercent <= 0) return 1f;

        if ((float) Math.random() < chancePercent * 0.01f) {
            return 2f;
        } else {
            return 1f + (chancePercent * 0.01f);
        }
    }
    
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if ((projectile == null) || (weapon == null)) return;
        
        final String DATA_KEY = DATA_KEY_PREFIX + weapon.getShip().getId() + "_" + weapon.getSlot().getId();
        LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        if (localData == null) {
            localData = new LocalData();
            engine.getCustomData().put(DATA_KEY, localData);
        }
        final Set<DamagingProjectileAPI> hullPierceProjectiles = localData.hullPierceProjectiles;
        
        hullPierceProjectiles.add(projectile);
        engine.addPlugin(new MS_APVisualEffectPlugin(projectile));
    }

    //*********************
    // PROJECTILE HIT LOGIC
    //**********************

    public static class MS_MicroLanceShot {
        final String id;
        ShipAPI source;
        float damagePerHit;
        float empPerHit;
        int hitsLeft;
        boolean expired;

        public MS_MicroLanceShot(DamagingProjectileAPI proj) {
            this.id = Global.getSector().genUID();
            this.source = proj.getSource();
            this.hitsLeft = MAX_HITS;
            this.damagePerHit = proj.getDamageAmount() / MAX_HITS;
            this.empPerHit = proj.getEmpAmount() / MAX_HITS;
            this.expired = false;
        }
    }

    private static final List<MS_MicroLanceShot> SHOTS = new ArrayList<>();
    private static final Map<String, IntervalUtil> COOLDOWNS = new HashMap<>();

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine == null) {
            SHOTS.clear();
            COOLDOWNS.clear();
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
        final Set<DamagingProjectileAPI> hullPierceProjectiles = localData.hullPierceProjectiles;
        
        if (hullPierceProjectiles.isEmpty()) return;
        
        Iterator<DamagingProjectileAPI> iter = hullPierceProjectiles.iterator();
        while (iter.hasNext()) {
            DamagingProjectileAPI proj = iter.next();
            if (proj.isExpired() || !Global.getCombatEngine().isEntityInPlay(proj)) {
                iter.remove();
            }
        }

        List<String> keysToRemove = new ArrayList<>();
        for (String key : COOLDOWNS.keySet()) {
            IntervalUtil cooldown = COOLDOWNS.get(key);
            cooldown.advance(amount);
            if (cooldown.intervalElapsed()) keysToRemove.add(key);
        }
        for (String key: keysToRemove) COOLDOWNS.remove(key);

        iter = hullPierceProjectiles.iterator();
        // Scan all shots on the map for NL projectiles
        PROJECTILES:
        while (iter.hasNext()) {
            DamagingProjectileAPI proj = iter.next();
            String spec = proj.getProjectileSpecId();

            // Is this a NL proj?
            if (spec == null || !spec.equals(PROJ_ID)) {
                continue;
            }

            MS_MicroLanceShot shot = null;
            // See if it is a shot already being tracked
            for (MS_MicroLanceShot s : SHOTS) {
                if (s.source == proj.getSource()) {
                    shot = s;
                    shot.expired = false;
                    break;
                }
            }
            // Else start tracking it
            if (shot == null) {
                shot = new MS_MicroLanceShot(proj);
                SHOTS.add(shot);
                proj.setCollisionClass(CollisionClass.NONE);
            }

            // Skip if out of damage
            if (shot.hitsLeft <= 0) {
                endLanceShot(shot, proj);
                continue;
            }


            // Iterate over missiles
            // Missiles don't count as hits
            List<CombatEntityAPI> missiles = new ArrayList<>();
            missiles.addAll(CombatUtils.getMissilesWithinRange(proj.getLocation(), proj.getCollisionRadius() + 5f));

            for (CombatEntityAPI entity : missiles) {
                if (entity.getCollisionClass() == CollisionClass.NONE) {
                    continue;
                }

                if (isHitCooldownActive(entity, shot)) continue;

                if (CollisionUtils.isPointWithinBounds(proj.getLocation(), entity)) {
                    engine.applyDamage(entity, entity.getLocation(),
                                shot.damagePerHit, DamageType.ENERGY,
                                shot.empPerHit, true, false,
                                proj.getSource());
                }

            }


            // Iterate over fighters
            List<CombatEntityAPI> ships = new ArrayList<>();
            ships.addAll(CombatUtils.getShipsWithinRange(proj.getLocation(), proj.getCollisionRadius() + 5f));
            // Would be nice to sort here so stuff gets hit closest
            // to farthest instead of top-bottom left-right

            // Check for shield hits on fighters
            for (CombatEntityAPI entity : ships) {
                if (entity.getCollisionClass() == CollisionClass.NONE) {
                    continue;
                }

                ShipAPI ship = (ShipAPI) entity;

                if (!ship.isFighter() || ship == shot.source || ship.getOwner() == shot.source.getOwner()) continue;

                if (isHitCooldownActive(ship, shot)) continue;

                if (isShieldHit(entity, proj)) {
                    float speed = proj.getVelocity().length();

                    float shieldFluxLeft = ship.getShield().getFluxPerPointOfDamage()
                                * (ship.getFluxTracker().getMaxFlux()
                                - ship.getFluxTracker().getCurrFlux());

                    // Min 1 hit, remainder is cut off
                    int hitsToBreak = 1 + (int) (shieldFluxLeft / shot.damagePerHit);

                    // Shield absorbs entire hit
                    if (hitsToBreak >= shot.hitsLeft) {
                        hitShield(shot, proj, entity, speed, amount);
                        endLanceShot(shot, proj);
                        continue PROJECTILES;
                    }

                    // Shield is pierced
                    shot.hitsLeft -= hitsToBreak;
                    startHitCooldown(ship, shot);

                    hitShield(shot, proj, entity, speed, amount);
                }

            }

            // Check for hull hits on fighters
            for (CombatEntityAPI entity : ships) {
                if (entity.getCollisionClass() == CollisionClass.NONE) {
                    continue;
                }

                ShipAPI ship = (ShipAPI) entity;

                if (!ship.isFighter() || ship == shot.source || ship.getOwner() == shot.source.getOwner()) continue;

                if (isHitCooldownActive(ship, shot)) continue;

                if (CollisionUtils.isPointWithinBounds(proj.getLocation(), entity)) {
                    // Calculate projectile speed
                    float speed = proj.getVelocity().length();

                    // Deal damage first, then check if it counts as a hit
                    hitHullPierced(shot, proj, entity, speed, amount);

                    startHitCooldown(ship, shot);

                    // Does it count?
                    if (ship.isAlive()) {
                        shot.hitsLeft--;
                    }

                    // Can this lance still deal damage?
                    if (shot.hitsLeft <= 0) {
                        endLanceShot(shot, proj);
                        continue PROJECTILES;
                    }
                }
            }


            // Iterate over ships and wrecks
            // Check for a shield hit
            for (CombatEntityAPI entity : ships) {
                if (entity.getCollisionClass() == CollisionClass.NONE) {
                    continue;
                }

                ShipAPI ship = (ShipAPI) entity;

                if (ship == shot.source) continue;

                if (ship.isFighter()) continue;

                if (isHitCooldownActive(ship, shot)) continue;

                if (isShieldHit(entity, proj)) {
                    float speed = proj.getVelocity().length();

                    float shieldFluxLeft = ship.getShield().getFluxPerPointOfDamage()
                                * (ship.getFluxTracker().getMaxFlux()
                                - ship.getFluxTracker().getCurrFlux());

                    // Min 1 hit, remainder is cut off
                    int hitsToBreak = 1 + (int) (shieldFluxLeft / shot.damagePerHit);

                    // Shield absorbs entire hit
                    if (hitsToBreak >= shot.hitsLeft) {
                        hitShield(shot, proj, entity, speed, amount);
                        endLanceShot(shot, proj);
                        continue PROJECTILES;
                    }

                    // Shield is pierced
                    shot.hitsLeft -= hitsToBreak;
                    startHitCooldown(ship, shot);

                    hitShieldPierced(shot, proj, entity, hitsToBreak, speed, amount);

                    continue PROJECTILES; // Allow only one shield hit
                }
            }

            // Check for a hull hit on a station/ship module
            for (CombatEntityAPI entity : ships) {
                if (entity.getCollisionClass() == CollisionClass.NONE) {
                    continue;
                }

                ShipAPI ship = (ShipAPI) entity;

                if (ship.isFighter()) continue;

                if (!ship.isStationModule()) continue;
                if (ship.getParentStation() == shot.source) continue;

                if (ship == shot.source) continue;

                if (isHitCooldownActive(ship, shot)) continue;

                if (CollisionUtils.isPointWithinBounds(proj.getLocation(), entity)) {
                    // Calculate projectile speed
                    float speed = proj.getVelocity().length();

                    // Dealing damage
                    shot.hitsLeft--;
                    startHitCooldown(ship, shot);
                    startHitCooldown(ship.getParentStation(), shot);

                    hitHullPierced(shot, proj, entity, speed, amount);

                    // Can this lance still deal damage?
                    if (shot.hitsLeft <= 0) {
                        endLanceShot(shot, proj);
                    }

                    continue PROJECTILES;
                }
            }

            // Check for a hull hit on a ship
            for (CombatEntityAPI entity : ships) {
                if (entity.getCollisionClass() == CollisionClass.NONE) {
                    continue;
                }

                ShipAPI ship = (ShipAPI) entity;

                if (ship.isFighter()) continue;

                if (ship.isStationModule()) continue;

                if (ship == shot.source) continue;

                if (isHitCooldownActive(ship, shot)) continue;

                if (CollisionUtils.isPointWithinBounds(proj.getLocation(), entity)) {
                    // Calculate projectile speed
                    float speed = proj.getVelocity().length();

                    // Dealing damage
                    shot.hitsLeft--;
                    startHitCooldown(ship, shot);

                    hitHullPierced(shot, proj, entity, speed, amount);

                    // Can this lance still deal damage?
                    if (shot.hitsLeft <= 0) {
                        endLanceShot(shot, proj);
                    }

                    continue PROJECTILES;
                }
            }

            // Iterate over asteroids
            List<CombatEntityAPI> asteroids = new ArrayList<>();
            asteroids.addAll(CombatUtils.getAsteroidsWithinRange(proj.getLocation(), proj.getCollisionRadius() + 5f));

            for (CombatEntityAPI entity : asteroids) {
                if (entity.getCollisionClass() == CollisionClass.NONE) {
                    continue;
                }

                if (isHitCooldownActive(entity, shot)) continue;

                if (CollisionUtils.isPointWithinBounds(proj.getLocation(), entity)) {
                    // Calculate projectile speed
                    float speed = proj.getVelocity().length();

                    // Dealing damage
                    startHitCooldown(entity, shot);

                    hitHullPierced(shot, proj, entity, speed, amount);

                    if (entity.getHitpoints() > 0) {
                        shot.hitsLeft--;
                        startHitCooldown(entity, shot);
                    }

                    // Can this lance still deal damage?
                    if (shot.hitsLeft <= 0) {
                        endLanceShot(shot, proj);
                        continue PROJECTILES;
                    }
                }

            }
        }


        // Clean up expired shots in SHOTS list
        List<MS_MicroLanceShot> toRemove = new ArrayList<>();
        for (MS_MicroLanceShot shot : SHOTS) {
            if (shot.expired) toRemove.add(shot);

            shot.expired = true; // We assume each shot will expire next time around
        }
        SHOTS.removeAll(toRemove);

    }

    private boolean isShieldHit(CombatEntityAPI entity, DamagingProjectileAPI proj) {
        return entity.getShield() != null && entity.getShield().isOn()
                    && entity.getShield().isWithinArc(proj.getLocation());
    }

    private boolean isHitCooldownActive(CombatEntityAPI entity, MS_MicroLanceShot shot) {
        String entityId = entity.toString();
        if (entity instanceof ShipAPI) entityId = ((ShipAPI) entity).getId();

        String key = entityId + shot.source.getId();

        return COOLDOWNS.containsKey(key);
    }

    private void startHitCooldown(CombatEntityAPI entity, MS_MicroLanceShot shot) {
        String entityId = entity.toString();
        if (entity instanceof ShipAPI) entityId = ((ShipAPI) entity).getId();

        COOLDOWNS.put(entityId + shot.source.getId(),
                    new IntervalUtil(DAMAGE_COOLDOWN, DAMAGE_COOLDOWN));
    }

    private void endLanceShot(MS_MicroLanceShot shot, DamagingProjectileAPI proj) {
        shot.hitsLeft = 0;
        shot.expired = true;
        Global.getCombatEngine().removeEntity(proj);
    }
    
    private static final class LocalData {
        final Set<DamagingProjectileAPI> hullPierceProjectiles = new LinkedHashSet<>(100);
    }
}