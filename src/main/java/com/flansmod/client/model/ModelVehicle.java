package com.flansmod.client.model;

import org.lwjgl.opengl.GL11;

import com.flansmod.client.tmt.ModelRendererTurbo;
import com.flansmod.common.data.DriveableType;
import com.flansmod.common.data.UpgradeType;
import com.flansmod.common.data.VehicleType;
import com.flansmod.common.driveables.EntityDriveable;
import com.flansmod.common.driveables.EntitySeat;
import com.flansmod.common.driveables.EntityVehicle;
import com.flansmod.common.driveables.EnumDriveablePart;
import com.flansmod.common.util.CrateBlock;
import com.flansmod.common.vector.Vector3f;

import net.fexcraft.mod.lib.api.item.fItem;
import net.fexcraft.mod.lib.util.math.Pos;
import net.fexcraft.mod.lib.util.render.RGB;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;

//Extensible ModelVehicle class for rendering vehicle models
public class ModelVehicle extends ModelDriveable
{
	public ModelRendererTurbo turretModel[] = new ModelRendererTurbo[0];			//The turret (for tanks)
	public ModelRendererTurbo barrelModel[] = new ModelRendererTurbo[0];			//The barrel of the main turret
	public ModelRendererTurbo ammoModel[][] = new ModelRendererTurbo[0][0];			//Ammo models for the main turret. ammoModel[i] will render if the vehicle has less than 3 ammo slots or if slot i is full. Checks shell / missile inventory
	public ModelRendererTurbo frontWheelModel[] = new ModelRendererTurbo[0];		//Front and back wheels are for bicycles and motorbikes and whatnot
	public ModelRendererTurbo backWheelModel[] = new ModelRendererTurbo[0];
	public ModelRendererTurbo leftFrontWheelModel[] = new ModelRendererTurbo[0];	//This set of 4 wheels are for 4 or more wheeled things
	public ModelRendererTurbo rightFrontWheelModel[] = new ModelRendererTurbo[0];	//The front wheels will turn as the player steers, and the back ones will not
	public ModelRendererTurbo leftBackWheelModel[] = new ModelRendererTurbo[0];		//They will all turn as the car drives if the option to do so is set on
	public ModelRendererTurbo rightBackWheelModel[] = new ModelRendererTurbo[0];	//In the vehicle type file
	public ModelRendererTurbo rightTrackModel[] = new ModelRendererTurbo[0];
	public ModelRendererTurbo leftTrackModel[] = new ModelRendererTurbo[0];
	public ModelRendererTurbo rightTrackWheelModels[] = new ModelRendererTurbo[0];	//These go with the tracks but rotate
	public ModelRendererTurbo leftTrackWheelModels[] = new ModelRendererTurbo[0];
	
	public ModelRendererTurbo leftAnimTrackModel[][] = new ModelRendererTurbo[0][0];  //Unlimited frame track animations
	public ModelRendererTurbo rightAnimTrackModel[][] = new ModelRendererTurbo[0][0];
	
	public ModelRendererTurbo bodyDoorOpenModel[] = new ModelRendererTurbo[0];
	public ModelRendererTurbo bodyDoorCloseModel[] = new ModelRendererTurbo[0];	
	public ModelRendererTurbo trailerModel[] = new ModelRendererTurbo[0];
	public ModelRendererTurbo steeringWheelModel[] = new ModelRendererTurbo[0];
	
	public ModelRendererTurbo drillHeadModel[] = new ModelRendererTurbo[0]; 		//Drill head. Rotates around
	public Vector3f drillHeadOrigin = new Vector3f();								//this point
	
	
	/*public ModelRendererTurbo primaryPaintBodyModel[] = new ModelRendererTurbo[0];
	public ModelRendererTurbo secondaryPaintBodyModel[] = new ModelRendererTurbo[0];
	public ModelRendererTurbo primaryPaintBodyDoorOpenModel[] = new ModelRendererTurbo[0];
	public ModelRendererTurbo primaryPaintBodyDoorCloseModel[] = new ModelRendererTurbo[0];*/
	
	public int animFrame = 0;

	
	public void render(RenderVehicle render, EntityDriveable driveable, float f1)
	{
		render(0.0625F, render, (EntityVehicle)driveable, f1);
	}
	
