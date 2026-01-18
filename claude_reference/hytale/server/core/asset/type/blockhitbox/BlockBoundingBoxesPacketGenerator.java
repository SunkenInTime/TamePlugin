package com.hypixel.hytale.server.core.asset.type.blockhitbox;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.Hitbox;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateBlockHitboxes;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class BlockBoundingBoxesPacketGenerator extends SimpleAssetPacketGenerator<String, BlockBoundingBoxes, IndexedLookupTableAssetMap<String, BlockBoundingBoxes>> {
   @Nonnull
   public Packet generateInitPacket(@Nonnull IndexedLookupTableAssetMap<String, BlockBoundingBoxes> assetMap, @Nonnull Map<String, BlockBoundingBoxes> assets) {
      UpdateBlockHitboxes packet = new UpdateBlockHitboxes();
      packet.type = UpdateType.Init;
      Map<Integer, Hitbox[]> hitboxes = new Int2ObjectOpenHashMap();
      Iterator var5 = assets.entrySet().iterator();

      while(var5.hasNext()) {
         Entry<String, BlockBoundingBoxes> entry = (Entry)var5.next();
         String key = (String)entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         hitboxes.put(index, ((BlockBoundingBoxes)entry.getValue()).toPacket());
      }

      packet.blockBaseHitboxes = hitboxes;
      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public Packet generateUpdatePacket(@Nonnull IndexedLookupTableAssetMap<String, BlockBoundingBoxes> assetMap, @Nonnull Map<String, BlockBoundingBoxes> loadedAssets) {
      UpdateBlockHitboxes packet = new UpdateBlockHitboxes();
      packet.type = UpdateType.AddOrUpdate;
      Map<Integer, Hitbox[]> hitboxes = new Int2ObjectOpenHashMap();
      Iterator var5 = loadedAssets.entrySet().iterator();

      while(var5.hasNext()) {
         Entry<String, BlockBoundingBoxes> entry = (Entry)var5.next();
         String key = (String)entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         hitboxes.put(index, ((BlockBoundingBoxes)entry.getValue()).toPacket());
      }

      packet.blockBaseHitboxes = hitboxes;
      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public Packet generateRemovePacket(@Nonnull IndexedLookupTableAssetMap<String, BlockBoundingBoxes> assetMap, @Nonnull Set<String> removed) {
      UpdateBlockHitboxes packet = new UpdateBlockHitboxes();
      packet.type = UpdateType.Remove;
      Map<Integer, Hitbox[]> hitboxes = new Int2ObjectOpenHashMap();
      Iterator var5 = removed.iterator();

      while(var5.hasNext()) {
         String key = (String)var5.next();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         hitboxes.put(index, (Object)null);
      }

      packet.blockBaseHitboxes = hitboxes;
      return packet;
   }
}
