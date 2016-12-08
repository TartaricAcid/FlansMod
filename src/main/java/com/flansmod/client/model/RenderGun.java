package com.flansmod.client.model;

import static net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND;
import static net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
import static net.minecraft.util.EnumHandSide.LEFT;

import org.lwjgl.opengl.GL11;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.FlansModResourceHandler;
import com.flansmod.client.RenderUtils;
import com.flansmod.common.guns.AttachmentType;
import com.flansmod.common.guns.EnumFireMode;
import com.flansmod.common.guns.GunType;
import com.flansmod.common.guns.IScope;
import com.flansmod.common.guns.ItemBullet;
import com.flansmod.common.guns.ItemGun;
import com.flansmod.common.guns.Paintjob;
import com.flansmod.common.types.PaintableType;
import com.flansmod.common.vector.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class RenderGun implements RenderCustomItem
{
	private static TextureManager renderEngine;

	public static float smoothing;
	public static boolean bindTextures = true;

	public RenderGun(RenderManager manager)
	{
		//super(manager);
	}


	@Override
	public boolean canRender(ItemCameraTransforms.TransformType perspective)
	{
		return perspective == THIRD_PERSON_LEFT_HAND || perspective == THIRD_PERSON_RIGHT_HAND;
	}

	@Override
	public void renderPerspective(ItemCameraTransforms.TransformType perspective, EntityLivingBase entity, ItemStack stack, World world)
	{

		//guns have custom rendering for first person (FOV, view-bobbing etc.)
		if (RenderUtils.isFirstPerson(perspective))
		{
		//	return;
		}
		renderItem(perspective, stack, entity);
	}


	/*
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		switch(type)
		{
		case ENTITY : if(!Minecraft.getMinecraft().gameSettings.fancyGraphics) return false;
		case EQUIPPED : case EQUIPPED_FIRST_PERSON :  /*case INVENTORY : *//*return item != null 
				&& item.getItem() instanceof ItemGun 
				&& ((ItemGun)item.getItem()).GetType().model != null;
		default : break;
		}
		return false;
	}
**/



	public void renderItem(ItemCameraTransforms.TransformType type, ItemStack item, EntityLivingBase entity)
	{
		//Avoid any broken cases by returning
		if(!(item.getItem() instanceof ItemGun))
			return;	
		
		GunType gunType = ((ItemGun)item.getItem()).getInfoType();
		if(gunType == null)
			return;
		
		ModelGun model = gunType.model;
		if(model == null)
			return;

		renderGun(type, item, gunType, entity);
	}

	/*
	//Render off-hand gun in 3rd person
	public void renderOffHandGun(EntityPlayer player, ItemStack offHandItemStack)
	{
		GunAnimations animations = FlansModClient.gunAnimationsLeft.get(player);
		if(animations == null)
		{
			animations = new GunAnimations();
			FlansModClient.gunAnimationsLeft.put(player, animations);
		}
		GunType offHandGunType = ((ItemGun)offHandItemStack.getItem()).GetType();
		if(!offHandGunType.oneHanded)
			return;
		
		renderGun(ItemRenderType.INVENTORY, offHandItemStack, offHandGunType, animations, true, player);
	}
	*/


	private void renderGun(ItemCameraTransforms.TransformType type, ItemStack item, GunType gunType, EntityLivingBase entity)
	{
		//The model scale
		float scale = 1F / 16F;
		ModelGun model = gunType.model;

		EnumHandSide side = RenderUtils.getSide(type);

		GunAnimations animations = FlansModClient.getGunAnimations(entity, side);

		int flip = side == LEFT ? -1 : 1;
		
		GL11.glPushMatrix();
		{
			//Get the reload animation rotation
			float reloadRotate = 0F;
			//Setup transforms based on gun position
			switch(type)
			{

				case GROUND:
				{
					//EntityItem entity = (EntityItem)data[1];
					//GL11.glRotatef(entity.getAge() + (entity.getAge() == 0 ? 0 : smoothing), 0F, 1F, 0F);
					GL11.glTranslatef(-0.45F + model.itemFrameOffset.x, -0.05F + model.itemFrameOffset.y, model.itemFrameOffset.z);
					break;
				}
				case FIXED:
				{
					GL11.glTranslatef(model.itemFrameOffset.x, model.itemFrameOffset.y, model.itemFrameOffset.z);
					break;
				}
				case THIRD_PERSON_RIGHT_HAND:
				case THIRD_PERSON_LEFT_HAND:
				{
					GL11.glRotatef(90F, 0F, 1F, 0F);
					GL11.glTranslatef(0F, -0.15F, 0F);
					GL11.glTranslatef(model.thirdPersonOffset.x, model.thirdPersonOffset.y, model.thirdPersonOffset.z);

					if(animations.meleeAnimationProgress > 0 && animations.meleeAnimationProgress < gunType.meleePath.size())
					{
						Vector3f meleePos = gunType.meleePath.get(animations.meleeAnimationProgress);
						Vector3f nextMeleePos = animations.meleeAnimationProgress + 1 < gunType.meleePath.size() ? gunType.meleePath.get(animations.meleeAnimationProgress + 1) : new Vector3f();
						GL11.glTranslatef(meleePos.x + (nextMeleePos.x - meleePos.x) * smoothing, meleePos.y + (nextMeleePos.y - meleePos.y) * smoothing, meleePos.z + (nextMeleePos.z - meleePos.z) * smoothing);
						Vector3f meleeAngles = gunType.meleePathAngles.get(animations.meleeAnimationProgress);
						Vector3f nextMeleeAngles = animations.meleeAnimationProgress + 1 < gunType.meleePathAngles.size() ? gunType.meleePathAngles.get(animations.meleeAnimationProgress + 1) : new Vector3f();
						GL11.glRotatef(meleeAngles.y + (nextMeleeAngles.y - meleeAngles.y) * smoothing, 0F, 1F, 0F);
						GL11.glRotatef(meleeAngles.z + (nextMeleeAngles.z - meleeAngles.z) * smoothing, 0F, 0F, 1F);
						GL11.glRotatef(meleeAngles.x + (nextMeleeAngles.x - meleeAngles.x) * smoothing, 1F, 0F, 0F);
					}

					break;
				}
				case FIRST_PERSON_LEFT_HAND:
				case FIRST_PERSON_RIGHT_HAND:
				{
					IScope scope = gunType.getCurrentScope(item);
					if(FlansModClient.zoomProgress > 0.9F && scope.hasZoomOverlay())
					{
						GL11.glPopMatrix();
						return;
					}
					float adsSwitch = FlansModClient.lastZoomProgress + (FlansModClient.zoomProgress - FlansModClient.lastZoomProgress) * smoothing;//0F;//((float)Math.sin((FlansMod.ticker) / 10F) + 1F) / 2F;
					
					if(side == LEFT)
					{
						GL11.glRotatef(45F, 0F, 1F, 0F);
						GL11.glTranslatef(-1F, 0.675F, -1.8F);
					}
					else
					{
						GL11.glRotatef(45F, 0F, 1F, 0F);
						GL11.glRotatef(0F - 5F * adsSwitch, 0F, 0F, 1F); 
						
						GL11.glTranslatef(-1F, 0.675F + 0.180F * adsSwitch, -1F - 0.395F * adsSwitch);

						if(gunType.hasScopeOverlay)
							GL11.glTranslatef(-0.7F * adsSwitch, -0.12F * adsSwitch, -0.05F * adsSwitch);
						GL11.glRotatef(4.5F * adsSwitch, 0F, 0F, 1F);
						GL11.glTranslatef(0F, -0.03F * adsSwitch, 0F);
					}
					
					if(animations.meleeAnimationProgress > 0 && animations.meleeAnimationProgress < gunType.meleePath.size()) 
					{
						Vector3f meleePos = gunType.meleePath.get(animations.meleeAnimationProgress);
						Vector3f nextMeleePos = animations.meleeAnimationProgress + 1 < gunType.meleePath.size() ? gunType.meleePath.get(animations.meleeAnimationProgress + 1) : new Vector3f();
						GL11.glTranslatef(meleePos.x + (nextMeleePos.x - meleePos.x) * smoothing, meleePos.y + (nextMeleePos.y - meleePos.y) * smoothing, meleePos.z + (nextMeleePos.z - meleePos.z) * smoothing);
						Vector3f meleeAngles = gunType.meleePathAngles.get(animations.meleeAnimationProgress);
						Vector3f nextMeleeAngles = animations.meleeAnimationProgress + 1 < gunType.meleePathAngles.size() ? gunType.meleePathAngles.get(animations.meleeAnimationProgress + 1) : new Vector3f();
						GL11.glRotatef(meleeAngles.y + (nextMeleeAngles.y - meleeAngles.y) * smoothing, 0F, 1F, 0F);
						GL11.glRotatef(meleeAngles.z + (nextMeleeAngles.z - meleeAngles.z) * smoothing, 0F, 0F, 1F);
						GL11.glRotatef(meleeAngles.x + (nextMeleeAngles.x - meleeAngles.x) * smoothing, 1F, 0F, 0F);

					}
					
					if(model.spinningCocking)
					{
						GL11.glTranslatef(model.spinPoint.x, model.spinPoint.y, model.spinPoint.z);
						float pumped = (animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing);
						GL11.glRotatef(pumped * 180F + 180F, 0F, 0F, 1F);
						GL11.glTranslatef(-model.spinPoint.x, -model.spinPoint.y, -model.spinPoint.z);
					}
					
					if(animations.reloading)
					{
						//Calculate the amount of tilt required for the reloading animation
						float effectiveReloadAnimationProgress = animations.lastReloadAnimationProgress + (animations.reloadAnimationProgress - animations.lastReloadAnimationProgress) * smoothing;
						reloadRotate = 1F;
						if(effectiveReloadAnimationProgress < model.tiltGunTime)
							reloadRotate = effectiveReloadAnimationProgress / model.tiltGunTime;
						if(effectiveReloadAnimationProgress > model.tiltGunTime + model.unloadClipTime + model.loadClipTime)
							reloadRotate = 1F - (effectiveReloadAnimationProgress - (model.tiltGunTime + model.unloadClipTime + model.loadClipTime)) / model.untiltGunTime;
						
						//Rotate the gun dependent on the animation type
						switch(model.animationType)
						{
							case BOTTOM_CLIP : case PISTOL_CLIP : case SHOTGUN : case END_LOADED : 
							{
								GL11.glRotatef(60F * reloadRotate, 0F, 0F, 1F);
								GL11.glRotatef(30F * reloadRotate * flip, 1F, 0F, 0F);
								GL11.glTranslatef(0.25F * reloadRotate, 0F, 0F);
								break;
							}
							case BACK_LOADED :
							{
								GL11.glRotatef(-75F * reloadRotate, 0F, 0F, 1F);
								GL11.glRotatef(-30F * reloadRotate * flip, 1F, 0F, 0F);
								GL11.glTranslatef(0.5F * reloadRotate, 0F, 0F);
								break;
							}
							case BULLPUP :
							{
								GL11.glRotatef(70F * reloadRotate, 0F, 0F, 1F);
								GL11.glRotatef(10F * reloadRotate * flip, 1F, 0F, 0F);
								GL11.glTranslatef(0.5F * reloadRotate, -0.2F * reloadRotate, 0F);
								break;
							}
							case RIFLE : 
							{
								GL11.glRotatef(30F * reloadRotate, 0F, 0F, 1F);
								GL11.glRotatef(-30F * reloadRotate * flip, 1F, 0F, 0F);
								GL11.glTranslatef(0.5F * reloadRotate, 0F, -0.5F * reloadRotate);
								break;
							}
							case RIFLE_TOP : case REVOLVER :
							{
								GL11.glRotatef(30F * reloadRotate, 0F, 0F, 1F);
								GL11.glRotatef(10F * reloadRotate, 0F, 1F, 0F);
								GL11.glRotatef(-10F * reloadRotate * flip, 1F, 0F, 0F);
								GL11.glTranslatef(0.1F * reloadRotate, -0.2F * reloadRotate, -0.1F * reloadRotate);
								break;
							}
							case ALT_PISTOL_CLIP :
							{
								GL11.glRotatef(60F * reloadRotate * flip, 0F, 1F, 0F);
								GL11.glTranslatef(0.15F * reloadRotate, 0.25F * reloadRotate, 0F);
								break;
							}
							case STRIKER : 
							{
								GL11.glRotatef(-35F * reloadRotate * flip, 1F, 0F, 0F);
								GL11.glTranslatef(0.2F * reloadRotate, 0F, -0.1F * reloadRotate);
								break;
							}
							case GENERIC :
							{
								//Gun reloads partly or completely off-screen.
								GL11.glRotatef(45F * reloadRotate, 0F, 0F, 1F);
								GL11.glTranslatef(-0.2F * reloadRotate, -0.5F * reloadRotate, 0F);
								break;
							}
							case CUSTOM :
							{
								GL11.glRotatef(model.rotateGunVertical * reloadRotate, 0F, 0F, 1F);
								GL11.glRotatef(model.rotateGunHorizontal * reloadRotate, 0F, 1F, 0F);
								GL11.glRotatef(model.tiltGun * reloadRotate, 1F, 0F, 0F);
								GL11.glTranslatef(model.translateGun.x * reloadRotate,  model.translateGun.y * reloadRotate, model.translateGun.z * reloadRotate);
								break;
							}
							default : break;
						}
					}
					break;
				}
				default : break;
			}

			renderGun(item, gunType, scale, model, animations, reloadRotate);
		}
		GL11.glPopMatrix();
	}


	/**
	 * Gun render method, separated from transforms so that mecha renderer may also call this
	 */
	public static void renderGun(ItemStack item, GunType type, float scale, ModelGun model, GunAnimations animations, float reloadRotate)
	{
		//Make sure we actually have the renderEngine
		if (renderEngine == null)
			renderEngine = Minecraft.getMinecraft().renderEngine;

		//If we have no animation variables, use defaults
		if (animations == null)
			animations = GunAnimations.defaults;

		//Get all the attachments that we may need to render
		AttachmentType scopeAttachment = type.getScope(item);
		AttachmentType barrelAttachment = type.getBarrel(item);
		AttachmentType stockAttachment = type.getStock(item);
		AttachmentType gripAttachment = type.getGrip(item);

		ItemStack scopeItemStack = type.getScopeItemStack(item);
		ItemStack barrelItemStack = type.getBarrelItemStack(item);
		ItemStack stockItemStack = type.getStockItemStack(item);
		ItemStack gripItemStack = type.getGripItemStack(item);

		ItemStack[] bulletStacks = new ItemStack[type.numAmmoItemsInGun];
		boolean empty = true;
		for (int i = 0; i < type.numAmmoItemsInGun; i++)
		{
			bulletStacks[i] = ((ItemGun) item.getItem()).getBulletItemStack(item, i);
			if (bulletStacks[i] != null && bulletStacks[i].getItem() instanceof ItemBullet && bulletStacks[i].getItemDamage() < bulletStacks[i].getMaxDamage())
				empty = false;
		}

		//Load texture
		//renderEngine.bindTexture(FlansModResourceHandler.getPaintjobTexture(type.getPaintjob(item.getTagCompound().getString("Paint"))));
		if (bindTextures)
		{
			if (PaintableType.HasCustomPaintjob(item))
			{
				renderEngine.bindTexture(PaintableType.GetCustomPaintjobSkinResource(item));
			}
			else
			{
				renderEngine.bindTexture(FlansModResourceHandler.getPaintjobTexture(type.getPaintjob(item.getItemDamage())));
			}
		}

		if (scopeAttachment != null)
			GL11.glTranslatef(0F, -scopeAttachment.model.renderOffset / 16F, 0F);

		//Render the gun and default attachment models
		GL11.glPushMatrix();
		{
			GL11.glScalef(type.modelScale, type.modelScale, type.modelScale);

			model.renderGun(scale);
			if (scopeAttachment == null && !model.scopeIsOnSlide && !model.scopeIsOnBreakAction)
				model.renderDefaultScope(scale);
			if (barrelAttachment == null)
				model.renderDefaultBarrel(scale);
			if (stockAttachment == null)
				model.renderDefaultStock(scale);
			if (gripAttachment == null && !model.gripIsOnPump)
				model.renderDefaultGrip(scale);

			//Render various shoot / reload animated parts
			//Render the slide
			GL11.glPushMatrix();
			{
				GL11.glTranslatef(-(animations.lastGunSlide + (animations.gunSlide - animations.lastGunSlide) * smoothing) * model.gunSlideDistance, 0F, 0F);
				model.renderSlide(scale);
				if (scopeAttachment == null && model.scopeIsOnSlide)
					model.renderDefaultScope(scale);
			}
			GL11.glPopMatrix();

			//Render the break action
			GL11.glPushMatrix();
			{
				GL11.glTranslatef(model.barrelBreakPoint.x, model.barrelBreakPoint.y, model.barrelBreakPoint.z);
				GL11.glRotatef(reloadRotate * -model.breakAngle, 0F, 0F, 1F);
				GL11.glTranslatef(-model.barrelBreakPoint.x, -model.barrelBreakPoint.y, -model.barrelBreakPoint.z);
				model.renderBreakAction(scale);
				if (scopeAttachment == null && model.scopeIsOnBreakAction)
					model.renderDefaultScope(scale);
			}
			GL11.glPopMatrix();

			//Render the pump-action handle
			GL11.glPushMatrix();
			{
				GL11.glTranslatef(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * model.pumpHandleDistance, 0F, 0F);
				model.renderPump(scale);
				if (gripAttachment == null && model.gripIsOnPump)
					model.renderDefaultGrip(scale);
			}
			GL11.glPopMatrix();

			//Render the minigun barrels
			if (type.mode == EnumFireMode.MINIGUN)
			{
				GL11.glPushMatrix();
				GL11.glTranslatef(model.minigunBarrelOrigin.x, model.minigunBarrelOrigin.y, model.minigunBarrelOrigin.z);
				GL11.glRotatef(animations.minigunBarrelRotation, 1F, 0F, 0F);
				GL11.glTranslatef(-model.minigunBarrelOrigin.x, -model.minigunBarrelOrigin.y, -model.minigunBarrelOrigin.z);
				model.renderMinigunBarrel(scale);
				GL11.glPopMatrix();
			}

			//Render the cocking handle

			//Render the revolver barrel
			GL11.glPushMatrix();
			{
				GL11.glTranslatef(model.revolverFlipPoint.x, model.revolverFlipPoint.y, model.revolverFlipPoint.z);
				GL11.glRotatef(reloadRotate * model.revolverFlipAngle, 1F, 0F, 0F);
				GL11.glTranslatef(-model.revolverFlipPoint.x, -model.revolverFlipPoint.y, -model.revolverFlipPoint.z);
				model.renderRevolverBarrel(scale);
			}
			GL11.glPopMatrix();

			//Render the clip
			GL11.glPushMatrix();
			{
				boolean shouldRender = true;
				//Check to see if the ammo should be rendered first
				switch (model.animationType)
				{
					case END_LOADED:
					case BACK_LOADED:
					{
						if (empty)
							shouldRender = false;
						break;
					}
					default:
						break;
				}
				//If it should be rendered, do the transformations required
				if (shouldRender && animations.reloading && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0)
				{
					//Calculate the amount of tilt required for the reloading animation
					float effectiveReloadAnimationProgress = animations.lastReloadAnimationProgress + (animations.reloadAnimationProgress - animations.lastReloadAnimationProgress) * smoothing;
					float clipPosition = 0F;
					if (effectiveReloadAnimationProgress > model.tiltGunTime && effectiveReloadAnimationProgress < model.tiltGunTime + model.unloadClipTime)
						clipPosition = (effectiveReloadAnimationProgress - model.tiltGunTime) / model.unloadClipTime;
					if (effectiveReloadAnimationProgress >= model.tiltGunTime + model.unloadClipTime && effectiveReloadAnimationProgress < model.tiltGunTime + model.unloadClipTime + model.loadClipTime)
						clipPosition = 1F - (effectiveReloadAnimationProgress - (model.tiltGunTime + model.unloadClipTime)) / model.loadClipTime;

					float loadOnlyClipPosition = Math.max(0F, Math.min(1F, 1F - ((effectiveReloadAnimationProgress - model.tiltGunTime) / (model.unloadClipTime + model.loadClipTime))));

					//Rotate the gun dependent on the animation type
					switch (model.animationType)
					{
						case BREAK_ACTION:
						{
							GL11.glTranslatef(model.barrelBreakPoint.x, model.barrelBreakPoint.y, model.barrelBreakPoint.z);
							GL11.glRotatef(reloadRotate * -model.breakAngle, 0F, 0F, 1F);
							GL11.glTranslatef(-model.barrelBreakPoint.x, -model.barrelBreakPoint.y, -model.barrelBreakPoint.z);
							GL11.glTranslatef(-1F * clipPosition, 0F, 0F);
							break;
						}
						case REVOLVER:
						{
							GL11.glTranslatef(model.revolverFlipPoint.x, model.revolverFlipPoint.y, model.revolverFlipPoint.z);
							GL11.glRotatef(reloadRotate * model.revolverFlipAngle, 1F, 0F, 0F);
							GL11.glTranslatef(-model.revolverFlipPoint.x, -model.revolverFlipPoint.y, -model.revolverFlipPoint.z);
							GL11.glTranslatef(-1F * clipPosition, 0F, 0F);
							break;
						}
						case BOTTOM_CLIP:
						{
							GL11.glRotatef(-180F * clipPosition, 0F, 0F, 1F);
							GL11.glRotatef(60F * clipPosition, 1F, 0F, 0F);
							GL11.glTranslatef(0.5F * clipPosition, 0F, 0F);
							break;
						}
						case PISTOL_CLIP:
						{
							GL11.glRotatef(-90F * clipPosition * clipPosition, 0F, 0F, 1F);
							GL11.glTranslatef(0F, -1F * clipPosition, 0F);
							break;
						}
						case ALT_PISTOL_CLIP:
						{
							GL11.glRotatef(5F * clipPosition, 0F, 0F, 1F);
							GL11.glTranslatef(0F, -3F * clipPosition, 0F);
							break;
						}
						case SIDE_CLIP:
						{
							GL11.glRotatef(180F * clipPosition, 0F, 1F, 0F);
							GL11.glRotatef(60F * clipPosition, 0F, 1F, 0F);
							GL11.glTranslatef(0.5F * clipPosition, 0F, 0F);
							break;
						}
						case BULLPUP:
						{
							GL11.glRotatef(-150F * clipPosition, 0F, 0F, 1F);
							GL11.glRotatef(60F * clipPosition, 1F, 0F, 0F);
							GL11.glTranslatef(1F * clipPosition, -0.5F * clipPosition, 0F);
							break;
						}
						case P90:
						{
							GL11.glRotatef(-15F * reloadRotate * reloadRotate, 0F, 0F, 1F);
							GL11.glTranslatef(0F, 0.075F * reloadRotate, 0F);
							GL11.glTranslatef(-2F * clipPosition, -0.3F * clipPosition, 0.5F * clipPosition);
							break;
						}
						case RIFLE:
						{
							float thing = clipPosition * model.numBulletsInReloadAnimation;
							int bulletNum = MathHelper.floor(thing);
							float bulletProgress = thing - bulletNum;

							GL11.glRotatef(bulletProgress * 15F, 0F, 1F, 0F);
							GL11.glRotatef(bulletProgress * 15F, 0F, 0F, 1F);
							GL11.glTranslatef(bulletProgress * -1F, 0F, bulletProgress * 0.5F);

							break;
						}
						case RIFLE_TOP:
						{
							float thing = clipPosition * model.numBulletsInReloadAnimation;
							int bulletNum = MathHelper.floor(thing);
							float bulletProgress = thing - bulletNum;

							GL11.glRotatef(bulletProgress * 55F, 0F, 1F, 0F);
							GL11.glRotatef(bulletProgress * 95F, 0F, 0F, 1F);
							GL11.glTranslatef(bulletProgress * -0.1F, bulletProgress * 1F, bulletProgress * 0.5F);

							break;
						}
						case SHOTGUN:
						case STRIKER:
						{
							float thing = clipPosition * model.numBulletsInReloadAnimation;
							int bulletNum = MathHelper.floor(thing);
							float bulletProgress = thing - bulletNum;

							GL11.glRotatef(bulletProgress * -30F, 0F, 0F, 1F);
							GL11.glTranslatef(bulletProgress * -0.5F, bulletProgress * -1F, 0F);

							break;
						}
						case CUSTOM:
						{
							GL11.glRotatef(model.rotateClipVertical * clipPosition, 0F, 0F, 1F);
							GL11.glRotatef(model.rotateClipHorizontal * clipPosition, 0F, 1F, 0F);
							GL11.glRotatef(model.tiltClip * clipPosition, 1F, 0F, 0F);
							GL11.glTranslatef(model.translateClip.x * clipPosition, model.translateClip.y * clipPosition, model.translateClip.z * clipPosition);
							break;
						}
						case END_LOADED:
						{
							//float bulletProgress = 1F;
							//if(effectiveReloadAnimationProgress > model.tiltGunTime)
							//	bulletProgress = 1F - Math.min((effectiveReloadAnimationProgress - model.tiltGunTime) / (model.unloadClipTime + model.loadClipTime), 1);


							float dYaw = (loadOnlyClipPosition > 0.5F ? loadOnlyClipPosition * 2F - 1F : 0F);


							GL11.glRotatef(-45F * dYaw, 0F, 0F, 1F);
							GL11.glTranslatef(-model.endLoadedAmmoDistance * dYaw, -0.5F * dYaw, 0F);

							float xDisplacement = (loadOnlyClipPosition < 0.5F ? loadOnlyClipPosition * 2F : 1F);

							GL11.glTranslatef(model.endLoadedAmmoDistance * xDisplacement, 0F, 0F);
							
							/*
							GL11.glTranslatef(1F * bulletProgress, -3F * bulletProgress, 0F);
							if(bulletProgress > 0.5F)
								GL11.glRotatef(-90F * (bulletProgress * 2F), 0F, 0F, 1F);	
							
							if(bulletProgress < 0.5F)
							{
								GL11.glTranslatef(-3F * (bulletProgress - 0.5F), 0F, 0F);
								
							}
							*/


							break;
						}
						case BACK_LOADED:
						{
							float dYaw = (loadOnlyClipPosition > 0.5F ? loadOnlyClipPosition * 2F - 1F : 0F);


							//GL11.glRotatef(-45F * dYaw, 0F, 0F, 1F);
							GL11.glTranslatef(model.endLoadedAmmoDistance * dYaw, -0.5F * dYaw, 0F);

							float xDisplacement = (loadOnlyClipPosition < 0.5F ? loadOnlyClipPosition * 2F : 1F);

							GL11.glTranslatef(-model.endLoadedAmmoDistance * xDisplacement, 0F, 0F);
						}

						default:
							break;
					}
				}

				if (shouldRender)
					model.renderAmmo(scale);
			}
			GL11.glPopMatrix();
		}
		GL11.glPopMatrix();

		//Render static attachments
		//Scope
		if (scopeAttachment != null)
		{
			GL11.glPushMatrix();
			{
				Paintjob paintjob = scopeAttachment.getPaintjob(scopeItemStack.getItemDamage());
				renderEngine.bindTexture(FlansModResourceHandler.getPaintjobTexture(paintjob));
				if (model.scopeIsOnBreakAction)
				{
					GL11.glTranslatef(model.barrelBreakPoint.x, model.barrelBreakPoint.y, model.barrelBreakPoint.z);
					GL11.glRotatef(reloadRotate * -model.breakAngle, 0F, 0F, 1F);
					GL11.glTranslatef(-model.barrelBreakPoint.x, -model.barrelBreakPoint.y, -model.barrelBreakPoint.z);
				}
				GL11.glTranslatef(model.scopeAttachPoint.x * type.modelScale, model.scopeAttachPoint.y * type.modelScale, model.scopeAttachPoint.z * type.modelScale);

				if (model.scopeIsOnSlide)
					GL11.glTranslatef(-(animations.lastGunSlide + (animations.gunSlide - animations.lastGunSlide) * smoothing) * model.gunSlideDistance, 0F, 0F);
				GL11.glScalef(scopeAttachment.modelScale, scopeAttachment.modelScale, scopeAttachment.modelScale);
				ModelAttachment scopeModel = scopeAttachment.model;
				if (scopeModel != null)
					scopeModel.renderAttachment(scale);
				renderEngine.bindTexture(FlansModResourceHandler.getTexture(type));
			}
			GL11.glPopMatrix();
		}

		//Grip
		if (gripAttachment != null)
		{
			GL11.glPushMatrix();
			{
				Paintjob paintjob = gripAttachment.getPaintjob(gripItemStack.getItemDamage());
				renderEngine.bindTexture(FlansModResourceHandler.getPaintjobTexture(paintjob));
				GL11.glTranslatef(model.gripAttachPoint.x * type.modelScale, model.gripAttachPoint.y * type.modelScale, model.gripAttachPoint.z * type.modelScale);
				if (model.gripIsOnPump)
					GL11.glTranslatef(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * model.pumpHandleDistance, 0F, 0F);
				GL11.glScalef(gripAttachment.modelScale, gripAttachment.modelScale, gripAttachment.modelScale);
				ModelAttachment gripModel = gripAttachment.model;
				if (gripModel != null)
					gripModel.renderAttachment(scale);
				renderEngine.bindTexture(FlansModResourceHandler.getTexture(type));
			}
			GL11.glPopMatrix();
		}

		//Barrel
		if (barrelAttachment != null)
		{
			GL11.glPushMatrix();
			{
				Paintjob paintjob = barrelAttachment.getPaintjob(barrelItemStack.getItemDamage());
				renderEngine.bindTexture(FlansModResourceHandler.getPaintjobTexture(paintjob));
				GL11.glTranslatef(model.barrelAttachPoint.x * type.modelScale, model.barrelAttachPoint.y * type.modelScale, model.barrelAttachPoint.z * type.modelScale);
				GL11.glScalef(barrelAttachment.modelScale, barrelAttachment.modelScale, barrelAttachment.modelScale);
				ModelAttachment barrelModel = barrelAttachment.model;
				if (barrelModel != null)
					barrelModel.renderAttachment(scale);
				renderEngine.bindTexture(FlansModResourceHandler.getTexture(type));
			}
			GL11.glPopMatrix();
		}

		//Stock
		if (stockAttachment != null)
		{
			GL11.glPushMatrix();
			{
				Paintjob paintjob = stockAttachment.getPaintjob(stockItemStack.getItemDamage());
				renderEngine.bindTexture(FlansModResourceHandler.getPaintjobTexture(paintjob));
				GL11.glTranslatef(model.stockAttachPoint.x * type.modelScale, model.stockAttachPoint.y * type.modelScale, model.stockAttachPoint.z * type.modelScale);
				GL11.glScalef(stockAttachment.modelScale, stockAttachment.modelScale, stockAttachment.modelScale);
				ModelAttachment stockModel = stockAttachment.model;
				if (stockModel != null)
					stockModel.renderAttachment(scale);
				renderEngine.bindTexture(FlansModResourceHandler.getTexture(type));
			}
			GL11.glPopMatrix();
		}
	}
}