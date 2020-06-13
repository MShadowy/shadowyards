package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.util.vector.Vector2f;



public class MS_mimeticSheath extends BaseHullMod {
    //grants armor regen, but also reduces maximum armor strength by 20%
    //and also the armor regen dies if too many armor cells are killed
    private static final Map<ShipAPI.HullSize, Float> MAG = new HashMap<>();
    private static final float ARMOR_REDUCTION_MULT = 0.2f;
    
    static {
        MAG.put(ShipAPI.HullSize.FIGHTER, 80f);
        MAG.put(ShipAPI.HullSize.FRIGATE, 70f);
        MAG.put(ShipAPI.HullSize.DESTROYER, 55f);
        MAG.put(ShipAPI.HullSize.CRUISER, 40f);
        MAG.put(ShipAPI.HullSize.CAPITAL_SHIP, 25f);
    }
    
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getArmorBonus().modifyMult(id, 1f - ARMOR_REDUCTION_MULT);
    }
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) {
            return "" + (int) (ARMOR_REDUCTION_MULT * 100f);
        }
        if (index == 1) {
            return "" + (int) (MAG.get(ShipAPI.HullSize.FRIGATE) * 100f);
        }
        if (index == 2) {
            return "" + (int) (MAG.get(ShipAPI.HullSize.DESTROYER) * 100f);
        }
        if (index == 3) {
            return "" + (int) (MAG.get(ShipAPI.HullSize.CRUISER) * 100f);
        }
        if (index == 4) {
            return "" + (int) (MAG.get(ShipAPI.HullSize.CAPITAL_SHIP) * 100f);
        }
        return null;
    }
    
    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return "Must be installed on a Shadowyards ship";
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return (ship.getHullSpec().getHullId().startsWith("ms_"));
    }
    
    @SuppressWarnings("AssignmentToMethodParameter")
    public static Vector2f getCellLocation(ShipAPI ship, float x, float y) {
        x -= ship.getArmorGrid().getGrid().length / 2f;
        y -= ship.getArmorGrid().getGrid()[0].length / 2f;
        float cellSize = ship.getArmorGrid().getCellSize();
        Vector2f cellLoc = new Vector2f();
        float theta = (float) (((ship.getFacing() - 90) / 350f) * (Math.PI * 2));
        cellLoc.x = (float) (x * Math.cos(theta) - y * Math.sin(theta)) * cellSize + ship.getLocation().x;
        cellLoc.y = (float) (x * Math.sin(theta) + y * Math.cos(theta)) * cellSize + ship.getLocation().y;

        return cellLoc;
    }
    
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (Global.getCombatEngine().isPaused() || ship.isHulk()) {
            return;
        }
        
        ArmorGridAPI armor = ship.getArmorGrid();
        
        float[][] cells = armor.getGrid();
        int cellsX = cells.length;
        int cellsY = cells[0].length;
        float newArmor = armor.getArmorValue(cellsX, cellsY);
        float cellSize = armor.getCellSize();
        int totalCells = cellsX * cellsY;
        int deadCells = 0;
        
        if (Float.compare(newArmor, armor.getMaxArmorInCell()) >= 0) {
            return;
        }
        
        if (armor.getArmorFraction(cellsX, cellsY) <= MAG.get(ship.getHullSize())) {
            
        }
    }
}
