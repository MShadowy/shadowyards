package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.util.vector.Vector2f;

public class MS_overdriveStats extends BaseShipSystemScript {

    public static final float ROF_BONUS = 1f;
    public static final float FLUX_MULT = 0.8f;
    public static final float ROF_MALUS = 0.5f;
    
    private static final String sound = "overloadSteam";

    private static final Map<WeaponSize, Float> mag = new HashMap<>();

    static {
        mag.put(WeaponSize.SMALL, 1f);
        mag.put(WeaponSize.MEDIUM, 1.5f);
        mag.put(WeaponSize.LARGE, 2.5f);
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        CombatEngineAPI engine = Global.getCombatEngine();
        ShipAPI ship = (ShipAPI) stats.getEntity();
        List<WeaponAPI> weaps = ship.getAllWeapons();
        boolean hasPlayed = false;
        
        if (state == State.ACTIVE) {
            float mult = 1f + ROF_BONUS * effectLevel;
            stats.getBallisticRoFMult().modifyMult(id, mult);
            stats.getBallisticWeaponFluxCostMod().modifyMult(id, FLUX_MULT);
            stats.getMissileRoFMult().modifyMult(id, mult);
            stats.getEnergyRoFMult().modifyMult(id, mult);
            stats.getEnergyWeaponFluxCostMod().modifyMult(id, FLUX_MULT);
            stats.getBeamWeaponFluxCostMult().modifyFlat(id, FLUX_MULT);
            
            for (WeaponAPI w : weaps) {
                if (engine.isPaused()) {
                    return;
                }
                
                float smokeSize = 0.8f + 0.4f * (float) Math.random();
                
                float smokeSizeValue = mag.get(w.getSize());
                

                float velX = (float) Math.random() * 10f - 5f;
                float velY = (float) Math.sqrt(25f - velX * velX);
                if ((float) Math.random() >= 0.5f) {
                    velY = -velY;
                }

                engine.addSmokeParticle(w.getLocation(), new Vector2f(velX, velY), 30f * smokeSize * smokeSizeValue, 0.05f, 4f, new Color(130, 130, 160, 20));
                engine.addSmokeParticle(w.getLocation(), new Vector2f(velX, velY), 15f * smokeSize * smokeSizeValue, 0.05f, 3f, new Color(180, 180, 210, 20));
            }
            
            if (!hasPlayed) {
                Global.getSoundPlayer().playSound(sound, 0.8f, 0.5f, ship.getLocation(), ship.getVelocity());
                hasPlayed = true;
            }
        } else if (state == State.OUT) {
            float mult = 1f - ROF_MALUS * effectLevel;
            stats.getBallisticRoFMult().modifyMult(id, mult);
            stats.getMissileRoFMult().modifyMult(id, mult);
            stats.getEnergyRoFMult().modifyMult(id, mult);
            
            hasPlayed = false;
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getBallisticRoFMult().unmodify(id);
        stats.getBallisticWeaponFluxCostMod().unmodify(id);
        stats.getMissileRoFMult().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
        stats.getBeamWeaponFluxCostMult().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        float mult = 1f + ROF_BONUS * effectLevel;
        float curs = 1f - ROF_MALUS * effectLevel;
        float bonusPercent = (int) (mult - 1f) * 100f;
        float malusPercent = (int) (curs - 1f) * 100f;
        if (index == 0) {
            return new StatusData("all weapons rate of fire +" + (int) bonusPercent + "%", false);
        } else if (index == 1) {
            return new StatusData("all weapons rate of fire -" + (int) malusPercent + "%", false);
        }
        return null;
    }
}
