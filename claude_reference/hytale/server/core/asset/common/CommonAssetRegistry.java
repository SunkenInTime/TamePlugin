package com.hypixel.hytale.server.core.asset.common;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.common.util.PatternUtil;
import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommonAssetRegistry {
   private static final Map<String, List<CommonAssetRegistry.PackAsset>> assetByNameMap = new ConcurrentHashMap();
   private static final Map<String, List<CommonAssetRegistry.PackAsset>> assetByHashMap = new ConcurrentHashMap();
   private static final AtomicInteger duplicateAssetCount = new AtomicInteger();
   private static final Collection<List<CommonAssetRegistry.PackAsset>> unmodifiableAssetByNameMapValues;

   public static int getDuplicateAssetCount() {
      return duplicateAssetCount.get();
   }

   @Nonnull
   public static Map<String, List<CommonAssetRegistry.PackAsset>> getDuplicatedAssets() {
      Map<String, List<CommonAssetRegistry.PackAsset>> duplicates = new Object2ObjectOpenHashMap();
      Iterator var1 = assetByHashMap.entrySet().iterator();

      while(var1.hasNext()) {
         Entry<String, List<CommonAssetRegistry.PackAsset>> entry = (Entry)var1.next();
         if (((List)entry.getValue()).size() > 1) {
            duplicates.put((String)entry.getKey(), new ObjectArrayList((Collection)entry.getValue()));
         }
      }

      return duplicates;
   }

   @Nonnull
   public static Collection<List<CommonAssetRegistry.PackAsset>> getAllAssets() {
      return unmodifiableAssetByNameMapValues;
   }

   public static void clearAllAssets() {
      assetByNameMap.clear();
      assetByHashMap.clear();
   }

   @Nonnull
   public static CommonAssetRegistry.AddCommonAssetResult addCommonAsset(String pack, @Nonnull CommonAsset asset) {
      CommonAssetRegistry.AddCommonAssetResult result = new CommonAssetRegistry.AddCommonAssetResult();
      result.newPackAsset = new CommonAssetRegistry.PackAsset(pack, asset);
      List<CommonAssetRegistry.PackAsset> list = (List)assetByNameMap.computeIfAbsent(asset.getName(), (v) -> {
         return new CopyOnWriteArrayList();
      });
      boolean added = false;
      boolean addHash = true;

      for(int i = 0; i < list.size(); ++i) {
         CommonAssetRegistry.PackAsset e = (CommonAssetRegistry.PackAsset)list.get(i);
         if (e.pack().equals(pack)) {
            result.previousNameAsset = e;
            if (i == list.size() - 1) {
               ((List)assetByHashMap.get(e.asset.getHash())).remove(e);
               assetByHashMap.compute(e.asset.getHash(), (k, v) -> {
                  return v != null && !v.isEmpty() ? v : null;
               });
            } else {
               addHash = false;
            }

            list.set(i, result.newPackAsset);
            added = true;
            break;
         }
      }

      if (!added) {
         if (!list.isEmpty()) {
            CommonAssetRegistry.PackAsset e = (CommonAssetRegistry.PackAsset)list.getLast();
            ((List)assetByHashMap.get(e.asset.getHash())).remove(e);
            assetByHashMap.compute(e.asset.getHash(), (k, v) -> {
               return v != null && !v.isEmpty() ? v : null;
            });
            result.previousNameAsset = e;
         }

         list.add(result.newPackAsset);
      }

      if (addHash) {
         List<CommonAssetRegistry.PackAsset> commonAssets = (List)assetByHashMap.computeIfAbsent(asset.getHash(), (k) -> {
            return new CopyOnWriteArrayList();
         });
         if (!commonAssets.isEmpty()) {
            result.previousHashAssets = (CommonAssetRegistry.PackAsset[])commonAssets.toArray((x$0) -> {
               return new CommonAssetRegistry.PackAsset[x$0];
            });
         }

         commonAssets.add(result.newPackAsset);
      }

      if (result.previousHashAssets != null || result.previousNameAsset != null) {
         result.duplicateAssetId = duplicateAssetCount.getAndIncrement();
      }

      result.activeAsset = (CommonAssetRegistry.PackAsset)list.getLast();
      return result;
   }

   @Nullable
   public static BooleanObjectPair<CommonAssetRegistry.PackAsset> removeCommonAssetByName(String pack, String name) {
      name = PatternUtil.replaceBackslashWithForwardSlash(name);
      List<CommonAssetRegistry.PackAsset> oldAssets = (List)assetByNameMap.get(name);
      if (oldAssets == null) {
         return null;
      } else {
         CommonAssetRegistry.PackAsset previousCurrent = (CommonAssetRegistry.PackAsset)oldAssets.getLast();
         oldAssets.removeIf((v) -> {
            return v.pack().equals(pack);
         });
         assetByNameMap.compute(name, (k, v) -> {
            return v != null && !v.isEmpty() ? v : null;
         });
         if (oldAssets.isEmpty()) {
            removeCommonAssetByHash0(previousCurrent);
            return BooleanObjectPair.of(false, previousCurrent);
         } else {
            CommonAssetRegistry.PackAsset newCurrent = (CommonAssetRegistry.PackAsset)oldAssets.getLast();
            if (newCurrent.equals(previousCurrent)) {
               return null;
            } else {
               removeCommonAssetByHash0(previousCurrent);
               ((List)assetByHashMap.computeIfAbsent(newCurrent.asset.getHash(), (v) -> {
                  return new CopyOnWriteArrayList();
               })).add(newCurrent);
               return BooleanObjectPair.of(true, newCurrent);
            }
         }
      }
   }

   @Nonnull
   public static List<CommonAsset> getCommonAssetsStartingWith(String pack, String name) {
      List<CommonAsset> oldAssets = new ObjectArrayList();
      Iterator var3 = assetByNameMap.values().iterator();

      while(var3.hasNext()) {
         List<CommonAssetRegistry.PackAsset> assets = (List)var3.next();
         Iterator var5 = assets.iterator();

         while(var5.hasNext()) {
            CommonAssetRegistry.PackAsset asset = (CommonAssetRegistry.PackAsset)var5.next();
            if (asset.asset().getName().startsWith(name) && asset.pack().equals(pack)) {
               oldAssets.add(asset.asset());
            }
         }
      }

      return oldAssets;
   }

   public static boolean hasCommonAsset(String name) {
      return assetByNameMap.containsKey(name);
   }

   public static boolean hasCommonAsset(AssetPack pack, String name) {
      List<CommonAssetRegistry.PackAsset> packAssets = (List)assetByNameMap.get(name);
      if (packAssets != null) {
         Iterator var3 = packAssets.iterator();

         while(var3.hasNext()) {
            CommonAssetRegistry.PackAsset packAsset = (CommonAssetRegistry.PackAsset)var3.next();
            if (packAsset.pack.equals(pack.getName())) {
               return true;
            }
         }
      }

      return false;
   }

   @Nullable
   public static CommonAsset getByName(String name) {
      name = PatternUtil.replaceBackslashWithForwardSlash(name);
      List<CommonAssetRegistry.PackAsset> asset = (List)assetByNameMap.get(name);
      return asset == null ? null : ((CommonAssetRegistry.PackAsset)asset.getLast()).asset();
   }

   @Nullable
   public static CommonAsset getByHash(@Nonnull String hash) {
      List<CommonAssetRegistry.PackAsset> assets = (List)assetByHashMap.get(hash.toLowerCase());
      return assets != null && !assets.isEmpty() ? ((CommonAssetRegistry.PackAsset)assets.getFirst()).asset() : null;
   }

   private static void removeCommonAssetByHash0(@Nonnull CommonAssetRegistry.PackAsset oldAsset) {
      List<CommonAssetRegistry.PackAsset> commonAssets = (List)assetByHashMap.get(oldAsset.asset().getHash());
      if (commonAssets != null && commonAssets.remove(oldAsset) && commonAssets.isEmpty()) {
         assetByHashMap.compute(oldAsset.asset().getHash(), (key, assets) -> {
            return assets != null && !assets.isEmpty() ? assets : null;
         });
      }

   }

   static {
      unmodifiableAssetByNameMapValues = Collections.unmodifiableCollection(assetByNameMap.values());
   }

   public static class AddCommonAssetResult {
      private CommonAssetRegistry.PackAsset newPackAsset;
      private CommonAssetRegistry.PackAsset previousNameAsset;
      private CommonAssetRegistry.PackAsset activeAsset;
      private CommonAssetRegistry.PackAsset[] previousHashAssets;
      private int duplicateAssetId;

      public CommonAssetRegistry.PackAsset getNewPackAsset() {
         return this.newPackAsset;
      }

      public CommonAssetRegistry.PackAsset getPreviousNameAsset() {
         return this.previousNameAsset;
      }

      public CommonAssetRegistry.PackAsset getActiveAsset() {
         return this.activeAsset;
      }

      public CommonAssetRegistry.PackAsset[] getPreviousHashAssets() {
         return this.previousHashAssets;
      }

      public int getDuplicateAssetId() {
         return this.duplicateAssetId;
      }

      @Nonnull
      public String toString() {
         String var10000 = String.valueOf(this.previousNameAsset);
         return "AddCommonAssetResult{previousNameAsset=" + var10000 + ", previousHashAssets=" + Arrays.toString(this.previousHashAssets) + ", duplicateAssetId=" + this.duplicateAssetId + "}";
      }
   }

   public static record PackAsset(String pack, CommonAsset asset) {
      public PackAsset(String pack, CommonAsset asset) {
         this.pack = pack;
         this.asset = asset;
      }

      public boolean equals(@Nullable Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            CommonAssetRegistry.PackAsset packAsset = (CommonAssetRegistry.PackAsset)o;
            return !this.pack.equals(packAsset.pack) ? false : this.asset.equals(packAsset.asset);
         } else {
            return false;
         }
      }

      @Nonnull
      public String toString() {
         String var10000 = this.pack;
         return "PackAsset{pack='" + var10000 + "', asset=" + String.valueOf(this.asset) + "}";
      }

      public String pack() {
         return this.pack;
      }

      public CommonAsset asset() {
         return this.asset;
      }
   }
}
