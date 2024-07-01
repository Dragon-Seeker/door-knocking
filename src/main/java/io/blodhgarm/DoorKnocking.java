package io.blodhgarm;

import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

public class DoorKnocking implements ModInitializer {

    public static final String MODID = "door_knocking";

    public static final EasyJsonConfig<Config> CONFIG_HOLDER = new EasyJsonConfig<>(DoorKnocking.id("main"), FabricLoader.getInstance()::getConfigDir,
            (object) -> {
                var volumeElement = object.get("volume");

                return new Config(volumeElement.getAsFloat());
            },
            () -> {
                var object = new JsonObject();

                object.addProperty("volume", 0.8f);

                return object;
            }
    );

    public void onInitialize() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if(!player.isSpectator() && !player.isCreative() && player.getMainHandStack().isEmpty()) {
                var blockState = world.getBlockState(pos);

                float pitch;
                boolean isWooden;

                if (blockState.isIn(BlockTags.DOORS)) {
                    isWooden = blockState.isIn(BlockTags.WOODEN_DOORS);
                    pitch = 0.6f;
                } else if (blockState.isIn(BlockTags.TRAPDOORS)) {
                    isWooden = blockState.isIn(BlockTags.WOODEN_TRAPDOORS);
                    pitch = 4.0f;
                } else {
                    return ActionResult.PASS;
                }

                var soundEvent = isWooden
                        ? SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR
                        : SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR;

                world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, CONFIG_HOLDER.instance().volume(), pitch);
            }

            return ActionResult.PASS;
        });
    }

    public record Config(float volume) {}

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
        //return Identifier.of(MODID, path);
    }
}
