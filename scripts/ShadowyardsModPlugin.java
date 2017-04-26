package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.ai.WindingMissileAI;
import data.scripts.ai.WindingRocketAI;
import data.scripts.ai.MS_SimpleMissileAI;
import data.scripts.ai.MS_Barrago_S2_AI;
import data.scripts.ai.MS_BlackcapAI;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;
import exerelin.campaign.SectorManager;
import data.scripts.hullmods.TEM_LatticeShield;

import data.scripts.world.SHIGen;

public class ShadowyardsModPlugin extends BaseModPlugin {

    public static final String SIDEWINDER_MISSILE = "ms_tusk_bomblet";
    public static final String SIDEWINDER_ROCKET = "ms_splinter_rocket";
    public static final String SIMPLE_SHRIKE = "ms_shrike";
    public static final String BARRAGO_LRM_S2 = "ma_barrago_lrm_s2";
    public static final String BLACKCAP_AFM = "ms_blackcap";
    
    public static boolean hasTwigLib;
    public static boolean isExerelin;
    public static boolean hasShaderLib;
    public static boolean templarsExist = false;

    @Override
    public void onApplicationLoad() {
        isExerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        hasTwigLib = Global.getSettings().getModManager().isModEnabled("ztwiglib");
        hasShaderLib = Global.getSettings().getModManager().isModEnabled("shaderLib");
        templarsExist = Global.getSettings().getModManager().isModEnabled("Templars");
        
        try {
            if (hasShaderLib) {
                ShaderLib.init();
        
                if (ShaderLib.areShadersAllowed() && ShaderLib.areBuffersAllowed()) {
                    LightData.readLightDataCSV("data/lights/ms_light_data.csv");
                    TextureData.readTextureDataCSV("data/lights/ms_texture_data.csv");
                }
            }
        } catch (NoClassDefFoundError ex) {
            String message = System.lineSeparator()
                    + System.lineSeparator() + "ShaderLib not found; advanced graphics function disabled."
                    + System.lineSeparator();
            throw new NoClassDefFoundError(message); 
        }
        try {
            if (TEM_LatticeShield.AEGIS_SHIELD_COLOR != null) {
                templarsExist = true;
            }
        } catch (NoClassDefFoundError ex) {
        }
    }
    
    @Override
    public void onNewGame() {
        if (isExerelin && !SectorManager.getCorvusMode())
        {
            return;
        }
        initShadowyards();
    }
    
    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case SIDEWINDER_MISSILE:
                return new PluginPick<MissileAIPlugin>(new WindingMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
            case SIDEWINDER_ROCKET:
                return new PluginPick<MissileAIPlugin>(new WindingRocketAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
            case SIMPLE_SHRIKE:
                return new PluginPick<MissileAIPlugin>(new MS_SimpleMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
            case BARRAGO_LRM_S2:
                return new PluginPick<MissileAIPlugin>(new MS_Barrago_S2_AI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
            case BLACKCAP_AFM:
                return new PluginPick<MissileAIPlugin>(new MS_BlackcapAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
            default:
                return null;
        }
    }
    
    
    private static void initShadowyards() {
        new SHIGen().generate(Global.getSector());
    }
}
