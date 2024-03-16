package org.example.teleportmod.teleportmod.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class TeleportModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerCommands();
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(
                literal("tpmod")
                        .then(ClientCommandManager.argument("distance", IntegerArgumentType.integer())
                                .executes(this::onTpmodCommand))
        ));
    }

    private int onTpmodCommand(CommandContext<FabricClientCommandSource> context) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        ClientPlayerEntity player = minecraftClient.player;

        if (player != null) {
            int totalDistance = IntegerArgumentType.getInteger(context, "distance");

            double yaw = Math.toRadians(player.getYaw());
            double pitch = Math.toRadians(player.getPitch());

            // Calculate the direction vector based on yaw and pitch.
            double x = -Math.sin(yaw) * Math.cos(pitch);
            double y = -Math.sin(pitch);
            double z = Math.cos(yaw) * Math.cos(pitch);

            // Ensure the distance is within the teleport limit (1-8 blocks)
            int teleportLimit = 8;

            if (totalDistance >= 1 && totalDistance <= teleportLimit) {
                teleportPlayer(player, totalDistance, x, y, z);

                // Notify the player that they have teleported.
                Text message = Text.of("§8[§2TeleportMod§8] §7Teleported§a " + player.getName().getString() + " §7forward §a" + totalDistance + " §7blocks");
                context.getSource().sendFeedback(message);
            } else {
                Text errorMessage = Text.of("§8[§2TeleportMod§8] §7Teleport distance must be between §a1 §7and §a8 §7blocks");
                context.getSource().sendError(errorMessage);
            }

            return 1;
        } else {
            // Send an error message if the player is not in-game.
            Text errorMessage = Text.of("§8[§2TeleportMod§8] §7Error teleporting, please try again");
            context.getSource().sendError(errorMessage);
            return 0;
        }
    }

    private void teleportPlayer(ClientPlayerEntity player, int distance, double x, double y, double z) {
        double newX = player.getX() + x * distance;
        double newY = player.getY() + y * distance;
        double newZ = player.getZ() + z * distance;

        // Set the player's position directly.
        player.updatePosition(newX, newY, newZ);
    }
}
