package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.util.vector.Vector2f;

public class MS_OverheatSteam extends BaseEveryFrameCombatPlugin {

    private static final String sound = "overloadSteam";

    private static final Map<WeaponSize, Float> mag = new HashMap<>();

    static {
        mag.put(WeaponSize.SMALL, 1f);
        mag.put(WeaponSize.MEDIUM, 1.5f);
        mag.put(WeaponSize.LARGE, 2.5f);
    }

    @Override
    public void init(CombatEngineAPI engine) {
    }

    private final IntervalUtil interval = new IntervalUtil(0.1f, 0.1f);

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();

        if (engine.isPaused()) {
            return;
        }

        interval.advance(amount);

        if (interval.intervalElapsed()) {
            float smokeSize = 0.8f + 0.4f * (float) Math.random();

            List<ShipAPI> ships = engine.getShips();

            for (ShipAPI ship : ships) {

                if (ship.getSystem() != null && ship.getSystem().getId().contentEquals("ms_overdrive")) {
                    ShipSystemAPI system = ship.getSystem();

                    List<WeaponAPI> weapons = ship.getAllWeapons();

                    if (system != null) {
                        if (system.isActive() && !system.isOn()) {
                            for (WeaponAPI weapon : weapons) {

                                float smokeSizeValue = mag.get(weapon.getSize());

                                float velX = (float) Math.random() * 10f - 5f;
                                float velY = (float) Math.sqrt(25f - velX * velX);
                                if ((float) Math.random() >= 0.5f) {
                                    velY = -velY;
                                }

                                engine.addSmokeParticle(weapon.getLocation(), new Vector2f(velX, velY), 30f * smokeSize * smokeSizeValue, 0.05f, 4f, new Color(130, 130, 160, 20));
                                engine.addSmokeParticle(weapon.getLocation(), new Vector2f(velX, velY), 15f * smokeSize * smokeSizeValue, 0.05f, 3f, new Color(180, 180, 210, 20));
                            }

                            Global.getSoundPlayer().playSound(sound, 1f, 0.5f, ship.getLocation(), ship.getVelocity());
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public void renderInUICoords(ViewportAPI viewport) {
    }
     
    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {  
    }
}
