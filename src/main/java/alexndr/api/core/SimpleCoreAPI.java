package alexndr.api.core;

import java.util.List;

import alexndr.api.content.inventory.SimpleTab;
import alexndr.api.helpers.events.CommonEventHelper;
import alexndr.api.helpers.game.TabHelper;
import alexndr.api.helpers.game.TestFurnaceGuiHandler;
import alexndr.api.logger.LogHelper;
import alexndr.api.registry.ContentCategories;
import alexndr.api.registry.Plugin;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

/**
 * @author AleXndrTheGr8st
 */
@Mod(modid = APIInfo.ID, name = APIInfo.NAME, version = APIInfo.VERSION,
	dependencies=APIInfo.DEPENDENCIES, acceptedMinecraftVersions=APIInfo.ACCEPTED_VERSIONS,
	updateJSON=APIInfo.VERSIONURL)
public class SimpleCoreAPI 
{
	@SidedProxy(clientSide = "alexndr.api.core.ProxyClient", 
			    serverSide = "alexndr.api.core.ProxyCommon")
	public static ProxyCommon proxy;
	
	@Mod.Instance
	public static SimpleCoreAPI instance;
	
	public static Plugin plugin = new Plugin(APIInfo.ID, APIInfo.NAME);
	public static Plugin vanilla = new Plugin("minecraft", "Minecraft");
	
	//Creative Tabs
	private static boolean iconsSet = false;
	private static SimpleTab simpleBlocks, simpleDecorations, simpleMaterials, 
							 simpleTools, simpleCombat, simpleMachines;

	public SimpleCoreAPI() {
		FluidRegistry.enableUniversalBucket();
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) 
	{
		LogHelper.info("SimpleCore API Loading...");
		//Register Event Stuff
		MinecraftForge.EVENT_BUS.register(new CommonEventHelper());
		
		proxy.preInit(event);
		// tabPreInit();  // plugin should call this, not API. 
	} // end preInit()
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) 
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(this, (IGuiHandler) new TestFurnaceGuiHandler());
		proxy.load(event);
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) 
	{
		// LogHelper.verbose("Total number of mods UpdateChecker is checking for = " + UpdateChecker.getNumberOfMods());
		LogHelper.info("SimpleCore API Loading Complete!");
	}
	
	/**
	 * create tab or separate tabs for SimpleCore-based plugins. Note that SimpleCoreAPI itself
	 * SHOULD NOT CALL THIS, as it will result in crashes if SimpleCoreAPI is loaded stand-alone,
	 * or with any plugin that does not call setTabIcons -- Sinhika
	 */
	public static void tabPreInit() 
	{
		LogHelper.verbose("Creating tabs");
		if(APISettings.tabs) 
		{
			simpleBlocks = new SimpleTab(SimpleCoreAPI.plugin, "SimpleBlocks", ContentCategories.CreativeTab.BLOCKS);
			if(APISettings.separateTabs) 
			{
				simpleDecorations = new SimpleTab(SimpleCoreAPI.plugin, "SimpleDecorations", ContentCategories.CreativeTab.DECORATIONS);
				simpleMaterials = new SimpleTab(SimpleCoreAPI.plugin, "SimpleMaterials", ContentCategories.CreativeTab.MATERIALS);
				simpleTools = new SimpleTab(SimpleCoreAPI.plugin, "SimpleTools", ContentCategories.CreativeTab.TOOLS);
				simpleCombat = new SimpleTab(SimpleCoreAPI.plugin, "SimpleCombat", ContentCategories.CreativeTab.COMBAT);
				simpleMachines = new SimpleTab(SimpleCoreAPI.plugin, "SimpleMachines", 
						   ContentCategories.CreativeTab.REDSTONE);
			}
		}
		TabHelper.setTabInitDone(true);
	} // end tabPreInit()
	
	/**
	 * Sets the Icons for the CreativeTabs added by this mod. Call this during Initialisation phase.
	 * Must be in correct order, with the correct number of elements (5). They are: 
	 * 1. SimpleBlocks.
	 * 2. SimpleDecorations.
	 * 3. SimpleMaterials.
	 * 4. SimpleTools.
	 * 5. SimpleCombat.
	 * 6. SimpleMachines
	 * @param iconItemsList List of Items with which to set the tab icons
	 */
	public static void setTabIcons(List<Item> iconItemsList) {
		if(!iconsSet) {
			iconsSet = true;
			if(APISettings.tabs) {
				simpleBlocks.setIcon(iconItemsList.get(0));
				if(APISettings.separateTabs) 
				{
					simpleDecorations.setIcon(iconItemsList.get(1));
					simpleMaterials.setIcon(iconItemsList.get(2));
					simpleTools.setIcon(iconItemsList.get(3));
					simpleCombat.setIcon(iconItemsList.get(4));
					simpleMachines.setIcon(iconItemsList.get(5));
				}
			}
		}
	} // end setTabIcons()
	
	

} // end class
