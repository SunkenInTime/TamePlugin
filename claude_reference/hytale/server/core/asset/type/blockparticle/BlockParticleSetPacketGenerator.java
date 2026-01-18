package com.hypixel.hytale.server.core.asset.type.blockparticle;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateBlockParticleSets;
import com.hypixel.hytale.server.core.asset.packet.DefaultAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.blockparticle.config.BlockParticleSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class BlockParticleSetPacketGenerator extends DefaultAssetPacketGenerator<String, BlockParticleSet> {
   @Nonnull
   public Packet generateInitPacket(DefaultAssetMap<String, BlockParticleSet> assetMap, @Nonnull Map<String, BlockParticleSet> assets) {
      UpdateBlockParticleSets packet = new UpdateBlockParticleSets();
      packet.type = UpdateType.Init;
      packet.blockParticleSets = new Object2ObjectOpenHashMap();
      Iterator var4 = assets.entrySet().iterator();

      while(var4.hasNext()) {
         Entry<String, BlockParticleSet> entry = (Entry)var4.next();
         packet.blockParticleSets.put((String)entry.getKey(), ((BlockParticleSet)entry.getValue()).toPacket());
      }

      return packet;
   }

   @Nonnull
   public Packet generateUpdatePacket(@Nonnull Map<String, BlockParticleSet> loadedAssets) {
      UpdateBlockParticleSets packet = new UpdateBlockParticleSets();
      packet.type = UpdateType.AddOrUpdate;
      packet.blockParticleSets = new Object2ObjectOpenHashMap();
      Iterator var3 = loadedAssets.entrySet().iterator();

      while(var3.hasNext()) {
         Entry<String, BlockParticleSet> entry = (Entry)var3.next();
         packet.blockParticleSets.put((String)entry.getKey(), ((BlockParticleSet)entry.getValue()).toPacket());
      }

      return packet;
   }

   @Nonnull
   public Packet generateRemovePacket(@Nonnull Set<String> removed) {
      UpdateBlockParticleSets packet = new UpdateBlockParticleSets();
      packet.type = UpdateType.Remove;
      packet.blockParticleSets = new Object2ObjectOpenHashMap();
      Iterator var3 = removed.iterator();

      while(var3.hasNext()) {
         String key = (String)var3.next();
         packet.blockParticleSets.put(key, (Object)null);
      }

      return packet;
   }
}
