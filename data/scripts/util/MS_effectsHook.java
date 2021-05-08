/*
Borrowed from MesoTronic with permission
 */
package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_effectsHook extends BaseEveryFrameCombatPlugin {
    private static final float CONCUSSION_SHOCKWAVE_DURATION = 0.25f;
    private static final float CONCUSSION_SHOCKWAVE_MAX_SCALE = 1.5f;
    private static final float CONCUSSION_SHOCKWAVE_MIN_SCALE = 0.25f;
    private static final String DATA_KEY = "MS_effectsHook";
    private static final float FLAK_SHOCKWAVE_DURATION = 0.2f;
    private static final float FLAK_SHOCKWAVE_MAX_SCALE = 0.8f;
    private static final float FLAK_SHOCKWAVE_MIN_SCALE = 0.15f;
    private static final float GAP_DURATION = 0.2f;
    private static final float GAP_MAX_SCALE = 0.8f;
    private static final float GAP_MIN_SCALE = 0.15f;
    private static final float PING_DURATION = 3f;
    private static final float PING_MAX_SCALE = 3.5f;
    private static final float PING_MIN_SCALE = 0.75f;
    private static final float PULSE_DURATION = 1.25f;
    private static final float PULSE_MAX_SCALE = 2.5f;
    private static final float PULSE_MIN_SCALE = 0.5f;
    private static final float EMP_SHOCKWAVE_DURATION = 0.05f;
    private static final float EMP_SHOCKWAVE_MAX_SCALE = 1f;
    private static final float EMP_SHOCKWAVE_MIN_SCALE = 0.2f;
    private static final float SPARK_SHOCKWAVE_DURATION = 0.25f;
    private static final float SPARK_SHOCKWAVE_MAX_SCALE = 1f;
    private static final float SPARK_SHOCKWAVE_MIN_SCALE = 0.2f;
    private static final int SHOCKWAVE_SIZE = 256;
    
    public static void createFlakShockwave(Vector2f location)
    {
        final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
        if (localData == null)
        {
            return;
        }

        final List<Shockwave> shockwaves = localData.shockwaves;

        shockwaves.add(new Shockwave(location, FLAK_SHOCKWAVE_DURATION, FLAK_SHOCKWAVE_MAX_SCALE, FLAK_SHOCKWAVE_MIN_SCALE));
    }

    public static void createShockwave(Vector2f location)
    {
        final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
        if (localData == null)
        {
            return;
        }

        final List<Shockwave> shockwaves = localData.shockwaves;

        shockwaves.add(new Shockwave(location, CONCUSSION_SHOCKWAVE_DURATION, CONCUSSION_SHOCKWAVE_MAX_SCALE, CONCUSSION_SHOCKWAVE_MIN_SCALE));
    }
    
    public static void createEMPShockwave(Vector2f location)
    {
        final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
        if (localData == null)
        {
            return;
        }

        final List<Shockwave> shockwaves = localData.shockwaves;

        shockwaves.add(new Shockwave(location, EMP_SHOCKWAVE_DURATION, EMP_SHOCKWAVE_MAX_SCALE, EMP_SHOCKWAVE_MIN_SCALE));
    }
    
    public static void createPing(Vector2f location, Vector2f velocity)
    {
        final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
        if (localData == null)
        {
            return;
        }

        final List<tagPing> pings = localData.pings;

        pings.add(new tagPing(location, velocity, PING_DURATION, PING_MAX_SCALE, PING_MIN_SCALE));
    }
    
    public static void createPulse(Vector2f location)
    {
        final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
        if (localData == null)
        {
            return;
        }

        final List<swacsPulse> pulses = localData.pulses;

        pulses.add(new swacsPulse(location, PULSE_DURATION, PULSE_MAX_SCALE, PULSE_MIN_SCALE));
    }
    
    /*Animation handler for the Shamash projectile explosion
    Splat down horizon, which remains the same size but fades out
    Over that is the Gap, which shrinks rapidly and keeps it opacity
    Over that is the anamorphic flare; stretches horizontally and fades out and a hit particle*/
    public static void createRift (Vector2f location) {
        final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
        if (localData == null)
        {
            return;
        }
        
        final List<Rift> rifts = localData.rifts;
        
        rifts.add(new Rift(location, GAP_DURATION, GAP_MAX_SCALE, GAP_MIN_SCALE));
    }
    
    public static void createSparkShockwave(Vector2f location)
    {
        final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
        if (localData == null)
        {
            return;
        }

        final List<Shockwave> shockwaves = localData.shockwaves;

        shockwaves.add(new Shockwave(location, SPARK_SHOCKWAVE_DURATION, SPARK_SHOCKWAVE_MAX_SCALE, SPARK_SHOCKWAVE_MIN_SCALE));
    }
    private CombatEngineAPI engine; // Assigned per combat

    @Override
    public void advance(float amount, List<InputEventAPI> events)
    {
        if (engine == null)
        {
            return;
        }

        if (engine.isPaused())
        {
            return;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final List<Shockwave> shockwaves = localData.shockwaves;

        Iterator<Shockwave> iter = shockwaves.iterator();
        while (iter.hasNext())
        {
            Shockwave wave = iter.next();

            wave.lifespan -= amount;
            if (wave.lifespan <= 0f)
            {
                iter.remove();
                continue;
            }

            wave.alpha = wave.lifespan / wave.maxLifespan;
            wave.scale = wave.minScale + (((wave.maxLifespan - wave.lifespan) / wave.maxLifespan) * (wave.maxScale - wave.minScale));
        }
        
        final List<tagPing> pings = localData.pings;
        
        Iterator<tagPing> iterB = pings.iterator();
        while (iterB.hasNext())
        {
            tagPing ping = iterB.next();
            
            ping.lifespan -= amount;
            if (ping.lifespan <= 0f)
            {
                iterB.remove();
                continue;
            }
            
            ping.alpha = ping.lifespan / ping.maxLifespan;
            ping.scale = ping.minScale + (((ping.maxLifespan - ping.lifespan) / ping.maxLifespan) * (ping.maxScale - ping.minScale));
        }
        
        final List<swacsPulse> pulses = localData.pulses;
        
        Iterator<swacsPulse> iterC = pulses.iterator();
        while (iterC.hasNext()) 
        {
            swacsPulse pulse = iterC.next();
            
            pulse.lifespan -= amount;
            if (pulse.lifespan <= 0f)
            {
                iterC.remove();
                continue;
            }
            
            pulse.alpha = pulse.lifespan / pulse.maxLifespan;
            pulse.scale = pulse.minScale + (((pulse.maxLifespan - pulse.lifespan) / pulse.maxLifespan) * (pulse.maxScale - pulse.minScale));
        }
        
        final List<Rift> rifts = localData.rifts;
        
        Iterator<Rift> iterD = rifts.iterator();
        while (iterD.hasNext())
        {
            Rift rift = iterD.next();
            
            rift.lifespan -= amount;
            if (rift.lifespan <= 0f)
            {
                iterD.remove();
                continue;
            }
            
            rift.alpha = rift.lifespan / rift.maxLifespan;
            rift.scale = rift.maxScale - (((rift.maxLifespan - rift.lifespan) / rift.maxLifespan) * (rift.maxScale - rift.minScale));
        }
    }

    @Override
    public void init(CombatEngineAPI engine)
    {
        this.engine = engine;
        engine.getCustomData().put(DATA_KEY, new LocalData());
    }

    @Override
    public void renderInWorldCoords(ViewportAPI viewport)
    {
        if (engine == null)
        {
            return;
        }
        
        

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final List<Shockwave> shockwaves = localData.shockwaves;
        final List<tagPing> pings = localData.pings;
        final List<swacsPulse> pulses = localData.pulses;
        final List<Rift> rifts = localData.rifts;

        // Concussion shockwave sprite
        for (Shockwave wave : shockwaves)
        {
            SpriteAPI waveSprite = Global.getSettings().getSprite("concussion", "flareFlakWave");
            if (waveSprite != null)
            {
                waveSprite.setAlphaMult(wave.alpha);
                waveSprite.setAdditiveBlend();
                waveSprite.setAngle(wave.facing);
                waveSprite.setSize(wave.scale * SHOCKWAVE_SIZE, wave.scale * SHOCKWAVE_SIZE);
                waveSprite.renderAtCenter(wave.location.x, wave.location.y);
            }
        }
        
        for (tagPing ping : pings) {
            SpriteAPI waveSprite = Global.getSettings().getSprite("ping", "tagPing");
            if (waveSprite != null)
            {
                waveSprite.setAlphaMult(ping.alpha);
                waveSprite.setAdditiveBlend();
                waveSprite.setAngle(ping.facing);
                waveSprite.setSize(ping.scale * SHOCKWAVE_SIZE, ping.scale * SHOCKWAVE_SIZE);
                waveSprite.renderAtCenter(ping.location.x, ping.location.y);
            }
        }
        
        for (swacsPulse pulse : pulses) {
            SpriteAPI waveSprite = Global.getSettings().getSprite("ping", "swacsPing");
            if (waveSprite != null)
            {
                waveSprite.setAlphaMult(pulse.alpha);
                waveSprite.setAdditiveBlend();
                waveSprite.setAngle(pulse.facing);
                waveSprite.setSize(pulse.scale * SHOCKWAVE_SIZE, pulse.scale * SHOCKWAVE_SIZE);
                waveSprite.renderAtCenter(pulse.location.x, pulse.location.y);
            }
        }
        
        for (Rift rift : rifts) {
            SpriteAPI eventHorizonSprite = Global.getSettings().getSprite("misc", "ms_eventHorizon");
            SpriteAPI gapSprite = Global.getSettings().getSprite("misc", "ms_phaseSpaceRift" + MathUtils.getRandomNumberInRange(1, 5));
            //SpriteAPI gapSprite = Global.getSettings().getSprite("misc", vistas.iterator().next().toString());
            SpriteAPI flareSprite = Global.getSettings().getSprite("flare", "nidhoggr_ALF");
            if (eventHorizonSprite != null)
            {
                eventHorizonSprite.setAlphaMult(rift.alpha);
                eventHorizonSprite.setAdditiveBlend();
                eventHorizonSprite.setAngle(rift.facing);
                eventHorizonSprite.setSize(150, 150);
                eventHorizonSprite.renderAtCenter(rift.location.x, rift.location.y);
            }
            
            if (gapSprite != null)
            {
                gapSprite.setAlphaMult(0.8f);
                gapSprite.setAdditiveBlend();
                gapSprite.setAngle(rift.facing);
                gapSprite.setSize(rift.scale * SHOCKWAVE_SIZE, rift.scale * SHOCKWAVE_SIZE);
                gapSprite.renderAtCenter(rift.location.x, rift.location.y);
            }
            
            
            if (flareSprite != null)
            {
                flareSprite.setAlphaMult(rift.alpha);
                flareSprite.setAdditiveBlend();
                flareSprite.setAngle(0f);
                flareSprite.renderAtCenter(rift.location.x, rift.location.y);
            }
        }
    }

    private static final class LocalData
    {
        final List<Shockwave> shockwaves = new LinkedList<>();
        final List<tagPing> pings = new LinkedList<>();
        final List<swacsPulse> pulses = new LinkedList<>();
        final List<Rift> rifts = new LinkedList<>();
    }

    static class Shockwave
    {
        float alpha;
        final float facing;
        float lifespan;
        final Vector2f location;
        float maxLifespan;
        float maxScale;
        float minScale;
        float scale;

        Shockwave(Vector2f location, float duration, float maxScale, float minScale)
        {
            this.location = new Vector2f(location);
            alpha = 1f;
            facing = (float) Math.random() * 360f;
            maxLifespan = duration;
            lifespan = maxLifespan;
            this.maxScale = maxScale;
            this.minScale = minScale;
            scale = minScale;
        }
    }
    
    static class tagPing
    {
        float alpha;
        final float facing;
        float lifespan;
        final Vector2f location;
        final Vector2f velocity;
        float maxLifespan;
        float maxScale;
        float minScale;
        float scale;

        tagPing(Vector2f location, Vector2f velocity, float duration, float maxScale, float minScale)
        {
            this.location = new Vector2f(location);
            this.velocity = new Vector2f(velocity);
            alpha = 1f;
            facing = (float) Math.random() * 360f;
            maxLifespan = duration;
            lifespan = maxLifespan;
            this.maxScale = maxScale;
            this.minScale = minScale;
            scale = minScale;
        }
    }
    
    static class swacsPulse
    {
        float alpha;
        final float facing;
        float lifespan;
        final Vector2f location;
        float maxLifespan;
        float maxScale;
        float minScale;
        float scale;

        swacsPulse(Vector2f location, float duration, float maxScale, float minScale)
        {
            this.location = new Vector2f(location);
            alpha = 1f;
            facing = (float) Math.random() * 360f;
            maxLifespan = duration;
            lifespan = maxLifespan;
            this.maxScale = maxScale;
            this.minScale = minScale;
            scale = minScale;
        }
    }
    
    static class Rift
    {
        float alpha;
        final float facing;
        float lifespan;
        final Vector2f location;
        float maxLifespan;
        float maxScale;
        float minScale;
        float scale;
        
        Rift(Vector2f location, float duration, float maxScale, float minScale)
        {
            this.location = new Vector2f(location);
            alpha = 1f;
            facing = (float) Math.random() * 360f;
            maxLifespan = duration;
            lifespan = maxLifespan;
            this.maxScale = maxScale;
            this.minScale = minScale;
            scale = minScale;
        }
    }
}
