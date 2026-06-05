package me.alpestrine.c.reward.server;

import me.alpestrine.c.reward.MainMod;
import me.alpestrine.c.reward.config.GlobalConfigHandler;
import me.alpestrine.c.reward.config.objects.JsonPlayerData;
import me.alpestrine.c.reward.screen.CustomScreenHandler;
import me.alpestrine.c.reward.screen.button.InventoryEvent;
import me.alpestrine.c.reward.screen.screens.AbstractACScreen;
import me.alpestrine.c.reward.screen.screens.reward.AbstractRewardScreen;
import me.alpestrine.c.reward.screen.screens.reward.DailyScreen;
import me.alpestrine.c.reward.screen.screens.reward.PlaytimeScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainServer {
    public static final Executor configHandlerThread = Executors.newSingleThreadExecutor();
    public static final GlobalConfigHandler globalConfigHandler = new GlobalConfigHandler();
    public static int millisecondsInDay;
    public static int ticksPerUpdate;
    public static boolean allowPlayerCommand = true;
    public static HashSet<UUID> screenEntities = new HashSet<>();
    private static int lastSave;
    public static MinecraftServer server;

    public static void onInit(Object object) {
        server = (MinecraftServer) object;
        DailyScreen.dailyHandler.onInit();
        PlaytimeScreen.playtimeHandler.onInit();
        AbstractRewardScreen.dataHandler.onInit();
        globalConfigHandler.onInit();
    }

    public static int execute(Runnable task) {
        configHandlerThread.execute(task);
        return 0;
    }

    public static void handleClick(ScreenHandler sh, int slotIndex, int button, SlotActionType actionType,
            PlayerEntity player, CallbackInfo ci) {
        try {
            if (!(player instanceof ServerPlayerEntity spe)) {
                return;
            }
            if (sh instanceof CustomScreenHandler) {
                ci.cancel();
            }

            // --- INICIO DE LA VALIDACIÓN AÑADIDA ---
            // Si el índice es menor a 0 y no es un clic fuera del inventario (EMPTY_SPACE),
            // lo ignoramos.
            if (slotIndex < 0 && slotIndex != ScreenHandler.EMPTY_SPACE_SLOT_INDEX) {
                return;
            }
            // --- FIN DE LA VALIDACIÓN AÑADIDA ---

            Inventory inventory = slotIndex == ScreenHandler.EMPTY_SPACE_SLOT_INDEX ? player.getInventory()
                    : sh.slots.get(slotIndex).inventory;
            InventoryEvent ie = new InventoryEvent(slotIndex, button, actionType, inventory, spe);
            if (inventory instanceof AbstractACScreen s) {
                s.onClick(ie);
            }
        } catch (Exception e) {
            System.err.println("Error handling click mixin");
            e.printStackTrace(System.err);
        }
    }

    public static void onTick() {
        int tect = TickExecutor.currentTick.get() + 1;
        TickExecutor.tickTasks.getOrDefault(tect, new ArrayList<>()).forEach(Runnable::run);
        TickExecutor.currentTick.set(tect);
        if (tect >= lastSave + ticksPerUpdate) {
            boolean toUpdate = false;
            long currentTime = System.currentTimeMillis();

            for (ServerPlayerEntity spe : MainServer.server.getPlayerManager().getPlayerList()) {
                JsonPlayerData join = AbstractRewardScreen.dataHandler.getForUUID(spe.getUuid());

                // 1. Pérdida de racha por inactividad (>48 horas)
                if (join.currentStreak > 0 && currentTime > join.lastRewardTime + (millisecondsInDay * 2L)) {
                    join.currentStreak = 0;
                    join.claimedDaily.clear();
                }

                // 2. Aumento de racha al pasar 24 horas (o entrar por primera vez)
                if (join.currentStreak == 0 || join.lastRewardTime + millisecondsInDay < currentTime) {
                    join.currentStreak++;

                    // --- NUEVA LÓGICA DE LOOP (REINICIO AUTOMÁTICO) ---
                    // Buscamos cuál es el último día configurado en daily.json
                    int maxDay = 0;
                    for (Integer day : DailyScreen.dailyHandler.getItems().keySet()) {
                        if (day > maxDay) {
                            maxDay = day;
                        }
                    }

                    // Si el jugador entra y su racha supera el último día, empieza el ciclo de
                    // nuevo
                    if (maxDay > 0 && join.currentStreak > maxDay) {
                        join.currentStreak = 1;
                        join.claimedDaily.clear(); // Resetea el historial de reclamados
                    }
                    // ---------------------------------------------------

                    join.lastRewardTime = currentTime;
                }

                join.playtimeSeconds += ticksPerUpdate / 20;
                join.lastJoin = currentTime;
                MainMod.refreshIfRewardScreen(spe);
                toUpdate = true;
            }

            lastSave = tect;
            if (toUpdate) {
                configHandlerThread.execute(AbstractRewardScreen.dataHandler::writeCurrentValue);
            }
        }
    }
}