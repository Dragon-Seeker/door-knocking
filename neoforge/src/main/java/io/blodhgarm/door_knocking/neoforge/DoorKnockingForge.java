package io.blodhgarm.door_knocking.neoforge;

import io.blodhgarm.door_knocking.DoorKnocking;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@Mod(value = DoorKnocking.MODID)
public class DoorKnockingForge {

    public DoorKnockingForge(IEventBus eventBus) {
        DoorKnocking.onInitialize(FMLPaths.CONFIGDIR::get);

        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(DoorKnocking.CONFIG_HOLDER));
        eventBus.addListener(this::onInitialize);
    }

    public void onInitialize(FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.LeftClickBlock event1) -> {
            if (event1.getLevel().isClientSide) return;

            if (event1.getAction().equals(PlayerInteractEvent.LeftClickBlock.Action.START))
                DoorKnocking.attemptDoorInteraction(event1.getEntity(), event1.getLevel(), event1.getPos());
        });
    }
}
