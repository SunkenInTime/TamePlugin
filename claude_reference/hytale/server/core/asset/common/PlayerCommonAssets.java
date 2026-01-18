package com.hypixel.hytale.server.core.asset.common;

import com.hypixel.hytale.protocol.Asset;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerCommonAssets {
   @Nonnull
   private final Map<String, String> assetMissing = new Object2ObjectOpenHashMap();
   @Nonnull
   private final Map<String, String> assetSent;

   public PlayerCommonAssets(@Nonnull Asset[] requiredAssets) {
      Asset[] var2 = requiredAssets;
      int var3 = requiredAssets.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Asset requiredAsset = var2[var4];
         this.assetMissing.put(requiredAsset.hash, requiredAsset.name);
      }

      this.assetSent = new Object2ObjectOpenHashMap();
   }

   public void sent(@Nullable Asset[] hashes) {
      Set<String> set = new HashSet();
      if (hashes != null) {
         Asset[] var3 = hashes;
         int var4 = hashes.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            Asset hash = var3[var5];
            set.add(hash.hash);
         }
      }

      Iterator iterator = this.assetMissing.keySet().iterator();

      while(iterator.hasNext()) {
         String hash = (String)iterator.next();
         if (set.contains(hash)) {
            iterator.remove();
            set.remove(hash);
         }
      }

      if (!set.isEmpty()) {
         throw new RuntimeException("Still had hashes: " + String.valueOf(set));
      } else {
         this.assetSent.putAll(this.assetMissing);
         this.assetMissing.clear();
      }
   }
}
