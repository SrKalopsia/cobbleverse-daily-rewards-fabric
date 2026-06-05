package me.alpestrine.c.reward.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import me.alpestrine.c.reward.config.objects.JsonPlaytimeReward;
import me.alpestrine.c.reward.config.objects.JsonStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class PlaytimeConfigHandler implements BasicConfigReader<Double, JsonPlaytimeReward> {
    private JsonArray value = new JsonArray();
    private Map<Double, JsonPlaytimeReward> stacks = new HashMap<>();

    @Override
    public Map<Double, JsonPlaytimeReward> getItems() {
        return Map.copyOf(stacks);
    }

    @Override
    public JsonArray getJson() {
        return value.deepCopy();
    }

    @Override
    public String getDefault() {
        /*JsonArray ja = new JsonArray();

        for (int i = 15; i <= 45; i += 15) {
            ArrayList<JsonStack> jss = new ArrayList<>();
            for (int items = 0; items < ThreadLocalRandom.current().nextInt(1, 4); items++) {
                jss.add(getRandomItem());
            }

            ja.add(new JsonPlaytimeReward().fromJson(new JsonPlaytimeReward()
                    .setTimeThing(JsonPlaytimeReward.class, secondsToHours(i))
                    .addJsonStack(JsonPlaytimeReward.class, jss.toArray(new JsonStack[0]))
                    .toJson()).toJson());
        }
        return gson.toJson(ja);*/

        return readStringFromAsset("config/templates/vanilla_playtime.json");
    }

    @Override
    public void setJson(JsonArray value) {
        this.value = value;

        Map<Double, JsonPlaytimeReward> stacks = new HashMap<>();
        for (JsonElement je : value) {
            JsonPlaytimeReward jdr = new JsonPlaytimeReward().fromJson(je.getAsJsonObject());
            stacks.put(jdr.getTime(), jdr);
        }
        this.stacks = stacks;
    }

    @Override
    public String getName() {
        return "playtime";
    }
}
