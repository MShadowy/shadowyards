package data.scripts.util;

import java.awt.Color;
import java.util.Arrays;
import java.util.EnumSet;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Noise;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

public class MS_NebulizerPlugin extends BaseCombatLayeredRenderingPlugin {
    
    public static class NebulaParams implements Cloneable {
        public float fadeIn = 0.15f;
	public float fadeOut = 0.7f;
	public float spawnHitGlowAt = 0f;
	public float hitGlowSizeMult = 0.75f;
	public float radius = 20f;
        public float thickness = 25f;
        public float noiseMag = 1f;
        public float noisePeriod = 0.1f;
        public boolean withHitGlow = true;
        public Color color = new Color(255, 145, 215, 185);
        public Color underglow = new Color(215, 115, 255, 185);
        public Color invert = null;
        
        public NebulaParams() {
        }
        
        public NebulaParams(float radius, Color color) {
            super();
            this.radius = radius;
            this.color = color;
        }
        
        @Override
        protected NebulaParams clone() {
            try {
                return (NebulaParams) super.clone();
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }
    }
    
    protected FaderUtil fader;
    protected SpriteAPI atmosphereTex;
    
    protected float [] noise;
    protected float [] noise1;
    protected float [] noise2;
    
    protected NebulaParams p;
    
    protected int segments;
    protected float noiseElapsed = 0f;
    
    protected boolean spawnedHitGlow = false;
    
    public MS_NebulizerPlugin(NebulaParams p) {
        this.p = p;
    }
    
    @Override
    public float getRenderRadius() {
	return p.radius + 500f;
    }
    
    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return EnumSet.of(CombatEngineLayers.ABOVE_PARTICLES_LOWER, CombatEngineLayers.ABOVE_PARTICLES);
    }
    
    @Override
    public void advance(float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused()) return;
        
        fader.advance(amount);
        
        if (p.noiseMag > 0) {
            noiseElapsed += amount;
            if (noiseElapsed > p.noisePeriod) {
                noiseElapsed = 0;
                noise1 = Arrays.copyOf(noise2, noise2.length);
                noise2 = Noise.genNoise(segments, p.noiseMag);
            }
            float rander = noiseElapsed / p.noisePeriod;
            for (int i = 0; i < noise.length; i++) {
                float n1 = noise1[i];
                float n2 = noise2[i];
                noise[i] = n1 + (n2 - n1) * rander;
            }
        }
        
        if (!p.withHitGlow) return;
        
