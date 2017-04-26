/*
By Tartiflette
 */
package data.scripts.util;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

    

public class CustomMissileTargetingTool {
    
    /**
     * @param missile
     * The missile concerned.
     * 
     * @param randomTarget
     * Does the missile find a random target or tries to hit the ship's one?
     * 
     *      0: No random target seeking, 
     * If the launching ship has a valid target, the missile will pursue it.
     * If there is no target, it will check for an unselected cursor target.
     * If there is none, it will pursue its closest valid threat.    
     *   
     *      1: Local random target, 
     * If the ship has a target, the missile will pick a random valid threat 
     * around that one. 
     * If the ship has none, the missile will pursue a random valid threat 
     * around the cursor, or itself in AI control.   
     * 
     *      2: Full random, 
     * The missile will always seek a random valid threat around itself.
     * 
     * @param antiFighter
     * INCOMPATIBLE WITH ASSAULT
     * Prioritize hitting fighters and drones (if false, the missile will 
     * still be able to target fighters but not drones) 
     * 
     * @param assault
     * INCOMPATIBLE WITH ANTI-FIGHTER
     * Target the biggest threats first.
     * 
     * Both targeting behavior can be false for a missile that always get the 
     * ship's target or the closest one.
     * 
     * @param maxRange
     * Maximum range of the missile.
     * 
     * @param maxSearchRange
     * Range in which the missile seek a target in game units.
     * 
     * @param searchCone
     * Angle in witch the missile will seek the target. Negative to ignore.
     * 
     * @return 
     */
    
    public static CombatEntityAPI assignTarget(MissileAPI missile, Integer randomTarget, boolean antiFighter, boolean assault, Float maxRange, Float maxSearchRange, Float searchCone){
        
        ShipAPI theTarget=null;        
        ShipAPI source = missile.getSource();        
        ShipAPI currentTarget;
        
        //check for a target from its source
        if(source != null
                && source.getShipTarget() != null
                && source.getShipTarget() instanceof ShipAPI
                && source.getShipTarget().getOwner() != missile.getOwner()
                ){
            currentTarget=source.getShipTarget();
        } else {
            currentTarget=null;
        }
        
        //random target selection
        if (randomTarget>0){  
            //random mode 1: the missile will look for a target around itsef
            Vector2f location = missile.getLocation();   
            //random mode 2: if its source has a target selected, it will look for random one around that point
            if( randomTarget<2){                                     
                if(currentTarget != null 
                        && currentTarget.isAlive()
                        && MathUtils.isWithinRange(missile, currentTarget, maxRange)
                        ){
                    location = currentTarget.getLocation();
                } else if (source != null
                        && source.getMouseTarget()!=null){
                    location=source.getMouseTarget();
                }
            }
            //fetch the right kind of target
            if(antiFighter){
                theTarget = getRandomFighterTarget(location, missile, maxRange, searchCone);
            } else if(assault){
                theTarget = getRandomLargeTarget(location, missile, maxRange, searchCone);
            } else {
                theTarget = getAnyTarget(location, missile, maxRange, searchCone);
            }    
        //non random targeting    
        } else {
            if(source!=null){
                //ship target first
                if(currentTarget!=null
                        && currentTarget.isAlive()
                        && currentTarget.getOwner()!=missile.getOwner()
                        && !(antiFighter && !(currentTarget.isDrone() && currentTarget.isFighter()))
                        && !(assault && (currentTarget.isDrone() || currentTarget.isFighter()))
                        && !(searchCone>0 && MathUtils.getShortestRotation(
                                missile.getFacing(),
                                VectorUtils.getAngle(missile.getLocation(), currentTarget.getLocation())
                                )>searchCone/2)
                        ){
                    theTarget=currentTarget;                
                } else {
                    //or cursor target if there isn't one
                    List<ShipAPI> mouseTargets = CombatUtils.getShipsWithinRange(source.getMouseTarget(), 100f);
                    if (!mouseTargets.isEmpty()) {
                        Collections.sort(mouseTargets, new CollectionUtils.SortEntitiesByDistance(source.getMouseTarget()));
                        for (ShipAPI tmp : mouseTargets) {
                            if (tmp.isAlive() 
                                    && tmp.getOwner() != missile.getOwner()
                                    && !(antiFighter && !(tmp.isDrone() && tmp.isFighter()))
                                    && !(assault && (tmp.isDrone() || tmp.isFighter() || tmp.isFrigate()))
                                    && !(searchCone>0 && MathUtils.getShortestRotation(
                                            missile.getFacing(),
                                            VectorUtils.getAngle(missile.getLocation(), tmp.getLocation())
                                            )>searchCone/2)
                                    ) {
                                theTarget=tmp;
                                break;
                            }
                        }
                    }                
                }
            }
            //still no valid target? lets try the closest one
            //most of the time a ship will have a target so that doesn't need to be perfect.
            if(theTarget==null){
                List<ShipAPI> closeTargets = AIUtils.getNearbyEnemies(missile, maxSearchRange);
                if (!closeTargets.isEmpty()) {
                    Collections.sort(closeTargets, new CollectionUtils.SortEntitiesByDistance(missile.getLocation()));
                    if (assault){   //assault missiles will somewhat prioritize toward bigger threats even if there is a closer small one, and avoid fighters and drones.
                        for (ShipAPI tmp : closeTargets) {
                            if (tmp.isAlive() 
                                    && tmp.getOwner() != missile.getOwner()
                                    && !(searchCone>0 && MathUtils.getShortestRotation(
                                            missile.getFacing(),
                                            VectorUtils.getAngle(missile.getLocation(), tmp.getLocation())
                                            )>searchCone/2)
                                    ) {
                                if (tmp.isCapital() || tmp.isCruiser()){
                                    theTarget=tmp;
                                    break;
                                } else if (tmp.isDestroyer() && Math.random()>0.5){
                                    theTarget=tmp;
                                    break;
                                } else if (tmp.isDestroyer() && Math.random()>0.75){
                                    theTarget=tmp;
                                    break;
                                } else if (!tmp.isDrone() && !tmp.isFighter() && Math.random()>0.95){
                                    theTarget=tmp;
                                    break;
                                }
                            }
                        }
                    }else if(antiFighter){    //anti-fighter missile will target the closest drone or fighter
                        for (ShipAPI tmp : closeTargets) {
                            if (tmp.isAlive() 
                                    && tmp.getOwner() != missile.getOwner()
                                    && (tmp.isDrone() || tmp.isFighter())                                    
                                    && !(searchCone>0 && MathUtils.getShortestRotation(
                                            missile.getFacing(),
                                            VectorUtils.getAngle(missile.getLocation(), tmp.getLocation())
                                            )>searchCone/2)
                                    ) {
                                theTarget=tmp;
                                break;
                            }
                        }
                    }else{  //non assault, non anti-fighter missile target the closest non-drone ship
                        for (ShipAPI tmp : closeTargets) {
                            if (tmp.isAlive() 
                                    && tmp.getOwner() != missile.getOwner()
                                    && !tmp.isDrone()
                                    && !(searchCone>0 && MathUtils.getShortestRotation(
                                            missile.getFacing(),
                                            VectorUtils.getAngle(missile.getLocation(), tmp.getLocation())
                                            )>searchCone/2)
                                    ) {  
                                theTarget=tmp;
                                break;
                            }
                        }
                    }
                } 
            }
        }        
        return theTarget;
    }
    
