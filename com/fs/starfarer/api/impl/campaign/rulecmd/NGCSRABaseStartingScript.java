package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.JumpPointInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.tutorial.RogueMinerMiscFleetManager;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialLeashAssignmentAI;
import com.fs.starfarer.api.ui.HintPanelAPI;
import com.fs.starfarer.api.util.Misc;
import static com.fs.starfarer.api.util.Misc.clearAreaAroundPlayer;
import java.util.List;
import java.util.Map;



public class NGCSRABaseStartingScript extends BaseCommandPlugin {

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }
        CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");
        final MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
        
        data.addScript(new Script() {
            public void run() {
                CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
                
                CargoAPI cargo = fleet.getCargo();
                cargo.initPartialsIfNeeded();
                
                boolean addCadre = memory.getBoolean("$ngcAddCadre");
                
                fleet.getFleetData().ensureHasFlagship();
                
                if (addCadre) {
                    for (FleetMemberAPI m : fleet.getFleetData().getMembersListCopy()) {
                        if (!m.isFlagship()) {
                            PersonAPI comrade = Global.getSector().getPlayerFaction().createRandomPerson();
                            comrade.getStats().setSkillLevel(Skills.DEFENSIVE_SYSTEMS, 1);
                            comrade.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 1);
                            comrade.setRankId(Ranks.SPACE_LIEUTENANT);
                            comrade.setPostId(Ranks.POST_MERCENARY);
                            comrade.setPersonality(Personalities.AGGRESSIVE);
                            comrade.getStats().refreshCharacterStatsEffects();
                            
                            m.setCaptain(comrade);
                            fleet.getFleetData().addOfficer(comrade);
                            break;
                        }
                    }
                }
                
                float freeCargo = cargo.getSpaceLeft();
                float addSupplies = freeCargo * 0.75f;
                if (addSupplies > 0) {
                    cargo.addSupplies(addSupplies);
                    freeCargo -= addSupplies;
                }
                float addMachinery = Math.min(freeCargo, 15f);
                if (addMachinery > 0) {
                    cargo.addCommodity(Commodities.HEAVY_MACHINERY, addMachinery);
                    freeCargo -= addMachinery;
                }
                
                float addFuel = cargo.getFreeFuelSpace() * 0.75f;
                if (addFuel > 0) {
                    cargo.addFuel(addFuel);
                }
                
                float addCrew = cargo.getFreeCrewSpace() * 0.75f;
                if (addCrew > 0) {
                    cargo.addCrew((int) addCrew);
                }
                
                cargo.getCredits().add(20000);
                
                for (FleetMemberAPI m : fleet.getFleetData().getMembersListCopy()) {
                    m.getRepairTracker().setCR(0.7f);
                }
                fleet.getFleetData().setSyncNeeded();
                
                StarSystemAPI system = Global.getSector().getStarSystem("galatia");
		PlanetAPI ancyra = (PlanetAPI) system.getEntityById("ancyra");
		SectorEntityToken derinkuyu = system.getEntityById("derinkuyu_station");
		SectorEntityToken inner = system.getEntityById("galatia_jump_point_alpha");
		SectorEntityToken fringe = system.getEntityById("galatia_jump_point_fringe");
		SectorEntityToken relay = system.getEntityById("ancyra_relay");
		
		relay.getMemoryWithoutUpdate().unset(MemFlags.OBJECTIVE_NON_FUNCTIONAL);
		
		ancyra.getMarket().removeSubmarket(Submarkets.LOCAL_RESOURCES);
		
		Global.getSector().getEconomy().addMarket(ancyra.getMarket(), false);
		Global.getSector().getEconomy().addMarket(derinkuyu.getMarket(), false);
		
		HintPanelAPI hints = Global.getSector().getCampaignUI().getHintPanel();
		if (hints != null) {
			hints.clearHints(false);
		}
		
		RogueMinerMiscFleetManager script = new RogueMinerMiscFleetManager(derinkuyu);
		for (int i = 0; i < 20; i++) {
			script.advance(1f);
		}
		system.addScript(script);
		
		for (CampaignFleetAPI pirateFleet : system.getFleets()) {
			if (Factions.PIRATES.equals(pirateFleet.getFaction().getId())) {
				pirateFleet.removeScriptsOfClass(TutorialLeashAssignmentAI.class);
			}
		}
		
		inner.getMemoryWithoutUpdate().unset(JumpPointInteractionDialogPluginImpl.UNSTABLE_KEY);
		inner.getMemoryWithoutUpdate().unset(JumpPointInteractionDialogPluginImpl.CAN_STABILIZE);
		
		fringe.getMemoryWithoutUpdate().unset(JumpPointInteractionDialogPluginImpl.UNSTABLE_KEY);
		fringe.getMemoryWithoutUpdate().unset(JumpPointInteractionDialogPluginImpl.CAN_STABILIZE);
		
		system.removeTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER);
		
		MarketAPI market = ancyra.getMarket();
		market.getMemoryWithoutUpdate().unset(MemFlags.MARKET_DO_NOT_INIT_COMM_LISTINGS);
		market.getStats().getDynamic().getMod(Stats.PATROL_NUM_LIGHT_MOD).unmodifyMult("tut");
		market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).unmodifyMult("tut");
		market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).unmodifyMult("tut");
		market.setEconGroup(null);
		
		derinkuyu.getMarket().setEconGroup(null);
                
            }
        });
        
        clearAreaAroundPlayer(2000f);
        return true;
    }
    
}
