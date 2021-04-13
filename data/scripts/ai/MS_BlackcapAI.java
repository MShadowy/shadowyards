package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.Collections;
import java.util.List;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;



public class MS_BlackcapAI implements MissileAIPlugin, GuidedMissileAI {
    //data
    private static final float ANTI_CLUMP_RANGE = 45f;
    private static final float VELOCITY_DAMPING_FACTOR = 0.1f;
    private static final Vector2f ZERO = new Vector2f();
    private IntervalUtil timer = new IntervalUtil(0.1f, 0.25f);
    private final MissileAPI missile;
    private float nearestMissileAngle = 180f;
    private float nearestMissileDistance = Float.MAX_VALUE;
    private CombatEntityAPI target;
    
    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public static ShipAPI findBestTarget(MissileAPI missile) {
        ShipAPI source = missile.getSource();
        if (source != null && source.getShipTarget() != null && !source.getShipTarget().isHulk()) {
            return source.getShipTarget();
        }

        return AIUtils.getNearestEnemy(missile);
    }
    
    public MS_BlackcapAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
        
        if (launchingShip.getShipTarget() != null && !launchingShip.getShipTarget().isHulk())
        {
            setTarget(launchingShip.getShipTarget());
        }
        
        if (target == null) {
            List<ShipAPI> directTargets = CombatUtils.getShipsWithinRange(launchingShip.getMouseTarget(), 200f);
            if (!directTargets.isEmpty()) {
                Collections.sort(directTargets, new CollectionUtils.SortEntitiesByDistance(launchingShip.getMouseTarget()));
                int size = directTargets.size();
                for (int i = 0; i < size; i++) 
                {
                    ShipAPI tmp = directTargets.get(i);
                    if (!tmp.isHulk() && tmp.getOwner() != launchingShip.getOwner()) {
                        setTarget(tmp);
                        break;
                    }
                }
            }
        }

        // Otherwise, use default Scatter targeting AI
        if (target == null) {
            setTarget(findBestTarget(missile));
        }
    }
    
    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////
    
    @Override
    public void advance (float amount) {
        // Apparently commands still work while fizzling
        if (missile.isFading() || missile.isFizzling()) {
            return;
        }
        
        // If our current target is lost, assign a new one
        if (target == null // unset
                || (target instanceof ShipAPI && ((ShipAPI) target).isHulk()) // dead
                || (missile.getOwner() == target.getOwner()) // friendly
                || !Global.getCombatEngine().isEntityInPlay(target)) // completely removed
        {
            setTarget(findBestTarget(missile));
            //if no new target can be found, just wellp on out
            if (target == null)
            {
                missile.giveCommand(ShipCommand.ACCELERATE);
                return;
            }
        }
        
        timer.advance(amount);
        
        //cancelling IF: skip the AI if the game is paused, the missile is way off course, engineless or without a target or target is phased out
        if (Global.getCombatEngine().isPaused() || missile.isFading() || missile.isFizzling() || target == null) {return;}
        
        //finding lead point to aim to
        //public static Vector2f getBestInterceptPoint(Vector2f point, float speed,Vector2f targetLoc, Vector2f targetVel)
        float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
        float flightSpeed = missile.getMaxSpeed();
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
        
        if (timer.intervalElapsed())
        {
            nearestMissileDistance = Float.MAX_VALUE;
            nearestMissileAngle = 180f;
            List<MissileAPI> nearbyBlackcaps = CombatUtils.getMissilesWithinRange(missile.getLocation(), ANTI_CLUMP_RANGE);
            for (MissileAPI b : nearbyBlackcaps)
            {
                if (b == missile)
                {
                    continue;
                }

                if (b.getProjectileSpecId() != null && missile.getProjectileSpecId() != null && b.getProjectileSpecId().contentEquals(
                        missile.getProjectileSpecId()))
                {
                    float bcDistance = MathUtils.getDistance(missile.getLocation(), b.getLocation());
                    if (bcDistance < nearestMissileDistance)
                    {
                        nearestMissileDistance = bcDistance;
                        nearestMissileAngle = VectorUtils.getAngle(missile.getLocation(), b.getLocation());
                    }
                }
            }
        }
        
        float angularDistance = MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(missile.getLocation(), lead));
        float nearestMissileAngularDistance = MathUtils.getShortestRotation(missile.getFacing(), nearestMissileAngle);
        if (nearestMissileDistance <= ANTI_CLUMP_RANGE && Math.abs(nearestMissileAngularDistance) <= 100f && distance > 600f)
        {
            if (nearestMissileAngularDistance <= 0f)
            {
                angularDistance += 0.75f * (1f - nearestMissileDistance / ANTI_CLUMP_RANGE) * (100f + nearestMissileAngularDistance);
            }
            else
            {
                angularDistance += 0.75f * (1f - nearestMissileDistance / ANTI_CLUMP_RANGE) * (-100f + nearestMissileAngularDistance);
            }
        }
        float absDAng = Math.abs(angularDistance);
        
        missile.giveCommand(angularDistance < 0 ? ShipCommand.TURN_RIGHT : ShipCommand.TURN_LEFT);

        if (absDAng <= 90f || missile.getVelocity().length() <= flightSpeed * 0.75f)
        {
            missile.giveCommand(ShipCommand.ACCELERATE);
        }

        if (absDAng < 5)
        {
            float MFlightAng = VectorUtils.getAngle(ZERO, missile.getVelocity());
            float MFlightCC = MathUtils.getShortestRotation(missile.getFacing(), MFlightAng);
            if (Math.abs(MFlightCC) > 20)
            {
                missile.giveCommand(MFlightCC < 0 ? ShipCommand.STRAFE_LEFT : ShipCommand.STRAFE_RIGHT);
            }
        }

        if (absDAng < Math.abs(missile.getAngularVelocity()) * VELOCITY_DAMPING_FACTOR)
        {
            missile.setAngularVelocity(angularDistance / VELOCITY_DAMPING_FACTOR);
        }
    }
    
    //////////////////////
    //    TARGETTING    //
    //////////////////////
    
    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public final void setTarget(CombatEntityAPI target) {
        this.target = target;
    }
    
    public void init(CombatEngineAPI engine) {}
    
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