    //Random picker for fighters and drones
    public static ShipAPI getRandomFighterTarget(Vector2f location, MissileAPI missile, Float maxRange, Float searchCone){
        ShipAPI select=null;
        Map<Integer, ShipAPI> PRIORITYLIST = new HashMap<>();
        Map<Integer, ShipAPI> OTHERSLIST = new HashMap<>();
        int i=1, u=1;
        List<ShipAPI> potentialTargets = CombatUtils.getShipsWithinRange(location, maxRange);
        if (!potentialTargets.isEmpty()) {
            for (ShipAPI tmp : potentialTargets) {
                if (tmp.isAlive() 
                        && tmp.getOwner() != missile.getOwner()
                        && !(searchCone>0 && MathUtils.getShortestRotation(
                                missile.getFacing(),
                                VectorUtils.getAngle(missile.getLocation(), tmp.getLocation())
                                )>searchCone/2)
                        ) {
                    if (tmp.isFighter() || tmp.isDrone()){
                        PRIORITYLIST.put(i, tmp);
                        i++;
                    } else {                            
                        OTHERSLIST.put(u, tmp);
                        u++;
                    }
                }
            }
            if (!PRIORITYLIST.isEmpty()){
                int chooser=Math.round((float)Math.random()*(i-1)+0.5f);
                select=PRIORITYLIST.get(chooser);
            } else if (!OTHERSLIST.isEmpty()){                    
                int chooser=Math.round((float)Math.random()*(u-1)+0.5f);
                select=OTHERSLIST.get(chooser);
            }
        }
        return select;
    }
    
