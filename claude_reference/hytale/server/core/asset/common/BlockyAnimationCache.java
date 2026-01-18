package com.hypixel.hytale.server.core.asset.common;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec.Builder;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockyAnimationCache {
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final Map<String, BlockyAnimationCache.BlockyAnimation> animations = new ConcurrentHashMap();

   @Nonnull
   public static CompletableFuture<BlockyAnimationCache.BlockyAnimation> get(String name) {
      BlockyAnimationCache.BlockyAnimation animationData = (BlockyAnimationCache.BlockyAnimation)animations.get(name);
      if (animationData != null) {
         return CompletableFuture.completedFuture(animationData);
      } else {
         CommonAsset asset = CommonAssetRegistry.getByName(name);
         return asset == null ? CompletableFuture.completedFuture((Object)null) : get0(asset);
      }
   }

   @Nonnull
   public static CompletableFuture<BlockyAnimationCache.BlockyAnimation> get(@Nonnull CommonAsset asset) {
      BlockyAnimationCache.BlockyAnimation animationData = (BlockyAnimationCache.BlockyAnimation)animations.get(asset.getName());
      return animationData != null ? CompletableFuture.completedFuture(animationData) : get0(asset);
   }

   @Nullable
   public static BlockyAnimationCache.BlockyAnimation getNow(String name) {
      BlockyAnimationCache.BlockyAnimation animationData = (BlockyAnimationCache.BlockyAnimation)animations.get(name);
      if (animationData != null) {
         return animationData;
      } else {
         CommonAsset asset = CommonAssetRegistry.getByName(name);
         return asset == null ? null : (BlockyAnimationCache.BlockyAnimation)get0(asset).join();
      }
   }

   public static BlockyAnimationCache.BlockyAnimation getNow(@Nonnull CommonAsset asset) {
      BlockyAnimationCache.BlockyAnimation animationData = (BlockyAnimationCache.BlockyAnimation)animations.get(asset.getName());
      return animationData != null ? animationData : (BlockyAnimationCache.BlockyAnimation)get0(asset).join();
   }

   @Nonnull
   private static CompletableFuture<BlockyAnimationCache.BlockyAnimation> get0(@Nonnull CommonAsset asset) {
      String name = asset.getName();
      return CompletableFutureUtil._catch(asset.getBlob().thenApply((bytes) -> {
         String str = new String(bytes, StandardCharsets.UTF_8);
         RawJsonReader reader = RawJsonReader.fromJsonString(str);

         try {
            ExtraInfo extraInfo = (ExtraInfo)ExtraInfo.THREAD_LOCAL.get();
            BlockyAnimationCache.BlockyAnimation newAnimationData = (BlockyAnimationCache.BlockyAnimation)BlockyAnimationCache.BlockyAnimation.CODEC.decodeJson(reader, extraInfo);
            extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER);
            animations.put(name, newAnimationData);
            return newAnimationData;
         } catch (IOException var6) {
            throw SneakyThrow.sneakyThrow(var6);
         }
      }));
   }

   public static void invalidate(String name) {
      animations.remove(name);
   }

   public static class BlockyAnimation {
      public static final BuilderCodec<BlockyAnimationCache.BlockyAnimation> CODEC;
      public static final double FRAMES_PER_SECOND = 60.0D;
      private int duration;

      public int getDurationFrames() {
         return this.duration;
      }

      public double getDurationMillis() {
         return (double)this.duration * 1000.0D / 60.0D;
      }

      public double getDurationSeconds() {
         return (double)this.duration / 60.0D;
      }

      @Nonnull
      public String toString() {
         return "BlockyAnimation{duration=" + this.duration + "}";
      }

      static {
         CODEC = ((Builder)BuilderCodec.builder(BlockyAnimationCache.BlockyAnimation.class, BlockyAnimationCache.BlockyAnimation::new).addField(new KeyedCodec("duration", Codec.INTEGER, true, true), (blockyAnimation, i) -> {
            blockyAnimation.duration = i;
         }, (blockyAnimation) -> {
            return blockyAnimation.duration;
         })).build();
      }
   }
}
