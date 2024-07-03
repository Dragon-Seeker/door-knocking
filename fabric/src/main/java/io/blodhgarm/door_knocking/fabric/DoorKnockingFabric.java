package io.blodhgarm.door_knocking.fabric;

import io.blodhgarm.door_knocking.DoorKnocking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public class DoorKnockingFabric implements ModInitializer {

    public void onInitialize() {
        DoorKnocking.onInitialize(FabricLoader.getInstance()::getConfigDir);

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> DoorKnocking.attemptDoorInteraction(player, world, pos));

        var listener = new SimpleSynchronousResourceReloadListener() {
            @Override public ResourceLocation getFabricId() { return DoorKnocking.CONFIG_HOLDER.getId(); }
            @Override public void onResourceManagerReload(ResourceManager resourceManager) { DoorKnocking.CONFIG_HOLDER.onResourceManagerReload(resourceManager); }
        };

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(listener);
    }
}
