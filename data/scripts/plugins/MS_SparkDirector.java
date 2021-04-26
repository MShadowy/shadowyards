package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_SparkDirector extends BaseEveryFrameCombatPlugin {
    
    private static final float MIN_SPARK_TURN_INTERVAL = 0.5f;
    private static final float MAX_SPARK_TURN_INTERVAL = 1.5f;
    
    private static final float SWAY_AMOUNT_PRIMARY = 360f;
    private static final float SWAY_AMOUNT_SECONDARY = SWAY_AMOUNT_PRIMARY / 2;
    
    private static final float SWAY_FALLOFF_FACTOR = 0f;
    
    //private Vector2f targetPoint;
    private float swayCounter1; // Counter for handling primary sway
    private float swayCounter2; // Counter for handling secondary sway
    private float swayThisFrame;
    private float lifeCounter; // Keeps track of projectile lifetime
    private float estimateMaxLife; // How long we estimate this projectile should be alive
    private float targetAngle; // Only for ONE_TURN_DUMB, the target angle that we want to hit with the projectile
    
    private final IntervalUtil nextTurn = new IntervalUtil (0.5f, 1.5f); 
    
    private DamagingProjectileAPI proj; //The projectile itself
    private CombatEntityAPI target;
    
    public MS_SparkDirector (@NotNull DamagingProjectileAPI proj, CombatEntityAPI target) {
        this.proj = proj;
        this.target = target;
        swayCounter1 = MathUtils.getRandomNumberInRange(0f, 1f);
	swayCounter2 = MathUtils.getRandomNumberInRange(0f, 1f);
        lifeCounter = 0f;
        estimateMaxLife = proj.getWeapon().getRange() / new Vector2f(proj.getVelocity().x - proj.getSource().getVelocity().x, proj.getVelocity().y - proj.getSource().getVelocity().y).length();
        targetAngle = proj.getWeapon().getCurrAngle() + MathUtils.getRandomNumberInRange(-0.5f, 0.5f);
        //targetPoint = MathUtils.getRandomPointInCircle(getApproximateInterception(25), 0.5f);
    }
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        //Sanity checks
	if (Global.getCombatEngine() == null) {
		return;
	}
	if (Global.getCombatEngine().isPaused()) {
		amount = 0f;
	}
        
        //Checks if our script should be removed from the combat engine
	if (proj == null || proj.didDamage() || proj.isFading() || !Global.getCombatEngine().isEntityInPlay(proj)) {
		Global.getCombatEngine().removePlugin(this);
		return;
	}

        //Ticks up our life counter: if we miscalculated, also top it off
        lifeCounter+=amount;
        if (lifeCounter > estimateMaxLife) { lifeCounter = estimateMaxLife; }

        swayCounter1 += amount*MIN_SPARK_TURN_INTERVAL;
        swayCounter2 += amount*MAX_SPARK_TURN_INTERVAL;
        swayThisFrame = (float)Math.pow(1f - (lifeCounter / estimateMaxLife), SWAY_FALLOFF_FACTOR) *
                    ((float)(FastTrig.sin(Math.PI * 2f * swayCounter1) * SWAY_AMOUNT_PRIMARY) + 
                (float)(FastTrig.sin(Math.PI * 2f * swayCounter2) * SWAY_AMOUNT_SECONDARY));
                    
        nextTurn.advance(amount);
        
        if (!proj.didDamage() || !proj.isFading() || Global.getCombatEngine().isEntityInPlay(proj)) {
            float facingSwayless = proj.getFacing() - swayThisFrame;
            float turnAngle = proj.getFacing();
            if (nextTurn.intervalElapsed()) {
                turnAngle += (SWAY_AMOUNT_PRIMARY * Math.random()) - SWAY_AMOUNT_SECONDARY;
            }
            float angleDiffAbsolute = Math.abs(targetAngle - facingSwayless);
            while (angleDiffAbsolute > 180f) { angleDiffAbsolute = Math.abs(angleDiffAbsolute-360f);}
            facingSwayless += Misc.getClosestTurnDirection(facingSwayless, turnAngle) * Math.min(angleDiffAbsolute, 180f * amount);
            proj.setFacing(facingSwayless + swayThisFrame);
            proj.getVelocity().x = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless+swayThisFrame).x;
            proj.getVelocity().y = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless+swayThisFrame).y;
        }
                    
        /*if (nextTurn.intervalElapsed()) {
            float facingSwayless = proj.getFacing() - swayThisFrame;
            float angleDiffAbsolute = Math.abs(targetAngle - facingSwayless);
            while (angleDiffAbsolute > 180f) { angleDiffAbsolute = Math.abs(angleDiffAbsolute-360f);}
            facingSwayless += (SWAY_AMOUNT_PRIMARY * Math.random()) - SWAY_AMOUNT_SECONDARY;
            proj.setFacing(facingSwayless + swayThisFrame);
            proj.getVelocity().x = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless+swayThisFrame).x;
            proj.getVelocity().y = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless+swayThisFrame).y;
        }*/
    }
}