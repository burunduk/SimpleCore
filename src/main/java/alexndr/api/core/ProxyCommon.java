package alexndr.api.core;

import alexndr.api.helpers.game.OreGenerator;
import alexndr.api.logger.LogHelper;
import alexndr.api.registry.ContentCategories;
import alexndr.api.registry.ContentRegistry;
import alexndr.api.registry.Plugin;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;


/**
 * @author AleXndrTheGr8st
 */
public class ProxyCommon 
{
	public void preInit(FMLPreInitializationEvent event) 
	{
		//Configuration
		APISettings.createOrLoadSettings(event);
		LogHelper.loggerSetup();
		
		//Content
		if (false == APISettings.tabs)
		{
			addVanillaTabs();
		}
		
		try {
			ModBlocks.configureBlocks();
		} 
		catch (Exception e) {
			LogHelper.severe(APIInfo.NAME,
					"Content pre-init failed. This is a serious problem!");
			e.printStackTrace();
		}
	
	} // end preInit()
	
    public void load(FMLInitializationEvent event)
    {
		//World Generator
		GameRegistry.registerWorldGenerator(new OreGenerator(), 1);
    	
    } // end load()

    public void postInit(FMLPostInitializationEvent event) 
    { 
    } // end postInit()    

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) 
	{
   	 	//Registers
		ModBlocks.register(event.getRegistry());
	} // end registerBlocks()

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) 
	{
    	ModBlocks.registerItemBlocks(event.getRegistry());
	}
    
    public void registerItemRenderer(Plugin plugin, Item item, int meta, String id) {
    }
    
	/**
	 * Adds the vanilla Minecraft tabs to the ContentRegistry.
	 */
	private void addVanillaTabs() 
	{
		LogHelper.verbose("Adding vanilla tabs to ContentRegistry");
		ContentRegistry.registerPlugin(SimpleCoreAPI.vanilla);
		ContentRegistry.registerTab(SimpleCoreAPI.vanilla, CreativeTabs.BUILDING_BLOCKS,
				CreativeTabs.BUILDING_BLOCKS.getTabLabel(), ContentCategories.CreativeTab.GENERAL);
		ContentRegistry.registerTab(SimpleCoreAPI.vanilla, CreativeTabs.MISC, CreativeTabs.MISC.getTabLabel(),
				ContentCategories.CreativeTab.OTHER);
		ContentRegistry.registerTab(SimpleCoreAPI.vanilla, CreativeTabs.BREWING, CreativeTabs.BREWING.getTabLabel(),
				ContentCategories.CreativeTab.OTHER);
		ContentRegistry.registerTab(SimpleCoreAPI.vanilla, CreativeTabs.COMBAT, CreativeTabs.COMBAT.getTabLabel(),
				ContentCategories.CreativeTab.COMBAT);
		ContentRegistry.registerTab(SimpleCoreAPI.vanilla, CreativeTabs.DECORATIONS, CreativeTabs.DECORATIONS.getTabLabel(),
				ContentCategories.CreativeTab.DECORATIONS);
		ContentRegistry.registerTab(SimpleCoreAPI.vanilla, CreativeTabs.FOOD, CreativeTabs.FOOD.getTabLabel(),
				ContentCategories.CreativeTab.OTHER);
		ContentRegistry.registerTab(SimpleCoreAPI.vanilla, CreativeTabs.MATERIALS, CreativeTabs.MATERIALS.getTabLabel(),
				ContentCategories.CreativeTab.MATERIALS);
		ContentRegistry.registerTab(SimpleCoreAPI.vanilla, CreativeTabs.REDSTONE, CreativeTabs.REDSTONE.getTabLabel(),
				ContentCategories.CreativeTab.REDSTONE);
		ContentRegistry.registerTab(SimpleCoreAPI.vanilla, CreativeTabs.TOOLS, CreativeTabs.TOOLS.getTabLabel(),
				ContentCategories.CreativeTab.TOOLS);
		ContentRegistry.registerTab(SimpleCoreAPI.vanilla, CreativeTabs.TRANSPORTATION, CreativeTabs.TRANSPORTATION.getTabLabel(),
				ContentCategories.CreativeTab.OTHER);
	} // end addVanillaTabs()
	
} // end class
