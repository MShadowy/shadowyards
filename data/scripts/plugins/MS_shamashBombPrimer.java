package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glEnable;
import org.lwjgl.util.vector.Vector2f;

public class MS_shamashBombPrimer extends BaseEveryFrameCombatPlugin {
    //Animation handler for the Shamash projectile sprite
    //While not in proximity does a short loop to give a pulsing effect
    //If close enough, scales downward to a single point
    
    private static final float BLINKER_Y_OFFSET = -3f;
    private static final float BLINKS_PER_SECOND = 4f;
    private static final String BOMB_PROJ_ID = "ms_phaseblast";
    private static final float TIME_BETWEEN_CHECKS = 0.33f;
    private final Map<DamagingProjectileAPI, Blinker> bombs = new HashMap<>(30);
    private float nextCheck = TIME_BETWEEN_CHECKS;
    private SpriteAPI sprite;
    private float spriteHeight;
    private float spriteWidth;
    
    private DamagingProjectileAPI proj;
    
    public MS_shamashBombPrimer (DamagingProjectileAPI proj) {
        this.proj = proj;
    }
 
    @Override
    public void advance(float amount, List<InputEventAPI> events)
    {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null || engine.isPaused())
        {
            return;
        }
        
        if (proj == null || proj.isFading() || proj.didDamage() || !Global.getCombatEngine().isEntityInPlay(proj)) {
            Global.getCombatEngine().removePlugin(this);
            return;
	}
 
        // Scan all projectiles for new bombs periodically
        nextCheck -= amount;
        if (nextCheck <= 0f)
        {
            nextCheck = TIME_BETWEEN_CHECKS;
 
            // Scan for bombs, activate blinkers on any that don't have one yet
            for (DamagingProjectileAPI proj : engine.getProjectiles())
            {
                if (BOMB_PROJ_ID.equals(proj.getProjectileSpecId())
                        && !bombs.containsKey(proj))
                {
                    bombs.put(proj, new Blinker(proj));
                }
            }
        }
    }
 
    @Override
    public void init(CombatEngineAPI engine)
    {
        sprite = Global.getSettings().getSprite("misc", "ms_bomb_blinker");
        sprite.setAdditiveBlend();
        spriteWidth = sprite.getWidth();
        spriteHeight = sprite.getHeight();
        bombs.clear();
    }
 
    @Override
    public void renderInWorldCoords(ViewportAPI view)
    {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null)
        {
            return;
        }
 
        if (!bombs.isEmpty())
        {
            float amount = (engine.isPaused() ? 0f : engine.getElapsedInLastFrame());
            glEnable(GL_TEXTURE_2D);
            for (Iterator<Map.Entry<DamagingProjectileAPI, Blinker>> iter = bombs.entrySet().iterator(); iter.hasNext();)
            {
                Map.Entry<DamagingProjectileAPI, Blinker> entry = iter.next();
                DamagingProjectileAPI proj = entry.getKey();
 
                // Remove dead missiles
                if (proj.didDamage() || !engine.isEntityInPlay(proj))
                {
                    iter.remove();
                    continue;
                }
 
                // Advance blinker effect
                entry.getValue().advanceAndRender(amount);
            }
        }
    }
 
    private class Blinker
    {
        private boolean blinkDirection;
        private float blinkProgress;
        private final DamagingProjectileAPI bomb;
 
        private Blinker(DamagingProjectileAPI bomb)
        {
            this.bomb = bomb;
            blinkProgress = 0f;
            blinkDirection = true;
        }
 
        private void advanceAndRender(float amount)
        {
            // Blinker oscillates between -1 and 1, only drawn above 0
            if (amount > 0f)
            {
                blinkProgress += amount * BLINKS_PER_SECOND * (blinkDirection ? 4f : -4f);
                if ((blinkDirection && blinkProgress > 1f)
                        || (!blinkDirection && blinkProgress < -1f))
                {
                    blinkProgress = (blinkDirection ? 1f : -1f);
                    blinkDirection = !blinkDirection;
                }
            }
 
            if (blinkProgress >= 0f)
            {
                // Render blinker, altering size and intensity based on current blink progress
                sprite.setAlphaMult(Math.min(1f, 0.5f + (blinkProgress / 2f)));
                sprite.setSize(spriteWidth * blinkProgress, spriteHeight * blinkProgress);
                Vector2f loc = MathUtils.getPointOnCircumference(
                        bomb.getLocation(), BLINKER_Y_OFFSET, bomb.getFacing());
                sprite.renderAtCenter(loc.x, loc.y);
            }
        }
    }
}
