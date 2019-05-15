package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class MS_dumper implements EveryFrameWeaponEffectPlugin {
    //simple and straightforward; removes 20% of the ships maximum flux
    private int last_weapon_ammo = 0;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }
        
        int weapon_ammo = weapon.getAmmo();
        float toDump = weapon.getShip().getFluxTracker().getMaxFlux() * 0.2f;
        if (weapon_ammo < last_weapon_ammo) {
            weapon.getShip().getFluxTracker().decreaseFlux(toDump);
        }
        last_weapon_ammo = weapon_ammo;
    }
}