	@Override
	/** GUI render method */
	public void render(DriveableType type)
	{
		super.render(type);
		renderPart(leftBackWheelModel);
		renderPart(rightBackWheelModel);
		renderPart(leftFrontWheelModel);
		renderPart(rightFrontWheelModel);
		renderPart(rightTrackModel);
		renderPart(leftTrackModel);
		renderPart(rightTrackWheelModels);
		renderPart(leftTrackWheelModels);
		renderPart(bodyDoorCloseModel);
		renderPart(trailerModel);
		renderPart(turretModel);
		renderPart(barrelModel);
		renderPart(drillHeadModel);
		for(ModelRendererTurbo[] mods : ammoModel)
			renderPart(mods);
		//This might cause all animation frames to render simultaneously in the crafting box... but it doesn't crash. Which is a plus I guess?
		for(ModelRendererTurbo[] latm : leftAnimTrackModel)
			renderPart(latm);
		for(ModelRendererTurbo[] ratm : rightAnimTrackModel)
			renderPart(ratm);
		renderPart(steeringWheelModel);
	}
	
	public void render(float f5, RenderVehicle render, EntityVehicle vehicle, float f){
		
		boolean rotateWheels = vehicle.getVehicleType().rotateWheels;
		animFrame = vehicle.animFrame;

		//Rendering the body
		if(vehicle.isPartIntact(EnumDriveablePart.core)){
			
			for (ModelRendererTurbo aBodyModel : bodyModel){
				aBodyModel.render(f5, oldRotateOrder);
			};
			for (ModelRendererTurbo aBodyDoorOpenModel : bodyDoorOpenModel) {
				if (vehicle.varDoor)
					aBodyDoorOpenModel.render(f5, oldRotateOrder);
			}
			for (ModelRendererTurbo aBodyDoorCloseModel : bodyDoorCloseModel) {
				if (!vehicle.varDoor)
					aBodyDoorCloseModel.render(f5, oldRotateOrder);
			}
			
			//PAINT START
			if(vehicle.driveableData.hasColor){
				vehicle.driveableData.primary_color.glColorApply();
				for(ModelRendererTurbo model : primaryPaintBodyModel) {
					model.render(f5, oldRotateOrder);
				};
				for(ModelRendererTurbo model : primaryPaintBodyDoorOpenModel) {
					if(vehicle.varDoor){
						model.render(f5, oldRotateOrder);
					}
				};
				for(ModelRendererTurbo model : primaryPaintBodyDoorCloseModel) {
					if(!vehicle.varDoor){
						model.render(f5, oldRotateOrder);
					}
				};
				RGB.glColorReset();
				vehicle.driveableData.secondary_color.glColorApply();
				for (ModelRendererTurbo model : secondaryPaintBodyModel) {
					model.render(f5, oldRotateOrder);
				};
				RGB.glColorReset();
			}
			//PAINT END
			
			for (ModelRendererTurbo aSteeringWheelModel : steeringWheelModel) {
				aSteeringWheelModel.rotateAngleX = vehicle.wheelsYaw * 3.14159265F / 180F * 3F;
				aSteeringWheelModel.render(f5, oldRotateOrder);
			}
		}
		
		//Wheels
		if(vehicle.isPartIntact(EnumDriveablePart.backLeftWheel)){
			for (ModelRendererTurbo aLeftBackWheelModel : leftBackWheelModel) {
				aLeftBackWheelModel.rotateAngleZ = rotateWheels ? -vehicle.wheelsAngle : 0;
				aLeftBackWheelModel.render(f5, oldRotateOrder);
			}
		}
		if(vehicle.isPartIntact(EnumDriveablePart.backRightWheel)){
			for (ModelRendererTurbo aRightBackWheelModel : rightBackWheelModel) {
				aRightBackWheelModel.rotateAngleZ = rotateWheels ? -vehicle.wheelsAngle : 0;
				aRightBackWheelModel.render(f5, oldRotateOrder);
			}
		}
		if(vehicle.isPartIntact(EnumDriveablePart.frontLeftWheel)){
			for (ModelRendererTurbo aLeftFrontWheelModel : leftFrontWheelModel) {
				aLeftFrontWheelModel.rotateAngleZ = rotateWheels ? -vehicle.wheelsAngle : 0;
				aLeftFrontWheelModel.rotateAngleY = -vehicle.wheelsYaw * 3.14159265F / 180F * 3F;
				aLeftFrontWheelModel.render(f5, oldRotateOrder);
			}
		}
		if(vehicle.isPartIntact(EnumDriveablePart.frontRightWheel)){
			for (ModelRendererTurbo aRightFrontWheelModel : rightFrontWheelModel) {
				aRightFrontWheelModel.rotateAngleZ = rotateWheels ? -vehicle.wheelsAngle : 0;
				aRightFrontWheelModel.rotateAngleY = -vehicle.wheelsYaw * 3.14159265F / 180F * 3F;
				aRightFrontWheelModel.render(f5, oldRotateOrder);
			}
		}
		if(vehicle.isPartIntact(EnumDriveablePart.frontWheel)){
			for (ModelRendererTurbo aFrontWheelModel : frontWheelModel) {
				aFrontWheelModel.rotateAngleZ = rotateWheels ? -vehicle.wheelsAngle : 0;
				aFrontWheelModel.rotateAngleY = -vehicle.wheelsYaw * 3.14159265F / 180F * 3F;
				aFrontWheelModel.render(f5, oldRotateOrder);
			}
		}
		if(vehicle.isPartIntact(EnumDriveablePart.backWheel)){
			for (ModelRendererTurbo aBackWheelModel : backWheelModel) {
				aBackWheelModel.rotateAngleZ = rotateWheels ? -vehicle.wheelsAngle : 0;
				aBackWheelModel.render(f5, oldRotateOrder);
			}
		}

		if(vehicle.isPartIntact(EnumDriveablePart.leftTrack)){
			for (ModelRendererTurbo aLeftTrackModel : leftTrackModel) {
				aLeftTrackModel.render(f5, oldRotateOrder);
			}
			for (ModelRendererTurbo leftTrackWheelModel : leftTrackWheelModels) {
				leftTrackWheelModel.rotateAngleZ = rotateWheels ? -vehicle.wheelsAngle : 0;
				leftTrackWheelModel.render(f5, oldRotateOrder);
			}
			
			for(int i = 0; i < leftAnimTrackModel.length; i++)
			{
				if(i == animFrame)
				{
					for (ModelRendererTurbo aLeftAnimTrackModel : leftAnimTrackModel[i]) {
						aLeftAnimTrackModel.render(f5, oldRotateOrder);
					}
				}
			}

		}

		if(vehicle.isPartIntact(EnumDriveablePart.rightTrack)){
			for (ModelRendererTurbo aRightTrackModel : rightTrackModel) {
				aRightTrackModel.render(f5, oldRotateOrder);
			}
			for (ModelRendererTurbo rightTrackWheelModel : rightTrackWheelModels) {
				rightTrackWheelModel.rotateAngleZ = rotateWheels ? -vehicle.wheelsAngle : 0;
				rightTrackWheelModel.render(f5, oldRotateOrder);
			}
			
			for(int i = 0; i < rightAnimTrackModel.length; i++)
			{
				if(i == animFrame)
				{
					for (ModelRendererTurbo aRightAnimTrackModel : rightAnimTrackModel[i]) {
						aRightAnimTrackModel.render(f5, oldRotateOrder);
					}
				}
			}
		}
		if(vehicle.isPartIntact(EnumDriveablePart.trailer)){
			for (ModelRendererTurbo aTrailerModel : trailerModel) {
				aTrailerModel.render(f5, oldRotateOrder);
			}
		}

		//Render guns
		for(EntitySeat seat : vehicle.seats)
		{
			//If the seat has a gun model attached
			if(seat != null && seat.seatInfo != null && seat.seatInfo.gunName != null && gunModels.get(seat.seatInfo.gunName) != null && vehicle.isPartIntact(seat.seatInfo.part) && !vehicle.rotateWithTurret(seat.seatInfo))
			{
				float yaw = seat.prevLooking.getYaw() + (seat.looking.getYaw() - seat.prevLooking.getYaw()) * f;
				float pitch = seat.prevLooking.getPitch() + (seat.looking.getPitch() - seat.prevLooking.getPitch()) * f;

				//Iterate over the parts of that model
				ModelRendererTurbo[][] gunModel = gunModels.get(seat.seatInfo.gunName);
				//Yaw only parts
				for(ModelRendererTurbo gunModelPart : gunModel[0])
				{
					//Yaw and render
					gunModelPart.rotateAngleY = -yaw * 3.14159265F / 180F;
					gunModelPart.render(f5);
				}
				//Yaw and pitch, no recoil parts
				for(ModelRendererTurbo gunModelPart : gunModel[1])
				{
					//Yaw, pitch and render
					gunModelPart.rotateAngleY = -yaw * 3.14159265F / 180F;
					gunModelPart.rotateAngleZ = -pitch * 3.14159265F / 180F;
					gunModelPart.render(f5);
				}
				//Yaw, pitch and recoil parts
				for(ModelRendererTurbo gunModelPart : gunModel[2])
				{
					//Yaw, pitch, recoil and render
					gunModelPart.rotateAngleY = -yaw * 3.14159265F / 180F;
					gunModelPart.rotateAngleZ = -pitch * 3.14159265F / 180F;
					gunModelPart.render(f5);
				}
			}
		}
		
		for(UpgradeType type : vehicle.driveableData.upgrades){
			GlStateManager.pushMatrix();
			Pos pos = type.offset.get(vehicle.driveableData.type);
			GL11.glTranslatef(pos.to16FloatX(), pos.to16FloatY(), pos.to16FloatZ());
			render.bindTexture(type.getTexture(vehicle.driveableData));
			((ModelUpgradePart)type.getModel()).render(f5, render, vehicle, f);
			GlStateManager.popMatrix();
		}
		
		if(vehicle.driveableData.cargo.size() > 0){
			DriveableType type = vehicle.getVehicleType();
			for(int i = 0; i < vehicle.driveableData.cargo.size(); i++){
				if(i >= type.cargopos.size()){
					break;
				}
				IBlockState state = getBlockToRender(i, vehicle);
				if(state.getRenderType() != EnumBlockRenderType.INVISIBLE){
		            GlStateManager.pushMatrix();
		            render.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		            //GlStateManager.scale(0.75F, 0.75F, 0.75F);
		            GlStateManager.translate(pos(type.cargopos.get(i).xCoord) + 0.5, pos(type.cargopos.get(i).yCoord), pos(type.cargopos.get(i).zCoord) + 0.5);
		            GlStateManager.pushMatrix();
		            //TODO GL11.glScalef(0.5f, 0.5f, 0.5f);
		            Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(state, vehicle.getBrightness());
		            //TODO GL11.glScalef(1f, 1f, 1f);
		            GlStateManager.popMatrix();
		            GlStateManager.popMatrix();
		            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		            render.bindTexture(vehicle);
		        }
			}
		}
	}
	
