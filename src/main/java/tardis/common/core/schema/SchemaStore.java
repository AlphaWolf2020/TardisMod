package tardis.common.core.schema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.GameData;
import tardis.common.TMRegistry;
import tardis.common.core.TardisOutput;
import tardis.common.tileents.SchemaCoreTileEntity;

public class SchemaStore
{
	private String							blockName	= null;
	private Block							block;
	private int								blockMeta;
	private NBTTagCompound					nbtStore	= null;

	public static final SchemaStore			airBlock	= new SchemaStore(Blocks.air, 0, null);

	private static HashSet<Block>			bannedIDs	= null;
	private static HashMap<String, Block>	blockCache	= new HashMap<String, Block>();

	public Block getBlock()
	{
		return block;
	}

	public int getBlockMeta()
	{
		return blockMeta;
	}

	private SchemaStore(Block bid, int bm, NBTTagCompound nbt)
	{
		block = bid;
		blockMeta = bm;
		nbtStore = nbt;
	}

	private SchemaStore()
	{
		if (bannedIDs == null)
		{
			bannedIDs = new HashSet<Block>();
			bannedIDs.add(TMRegistry.tardisCoreBlock);
			bannedIDs.add(TMRegistry.tardisConsoleBlock);
			bannedIDs.add(TMRegistry.tardisEngineBlock);
		}
	}

	public void loadToWorld(World w, int x, int y, int z)
	{
		loadToWorld(w, blockMeta, x, y, z);
	}

	public void loadToWorld(World w, int meta, int x, int y, int z)
	{
		if ((block == TMRegistry.decoBlock) && (meta == 6))
		{
			block = TMRegistry.decoTransBlock;
			meta = blockMeta = 0;
		}
		if (block == TMRegistry.decoBlock)
		{
			switch(meta)
			{
				case 1: meta = 0; break;
				case 2: block = TMRegistry.colorableRoundelBlock; meta = 3; break;
				case 4: block = TMRegistry.colorableRoundelBlock; meta = 15; break;
				case 3: block = TMRegistry.colorableWallBlock; meta = 15; break;
				case 5: block = TMRegistry.colorableFloorBlock; meta = 15; break;
				case 7: block = TMRegistry.colorableWallBlock; meta = 3; break;
			}
		}

		w.setBlock(x, y, z, block, meta, 3);
		if (nbtStore != null)
		{
			nbtStore.setInteger("x", x);
			nbtStore.setInteger("y", y);
			nbtStore.setInteger("z", z);
			TileEntity newTileEntity = TileEntity.createAndLoadEntity(nbtStore);
			if (newTileEntity != null)
			{
				newTileEntity.setWorldObj(w);
				w.setTileEntity(x, y, z, newTileEntity);
				newTileEntity.validate();
			}
		}
	}

	public static SchemaStore storeWorldBlock(World w, int x, int y, int z)
	{
		if (bannedIDs == null)
		{
			bannedIDs = new HashSet<Block>();
			bannedIDs.add(TMRegistry.tardisCoreBlock);
			bannedIDs.add(TMRegistry.tardisConsoleBlock);
			bannedIDs.add(TMRegistry.tardisEngineBlock);
		}

		if ((w.getBlock(x, y, z) == Blocks.air) || bannedIDs.contains(w.getBlock(x, y, z))) return null;

		SchemaStore newStore = new SchemaStore();
		newStore.block = w.getBlock(x, y, z);
		newStore.blockMeta = w.getBlockMetadata(x, y, z);
		TileEntity te = w.getTileEntity(x, y, z);
		if ((te != null) && !(te instanceof SchemaCoreTileEntity))
		{
			newStore.nbtStore = new NBTTagCompound();
			te.writeToNBT(newStore.nbtStore);
		}
		newStore.blockName = getNameFromBlock(newStore.block);
		return newStore;
	}

	public static SchemaStore loadFromNBT(NBTTagCompound nbt)
	{
		SchemaStore newStore = new SchemaStore();
		newStore.blockMeta = nbt.getInteger("tdSchemaBMD");
		if (nbt.hasKey("tdSchemaBName"))
		{
			String name = nbt.getString("tdSchemaBName");
			Block nbid = getBlockFromName(name);
			if (nbid != null)
			{
				newStore.block = nbid;
			}
			else
			{
				newStore.block = Blocks.air;
			}
		}
		if (nbt.hasKey("tdSchemaNBT")) newStore.nbtStore = nbt.getCompoundTag("tdSchemaNBT");
		// TardisOutput.print("TSS", newStore.toString());
		return newStore;
	}

	public NBTTagCompound getTagCompound()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("tdSchemaBMD", blockMeta);
		if (blockName == null) blockName = getNameFromBlock(block);
		if (blockName != null) nbt.setString("tdSchemaBName", blockName);
		if (nbtStore != null) nbt.setTag("tdSchemaNBT", nbtStore);
		return nbt;
	}

	@Override
	public String toString()
	{
		return "SchemaStore [blockID=" + block.getUnlocalizedName() + ", blockMeta=" + blockMeta + "]";
	}

	private static String getNameFromBlock(Block id)
	{
		String blockName = GameData.getBlockRegistry().getNameForObject(id);
		if (blockName != null) return blockName;
		if (id != null) return id.getUnlocalizedName();
		return null;
	}

	private static Block getBlockFromName(String name)
	{
		Block b = null;
		b = GameData.getBlockRegistry().getObject(name);
		if ((b != null) && !b.equals(Blocks.air))
			return b;
		else
		{
			if (blockCache.containsKey(name))
				return blockCache.get(name);
			else
			{
				if (name.equals("TMRegistry.tile.TardisMod.DecoBlockDark")) return TMRegistry.decoBlock;
				Iterator<Block> blockIter = GameData.getBlockRegistry().iterator();
				while (blockIter.hasNext())
				{
					b = blockIter.next();
					if (b.getUnlocalizedName().equals(name))
					{
						if ((name == TMRegistry.decoBlock.getUnlocalizedName()) && (b != TMRegistry.decoBlock)) continue;
						blockCache.put(name, b);
						return b;
					}
					else
						blockCache.put(b.getUnlocalizedName(), b);
				}
			}
		}
		if (!b.equals(Blocks.air)) TardisOutput.print("TSS", "No block found for " + name + ":(");
		return null;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((block == null) ? 0 : block.hashCode());
		result = (prime * result) + blockMeta;
		result = (prime * result) + ((blockName == null) ? 0 : blockName.hashCode());
		result = (prime * result) + ((nbtStore == null) ? 0 : nbtStore.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof SchemaStore)) return false;
		SchemaStore other = (SchemaStore) obj;
		if (block == null)
		{
			if (other.block != null) return false;
		}
		else if (!block.equals(other.block)) return false;
		if (blockMeta != other.blockMeta) return false;
		if (blockName == null)
		{
			if (other.blockName != null) return false;
		}
		else if (!blockName.equals(other.blockName)) return false;
		if (nbtStore == null)
		{
			if (other.nbtStore != null) return false;
		}
		else if (!nbtStore.equals(other.nbtStore)) return false;
		return true;
	}

}
