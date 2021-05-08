package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_DerazhoFlareEffectPlugin extends BaseEveryFrameCombatPlugin {
    
    private CombatEngineAPI engine;
    private DamagingProjectileAPI proj;
    
    public MS_DerazhoFlareEffectPlugin(@NotNull DamagingProjectileAPI proj) {
        this.proj = proj;
    }
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }
        
        if (proj == null || proj.didDamage() || !Global.getCombatEngine().isEntityInPlay(proj)) {
		Global.getCombatEngine().removePlugin(this);
	}
    }
    
    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {
        if (Global.getCombatEngine() == null) {
            return;
        }

        Vector2f Here = new Vector2f(0,0) ;
        Here.x = proj.getLocation().x;
        Here.y = proj.getLocation().y;

        SpriteAPI sprite = Global.getSettings().getSprite("flare", "derazho_ALF");

        if (!Global.getCombatEngine().isPaused()) {
            sprite.setAlphaMult(MathUtils.getRandomNumberInRange(0.9f, 1f));
        } else {
            float tAlf = sprite.getAlphaMult();
            sprite.setAlphaMult(tAlf);
        }
        
        sprite.setSize(600, 150);
        sprite.setAdditiveBlend();
        sprite.renderAtCenter(Here.x, Here.y);
        
    }
    
    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }
}
