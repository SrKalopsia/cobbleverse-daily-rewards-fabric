package me.alpestrine.c.reward.screen.screens.reward;

import me.alpestrine.c.reward.MainMod;
import me.alpestrine.c.reward.config.DailyConfigHandler;
import me.alpestrine.c.reward.config.objects.JsonDailyReward;
import me.alpestrine.c.reward.config.objects.JsonPlayerData;
import me.alpestrine.c.reward.screen.button.ItemBuilder;
import me.alpestrine.c.reward.server.MainServer;
import me.alpestrine.c.reward.util.TimeFormatter;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Map;

public class DailyScreen extends AbstractRewardScreen {
    public static final Item dailyItem = Items.BELL;
    public static final DailyConfigHandler dailyHandler = new DailyConfigHandler();

    @Override
    public void addButtons(ServerPlayerEntity viewer) {
        JsonPlayerData data = dataHandler.getForUUID(viewer.getUuid());
        int currentStreak = data.currentStreak;

        setButton(4, ItemBuilder.start(dailyItem)
                .name(MainMod.t(Text.translatable(
                        currentStreak == 1 ? "gui.rewards.daily.streak.singular" : "gui.rewards.daily.streak.plural",
                        currentStreak), viewer))
                .button());

        int currentSlot = minSlot - 1;
        int currentPage = 1;
        ArrayList<Map.Entry<Integer, JsonDailyReward>> rewards = new ArrayList<>(getRewards().entrySet());
        rewards.sort(Map.Entry.comparingByKey());

        for (Map.Entry<Integer, JsonDailyReward> entry : rewards) {
            JsonDailyReward jpr = entry.getValue();
            int reqTrek = entry.getKey();
            boolean isClaimed = data.claimedDaily.contains(jpr.getId());
            boolean isClaimable = currentStreak >= reqTrek;

            Text[] tooltip = getToolTip(jpr.getRewardItems(), viewer);
            if (!isClaimed && !isClaimable && reqTrek == currentStreak + 1) {
                long currentTime = System.currentTimeMillis();
                long nextAdvancement = data.lastRewardTime + MainServer.millisecondsInDay;
                long timeLeft = nextAdvancement - currentTime;

                if (timeLeft > 0) {
                    Text cooldownText = MainMod.t(Text.translatable("gui.rewards.daily.cooldown", TimeFormatter.formatShort(timeLeft)), viewer);

                    Text[] newTooltip = new Text[tooltip.length + 2];
                    System.arraycopy(tooltip, 0, newTooltip, 0, tooltip.length);
                    newTooltip[tooltip.length] = Text.empty();
                    newTooltip[tooltip.length + 1] = cooldownText.copy().formatted(Formatting.GRAY);
                    tooltip = newTooltip;
                }
            }

            int sl;
            if (currentSlot >= maxSlot) {
                currentSlot = minSlot;
                currentPage++;
                sl = currentSlot;
            } else {
                sl = (currentSlot += 1);
            }
            setButton(currentPage, sl, ItemBuilder.start(getClaimItem(isClaimed, isClaimable))
                    .name(MainMod.t(Text.translatable("gui.rewards.daily.day", reqTrek), viewer))
                    .tooltip(tooltip)
                    .button(event -> onClick(isClaimed, isClaimable, event.player, data, jpr, Type.Daily)));
        }
    }

    @Override
    public Text getName() {
        return MainMod.t(Text.translatable("gui.rewards.daily.title"));
    }

    @Override
    protected Map<Integer, JsonDailyReward> getRewards() {
        return dailyHandler.getItems();
    }
}