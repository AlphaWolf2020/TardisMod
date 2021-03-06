package tardis.common.blocks;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;

import io.darkcraft.darkcore.mod.abstracts.AbstractItemBlock;

import cpw.mods.fml.common.registry.GameRegistry;
import tardis.TardisMod;
import tardis.common.recipes.LabRecipeRegistry;
import tardis.common.tileents.BatteryTileEntity;
import tardis.common.tileents.extensions.CraftingComponentType;
import tardis.common.tileents.extensions.LabFlag;
import tardis.common.tileents.extensions.LabRecipe;

public class BatteryBlock extends AbstractScrewableBlockContainer
{
	public BatteryBlock()
	{
		super(false,true,TardisMod.modName);
		setCreativeTab(TardisMod.cTab);
	}

	@Override
	public Class<? extends AbstractItemBlock> getIB()
	{
		return BatteryBlockItemBlock.class;
	}

	@Override
	public TileEntity createNewTileEntity(World w, int m)
	{
		return new BatteryTileEntity(m+1);
	}

	@Override
	public void initData()
	{
		setBlockName("Battery");
		setSubNames("Basic","Advanced","Temporal");
	}

	@Override
	public ItemStack getIS(int am, int dam)
	{
		ItemStack is = super.getIS(am, dam);
		NBTTagCompound d;
		if(is.stackTagCompound == null)
			d = new NBTTagCompound();
		else
			d = is.stackTagCompound;
		d.setInteger("ae",0);
		is.stackTagCompound = d;
		return is;
	}

	@Override
	public void initRecipes()
	{
		GameRegistry.addRecipe(new ShapedOreRecipe(getIS(1,0), false, "cdc","dkd", "cdc",
				'c', CraftingComponentType.CHRONOSTEEL.getIS(1),
				'd', CraftingComponentType.DALEKANIUM.getIS(1),
				'k', CraftingComponentType.KONTRON.getIS(1)));
		GameRegistry.addRecipe(new ShapedOreRecipe(getIS(1,1), false, "cdc","dkd", "cdc",
				'c', CraftingComponentType.CHRONOSTEEL.getIS(1),
				'd', CraftingComponentType.DALEKANIUM.getIS(1),
				'k', getIS(1,0)));
		LabRecipeRegistry.addRecipe(new LabRecipe("tm.battery3",getIS(1,1), getIS(1,2), EnumSet.of(LabFlag.INFLIGHT),200));
	}

	@Override
	public Class<? extends TileEntity> getTEClass()
	{
		return BatteryTileEntity.class;
	}

}
