package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.BaseHullMod;

public class MS_marcom extends BaseHullMod {

    public static final float MARCOM_EFFECT = 33f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableCharacterStatsAPI stats, String id, MutableShipStatsAPI hull, ShipAPI ship) {
        stats.getMarineEffectivnessMult().modifyPercent(id, MARCOM_EFFECT);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + (int) MARCOM_EFFECT + "%";
        }
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return false;
    }
}
