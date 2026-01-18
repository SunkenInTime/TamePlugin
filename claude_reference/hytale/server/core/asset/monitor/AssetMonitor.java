package com.hypixel.hytale.server.core.asset.monitor;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.HytaleLogger.Api;
import com.hypixel.hytale.logger.sentry.SkipSentryException;
import com.hypixel.hytale.server.core.util.concurrent.ThreadUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class AssetMonitor {
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(ThreadUtil.daemon("AssetMonitor Thread"));
   private final Map<Path, List<AssetMonitorHandler>> directoryMonitors = new ConcurrentHashMap();
   private final Map<Path, FileChangeTask> fileChangeTasks = new ConcurrentHashMap();
   private final Map<Path, Map<AssetMonitorHandler, DirectoryHandlerChangeTask>> directoryHandlerChangeTasks = new ConcurrentHashMap();
   @Nonnull
   private final PathWatcherThread pathWatcherThread = new PathWatcherThread(this::onChange);

   public AssetMonitor() throws IOException {
      this.pathWatcherThread.start();
   }

   public void shutdown() {
      this.pathWatcherThread.shutdown();
   }

   public void monitorDirectoryFiles(@Nonnull Path path, @Nonnull AssetMonitorHandler handler) {
      if (!Files.isDirectory(path, new LinkOption[0])) {
         throw new IllegalArgumentException(String.valueOf(path));
      } else {
         try {
            Path normalize = path.toAbsolutePath().normalize();
            LOGGER.at(Level.FINE).log("Monitoring Directory: %s", normalize);
            ((List)this.directoryMonitors.computeIfAbsent(normalize, SneakyThrow.sneakyFunction((k) -> {
               this.pathWatcherThread.addPath(k);
               return new ObjectArrayList();
            }))).add(handler);
         } catch (Exception var4) {
            ((Api)LOGGER.at(Level.SEVERE).withCause(new SkipSentryException(var4))).log("Failed to monitor directory: %s", path);
         }

      }
   }

   public void removeMonitorDirectoryFiles(@Nonnull Path path, @Nonnull Object key) {
      if (!Files.isDirectory(path, new LinkOption[0])) {
         throw new IllegalArgumentException(String.valueOf(path));
      } else {
         try {
            Path normalize = path.toAbsolutePath().normalize();
            LOGGER.at(Level.FINE).log("Monitoring Directory: %s", normalize);
            ((List)this.directoryMonitors.computeIfAbsent(normalize, SneakyThrow.sneakyFunction((k) -> {
               this.pathWatcherThread.addPath(k);
               return new ObjectArrayList();
            }))).removeIf((v) -> {
               return v.getKey().equals(key);
            });
         } catch (Exception var4) {
            ((Api)LOGGER.at(Level.SEVERE).withCause(new SkipSentryException(var4))).log("Failed to monitor directory: %s", path);
         }

      }
   }

   protected void onChange(@Nonnull Path file, EventKind eventKind) {
      LOGGER.at(Level.FINER).log("onChange: %s of %s", file, eventKind);
      Path path = file.toAbsolutePath().normalize();
      FileChangeTask oldTask = (FileChangeTask)this.fileChangeTasks.remove(path);
      if (oldTask != null) {
         oldTask.cancelSchedule();
      }

      Iterator var5 = this.directoryHandlerChangeTasks.values().iterator();

      while(var5.hasNext()) {
         Map<AssetMonitorHandler, DirectoryHandlerChangeTask> tasks = (Map)var5.next();
         Iterator var7 = tasks.values().iterator();

         while(var7.hasNext()) {
            DirectoryHandlerChangeTask task = (DirectoryHandlerChangeTask)var7.next();
            task.removePath(path);
         }
      }

      boolean createdOrModified = eventKind == EventKind.ENTRY_CREATE || eventKind == EventKind.ENTRY_MODIFY;
      if (createdOrModified && !Files.exists(path, new LinkOption[0])) {
         LOGGER.at(Level.WARNING).log("The asset file '%s' was deleted before we could load/update it!", path);
      } else {
         try {
            this.fileChangeTasks.put(path, new FileChangeTask(this, path, new PathEvent(eventKind, System.nanoTime())));
         } catch (FileNotFoundException | AccessDeniedException | NoSuchFileException var9) {
            LOGGER.at(Level.WARNING).log("The asset file '%s' was deleted before we could load/update it!", path);
         } catch (IOException var10) {
            ((Api)LOGGER.at(Level.SEVERE).withCause(var10)).log("Failed to queue asset to be reloaded %s", path);
         }

      }
   }

   public void onDelayedChange(@Nonnull Path path, @Nonnull PathEvent pathEvent) {
      LOGGER.at(Level.FINER).log("onDelayedChange: %s of %s", path, pathEvent);
      Iterator var3 = this.directoryMonitors.entrySet().iterator();

      while(true) {
         Entry entry;
         Path parent;
         do {
            if (!var3.hasNext()) {
               return;
            }

            entry = (Entry)var3.next();
            parent = (Path)entry.getKey();
         } while(!path.startsWith(parent));

         Map<AssetMonitorHandler, DirectoryHandlerChangeTask> tasks = (Map)this.directoryHandlerChangeTasks.computeIfAbsent(parent, (k) -> {
            return new ConcurrentHashMap();
         });
         Iterator var7 = ((List)entry.getValue()).iterator();

         while(var7.hasNext()) {
            AssetMonitorHandler directoryHandler = (AssetMonitorHandler)var7.next();

            try {
               if (directoryHandler.test(path, pathEvent.getEventKind())) {
                  ((DirectoryHandlerChangeTask)tasks.computeIfAbsent(directoryHandler, (handler) -> {
                     return new DirectoryHandlerChangeTask(this, parent, handler);
                  })).addPath(path, pathEvent);
               }
            } catch (Exception var10) {
               ((Api)LOGGER.at(Level.SEVERE).withCause(var10)).log("Failed to run directoryHandler.test for parent: %s, %s of %s", parent, path, pathEvent);
            }
         }
      }
   }

   public void removeFileChangeTask(@Nonnull FileChangeTask fileChangeTask) {
      this.fileChangeTasks.remove(fileChangeTask.getPath());
   }

   public void markChanged(@Nonnull Path path) {
      Iterator var2 = this.directoryHandlerChangeTasks.entrySet().iterator();

      while(true) {
         Entry entry;
         Path parent;
         do {
            if (!var2.hasNext()) {
               return;
            }

            entry = (Entry)var2.next();
            parent = (Path)entry.getKey();
         } while(!path.startsWith(parent));

         Iterator var5 = ((Map)entry.getValue()).values().iterator();

         while(var5.hasNext()) {
            DirectoryHandlerChangeTask hookChangeTask = (DirectoryHandlerChangeTask)var5.next();
            hookChangeTask.markChanged();
         }
      }
   }

   public void removeHookChangeTask(@Nonnull DirectoryHandlerChangeTask directoryHandlerChangeTask) {
      AssetMonitorHandler hook = directoryHandlerChangeTask.getHandler();
      this.directoryHandlerChangeTasks.compute(directoryHandlerChangeTask.getParent(), (k, map) -> {
         if (map == null) {
            return null;
         } else {
            map.remove(hook);
            return map.isEmpty() ? null : map;
         }
      });
   }

   @Nonnull
   public static ScheduledFuture<?> runTask(@Nonnull Runnable task, long millisDelay) {
      return EXECUTOR.scheduleWithFixedDelay(task, millisDelay, millisDelay, TimeUnit.MILLISECONDS);
   }
}
