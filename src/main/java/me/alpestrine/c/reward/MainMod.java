package me.alpestrine.c.reward;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.alpestrine.c.reward.screen.CustomScreenHandler;
import me.alpestrine.c.reward.screen.screens.AbstractACScreen;
import me.alpestrine.c.reward.screen.screens.reward.AbstractRewardScreen;
import me.alpestrine.c.reward.screen.screens.reward.DailyScreen;
import me.alpestrine.c.reward.screen.screens.reward.PlaytimeScreen;
import me.alpestrine.c.reward.server.MainServer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.nucleoid.server.translations.api.Localization;
import eu.pb4.polymer.core.api.utils.PolymerUtils;

import java.util.UUID;

import net.fabricmc.loader.api.FabricLoader;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.stream.Stream;

public class MainMod implements ModInitializer {
	public static final String ID = "rewards";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Rewards Mod Initializing!");
		exportTemplates();
		CommandRegistrationCallback.EVENT.register(this::register);
	}

	private void exportTemplates() {
		String[] templates = {
				"vanilla_daily.json", "vanilla_playtime.json",
				"economy_daily.json", "economy_playtime.json",
				"cobbleverse_daily.json", "cobbleverse_playtime.json"
		};
		Path templatesDir = FabricLoader.getInstance().getConfigDir().resolve("rewards").resolve("templates");
		try {
			Files.createDirectories(templatesDir);
			for (String template : templates) {
				Path file = templatesDir.resolve(template);
				// SOBREESCRIBIR SIEMPRE PARA ASEGURAR COMPATIBILIDAD CON 4.0.0
				try (InputStream is = getClass().getClassLoader().getResourceAsStream("config/templates/" + template)) {
					if (is != null) {
						Files.copy(is, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
					} else {
						LOGGER.warn("Template {} not found in resources!", template);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Failed to export templates", e);
		}
	}

	private void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
			CommandManager.RegistrationEnvironment environment) {
		for (ConfigType ct : ConfigType.values()) {
			dispatcher
					.register(CommandManager.literal(String.format("rewards-reload-%s-config", ct.name().toLowerCase()))
							.requires(source -> source.hasPermissionLevel(2) || environment.integrated)
							.executes(context -> MainServer.execute(() -> execCommand(context, environment, ct))));
		}

		// PLAYER COMMAND: /daily
		dispatcher.register(CommandManager.literal("daily")
				.executes(context -> {
					ServerCommandSource source = context.getSource();
					if (source.isExecutedByPlayer() && MainServer.allowPlayerCommand) {
						source.getPlayer().openHandledScreen(new me.alpestrine.c.reward.screen.screens.SelectionScreen());
						return 1;
					} else if (!MainServer.allowPlayerCommand) {
						source.sendError(MainMod.t(Text.translatable("commands.rewards.error.no_permission"), source.getPlayer()));
					}
					return 0;
				}));

		// PLAYER COMMAND ALIAS: /rewards open
		dispatcher.register(CommandManager.literal("rewards").then(CommandManager.literal("open")
				.executes(context -> {
					ServerCommandSource source = context.getSource();
					if (source.isExecutedByPlayer() && MainServer.allowPlayerCommand) {
						source.getPlayer().openHandledScreen(new me.alpestrine.c.reward.screen.screens.SelectionScreen());
						return 1;
					} else if (!MainServer.allowPlayerCommand) {
						source.sendError(MainMod.t(Text.translatable("commands.rewards.error.no_permission"), source.getPlayer()));
					}
					return 0;
				})));

		// ADMIN COMMAND: /rewards-setup load <template>
		dispatcher.register(CommandManager.literal("rewards-setup")
				.requires(source -> source.hasPermissionLevel(4) || environment.integrated)
				.then(CommandManager.literal("load")
						.then(CommandManager.argument("template", com.mojang.brigadier.arguments.StringArgumentType.string())
								.suggests((context, builder) -> {
									Path templatesDir = FabricLoader.getInstance().getConfigDir().resolve("rewards").resolve("templates");
									if (Files.exists(templatesDir)) {
										try (Stream<Path> stream = Files.list(templatesDir)) {
											stream.map(Path::getFileName).map(Path::toString)
													.filter(s -> s.endsWith("_daily.json"))
													.map(s -> s.replace("_daily.json", ""))
													.forEach(builder::suggest);
										} catch (IOException ignored) {}
									}
									return builder.buildFuture();
								})
								.executes(context -> {
									String templateName = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "template");
									ServerCommandSource source = context.getSource();
									Path templatesDir = FabricLoader.getInstance().getConfigDir().resolve("rewards").resolve("templates");
									Path targetDaily = FabricLoader.getInstance().getConfigDir().resolve("rewards").resolve("daily.json");
									Path targetPlaytime = FabricLoader.getInstance().getConfigDir().resolve("rewards").resolve("playtime.json");
									Path sourceDaily = templatesDir.resolve(templateName + "_daily.json");
									Path sourcePlaytime = templatesDir.resolve(templateName + "_playtime.json");
									
									if (Files.exists(sourceDaily) && Files.exists(sourcePlaytime)) {
										try {
											Files.copy(sourceDaily, targetDaily, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
											Files.copy(sourcePlaytime, targetPlaytime, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
											
											DailyScreen.dailyHandler.reload();
											PlaytimeScreen.playtimeHandler.reload();
											source.getServer().getPlayerManager().getPlayerList().forEach(MainMod::refreshIfRewardScreen);
											
											source.sendMessage(MainMod.t(Text.translatable("commands.rewards.setup.success", templateName), source.getPlayer()));
											return 1;
										} catch (Exception e) {
											LOGGER.error("Error loading template", e);
											source.sendError(MainMod.t(Text.translatable("commands.rewards.setup.error.load", e.getMessage()), source.getPlayer()));
											return 0;
										}
									} else {
										source.sendError(MainMod.t(Text.translatable("commands.rewards.setup.error.not_found", templateName), source.getPlayer()));
										return 0;
									}
								}))));

		dispatcher.register(CommandManager.literal("rewards-reset")
				.requires(source -> source.hasPermissionLevel(2) || environment.integrated)
				.then(CommandManager.argument("target", EntityArgumentType.player())
						.executes(context -> {
							ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
							me.alpestrine.c.reward.config.objects.JsonPlayerData data = AbstractRewardScreen.dataHandler
									.getForUUID(target.getUuid());

							// Ponemos todo a cero
							data.currentStreak = 0;
							data.playtimeSeconds = 0;
							data.claimedDaily.clear();
							data.claimedPlaytime.clear();
							data.lastRewardTime = 0;

							// Guardamos y actualizamos la pantalla si la tiene abierta
							MainServer.configHandlerThread.execute(AbstractRewardScreen.dataHandler::writeCurrentValue);
							MainMod.refreshIfRewardScreen(target);

							context.getSource().sendMessage(MainMod.t(
									Text.translatable("commands.rewards.reset.success", target.getName().getString()), context.getSource().getPlayer()));
							return 1;
						})));

		dispatcher.register(CommandManager.literal("rewards-setstreak")
				.requires(source -> source.hasPermissionLevel(2) || environment.integrated)
				.then(CommandManager.argument("target", EntityArgumentType.player())
						.then(CommandManager
								.argument("streak", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0))
								.executes(context -> {
									ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
									int newStreak = com.mojang.brigadier.arguments.IntegerArgumentType
											.getInteger(context, "streak");
									me.alpestrine.c.reward.config.objects.JsonPlayerData data = AbstractRewardScreen.dataHandler
											.getForUUID(target.getUuid());

									data.currentStreak = newStreak;

									MainServer.configHandlerThread
											.execute(AbstractRewardScreen.dataHandler::writeCurrentValue);
									MainMod.refreshIfRewardScreen(target);

									context.getSource()
											.sendMessage(MainMod.t(Text.translatable("commands.rewards.setstreak.success",
													target.getName().getString(), newStreak), context.getSource().getPlayer()));
									return 1;
								}))));

		dispatcher.register(CommandManager.literal("rewards-setplaytime")
				.requires(source -> source.hasPermissionLevel(2) || environment.integrated)
				.then(CommandManager.argument("target", EntityArgumentType.player())
						.then(CommandManager
								.argument("seconds", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0))
								.executes(context -> {
									ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
									int newSeconds = com.mojang.brigadier.arguments.IntegerArgumentType
											.getInteger(context, "seconds");
									me.alpestrine.c.reward.config.objects.JsonPlayerData data = AbstractRewardScreen.dataHandler
											.getForUUID(target.getUuid());

									data.playtimeSeconds = newSeconds;

									MainServer.configHandlerThread
											.execute(AbstractRewardScreen.dataHandler::writeCurrentValue);
									MainMod.refreshIfRewardScreen(target);

									context.getSource()
											.sendMessage(MainMod.t(Text.translatable("commands.rewards.setplaytime.success",
													target.getName().getString(), newSeconds), context.getSource().getPlayer()));
									return 1;
								}))));

		String rewardScreenEntity = "rewards-screen-entity";
		dispatcher.register(CommandManager.literal(rewardScreenEntity)
				.then(CommandManager.literal("add")
						.then((CommandManager.argument("targets", EntityArgumentType.entities())
								.executes(context -> doScreenEntity(context, environment, ScreenEntityAction.Added,
										EntityArgumentType.getEntity(context, "targets")))))));

		dispatcher.register(CommandManager.literal(rewardScreenEntity)
				.then(CommandManager.literal("remove")
						.then(CommandManager.argument("targets", EntityArgumentType.entities())
								.executes(context -> doScreenEntity(context, environment, ScreenEntityAction.Removed,
										EntityArgumentType.getEntity(context, "targets"))))));
	}

	private int doScreenEntity(CommandContext<ServerCommandSource> context,
			CommandManager.RegistrationEnvironment environment, ScreenEntityAction action, Entity entity) {
		ServerCommandSource scs = context.getSource();
		UUID uuid;
		if (entity == null || (uuid = entity.getUuid()) == null) {
			scs.sendError(MainMod.t(Text.translatable("commands.rewards.error.entity_null"), scs.getPlayer()));
			return 0;
		}
		if (scs.hasPermissionLevel(2) || environment.integrated) {
			boolean bl = switch (action) {
				case Added -> MainServer.screenEntities.add(uuid);
				case Removed -> MainServer.screenEntities.remove(uuid);
			};
			if (!bl) {
				scs.sendError(MainMod.t(Text.translatable(switch (action) {
					case Added -> "commands.rewards.screen_entity.already_added";
					case Removed -> "commands.rewards.screen_entity.not_found";
				}, uuid.toString()), scs.getPlayer()));

			} else {
				scs.sendMessage(MainMod.t(Text.translatable(switch (action) {
					case Added -> "commands.rewards.screen_entity.added";
					case Removed -> "commands.rewards.screen_entity.removed";
				}, uuid.toString()), scs.getPlayer()));
			}

			MainServer.configHandlerThread.execute(MainServer.globalConfigHandler::writeCurrentValue);
		} else {
			scs.sendError(MainMod.t(Text.translatable("commands.rewards.error.no_permission"), scs.getPlayer()));
		}
		return 0;
	}

	public enum ScreenEntityAction {
		Added, Removed
	}

	private void execCommand(CommandContext<ServerCommandSource> context,
			CommandManager.RegistrationEnvironment environment, ConfigType type) {
		ServerCommandSource scs = context.getSource();
		if (scs.hasPermissionLevel(2) || environment.integrated) {
			try {
				boolean reloaded = (switch (type) {
					case Daily -> DailyScreen.dailyHandler;
					case Playtime -> PlaytimeScreen.playtimeHandler;
					case PlayerData -> AbstractRewardScreen.dataHandler;
					case Global -> MainServer.globalConfigHandler;
				}).reload();

				if (reloaded) {
					scs.sendMessage(MainMod.t(Text.translatable("commands.rewards.reload.success", type.name()), scs.getPlayer()));
				} else {
					scs.sendMessage(MainMod.t(Text.translatable("commands.rewards.reload.default", type.name()), scs.getPlayer()));
				}
				scs.getServer().getPlayerManager().getPlayerList().forEach(MainMod::refreshIfRewardScreen);
			} catch (Exception e) {
				scs.sendMessage(MainMod.t(Text.translatable("commands.rewards.reload.failed", type.name()), scs.getPlayer()));
				e.printStackTrace(System.err);
			}
		} else {
			scs.sendError(MainMod.t(Text.translatable("commands.rewards.error.no_permission"), scs.getPlayer()));
		}
	}

	public static void refreshIfRewardScreen(ServerPlayerEntity spe) {
		if (spe.currentScreenHandler instanceof CustomScreenHandler csh
				&& csh.getInventory() instanceof AbstractACScreen aacs) {
			aacs.refresh(spe);
		}
	}

	public enum ConfigType {
		Daily, Playtime, Global, PlayerData
	}

	public static Text t(Text text, ServerPlayerEntity player) {
		if (player == null) return text;
		return Localization.text(text, player);
	}

	public static Text t(Text text) {
		return t(text, PolymerUtils.getPlayerContext());
	}
}