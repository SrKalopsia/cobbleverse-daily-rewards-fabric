package me.alpestrine.c.reward.screen.screens.reward;

import me.alpestrine.c.reward.MainMod;
import me.alpestrine.c.reward.config.PlayerDataHandler;
import me.alpestrine.c.reward.config.objects.JsonBaseReward;
import me.alpestrine.c.reward.config.objects.JsonPlayerData;
import me.alpestrine.c.reward.config.objects.JsonStack;
import me.alpestrine.c.reward.screen.button.ItemBuilder;
import me.alpestrine.c.reward.screen.screens.AbstractACScreen;
import me.alpestrine.c.reward.screen.screens.SelectionScreen;
import me.alpestrine.c.reward.server.MainServer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractRewardScreen extends AbstractACScreen {
    public static final Item canClaim = Items.LIME_STAINED_GLASS_PANE;
    public static final Item alreadyClaimed = Items.GREEN_STAINED_GLASS_PANE;
    public static final Item cannotBeClaimed = Items.RED_STAINED_GLASS_PANE;

    public static final Item backItem = Items.COMPASS;
    public static final Item prevItem = Items.OAK_SIGN;
    public static final Item nextItem = Items.OAK_SIGN;
    public static final Item invalidItem = Items.GRAY_STAINED_GLASS_PANE;

    protected static final int maxSlot = 16;
    protected static final int minSlot = 10;

    public static final PlayerDataHandler dataHandler = new PlayerDataHandler();

    protected int page;

    protected AbstractRewardScreen() {
        this(1);
    }

    protected AbstractRewardScreen(int page) {
        setPage(page);
    }

    protected void setPage(int page) {
        this.page = Math.max(Math.min(page, getMaxPage()), 1);
    }

    protected boolean canSetPageTo(int page) {
        return page == Math.max(Math.min(page, getMaxPage()), 1);
    }

    protected int getMaxPage() {
        return (int) Math.ceil((double) elementAmount() / 7.0);
    }

    protected abstract Map<? extends Number, ? extends JsonBaseReward<?>> getRewards();

    protected int elementAmount() {
        return getRewards().size();
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    protected void fillEmpty() {
        super.fillEmpty();

        setButton(18, ItemBuilder.start(backItem).name(MainMod.t(Text.translatable("gui.rewards.back_button")))
                .button(event -> event.player.openHandledScreen(new SelectionScreen())));

        int prev = getPage() - 1;
        boolean canPrev = canSetPageTo(prev);
        setButton(25,
                ItemBuilder.start(canPrev ? prevItem : invalidItem)
                        .name(canPrev ? MainMod.t(Text.translatable("gui.rewards.prev_page", prev)) : Text.empty())
                        .button(canPrev ? event -> setPageForPlayer(prev, event.player) : null));

        int next = getPage() + 1;
        boolean canNext = canSetPageTo(next);
        setButton(26,
                ItemBuilder.start(canNext ? nextItem : invalidItem)
                        .name(canNext ? MainMod.t(Text.translatable("gui.rewards.next_page", next)) : Text.empty())
                        .button(canNext ? event -> setPageForPlayer(next, event.player) : null));
    }

    private void setPageForPlayer(int page, ServerPlayerEntity viewer) {
        setPage(page);
        init(viewer);
        viewer.currentScreenHandler.updateToClient();
    }

    @Override
    public void refresh(ServerPlayerEntity viewer) {
        setPageForPlayer(getPage(), viewer);
    }

    protected Text[] getToolTip(List<JsonStack> stacks, ServerPlayerEntity viewer) {
        ArrayList<Text> tooltips = new ArrayList<>();
        for (JsonStack stack : stacks) {
            tooltips.add(Text.empty());
            for (Text t : stack.toToolTipText()) {
                tooltips.add(MainMod.t(t, viewer));
            }
        }
        return tooltips.toArray(new Text[0]);
    }

    protected Item getClaimItem(boolean isClaimed, boolean isClaimable) {
        return isClaimed ? alreadyClaimed : (isClaimable ? canClaim : cannotBeClaimed);
    }

    protected void onClick(boolean isClaimed, boolean isClaimable, ServerPlayerEntity player, JsonPlayerData data,
            JsonBaseReward<?> jbr, Type type) {
        if (!isClaimed && isClaimable) {
            List<ItemStack> stacks = jbr.getRewardItems().stream()
                    .filter(JsonStack::isGiveItem)
                    .map(JsonStack::toItemStack)
                    .toList();

            if (!stacks.isEmpty()) {
                int empty = 0;
                for (ItemStack iStack : player.getInventory().main) {
                    if (!iStack.isEmpty())
                        continue;
                    empty++;
                }
                if (empty < stacks.size()) {
                    player.sendMessage(MainMod.t(Text.translatable("gui.rewards.inventory_full", stacks.size(), empty)
                            .formatted(Formatting.RED), player));
                    return;
                }
                for (ItemStack is : stacks) {
                    player.getInventory().insertStack(is);
                }
            }

            if (jbr.getCommands() != null && !jbr.getCommands().isEmpty()) {
                net.minecraft.server.MinecraftServer server = player.getServer();
                if (server != null) {
                    for (String cmd : jbr.getCommands()) {
                        String finalCmd = cmd.replace("%player%", player.getName().getString());
                        server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), finalCmd);
                    }
                }
            }

            switch (type) {
                case Playtime -> data.claimedPlaytime.add(jbr.getId());
                case Daily -> data.claimedDaily.add(jbr.getId());
            }

            // REPRODUCIR SONIDO AL RECLAMAR
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f);

            MainServer.configHandlerThread.execute(AbstractRewardScreen.dataHandler::writeCurrentValue);
            refresh(player);
        }
    }

    public enum Type {
        Playtime, Daily
    }
}