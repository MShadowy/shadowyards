package data.campaign.fleets;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.AICoreAdminPlugin;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.BattleAutoresolverPlugin;
import com.fs.starfarer.api.campaign.BattleCreationPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetInflater;
import com.fs.starfarer.api.campaign.FleetStubAPI;
import com.fs.starfarer.api.campaign.FleetStubConverterPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.ai.AbilityAIPlugin;
import com.fs.starfarer.api.campaign.ai.AssignmentModulePlugin;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.NavigationModulePlugin;
import com.fs.starfarer.api.campaign.ai.StrategicModulePlugin;
import com.fs.starfarer.api.campaign.ai.TacticalModulePlugin;
import com.fs.starfarer.api.campaign.econ.ImmigrationPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.fleets.MS_FleetInflaterPlugin;
import com.fs.starfarer.api.plugins.AutofitPlugin;


public class MS_InflaterCaller implements CampaignPlugin {
    
    @Override
    public PluginPick<FleetInflater> pickFleetInflater(CampaignFleetAPI fleet, Object params) {
        if (params instanceof DefaultFleetInflaterParams && fleet.getFaction().getId().equals("shadow_industry")) {  
            DefaultFleetInflaterParams p = (DefaultFleetInflaterParams) params;    
            return new PluginPick<FleetInflater>(new MS_FleetInflaterPlugin(p), CampaignPlugin.PickPriority.MOD_SET);    
        }  
        return null; 
    }

    @Override
    public String getId() {
        String id = "MS_InflaterCaller";
        return id;
    }

    @Override
    public boolean isTransient() { return false; }

    @Override
    public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken set) { return null; }

    @Override
    public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(Object o, SectorEntityToken set) { return null; }

    @Override
    public PluginPick<BattleCreationPlugin> pickBattleCreationPlugin(SectorEntityToken set) { return null; }

    @Override
    public PluginPick<BattleAutoresolverPlugin> pickBattleAutoresolverPlugin(BattleAPI bapi) { return null; }

    @Override
    public PluginPick<ReputationActionResponsePlugin> pickReputationActionResponsePlugin(Object o, String string) { return null; }

    @Override
    public PluginPick<ReputationActionResponsePlugin> pickReputationActionResponsePlugin(Object o, PersonAPI papi) { return null; }

    @Override
    public void updateEntityFacts(SectorEntityToken set, MemoryAPI mapi) {}

    @Override
    public void updatePersonFacts(PersonAPI papi, MemoryAPI mapi) {}

    @Override
    public void updateFactionFacts(FactionAPI fapi, MemoryAPI mapi) {}

    @Override
    public void updateGlobalFacts(MemoryAPI mapi) {}

    @Override
    public void updatePlayerFacts(MemoryAPI mapi) {}

    @Override
    public void updateMarketFacts(MarketAPI mapi, MemoryAPI mapi1) {}

    @Override
    public PluginPick<AssignmentModulePlugin> pickAssignmentAIModule(CampaignFleetAPI cfapi, ModularFleetAIAPI mfp) { return null; }

    @Override
    public PluginPick<StrategicModulePlugin> pickStrategicAIModule(CampaignFleetAPI cfapi, ModularFleetAIAPI mfp) { return null; }

    @Override
    public PluginPick<TacticalModulePlugin> pickTacticalAIModule(CampaignFleetAPI cfapi, ModularFleetAIAPI mfp) { return null; }

    @Override
    public PluginPick<NavigationModulePlugin> pickNavigationAIModule(CampaignFleetAPI cfapi, ModularFleetAIAPI mfp) { return null; }

    @Override
    public PluginPick<AbilityAIPlugin> pickAbilityAI(AbilityPlugin ap, ModularFleetAIAPI mfp) { return null; }

    @Override
    public PluginPick<FleetStubConverterPlugin> pickStubConverter(FleetStubAPI fsapi) { return null; }

    @Override
    public PluginPick<FleetStubConverterPlugin> pickStubConverter(CampaignFleetAPI cfapi) { return null; }

    @Override
    public PluginPick<AutofitPlugin> pickAutofitPlugin(FleetMemberAPI fmapi) { return null; }

    @Override
    public PluginPick<InteractionDialogPlugin> pickRespawnPlugin() { return null; }

    @Override
    public PluginPick<ImmigrationPlugin> pickImmigrationPlugin(MarketAPI mapi) { return null; }

    @Override
    public PluginPick<AICoreAdminPlugin> pickAICoreAdminPlugin(String string) { return null; }
}
