package io.blodhgarm.door_knocking;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class DoorKnocking {

    public static final String MODID = "door_knocking";

    @NotNull
    public static EasyJsonConfig<Config> CONFIG_HOLDER = null;

    public static void onInitialize(Supplier<Path> configPathSup) {
        CONFIG_HOLDER = new EasyJsonConfig<>(DoorKnocking.id("main"), configPathSup,
                (object) -> {
                    var volume = getFrom(object, "volume", JsonElement::getAsFloat);

                    if (!object.has("knocking_checks")) return null;

                    var checks = KnockingNoiseCheck.fromJsonArray(getFrom(object, "knocking_checks", JsonElement::getAsJsonArray));

                    return new Config(volume, checks);
                },
                () -> {
                    var object = new JsonObject();

                    object.addProperty("volume", 0.6f);
                    object.add("knocking_checks", KnockingNoiseCheck.toJsonArray(DEFAULT_CHECKS));

                    return object;
                }
        );
    }

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final List<KnockingNoiseCheck> DEFAULT_CHECKS = List.of(
            new KnockingNoiseCheck(BlockTags.WOODEN_DOORS.location(), 0.6f, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR.getLocation()),
            new KnockingNoiseCheck(BlockTags.DOORS.location(), 0.6f, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR.getLocation()),
            new KnockingNoiseCheck(BlockTags.WOODEN_TRAPDOORS.location(), 4.0f, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR.getLocation()),
            new KnockingNoiseCheck(BlockTags.TRAPDOORS.location(), 4.0f, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR.getLocation()),
            new KnockingNoiseCheck(BlockTags.FENCE_GATES.location(), 1.2f, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR.getLocation(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR.getLocation())
    );

    private static <T> T getFrom(JsonObject object, String name, Function<JsonElement, T> function) {
        try {
            return function.apply(object.get(name));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to handle field [" + name + "] as decoding threw an exception!: ", e);
        }
    }

    public record KnockingNoiseCheck(ResourceLocation isOfType, float pitch, ResourceLocation primarySoundEvent, @Nullable ResourceLocation woodenSoundEvent){
        public KnockingNoiseCheck(ResourceLocation isOfType, float pitch, ResourceLocation primarySoundEvent) {
            this(isOfType, pitch, primarySoundEvent, null);
        }

        public @Nullable Pair<SoundEvent, Float> getSoundInfo(BlockState blockState) {
            if (!blockState.is(TagKey.create(Registries.BLOCK, isOfType))) return null;

            ResourceLocation soundEventId = primarySoundEvent;

            if (blockState.ignitedByLava() && woodenSoundEvent != null) {
                soundEventId = woodenSoundEvent;
            }

            var soundEvent = BuiltInRegistries.SOUND_EVENT.get(soundEventId);

            if (soundEvent == null) {
                LOGGER.warn("[DoorKnocking]: Unable to locate the given SoundEvent [{}] as it was not found in the registry!", soundEventId);

                return null;
            }

            return new Pair<>(soundEvent, pitch);
        }

        public static List<KnockingNoiseCheck> fromJsonArray(JsonArray jsonArray) {
            var checks = new ArrayList<KnockingNoiseCheck>();

            for (var checkObj : jsonArray) {
                try {
                    checks.add(KnockingNoiseCheck.fromJson(checkObj.getAsJsonObject()));
                } catch (Exception e) {
                    throw new IllegalStateException("Unable to decode a knocking check as it was invalid in some way: ", e);
                }
            }

            return Collections.unmodifiableList(checks);
        }

        public static JsonElement toJsonArray(List<KnockingNoiseCheck> checks) {
            var array = new JsonArray();

            checks.stream()
                    .map(KnockingNoiseCheck::toJson)
                    .forEach(array::add);

            return array;
        }

        public static KnockingNoiseCheck fromJson(JsonObject jsonObject) {
            var isOfTypeTag = ResourceLocation.tryParse(getFrom(jsonObject, "block_tag", JsonElement::getAsString));
            var pitch = getFrom(jsonObject, "pitch", JsonElement::getAsFloat);
            var primarySoundEvent = ResourceLocation.tryParse(getFrom(jsonObject, "sound_event", JsonElement::getAsString));

            ResourceLocation woodenSoundEvent = null;

            if (jsonObject.has("wooden_sound_event")) {
                woodenSoundEvent = ResourceLocation.tryParse(getFrom(jsonObject, "wooden_sound_event", JsonElement::getAsString));
            }

            return new KnockingNoiseCheck(isOfTypeTag, pitch, primarySoundEvent, woodenSoundEvent);
        }

        public JsonElement toJson() {
            var obj = new JsonObject();

            obj.addProperty("block_tag", this.isOfType.toString());
            obj.addProperty("pitch", this.pitch);
            obj.addProperty("sound_event", this.primarySoundEvent.toString());

            if (this.woodenSoundEvent != null){
                obj.addProperty("wooden_sound_event", this.woodenSoundEvent.toString());
            }

            return obj;
        }
    }

    public static InteractionResult attemptDoorInteraction(Player player, Level world, BlockPos pos) {
        if(!player.isSpectator() && !player.isCreative() && player.getMainHandItem().isEmpty()) {
            var blockState = world.getBlockState(pos);

            for (var check : DoorKnocking.CONFIG_HOLDER.instance().checks()) {
                var soundData = check.getSoundInfo(blockState);

                if (soundData != null) {
                    world.playSound(null, pos, soundData.getFirst(), SoundSource.BLOCKS, DoorKnocking.CONFIG_HOLDER.instance().volume(), soundData.getSecond());
                    break;
                }
            }
        }

        return InteractionResult.PASS;
    }

    public record Config(float volume, List<KnockingNoiseCheck> checks) {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
