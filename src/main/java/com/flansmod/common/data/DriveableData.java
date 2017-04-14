package com.flansmod.common.data;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import com.flansmod.common.driveables.DriveablePart;
import com.flansmod.common.driveables.EnumDriveablePart;
import com.flansmod.common.items.ItemKey;
import com.flansmod.common.items.ItemPart;
import com.flansmod.common.util.fni.RGB;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class DriveableData implements IInventory {
	
	/** The name of this driveable's type */
	public String type;
	/** The sizes of each inventory (guns, bombs / mines, missiles / shells, cargo) */
	public int numGuns, numBombs, numMissiles, numCargo;
	/** The inventory stacks */
	public ItemStack[] ammo, bombs, missiles, cargo;
	/** The engine in this driveable */
	public PartType engine;
	/** The stack in the fuel slot */
	public ItemStack fuel;
	/** The amount of fuel in the tank */
	public float fuelInTank;
	/** Each driveable part has a small class that holds its current status */
	public HashMap<EnumDriveablePart, DriveablePart> parts;
	public Set<UpgradeType> upgrades;
	/** Paintjob index */
	public int paintjobID = 0;
	
	//MINUS START
	public RGB primary_color = RGB.BLUE;
	public RGB secondary_color = RGB.GREEN;
	public boolean hasColor = false;
	public boolean allowURL = false;
	public String texture_url;
	public String lock_code;
	public boolean isLocked;
	public boolean hasLock;
	public int spawnedKeys;
	//MINUS END
	
	public DriveableData(NBTTagCompound tags, int paintjobID){
		this(tags);
		this.paintjobID = paintjobID;
	}
	
	public DriveableData(NBTTagCompound tags){
		parts = new HashMap<EnumDriveablePart, DriveablePart>();
		upgrades = new TreeSet<UpgradeType>();
		readFromNBT(tags);
	}

	public void readFromNBT(NBTTagCompound tag){
		if(tag == null){
			return;
		}
		if(!tag.hasKey("Type")){
			return;
		}
		type = tag.getString("Type");
		DriveableType dType = DriveableType.getDriveable(type);
		numBombs = dType.numBombSlots;
		numCargo = dType.numCargoSlots;
		numMissiles = dType.numMissileSlots;
		engine = PartType.getPart(tag.getString("Engine"));
		if(engine == null){
			engine = PartType.defaultEngines.get(EnumType.getFromObject(dType));
		}
		paintjobID = tag.getInteger("Paint");
		ammo = new ItemStack[numGuns];
		bombs = new ItemStack[numBombs];
		missiles = new ItemStack[numMissiles];
		cargo = new ItemStack[numCargo];
		for(int i = 0; i < numGuns; i++){
			if(tag.hasKey("Ammo " + i)){
				ammo[i] = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Ammo " + i));
			}
		}
		for(int i = 0; i < numBombs; i++){
			if(tag.hasKey("Bombs " + i)){
				bombs[i] = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Bombs " + i));
			}
		}
		for(int i = 0; i < numMissiles; i++){
			if(tag.hasKey("Missiles " + i)){
				missiles[i] = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Missiles " + i));
			}
		}
 		for(int i = 0; i < numCargo; i++){
			if(tag.hasKey("Cargo " + i)){
	 			cargo[i] = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Cargo " + i));
			}
 		}
		fuel = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Fuel"));
		fuelInTank = tag.getInteger("FuelInTank");
		/*for(EnumDriveablePart part : EnumDriveablePart.values()){
			parts.put(part, new DriveablePart(part, dType.health.get(part)));
		}*/
		for(DriveablePart part : parts.values()){
			part.readFromNBT(tag);
		}
		
		if(tag.hasKey("Minus")){
			NBTTagCompound nbt = tag.getCompoundTag("Minus");
			if(nbt.hasKey("HasColor") && nbt.getBoolean("HasColor")){
				hasColor = true;
				if(nbt.hasKey("PrimaryColorRed")){
					float pr = nbt.getFloat("PrimaryColorRed");
					float pg = nbt.getFloat("PrimaryColorGreen");
					float pb = nbt.getFloat("PrimaryColorBlue");
					primary_color = new RGB(pr, pg, pb);
				}
				else{
					primary_color = dType.default_primary_color;
				}
				if(nbt.hasKey("SecondaryColorRed")){
					float sr = nbt.getFloat("SecondaryColorRed");
					float sg = nbt.getFloat("SecondaryColorGreen");
					float sb = nbt.getFloat("SecondaryColorBlue");
					secondary_color = new RGB(sr, sg, sb);
				}
				else{
					secondary_color = dType.default_secondary_color;
				}
			}
			else{
				hasColor = dType.hasColor;
				if(hasColor){
					primary_color = dType.default_primary_color;
					secondary_color = dType.default_secondary_color;
				}
			}
			if(nbt.hasKey("AllowRemoteTextures") && nbt.getBoolean("AllowRemoteTextures")){
				allowURL = true;
				texture_url = nbt.getString("RemoteTexture");
			}
			else{
				allowURL = dType.allowURL;
				texture_url = new String();
			}
			if(nbt.hasKey("HasLock") && nbt.getBoolean("HasLock")){
				hasLock = true;
				isLocked = nbt.getBoolean("Locked");
				lock_code = nbt.getString("LockCode");
				spawnedKeys = nbt.getInteger("SpawnedKeys");
			}
			else{
				hasLock = dType.hasLock;
				isLocked = false;
				lock_code = ItemKey.newKeyCode();
				spawnedKeys = 0;
			}

			if(tag.hasKey("Upgrades")){
				JsonArray array = JsonUtil.getFromString(tag.getString("Upgrades")).getAsJsonArray();
				for(JsonElement elm : array){
					UpgradeType type = UpgradeType.getUpgrade(elm.getAsString());
					if(type != null){
						upgrades.add(type);
					}
				}
			}
			
		}
	}

	public NBTTagCompound writeToNBT(NBTTagCompound tag){
		tag.setString("Type", type);
		if(engine != null){
			tag.setString("Engine", engine.registryname);
		}
		tag.setInteger("Paint", paintjobID);
		for(int i = 0; i < ammo.length; i++){
			if(ammo[i] != null){
				tag.setTag("Ammo " + i, ammo[i].writeToNBT(new NBTTagCompound()));
			}
		}
		for(int i = 0; i < bombs.length; i++){
			if(bombs[i] != null){
				tag.setTag("Bombs " + i, bombs[i].writeToNBT(new NBTTagCompound()));
			}
		}
		for(int i = 0; i < missiles.length; i++){
			if(missiles[i] != null){
				tag.setTag("Missiles " + i, missiles[i].writeToNBT(new NBTTagCompound()));
			}
		}
		for(int i = 0; i < cargo.length; i++){
			if(cargo[i] != null){
				tag.setTag("Cargo " + i, cargo[i].writeToNBT(new NBTTagCompound()));
			}
		}
		if(fuel != null){
			tag.setTag("Fuel", fuel.writeToNBT(new NBTTagCompound()));
		}
		tag.setInteger("FuelInTank", (int)fuelInTank);
		for(DriveablePart part : parts.values()){
			part.writeToNBT(tag);
		}

		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setBoolean("HasColor", hasColor);
		if(hasColor){
			nbt.setFloat("PrimaryColorRed", primary_color.red);
			nbt.setFloat("PrimaryColorGreen", primary_color.green);
			nbt.setFloat("PrimaryColorBlue", primary_color.blue);
			nbt.setFloat("SecondaryColorRed", secondary_color.red);
			nbt.setFloat("SecondaryColorGreen", secondary_color.green);
			nbt.setFloat("SecondaryColorBlue", secondary_color.blue);
		}
		nbt.setBoolean("AllowRemoteTextures", allowURL);
		if(allowURL){
			nbt.setString("RemoteTexture", texture_url);
		}
		nbt.setBoolean("HasLock", hasLock);
		if(hasLock){
			nbt.setBoolean("Locked", isLocked);
			nbt.setString("LockCode", lock_code);
			nbt.setInteger("SpawnedKeys", spawnedKeys);
		}
		if(upgrades.size() > 0){
			JsonArray array = new JsonArray();
			for(UpgradeType type : upgrades){
				array.add(new JsonPrimitive(type.registryname));
			}
			tag.setString("Upgrades", array.toString());
		}
		tag.setTag("Minus", nbt);
		
		return tag;
	}
	
	@Override
	public int getSizeInventory() 
	{ 
		return getFuelSlot() + 1; 
	}

	@Override
	public ItemStack getStackInSlot(int i) 
	{ 
		//Find the correct inventory
		ItemStack[] inv = ammo;
		if(i >= ammo.length)
		{
			i -= ammo.length;
			inv = bombs;
			if(i >= bombs.length)
			{
				i -= bombs.length;
				inv = missiles;
				if(i >= missiles.length)
				{
					i -= missiles.length;
					inv = cargo;
					if(i >= cargo.length)
					{
						return fuel;
					}
				}
			}	
		}
		//Return the stack in the slot
		return inv[i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		//Find the correct inventory
		ItemStack[] inv = ammo;
		if(i >= ammo.length){
			i -= ammo.length;
			inv = bombs;
			if(i >= bombs.length){
				i -= bombs.length;
				inv = missiles;
				if(i >= missiles.length){
					i -= missiles.length;
					inv = cargo;
					if(i >= cargo.length){
						//Put the fuel stack in a stack array just to simplify the code
						i -= cargo.length;
						inv = new ItemStack[1];
						inv[0] = fuel;	
						setInventorySlotContents(getFuelSlot(), null);
					}
				}
			}	
		}
		//Decrease the stack size
		if(inv[i] != null){
			if(inv[i].stackSize <= j){
				ItemStack itemstack = inv[i];
				inv[i].stackSize = 0;
				return itemstack;
			}
			ItemStack itemstack1 = inv[i].splitStack(j);
			if(inv[i].stackSize <= 0){
				inv[i].stackSize = 0;
			}
			return itemstack1;
		}
		else{
			return null;
		}
		
	}

	@Override
	public ItemStack removeStackFromSlot(int i){ 
		return getStackInSlot(i);	
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack stack) { 
		//Find the correct inventory
		ItemStack[] inv = ammo;
		if(i >= ammo.length){
			i -= ammo.length;
			inv = bombs;
			if(i >= bombs.length){
				i -= bombs.length;
				inv = missiles;
				if(i >= missiles.length){
					i -= missiles.length;
					inv = cargo;
					if(i >= cargo.length){
						fuel = stack;
						return;
					}
				}
			}	
		}
		//Set the stack
		inv[i] = stack;
	}

    @Override
	public int getInventoryStackLimit() { 
		return 64; 
	}

	@Override
	public void markDirty(){}



	@Override
	public boolean isUseableByPlayer(EntityPlayer player){
		return true;
	}
	
	public int getAmmoInventoryStart(){
		return 0;
	}
	
	public int getBombInventoryStart(){
		return ammo.length;
	}	
	
	public int getMissileInventoryStart(){
		return ammo.length + bombs.length;
	}	
	
	public int getCargoInventoryStart(){
		return ammo.length + bombs.length + missiles.length; 
	}
	
	public int getFuelSlot(){
		return ammo.length + bombs.length + missiles.length + cargo.length;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		if(i >= getCargoInventoryStart() && i < getFuelSlot())
		{
			return true;
		}
		if(i == getFuelSlot() && itemstack != null && itemstack.getItem() instanceof ItemPart && ((ItemPart)itemstack.getItem()).type.category == EnumPartCategory.FUEL) //Fuel
		{
			return true;
		}

		return false;
	}

	@Override
	public String getName(){
		return "Flan's Secret Data"; 
	}

	@Override
	public boolean hasCustomName(){
		return false;
	}

	@Override
	public ITextComponent getDisplayName(){
		return new TextComponentString("driveable.inventory");
	}

	@Override
	public void openInventory(EntityPlayer player){
		
	}

	@Override
	public void closeInventory(EntityPlayer player){
		
	}

	@Override
	public int getField(int id) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getFieldCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void clear() {
		cargo = new ItemStack[numCargo];
	}
	
	//TODO
	public int getInventorySize(){
		int i = 0;
		for(ItemStack stack : cargo){
			if(stack != null){
				i += 1;
			}
		}
		return i;
	}
	
}
