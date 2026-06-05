package me.alpestrine.c.reward.config.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.alpestrine.c.reward.server.MainServer;
import me.alpestrine.c.reward.util.RomanNumber;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonStack implements JSONAble {
    private Item item;
    private String name;
    private int amount;
    private int damage;
    private Map<RegistryEntry<Enchantment>, Integer> enchantments;

    // --- NUEVOS CAMPOS ---
    private List<String> lore;
    private boolean glint;
    private boolean give_item = true;

    public JsonStack() {
    }

    public JsonStack(Item item, String name, int amount, int damage,
            Map<RegistryEntry<Enchantment>, Integer> enchantments) {
        this();
        this.item = item;
        this.name = name;
        this.amount = amount;
        this.damage = damage;
        this.enchantments = enchantments;
        this.give_item = true;
    }

    public boolean isGiveItem() {
        return give_item;
    }

    public ItemStack toItemStack() {
        ItemStack is = new ItemStack(item, amount);
        is.setDamage(damage);
        MinecraftServer server = MainServer.server;
        net.minecraft.registry.RegistryWrapper.WrapperLookup registries = server != null ? server.getRegistryManager()
                : null;

        if (name != null) {
            try {
                Text parsedName = Text.Serialization.fromJson(name, registries);
                is.set(DataComponentTypes.ITEM_NAME, parsedName != null ? parsedName : Text.of(name));
            } catch (Exception e) {
                is.set(DataComponentTypes.ITEM_NAME, Text.of(name));
            }
        }

        if (lore != null && !lore.isEmpty()) {
            List<Text> loreTexts = new ArrayList<>();
            for (String line : lore) {
                try {
                    Text parsedLine = Text.Serialization.fromJson(line, registries);
                    loreTexts.add(parsedLine != null ? parsedLine : Text.of(line));
                } catch (Exception e) {
                    loreTexts.add(Text.of(line));
                }
            }
            is.set(DataComponentTypes.LORE, new net.minecraft.component.type.LoreComponent(loreTexts, loreTexts));
        }

        if (glint) {
            is.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        if (enchantments != null) {
            for (Map.Entry<RegistryEntry<Enchantment>, Integer> entry : enchantments.entrySet()) {
                is.addEnchantment(entry.getKey(), entry.getValue());
            }
        }
        return is;
    }

    public List<Text> toToolTipText() {
        MinecraftServer server = MainServer.server;
        net.minecraft.registry.RegistryWrapper.WrapperLookup registries = server != null ? server.getRegistryManager()
                : null;

        ArrayList<Text> tt = new ArrayList<>();

        if (name != null) {
            try {
                Text parsedName = Text.Serialization.fromJson(name, registries);
                tt.add(Text.literal(amount + "x ").append(parsedName != null ? parsedName : Text.literal(name)));
            } catch (Exception e) {
                tt.add(Text.literal(amount + "x " + name));
            }
        } else {
            // USAR EL TRANSLATION KEY NATIVO DEL ITEM (Minecraft o Mods)
            tt.add(Text.literal(amount + "x ").append(Text.translatable(item.getTranslationKey())));
        }

        if (lore != null) {
            for (String line : lore) {
                try {
                    Text parsedLine = Text.Serialization.fromJson(line, registries);
                    tt.add(parsedLine != null ? parsedLine : Text.literal(line));
                } catch (Exception e) {
                    tt.add(Text.literal(line));
                }
            }
        }

        if (enchantments != null) {
            for (Map.Entry<RegistryEntry<Enchantment>, Integer> entry : enchantments.entrySet()) {
                Text enchText = entry.getKey().value().description();
                int level = entry.getValue();
                String rom = level != 1 ? " " + RomanNumber.toRoman(level) : "";
                tt.add(enchText.copy().append(Text.literal(rom)));
            }
        }
        return tt;
    }

    // Mantenemos este por si el mod lo llama internamente
    public List<String> toToolTip() {
        return new ArrayList<>();
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        String itemId = Registries.ITEM.getId(item).toString();
        obj.addProperty("item", itemId);
        if (amount != 1)
            obj.addProperty("amount", amount);
        if (damage != 0)
            obj.addProperty("damage", damage);
        if (name != null)
            obj.addProperty("name", name);
        if (lore != null && !lore.isEmpty()) {
            JsonArray l = new JsonArray();
            for (String s : lore)
                l.add(s);
            obj.add("lore", l);
        }
        if (glint)
            obj.addProperty("glint", true);
        if (!give_item)
            obj.addProperty("give_item", false);

        if (enchantments != null) {
            JsonObject enchants = new JsonObject();
            for (Map.Entry<RegistryEntry<Enchantment>, Integer> entry : enchantments.entrySet()) {
                RegistryEntry<Enchantment> ench = entry.getKey();
                enchants.addProperty(ench.getKey()
                        .orElseThrow(() -> new RuntimeException("Failed to get enchantment key")).getValue().toString(),
                        entry.getValue());
            }
            obj.add("enchants", enchants);
        }
        return obj;
    }

    @Override
    @SuppressWarnings("unchecked cast")
    public JsonStack fromJson(JsonObject obj) {
        Identifier id = Identifier.of(obj.get("item").getAsString());
        this.item = Registries.ITEM.getOrEmpty(id)
                .orElseThrow(() -> new RuntimeException("Failed to get item for id: " + id));
        this.amount = obj.has("amount") ? obj.get("amount").getAsInt() : 1;
        this.damage = obj.has("damage") ? obj.get("damage").getAsInt() : 0;
        this.name = obj.has("name") ? obj.get("name").getAsString() : null;

        if (obj.has("lore")) {
            this.lore = new ArrayList<>();
            for (JsonElement e : obj.getAsJsonArray("lore")) {
                this.lore.add(e.getAsString());
            }
        }
        this.glint = obj.has("glint") && obj.get("glint").getAsBoolean();
        this.give_item = !obj.has("give_item") || obj.get("give_item").getAsBoolean();

        Map<RegistryEntry<Enchantment>, Integer> enchantments = null;
        MinecraftServer server = MainServer.server;
        if (obj.get("enchants") instanceof JsonObject enchants && server != null) {
            enchantments = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : enchants.entrySet()) {
                Identifier enchId = Identifier.of(entry.getKey());
                RegistryEntry.Reference<Enchantment> enchEntry = server.getRegistryManager()
                        .get(RegistryKeys.ENCHANTMENT).getEntry(enchId)
                        .orElseThrow(() -> new RuntimeException("Failed to get enchantment"));
                enchantments.put(enchEntry, Math.max(entry.getValue().getAsInt(), 1));
            }
        }
        this.enchantments = enchantments;
        return this;
    }
}