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
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import data.scripts.util.MagicTargeting;

public class MS_ApisAI implements MissileAIPlugin, GuidedMissileAI {
    
    private final MissileAPI missile;
    private float nearestMissileAngle = 180f;
    private float nearestMissileDistance = Float.MAX_VALUE;
    private static final float ANTI_CLUMP_RANGE = 45f;
    private static final Vector2f ZERO = new Vector2f();
    private CombatEntityAPI target;
    private final static float VELOCITY_DAMPING_FACTOR = 0.1f;
    private IntervalUtil timer = new IntervalUtil(0.1f, 0f);
    private IntervalUtil track = new IntervalUtil(0.3f, 0f);
    
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
    
    public MS_ApisAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
        
        // Main targeting loop; missiles randomly choose a target in their overall flight range, heavily weighted towards fighters
        List<ShipAPI> directTargets = CombatUtils.getShipsWithinRange(launchingShip.getMouseTarget(), 100f);
        if (!directTargets.isEmpty()) {
            ShipAPI tmp = MagicTargeting.pickMissileTarget(
            missile, 
            MagicTargeting.targetSeeking.FULL_RANDOM, 
            4000, 
            360, 
            100, 
            5, 
            2, 
            1, 
            1);
                
            if (tmp.getOwner() != missile.getOwner()) {
                setTarget(tmp);
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
        
        timer.advance(amount);
        
        //the Apis never not thrusts; always thrust, never stop!
        missile.giveCommand(ShipCommand.ACCELERATE);   
        
        if (!track.intervalElapsed()) { 
            track.advance(amount);
            return; 
        }
        
        // If our current target is lost, assign a new one
        if (target == null // unset
                || (target instanceof ShipAPI && ((ShipAPI) target).isHulk()) // dead
                || (missile.getOwner() == target.getOwner()) // friendly
                || !Global.getCombatEngine().isEntityInPlay(target)) // completely removed
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
        
        //fiding lead point to aim to
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
        
        if (timer.intervalElapsed())
        {
            nearestMissileDistance = Float.MAX_VALUE;
            nearestMissileAngle = 180f;
            List<MissileAPI> nearbyMissiles = CombatUtils.getMissilesWithinRange(missile.getLocation(), ANTI_CLUMP_RANGE);
            for (MissileAPI b : nearbyMissiles)
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
        
        //aimAngle = angle between the missile facing and the lead direction
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
    public void setTarget(CombatEntityAPI target) {
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
