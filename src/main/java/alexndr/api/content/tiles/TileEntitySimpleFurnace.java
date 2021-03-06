/**
 * 
 */
package alexndr.api.content.tiles;

import javax.annotation.Nullable;

import alexndr.api.content.blocks.SimpleFurnace;
import alexndr.api.helpers.game.FurnaceHelper;
import alexndr.api.helpers.game.SimpleItemStackHelper;
import alexndr.api.logger.LogHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.SlotFurnaceFuel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Inheritable re-write of TileEntityFurnace, for use with SimpleCore machines.
 * @author Sinhika
 *
 */
@Deprecated
public class TileEntitySimpleFurnace extends TileEntityLockable implements
		ITickable, ISidedInventory 
{
    protected static final int NDX_INPUT_SLOT = 0;
    protected static final int NDX_FUEL_SLOT = 1;
    protected static final int NDX_OUTPUT_SLOT = 2;
    
    protected static int[] slotsTop = new int[] {NDX_INPUT_SLOT};  // input
    protected static int[] slotsBottom = new int[] {NDX_OUTPUT_SLOT, NDX_FUEL_SLOT};  // output, fuel
    protected static int[] slotsSides = new int[] {NDX_FUEL_SLOT};  // fuel
    
    /** The ItemStacks that hold the items currently being used in the furnace */
    protected NonNullList<ItemStack> furnaceItemStacks;
    
    /** The number of ticks that the furnace will keep burning */
    protected int furnaceBurnTime;
    
    /** The number of ticks that a fresh copy of the currently-burning item would keep the furnace burning for */
    protected int currentItemBurnTime;
    
    /** number of ticks we've cooked so far. */
    protected int cookTime;
    
    /** number of ticks it takes to cook this item. */
    protected int totalCookTime;		
    
    /** what is this for? */
    protected int maxCookTime;
    
    protected String furnaceCustomName;
    protected String furnaceName;
    protected String furnaceGuiId;
    
    /**
	 * 
	 */
	public TileEntitySimpleFurnace(String tileName, int max_cook_time,
								   String guiID, int furnace_stack_count) 
	{
		LogHelper.verbose("Finished TileEntity ctor for " + tileName);
		this.furnaceName = tileName;
		this.maxCookTime = max_cook_time;
		this.furnaceGuiId = guiID;
		this.furnaceItemStacks = NonNullList.<ItemStack>withSize(3, ItemStack.EMPTY);;
	}

	/*------- MUCH OF THE FOLLOWING IS CUT & PASTED FROM TileEntityFurnace -------------*/
	
	/* (non-Javadoc)
	 * @see net.minecraft.inventory.IInventory#getSizeInventory()
	 */
	@Override
	public int getSizeInventory() 
	{
		return this.furnaceItemStacks.size();
	}

	/* (non-Javadoc)
	 * @see net.minecraft.inventory.IInventory#getStackInSlot(int)
	 */
	@Override
	public ItemStack getStackInSlot(int index) 
	{
        return this.furnaceItemStacks.get(index);
	}

	/* (non-Javadoc)
	 * @see net.minecraft.inventory.IInventory#decrStackSize(int, int)
	 */
	@Override
	public ItemStack decrStackSize(int index, int count) 
	{
        return ItemStackHelper.getAndSplit(this.furnaceItemStacks, index, count);
	} // end decrStackSize()

	/* (non-Javadoc)
	 * @see net.minecraft.inventory.IInventory#removeStackFromSlot(int)
	 */
	@Override
	public ItemStack removeStackFromSlot(int index) 
	{
	       return ItemStackHelper.getAndRemove(this.furnaceItemStacks, index);
	}

	/* (non-Javadoc)
	 * @see net.minecraft.inventory.IInventory#setInventorySlotContents(int, net.minecraft.item.ItemStack)
	 */
	@Override
	public void setInventorySlotContents(int index, @Nullable ItemStack stack) 
	{
        ItemStack itemstack = (ItemStack)this.getStackInSlot(index);
        boolean flag = !stack.isEmpty() && stack.isItemEqual(itemstack) 
        		&& ItemStack.areItemStackTagsEqual(stack, itemstack);
        this.furnaceItemStacks.set(index, stack);

        if (stack.getCount() > this.getInventoryStackLimit())
        {
        	stack.setCount(this.getInventoryStackLimit());
        }

        if (index == NDX_INPUT_SLOT && !flag)
        {
            this.totalCookTime = this.getCookTime(stack);
            this.cookTime = 0;
            this.markDirty();
        }

	} // end ()

	/* (non-Javadoc)
	 * @see net.minecraft.inventory.IInventory#getInventoryStackLimit()
	 */
	@Override
	public int getInventoryStackLimit() {
        return 64;
	}

	/* (non-Javadoc)
	 * @see net.minecraft.inventory.IInventory#isUseableByPlayer(net.minecraft.entity.player.EntityPlayer)
	 */
	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
        return this.getWorld().getTileEntity(this.pos) != this 
                 ? false 
                 : player.getDistanceSq((double)this.pos.getX() + 0.5D, 
                                        (double)this.pos.getY() + 0.5D, 
                                        (double)this.pos.getZ() + 0.5D) <= 64.0D;
	}

	/* (non-Javadoc)
	 * @see net.minecraft.inventory.IInventory#isItemValidForSlot(int, net.minecraft.item.ItemStack)
	 */
	@Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        if (index == NDX_OUTPUT_SLOT)
        {
            return false;
        }
        else if (index != NDX_FUEL_SLOT)
        {
            return true;
        }
        else
        {
            ItemStack itemstack = this.getStackInSlot(NDX_FUEL_SLOT);
            return isItemFuel(stack) || SlotFurnaceFuel.isBucket(stack) 
            							&& itemstack.getItem() != Items.BUCKET;
        }
    } // end isItemValidForSlot()

	public int getMaxCookTime() {
		return maxCookTime;
	}

	public void setMaxCookTime(int maxCookTime) {
		this.maxCookTime = maxCookTime;
	}

	/* (non-Javadoc)
	 * @see net.minecraft.inventory.IInventory#getField(int)
	 */
	@Override
	public int getField(int id) {
        switch (id)
        {
            case 0: // number of ticks that the furnace will still keep burning
                return this.furnaceBurnTime;
            case 1: // max number of ticks we started with for current fuel item burning
                return this.currentItemBurnTime;
            case 2:	// number of ticks we've cooked this item
                return this.cookTime;
            case 3:	// number of ticks total to cook this item.
                return this.totalCookTime;
            default:
                return 0;
        }
	} // end ()

	/* (non-Javadoc)
	 * @see net.minecraft.inventory.IInventory#setField(int, int)
	 */
	@Override
	public void setField(int id, int value) {
        switch (id)
        {
            case 0:
                this.furnaceBurnTime = value;
                break;
            case 1:
                this.currentItemBurnTime = value;
                break;
            case 2:
                this.cookTime = value;
                break;
            case 3:
                this.totalCookTime = value;
                break;
        }
	} // end ()

	/* (non-Javadoc)
	 * @see net.minecraft.inventory.IInventory#getFieldCount()
	 */
	@Override
	public int getFieldCount() {
        return 4;
	}

	/* (non-Javadoc)
	 * @see net.minecraft.inventory.IInventory#clear()
	 */
	@Override
	public void clear() {
	       this.furnaceItemStacks.clear();
	} // end clear()

	/* (non-Javadoc)
	 * @see net.minecraft.world.IWorldNameable#getName()
	 */
	@Override
	public String getName() {
	       return this.hasCustomName() ? this.furnaceCustomName : this.furnaceName;	
	}

	/* (non-Javadoc)
	 * @see net.minecraft.world.IWorldNameable#hasCustomName()
	 */
	@Override
	public boolean hasCustomName() {
        return this.furnaceCustomName != null && this.furnaceCustomName.length() > 0;
	}
	
    public void setCustomInventoryName(String p_145951_1_)
    {
        this.furnaceCustomName = p_145951_1_;
    }

    public static void registerFixesFurnace(DataFixer fixer)
    {
        fixer.registerWalker(FixTypes.BLOCK_ENTITY, 
        					 new ItemStackDataLists(TileEntitySimpleFurnace.class, new String[] {"Items"}));
    }


 	/* (non-Javadoc)
	 * @see net.minecraft.world.IInteractionObject#createContainer(net.minecraft.entity.player.InventoryPlayer, net.minecraft.entity.player.EntityPlayer)
	 * Override this for custom classes.
	 */
	@Override
	public Container createContainer(InventoryPlayer playerInventory,
			EntityPlayer playerIn) 
	{
        return new ContainerFurnace(playerInventory, this);
	}

	/* (non-Javadoc)
	 * @see net.minecraft.world.IInteractionObject#getGuiID()
	 */
	@Override
	public String getGuiID() {
		return furnaceGuiId;
	}

	/* (non-Javadoc)
	 * @see net.minecraft.inventory.ISidedInventory#getSlotsForFace(net.minecraft.util.EnumFacing)
	 */
	@Override
	public int[] getSlotsForFace(EnumFacing side) 
	{
        return ((side == EnumFacing.DOWN) 
                       ? TileEntitySimpleFurnace.slotsBottom 
                       : (side == EnumFacing.UP 
                                       ? TileEntitySimpleFurnace.slotsTop 
                                       : TileEntitySimpleFurnace.slotsSides));
	}

	/* (non-Javadoc)
	 * @see net.minecraft.inventory.ISidedInventory#canInsertItem(int, net.minecraft.item.ItemStack, net.minecraft.util.EnumFacing)
	 */
	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn,
			EnumFacing direction) 
	{
        return this.isItemValidForSlot(index, itemStackIn);
	}

	/* (non-Javadoc)
	 * @see net.minecraft.inventory.ISidedInventory#canExtractItem(int, net.minecraft.item.ItemStack, net.minecraft.util.EnumFacing)
	 */
	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) 
	{
        if (direction == EnumFacing.DOWN && index == NDX_FUEL_SLOT)
        {
            Item item = stack.getItem();

            // TODO rewrite this to work with SimpleBucket items, or Universal buckets.
            if (item != Items.WATER_BUCKET && item != Items.BUCKET)
            {
                return false;
            }
        }

        return true;
	}

	/* (non-Javadoc)
	 * @see net.minecraft.util.ITickable#update()
	 */
	@Override
	public void update() 
	{
        boolean flag = this.isBurning();
        boolean flag1 = false;
        int burnTime = 0;
        
        if (this.isBurning())
        {
            --this.furnaceBurnTime;
        }

        if (!this.getWorld().isRemote)
        {
            ItemStack itemstack = (ItemStack)this.getStackInSlot(NDX_FUEL_SLOT);
            if (!itemstack.isEmpty()) 
			{
                burnTime = TileEntitySimpleFurnace.getItemBurnTime(itemstack);
            }
            flag1 = default_cooking_update(flag1, itemstack, burnTime);
           if (flag != this.isBurning())
            {
                flag1 = true;
                SimpleFurnace.setState(this.isBurning(), this.getWorld(), this.pos);
            } // end-if
        } // end-if

        if (flag1)
        {
            this.markDirty();
        }
	} // end update()
	
	@Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.furnaceItemStacks = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);

        ItemStackHelper.loadAllItems(compound, this.furnaceItemStacks);

        this.furnaceBurnTime = compound.getInteger("BurnTime");
        this.cookTime = compound.getInteger("CookTime");
        this.totalCookTime = compound.getInteger("CookTimeTotal");
        this.currentItemBurnTime = getItemBurnTime((ItemStack)this.getStackInSlot(NDX_FUEL_SLOT));

        if (compound.hasKey("CustomName", 8))
        {
            this.furnaceCustomName = compound.getString("CustomName");
        }
    } // end readFromNBT()

	@Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger("BurnTime", (short)this.furnaceBurnTime);
        compound.setInteger("CookTime", (short)this.cookTime);
        compound.setInteger("CookTimeTotal", (short)this.totalCookTime);
        SimpleItemStackHelper.write_itemStackToNBT(compound, this.furnaceItemStacks);

        if (this.hasCustomName())
        {
            compound.setString("CustomName", this.furnaceCustomName);
        }

        return compound;
    } // end writeToNBT()

    /**
     * Furnace isBurning
     */
    public boolean isBurning()
    {
        return this.furnaceBurnTime > 0;
    }

    @SideOnly(Side.CLIENT)
    public static boolean isBurning(IInventory inventory)
    {
        return inventory.getField(0) > 0;
    }

    // override this as necessary in custom furnace classes.
    public int getCookTime(ItemStack stack)
    {
        return maxCookTime;
    }

    /**
     * Returns true if the furnace can smelt an item, i.e. has a source item, destination stack isn't full, 
     * etc. Obviously will have to be overriden for special multi-slot furnaces like the Fusion Furnace.
     */
    protected boolean canSmelt()
    {
        if (this.getStackInSlot(NDX_INPUT_SLOT).isEmpty())
        {
            return false;
        }
        else
        {
            ItemStack itemstack = 
                 FurnaceRecipes.instance().getSmeltingResult(this.getStackInSlot(NDX_INPUT_SLOT));
            if (itemstack.isEmpty()) 
            {
            	return false;
            }
            else {
                ItemStack itemstack1 = (ItemStack)this.getStackInSlot(NDX_OUTPUT_SLOT);
                if (itemstack1.isEmpty()) return true;
                if (!itemstack1.isItemEqual(itemstack)) return false;
				int result = itemstack1.getCount() + itemstack.getCount();
                return result <= getInventoryStackLimit() 
                         && result <= itemstack1.getMaxStackSize(); // Forge fix: make furnace respect stack sizes in furnace recipes
            }
        }
    } // end canSmelt()

    /**
     * Turn one item from the furnace source stack into the appropriate smelted item in the furnace result 
     * stack. Override for special multi-slot furnaces like the Fusion Furnace.
     */
    public void smeltItem()
    {
        if (this.canSmelt())
        {
            ItemStack instack = (ItemStack)this.getStackInSlot(NDX_INPUT_SLOT);
            ItemStack result_stack = FurnaceRecipes.instance().getSmeltingResult(instack);
            ItemStack outstack = (ItemStack)this.getStackInSlot(NDX_OUTPUT_SLOT);

            if (outstack.isEmpty())
            {
				FurnaceHelper.SetInSlot(furnaceItemStacks, NDX_OUTPUT_SLOT, result_stack.copy());

            }
            else if (ItemStack.areItemsEqual(outstack, result_stack))
            {
            	outstack.grow(result_stack.getCount());
            }
            if (instack.getItem() == Item.getItemFromBlock(Blocks.SPONGE) 
            	&& instack.getMetadata() == 1 
            	&& !this.getStackInSlot(NDX_FUEL_SLOT).isEmpty() 
            	&& (this.getStackInSlot(NDX_FUEL_SLOT)).getItem() == Items.BUCKET)
            {
                FurnaceHelper.SetInSlot(furnaceItemStacks, NDX_FUEL_SLOT,
                                        new ItemStack(Items.WATER_BUCKET));
            }

            //ItemStackTools.incStackSize(instack, -1);
            this.decrStackSize(NDX_INPUT_SLOT, 1);
        }
    } // end smeltItem()

    /**
     * Returns the number of ticks that the supplied fuel item will keep the furnace burning, or 0 
     * if the item isn't fuel.
     */
    public static int getItemBurnTime(ItemStack burnItem)
    {
        if (burnItem.isEmpty())
        {
            return 0;
        }
        else
        {
            Item item = burnItem.getItem();

            if (item instanceof net.minecraft.item.ItemBlock 
            		&& Block.getBlockFromItem(item) != Blocks.AIR)
            {
                Block block = Block.getBlockFromItem(item);

                if (block == Blocks.WOODEN_SLAB)
                {
                    return 150;
                }

                if (block.getDefaultState().getMaterial() == Material.WOOD)
                {
                    return 300;
                }

                if (block == Blocks.COAL_BLOCK)
                {
                    return 16000;
                }
            }

            if (item instanceof ItemTool && ((ItemTool)item).getToolMaterialName().equals("WOOD")) return 200;
            if (item instanceof ItemSword && ((ItemSword)item).getToolMaterialName().equals("WOOD")) return 200;
            if (item instanceof ItemHoe && ((ItemHoe)item).getMaterialName().equals("WOOD")) return 200;
            if (item == Items.STICK) return 100;
            if (item == Items.COAL) return 1600;
            if (item == Items.LAVA_BUCKET) return 20000;
            if (item == Item.getItemFromBlock(Blocks.SAPLING)) return 100;
            if (item == Items.BLAZE_ROD) return 2400;
            return ForgeEventFactory.getItemBurnTime(burnItem);
        }
    } // end getItemBurnTime()

    /**
     * Returns the number of ticks that the supplied fuel item will keep the furnace burning, or 0 if the item isn't
     * fuel
     */
    public static boolean isItemFuel(ItemStack stack)
    {
        return getItemBurnTime(stack) > 0;
    }

	@Override
	public void openInventory(EntityPlayer player) {}

	@Override
	public void closeInventory(EntityPlayer player) {}

    net.minecraftforge.items.IItemHandler handlerTop = new net.minecraftforge.items.wrapper.SidedInvWrapper(this, net.minecraft.util.EnumFacing.UP);
    net.minecraftforge.items.IItemHandler handlerBottom = new net.minecraftforge.items.wrapper.SidedInvWrapper(this, net.minecraft.util.EnumFacing.DOWN);
    net.minecraftforge.items.IItemHandler handlerSide = new net.minecraftforge.items.wrapper.SidedInvWrapper(this, net.minecraft.util.EnumFacing.WEST);

    @SuppressWarnings("unchecked")
	@Override
    public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, net.minecraft.util.EnumFacing facing)
    {
        if (facing != null && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            if (facing == EnumFacing.DOWN)
                return (T) handlerBottom;
            else if (facing == EnumFacing.UP)
                return (T) handlerTop;
            else
                return (T) handlerSide;
        return super.getCapability(capability, facing);
    }

	public boolean func_191420_l() {
        for (ItemStack itemstack : this.furnaceItemStacks)
        {
            if (!itemstack.isEmpty())
            {
                return false;
            }
        }
        return true;
	}
    
	protected boolean default_cooking_update(boolean flag1, ItemStack itemstackFuel, int burnTime)
	{
        if (this.isBurning() || !itemstackFuel.isEmpty() 
            &&  !this.getStackInSlot(NDX_INPUT_SLOT).isEmpty())
        {
            if (!this.isBurning() && this.canSmelt())
            {
                this.furnaceBurnTime = burnTime;
                this.currentItemBurnTime = this.furnaceBurnTime;

                if (this.isBurning())
                {
                    flag1 = true;

                    if (!itemstackFuel.isEmpty())
                    {
                        Item item = itemstackFuel.getItem();
                        itemstackFuel.shrink(1);
                        if (!itemstackFuel.isEmpty()) {
                            ItemStack item1 = item.getContainerItem(itemstackFuel);
                            this.furnaceItemStacks.set(NDX_FUEL_SLOT, item1);
                        }
                    }
                } // end-if isBurning
            } // end-if !isBurning && canSmelt

            if (this.isBurning() && this.canSmelt())
            {
                ++this.cookTime;

                if (this.cookTime == this.totalCookTime)
                {
                    this.cookTime = 0;
                    this.totalCookTime = this.getCookTime(this.getStackInSlot(NDX_INPUT_SLOT));
                    this.smeltItem();
                    flag1 = true;
                }
            }
            else
            {
                this.cookTime = 0;
            }
        } // end-if isBurnning && valid FUEL && valid INPUT
        else if (!this.isBurning() && this.cookTime > 0)
        {
            this.cookTime = MathHelper.clamp(this.cookTime - 2, 0, this.totalCookTime);
        }
        return flag1;
	} // end default_cooking_update()

	@Override
	public boolean isEmpty() 
	{
        for (ItemStack itemstack : this.furnaceItemStacks)
        {
            if (!itemstack.isEmpty())
            {
                return false;
            }
        }
        return true;
	}

} // end class
