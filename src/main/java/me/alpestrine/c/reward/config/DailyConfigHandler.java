package me.alpestrine.c.reward.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import me.alpestrine.c.reward.config.objects.JsonDailyReward;
import me.alpestrine.c.reward.config.objects.JsonStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class DailyConfigHandler implements BasicConfigReader<Integer, JsonDailyReward> {
    private JsonArray value = new JsonArray();
    private Map<Integer, JsonDailyReward> stacks = new HashMap<>();


    @Override
    public Map<Integer, JsonDailyReward> getItems() {
        return Map.copyOf(stacks);
    }

    @Override
    public JsonArray getJson() {
        return value.deepCopy();
    }

    @Override
    public String getDefault() {
        /*JsonArray ja = new JsonArray();

        for (int i = 1; i <= 3; i++) {
            ArrayList<JsonStack> jss = new ArrayList<>();
            for (int items = 0; items < ThreadLocalRandom.current().nextInt(1, 4); items++) {
                jss.add(getRandomItem());
            }

            ja.add(new JsonDailyReward().fromJson(new JsonDailyReward()
                    .setTimeThing(JsonDailyReward.class, i)
                    .addJsonStack(JsonDailyReward.class, jss.toArray(new JsonStack[0]))
                    .toJson()).toJson());
        }
        return gson.toJson(ja);*/

        return readStringFromAsset("config/templates/vanilla_daily.json");
    }

    @Override
    public void setJson(JsonArray value) {
        this.value = value;

        Map<Integer, JsonDailyReward> stacks = new HashMap<>();
        for (JsonElement je : value) {
            JsonDailyReward jdr = new JsonDailyReward().fromJson(je.getAsJsonObject());
            stacks.put(jdr.getTime(), jdr);
        }
        this.stacks = stacks;
    }

    @Override
    public String getName() {
        return "daily";
    }
}
