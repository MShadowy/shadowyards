package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import java.util.Collections;
import java.util.List;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_Barrago_S2_AI implements MissileAIPlugin, GuidedMissileAI
{
    private static final float MIRV_DISTANCE = 400f;
    private static final float MIRV_SPEED = 150f;
    private static final float VELOCITY_DAMPING_FACTOR = 0.15f;
    private static final Vector2f ZERO = new Vector2f();
    private static final String MIRV_ID = "ms_barrago_lrm_shatter_clone";
    private static final String SOUND_ID = "barrago_stage_three_fire";
    private final MissileAPI missile;
    private float nearestHorizonAngle = 180f;
    private CombatEntityAPI target;
    private float timeLive = 0f;

    public MS_Barrago_S2_AI(MissileAPI missile, ShipAPI launchingShip)
    {
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

    @Override
    public void advance(float amount)
    {
        if (missile.isFizzling() || missile.isFading())
        {
            if (target == null)
            {
                return;
            }
            float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
            float guidance = 0.45f;
            float mirvSpeed = MIRV_SPEED;
            if (missile.getSource() != null)
            {
                guidance += Math.min(missile.getSource().getMutableStats().getMissileGuidance().getModifiedValue()
                        - missile.getSource().getMutableStats().getMissileGuidance().getBaseValue(), 1f) * 0.3f;
                mirvSpeed *= missile.getSource().getMutableStats().getProjectileSpeedMult().getModifiedValue();
            }

            Vector2f mirvTarget = intercept(missile.getLocation(), mirvSpeed, target.getLocation(), Vector2f.sub(target.getVelocity(), missile.getVelocity(),
                    null));
            if (mirvTarget == null)
            {
                Vector2f projection = new Vector2f(target.getVelocity());
                float scalar = distance / mirvSpeed;
                projection.scale(scalar);
                Vector2f.add(target.getLocation(), projection, mirvTarget);
            }
            if (mirvTarget != null)
            {
                Vector2f.sub(mirvTarget, target.getLocation(), mirvTarget);
                mirvTarget.scale(guidance * 0.3f);
                Vector2f.add(mirvTarget, target.getLocation(), mirvTarget);

                float angularDistance = MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(missile.getLocation(), mirvTarget));
                float offBy = (float) Math.sin(Math.toRadians(angularDistance)) * distance;
                if (distance <= MIRV_DISTANCE + target.getCollisionRadius() + missile.getCollisionRadius() && offBy <= target.getCollisionRadius())
                {
                    mirv(missile);
                }
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

        float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
        float acceleration = missile.getAcceleration();
        float maxSpeed = missile.getMaxSpeed();
        float mirvSpeed = MIRV_SPEED;
        float guidance = 0.45f;
        if (missile.getSource() != null)
        {
            mirvSpeed *= missile.getSource().getMutableStats().getProjectileSpeedMult().getModifiedValue();
            guidance += Math.min(missile.getSource().getMutableStats().getMissileGuidance().getModifiedValue()
                    - missile.getSource().getMutableStats().getMissileGuidance().getBaseValue(), 1f) * 0.3f;
        }
        Vector2f guidedTarget = intercept(missile.getLocation(), missile.getVelocity().length(), acceleration, maxSpeed, target.getLocation(),
                Vector2f.sub(target.getVelocity(), missile.getVelocity(), null));
        if (guidedTarget == null)
        {
            Vector2f projection = new Vector2f(target.getVelocity());
            float scalar = distance / (missile.getVelocity().length() + 1f);
            projection.scale(scalar);
            guidedTarget = Vector2f.add(target.getLocation(), projection, null);
        }
        Vector2f.sub(guidedTarget, target.getLocation(), guidedTarget);
        guidedTarget.scale(guidance);
        Vector2f.add(guidedTarget, target.getLocation(), guidedTarget);

        Vector2f mirvTarget = intercept(missile.getLocation(), mirvSpeed, target.getLocation(), Vector2f.sub(target.getVelocity(), missile.getVelocity(), null));
        if (mirvTarget == null)
        {
            Vector2f projection = new Vector2f(target.getVelocity());
            float scalar = distance / mirvSpeed;
            projection.scale(scalar);
            Vector2f.add(target.getLocation(), projection, mirvTarget);
        }
        if (mirvTarget != null)
        {
            Vector2f.sub(mirvTarget, target.getLocation(), mirvTarget);
            mirvTarget.scale(guidance * 0.3f);
            Vector2f.add(mirvTarget, target.getLocation(), mirvTarget);
            float mirvAngularDistance = MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(missile.getLocation(), mirvTarget));
            float offBy = (float) Math.sin(Math.toRadians(mirvAngularDistance)) * distance;

            if (target.getShield() != null) {
                if (!target.getShield().isOn() && target.getCollisionClass() != CollisionClass.NONE && distance <= MIRV_DISTANCE + 
                    target.getCollisionRadius() + missile.getCollisionRadius() && offBy <= target.getCollisionRadius() && timeLive >= 1f ||
                target.getShield().isOn() && target.getCollisionClass() != CollisionClass.NONE && distance <= MIRV_DISTANCE + 
                    target.getCollisionRadius() + missile.getCollisionRadius() && offBy <= target.getShield().getRadius() && timeLive >= 1f)
                {
                    timeLive = -99999f;
                    mirv(missile);
                    return;
                }
            } else if (target.getShield() == null && target.getCollisionClass() != CollisionClass.NONE && distance <= MIRV_DISTANCE + 
                    target.getCollisionRadius() + missile.getCollisionRadius() && offBy <= target.getCollisionRadius() && timeLive >= 1f) {
                timeLive = -99999f;
                mirv(missile);
                return;
            }
        }

        float velocityFacing = VectorUtils.getFacing(missile.getVelocity());
        float absoluteDistance = MathUtils.getShortestRotation(velocityFacing, VectorUtils.getAngle(missile.getLocation(), guidedTarget));
        float angularDistance = MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(missile.getLocation(), guidedTarget));
        float nearestHorizonAngularDistance = MathUtils.getShortestRotation(missile.getFacing(), nearestHorizonAngle);
        
        float compensationDifference = MathUtils.getShortestRotation(angularDistance, absoluteDistance);
        if (Math.abs(compensationDifference) <= 75f)
        {
            angularDistance += 0.5f * compensationDifference;
        }
        float absDVel = Math.abs(absoluteDistance);
        float absDAng = Math.abs(angularDistance);

        missile.giveCommand(angularDistance < 0 ? ShipCommand.TURN_RIGHT : ShipCommand.TURN_LEFT);

        if (absDVel >= 135f || absDAng <= 90f || missile.getVelocity().length() <= maxSpeed * 0.75f)
        {
            missile.giveCommand(ShipCommand.ACCELERATE);
        }

        if (absDAng < Math.abs(missile.getAngularVelocity()) * VELOCITY_DAMPING_FACTOR)
        {
            missile.setAngularVelocity(angularDistance / VELOCITY_DAMPING_FACTOR);
        }
    }

    @Override
    public CombatEntityAPI getTarget()
    {
        return target;
    }

    @Override
    public final void setTarget(CombatEntityAPI target)
    {
        this.target = target;
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

    private static Vector2f intercept(Vector2f point, float speed, Vector2f target, Vector2f targetVel)
    {
        Vector2f difference = new Vector2f(target.x - point.x, target.y - point.y);

        float a = targetVel.x * targetVel.x + targetVel.y * targetVel.y - speed * speed;
        float b = 2 * (targetVel.x * difference.x + targetVel.y * difference.y);
        float c = difference.x * difference.x + difference.y * difference.y;

        Vector2f solutionSet = quad(a, b, c);

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

    private static Vector2f intercept(Vector2f point, float speed, float acceleration, float maxspeed, Vector2f target, Vector2f targetVel)
    {
        Vector2f difference = new Vector2f(target.x - point.x, target.y - point.y);

        float s = speed;
        float a = acceleration / 2f;
        float b = speed;
        float c = difference.length();
        Vector2f solutionSet = quad(a, b, c);
        if (solutionSet != null)
        {
            float t = Math.min(solutionSet.x, solutionSet.y);
            if (t < 0)
            {
                t = Math.max(solutionSet.x, solutionSet.y);
            }
            if (t > 0)
            {
                s = acceleration * t;
                s = s / 2f + speed;
                s = Math.min(s, maxspeed);
            }
        }

        a = targetVel.x * targetVel.x + targetVel.y * targetVel.y - s * s;
        b = 2 * (targetVel.x * difference.x + targetVel.y * difference.y);
        c = difference.x * difference.x + difference.y * difference.y;

        solutionSet = quad(a, b, c);

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

    private static void mirv(MissileAPI missile)
    {
        Global.getSoundPlayer().playSound(SOUND_ID, 1f, 1f, missile.getLocation(), ZERO);
        Global.getCombatEngine().applyDamage(missile, missile.getLocation(), missile.getHitpoints() * 100f, DamageType.FRAGMENTATION, 0f, false, false, missile);
        Vector2f location = MathUtils.getPointOnCircumference(missile.getLocation(), 5f, missile.getFacing());
        Vector2f vel = missile.getVelocity();
        int shotCount = (20);
        for (int j = 0; j < shotCount; j++) {
            Vector2f randomVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(10f, 20f));
            randomVel.x += vel.x;
            randomVel.y += vel.y;
            //spec + "_clone" means is, if its got the same name in its name (except the "_clone" part) then it must be that weapon.
            Global.getCombatEngine().spawnProjectile(missile.getSource(), missile.getWeapon(), MIRV_ID, location, missile.getFacing(), randomVel);
        }
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