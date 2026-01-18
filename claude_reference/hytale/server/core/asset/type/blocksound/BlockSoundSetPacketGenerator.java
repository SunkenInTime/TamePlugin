package com.hypixel.hytale.server.core.asset.type.blocksound;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateBlockSoundSets;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.blocksound.config.BlockSoundSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class BlockSoundSetPacketGenerator extends SimpleAssetPacketGenerator<String, BlockSoundSet, IndexedLookupTableAssetMap<String, BlockSoundSet>> {
   @Nonnull
   public Packet generateInitPacket(@Nonnull IndexedLookupTableAssetMap<String, BlockSoundSet> assetMap, @Nonnull Map<String, BlockSoundSet> assets) {
      UpdateBlockSoundSets packet = new UpdateBlockSoundSets();
      packet.type = UpdateType.Init;
      packet.blockSoundSets = new Int2ObjectOpenHashMap();
      Iterator var4 = assets.entrySet().iterator();

      while(var4.hasNext()) {
         Entry<String, BlockSoundSet> entry = (Entry)var4.next();
         String key = (String)entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.blockSoundSets.put(index, ((BlockSoundSet)entry.getValue()).toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public Packet generateUpdatePacket(@Nonnull IndexedLookupTableAssetMap<String, BlockSoundSet> assetMap, @Nonnull Map<String, BlockSoundSet> loadedAssets) {
      UpdateBlockSoundSets packet = new UpdateBlockSoundSets();
      packet.type = UpdateType.AddOrUpdate;
      packet.blockSoundSets = new Int2ObjectOpenHashMap();
      Iterator var4 = loadedAssets.entrySet().iterator();

      while(var4.hasNext()) {
         Entry<String, BlockSoundSet> entry = (Entry)var4.next();
         String key = (String)entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.blockSoundSets.put(index, ((BlockSoundSet)entry.getValue()).toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public Packet generateRemovePacket(@Nonnull IndexedLookupTableAssetMap<String, BlockSoundSet> assetMap, @Nonnull Set<String> removed) {
      UpdateBlockSoundSets packet = new UpdateBlockSoundSets();
      packet.type = UpdateType.Remove;
      packet.blockSoundSets = new Int2ObjectOpenHashMap();
      Iterator var4 = removed.iterator();

      while(var4.hasNext()) {
         String key = (String)var4.next();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.blockSoundSets.put(index, (Object)null);
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
