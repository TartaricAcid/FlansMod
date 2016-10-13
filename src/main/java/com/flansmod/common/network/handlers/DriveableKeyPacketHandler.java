package com.flansmod.common.network.handlers;

import com.flansmod.api.IControllable;
import com.flansmod.common.network.packets.PacketDriveableKey;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DriveableKeyPacketHandler implements IMessageHandler<PacketDriveableKey, IMessage>{
	
	@Override
	public IMessage onMessage(final PacketDriveableKey packet, final MessageContext ctx) {
		IThreadListener ls = FMLCommonHandler.instance().getMinecraftServerInstance();
		ls.addScheduledTask(new Runnable(){
			@Override
			public void run(){
				EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(ctx.getServerHandler().playerEntity.getName());
				if(player.getRidingEntity() != null && player.getRidingEntity() instanceof IControllable){
					((IControllable)player.getRidingEntity()).pressKey(packet.key, player);
				}
			}
			
		});
		return null;
	}
	
}