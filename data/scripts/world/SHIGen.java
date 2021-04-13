package data.scripts.world;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;

import data.scripts.world.anar.Anar;
import data.scripts.world.gigas.Gigas;
import data.scripts.world.outposts.Outposter;
import data.scripts.world.yajna.Yajna;

public class SHIGen implements SectorGeneratorPlugin {
    
    @Override
    public void generate(SectorAPI sector) {
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("shadow_industry");
        initFactionRelationships(sector);
        
        new Anar().generate(sector);
        new Gigas().generate(sector);
        new Yajna().generate(sector);
        new Outposter().generate(sector);
    }
    
    public static void initFactionRelationships(SectorAPI sector) {
        FactionAPI hegemony = sector.getFaction(Factions.HEGEMONY);
	FactionAPI tritachyon = sector.getFaction(Factions.TRITACHYON);
	FactionAPI pirates = sector.getFaction(Factions.PIRATES);
	FactionAPI independent = sector.getFaction(Factions.INDEPENDENT);
	FactionAPI kol = sector.getFaction(Factions.KOL);
	FactionAPI church = sector.getFaction(Factions.LUDDIC_CHURCH);
	FactionAPI path = sector.getFaction(Factions.LUDDIC_PATH);
	FactionAPI player = sector.getFaction(Factions.PLAYER);
	FactionAPI diktat = sector.getFaction(Factions.DIKTAT);
        FactionAPI league = sector.getFaction(Factions.PERSEAN);
        FactionAPI shadow = sector.getFaction("shadow_industry");
        
        player.setRelationship(shadow.getId(), 0);
        
        shadow.setRelationship(hegemony.getId(), -0.6f);
        shadow.setRelationship(pirates.getId(), -0.6f);
        shadow.setRelationship(diktat.getId(), -0.6f);
        
        shadow.setRelationship(tritachyon.getId(), 0f);
        
        shadow.setRelationship(independent.getId(), 0.2f);
        shadow.setRelationship(league.getId(), 0.1f);
        
        church.setRelationship(shadow.getId(), -0.3f);
        path.setRelationship(shadow.getId(), -0.9f);
        kol.setRelationship(shadow.getId(), -0.3f);
    }
}
