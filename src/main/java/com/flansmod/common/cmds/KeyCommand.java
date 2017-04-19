package com.flansmod.common.cmds;

import com.flansmod.common.FlansMod;
import com.flansmod.common.driveables.EntityDriveable;
import com.flansmod.common.driveables.EntitySeat;
import com.flansmod.common.util.Config;
import com.flansmod.fvm.LandVehicle;

import net.fexcraft.mod.lib.api.item.KeyItem.KeyType;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

public class KeyCommand extends CommandBase {
	
	public static final String NAME = "fmKey";
	
	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/" + NAME;
	}
	
	@Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
    	if(sender instanceof EntityPlayer){
    		return true;
    	}
    	else return false;
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(sender instanceof EntityPlayer == false){
			Print.chat(sender, "This command cann be only executed ingame!");
			return;
		}
		if(sender.getCommandSenderEntity().getRidingEntity() instanceof EntitySeat == false && sender.getCommandSenderEntity().getRidingEntity() instanceof com.flansmod.fvm.EntitySeat == false){
			Print.chat(sender, "You must be inside a vehicle to generate a key.");
			return;
		}
		if(args.length < 1){
			Print.chat(sender, "No Arguments speficied.");
			Print.chat(sender, "/" + NAME + " ?");
		}
		else if(args[0].equals("help") || args[0].equals("?")){
			Print.chat(sender, "Availavble subcommads:");
			Print.chat(sender, "/" + NAME + " help");
			Print.chat(sender, "/" + NAME + " universal");
			Print.chat(sender, "/" + NAME + " get <one/all>");
			Print.chat(sender, "/" + NAME + " viewcode");
			Print.chat(sender, "/" + NAME + " togglelock");
		}
		else if(args[0].equals("universal")){
			if(Static.isOp(sender.getName())){
				givePlayerKey((EntityPlayer)sender, true, 1);
			}
			else{
				Print.chat(sender, "No Permission.");
			}
		}
		else if(args[0].equals("get")){
			if(args.length < 2){
				Print.chat(sender, "Missing argument.");
				Print.chat(sender, "try: /" + NAME + " get <one/all>");
			}
			else if(args[1].equals("one")){
				givePlayerKey((EntityPlayer)sender, false, 1);
			}
			else if(args[1].equals("all")){
				givePlayerKey((EntityPlayer)sender, false, Config.maxVehicleKeys);
			}
			else{
				Print.chat(sender, "Invalid argument.");
				Print.chat(sender, "try: /" + NAME + " get <one/all>");
			}
		}
		else if(args[0].equals("viewcode")){
			String code = "<null/error>";
			if(sender.getCommandSenderEntity().getRidingEntity() instanceof EntitySeat){
				code = ((EntitySeat)sender.getCommandSenderEntity().getRidingEntity()).driveable.driveableData.lock_code;
			}
			if(sender.getCommandSenderEntity().getRidingEntity() instanceof com.flansmod.fvm.EntitySeat){
				code = ((com.flansmod.fvm.EntitySeat)sender.getCommandSenderEntity().getRidingEntity()).vehicle.data.lock_code;
			}
			Print.chat(sender, "CODE: " + code);
		}
		else if(args[0].equals("togglelock")){
			boolean bool = false;
			if(sender.getCommandSenderEntity().getRidingEntity() instanceof EntitySeat){
				bool = ((EntitySeat)sender.getCommandSenderEntity().getRidingEntity()).driveable.driveableData.hasLock = !((EntitySeat)sender.getCommandSenderEntity().getRidingEntity()).driveable.driveableData.hasLock;
			}
			if(sender.getCommandSenderEntity().getRidingEntity() instanceof com.flansmod.fvm.EntitySeat){
				bool = ((com.flansmod.fvm.EntitySeat)sender.getCommandSenderEntity().getRidingEntity()).vehicle.data.hasLock = !((com.flansmod.fvm.EntitySeat)sender.getCommandSenderEntity().getRidingEntity()).vehicle.data.hasLock;
			}
			Print.chat(sender, "Can be locked: " + bool);
		}
		else{
			Print.chat(sender, "Wrong argument.");
			Print.chat(sender, "/" + NAME + " ?");
		}
	}

	private void givePlayerKey(EntityPlayer sender, boolean universal, int i){
		if(sender.getRidingEntity() instanceof EntitySeat){
			EntityDriveable driveable = ((EntitySeat)sender.getRidingEntity()).driveable;
			if(!driveable.driveableData.hasLock){
				Print.chat(sender, "This vehicle doesn't allow locking.");
				return;
			}
			for(int j = 0; j < i; j++){
				if((driveable.driveableData.spawnedKeys + 1) <= Config.maxVehicleKeys){
					ItemStack stack = new ItemStack(FlansMod.key, 1, 0);
					if(stack.getTagCompound() == null){
						stack.setTagCompound(new NBTTagCompound());
					}
					stack.getTagCompound().setString("KeyCreator", sender.getGameProfile().getId().toString());
					stack.getTagCompound().setString("KeyOrigin", "crafted");
					stack.getTagCompound().setString("KeyCode", driveable.driveableData.lock_code);
					stack.getTagCompound().setString("VehicleType", driveable.driveableData.type);
					stack.getTagCompound().setString("KeyType", universal ? KeyType.ADMIN.toString() : KeyType.COMMON.toString());
					sender.inventory.addItemStackToInventory(stack);
					driveable.driveableData.spawnedKeys++;
				}
				else{
					Print.chat(sender, "Reached limit of spawned keys for this vehicle.");
				}
			}
		}
		else if(sender.getRidingEntity() instanceof com.flansmod.fvm.EntitySeat){
			LandVehicle driveable = ((com.flansmod.fvm.EntitySeat)sender.getRidingEntity()).vehicle;
			if(!driveable.data.hasLock){
				Print.chat(sender, "This vehicle doesn't allow locking.");
				return;
			}
			for(int j = 0; j < i; j++){
				if((driveable.data.spawnedKeys + 1) <= Config.maxVehicleKeys){
					ItemStack stack = new ItemStack(FlansMod.key, 1, 0);
					if(stack.getTagCompound() == null){
						stack.setTagCompound(new NBTTagCompound());
					}
					stack.getTagCompound().setString("KeyCreator", sender.getGameProfile().getId().toString());
					stack.getTagCompound().setString("KeyOrigin", "crafted");
					stack.getTagCompound().setString("KeyCode", driveable.data.lock_code);
					stack.getTagCompound().setString("VehicleType", driveable.data.registryname);
					stack.getTagCompound().setString("KeyType", universal ? KeyType.ADMIN.toString() : KeyType.COMMON.toString());
					sender.inventory.addItemStackToInventory(stack);
					driveable.data.spawnedKeys++;
				}
				else{
					Print.chat(sender, "Reached limit of spawned keys for this vehicle.");
				}
			}
		}
		else{
			Print.chat(sender, "ERROR, INVALID ENTITY TYPE;");
		}
	}
	
}