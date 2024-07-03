package io.blodhgarm.door_knocking;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.function.Supplier;

public class DoorKnocking {

    public static final String MODID = "door_knocking";

    @NotNull
    public static EasyJsonConfig<Config> CONFIG_HOLDER = null;

    public static void onInitialize(Supplier<Path> configPathSup) {
        CONFIG_HOLDER = new EasyJsonConfig<>(DoorKnocking.id("main"), configPathSup,
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
    }

    public static InteractionResult attemptDoorInteraction(Player player, Level world, BlockPos pos) {
        if(!player.isSpectator() && !player.isCreative() && player.getMainHandItem().isEmpty()) {
            var blockState = world.getBlockState(pos);

            float pitch;
            boolean isWooden;

            if (blockState.is(BlockTags.DOORS)) {
                isWooden = blockState.is(BlockTags.WOODEN_DOORS);
                pitch = 0.6f;
            } else if (blockState.is(BlockTags.TRAPDOORS)) {
                isWooden = blockState.is(BlockTags.WOODEN_TRAPDOORS);
                pitch = 4.0f;
            } else {
                return InteractionResult.PASS;
            }

            var soundEvent = isWooden
                    ? SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR
                    : SoundEvents.ZOMBIE_ATTACK_IRON_DOOR;

            world.playSound(null, pos, soundEvent, SoundSource.BLOCKS, DoorKnocking.CONFIG_HOLDER.instance().volume(), pitch);
        }

        return InteractionResult.PASS;
    }

    public record Config(float volume) {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
