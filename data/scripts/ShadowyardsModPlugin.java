package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.campaign.fleets.MS_fleetFighterFinagler;
import data.scripts.ai.MS_ApisAI;
import data.scripts.ai.MS_Barrago_S1_AI;
import data.scripts.ai.WindingMissileAI;
import data.scripts.ai.WindingRocketAI;
import data.scripts.ai.MS_SimpleMissileAI;
import data.scripts.ai.MS_Barrago_S2_AI;
import data.scripts.ai.MS_BlackcapAI;
import data.scripts.ai.MS_SynapticAI;
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
    public static final String BARRAGO_LRM_S1 = "ms_barrago_lrm_s1";
    public static final String BARRAGO_LRM_S2 = "ms_barrago_lrm_s2";
    public static final String BLACKCAP_AFM = "ms_blackcap";
    public static final String APIS_SWARM_MISSILE = "ms_apis_swarmer";
    //public static final String PDMISSILE = "ms_pdMissile";
    public static final String SYNAPTIC_BOMB = "ms_synaptic_bomb";
    
    public static boolean hasTwigLib;
    public static boolean isExerelin;
    public static boolean hasShaderLib;
    public static boolean templarsExist = false;
    private EveryFrameScript MS_fleetFighterFinagler;

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
        
        Global.getSector().addScript(new MS_fleetFighterFinagler());
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
            case BARRAGO_LRM_S1:
                return new PluginPick<MissileAIPlugin>(new MS_Barrago_S1_AI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
            case BARRAGO_LRM_S2:
                return new PluginPick<MissileAIPlugin>(new MS_Barrago_S2_AI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
            case BLACKCAP_AFM:
                return new PluginPick<MissileAIPlugin>(new MS_BlackcapAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
            case APIS_SWARM_MISSILE:
                return new PluginPick<MissileAIPlugin>(new MS_ApisAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
            case SYNAPTIC_BOMB:
                return new PluginPick<MissileAIPlugin>(new MS_SynapticAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
            default:
                return null;
        }
    }
    
    /*public PluginPick<FleetInflater> pickFleetInflater(CampaignFleetAPI fleet, Object params) {
        if (params instanceof DefaultFleetInflaterParams && (fleet.getFaction().getId().contains("shadow_industry") ||
                fleet.getFaction().getId().contains(Factions.PIRATES) || fleet.getFaction().getId().contains(Factions.PLAYER) )) {
            DefaultFleetInflaterParams p = (DefaultFleetInflaterParams) params;  
            return new PluginPick<FleetInflater>(new MS_FleetInflaterPlugin(p), CampaignPlugin.PickPriority.MOD_SET);  
        }
        return null;
    }*/
    
    private static void initShadowyards() {
        new SHIGen().generate(Global.getSector());
    }
}