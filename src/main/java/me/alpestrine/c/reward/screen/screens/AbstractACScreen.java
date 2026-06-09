package me.alpestrine.c.reward.screen.screens;

import me.alpestrine.c.reward.screen.CustomScreenHandler;
import me.alpestrine.c.reward.screen.button.ACButton;
import me.alpestrine.c.reward.screen.button.AbstractButtonHolder;
import me.alpestrine.c.reward.screen.button.ItemBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractACScreen extends AbstractButtonHolder implements Inventory, NamedScreenHandlerFactory {
    private static final int defaultRows = 3;

    public static final ItemStack filler = ItemBuilder.start(Items.GRAY_STAINED_GLASS_PANE).name("").build();
    private final int rows;
    private final DefaultedList<ItemStack> heldStacks;

    public AbstractACScreen() {
        this(defaultRows);
    }

    public AbstractACScreen(int rows) {
        this.rows = rows;
        this.heldStacks = DefaultedList.ofSize(this.rows * 9, ItemStack.EMPTY);
    }

    public void init(ServerPlayerEntity viewer) {
        fillEmpty();
        addButtons(viewer);
    }

    public ACButton setButton(int index, ACButton button) {
        return setButton(getPage(), index, button);
    }

    @Override
    public ACButton setButton(int page, int index, ACButton button) {
        ACButton ac = super.setButton(page, index, button);
        if (page == getPage()) {
            setStack(index, button.getStack());
        }
        return ac;
    }

    @Override
    public int size() {
        return rows * 9;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : heldStacks) {
            if (itemStack.isEmpty())
                continue;
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot < 0 || slot > heldStacks.size()) {
            return ItemStack.EMPTY;
        }
        return heldStacks.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(heldStacks, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(heldStacks, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        heldStacks.set(slot, stack);
        stack.capCount(this.getMaxCount(stack));
    }

    @Override
    public void clear() {
        heldStacks.clear();
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public Text getDisplayName() {
        // AHORA DEVUELVE EL TEXTO TRADUCIBLE DIRECTAMENTE
        return getName();
    }

    protected void fillEmpty() {
        for (int i = 0; i < size(); i++) {
            setStack(i, filler.copy());
        }
    }

    public void refresh(ServerPlayerEntity viewer) {

    }

    protected abstract void addButtons(ServerPlayerEntity viewer);

    // CAMBIADO DE String A Text
    public abstract Text getName();

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new CustomScreenHandler(switch (rows) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            case 6 -> ScreenHandlerType.GENERIC_9X6;
            default -> throw new IllegalArgumentException("invalid rows: " + rows);
        }, syncId, playerInventory, this, player, rows);
    }
}