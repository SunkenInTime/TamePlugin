package com.hypixel.hytale.server.core.asset.type.ambiencefx;

import com.hypixel.hytale.assetstore.map.IndexedAssetMap;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateAmbienceFX;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.ambiencefx.config.AmbienceFX;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class AmbienceFXPacketGenerator extends SimpleAssetPacketGenerator<String, AmbienceFX, IndexedAssetMap<String, AmbienceFX>> {
   @Nonnull
   public Packet generateInitPacket(@Nonnull IndexedAssetMap<String, AmbienceFX> assetMap, @Nonnull Map<String, AmbienceFX> assets) {
      UpdateAmbienceFX packet = new UpdateAmbienceFX();
      packet.type = UpdateType.Init;
      packet.ambienceFX = new Object2ObjectOpenHashMap();
      Iterator var4 = assets.entrySet().iterator();

      while(var4.hasNext()) {
         Entry<String, AmbienceFX> entry = (Entry)var4.next();
         String key = (String)entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.ambienceFX.put(index, ((AmbienceFX)entry.getValue()).toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public Packet generateUpdatePacket(@Nonnull IndexedAssetMap<String, AmbienceFX> assetMap, @Nonnull Map<String, AmbienceFX> loadedAssets) {
      UpdateAmbienceFX packet = new UpdateAmbienceFX();
      packet.type = UpdateType.AddOrUpdate;
      packet.ambienceFX = new Object2ObjectOpenHashMap();
      Iterator var4 = loadedAssets.entrySet().iterator();

      while(var4.hasNext()) {
         Entry<String, AmbienceFX> entry = (Entry)var4.next();
         String key = (String)entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.ambienceFX.put(index, ((AmbienceFX)entry.getValue()).toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public Packet generateRemovePacket(@Nonnull IndexedAssetMap<String, AmbienceFX> assetMap, @Nonnull Set<String> removed) {
      UpdateAmbienceFX packet = new UpdateAmbienceFX();
      packet.type = UpdateType.Remove;
      packet.ambienceFX = new Object2ObjectOpenHashMap();
      Iterator var4 = removed.iterator();

      while(var4.hasNext()) {
         String key = (String)var4.next();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.ambienceFX.put(index, (Object)null);
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
