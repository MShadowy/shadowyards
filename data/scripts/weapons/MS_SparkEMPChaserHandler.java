package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_SparkEMPChaserHandler extends BaseEveryFrameCombatPlugin {
    
    private static final float TURN_RATE = 110f;
    private static final float SWAY_AMOUNT_PRIMARY = 30f;
    private static final float SWAY_AMOUNT_SECONDARY = 10f;
    private static final float SWAY_PERIOD_PRIMARY = 2f;
    private static final float SWAY_PERIOD_SECONDARY = 1f;
    private static final float SWAY_FALLOFF_FACTOR = 0f;
    private static final float ONE_TURN_DUMB_INACCURACY = 60f;
    
    private float swayCounter1; // Counter for handling primary sway
    private float swayCounter2; // Counter for handling secondary sway
    private float targetAngle;
    private float lifeCounter; // Keeps track of projectile lifetime
    private float estimateMaxLife; // How long we estimate this projectile should be alive
    
    private Vector2f offsetVelocity; // Only used for ONE_TURN_DUMB: keeps velocity from the ship and velocity from the projectile separate (messes up calculations otherwise)
 
    private CombatEngineAPI engine;
    private DamagingProjectileAPI proj;
    
    public MS_SparkEMPChaserHandler(@NotNull DamagingProjectileAPI proj) {
        this.proj = proj;
        swayCounter1 = MathUtils.getRandomNumberInRange(0f, 1f);
	swayCounter2 = MathUtils.getRandomNumberInRange(0f, 1f);
        targetAngle = proj.getFacing() + MathUtils.getRandomNumberInRange(-ONE_TURN_DUMB_INACCURACY, ONE_TURN_DUMB_INACCURACY);
        lifeCounter = 0f;
        estimateMaxLife = proj.getWeapon().getRange() / new Vector2f(proj.getVelocity().x - proj.getSource().getVelocity().x, proj.getVelocity().y - proj.getSource().getVelocity().y).length();
    }
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }
        
        //Checks if our script should be removed from the combat engine
	if (proj == null || proj.didDamage() || proj.isFading() || !Global.getCombatEngine().isEntityInPlay(proj)) {
            Global.getCombatEngine().removePlugin(this);
            return;
	}
        
        chaseDirector(amount, events);
    }
    
    public void chaseDirector(float amount, List<InputEventAPI> events) {
        //Sanity checks
	if (Global.getCombatEngine() == null) {
		return;
	}
	if (Global.getCombatEngine().isPaused()) {
		amount = 0f;
	}
        
        //Tick the sway counter up here regardless of if we need it or not: helps reduce boilerplate code
	swayCounter1 += amount*SWAY_PERIOD_PRIMARY;
	swayCounter2 += amount*SWAY_PERIOD_SECONDARY;
	float swayThisFrame = (float)Math.pow(1f - (lifeCounter / estimateMaxLife), SWAY_FALLOFF_FACTOR) *
		((float)(FastTrig.sin(Math.PI * 2f * swayCounter1) * SWAY_AMOUNT_PRIMARY) + (float)(FastTrig.sin(Math.PI * 2f * swayCounter2) * SWAY_AMOUNT_SECONDARY));
        
        if (!proj.didDamage() || !proj.isFading() || Global.getCombatEngine().isEntityInPlay(proj)) {
            float facingSwayless = proj.getFacing() - swayThisFrame;
            float angleDiffAbsolute = Math.abs(targetAngle - facingSwayless);
            while (angleDiffAbsolute > 180f) { angleDiffAbsolute = Math.abs(angleDiffAbsolute-360f);}
            facingSwayless += Misc.getClosestTurnDirection(facingSwayless, targetAngle) * Math.min(angleDiffAbsolute, TURN_RATE*amount);
            Vector2f pureVelocity = new Vector2f(proj.getVelocity());
            pureVelocity.x -= offsetVelocity.x;
            pureVelocity.y -= offsetVelocity.y;
            proj.setFacing(facingSwayless + swayThisFrame);
            proj.getVelocity().x = MathUtils.getPoint(new Vector2f(Misc.ZERO), pureVelocity.length(), facingSwayless+swayThisFrame).x + offsetVelocity.x;
            proj.getVelocity().y = MathUtils.getPoint(new Vector2f(Misc.ZERO), pureVelocity.length(), facingSwayless+swayThisFrame).y + offsetVelocity.y;
        }
    }
    
    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }
}
