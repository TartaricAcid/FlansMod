package com.flansmod.client.debug;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class UtilGui {
	
	private static final Minecraft mc = Minecraft.getMinecraft();
	//private static String n0, n1, n2, n3, n4, n5, n6, n7;
	private static String[] n = new String[16];
	
	@SubscribeEvent
    public void onRenderGui(RenderGameOverlayEvent event){
		if(event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR){
			for(int i = 0; i < n.length; i++){
				if(n[i] == null){
					continue;
				}
				mc.fontRenderer.drawStringWithShadow(n[i], 4, 4 + (8 * i), 0x90C3D4);
			}
			return;
			/*mc.fontRendererObj.drawStringWithShadow(n0, 4, 4, 0x90C3D4);
			mc.fontRendererObj.drawStringWithShadow(n1, 4, 12, 0x90C3D4);
			mc.fontRendererObj.drawStringWithShadow(n2, 4, 20, 0x90C3D4);
			mc.fontRendererObj.drawStringWithShadow(n3, 4, 28, 0x90C3D4);
			mc.fontRendererObj.drawStringWithShadow(n4, 4, 36, 0x90C3D4);
			mc.fontRendererObj.drawStringWithShadow(n5, 4, 44, 0x90C3D4);
			mc.fontRendererObj.drawStringWithShadow(n6, 4, 52, 0x90C3D4);
			mc.fontRendererObj.drawStringWithShadow(n7, 4, 60, 0x90C3D4);*/
		}
    }
	
	public static void byId(int i, String s){
		if(i < n.length){
			n[i] = s;
		}
	}
	
}