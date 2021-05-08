package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import data.scripts.plugins.MS_OverheatSteam;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.lwjgl.util.vector.Vector2f;

public class MS_overdriveStats extends BaseShipSystemScript {
    private final Set<ShipAPI> activeRegistry = new HashSet<>();
    
    protected Object STATUSKEY1 = new Object();
    protected Object STATUSKEY2 = new Object();
    private static final float ROF_BONUS = 1f;
    private static final float FLUX_MULT = 0.8f;
    private static final float ROF_MALUS = 0.5f;
    
    private static final String SOUND = "overloadSteam";

    private static final Map<WeaponSize, Float> MAG = new HashMap<>();

    static {
        MAG.put(WeaponSize.SMALL, 1f);
        MAG.put(WeaponSize.MEDIUM, 1.5f);
        MAG.put(WeaponSize.LARGE, 2.5f);
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        CombatEngineAPI engine = Global.getCombatEngine();
        ShipAPI ship = (ShipAPI) stats.getEntity();
        ShipSystemAPI system = ship.getSystem();
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
            
            if (!activeRegistry.contains(ship)) {
                engine.addPlugin(new MS_OverheatSteam(ship));
                activeRegistry.add(ship);
            }
            
            for (WeaponAPI w : weaps) {
                if (engine.isPaused()) {
                    return;
                }
                
                float smokeSize = 0.8f + 0.4f * (float) Math.random();
                
                float smokeSizeValue = MAG.get(w.getSize());
                

                float velX = (float) Math.random() * 10f - 5f;
                float velY = (float) Math.sqrt(25f - velX * velX);
                if ((float) Math.random() >= 0.5f) {
                    velY = -velY;
                }

                engine.addSmokeParticle(w.getLocation(), new Vector2f(velX, velY), 30f * smokeSize * smokeSizeValue, 0.05f, 4f, new Color(130, 130, 160, 20));
                engine.addSmokeParticle(w.getLocation(), new Vector2f(velX, velY), 15f * smokeSize * smokeSizeValue, 0.05f, 3f, new Color(180, 180, 210, 20));
            }
            
            if (!hasPlayed) {
                Global.getSoundPlayer().playSound(SOUND, 0.8f, 0.5f, ship.getLocation(), ship.getVelocity());
                hasPlayed = true;
            }
            
            if (ship == Global.getCombatEngine().getPlayerShip() && effectLevel > 0) {
                float bonus1 = (int) (mult - 1f) * 100f;
                float bonus2 = (int) 100f - (FLUX_MULT * 100f);
                
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1,
                    system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
                    "all weapons rate of fire increased " + (int) Math.round(bonus1) + "%", false);
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY2,
                    system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
                    "all weapons flux cost decreased " + (int) Math.round(bonus2) + "%", false);
            }
        } else if (state == State.OUT) {
            float mult = 1f - ROF_MALUS * effectLevel;
            stats.getBallisticRoFMult().modifyMult(id, mult);
            stats.getMissileRoFMult().modifyMult(id, mult);
            stats.getEnergyRoFMult().modifyMult(id, mult);
            
            
            
            hasPlayed = false;
            
            if (ship == Global.getCombatEngine().getPlayerShip() && effectLevel > 0) {
                float malus1 = (int) 100f - (mult * 100f);
                
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1,
                    system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
                    "all weapons rate of fire decreased " + (int) Math.round(malus1) + "%", false);
            }
        }
        
        if (!activeRegistry.isEmpty() && (state != State.ACTIVE || !ship.isAlive() || !Global.getCombatEngine().isEntityInPlay(ship))) {
            activeRegistry.clear();
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
        return null;
    }
}
