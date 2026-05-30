package me.alpestrine.c.reward.screen.screens.reward;

import me.alpestrine.c.reward.MainMod;
import me.alpestrine.c.reward.config.PlaytimeConfigHandler;
import me.alpestrine.c.reward.config.objects.JsonPlayerData;
import me.alpestrine.c.reward.config.objects.JsonPlaytimeReward;
import me.alpestrine.c.reward.screen.button.ItemBuilder;
import me.alpestrine.c.reward.util.IMath;
import me.alpestrine.c.reward.util.TimeFormatter;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Map;

public class PlaytimeScreen extends AbstractRewardScreen {
    public static final Item playtimeItem = Items.CLOCK;
    public static final PlaytimeConfigHandler playtimeHandler = new PlaytimeConfigHandler();

    @Override
    public void addButtons(ServerPlayerEntity viewer) {
        JsonPlayerData data = dataHandler.getForUUID(viewer.getUuid());
        int currentPlaytime = data.playtimeSeconds;

        setButton(4, ItemBuilder.start(playtimeItem)
                .name(MainMod.t(Text.translatable("gui.rewards.playtime.played",
                        TimeFormatter.format(IMath.round(currentPlaytime / 60, 3))), viewer))
                .button());

        int currentSlot = minSlot - 1;
        int currentPage = 1;
        ArrayList<Map.Entry<Double, JsonPlaytimeReward>> rewards = new ArrayList<>(getRewards().entrySet());
        rewards.sort(Map.Entry.comparingByKey());

        for (Map.Entry<Double, JsonPlaytimeReward> entry : rewards) {
            int seconds = (int) Math.round(entry.getKey() * 60 * 60);
            JsonPlaytimeReward jpr = entry.getValue();
            boolean isClaimed = data.claimedPlaytime.contains(jpr.getId());
            boolean isClaimable = currentPlaytime >= seconds;
            int sl;
            if (currentSlot >= maxSlot) {
                currentSlot = minSlot;
                currentPage++;
                sl = currentSlot;
            } else {
                sl = (currentSlot += 1);
            }
            setButton(currentPage, sl, ItemBuilder.start(getClaimItem(isClaimed, isClaimable))
                    .name(MainMod.t(Text.translatable("gui.rewards.playtime.required",
                            TimeFormatter.format(IMath.round(seconds / 60, 3))), viewer))
                    .tooltip(getToolTip(jpr.getRewardItems()))
                    .button(event -> onClick(isClaimed, isClaimable, event.player, data, jpr, Type.Playtime)));
        }
    }

    @Override
    protected Map<Double, JsonPlaytimeReward> getRewards() {
        return playtimeHandler.getItems();
    }

    @Override
    public Text getName() {
        return MainMod.t(Text.translatable("gui.rewards.playtime.title"));
    }
}