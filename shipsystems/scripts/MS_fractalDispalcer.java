/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import org.lwjgl.util.vector.Vector2f;

public class MS_fractalDispalcer implements ShipSystemStatsScript {
    
    private static final Vector2f ZERO = new Vector2f();
    
    @Override
    public void apply (MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        // instanceof also acts as a null check
        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }
        
        ShipAPI ship = (ShipAPI) stats.getEntity();
        
    }

    @Override
    public StatusData getStatusData(int index, State state, float f) {
        if (state == State.IN) {
            if (index == 0) {
                return new StatusData("fractal displacer active", false);
            }
        }

        return null;
    }

    @Override
    public void unapply(MutableShipStatsAPI mssapi, String string) {}
}
