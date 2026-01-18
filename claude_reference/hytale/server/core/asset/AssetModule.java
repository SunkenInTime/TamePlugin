package com.hypixel.hytale.server.core.asset;

import com.hypixel.hytale.assetstore.AssetLoadResult;
import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.event.RegisterAssetStoreEvent;
import com.hypixel.hytale.assetstore.event.RemoveAssetStoreEvent;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.HytaleLogger.Api;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.HytaleServerConfig.ModConfig;
import com.hypixel.hytale.server.core.asset.monitor.AssetMonitor;
import com.hypixel.hytale.server.core.asset.type.gameplay.respawn.HomeOrSpawnPoint;
import com.hypixel.hytale.server.core.asset.type.gameplay.respawn.RespawnController;
import com.hypixel.hytale.server.core.asset.type.gameplay.respawn.WorldSpawnPoint;
import com.hypixel.hytale.server.core.asset.type.item.DroplistCommand;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldgen.IWorldGen;
import com.hypixel.hytale.server.core.universe.world.worldgen.ValidatableWorldGen;
import com.hypixel.hytale.server.core.universe.world.worldgen.WorldGenLoadException;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.IWorldMap;
import com.hypixel.hytale.server.core.universe.world.worldmap.provider.IWorldMapProvider;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetModule extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(AssetModule.class).build();
   private static AssetModule instance;
   @Nullable
   private AssetMonitor assetMonitor;
   @Nonnull
   private final List<AssetPack> assetPacks = new CopyOnWriteArrayList();
   private boolean hasLoaded = false;
   private final List<AssetStore<?, ?, ?>> pendingAssetStores = new CopyOnWriteArrayList();

   public static AssetModule get() {
      return instance;
   }

   public AssetModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   protected void setup() {
      if (Options.getOptionSet().has(Options.DISABLE_FILE_WATCHER)) {
         this.getLogger().at(Level.WARNING).log("Not running asset watcher because --disable-file-watcher was set");
      } else {
         try {
            this.assetMonitor = new AssetMonitor();
            this.getLogger().at(Level.INFO).log("Asset monitor enabled!");
         } catch (IOException var4) {
            ((Api)this.getLogger().at(Level.SEVERE).withCause(var4)).log("Failed to create asset monitor!");
         }
      }

      List<Path> paths = Options.getOptionSet().valuesOf(Options.ASSET_DIRECTORY);
      Iterator var2 = paths.iterator();

      Path modsPath;
      while(var2.hasNext()) {
         modsPath = (Path)var2.next();
         this.loadAndRegisterPack(modsPath);
      }

      this.loadPacksFromDirectory(PluginManager.MODS_PATH);
      var2 = Options.getOptionSet().valuesOf(Options.MODS_DIRECTORIES).iterator();

      while(var2.hasNext()) {
         modsPath = (Path)var2.next();
         this.loadPacksFromDirectory(modsPath);
      }

      this.getEventRegistry().register((short)-16, LoadAssetEvent.class, (event) -> {
         if (this.hasLoaded) {
            throw new IllegalStateException("LoadAssetEvent has already been dispatched");
         } else {
            AssetRegistry.ASSET_LOCK.writeLock().lock();

            try {
               this.hasLoaded = true;
               AssetRegistryLoader.preLoadAssets(event);
               Iterator i$ = this.assetPacks.iterator();

               while(i$.hasNext()) {
                  AssetPack pack = (AssetPack)i$.next();
                  AssetRegistryLoader.loadAssets(event, pack);
               }
            } finally {
               AssetRegistry.ASSET_LOCK.writeLock().unlock();
            }

         }
      });
      this.getEventRegistry().register((short)-16, AssetPackRegisterEvent.class, (event) -> {
         AssetRegistryLoader.loadAssets((LoadAssetEvent)null, event.getAssetPack());
      });
      this.getEventRegistry().register(AssetPackUnregisterEvent.class, (event) -> {
         Iterator i$ = AssetRegistry.getStoreMap().values().iterator();

         while(i$.hasNext()) {
            AssetStore<?, ?, ?> assetStore = (AssetStore)i$.next();
            assetStore.removeAssetPack(event.getAssetPack().getName());
         }

      });
      this.getEventRegistry().register(LoadAssetEvent.class, AssetModule::validateWorldGen);
      this.getEventRegistry().register(EventPriority.FIRST, LoadAssetEvent.class, SneakyThrow.sneakyConsumer(AssetRegistryLoader::writeSchemas));
      this.getEventRegistry().register(RegisterAssetStoreEvent.class, this::onNewStore);
      this.getEventRegistry().register(RemoveAssetStoreEvent.class, this::onRemoveStore);
      this.getEventRegistry().registerGlobal(BootEvent.class, (event) -> {
         StringBuilder sb = new StringBuilder("Total Loaded Assets: ");
         AssetStore[] assetStores = (AssetStore[])AssetRegistry.getStoreMap().values().toArray((x$0) -> {
            return new AssetStore[x$0];
         });
         Arrays.sort(assetStores, Comparator.comparingInt((o) -> {
            return o.getAssetMap().getAssetCount();
         }));

         for(int i = assetStores.length - 1; i >= 0; --i) {
            AssetStore assetStore = assetStores[i];
            String simpleName = assetStore.getAssetClass().getSimpleName();
            int assetCount = assetStore.getAssetMap().getAssetCount();
            sb.append(simpleName).append(": ").append(assetCount).append(", ");
         }

         sb.setLength(sb.length() - 2);
         this.getLogger().at(Level.INFO).log(sb.toString());
      });
      RespawnController.CODEC.register("HomeOrSpawnPoint", HomeOrSpawnPoint.class, HomeOrSpawnPoint.CODEC);
      RespawnController.CODEC.register("WorldSpawnPoint", WorldSpawnPoint.class, WorldSpawnPoint.CODEC);
      this.getCommandRegistry().registerCommand(new DroplistCommand());
   }

   protected void shutdown() {
      if (this.assetMonitor != null) {
         this.assetMonitor.shutdown();
         this.assetMonitor = null;
      }

      Iterator var1 = this.assetPacks.iterator();

      while(var1.hasNext()) {
         AssetPack pack = (AssetPack)var1.next();
         if (pack.getFileSystem() != null) {
            try {
               pack.getFileSystem().close();
            } catch (IOException var4) {
               ((Api)this.getLogger().at(Level.WARNING).withCause(var4)).log("Failed to close asset pack filesystem: %s", pack.getName());
            }
         }
      }

      this.assetPacks.clear();
   }

   @Nonnull
   public AssetPack getBaseAssetPack() {
      return (AssetPack)this.assetPacks.getFirst();
   }

   @Nonnull
   public List<AssetPack> getAssetPacks() {
      return this.assetPacks;
   }

   @Nullable
   public AssetMonitor getAssetMonitor() {
      return this.assetMonitor;
   }

   @Nullable
   public AssetPack findAssetPackForPath(Path path) {
      path = path.toAbsolutePath().normalize();
      Iterator var2 = this.assetPacks.iterator();

      AssetPack pack;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         pack = (AssetPack)var2.next();
      } while(!path.startsWith(pack.getRoot()));

      return pack;
   }

   public boolean isAssetPathImmutable(@Nonnull Path path) {
      AssetPack pack = this.findAssetPackForPath(path);
      return pack != null && pack.isImmutable();
   }

   @Nullable
   private PluginManifest loadPackManifest(Path packPath) throws IOException {
      PluginManifest manifest;
      if (packPath.getFileName().toString().toLowerCase().endsWith(".zip")) {
         FileSystem fs = FileSystems.newFileSystem(packPath, (ClassLoader)null);

         PluginManifest var9;
         label96: {
            try {
               Path manifestPath = fs.getPath("manifest.json");
               if (Files.exists(manifestPath, new LinkOption[0])) {
                  BufferedReader reader = Files.newBufferedReader(manifestPath, StandardCharsets.UTF_8);

                  try {
                     char[] buffer = (char[])RawJsonReader.READ_BUFFER.get();
                     RawJsonReader rawJsonReader = new RawJsonReader(reader, buffer);
                     ExtraInfo extraInfo = (ExtraInfo)ExtraInfo.THREAD_LOCAL.get();
                     manifest = (PluginManifest)PluginManifest.CODEC.decodeJson(rawJsonReader, extraInfo);
                     extraInfo.getValidationResults().logOrThrowValidatorExceptions(this.getLogger());
                     var9 = manifest;
                  } catch (Throwable var14) {
                     if (reader != null) {
                        try {
                           reader.close();
                        } catch (Throwable var12) {
                           var14.addSuppressed(var12);
                        }
                     }

                     throw var14;
                  }

                  if (reader != null) {
                     reader.close();
                  }
                  break label96;
               }
            } catch (Throwable var15) {
               if (fs != null) {
                  try {
                     fs.close();
                  } catch (Throwable var11) {
                     var15.addSuppressed(var11);
                  }
               }

               throw var15;
            }

            if (fs != null) {
               fs.close();
            }

            return null;
         }

         if (fs != null) {
            fs.close();
         }

         return var9;
      } else if (Files.isDirectory(packPath, new LinkOption[0])) {
         Path manifestPath = packPath.resolve("manifest.json");
         if (Files.exists(manifestPath, new LinkOption[0])) {
            FileReader reader = new FileReader(manifestPath.toFile(), StandardCharsets.UTF_8);

            try {
               char[] buffer = (char[])RawJsonReader.READ_BUFFER.get();
               RawJsonReader rawJsonReader = new RawJsonReader(reader, buffer);
               ExtraInfo extraInfo = (ExtraInfo)ExtraInfo.THREAD_LOCAL.get();
               PluginManifest manifest = (PluginManifest)PluginManifest.CODEC.decodeJson(rawJsonReader, extraInfo);
               extraInfo.getValidationResults().logOrThrowValidatorExceptions(this.getLogger());
               manifest = manifest;
            } catch (Throwable var13) {
               try {
                  reader.close();
               } catch (Throwable var10) {
                  var13.addSuppressed(var10);
               }

               throw var13;
            }

            reader.close();
            return manifest;
         }
      }

      return null;
   }

   private void loadPacksFromDirectory(Path modsPath) {
      if (Files.isDirectory(modsPath, new LinkOption[0])) {
         this.getLogger().at(Level.INFO).log("Loading packs from directory: %s", modsPath);

         try {
            DirectoryStream stream = Files.newDirectoryStream(modsPath);

            try {
               Iterator var3 = stream.iterator();

               while(var3.hasNext()) {
                  Path packPath = (Path)var3.next();
                  if (packPath.getFileName() != null && !packPath.getFileName().toString().toLowerCase().endsWith(".jar")) {
                     this.loadAndRegisterPack(packPath);
                  }
               }
            } catch (Throwable var6) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (stream != null) {
               stream.close();
            }
         } catch (IOException var7) {
            ((Api)this.getLogger().at(Level.SEVERE).withCause(var7)).log("Failed to load mods from: %s", modsPath);
         }

      }
   }

   private void loadAndRegisterPack(Path packPath) {
      PluginManifest manifest;
      try {
         manifest = this.loadPackManifest(packPath);
         if (manifest == null) {
            this.getLogger().at(Level.WARNING).log("Skipping pack at %s: missing or invalid manifest.json", packPath.getFileName());
            return;
         }
      } catch (Exception var7) {
         ((Api)this.getLogger().at(Level.WARNING).withCause(var7)).log("Failed to load manifest for pack at %s", packPath);
         return;
      }

      PluginIdentifier packIdentifier = new PluginIdentifier(manifest);
      ModConfig modConfig = (ModConfig)HytaleServer.get().getConfig().getModConfig().get(packIdentifier);
      boolean enabled = modConfig == null || modConfig.getEnabled() == null || modConfig.getEnabled();
      String packId = packIdentifier.toString();
      if (enabled) {
         this.registerPack(packId, packPath, manifest);
         this.getLogger().at(Level.INFO).log("Loaded pack: %s from %s", packId, packPath.getFileName());
      } else {
         this.getLogger().at(Level.INFO).log("Skipped disabled pack: %s", packId);
      }

   }

   public void registerPack(@Nonnull String name, @Nonnull Path path, @Nonnull PluginManifest manifest) {
      Path absolutePath = path.toAbsolutePath().normalize();
      FileSystem fileSystem = null;
      boolean isImmutable = false;
      String lowerFileName = absolutePath.getFileName().toString().toLowerCase();
      if (!lowerFileName.endsWith(".zip") && !lowerFileName.endsWith(".jar")) {
         isImmutable = Files.isRegularFile(absolutePath.resolve("CommonAssetsIndex.hashes"), new LinkOption[0]);
      } else {
         try {
            fileSystem = FileSystems.newFileSystem(absolutePath, (ClassLoader)null);
            absolutePath = fileSystem.getPath("").toAbsolutePath().normalize();
            isImmutable = true;
         } catch (IOException var13) {
            throw SneakyThrow.sneakyThrow(var13);
         }
      }

      AssetPack pack = new AssetPack(absolutePath, name, absolutePath, fileSystem, isImmutable, manifest);
      this.assetPacks.add(pack);
      AssetRegistry.ASSET_LOCK.writeLock().lock();

      try {
         if (this.hasLoaded) {
            HytaleServer.get().getEventBus().dispatchFor(AssetPackRegisterEvent.class).dispatch(new AssetPackRegisterEvent(pack));
            return;
         }
      } finally {
         AssetRegistry.ASSET_LOCK.writeLock().unlock();
      }

   }

   public void unregisterPack(@Nonnull String name) {
      AssetPack pack = this.getAssetPack(name);
      if (pack == null) {
         this.getLogger().at(Level.WARNING).log("Tried to unregister non-existent asset pack: %s", name);
      } else {
         this.assetPacks.remove(pack);
         if (pack.getFileSystem() != null) {
            try {
               pack.getFileSystem().close();
            } catch (IOException var8) {
               throw SneakyThrow.sneakyThrow(var8);
            }
         }

         AssetRegistry.ASSET_LOCK.writeLock().lock();

         try {
            HytaleServer.get().getEventBus().dispatchFor(AssetPackUnregisterEvent.class).dispatch(new AssetPackUnregisterEvent(pack));
         } finally {
            AssetRegistry.ASSET_LOCK.writeLock().unlock();
         }

      }
   }

   public AssetPack getAssetPack(@Nonnull String name) {
      Iterator var2 = this.assetPacks.iterator();

      AssetPack pack;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         pack = (AssetPack)var2.next();
      } while(!name.equals(pack.getName()));

      return pack;
   }

   private void onRemoveStore(@Nonnull RemoveAssetStoreEvent event) {
      AssetStore<?, ? extends JsonAssetWithMap<?, ? extends AssetMap<?, ?>>, ? extends AssetMap<?, ? extends JsonAssetWithMap<?, ?>>> assetStore = event.getAssetStore();
      String path = assetStore.getPath();
      if (path != null) {
         Iterator var4 = this.assetPacks.iterator();

         while(var4.hasNext()) {
            AssetPack pack = (AssetPack)var4.next();
            if (!pack.isImmutable()) {
               Path assetsPath = pack.getRoot().resolve("Server").resolve(path);
               if (Files.isDirectory(assetsPath, new LinkOption[0])) {
                  assetStore.removeFileMonitor(assetsPath);
               }
            }
         }

      }
   }

   private void onNewStore(@Nonnull RegisterAssetStoreEvent event) {
      if (AssetRegistry.HAS_INIT) {
         this.pendingAssetStores.add(event.getAssetStore());
      }
   }

   public void initPendingStores() {
      for(int i = 0; i < this.pendingAssetStores.size(); ++i) {
         this.initStore((AssetStore)this.pendingAssetStores.get(i));
      }

      this.pendingAssetStores.clear();
   }

   private void initStore(@Nonnull AssetStore<?, ?, ?> assetStore) {
      AssetRegistry.ASSET_LOCK.writeLock().lock();

      try {
         List<?> preAddedAssets = assetStore.getPreAddedAssets();
         if (preAddedAssets != null && !preAddedAssets.isEmpty()) {
            AssetLoadResult loadResult = assetStore.loadAssets("Hytale:Hytale", preAddedAssets);
            if (loadResult.hasFailed()) {
               throw new RuntimeException("Failed to load asset store: " + String.valueOf(assetStore.getAssetClass()));
            }
         }

         Iterator var14 = this.assetPacks.iterator();

         while(var14.hasNext()) {
            AssetPack pack = (AssetPack)var14.next();
            Path serverAssetDirectory = pack.getRoot().resolve("Server");
            String path = assetStore.getPath();
            Path assetsPath;
            if (path != null) {
               assetsPath = serverAssetDirectory.resolve(path);
               if (Files.isDirectory(assetsPath, new LinkOption[0])) {
                  AssetLoadResult<?, ? extends JsonAssetWithMap<?, ? extends AssetMap<?, ?>>> loadResult = assetStore.loadAssetsFromDirectory(pack.getName(), assetsPath);
                  if (loadResult.hasFailed()) {
                     throw new RuntimeException("Failed to load asset store: " + String.valueOf(assetStore.getAssetClass()));
                  }
               } else {
                  this.getLogger().at(Level.SEVERE).log("Path for %s isn't a directory or doesn't exist: %s", assetStore.getAssetClass().getSimpleName(), assetsPath);
               }
            }

            assetStore.validateCodecDefaults();
            if (path != null) {
               assetsPath = serverAssetDirectory.resolve(path);
               if (Files.isDirectory(assetsPath, new LinkOption[0])) {
                  assetStore.addFileMonitor(pack.getName(), assetsPath);
               }
            }
         }
      } catch (IOException var12) {
         throw SneakyThrow.sneakyThrow(var12);
      } finally {
         AssetRegistry.ASSET_LOCK.writeLock().unlock();
      }

   }

   private static void validateWorldGen(@Nonnull LoadAssetEvent event) {
      if (Options.getOptionSet().has(Options.VALIDATE_WORLD_GEN)) {
         long start = System.nanoTime();

         try {
            IWorldGenProvider provider = (IWorldGenProvider)IWorldGenProvider.CODEC.getDefault();
            IWorldGen generator = provider.getGenerator();
            generator.getDefaultSpawnProvider(0);
            if (generator instanceof ValidatableWorldGen) {
               boolean valid = ((ValidatableWorldGen)generator).validate();
               if (!valid) {
                  event.failed(true, "failed to validate world gen");
               }
            }

            if (generator instanceof IWorldMapProvider) {
               IWorldMapProvider worldMapProvider = (IWorldMapProvider)generator;
               IWorldMap worldMap = worldMapProvider.getGenerator((World)null);
               worldMap.getWorldMapSettings();
            }
         } catch (WorldGenLoadException var7) {
            ((Api)HytaleLogger.getLogger().at(Level.SEVERE).withCause(var7)).log("Failed to load default world gen!");
            HytaleLogger.getLogger().at(Level.SEVERE).log("\n" + var7.getTraceMessage("\n"));
            event.failed(true, "failed to validate world gen: " + var7.getTraceMessage(" -> "));
         } catch (Throwable var8) {
            ((Api)HytaleLogger.getLogger().at(Level.SEVERE).withCause(var8)).log("Failed to load default world gen!");
            event.failed(true, "failed to validate world gen");
         }

         HytaleLogger.getLogger().at(Level.INFO).log("Validate world gen phase completed! Boot time %s, Took %s", FormatUtil.nanosToString(System.nanoTime() - event.getBootStart()), FormatUtil.nanosToString(System.nanoTime() - start));
      }
   }
}
