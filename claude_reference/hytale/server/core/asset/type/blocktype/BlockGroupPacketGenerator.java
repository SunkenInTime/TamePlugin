package com.hypixel.hytale.server.core.asset.type.blocktype;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateBlockGroups;
import com.hypixel.hytale.server.core.asset.packet.DefaultAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.item.config.BlockGroup;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockGroupPacketGenerator extends DefaultAssetPacketGenerator<String, BlockGroup> {
   @Nonnull
   public Packet generateInitPacket(@Nonnull DefaultAssetMap<String, BlockGroup> assetMap, Map<String, BlockGroup> assets) {
      UpdateBlockGroups packet = new UpdateBlockGroups();
      packet.type = UpdateType.Init;
      packet.groups = (Map)assetMap.getAssetMap().entrySet().stream().collect(Collectors.toMap(Entry::getKey, (entry) -> {
         return ((BlockGroup)entry.getValue()).toPacket();
      }));
      return packet;
   }

   @Nonnull
   public Packet generateUpdatePacket(@Nonnull Map<String, BlockGroup> loadedAssets) {
      UpdateBlockGroups packet = new UpdateBlockGroups();
      packet.type = UpdateType.AddOrUpdate;
      packet.groups = (Map)loadedAssets.entrySet().stream().collect(Collectors.toMap(Entry::getKey, (entry) -> {
         return ((BlockGroup)entry.getValue()).toPacket();
      }));
      return packet;
   }

   @Nullable
   public Packet generateRemovePacket(@Nonnull Set<String> removed) {
      UpdateBlockGroups packet = new UpdateBlockGroups();
      packet.type = UpdateType.Remove;
      packet.groups = new Object2ObjectOpenHashMap();
      Iterator var3 = removed.iterator();

      while(var3.hasNext()) {
         String string = (String)var3.next();
         packet.groups.put(string, (Object)null);
      }

      return null;
   }
}
