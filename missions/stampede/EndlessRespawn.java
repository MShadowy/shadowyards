/*Original scripting framework by Debido
 Ship revival code originally ship boarding code by Xenoargh
 concurrency issues/optimisation rework by Dark Revenant
 Designed for use with the stampede mission for SHI


 This mission will spawn MAX_RESPAWN amount of enemies, then end combat.
 This mission well have a maximum of MAX_BUFFALO buffalos on the map at any one time

 */
package data.missions.stampede;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatAssignmentType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class EndlessRespawn extends BaseEveryFrameCombatPlugin {

    private static CombatEngineAPI engine; //reference to the current engine
    //private static final float INTERVAL = 5f;
    //private final IntervalUtil tracker = new IntervalUtil(INTERVAL, INTERVAL); //tracker used for main resurrection
    private float mapX = 0f;
    private float mapY = 0f;
    private int countingCows = 0;
    private boolean combatOver = false;
    private static final int MAX_RESPAWN = 300;
    private static final int MAX_BUFFALO = 30;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
            mapX = engine.getMapWidth();
            mapY = engine.getMapHeight();
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        //Here we're checking to make sure we're in a Campaign mission context; if not, halt!
        if (Global.getSector().getPlayerFleet() != null) {
            return;
        }

        if (!combatOver) {
            if (countingCows >= MAX_RESPAWN) {
                combatOver = true;
                CombatFleetManagerAPI eManager = engine.getFleetManager(FleetSide.ENEMY);
                eManager.getTaskManager(combatOver).orderFullRetreat();
                return;
                //engine.endCombat(3f);
            }
            engine.setDoNotEndCombat(true);

            //if (tracker.intervalElapsed()) { //1. If the interval period has elapsed
            List<ShipAPI> ships = engine.getShips(); //2. Get all ships in combat

            for (ShipAPI ship : ships) { //3. For each ship in our list of ships
                if (ship == null) { //4. If the ship is null, continue
                    continue;
                }

                if (ship.isAlive() && ship.getOriginalOwner() == 1 && !ship.isShuttlePod() && !ship.isWingLeader() && !ship.isDrone()) {
                    CombatFleetManagerAPI eManager = engine.getFleetManager(FleetSide.ENEMY);
                    if (eManager.getTaskManager(combatOver).getAssignmentFor(ship) != null && eManager.getTaskManager(combatOver).getAssignmentFor(ship).getType() == CombatAssignmentType.RETREAT) {
                        eManager.getTaskManager(combatOver).orderSearchAndDestroy(eManager.getDeployedFleetMember(ship), false);
                    }
                }

                if (ship.isHulk() == true && !ship.isFighter() && ship.getOriginalOwner() == 1) { //5. if the ship is a hulk
                    if (ships.size() < MAX_BUFFALO) {
                        int owner = ship.getOriginalOwner(); //20. get owner value
                        Vector2f safeSpawnLoc = getSafeSpawn(owner);
                        Vector2f safeSpawnLoc2 = getSafeSpawn(owner);

                        String variantID = ship.getVariant().getHullVariantId();

                        ShipAPI randShip = engine.getShips().get(0); //let's just grab a ship, any ship, to destroy our reviveTarget ship in a moment

                        if (owner == 1) {

                            //increment number of buffalo killed
                            ++countingCows;
                            engine.addFloatingText(engine.getPlayerShip().getLocation(), "You have killed " + countingCows + " Buffalo!", 30f, Color.yellow, engine.getPlayerShip(), 1f, 2f);

                            //spawn 2 buffalo
                            ShipAPI spawned = engine.getFleetManager(FleetSide.ENEMY).spawnShipOrWing(variantID, safeSpawnLoc, 270f, 3f);
                            ShipAPI spawned2 = engine.getFleetManager(FleetSide.ENEMY).spawnShipOrWing(variantID, safeSpawnLoc2, 270f, 3f);

                            spawned.setCurrentCR(ship.getCurrentCR()); //instead of a random CR, sets it to the current CR of the ship
                            spawned2.setCurrentCR(ship.getCurrentCR()); //instead of a random CR, sets it to the current CR of the ship

                            engine.getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(spawned).getMember().getCaptain().setPersonality(Personalities.AGGRESSIVE);
                            engine.getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(spawned2).getMember().getCaptain().setPersonality(Personalities.AGGRESSIVE);

                            spawned.getShipAI().forceCircumstanceEvaluation();
                            spawned2.getShipAI().forceCircumstanceEvaluation();

                        }
                //the remove entity method prevents missions from ending as well
                        //engine.removeEntity(ship);

                        //the damage method allows combat to end, but we have the setDoNotEndCombat(true) preventing that   
                        engine.applyDamage(ship, ship.getLocation(), 1000000f, DamageType.ENERGY, 0f, true, true, randShip);

                    } else {
                        //let's do some cleanup otherwise of the dead hulks/ destroying them
                        ShipAPI randShip = engine.getShips().get(0); //let's just grab a ship, any ship, to destroy our reviveTarget ship in a moment
                        engine.applyDamage(ship, ship.getLocation(), 1000000f, DamageType.ENERGY, 0f, true, true, randShip);
                    }
                }
            }
        }
    } //ADAVNCE

    @Override
    public void init(CombatEngineAPI engine) {
    }

    private Vector2f getSafeSpawn(int side) {
        Vector2f spawnLocation = new Vector2f();

        spawnLocation.x = MathUtils.getRandomNumberInRange(-mapX / 2, mapX / 2);
        if (side == 100 || side == 0) {
            spawnLocation.y = (-mapY / 2f);

        } else {
            spawnLocation.y = mapY / 2;
        }

        return spawnLocation;
    }
}
