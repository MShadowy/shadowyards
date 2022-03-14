package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.impl.campaign.econ.MS_redwingsMarketHandlerPlugin;
import data.campaign.fleets.MS_RedwingsReinforcementManager;
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
import data.scripts.world.MS_industryAdder;

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
    
    public static boolean isExerelin;
    public static boolean templarsExist;

    @Override
    public void onApplicationLoad() {
        isExerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        boolean hasShaderLib = Global.getSettings().getModManager().isModEnabled("shaderLib");
        boolean hasMagicLib = Global.getSettings().getModManager().isModEnabled("MagicLib");
        boolean hasLazyLib = Global.getSettings().getModManager().isModEnabled("lw_lazylib");
        templarsExist = Global.getSettings().getModManager().isModEnabled("Templars");
        
        if (!hasLazyLib)
        {
            throw new RuntimeException("Shadowyards requires LazyLib!"
                    + "\nGet it at http://fractalsoftworks.com/forum/index.php?topic=5444");
        }
        if (!hasMagicLib)
        {
            throw new RuntimeException("Shadowyards requires MagicLib!"
                    + "\nGet it at http://fractalsoftworks.com/forum/index.php?topic=13718.0");
        }
        if (!hasShaderLib)
        {
            throw new RuntimeException("Shadowyards requires GraphicsLib!"
                     + "\nGet it at http://fractalsoftworks.com/forum/index.php?topic=10982");
        }
        else
        {
            ShaderLib.init();
            LightData.readLightDataCSV("data/lights/ms_light_data.csv");
            TextureData.readTextureDataCSV("data/lights/ms_texture_data.csv");
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
        if (isExerelin && !SectorManager.getManager().isCorvusMode())
        {
            Global.getSector().addScript(new MS_fleetFighterFinagler());
            Global.getSector().addScript(new MS_redwingsMarketHandlerPlugin());
            return;
        }
        
        Global.getSector().addScript(new MS_fleetFighterFinagler());
        Global.getSector().addScript(new MS_redwingsMarketHandlerPlugin());
        Global.getSector().addScript(new MS_RedwingsReinforcementManager());
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("shadow_industry");
        //MS_specialItemInitializer.run();
        initShadowyards();
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        MS_industryAdder.run();
        
        MarketAPI market = Global.getSector().getEconomy().getMarket("euripides");
        if (market != null) {
            PersonAPI admin = Global.getFactory().createPerson();
            admin.setFaction("shadow_industry");
            admin.setGender(Gender.FEMALE);
            admin.setPostId(Ranks.POST_FACTION_LEADER);
            admin.setRankId(Ranks.FACTION_LEADER);

            admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3);
            admin.setAICoreId(Commodities.BETA_CORE);
            admin.getName().setFirst("Mahika");
            admin.getName().setLast("Grey");
            admin.setPortraitSprite("graphics/portraits/characters/ms_mahikaGrey.png");
            
            market.setAdmin(admin);
            market.getCommDirectory().addPerson(admin, 0);
            market.addPerson(admin);
        }
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
    
    private static void initShadowyards() {
        new SHIGen().generate(Global.getSector());
    }
}