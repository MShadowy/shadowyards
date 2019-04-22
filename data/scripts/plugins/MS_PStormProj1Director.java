package data.scripts.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.ArrayList;
import java.util.List;

class MS_PStormProj1Director extends BaseEveryFrameCombatPlugin {
    
    private static final String PROJ_ID = "ms_shieldkeeper_spark";
    private static final float SPARK_SPEED = 200f;
    private static final float MIN_SPARK_LIFETIME = 2f;
    private static final float MAX_SPARK_LIFETIME = 4f;
    // How often should the spark change direction?
    private static final float MIN_SPARK_TURN_INTERVAL = 0.5f;
    private static final float MAX_SPARK_TURN_INTERVAL = 1.5f;
    // What is the max change in facing per turn?
    private static final float MAX_SPARK_FACING_CHANGE = 360f;
    private static final float HALF_SPARK_FACING_CHANGE = MAX_SPARK_FACING_CHANGE / 2;
    // Keeps track of all active sparks on the battle map
    private static final List SPARKS = new ArrayList(24);
    private CombatEngineAPI engine;
    
    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        SPARKS.clear();
    }
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine.isPaused()) {
            return;
        }
        
        SparkData spark;
        
    }
    
    private static final class SparkData {
        private float intendedFacing;
        private final DamagingProjectileAPI spark;
        private final IntervalUtil nextTurn, fade;
        
        private SparkData(DamagingProjectileAPI spark) {
            this.spark = spark;
            spark.setCollisionClass(CollisionClass.NONE);
            chooseNextFacing();
            nextTurn = new IntervalUtil(MIN_SPARK_TURN_INTERVAL, MAX_SPARK_TURN_INTERVAL);
            fade = new IntervalUtil(MIN_SPARK_LIFETIME, MAX_SPARK_LIFETIME);
        }
        
        public void adjustFacing(float amount) {
            float facingChange = (intendedFacing - spark.getFacing()) * amount * 2f;
            spark.setFacing(spark.getFacing() + facingChange);
            spark.getVelocity().set(SPARK_SPEED, spark.getFacing());
        }
        
        public void chooseNextFacing()
        {
            intendedFacing = spark.getFacing();
            intendedFacing += (MAX_SPARK_FACING_CHANGE * Math.random())
                    - HALF_SPARK_FACING_CHANGE;
        }
        
        //unlike the original spark manager we're not worried about doing damage
        public void advance(float amount) {
            fade.advance(amount);
            if (fade.intervalElapsed()) {
                spark.isFading();
            }
            
            nextTurn.advance(amount);
            if (nextTurn.intervalElapsed()) {
                chooseNextFacing();
            }
            
            adjustFacing(amount);
        }
    }
    
    @Override
    public void renderInUICoords(ViewportAPI viewport) {
    }
     
    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {  
    }
}
