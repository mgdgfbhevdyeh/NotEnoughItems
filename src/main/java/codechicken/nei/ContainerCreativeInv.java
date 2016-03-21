package codechicken.nei;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerCreativeInv extends Container {
    private class SlotArmor extends Slot {
        EntityEquipmentSlot equipmentSlot;

        @Deprecated //TODO remove this.
        public SlotArmor(IInventory inv, int slot, int x, int y, int armorType) {
            this(inv, slot, x, y, getArmorEquipmentSlotFromSlotID(armorType));
        }

        public SlotArmor(IInventory inv, int slot, int x, int y, EntityEquipmentSlot armor) {
            super(inv, slot, x, y);
            equipmentSlot = armor;
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return stack != null && stack.getItem().isValidArmor(stack, equipmentSlot, player);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public String getSlotTexture() {
            return ItemArmor.EMPTY_SLOT_NAMES[equipmentSlot.getIndex()];
        }
    }

    private class SlotBlockArmor extends SlotArmor {
        public SlotBlockArmor(IInventory inv, int slot, int x, int y, int armor) {
            super(inv, slot, x, y, armor);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return super.isItemValid(stack) || stack != null;// && Block.getBlockFromItem(stack.getItem()) != Blocks.air;
        }
    }

    public EntityPlayer player;

    public ContainerCreativeInv(EntityPlayer player, ExtendedCreativeInv extraInv) {
        this.player = player;
        InventoryPlayer invPlayer = player.inventory;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(extraInv, col + row * 9, 8 + col * 18, 5 + row * 18));
            }
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlotToContainer(new Slot(invPlayer, col + row * 9 + 9, 8 + col * 18, 118 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            addSlotToContainer(new Slot(invPlayer, col, 8 + col * 18, 176));
        }

        addSlotToContainer(new SlotBlockArmor(invPlayer, invPlayer.getSizeInventory() - 1, -15, 23, 0));
        for (int armorType = 1; armorType < 4; armorType++) {
            addSlotToContainer(new SlotArmor(invPlayer, invPlayer.getSizeInventory() - 1 - armorType, -15, 23 + armorType * 18, armorType));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slotIndex) {
        ItemStack transferredStack = null;
        Slot slot = (Slot) inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            transferredStack = stack.copy();

            if (stack.getItem() instanceof ItemArmor) {
                ItemArmor armor = (ItemArmor) stack.getItem();
                if (!getSlot(90 + armor.armorType.getIndex()).getHasStack()) {
                    getSlot(90 + armor.armorType.getIndex()).putStack(transferredStack);
                    slot.putStack(null);
                    return transferredStack;
                }
            }

            if (slotIndex < 54) {
                if (!this.mergeItemStack(stack, 54, 90, true)) {
                    return null;
                }
            } else if (!this.mergeItemStack(stack, 0, 54, false)) {
                return null;
            }

            if (stack.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
        }

        return transferredStack;
    }

    public boolean canInteractWith(EntityPlayer var1) {
        return true;
    }

    private static EntityEquipmentSlot getArmorEquipmentSlotFromSlotID(int armorType) {
        EntityEquipmentSlot entityEquipmentSlot = null;
        for (EntityEquipmentSlot equipmentSlot : EntityEquipmentSlot.values()) {
            if (equipmentSlot.func_188452_c() == armorType) {
                entityEquipmentSlot = equipmentSlot;
            }
        }
        if (entityEquipmentSlot == null) {
            throw new RuntimeException(String.format("Invalid slot number for armor slot! Given [%s], Expected [1 -> 4]", armorType));
        }
        return entityEquipmentSlot;
    }
}