	private static float pos(double d){
		return (float)(d / 16f);
	}
	
	private static IBlockState getBlockToRender(int index, EntityVehicle vehicle){
		if(vehicle.driveableData.cargo.get(index).isEmpty()){
			return Blocks.AIR.getDefaultState();
		}
		else if(vehicle.driveableData.cargo.get(index).getItem() instanceof ItemBlock == false){
			return CrateBlock.INSTANCE.getDefaultState();
		}
		else if(vehicle.driveableData.cargo.get(index).getItem() instanceof fItem){
			return Blocks.BEDROCK.getDefaultState();
		}
		else{
			ItemStack stack = vehicle.driveableData.cargo.get(index);
			return ((ItemBlock)stack.getItem()).getBlock().getStateFromMeta(stack.getMetadata());
		}
	}
		
	/** Render the tank turret
	 * @param dt */
	public void renderTurret(float f, float f1, float f2, float f3, float f4, float f5, EntityVehicle vehicle, float dt)
	{
		VehicleType type = vehicle.getVehicleType();
		
		//Render main turret barrel
		{
			//float yaw = vehicle.seats[0].looking.getYaw();
			float pitch = vehicle.seats[0].looking.getPitch();

			for (ModelRendererTurbo aTurretModel : turretModel) {
				aTurretModel.render(f5, oldRotateOrder);
			}
			for (ModelRendererTurbo aBarrelModel : barrelModel) {
				aBarrelModel.rotateAngleZ = -pitch * 3.14159265F / 180F;
				aBarrelModel.render(f5, oldRotateOrder);
			}
			for(int i = 0; i < ammoModel.length; i++)
			{
				if(i >= type.numMissileSlots || !vehicle.getDriveableData().missiles.get(i).isEmpty())
				{
					for(int j = 0; j < ammoModel[i].length; j++)
					{
						ammoModel[i][j].rotateAngleZ = -pitch * 3.14159265F / 180F;
						ammoModel[i][j].render(f5, oldRotateOrder);
					}
				}
			}
		}
		
		//Render turret guns
		for(EntitySeat seat : vehicle.seats)
		{
			//If the seat has a gun model attached
			if(seat != null && seat.seatInfo != null && seat.seatInfo.gunName != null && gunModels.get(seat.seatInfo.gunName) != null && vehicle.isPartIntact(seat.seatInfo.part) && vehicle.rotateWithTurret(seat.seatInfo))
			{
				EntitySeat driverSeat = vehicle.seats[0];

				float driverYaw = driverSeat.prevLooking.getYaw() + (driverSeat.looking.getYaw() - driverSeat.prevLooking.getYaw()) * dt;
				float yaw = seat.prevLooking.getYaw() + (seat.looking.getYaw() - seat.prevLooking.getYaw()) * dt;
				float pitch = seat.prevLooking.getPitch() + (seat.looking.getPitch() - seat.prevLooking.getPitch()) * dt;

				float effectiveYaw = yaw - driverYaw;

				//Iterate over the parts of that model
				ModelRendererTurbo[][] gunModel = gunModels.get(seat.seatInfo.gunName);
				//Yaw only parts
				for(ModelRendererTurbo gunModelPart : gunModel[0])
				{
					//Yaw and render
					gunModelPart.rotateAngleY = -effectiveYaw * 3.14159265F / 180F;
					gunModelPart.render(f5, oldRotateOrder);
				}
				//Yaw and pitch, no recoil parts
				for(ModelRendererTurbo gunModelPart : gunModel[1])
				{
					//Yaw, pitch and render
					gunModelPart.rotateAngleY = -effectiveYaw * 3.14159265F / 180F;
					gunModelPart.rotateAngleZ = -pitch * 3.14159265F / 180F;
					gunModelPart.render(f5, oldRotateOrder);
				}
				//Yaw, pitch and recoil parts
				for(ModelRendererTurbo gunModelPart : gunModel[2])
				{
					//Yaw, pitch, recoil and render
					gunModelPart.rotateAngleY = -effectiveYaw * 3.14159265F / 180F;
					gunModelPart.rotateAngleZ = -pitch * 3.14159265F / 180F;
					gunModelPart.render(f5, oldRotateOrder);
				}
			}
		}
	}
	
