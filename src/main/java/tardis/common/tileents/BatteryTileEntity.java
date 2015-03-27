package tardis.common.tileents;

import io.darkcraft.darkcore.mod.abstracts.AbstractTileEntity;
import io.darkcraft.darkcore.mod.helpers.ServerHelper;
import io.darkcraft.darkcore.mod.helpers.WorldHelper;
import io.darkcraft.darkcore.mod.interfaces.IActivatable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import tardis.TardisMod;
import tardis.api.IArtronEnergyProvider;
import tardis.api.IScrewable;
import tardis.api.ScrewdriverMode;
import tardis.common.core.ConfigFile;
import tardis.common.core.Helper;
import tardis.common.core.TardisOutput;
import tardis.common.tileents.extensions.LabFlag;

public class BatteryTileEntity extends AbstractTileEntity implements IArtronEnergyProvider, IScrewable, IActivatable
{
	private static ConfigFile config = null;
	private static int maxEnergyPerLevel = 100;
	private static int energyPerLevel = 1;
	private static int ticksPerEnergy = 20;
	
	private int level = 1;
	private int artronEnergy = 0;
	private int mode = 0; //0 landed, 1 uncoordinated, 2 coordinated
	
	private static double rotSpeed = 4;
	private double angle = 0; //max = 359.99
	private boolean changed = false;

	static
	{
		if(config == null)
		{
			config = TardisMod.configHandler.getConfigFile("ArtronBattery");
			maxEnergyPerLevel = config.getInt("max energy per level",100);
			energyPerLevel = config.getInt("energy per level", 1);
			ticksPerEnergy = config.getInt("ticks per energy", 20);
		}
	}
	
	public BatteryTileEntity(int _level)
	{
		level = _level;
	}
	
	public BatteryTileEntity()
	{
		level = -1;
	}
	
	@Override
	public void init()
	{
		if(level == -1)
			level = worldObj.getBlockMetadata(xCoord, yCoord, zCoord) +1;
	}
	
	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if((tt % ticksPerEnergy == 0) && (artronEnergy > 0 || Helper.isTardisWorld(worldObj)))
			addArtronEnergy(level * energyPerLevel,false);
		if(tt % 1200 == 0)
			sendUpdate();
		if(tt % 15 == 0 && changed)
		{
			changed = false;
			sendUpdate();
		}
			
		if(!ServerHelper.isServer())
		{
			angle += (rotSpeed * getArtronEnergy() / getMaxArtronEnergy());
			while(angle >= 360)
				angle -=360;
		}
	}
	
	@Override
	public int getMaxArtronEnergy()
	{
		return level * maxEnergyPerLevel;
	}

	@Override
	public int getArtronEnergy()
	{
		return artronEnergy;
	}

	@Override
	public boolean addArtronEnergy(int amount, boolean sim)
	{
		if(artronEnergy < getMaxArtronEnergy())
		{
			if(!sim)
				artronEnergy = Math.min(getMaxArtronEnergy(), artronEnergy+amount);
			return true;
		}
		return false;
	}

	@Override
	public boolean takeArtronEnergy(int amount, boolean sim)
	{
		if(artronEnergy >= amount)
		{
			if(!sim)
			{
				artronEnergy -= amount;
				changed = true;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean doesSatisfyFlag(LabFlag flag)
	{
		switch(flag)
		{
			case NOTINFLIGHT : return mode == 0;
			case INFLIGHT : return mode != 0;
			case INUNCOORDINATEDFLIGHT : return mode == 1;
			case INCOORDINATEDFLIGHT : return mode == 2;
			default : return false;
		}
	}
	
	@Override
	public boolean screw(ScrewdriverMode mode, EntityPlayer player)
	{
		if(mode.equals(ScrewdriverMode.Dismantle))
		{
			TardisOutput.print("BTE", xCoord +"," + yCoord +","+ zCoord);
			int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
			ItemStack is = new ItemStack(TardisMod.battery,1,meta);
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
			is.stackTagCompound = nbt;
			WorldHelper.giveItemStack(player, is);
			worldObj.setBlockToAir(xCoord, yCoord, zCoord);
			return true;
		}
		return false;
	}

	@Override
	public void writeTransmittable(NBTTagCompound nbt)
	{
		TardisOutput.print("BTE", "Writing stuff to nbt");
		nbt.setInteger("ae", artronEnergy);
		nbt.setInteger("l", level);
		nbt.setInteger("m", mode);
	}

	@Override
	public void readTransmittable(NBTTagCompound nbt)
	{
		artronEnergy = nbt.getInteger("ae");
		level = nbt.getInteger("l");
		mode = nbt.getInteger("m");
	}

	@Override
	public boolean activate(EntityPlayer pl, int side)
	{
		mode = (mode + 1) % 3;
		sendUpdate();
		return true;
	}
	
	public double getAngle()
	{
		return angle;
	}
	
	public int getMode()
	{
		return mode;
	}
	
	public int getLevel()
	{
		return level;
	}

}