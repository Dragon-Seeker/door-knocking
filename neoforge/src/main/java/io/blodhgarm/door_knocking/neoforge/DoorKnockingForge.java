package io.blodhgarm.door_knocking.neoforge;

import io.blodhgarm.door_knocking.DoorKnocking;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@Mod(value = DoorKnocking.MODID, dist = Dist.CLIENT)
public class DoorKnockingForge {

    public DoorKnockingForge(IEventBus eventBus) {
        DoorKnocking.onInitialize(FMLPaths.CONFIGDIR::get);

        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(DoorKnocking.CONFIG_HOLDER));
        eventBus.addListener(this::onInitialize);
    }

    public void onInitialize(FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.LeftClickBlock event1) -> {
            DoorKnocking.attemptDoorInteraction(event1.getEntity(), event1.getLevel(), event1.getPos());
        });
    }
}
