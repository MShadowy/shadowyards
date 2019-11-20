package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import data.scripts.util.MS_effectsHook;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_SynapticAI implements MissileAIPlugin, GuidedMissileAI {
    
    private static final float AREA_DAMAGE = 250f;
    private static final float AREA_EFFECT = 200f;
    private static final float AREA_EFFECT_INNER = 150f;
    private static final Color COLOR_CORE = new Color(255, 155, 155, 255);
    private static final Color COLOR_PARTICLE = new Color(255, 155, 155, 255);
    private static final String EXPLOSION_SOUND = "mtaf_split";
    private static final float MIRV_DISTANCE_LONG = 1200f;
    private static final float MIRV_DISTANCE = 800f;
    private static final float MIRV_PUSH_RADIUS = 250f;
    private static final int MMM_COUNT = 20;
    private final static float DAMPING = 0.1f;
    private static final Vector2f ZERO = new Vector2f();
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private float timeLive = 0f;
    
    private final Map<ShipAPI.HullSize, Float> SIZE_MULT = new HashMap<>();
    {
        SIZE_MULT.put(ShipAPI.HullSize.FIGHTER, 1.5f);
        SIZE_MULT.put(ShipAPI.HullSize.FRIGATE, 1f);
        SIZE_MULT.put(ShipAPI.HullSize.DESTROYER, 0.75f);
        SIZE_MULT.put(ShipAPI.HullSize.CRUISER, 0.33f);
        SIZE_MULT.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.1f);
        SIZE_MULT.put(ShipAPI.HullSize.DEFAULT, 1f);
    }
    
    public MS_SynapticAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
        
        if (launchingShip.getShipTarget() != null && !launchingShip.getShipTarget().isHulk())
        {
            setTarget(launchingShip.getShipTarget());
        }

        if (target == null)
        {
            List<ShipAPI> directTargets = CombatUtils.getShipsWithinRange(launchingShip.getMouseTarget(), 200f);
            if (!directTargets.isEmpty())
            {
                Collections.sort(directTargets, new CollectionUtils.SortEntitiesByDistance(launchingShip.getMouseTarget()));
                int size = directTargets.size();
                for (int i = 0; i < size; i++)
                {
                    ShipAPI tmp = directTargets.get(i);
                    if (!tmp.isHulk() && tmp.getOwner() != launchingShip.getOwner() && !tmp.isDrone() && !tmp.isFighter())
                    {
                        setTarget(tmp);
                        break;
                    }
                }
            }
        }

        if (target == null)
        {
            setTarget(findBestTarget(missile));
        }
    }
    
    /*so the big thing to change in the advance is to have it specifically check 
    the mirv range to see if there are fighters in that range, since the missile 
    is specifically meant to knock down fighters; set up a series of mirv ranges
    at which it will split if a certain number of hostile fighters are in that range
    Long: 1200, 4 fighters
    Mid: 800, 2 fighters
    Short: 400, 1 fighter*/
    @Override 
    public void advance(float amount) {
        if (missile.isFizzling() || missile.isFading()) {
            if (target == null) {
                return;
            }
            float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
            if (distance <= MIRV_DISTANCE_LONG + target.getCollisionRadius() + missile.getCollisionRadius()) {
                mirv(missile);
            }
            return;
        }
        
        timeLive += amount;
        
        if (target == null || (target instanceof ShipAPI && (((ShipAPI) target).isHulk())) || (missile.getOwner() == target.getOwner())
                || !Global.getCombatEngine().isEntityInPlay(target))
        {
            setTarget(findBestTarget(missile));
            if (target == null)
            {
                missile.giveCommand(ShipCommand.ACCELERATE);
                return;
            }
        }
        
        //cancelling IF: skip the AI if the game is paused, the missile is way off course, engineless or without a target or target is phased out
        if (Global.getCombatEngine().isPaused() || missile.isFading() || missile.isFizzling() || target == null) {return;}

        float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
        float guidance = 0.4f;
        if (missile.getSource() != null)
        {
            guidance += Math.min(missile.getSource().getMutableStats().getMissileGuidance().getModifiedValue()
                    - missile.getSource().getMutableStats().getMissileGuidance().getBaseValue(), 1f) * 0.4f;
        }        
        
        Vector2f lead = intercept(missile.getLocation(), missile.getVelocity().length(), target.getLocation(),
                Vector2f.sub(target.getVelocity(), missile.getVelocity(), null));
        
        if(lead == null) {
            Vector2f projection = new Vector2f(target.getVelocity());
            float scalar = distance / (missile.getVelocity().length() + 1f);
            projection.scale(scalar);
            lead = Vector2f.add(target.getLocation(), projection, null);
        }
        Vector2f.sub(lead, target.getLocation(), lead);
        lead.scale(guidance);
        Vector2f.add(lead, target.getLocation(), lead);
        
        /*int F_MAX_RANGE = 0;
        int F_MID_RANGE = 0;
        int F_MIN_RANGE = 0;
        List<ShipAPI> targets = CombatUtils.getShipsWithinRange(missile.getLocation(), MIRV_DISTANCE_LONG);
        
        Iterator<ShipAPI> iter = targets.iterator();
        while(iter.hasNext()) {
            ShipAPI ship = iter.next();
            if (!ship.isFighter() && !ship.isDrone() || ship.getCollisionClass() == CollisionClass.NONE)
            {
                iter.remove();
                continue;
            }
            
            for(ShipAPI tgt : targets) {
                if (!targets.isEmpty()) {
                    int size = targets.size();
                    Collections.sort(targets, new CollectionUtils.SortEntitiesByDistance(missile.getLocation()));
                    for (int i = 0; i < size; i++)
                    {
                        ShipAPI tmp = targets.get(i);
                        if (tmp.isFighter() && tmp.isDrone()) {
                            F_MAX_RANGE += 1;
                            
                            if (MathUtils.getDistance(target, missile) < MIRV_DISTANCE_SHORT) {
                                F_MIN_RANGE += 1;
                            }
                            if (tmp.isDrone() && MathUtils.getDistance(target, missile) < MIRV_DISTANCE) {
                                F_MID_RANGE += 1;
                            }
                        }
                    }
                }
            } 
        }*/
        if (distance <= MIRV_DISTANCE + target.getCollisionRadius() + missile.getCollisionRadius() && timeLive >= 2f)
        {
            timeLive = -99999f;
            mirv(missile);
        }
        
        
        
        float angularDistance = MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(missile.getLocation(), lead));
        
        missile.giveCommand(angularDistance < 0 ? ShipCommand.TURN_RIGHT : ShipCommand.TURN_LEFT);

        if (Math.abs(angularDistance) < 90) {
            missile.giveCommand(ShipCommand.ACCELERATE);
        }
        
        // Damp angular velocity if the missile aim is getting close to the targeted angle
        if (Math.abs(angularDistance) < Math.abs(missile.getAngularVelocity()) * DAMPING)
        {
            missile.setAngularVelocity(angularDistance / DAMPING);
        }
    }

    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        this.target = target;
    }
    
    private void getPush(CombatEntityAPI target, Vector2f direction, float force) {
        if (VectorUtils.isZeroVector(direction)) {
            return;
        }
        
        force *= 100;
        
        float mass = Math.max(1f, target.getCollisionRadius());
        float velChange = Math.min(1250f, force / mass);
        Vector2f dir = new Vector2f();
        direction.normalise(dir);
        dir.scale(velChange);
        
        Vector2f.add(dir, target.getVelocity(), target.getVelocity());
    }
    
    private void push(CombatEntityAPI target, float dir, float forceMultiplier)
    {
        getPush(target, MathUtils.getPointOnCircumference(new Vector2f(0,0), 1f, dir), forceMultiplier);
        /*Vector2f pushVec = VectorUtils.getDirectionalVector(sourceloc, target.getLocation());
        float magnitude = (1 / Math.max(1f, target.getMass())) * forceMultiplier;
        pushVec.x *= magnitude;
        pushVec.y *= magnitude;
        Vector2f.add(pushVec, target.getVelocity(), target.getVelocity());*/
    }
    
    private void mirv(MissileAPI missile)
    {
        Global.getSoundPlayer().playSound(EXPLOSION_SOUND, 1f, 1f, missile.getLocation(), ZERO);
        Global.getCombatEngine().addHitParticle(missile.getLocation(), ZERO, AREA_EFFECT * 1.5f, 1f, 1f, COLOR_CORE);
        Vector2f vel = new Vector2f();
        for (int i = 0; i < 120; i++)
        {
            vel.set(((float) Math.random() * 1.25f + 0.25f) * AREA_EFFECT, 0f);
            VectorUtils.rotate(vel, (float) Math.random() * 360f, vel);
            Global.getCombatEngine().addSmoothParticle(missile.getLocation(), vel, (float) Math.random() * 2.5f + 2.5f, 1f, (float) Math.random() * 0.4f + 0.8f,
                    COLOR_PARTICLE);
        }
        
        MS_effectsHook.createPulse(missile.getLocation());
        //apply a bit of push to craft in a radius, plus a bit of screwing with fighter angular momentum
        float pushRadius = Math.max(missile.getCollisionRadius(), MIRV_PUSH_RADIUS);
        List <ShipAPI> neighbors = CombatUtils.getShipsWithinRange(missile.getLocation(), pushRadius);
        if (neighbors !=null) {
            for (ShipAPI tmp : neighbors) {
                if (tmp.isFighter() || tmp.isDrone()) {
                    float aVel = tmp.getAngularVelocity() / MathUtils.getDistance(missile.getLocation(), tmp.getLocation());
                    tmp.setAngularVelocity(aVel);
                }
                push(tmp, VectorUtils.getAngle(missile.getLocation(), tmp.getLocation()), 250f * SIZE_MULT.get(tmp.getHullSize()));
            }
        }

        StandardLight light = new StandardLight(missile.getLocation(), ZERO, ZERO, null);
        light.setColor(COLOR_CORE);
        light.setSize(AREA_EFFECT * 2f);
        light.setIntensity(0.3f);
        light.fadeOut(0.5f);
        LightShader.addLight(light);

        List<CombatEntityAPI> targets = new ArrayList<>(50);
        targets.addAll(CombatUtils.getMissilesWithinRange(missile.getLocation(), AREA_EFFECT));
        targets.addAll(CombatUtils.getShipsWithinRange(missile.getLocation(), AREA_EFFECT));
        targets.addAll(CombatUtils.getAsteroidsWithinRange(missile.getLocation(), AREA_EFFECT));
        targets.remove(missile.getSource());

        Iterator<CombatEntityAPI> iter = targets.iterator();
        while (iter.hasNext())
        {
            CombatEntityAPI entity = iter.next();
            if (entity.getOwner() == missile.getOwner())
            {
                iter.remove();
                continue;
            }

            if (entity instanceof ShipAPI)
            {
                ShipAPI ship = (ShipAPI) entity;
                if (ship.getCollisionClass() == CollisionClass.NONE)
                {
                    iter.remove();
                    continue;
                }

                if (!ship.isFighter() && !ship.isDrone())
                {
                    continue;
                }

                boolean remove = false;
                for (CombatEntityAPI tgt : targets)
                {
                    if (tgt.getShield() != null && tgt != ship)
                    {
                        if (tgt.getShield().isWithinArc(ship.getLocation()) && tgt.getShield().isOn())
                        {
                            remove = true;
                        }
                    }
                }

                if (remove)
                {
                    iter.remove();
                }
            }
        }

        for (CombatEntityAPI tgt : targets)
        {
            float distance = MathUtils.getDistance(tgt.getLocation(), missile.getLocation());
            float reduction = 1f;
            if (distance > AREA_EFFECT_INNER + tgt.getCollisionRadius())
            {
                reduction = (AREA_EFFECT - distance) / (AREA_EFFECT - AREA_EFFECT_INNER);
            }

            if (reduction <= 0f)
            {
                continue;
            }

            Vector2f damagePoint;
            if (tgt instanceof DamagingProjectileAPI)
            {
                damagePoint = missile.getLocation();
            }
            else
            {
                Vector2f projection = VectorUtils.getDirectionalVector(missile.getLocation(), tgt.getLocation());
                projection.scale(tgt.getCollisionRadius());
                Vector2f.add(projection, tgt.getLocation(), projection);
                damagePoint = CollisionUtils.getCollisionPoint(missile.getLocation(), projection, tgt);
                if (damagePoint == null)
                {
                    damagePoint = missile.getLocation();
                }
            }
            Global.getCombatEngine().applyDamage(tgt, damagePoint, AREA_DAMAGE * reduction, DamageType.FRAGMENTATION, 0f, false, false, tgt);
        }

        Global.getCombatEngine().applyDamage(missile, missile.getLocation(), missile.getHitpoints() * 100f, DamageType.FRAGMENTATION, 0f, false, false, missile);
        for (int i = 0; i < MMM_COUNT; i++)
        {
            float angle = missile.getFacing() + i * 360f / MMM_COUNT + (float) Math.random() * 180f / MMM_COUNT;
            angle %= 360f;
            vel.set((float) Math.random() * 300f - 300f, 0f);
            VectorUtils.rotate(vel, angle, vel);
            Vector2f location = MathUtils.getPointOnCircumference(missile.getLocation(), 3f, angle);
            GuidedMissileAI mmm = (GuidedMissileAI) (((MissileAPI) Global.getCombatEngine().spawnProjectile(missile.getSource(), missile.getWeapon(),
                    "ms_swarm_mssl", location, angle, vel)).getMissileAI());
            mmm.setTarget(findMxBestTarget(missile));
        }
    }
    
    public static ShipAPI findMxBestTarget(MissileAPI missile)
    {
        ShipAPI closest = null;
        float distance, closestDistance = Float.MAX_VALUE;

        List<ShipAPI> ships = AIUtils.getEnemiesOnMap(missile);
        int size = ships.size();
        for (int i = 0; i < size; i++)
        {
            ShipAPI tmp = ships.get(i);
            float mod = 0f;
            if (tmp.getCollisionClass() == CollisionClass.NONE)
            {
                mod = 1000f;
            }
            distance = MathUtils.getDistance(tmp, missile.getLocation()) + mod + MathUtils.getRandomNumberInRange(200f, 1000f);
            if (distance < closestDistance)
            {
                closest = tmp;
                closestDistance = distance;
            }
        }

        return closest;
    }
    
    private static ShipAPI findBestTarget(MissileAPI missile)
    {
        ShipAPI closest = null;
        float distance, closestDistance = Float.MAX_VALUE;

        List<ShipAPI> ships = AIUtils.getEnemiesOnMap(missile);
        int size = ships.size();
        for (int i = 0; i < size; i++)
        {
            ShipAPI tmp = ships.get(i);
            float mod = 0f;
            if (tmp.isFighter() || tmp.isDrone() || tmp.getCollisionClass() == CollisionClass.NONE)
            {
                mod = 4000f;
            }
            distance = MathUtils.getDistance(tmp, missile.getLocation()) + mod;
            if (distance < closestDistance)
            {
                closest = tmp;
                closestDistance = distance;
            }
        }

        return closest;
    }
    
    public static Vector2f intercept(Vector2f point, float speed, Vector2f target, Vector2f targetVel)
    {
        final Vector2f difference = new Vector2f(target.x - point.x, target.y - point.y);

        final float a = targetVel.x * targetVel.x + targetVel.y * targetVel.y - speed * speed;
        final float b = 2 * (targetVel.x * difference.x + targetVel.y * difference.y);
        final float c = difference.x * difference.x + difference.y * difference.y;

        final Vector2f solutionSet = quad(a, b, c);

        Vector2f intercept = null;
        if (solutionSet != null)
        {
            float bestFit = Math.min(solutionSet.x, solutionSet.y);
            if (bestFit < 0)
            {
                bestFit = Math.max(solutionSet.x, solutionSet.y);
            }
            if (bestFit > 0)
            {
                intercept = new Vector2f(target.x + targetVel.x * bestFit, target.y + targetVel.y * bestFit);
            }
        }

        return intercept;
    }

    private static Vector2f quad(float a, float b, float c)
    {
        Vector2f solution = null;
        if (Float.compare(Math.abs(a), 0) == 0)
        {
            if (Float.compare(Math.abs(b), 0) == 0)
            {
                solution = (Float.compare(Math.abs(c), 0) == 0) ? new Vector2f(0, 0) : null;
            }
            else
            {
                solution = new Vector2f(-c / b, -c / b);
            }
        }
        else
        {
            float d = b * b - 4 * a * c;
            if (d >= 0)
            {
                d = (float) Math.sqrt(d);
                a = 2 * a;
                solution = new Vector2f((-b - d) / a, (-b + d) / a);
            }
        }
        return solution;
    }
}
