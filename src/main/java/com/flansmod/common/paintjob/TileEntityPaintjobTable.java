package com.flansmod.common.paintjob;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;

public class TileEntityPaintjobTable extends TileEntity implements IInventory, ITickable
{
	// Stack 0 is InfoType being painted. Stack 1 is paint cans
	private NonNullList<ItemStack> inventoryStacks = NonNullList.withSize(2, ItemStack.EMPTY);
	//private CustomPaintjob inProgressPaintjob;
	
	public TileEntityPaintjobTable()
	{
		
	}
	
	@Override
	public String getName() { return "PaintjobTable"; }

	@Override
	public boolean hasCustomName() { return false; }

	@Override
	public ITextComponent getDisplayName() { return null; }

	@Override
	public int getSizeInventory() { return 2; }

	@Override
	public ItemStack getStackInSlot(int index) 
	{ 
		return inventoryStacks.get(index); 
	}

	@Override
	public ItemStack decrStackSize(int index, int count) 
	{ 
		if(getStackInSlot(index) != null) 
		{ 
			if(count >= getStackInSlot(index).getCount())
			{
				ItemStack returnStack = getStackInSlot(index);
				setInventorySlotContents(index, null);
				return returnStack;
			}
			else
			{
				ItemStack returnStack = getStackInSlot(index).splitStack(count);
				
				return returnStack;
			}
		} 
		return null; 
	}

	@Override
	public ItemStack removeStackFromSlot(int index) 
	{ 
		ItemStack returnStack = getStackInSlot(index);
		setInventorySlotContents(index, null);
		return returnStack; 
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) 
	{ 
		inventoryStacks.set(index, stack);
	}

	@Override
	public int getInventoryStackLimit() { return 64; }

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) { return true; }

	@Override
	public void openInventory(EntityPlayer player) { }

	@Override
	public void closeInventory(EntityPlayer player) { }

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) { return true; }

	@Override
	public int getField(int id) { return 0; }

	@Override
	public void setField(int id, int value) { }

	@Override
	public int getFieldCount() { return 0; }

	@Override
	public void clear() 
	{ 
		for(int i = 0; i < getSizeInventory(); i++)
		{
			setInventorySlotContents(i, null);
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		
		for(int i = 0; i < inventoryStacks.size(); i++)
		{
			NBTTagCompound stackNBT = new NBTTagCompound();
			if(getStackInSlot(i) != null)
				getStackInSlot(i).writeToNBT(stackNBT);
			nbt.setTag("stack_" + i, stackNBT);
		}
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		for(int i = 0; i < inventoryStacks.size(); i++)
		{
			setInventorySlotContents(i, new ItemStack(nbt.getCompoundTag("stack_" + i)));
		}
	}

	@Override
	public void update() 
	{
	}
	
	@Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
        return new SPacketUpdateTileEntity(getPos(), getBlockMetadata(), nbt);
    }
	
	@Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, SPacketUpdateTileEntity packet)
    {
		readFromNBT(packet.getNbtCompound());
    }

	public ItemStack getPaintableStack() 
	{
		return inventoryStacks.get(0);
	}

	public void setPaintableStack(ItemStack stack) 
	{
		inventoryStacks.set(0, stack);
	}
	
	public ItemStack getPaintCans()
	{
		return inventoryStacks.get(1);
	}

	@Override
	public boolean isEmpty(){
		return inventoryStacks.isEmpty();
	}
}
