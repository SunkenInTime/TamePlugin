package com.hypixel.hytale.server.core.asset.type.audiocategory;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateAudioCategories;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.audiocategory.config.AudioCategory;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class AudioCategoryPacketGenerator extends SimpleAssetPacketGenerator<String, AudioCategory, IndexedLookupTableAssetMap<String, AudioCategory>> {
   @Nonnull
   public Packet generateInitPacket(@Nonnull IndexedLookupTableAssetMap<String, AudioCategory> assetMap, @Nonnull Map<String, AudioCategory> assets) {
      UpdateAudioCategories packet = new UpdateAudioCategories();
      packet.type = UpdateType.Init;
      packet.categories = new Int2ObjectOpenHashMap(assets.size());
      Iterator var4 = assets.entrySet().iterator();

      while(var4.hasNext()) {
         Entry<String, AudioCategory> entry = (Entry)var4.next();
         String key = (String)entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.categories.put(index, ((AudioCategory)entry.getValue()).toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public Packet generateUpdatePacket(@Nonnull IndexedLookupTableAssetMap<String, AudioCategory> assetMap, @Nonnull Map<String, AudioCategory> loadedAssets) {
      UpdateAudioCategories packet = new UpdateAudioCategories();
      packet.type = UpdateType.AddOrUpdate;
      packet.categories = new Int2ObjectOpenHashMap(loadedAssets.size());
      Iterator var4 = loadedAssets.entrySet().iterator();

      while(var4.hasNext()) {
         Entry<String, AudioCategory> entry = (Entry)var4.next();
         String key = (String)entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.categories.put(index, ((AudioCategory)entry.getValue()).toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public Packet generateRemovePacket(@Nonnull IndexedLookupTableAssetMap<String, AudioCategory> assetMap, @Nonnull Set<String> removed) {
      UpdateAudioCategories packet = new UpdateAudioCategories();
      packet.type = UpdateType.Remove;
      packet.categories = new Int2ObjectOpenHashMap(removed.size());
      Iterator var4 = removed.iterator();

      while(var4.hasNext()) {
         String key = (String)var4.next();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.categories.put(index, (Object)null);
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
