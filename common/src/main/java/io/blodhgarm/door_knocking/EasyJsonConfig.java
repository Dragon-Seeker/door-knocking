package io.blodhgarm.door_knocking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EasyJsonConfig<T> implements ResourceManagerReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Gson gson;
    private final ResourceLocation configName;

    private final Supplier<Path> configPathSup;

    private final Function<JsonObject, T> reader;
    private final Supplier<JsonObject> factory;

    @Nullable
    private T instance = null;

    public EasyJsonConfig(ResourceLocation configName, Supplier<Path> configPathSup, Function<JsonObject, T> reader, Supplier<JsonObject> constructor) {
        this(gsonBuilder -> {}, configName, configPathSup, reader, constructor);
    }

    public EasyJsonConfig(Consumer<GsonBuilder> builderConsumer, ResourceLocation configName, Supplier<Path> configPathSup, Function<JsonObject, T> reader, Supplier<JsonObject> factory) {
        var builder = new GsonBuilder().setPrettyPrinting();

        builderConsumer.accept(builder);

        this.gson = builder.create();
        this.configName = configName;

        this.configPathSup = configPathSup;

        this.reader = reader;
        this.factory = factory;

        init();
    }

    @Nullable
    public T instance() {
        return this.instance;
    }

    public void init() {
        File configFolder = configPathSup.get().resolve(configName.getNamespace()).toFile();

        if (!configFolder.exists()) {
            if (!configFolder.mkdir()) {
                LOGGER.warn("[EasyJsonConfig({})] Could not create configuration directory: {}", configName, configFolder.getAbsolutePath());
            }
        }

        JsonObject configObject = null;

        File configFile = new File(configFolder, configName.getPath() + ".json");

        try {
            if (!configFile.exists()) {
                LOGGER.info("[EasyJsonConfig({})]: Unable to find needed config file, will attempt to create such.", configName);

                configObject = factory.get();

                Files.createFile(configFile.toPath());

                try (FileWriter writer = new FileWriter(configFile)) {
                    writer.write(gson.toJson(configObject));
                }
            } else {
                configObject = gson.fromJson(Files.readString(configFile.toPath()), JsonObject.class);

                if (configObject == null) throw new IOException("The config file was not found!");
            }
        } catch (IOException exception) {
            LOGGER.error("[EasyJsonConfig({})]: Unable to create the needed config file, using default values!", configName, exception);
        } catch (JsonSyntaxException exception) {
            LOGGER.error("[EasyJsonConfig({})]: Unable to read the needed config file, using default values!", configName, exception);
        }

        try {
            if(configObject == null) configObject = this.factory.get();

            this.instance = reader.apply(configObject);

            LOGGER.info("[EasyJsonConfig({})]: Loaded Config File!", configName);
        } catch (Exception e) {
            LOGGER.error("[EasyJsonConfig({})]: Unable to deserialize the needed config, using default values!", configName, e);

            configObject = factory.get();

            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(gson.toJson(configObject));
            } catch (IOException exception) {
                LOGGER.error("[EasyJsonConfig({})]: Unable to fix the needed config file, using default values!", configName, exception);
            }
        }
    }

    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath("easy_json_config", configName.toString().replace(":", "/"));
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        init();
    }
}
