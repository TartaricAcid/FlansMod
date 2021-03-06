package com.flansmod.client.model;

import org.lwjgl.opengl.GL11;

import com.flansmod.common.FlansMod;
import com.flansmod.common.data.VehicleType;
import com.flansmod.common.driveables.DriveablePart;
import com.flansmod.common.driveables.EntityVehicle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderVehicle extends Render implements IRenderFactory {
	
	public RenderVehicle(RenderManager renderManager) {
		super(renderManager);
		shadowSize = 0.5F;
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void bindTexture(Entity ent){
		super.bindEntityTexture(ent);
	}
	
	public void bindTexture(ResourceLocation rs){
		super.bindTexture(rs);
	}
	
    public void render(EntityVehicle vehicle, double d, double d1, double d2, float f, float f1){
    	VehicleType type = vehicle.getVehicleType();
    	if(type == null){
    		return;
    	}
    	bindEntityTexture(vehicle);
        GL11.glPushMatrix();{
	        GL11.glTranslatef((float)d, (float)d1, (float)d2);
	        float dYaw = (vehicle.axes.getYaw() - vehicle.prevRotationYaw);
	        for(; dYaw > 180F; dYaw -= 360F) {}
	        for(; dYaw <= -180F; dYaw += 360F) {}
	        float dPitch = (vehicle.axes.getPitch() - vehicle.prevRotationPitch);
	        for(; dPitch > 180F; dPitch -= 360F) {}
	        for(; dPitch <= -180F; dPitch += 360F) {}
	        float dRoll = (vehicle.axes.getRoll() - vehicle.prevRotationRoll);
	        for(; dRoll > 180F; dRoll -= 360F) {}
	        for(; dRoll <= -180F; dRoll += 360F) {}
	        GL11.glRotatef(180F - vehicle.prevRotationYaw - dYaw * f1, 0.0F, 1.0F, 0.0F);
	        GL11.glRotatef(vehicle.prevRotationPitch + dPitch * f1, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(vehicle.prevRotationRoll + dRoll * f1, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(180F, 0.0F, 1.0F, 0.0F);
	
			float modelScale = type.modelScale;
			GL11.glPushMatrix();{
				GL11.glScalef(modelScale, modelScale, modelScale);
				ModelVehicle modVehicle = (ModelVehicle)type.model;
				if(modVehicle != null)
					modVehicle.render(this, vehicle, f1);
				
				GL11.glPushMatrix();
				if(type.turretOrigin != null && vehicle.seats != null && vehicle.seats[0] != null){
					dYaw = (vehicle.seats[0].looking.getYaw() - vehicle.seats[0].prevLooking.getYaw());
					for(; dYaw > 180F; dYaw -= 360F) {}
					for(; dYaw <= -180F; dYaw += 360F) {}
					float yaw = vehicle.seats[0].prevLooking.getYaw() + dYaw * f1;

					GL11.glTranslatef(type.turretOrigin.x, type.turretOrigin.y, type.turretOrigin.z);
					GL11.glRotatef(-yaw, 0.0F, 1.0F, 0.0F);
					GL11.glTranslatef(-type.turretOrigin.x, -type.turretOrigin.y, -type.turretOrigin.z);
					
					if(modVehicle != null){
						modVehicle.renderTurret(0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F, vehicle, f1);
					}
					
					if(FlansMod.DEBUG){					
						GL11.glTranslatef(type.turretOrigin.x, type.turretOrigin.y, type.turretOrigin.z);
						GL11.glRotatef(-vehicle.seats[0].looking.getPitch(), 0.0F, 0.0F, 1.0F);
						GL11.glTranslatef(-type.turretOrigin.x, -type.turretOrigin.y, -type.turretOrigin.z);
					}
				}
				GL11.glPopMatrix();
				if(modVehicle != null){
					GL11.glPushMatrix();
					
					GL11.glTranslatef(modVehicle.drillHeadOrigin.x, modVehicle.drillHeadOrigin.y, modVehicle.drillHeadOrigin.z);
					GL11.glRotatef(vehicle.harvesterAngle * 50F, 1.0F, 0.0F, 0.0F);
					GL11.glTranslatef(-modVehicle.drillHeadOrigin.x, -modVehicle.drillHeadOrigin.y, -modVehicle.drillHeadOrigin.z);
					modVehicle.renderDrillBit(vehicle, f1);
					
					GL11.glPopMatrix();
				}
			}
			GL11.glPopMatrix();
			
			if(FlansMod.DEBUG){
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				GL11.glColor4f(1F, 0F, 0F, 0.3F);
				GL11.glScalef(1F, 1F, 1F);
				for(DriveablePart part : vehicle.getDriveableData().parts.values()){
					if(part.box == null){
						continue;
					}
					
					renderOffsetAABB(new AxisAlignedBB(part.box.x, part.box.y, part.box.z, (part.box.x + part.box.w), (part.box.y + part.box.h), (part.box.z + part.box.d)), 0, 0, 0);
				}
				
				
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glColor4f(1F, 1F, 1F, 1F);
			}
		}
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity){
		EntityVehicle vehicle = (EntityVehicle)entity;
		/*if(!vehicle.driveableData.allowURL){
			/*DriveableType type = ((EntityDriveable)entity).getDriveableType();
			Paintjob paintjob = type.getPaintjob(((EntityDriveable)entity).getDriveableData().paintjobID);
			return FlansModResourceHandler.getPaintjobTexture(paintjob);*/
			/*DriveableType type = ((EntityDriveable)entity).getDriveableType();
			return type.textures.get(type.paintjob);
    	}
    	else{
    		if(vehicle.driveableData.texture_url == null || vehicle.driveableData.texture_url.length() < 4){
    			DriveableType type = ((EntityDriveable)entity).getDriveableType();
    			return type.textures.get(type.paintjob);
    		}
    		else{
    			return RemoteTextureRenderHelper.get(vehicle.driveableData.texture_url);
    		}
    	}*/
		return vehicle.getDriveableType().getTexture(vehicle.getDriveableData());
	}
	
	@SubscribeEvent
	public void renderWorld(RenderWorldLastEvent event)
	{
		//Get the world
		World world = Minecraft.getMinecraft().world;
		if(world == null)
			return;

		//Get the camera frustrum for clipping
        Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
        double x = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * event.getPartialTicks();
        double y = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * event.getPartialTicks();
        double z = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * event.getPartialTicks();
        
        //Frustum frustrum = new Frustum();
        //frustrum.setPosition(x, y, z);
        
        //Push
        GL11.glPushMatrix();
        //Setup lighting
        Minecraft.getMinecraft().entityRenderer.enableLightmap();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        
        RenderHelper.enableStandardItemLighting();
        
        GL11.glTranslatef(-(float)x, -(float)y, -(float)z);
		for(Object entity : world.loadedEntityList)
		{
			if(entity instanceof EntityVehicle)
			{
				EntityVehicle vehicle = (EntityVehicle)entity;
		        int i = vehicle.getBrightnessForRender();

		        if (vehicle.isBurning())
		        {
		            i = 15728880;
		        }

		        int j = i % 65536;
		        int k = i / 65536;
		        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
		        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		        render(vehicle, vehicle.prevPosX + (vehicle.posX - vehicle.prevPosX) * event.getPartialTicks(), vehicle.prevPosY + (vehicle.posY - vehicle.prevPosY) * event.getPartialTicks(), vehicle.prevPosZ + (vehicle.posZ - vehicle.prevPosZ) * event.getPartialTicks(), 0F, event.getPartialTicks());
			}
		}
		
		//Reset Lighting
		Minecraft.getMinecraft().entityRenderer.disableLightmap();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_LIGHTING);
		//Pop
		GL11.glPopMatrix();
	}

	@Override
	public Render createRenderFor(RenderManager manager){
		return new RenderVehicle(manager);
	}
}

