package com.fs.starfarer.api.impl.campaign.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetInflater;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater.AvailableFighterImpl;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater.AvailableWeaponImpl;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater.SortedWeapons;
import static com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater.getAverageDmodsForQuality;
import static com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater.getNumDModsToAdd;
import static com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater.getTierProbability;
import static com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater.makePicks;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.plugins.AutofitPlugin;
import com.fs.starfarer.api.plugins.AutofitPlugin.AvailableFighter;
import com.fs.starfarer.api.plugins.AutofitPlugin.AvailableWeapon;
import com.fs.starfarer.api.plugins.impl.CoreAutofitPlugin;
import com.fs.starfarer.api.util.ListMap;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MS_FleetInflaterPlugin implements FleetInflater, AutofitPlugin.AutofitPluginDelegate {
    protected DefaultFleetInflaterParams p;
    public static float GOAL_VARIANT_PROBABILITY = 0.5f;
    transient FleetMemberAPI currMember = null;
    transient ShipVariantAPI currVariant = null;
    transient List<AutofitPlugin.AvailableFighter> fighters;
    transient List<AutofitPlugin.AvailableWeapon> weapons;
    transient List<String> hullmods;
    transient CampaignFleetAPI fleet;
    transient FactionAPI faction;
    
    public MS_FleetInflaterPlugin(DefaultFleetInflaterParams p) {
        this.p = p;
    }

    public void setFaction(FactionAPI faction)
    {
        this.faction = faction;
    }
    
    @Override
    public void inflate(CampaignFleetAPI fleet) {
        Random random = new Random();
        if (p.seed != null) { random = new Random(p.seed); }
        
        Random dmodRandom = new Random();
        if (p.seed != null) { dmodRandom = Misc.getRandom(p.seed, 5); }
        
        String factionId = fleet.getFaction().getId();
        if (p.factionId != null) { factionId = p.factionId; }
        
        CoreAutofitPlugin auto = new CoreAutofitPlugin(fleet.getCommander());
        auto.setRandom(random);
        
        auto.setChecked(CoreAutofitPlugin.UPGRADE, true);
        
        this.fleet = fleet;
        this.faction = fleet.getFaction();
        if (p.factionId != null) { this.faction = Global.getSector().getFaction(p.factionId); }
        
        hullmods = new ArrayList<>(faction.getKnownHullMods());
        
        SortedWeapons nonPriorityWeapons = new SortedWeapons();
        SortedWeapons priorityWeapons = new SortedWeapons();
        
        Set<String> weaponCategories = new LinkedHashSet<>();
        for (String weaponId : faction.getKnownWeapons()) {
            if (!faction.isWeaponKnownAt(weaponId, p.timestamp)) continue;
			
            WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(weaponId);
			
            int tier = spec.getTier();
            String cat = spec.getAutofitCategory();
			
            if (isPriority(spec)) {
		List<AvailableWeapon> list = priorityWeapons.getWeapons(tier).getWeapons(cat);
		list.add(new AvailableWeaponImpl(spec, 1000));
            } else {
		List<AvailableWeapon> list = nonPriorityWeapons.getWeapons(tier).getWeapons(cat);
		list.add(new AvailableWeaponImpl(spec, 1000));
            }
            weaponCategories.add(cat);
	}
        
        ListMap<AvailableFighter> nonPriorityFighters = new ListMap<>();
        ListMap<AvailableFighter> priorityFighters = new ListMap<>();
        Set<String> fighterCategories = new LinkedHashSet<>();
        for (String wingId : faction.getKnownFighters()) {
            if (!faction.isFighterKnownAt(wingId, p.timestamp)) continue;
			
            FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(wingId);
            
            String cat = spec.getAutofitCategory();
            if (isPriority(spec)) {
		priorityFighters.add(cat, new AvailableFighterImpl(spec, 1000));
            } else {
		nonPriorityFighters.add(cat, new AvailableFighterImpl(spec, 1000));
            }
            fighterCategories.add(cat);
	}
        
        float averageDmods = getAverageDmodsForQuality(p.quality);
        
        int currFt = 0;
        int limit = 3;
        int fSize = fleet.getFleetPoints();
        
        if (fSize <= 40) { limit = 0; } else if (fSize <= 80) { limit = 1; } else if (fSize <= 200) { limit = 2; }
        
        int memberIndex = 0;
        for (FleetMemberAPI m : fleet.getFleetData().getMembersListCopy()) {
            if (m.getHullSpec().hasTag(Items.TAG_NO_AUTOFIT)) {
                continue;
            }
            
            if (p.seed != null) {
                int extra = m.getShipName().hashCode();
                random = new Random(p.seed * extra);
                auto.setRandom(random);
                dmodRandom = Misc.getRandom(p.seed * extra, 5);
            }
            
            weapons = new ArrayList<>();
            for (String cat : weaponCategories) {
                for (int t = 0; t < 4; t++) {
                    float percent = getTierProbability(t, this.p.quality);
                    if (this.p.allWeapons != null && this.p.allWeapons) {
                        percent = 1f;
                    }
                    
                    if (random.nextFloat() >= percent) { continue; }
                    
                    int num = 4;
                    
                    if (this.p.allWeapons != null && this.p.allWeapons) {
                        num = 100;
                    }
                    
                    List<AvailableWeapon> priority = priorityWeapons.getWeapons(t).getWeapons(cat);
                    Set<Integer> picks = makePicks(num, priority.size(), random);
                    for (Integer index : picks) {
                        AvailableWeapon w = priority.get(index);
                        weapons.add(w);
                    }
                    
                    num -= picks.size();
                    if (num > 0) {
                        List<AvailableWeapon> nonPriority = nonPriorityWeapons.getWeapons(t).getWeapons(cat);
                        picks = makePicks(num, nonPriority.size(), random);
                        for (Integer index : picks) {
                            AvailableWeapon w = nonPriority.get(index);
                            weapons.add(w);
                        }
                    }
                }
            }
            
            fighters = new ArrayList<>();
            for (String cat : fighterCategories) {
                for (String wingId : faction.getKnownFighters()) {
                    List<AutofitPlugin.AvailableFighter> priority = priorityFighters.get(cat);
                    
                    boolean madePriorityPicks = false;
                    
                    FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(wingId);
                    float percent = 1f;                    
                    if (this.p.allWeapons != null && this.p.allWeapons) {
                        percent = 1f;
                    }
					
                    int num = random.nextInt(2) + 1; 
					
                    if (this.p.allWeapons != null && this.p.allWeapons) {
                        num = 100;
                    }
                        
                    if (spec.getId().contains("ms_shikome")) {
                        percent = 0.005f;
                        if (currFt < limit) {
                            currFt += 1;
                            num = 1;
                        } else {
                            num = 0;
                        }
                    }
                    		
                    if (random.nextFloat() >= percent) continue;
                
                    if (priority != null) {               
                        Set<Integer> picks = makePicks(num, priority.size(), random);
                        for (Integer index : picks) {
                            AvailableFighter f = priority.get(index);
                            fighters.add(f);
                            madePriorityPicks = true;
                        }
                    }
                
                    if (!madePriorityPicks) {
                        List<AvailableFighter> nonPriority = nonPriorityFighters.get(cat);
                        Set<Integer> picks = makePicks(num, nonPriority.size(), random);
                        for (Integer index : picks) {
                            AvailableFighter f = nonPriority.get(index);
                            fighters.add(f);
                        }
                    }
                }
            }
            
            ShipVariantAPI target = m.getVariant();
            if (target.getOriginalVariant() != null) {
                target = Global.getSettings().getVariant(target.getOriginalVariant());
            }
            
            if (faction.isPlayerFaction()) {
                if (random.nextFloat() < GOAL_VARIANT_PROBABILITY) {
                    List<ShipVariantAPI> targets = Global.getSector().getAutofitVariants().getTargetVariants(m.getHullId());
                    WeightedRandomPicker<ShipVariantAPI> alts = new WeightedRandomPicker<>(random);
                    for (ShipVariantAPI curr : targets) {
			if (curr.getHullSpec().getHullId().equals(target.getHullSpec().getHullId())) {
                            alts.add(curr);
			}
                    }
                    if (!alts.isEmpty()) {
			target = alts.pick();
                    }
                }
            }
            
            currVariant = Global.getSettings().createEmptyVariant(fleet.getId() + "_" + memberIndex, target.getHullSpec());
            currMember = m;
            
            if (target.isStockVariant()) {
                currVariant.setOriginalVariant(target.getHullVariantId());
            }
            
            boolean randomize = random.nextFloat() < faction.getDoctrine().getAutofitRandomizeProbability();
            auto.setChecked(CoreAutofitPlugin.RANDOMIZE, randomize);
            
            memberIndex++;
            auto.doFit(currVariant, target, this);
            m.setVariant(currVariant, false, false);
            
            if (!currMember.isStation()) {
                int addDmods = getNumDModsToAdd(currVariant, averageDmods, dmodRandom);
                if (addDmods > 0) {
                    DModManager.setDHull(currVariant);
                    DModManager.addDMods(m, true, addDmods, dmodRandom);
                }
            }
        }
        
        fleet.getFleetData().setSyncNeeded();
        fleet.getFleetData().syncIfNeeded();
    }

    @Override
    public boolean removeAfterInflating()
    {
        return p.persistent == null || !p.persistent;
    }

    @Override
    public void setRemoveAfterInflating(boolean removeAfterInflating)
    {
        p.persistent = !removeAfterInflating;
        if (!p.persistent)
        {
            p.persistent = null;
        }
    }

    @Override
    public void clearFighterSlot(int index, ShipVariantAPI variant)
    {
        variant.setWingId(index, null);
        for (AutofitPlugin.AvailableFighter curr : fighters)
        {
            if (curr.getId().equals(curr.getId()))
            {
                curr.setQuantity(curr.getQuantity() + 1);
                break;
            }
        }
    }

    @Override
    public void clearWeaponSlot(WeaponSlotAPI slot, ShipVariantAPI variant)
    {
        variant.clearSlot(slot.getId());
        for (AutofitPlugin.AvailableWeapon curr : weapons)
        {
            if (curr.getId().equals(curr.getId()))
            {
                curr.setQuantity(curr.getQuantity() + 1);
                break;
            }
        }
    }

    @Override
    public void fitFighterInSlot(int index, AutofitPlugin.AvailableFighter fighter, ShipVariantAPI variant)
    {
        fighter.setQuantity(fighter.getQuantity() - 1);
        variant.setWingId(index, fighter.getId());
    }

    @Override
    public void fitWeaponInSlot(WeaponSlotAPI slot, AutofitPlugin.AvailableWeapon weapon, ShipVariantAPI variant)
    {
        weapon.setQuantity(weapon.getQuantity() - 1);
        variant.addWeapon(slot.getId(), weapon.getId());
    }

    @Override
    public List<AutofitPlugin.AvailableFighter> getAvailableFighters()
    {
        return fighters;
    }

    @Override
    public List<AutofitPlugin.AvailableWeapon> getAvailableWeapons()
    {
        return weapons;
    }

    @Override
    public List<String> getAvailableHullmods()
    {
        return hullmods;
    }

    @Override
    public ShipAPI getShip()
    {
        return null;
    }
    
    @Override
    public void syncUIWithVariant(ShipVariantAPI variant)
    {
    }

    @Override
    public boolean isPriority(WeaponSpecAPI weapon)
    {
        return faction.isWeaponPriority(weapon.getWeaponId());
    }

    @Override
    public boolean isPriority(FighterWingSpecAPI wing)
    {
        return faction.isFighterPriority(wing.getId());
    }

    public FleetMemberAPI getMember()
    {
        return currMember;
    }

    @Override
    public FactionAPI getFaction()
    {
        return faction;
    }

    public Long getSeed()
    {
        return p.seed;
    }

    public void setSeed(Long seed)
    {
        this.p.seed = seed;
    }

    public Boolean getPersistent()
    {
        return p.persistent;
    }

    public void setPersistent(Boolean persistent)
    {
        this.p.persistent = persistent;
    }

    @Override
    public float getQuality()
    {
        return p.quality;
    }

    @Override
    public void setQuality(float quality)
    {
        this.p.quality = quality;
    }

    public Long getTimestamp()
    {
        return p.timestamp;
    }

    public void setTimestamp(Long timestamp)
    {
        this.p.timestamp = timestamp;
    }

    @Override
    public Object getParams()
    {
        return p;
    }

    @Override
    public boolean canAddRemoveHullmodInPlayerCampaignRefit(String modId)
    {
        return true;
    }

    @Override
    public boolean isPlayerCampaignRefit()
    {
        return false;
    }
}
