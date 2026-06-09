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

    public DailyScreen() {
        super(1, calculateRows());
    }

    private static int calculateRows() {
        int max = 0;
        if (dailyHandler != null && dailyHandler.getItems() != null) {
            for (int day : dailyHandler.getItems().keySet()) {
                if (day > max) {
                    max = day;
                }
            }
        }
        if (max <= 0) {
            return 6; // fallback
        }
        int weeks = (Math.min(max, 28) + 6) / 7;
        weeks = Math.max(1, weeks);
        return weeks + 2;
    }

    private int getDaysPerPage() {
        return (size() / 9 - 2) * 7;
    }


    @Override
    protected int getMaxPage() {
        return (int) Math.ceil((double) elementAmount() / (double) getDaysPerPage());
    }

    private int getSlotForDay(int day) {
        int week = (day - 1) / 7;
        int dayOfWeek = (day - 1) % 7;
        int slot = (week + 1) * 9 + (dayOfWeek + 1);
        if (slot >= size()) {
            return size() - 1;
        }
        return slot;
    }

    @Override
    public void addButtons(ServerPlayerEntity viewer) {
        JsonPlayerData data = dataHandler.getForUUID(viewer.getUuid());
        int currentStreak = data.currentStreak;

        setButton(4, ItemBuilder.start(dailyItem)
                .name(MainMod.t(Text.translatable(
                        currentStreak == 1 ? "gui.rewards.daily.streak.singular" : "gui.rewards.daily.streak.plural",
                        currentStreak), viewer))
                .button());

        ArrayList<Map.Entry<Integer, JsonDailyReward>> rewards = new ArrayList<>(getRewards().entrySet());
        rewards.sort(Map.Entry.comparingByKey());

        for (Map.Entry<Integer, JsonDailyReward> entry : rewards) {
            JsonDailyReward jpr = entry.getValue();
            int reqTrek = entry.getKey();

            int daysPerPage = getDaysPerPage();
            int dayOnPage = reqTrek - (getPage() - 1) * daysPerPage;
            if (dayOnPage < 1 || dayOnPage > daysPerPage) {
                continue;
            }

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

            int sl = getSlotForDay(dayOnPage);
            setButton(sl, ItemBuilder.start(getClaimItem(isClaimed, isClaimable))
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