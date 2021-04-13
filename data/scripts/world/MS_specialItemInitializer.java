package data.scripts.world;

/*import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin.InstallableItemDescriptionMode;
import com.fs.starfarer.api.impl.campaign.econ.impl.BoostIndustryInstallableItemEffect;
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.MS_specialItems;*/


public class MS_specialItemInitializer {
    
    /*public static float DEGRADED_AUTOFORGE_BONUS = 0.1f;
    public static float PERFECT_AUTOFORGE_BONUS = 0.25f;
	
    public static int DEGRADED_AUTOFORGE_PROD = 1;
    public static int PERFECT_AUTOFORGE_PROD = 2;
    
    protected static float buildTime = 1f;
    
    public static void run() {
        initSpecial();
    }
    
    public static void initSpecial() {
        if (!ItemEffectsRepo.ITEM_EFFECTS.containsKey(MS_specialItems.DEGRADED_AUTOFORGE)) {
            ItemEffectsRepo.ITEM_EFFECTS.put(MS_specialItems.DEGRADED_AUTOFORGE, 
                                new BoostIndustryInstallableItemEffect(MS_specialItems.DEGRADED_AUTOFORGE, DEGRADED_AUTOFORGE_PROD, 0) {
                @Override
                public void apply(Industry industry) {
                    super.apply(industry);
                    industry.getMarket().getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD)
                                                    .modifyFlat("nanoforge", DEGRADED_AUTOFORGE_BONUS, Misc.ucFirst(spec.getName().toLowerCase()));
                }
                @Override
                public void unapply(Industry industry) {
                    super.unapply(industry);
                    industry.getMarket().getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD)
                                                    .unmodifyFlat("nanoforge");
                }
                protected void addItemDescription(Industry industry, TooltipMakerAPI text, SpecialItemData data,
					  							  InstallableItemDescriptionMode mode, String pre, float pad) {
			String name = "modular fabricators ";
			if (mode == InstallableItemDescriptionMode.MANAGE_ITEM_DIALOG_LIST) {
				name = "";
			}
			text.addPara(pre + "Increases ship and weapon production quality by %s. " +
				"Increases " + name + "production by %s units.",
				pad, Misc.getHighlightColor(), 
				"" + (int) Math.round(DEGRADED_AUTOFORGE_BONUS * 100f) + "%",
				"" + (int) DEGRADED_AUTOFORGE_PROD);
		}
            });
        }
        if (!ItemEffectsRepo.ITEM_EFFECTS.containsKey(MS_specialItems.PERFECT_AUTOFORGE)) {
            ItemEffectsRepo.ITEM_EFFECTS.put(MS_specialItems.PERFECT_AUTOFORGE, 
                                new BoostIndustryInstallableItemEffect(MS_specialItems.PERFECT_AUTOFORGE, PERFECT_AUTOFORGE_PROD, 0) {
                @Override
		public void apply(Industry industry) {
                    super.apply(industry);
                    industry.getMarket().getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD)
						.modifyFlat("nanoforge", PERFECT_AUTOFORGE_BONUS, Misc.ucFirst(spec.getName().toLowerCase()));
                    }
                @Override
                public void unapply(Industry industry) {
                    super.unapply(industry);
                    industry.getMarket().getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD)
						.unmodifyFlat("nanoforge");
                }
                public void addItemDescription(Industry industry, TooltipMakerAPI text, SpecialItemData data,
					  							  InstallableItemDescriptionMode mode, String pre, float pad) {
			String name = "modular fabricators ";
			if (mode == InstallableItemDescriptionMode.MANAGE_ITEM_DIALOG_LIST) {
				name = "";
			}
                    text.addPara(pre + "Increases ship and weapon production quality by %s. " +
                                        "Increases " + name + " production by %s unit.",
                            pad, Misc.getHighlightColor(), 
                            "" + (int) Math.round(DEGRADED_AUTOFORGE_BONUS * 100f) + "%",
                            "" + (int) DEGRADED_AUTOFORGE_PROD);
                }
            });
        }
    }*/
}
