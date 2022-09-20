package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashSet;
import java.util.Set;
import org.lazywizard.lazylib.MathUtils;

public class MS_wolfPack extends BaseHullMod {
    private static final float ROF_BONUS = 0.2f;
    private static final float DAMAGE_BONUS = 0.25f;
    
    private static final float ACTIVE_CHECK = 800f;
            
    private static final String CHECK_MOD = "ms_wolfPack";
    
    private static CombatEngineAPI engine = null;
    
    private static final Set<ShipAPI> wolfPack = new HashSet<>();
    
    private static final String STATIC_ID = "shadowyWolfPack";
    
    private static final String WP_ICON = "graphics/shi/icons/tactical/ms_wPackIcon.png";
    private static final String WP_NAME = "Wolf Pack System";
    
    protected Object STATUSKEY1 = new Object();
    protected Object STATUSKEY2 = new Object();
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return false;
    }
    
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (Global.getCombatEngine().isPaused() || ship.isHulk()) {
            return;
        }
        
        if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
            wolfPack.clear();
        }
        
        //so here we do some stuff, namely looking for other ships with the Wolf Pack hullmod in battle
        //we'll check every ship in a specified range to see if other pack members are near enough to bother tracking
        //and if they get close enough we add them to an active list
        //for each active ship in list we multiply rof and damage per shot
        if (ship.isAlive()) {
            for (ShipAPI pack : engine.getShips()) {
                if (!pack.isAlive()) {
                    continue;
                }
                if (pack == ship) {
                    continue; //don't bother tracking disabled ships, ships that aren't part of the pack, and oneself
                }

                if ((pack.getOwner() == ship.getOwner() && (pack.getHullSpec().getBuiltInMods().contains(CHECK_MOD))) && (MathUtils.getDistance(ship, pack) <= (ACTIVE_CHECK))) 
                {
                    if (!wolfPack.contains(pack)) {
                        wolfPack.add(pack);
                    }
                }

                if (wolfPack.contains(pack) && ((MathUtils.getDistance(ship, pack) > (ACTIVE_CHECK)) || pack.isHulk() || !pack.isAlive())) {
                    wolfPack.remove(pack);
                }
            }
        }
        
        int baseMult = 0;
        if (!wolfPack.isEmpty()) {
            baseMult = wolfPack.size() - 1;
        }
        
        if (baseMult > 0) {
            float fireMult = 1 + (ROF_BONUS * baseMult);
            float damageMult = 1 + (DAMAGE_BONUS * baseMult);
            
            float fireDisplay = 100f * (ROF_BONUS * baseMult);
            float damDisplay = 100f * (DAMAGE_BONUS * baseMult);
            
            ship.getMutableStats().getBallisticRoFMult().modifyMult(STATIC_ID, fireMult);
            ship.getMutableStats().getEnergyRoFMult().modifyMult(STATIC_ID, fireMult);
            ship.getMutableStats().getBeamWeaponDamageMult().modifyMult(STATIC_ID, damageMult);
            ship.getMutableStats().getMissileWeaponDamageMult().modifyMult(STATIC_ID, damageMult);
            
            if (ship == engine.getPlayerShip()) {
                engine.maintainStatusForPlayerShip(STATUSKEY1, WP_ICON, WP_NAME, "Ballistic and Energy RoF increased by "+(int) fireDisplay+"%", true);
                engine.maintainStatusForPlayerShip(STATUSKEY2, WP_ICON, WP_NAME, "Beam and Missile damage increased by "+(int) damDisplay+"%", true);
            }
        }
        if (baseMult == 0) {
            ship.getMutableStats().getBallisticRoFMult().unmodify(STATIC_ID);
            ship.getMutableStats().getEnergyRoFMult().unmodify(STATIC_ID);
            ship.getMutableStats().getBeamWeaponDamageMult().unmodify(STATIC_ID);
            ship.getMutableStats().getMissileWeaponDamageMult().unmodify(STATIC_ID);
        }
        
        if (!ship.isAlive() || engine.isCombatOver() == true) {
            wolfPack.clear();
        }
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        float fDisplay = 100f * ROF_BONUS;
        float dDisplay = 100f * DAMAGE_BONUS;
        
        if (index == 0) return "" + (int) fDisplay + "%";
        if (index == 1) return "" + (int) dDisplay + "%";
        return null;
    }
}
