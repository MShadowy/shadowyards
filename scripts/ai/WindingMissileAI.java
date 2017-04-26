package data.scripts.ai;

import com.fs.starfarer.api.Global;
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

public final class WindingMissileAI implements MissileAIPlugin, GuidedMissileAI {

    private final MissileAPI missile;
    // Our current target (can be null)
    private CombatEntityAPI target;
    private float offtarget = 40f * ((float) (.5 - Math.random()));           // max initial off target (scatters missiles at launch)
    private float baseofftarget = 20f * ((float) (.5 - Math.random()));        // min off target (makes missiles stay scattered)
    private IntervalUtil timer = new IntervalUtil(0.1f, 0f);

    public static ShipAPI findBestTarget(MissileAPI missile) {
        ShipAPI source = missile.getSource();
        if (source != null && source.getShipTarget() != null && !source.getShipTarget().isHulk()) {
            return source.getShipTarget();
        }

        return AIUtils.getNearestEnemy(missile);
    }

    public WindingMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;

        // Support for 'fire at target by clicking on them' behavior
        List<ShipAPI> directTargets = CombatUtils.getShipsWithinRange(launchingShip.getMouseTarget(), 100f);
        if (!directTargets.isEmpty()) {
            Collections.sort(directTargets, new CollectionUtils.SortEntitiesByDistance(launchingShip.getMouseTarget()));
            for (ShipAPI tmp : directTargets) {
                if (!tmp.isHulk() && tmp.getOwner() != launchingShip.getOwner()) {
                    setTarget(tmp);
                    break;
                }
            }
        }

        // Otherwise, use default Scatter targeting AI
        if (target == null) {
            setTarget(findBestTarget(missile));
        }
    }

    @Override
    public void advance(float amount) {
        // Apparently commands still work while fizzling
        if (missile.isFading() || missile.isFizzling()) {
            return;
        }

        timer.advance(amount);

        // This missile should always be accelerating
        missile.giveCommand(ShipCommand.ACCELERATE);

        // If our current target is lost, assign a new one
        if (target == null // unset
                || (target instanceof ShipAPI && ((ShipAPI) target).isHulk()) // dead
                || (missile.getOwner() == target.getOwner()) // friendly
                || !Global.getCombatEngine().isEntityInPlay(target)) // completely removed
        {
            setTarget(findBestTarget(missile));
            return;
        }

        float angularDistance = MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(missile.getLocation(), target.getLocation()));

        if (timer.intervalElapsed()) {
            offtarget = (offtarget > 0f ? offtarget - 1f : offtarget + 1f); //reduce off target counter;
        }

        float offtargetby = (0f + (offtarget + (baseofftarget * target.getCollisionRadius() / 75f)));
        float AbsAngD = Math.abs(angularDistance - offtargetby);

        // Point towards target  
        if (AbsAngD > 0.5f) {
            // makes missile fly off target  
            missile.giveCommand(angularDistance > offtargetby ? ShipCommand.TURN_LEFT : ShipCommand.TURN_RIGHT);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////
        // Correct missile velocity vector to be the same as missile facing  
        if (AbsAngD < 5f) {
            // Course correct for missile velocity vector  
            float MFlightAng = VectorUtils.getAngle(new Vector2f(), missile.getVelocity());
            float MFlightCC = MathUtils.getShortestRotation(missile.getFacing(), MFlightAng);
            if (Math.abs(MFlightCC) > 20f) {
                missile.giveCommand(MFlightCC < 0f ? ShipCommand.STRAFE_LEFT : ShipCommand.STRAFE_RIGHT);
            }
        }

        /////////////////////////////////////////////////////////////////////////////////////////////////
        // Stop turning once you are on target (way of the hack, ignores missile limitations)
        if (AbsAngD < 0.4f) {
            missile.setAngularVelocity(0f);
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
}
