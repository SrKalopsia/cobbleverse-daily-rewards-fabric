package me.alpestrine.c.reward.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public interface TimeFormatter {
    static Text format(int minutes) {
        int days = minutes / 1440;
        int hours = (minutes % 1440) / 60;
        int mins = minutes % 60;

        MutableText text = Text.empty();
        boolean hasPrev = false;

        if (days > 0) {
            text.append(Text.translatable(days > 1 ? "gui.rewards.time.days" : "gui.rewards.time.day", days));
            hasPrev = true;
        }

        if (hours > 0) {
            if (hasPrev) text.append(Text.literal(", "));
            text.append(Text.translatable(hours > 1 ? "gui.rewards.time.hours" : "gui.rewards.time.hour", hours));
            hasPrev = true;
        }

        if (mins > 0 || !hasPrev) {
            if (hasPrev) text.append(Text.literal(", "));
            text.append(Text.translatable(mins > 1 || mins == 0 ? "gui.rewards.time.minutes" : "gui.rewards.time.minute", mins));
        }

        return text;
    }

    static Text formatShort(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        minutes %= 60;

        MutableText text = Text.empty();
        if (hours > 0) {
            text.append(Text.literal(String.valueOf(hours)))
                    .append(Text.translatable("gui.rewards.time.short.hour"));
            if (minutes > 0) text.append(Text.literal(" "));
        }
        if (minutes > 0 || (hours == 0)) {
            text.append(Text.literal(String.valueOf(minutes)))
                    .append(Text.translatable("gui.rewards.time.short.minute"));
        }
        return text;
    }
}
