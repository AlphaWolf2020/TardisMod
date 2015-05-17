package tardis.common.command;

import io.darkcraft.darkcore.mod.abstracts.AbstractCommand;
import io.darkcraft.darkcore.mod.helpers.WorldHelper;

import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import tardis.common.core.Helper;
import tardis.common.core.TardisOutput;

public class SchemaLoadCommand extends AbstractCommand
{
	@Override
	public String getCommandName()
	{
		return "tardisload";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender)
	{
		return "/tardisload <schema> <x> <y> <z> <facing>";
	}

	@Override
	public void addAliases(List<String> aliases)
	{
		aliases.add("tload");
	}

	@Override
	public void commandBody(ICommandSender comSen, String[] astring)
	{
		if(comSen instanceof EntityPlayerMP)
		{
			EntityPlayerMP pl = (EntityPlayerMP)comSen;
			if(astring.length == 5)
			{
				String name = astring[0];
				TardisOutput.print("TLC", "Attempting to load " + name +" in dim " + WorldHelper.getWorldID(pl.worldObj));
				try
				{
					int x = Integer.parseInt(astring[1]);
					int y = Integer.parseInt(astring[2]);
					int z = Integer.parseInt(astring[3]);
					int facing = Integer.parseInt(astring[4]);
					Helper.loadSchema(name, pl.worldObj, x, y, z, facing);
				}
				catch(NumberFormatException e)
				{
					sendString(pl,"Totally not numbers");
				}
				catch(Exception e)
				{
					TardisOutput.print("TSSC", "ERROR:" + e.getMessage(),TardisOutput.Priority.ERROR);
					e.printStackTrace();
				}
			}
		}
	}
}
