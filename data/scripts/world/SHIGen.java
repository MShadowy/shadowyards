package data.scripts.world;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
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
        //the yardies
        FactionAPI shadow = sector.getFaction("shadow_industry");
        
        //vanilla factions
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
        FactionAPI remnants = sector.getFaction(Factions.REMNANTS);
        FactionAPI derelicts = sector.getFaction(Factions.DERELICT);
        
        //mod factions
        shadow.setRelationship("SCY", RepLevel.WELCOMING);                      // not really ideologically on the same page, but we're helping each other regardless
        shadow.setRelationship("ORA", RepLevel.FAVORABLE);                      // something's a little strange here, but they seem like they're on the level...
        shadow.setRelationship("scalartech", 0.4f);                             // isolationists, but reliable trading partners
        shadow.setRelationship("dassault_mikoyan", 0.2f);                       // corporate anti-fascists which... well, we'll roll with it
        shadow.setRelationship("6eme_bureau", 0.2f);                            // subfaction; dme legbreakers
        shadow.setRelationship("pack", RepLevel.FAVORABLE);                     // anarchist commune trying to resist imperialism; also we like dogs
        shadow.setRelationship("brighton", 0.5f);                               // not really in much position to help out more
        
        shadow.setRelationship("blackrock_driveyards", RepLevel.SUSPICIOUS);    // they're doing something in Gniess, and it ain't lookin nice
        shadow.setRelationship("sad", RepLevel.SUSPICIOUS);                     // a splinter group of Yardies gone completely off the chain and/or rails
        shadow.setRelationship("interstellarimperium", RepLevel.INHOSPITABLE);  // imperialists, but they hate the Hegomony, so, for now...
        shadow.setRelationship("kingdom_of_terra", RepLevel.INHOSPITABLE);      // ... we're not really sure but they seem kind of aggressive?
        shadow.setRelationship("HMI", -0.4f);                                   // pirates pretending to be a corporation
           
        shadow.setRelationship("diableavionics", RepLevel.HOSTILE);             // imperialists, and they seem to be actively trying to destabilize things
        shadow.setRelationship("mayorate", RepLevel.HOSTILE);                   // xenophobic, hostile... and what kind of government model even is this?
        shadow.setRelationship("junk_pirates", RepLevel.HOSTILE);               // innovative pirates
        shadow.setRelationship("exigency", RepLevel.HOSTILE);                   // technologically sophisticated pirates
        shadow.setRelationship("exipirated", RepLevel.HOSTILE);                 // also pirates
        shadow.setRelationship("cabal", -0.8f);                                 // really, really obnoxious pirates
        shadow.setRelationship("blade_breakers", RepLevel.HOSTILE);             // we don't know too much about them, but they seem... y'know, bad
              
        shadow.setRelationship("tahlan_legioinfernalis", -0.74f);               // better organized and better armed space pirates, might be fascists
        shadow.setRelationship("draco", -0.8f);                                 // what the hell is wrong with these people
        shadow.setRelationship("fang", -0.8f);                                  // again, what the hell is wrong with these people
        shadow.setRelationship("mess", -0.8f);                                  // killer goo? no thanks
        shadow.setRelationship("new_galactic_order", -2f);                      // fascists as they present themselves
        shadow.setRelationship("fpe", -0.74f);                                  // fascists as they actually are
            
        shadow.setRelationship(hegemony.getId(), -0.6f);                        // long standing foes, but it's a professional relationship
        shadow.setRelationship(pirates.getId(), -0.6f);                         // we can sympathize with how difficult matters are, but not the random murder and banditry
        shadow.setRelationship(diktat.getId(), -0.7f);                          // basically similar to the Hegemony, but worse
        shadow.setRelationship(remnants.getId(), RepLevel.HOSTILE);             // renegade Tri-Tach toys from the 1st AI War
        shadow.setRelationship(derelicts.getId(), RepLevel.HOSTILE);            // Domain leftovers, past their "best by" date
        
        shadow.setRelationship(tritachyon.getId(), 0f);                         // asshats, but we've got enough on our plate
        
        shadow.setRelationship(independent.getId(), 0.2f);                      // a mixed bag, but we support autarchy and independence
        shadow.setRelationship(league.getId(), 0.1f);                           // we're not really friends, but...
        
        church.setRelationship(shadow.getId(), -0.3f);                          // they kinda resent the SRA's efforts to materially help the sector
        kol.setRelationship(shadow.getId(), -0.3f);                             // subfaction: church legbreakers
        path.setRelationship(shadow.getId(), -2f);                              // we kinda hate them because they only didn't succeed in virus bombing Euripides 
                                                                                // because of all the genemodding we were (and are) doing
        
        player.setRelationship(shadow.getId(), 0);                              // ah, you're finally awake...
    }
}