    //Random target selection strongly weighted toward bigger threats in range
    public static ShipAPI getRandomLargeTarget(Vector2f location, MissileAPI missile, Float maxRange, Float searchCone){
        ShipAPI select=null;
        Map<Integer, ShipAPI> PRIORITY1 = new HashMap<>();
        Map<Integer, ShipAPI> PRIORITY2 = new HashMap<>();
        Map<Integer, ShipAPI> PRIORITY3 = new HashMap<>();
        Map<Integer, ShipAPI> PRIORITY4 = new HashMap<>();
        Map<Integer, ShipAPI> OTHERSLIST = new HashMap<>();
        int i=1, u=1, v=1, x=1, y=1;
        List<ShipAPI> potentialTargets = CombatUtils.getShipsWithinRange(location, maxRange);
        if (!potentialTargets.isEmpty()) {
            for (ShipAPI tmp : potentialTargets) {
                if (tmp.isAlive() 
                        && tmp.getOwner() != missile.getOwner()
                        && !tmp.isDrone()
                        && !(searchCone>0 && MathUtils.getShortestRotation(
                                missile.getFacing(),
                                VectorUtils.getAngle(missile.getLocation(), tmp.getLocation())
                                )>searchCone/2)
                        ) {
                    if (tmp.isCapital()){
                        PRIORITY1.put(i, tmp);
                        i++;
                        PRIORITY2.put(u, tmp);
                        u++;
                        PRIORITY3.put(x, tmp);
                        x++;
                        PRIORITY4.put(v, tmp);
                        v++;
                        OTHERSLIST.put(y, tmp);
                        y++;
                    } else if (tmp.isCruiser()){
                        PRIORITY2.put(u, tmp);
                        u++;
                        PRIORITY3.put(x, tmp);
                        x++;
                        PRIORITY4.put(v, tmp);
                        v++;
                        OTHERSLIST.put(y, tmp);
                        y++;
                    } else if (tmp.isDestroyer()){
                        PRIORITY3.put(x, tmp);
                        x++;
                        PRIORITY4.put(v, tmp);
                        v++;
                        OTHERSLIST.put(y, tmp);
                        y++;
                    } else if (tmp.isFrigate()){
                        PRIORITY4.put(v, tmp);
                        v++;
                        OTHERSLIST.put(y, tmp);
                        y++;
                    } else {
                        OTHERSLIST.put(y, tmp);
                        y++;
                    }
                }
            }
            if (!PRIORITY1.isEmpty() && Math.random()>0.8f){
                int chooser=Math.round((float)Math.random()*(i-1)+0.5f);
                select=PRIORITY1.get(chooser);
            } else if (!PRIORITY2.isEmpty() && Math.random()>0.8f){
                int chooser=Math.round((float)Math.random()*(u-1)+0.5f);
                select=PRIORITY2.get(chooser);
            } else if (!PRIORITY3.isEmpty() && Math.random()>0.8f){
                int chooser=Math.round((float)Math.random()*(x-1)+0.5f);
                select=PRIORITY3.get(chooser);
            } else if (!PRIORITY4.isEmpty() && Math.random()>0.8f){
                int chooser=Math.round((float)Math.random()*(v-1)+0.5f);
                select=PRIORITY4.get(chooser);
            } else if (!OTHERSLIST.isEmpty()){                    
                int chooser=Math.round((float)Math.random()*(y-1)+0.5f);
                select=OTHERSLIST.get(chooser);
            }
        }
        return select;
    }

    //Pure random target picker
    public static ShipAPI getAnyTarget(Vector2f location, MissileAPI missile, Float maxRange, Float searchCone){
        ShipAPI select=null;
        Map<Integer, ShipAPI> TARGETLIST = new HashMap<>();
        int i=1;
        List<ShipAPI> potentialTargets = CombatUtils.getShipsWithinRange(location, maxRange);
        if (!potentialTargets.isEmpty()) {
            for (ShipAPI tmp : potentialTargets) {
                if (tmp.isAlive() 
                        && tmp.getOwner() != missile.getOwner()
                        && !(searchCone>0 && MathUtils.getShortestRotation(
                                missile.getFacing(),
                                VectorUtils.getAngle(missile.getLocation(), tmp.getLocation())
                                )>searchCone/2)
                        && !tmp.isDrone()
                        ){
                    TARGETLIST.put(i, tmp);
                    i++;                        
                }
            }
            if (!TARGETLIST.isEmpty()){
                int chooser=Math.round((float)Math.random()*(i-1)+0.5f);
                select=TARGETLIST.get(chooser);
            }
        }
        return select;
    }
}