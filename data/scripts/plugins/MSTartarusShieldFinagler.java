package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.List;

public class MSTartarusShieldFinagler extends BaseEveryFrameCombatPlugin {

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();

        if (engine != null) {
            for (ShipAPI ship : engine.getShips()) {
                if ("ms_tartarus".equals(ship.getHullSpec().getHullId())) {
                    ShipSystemAPI system = ship.getSystem();
                    //MutableShipStatsAPI stats = ship.getMutableStats();
                    ShieldAPI shield = ship.getShield();

                    if (shield.getArc() < 360) { // this should only be true once per Tartarus deployed
                        // keep shield type, keep upkeep & efficiency (must match values in ship_data.csv), but change max arc to 360 - even if halved by omni shield emitter
                        ship.setShield(shield.getType(), 0.4f, 0.7f, 720.0f);
                        shield = ship.getShield();
                    }

                    // if shields are off or system is on, don't bother doing anything.
                    if (shield.isOn() && !system.isOn()) {
                        float maxShieldArc = 270.0f; // this needs to match what's in the ship data

                        // several special cases for shield-affecting hull mods; if I was being clever, I'd read the modifiers and make this work with all possible hull mods
                        // if omni shields
                        if (ship.getVariant().getHullMods().contains("adaptiveshields") && !ship.getVariant().getHullMods().contains("frontemitter")) {
                            maxShieldArc = maxShieldArc / 2.0f;
                        }
                        // if extended shields
                        if (ship.getVariant().getHullMods().contains("extendedshieldemitter")) {
                            maxShieldArc = maxShieldArc + 60.0f;
                        }
                        float mult = 100.0f;
                        // if accelerated shields, we need to modify unfold rate from no modifier to -200% instead of just -100%
                        if (ship.getVariant().getHullMods().contains("advancedshieldemitter")) {
                            mult = mult * 2.0f;
                        }
                        float currentArc = ship.getShield().getActiveArc();
                        if (currentArc > maxShieldArc) {
                            float targetArc = currentArc - mult * amount;
                            if (targetArc < maxShieldArc) {
                                targetArc = maxShieldArc;
                            }
                            shield.setActiveArc(targetArc);
                        }
                    }
                }
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