	public void renderDrillBit(EntityVehicle vehicle, float f) 
	{
		if(vehicle.isPartIntact(EnumDriveablePart.harvester))
		{
			for (ModelRendererTurbo adrillHeadModel : drillHeadModel) {
				adrillHeadModel.render(0.0625F, oldRotateOrder);
			}
		}
	}
	
	@Override
	public void flipAll()
	{
		super.flipAll();
		flip(bodyDoorOpenModel);
		flip(bodyDoorCloseModel);
		flip(turretModel);
		flip(barrelModel);
		flip(leftFrontWheelModel);
		flip(rightFrontWheelModel);
		flip(leftBackWheelModel);
		flip(rightBackWheelModel);
		flip(rightTrackModel);
		flip(leftTrackModel);
		flip(rightTrackWheelModels);
		flip(leftTrackWheelModels);
		flip(trailerModel);
		flip(steeringWheelModel);
		flip(frontWheelModel);
		flip(backWheelModel);
		flip(drillHeadModel);
		for(ModelRendererTurbo[] latm : leftAnimTrackModel)
			flip(latm);
		for(ModelRendererTurbo[] ratm : rightAnimTrackModel)
			flip(ratm);
	}	

	
	@Override
	public void translateAll(float x, float y, float z)
	{
		super.translateAll(x, y, z);
		translate(bodyDoorOpenModel, x, y, z);
		translate(bodyDoorCloseModel, x, y, z);		
		translate(turretModel, x, y, z);
		translate(barrelModel, x, y, z);
		translate(leftFrontWheelModel, x, y, z);
		translate(rightFrontWheelModel, x, y, z);
		translate(leftBackWheelModel, x, y, z);
		translate(rightBackWheelModel, x, y, z);
		translate(rightTrackModel, x, y, z);
		translate(leftTrackModel, x, y, z);
		translate(rightTrackWheelModels, x, y, z);
		translate(leftTrackWheelModels, x, y, z);
		translate(trailerModel, x, y, z);
		translate(steeringWheelModel, x, y, z);
		translate(frontWheelModel, x, y, z);
		translate(backWheelModel, x, y, z);
		translate(drillHeadModel, x, y, z);
		for(ModelRendererTurbo[] latm : leftAnimTrackModel)
			translate(latm, x, y, z);
		for(ModelRendererTurbo[] ratm : rightAnimTrackModel)
			translate(ratm, x, y, z);
	}
}
