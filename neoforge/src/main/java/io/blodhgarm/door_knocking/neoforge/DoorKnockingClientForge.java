package io.blodhgarm.door_knocking.neoforge;

import io.blodhgarm.door_knocking.DoorKnocking;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@Mod(value = DoorKnocking.MODID, dist = Dist.CLIENT)
public class DoorKnockingClientForge {

    public DoorKnockingClientForge(IEventBus eventBus) {
//        eventBus.addListener((RegisterClientReloadListenersEvent event) -> event.registerReloadListener(DoorKnocking.CONFIG_HOLDER));
    }
}
