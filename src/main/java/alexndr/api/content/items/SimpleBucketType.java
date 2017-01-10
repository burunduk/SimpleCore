package alexndr.api.content.items;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * @author AleXndrTheGr8st
 */
public class SimpleBucketType 
{
	private String material;
	private boolean destroyOnLava = false;
	private List<BucketVariant> variantList = Lists.newArrayList();
	
	public static int DESTROY_ON_LAVA_TEMP = 1300;
	
	/**
	 * Creates a new SimpleBucketType. 
	 * This is used for storing liquid variants of the bucket.
	 * @param material The name of the bucket material, eg. "copper"
	 */
	public SimpleBucketType(String material) {
		this.material = material;
	}
	
	/**
	 * Returns whether or not the bucket should be destroyed when attempting to collect lava.
	 * @return Whether or not to destroy bucket
	 */
	public boolean getDestroyOnLava() {
		return this.destroyOnLava;
	}
	
	/**
	 * Sets whether or not the bucket should be destroyed when attempting to collect lava.
	 * Default is false, so don't need to set this if you don't want it to be destroyed.
	 * @param destroyOnLava Destroy the bucket when collecting lava
	 */
	public void setDestroyOnLava(boolean destroyOnLava) {
		this.destroyOnLava = destroyOnLava;
	}
	
	/**
	 * Returns the material of the bucket type, eg. "copper".
	 * @return Bucket material
	 */
	public String getMaterial() {
		return this.material;
	}
	
	/**
	 * Adds a new variant to this bucket type. 
	 * @param name The name of the variant, eg. "Water"
	 * @param bucket The bucket item
	 * @param liquidBlock The liquid in the bucket
	 * @return SimpleBucketType
	 */
	public SimpleBucketType addVariant(String name, Fluid fluid) 
	{
		BucketVariant variant = 
		     new BucketVariant(name, new FluidStack(fluid, Fluid.BUCKET_VOLUME));
		this.variantList.add(variant);
		return this;
	}
	
	   /**
     * Adds a new variant to this bucket type. 
     * @param name The name of the variant, eg. "Water"
     * @param bucket The bucket item
     * @param liquidBlock The liquid in the bucket
     * @return SimpleBucketType
     */
    public SimpleBucketType addVariant(String name, FluidStack fsfluid) 
    {
        BucketVariant variant = new BucketVariant(name, fsfluid);
        this.variantList.add(variant);
        return this;
    }

	/**
	 * Returns a list of the liquid variants belonging to this SimpleBucketType.
	 * @return List of liquid variants.
	 */
	public List<FluidStack> getLiquidsList() {
		List<FluidStack> liquidList = Lists.newArrayList();
		for(BucketVariant variant : this.variantList) {
			liquidList.add(variant.liquidBlock);
		}
		return liquidList;
	}
	
	/**
	 * Checks if a liquid variant exists for this SimpleBucketType.
	 * Returns true if it exists, false otherwise.
	 * @param liquid The liquid to check for
	 * @return If the variant exists
	 */
	public boolean doesVariantExist(Fluid liquid) {
		for(BucketVariant variant : this.variantList) {
			if(variant.liquidBlock.getFluid() == liquid)
				return true;
		}
		return false;
	}

   /**
     * Checks if a liquid variant exists for this SimpleBucketType.
     * Returns true if it exists, false otherwise.
     * @param liquid The liquid to check for
     * @return If the variant exists
     */
    public boolean doesVariantExist(FluidStack fsliquid) {
        for(BucketVariant variant : this.variantList) {
            if(variant.liquidBlock == fsliquid)
                return true;
        }
        return false;
    }
    

//	/**
//	 * Returns a bucket variant containing the given liquid, if it exists.
//	 * Returns null if it doesn't exist.
//	 * @param liquid The liquid to check for
//	 * @return SimpleBucket containing the given liquid
//	 */
//	public Item getBucketFromLiquid(Fluid liquid) {
//		for(BucketVariant variant : this.variantList) {
//			if(variant.liquidBlock == liquid) 
//				return variant.bucket;
//		}
//		return null;
//	}
} // end class SimpleBucketType

class BucketVariant 
{
	String name;
	FluidStack liquidBlock;
	
	/**
	 * Creates a new BucketVariant that stores details on the bucket variant.
	 * @param name Name of the variant, eg. "Water"
	 * @param liquidBlock The liquid in the bucket
	 */
	public BucketVariant(String name, FluidStack liquidBlock) {
		this.name = name;
		this.liquidBlock = liquidBlock;
	}
} // end class BucketVariant
