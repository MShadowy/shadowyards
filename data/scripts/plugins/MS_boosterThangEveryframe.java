package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.lazywizard.lazylib.VectorUtils;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import org.lwjgl.util.vector.Vector2f;


public class MS_boosterThangEveryframe extends BaseEveryFrameCombatPlugin {
    
    private final Map<CombatEntityAPI, Float> shipTrailIDs = new WeakHashMap<>();
    private final Map<CombatEntityAPI, Float> shipTrailIDs2 = new WeakHashMap<>();
    private final Map<CombatEntityAPI, Float> shipTrailIDs3 = new WeakHashMap<>();
    
    private static final Set<String> SHIP_IDS = new HashSet<>();

    static {
        // Add all the enginekiller
        SHIP_IDS.add("ms_ninurta");
    }
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();
        
        for (ShipAPI s : engine.getShips()) {
            String spec = s.getHullSpec().getHullId();
            if (engine.isPaused() || !SHIP_IDS.contains(spec)) {
                continue;
            }
            
            ShipSystemAPI sys = s.getSystem();
            
            if (s.isAlive() && sys.isActive()) {
                shipTrailIDs.put(s, MagicTrailPlugin.getUniqueID());
                shipTrailIDs2.put(s, MagicTrailPlugin.getUniqueID());
                shipTrailIDs3.put(s, MagicTrailPlugin.getUniqueID());
                
                float dir = VectorUtils.getAngle(s.getLocation(), s.getVelocity());
                
                MagicTrailPlugin.AddTrailMemberAdvanced(s, shipTrailIDs.get(s), Global.getSettings().getSprite("sra_trails",
                        "rhpcb_proj_trail"), s.getLocation(), 0f, 0f, dir - 180f, 
                    0f, 0f, 24f, 0f, new Color(170, 225, 200), new Color(60, 90, 120), 0.4f, 0.25f, 0.45f, 0.3f, GL_SRC_ALPHA, GL_ONE, 
                    128, 500, new Vector2f(0,0), null, CombatEngineLayers.ABOVE_SHIPS_LAYER, 0.4f);
                MagicTrailPlugin.AddTrailMemberAdvanced(s, shipTrailIDs2.get(s), Global.getSettings().getSprite("sra_trails",
                        "rhpcb_secondary_proj_trail"), s.getLocation(), 0f, 0f, dir - 180f, 
                    0f, 0f, 24f, 0f, new Color(90, 120, 200), new Color(60, 90, 120), 0.4f, 0.25f, 0.45f, 0.3f, GL_SRC_ALPHA, GL_ONE, 
                    128, 500, new Vector2f(0,0), null, CombatEngineLayers.ABOVE_SHIPS_LAYER, 0.4f);
                MagicTrailPlugin.AddTrailMemberAdvanced(s, shipTrailIDs3.get(s), Global.getSettings().getSprite("sra_trails",
                        "wave_secondary_trail"), s.getLocation(), 0f, 0f, dir - 180f, 
                    0f, 0f, 24f, 0f, new Color(115, 180, 130), new Color(60, 90, 120), 0.4f, 0.25f, 0.45f, 0.3f, GL_SRC_ALPHA, GL_ONE, 
                    128, 500, new Vector2f(0,0), null, CombatEngineLayers.ABOVE_SHIPS_LAYER, 0.4f);
            }
        }
    }
    
    @Override
    public void init(CombatEngineAPI engine) {
    }
    
    @Override
    public void renderInUICoords(ViewportAPI viewport) {
    }
     
    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {  
    }
}