        float glowSpawnAt = 1f;
        if (!spawnedHitGlow && (!fader.isFadingIn() || fader.getBrightness() >= p.spawnHitGlowAt)) {
            float size = Math.min(p.radius * 7f, p.radius + 150f);
            float coreSize = Math.max(size, p.radius * 4f);
            if (coreSize > size) size = coreSize;
            
            size *= p.hitGlowSizeMult;
            coreSize *= p.hitGlowSizeMult;
            
            Vector2f point = entity.getLocation();
            Vector2f vel = entity.getVelocity();
            float dur = fader.getDurationOut() * glowSpawnAt;
            engine.addHitParticle(point, vel, size * 3f, 1f, dur, p.color);
            engine.addHitParticle(point, vel, coreSize * 1.5f, 1f, dur, Color.WHITE);
            
            Color invert = p.color;
            if (p.invert != null) invert = p.invert;
            Color c = new Color(255 - invert.getRed(), 255 - invert.getGreen(), 
                    255 - invert.getBlue(), 127);
            c = Misc.interpolateColor(c, Color.WHITE, 0.4f);
            
            float durMult = 1f;
            for (int i = 0; i < 7; i ++) {
                dur = 4f + 4f * (float) Math.random();
                
                dur *= durMult;
                dur *= 0.5f;
                
                float nSize = size * 1f;
                Vector2f loc = Misc.getPointAtRadius(point, nSize * 0.5f);
                Vector2f mov = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
                mov.scale(nSize + nSize * (float) Math.random() * 0.5f);
                mov.scale(0.15f);
                Vector2f.add(vel, mov, mov);
                
                mov = new Vector2f(entity.getVelocity());
                engine.addNegativeNebulaParticle(loc, mov, nSize * 1f, 2f,
                        p.fadeIn / dur, 0f, dur, c);
            }
            
            dur = p.fadeIn + p.fadeOut + 2f;
            dur *= durMult;
            float rampUp = (p.fadeIn + p.fadeOut) / dur;
            
            c = p.underglow;
            
            for (int i = 0; i < 15; i++) {
                Vector2f loc = new Vector2f(point);
                loc = Misc.getPointWithinRadius(loc, size * 1f);
                
                float s = size * 3f * (0.25f + (float) Math.random() * 0.25f);
                
                engine.addNebulaParticle(loc, entity.getVelocity(), s, 1.5f, rampUp, 0f, dur, c);
            }
            spawnedHitGlow = true;
        }
    }
    
    public static void spawnStandardNebula(DamagingProjectileAPI proj, NebulaParams params) {
        CombatEngineAPI engine = Global.getCombatEngine();
        proj.addDamagedAlready(proj.getSource());
        
        CombatEntityAPI prev = null;
        for (int i = 0; i < 2; i++) {
            NebulaParams p = params.clone();
            p.radius *= 0.75f + 0.5f * (float) Math.random();
            
            p.withHitGlow = prev == null;
            
            Vector2f loc = new Vector2f(proj.getLocation());
            loc = Misc.getPointAtRadius(loc, p.radius * 0.4f);
            
            CombatEntityAPI e = engine.addLayeredRenderingPlugin(new MS_NebulizerPlugin(p));
            e.getLocation().set(loc);
            
            if (prev != null) {
                float dist = Misc.getDistance(prev.getLocation(), loc);
                Vector2f vel = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(loc, prev.getLocation()));
                vel.scale(dist / (p.fadeIn + p.fadeOut) * 0.7f);
                e.getVelocity().set(vel);
            }
            
            prev = e;
        }
    }
    
    public static NebulaParams createStandardNebulaParams(Color borderColor, float radius) {
        NebulaParams p = new NebulaParams();
        
        p.hitGlowSizeMult = 0.75f;
        p.spawnHitGlowAt = 0f;
        p.noiseMag = 1f;
        p.fadeIn = 0.1f;
        p.underglow = Color.CYAN;
        p.withHitGlow = true;
        p.radius = radius;
        p.color = borderColor;
        
        return p;
    }
    
    @Override
    public void init(CombatEntityAPI entity) {
        super.init(entity);
        
        fader = new FaderUtil(0f, p.fadeIn, p.fadeOut);
        fader.setBounceDown(true);
        fader.fadeIn();
        
        atmosphereTex = Global.getSettings().getSprite("combat", "corona_hard");
        
        float perSegment = 2f;
        segments = (int) ((p.radius * 2f * 3.14f) / perSegment);
        if (segments < 8) segments = 8;
        
        noise1 = Noise.genNoise(segments, p.noiseMag);
        noise2 = Noise.genNoise(segments, p.noiseMag);
        noise = Arrays.copyOf(noise1, noise1.length);
    }
    
    @Override
    public boolean isExpired() {
	return fader.isFadedOut();
    }
    
    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        float x = entity.getLocation().x;
        float y = entity.getLocation().y;
        
        float f = fader.getBrightness();
        float alphaMult = viewport.getAlphaMult();
        if (f > 0.5f) {
            alphaMult *= f * 2f;
        }
        
        float r = p.radius;
        float tSmall = p.thickness;
        
        if (fader.isFadingIn()) {
            r *= 0.75f + Math.sqrt(f) + 0.25f;
        } else {
            r *= 0.1f + 0.9f * f;
            tSmall = Math.min(r * 1f, p.thickness);
        }
        
        if (layer == CombatEngineLayers.ABOVE_PARTICLES_LOWER) {
            float a = 1f;
            renderAtmosphere(x, y, r, tSmall, alphaMult * a, segments, atmosphereTex, noise, p.color, true);
            renderAtmosphere(x, y, r - 2f, tSmall, alphaMult * a, segments, atmosphereTex, noise, p.color, true);
        } else if (layer == CombatEngineLayers.ABOVE_PARTICLES) {
            float circleAlpha = 1f;
            if (alphaMult < 0.5f) {
                circleAlpha = alphaMult * 2f;
            }
            float tCircleBorder = 1f;
            renderCircle(x, y, r, circleAlpha, segments, Color.CYAN);
            renderAtmosphere(x, y, r, tCircleBorder, circleAlpha, segments, atmosphereTex, noise, Color.CYAN, false);
        }
    }
    
    private void renderCircle(float x, float y, float radius, float alphaMult, int segments, Color color) {
        if (fader.isFadingIn()) alphaMult = 1f;
        
        float startRad = (float) Math.toRadians(0);
        float endRad = (float) Math.toRadians(360);
        float spanRad = Misc.normalizeAngle(endRad - startRad);
        float anglePerSegment = spanRad / segments;
        
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glRotatef(0, 0, 0, 1);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        GL11.glColor4ub((byte)color.getRed(), 
                (byte)color.getGreen(), 
                (byte)color.getBlue(), 
                (byte)((float) color.getAlpha() * alphaMult));
        
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(0, 0);
        for (float i = 0; i < segments + 1; i++) {
            boolean last = i == segments;
            if (last) i = 0;
            float theta = anglePerSegment * i;
            float cos = (float) Math.cos(theta);
            float sin = (float) Math.sin(theta);
            
            float m1 = 0.75f + 0.65f * noise[(int)i];
            if (p.noiseMag <= 0) {
                m1 = 1f;
            }
            
            float x1 = cos * radius * m1;
            float y1 = sin * radius * m1;
            
            GL11.glVertex2f(x1, y1);
            
            if (last) break;
        }
        
        GL11.glEnd();
        GL11.glPopMatrix();
    }
    
    private void renderAtmosphere(float x, float y, float radius, float thickness, float alphaMult, int segments, 
            SpriteAPI tex, float [] noise, Color color, boolean additive) {
        
        float startRad = (float) Math.toRadians(0);
        float endRad = (float) Math.toRadians(360);
        float spanRad = Misc.normalizeAngle(endRad - startRad);
        float anglePerSegment = spanRad / segments;
        
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glRotatef(0, 0, 0, 1);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        
        tex.bindTexture();
        
        GL11.glEnable(GL11.GL_BLEND);
        if (additive) {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        } else {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }
        
        GL11.glColor4ub((byte)color.getRed(), 
                (byte)color.getGreen(), 
                (byte)color.getBlue(), 
                (byte)((float) color.getAlpha() * alphaMult));
        
        GL11.glBegin(GL11.GL_QUAD_STRIP);
        for (float i = 0; i < segments + 1; i++) {
            boolean last = i == segments;
            if (last) i = 0;
            float theta = anglePerSegment * i;
            float cos = (float) Math.cos(theta);
            float sin = (float) Math.sin(theta);
            
            float m1 = 0.75f + 0.65f * noise[(int)i];
            float m2 = m1;
            if (p.noiseMag <= 0) {
                m1 = 1f;
                m2 = 1f;
            }
            
            float x1 = cos * radius * m1;
            float y1 = sin * radius * m1;
            float x2 = cos * (radius + thickness * m2);
            float y2 = sin * (radius + thickness * m2);
            
            GL11.glTexCoord2f(0.5f, 0.05f);
            GL11.glVertex2f(x1, y1);
            
            GL11.glTexCoord2f(0.5f, 0.95f);
            GL11.glVertex2f(x2, y2);
            
            if (last) break;
        }
        
        GL11.glEnd();
        GL11.glPopMatrix();
    }
}
