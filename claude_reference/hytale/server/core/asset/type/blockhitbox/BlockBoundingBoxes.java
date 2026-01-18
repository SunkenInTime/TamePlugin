package com.hypixel.hytale.server.core.asset.type.blockhitbox;

import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.AssetExtraInfo.Data;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec.Builder;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.protocol.Hitbox;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.io.NetworkSerializers;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockBoundingBoxes implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, BlockBoundingBoxes>>, NetworkSerializable<Hitbox[]> {
   private static final int ROTATIONS;
   public static final int DEFAULT_ID = 0;
   public static final String DEFAULT = "Full";
   public static final BlockBoundingBoxes UNIT_BOX;
   public static final double UNIT_BOX_MAXIMUM_EXTENT;
   public static final AssetBuilderCodec<String, BlockBoundingBoxes> CODEC;
   public static final ValidatorCache<String> VALIDATOR_CACHE;
   public static final Hitbox[] EMPTY_HITBOXES;
   private static AssetStore<String, BlockBoundingBoxes, IndexedLookupTableAssetMap<String, BlockBoundingBoxes>> ASSET_STORE;
   protected Data data;
   protected String id;
   protected Box[] baseDetailBoxes;
   @Nonnull
   private final transient BlockBoundingBoxes.RotatedVariantBoxes[] variants;
   protected transient boolean protrudesUnitBox;

   public static AssetStore<String, BlockBoundingBoxes, IndexedLookupTableAssetMap<String, BlockBoundingBoxes>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(BlockBoundingBoxes.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, BlockBoundingBoxes> getAssetMap() {
      return (IndexedLookupTableAssetMap)getAssetStore().getAssetMap();
   }

   public BlockBoundingBoxes() {
      this.variants = new BlockBoundingBoxes.RotatedVariantBoxes[ROTATIONS];
   }

   public BlockBoundingBoxes(String id, double min, double max) {
      this.variants = new BlockBoundingBoxes.RotatedVariantBoxes[ROTATIONS];
      this.id = id;
      this.baseDetailBoxes = new Box[]{(new Box()).setMinMax(min, max)};
      this.processConfig();
   }

   public String getId() {
      return this.id;
   }

   public boolean protrudesUnitBox() {
      return this.protrudesUnitBox;
   }

   protected void processConfig() {
      if (this.baseDetailBoxes != null) {
         this.protrudesUnitBox = false;
         RotationTuple[] var1 = RotationTuple.VALUES;
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            RotationTuple tuple = var1[var3];
            Rotation yaw = tuple.yaw();
            Rotation pitch = tuple.pitch();
            Rotation roll = tuple.roll();
            BlockBoundingBoxes.RotatedVariantBoxes variant = getRotated(this, yaw, pitch, roll);
            this.variants[RotationTuple.index(yaw, pitch, roll)] = variant;
            this.protrudesUnitBox |= variant.boundingBox.min.x < 0.0D || variant.boundingBox.max.x > 1.0D || variant.boundingBox.min.y < 0.0D || variant.boundingBox.max.y > 1.0D || variant.boundingBox.min.z < 0.0D || variant.boundingBox.max.z > 1.0D;
         }

      }
   }

   public BlockBoundingBoxes.RotatedVariantBoxes get(@Nonnull Rotation yaw, @Nonnull Rotation pitch, @Nonnull Rotation roll) {
      return this.variants[RotationTuple.index(yaw, pitch, roll)];
   }

   public BlockBoundingBoxes.RotatedVariantBoxes get(int index) {
      return this.variants[index];
   }

   public Hitbox[] toPacket() {
      if (this.baseDetailBoxes == null) {
         return EMPTY_HITBOXES;
      } else {
         Hitbox[] arr = new Hitbox[this.baseDetailBoxes.length];

         for(int i = 0; i < this.baseDetailBoxes.length; ++i) {
            arr[i] = (Hitbox)NetworkSerializers.BOX.toPacket(this.baseDetailBoxes[i]);
         }

         return arr;
      }
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.data);
      return "BlockBoundingBoxes{data=" + var10000 + ", id='" + this.id + "', baseDetailBoxes=" + Arrays.toString(this.baseDetailBoxes) + ", variants=" + Arrays.toString(this.variants) + ", protrudesUnitBox=" + this.protrudesUnitBox + "}";
   }

   @Nonnull
   public static BlockBoundingBoxes getUnitBoxFor(String id) {
      return new BlockBoundingBoxes(id, 0.0D, 1.0D);
   }

   @Nonnull
   private static BlockBoundingBoxes.RotatedVariantBoxes getRotated(@Nonnull BlockBoundingBoxes boxes, @Nonnull Rotation rotationYaw, @Nonnull Rotation rotationPitch, @Nonnull Rotation rotationRoll) {
      Box[] detailBoxes = boxes.baseDetailBoxes;
      Box[] newDetailBoxes = new Box[detailBoxes.length];

      for(int i = 0; i < detailBoxes.length; ++i) {
         newDetailBoxes[i] = rotate(new Box(detailBoxes[i]), rotationYaw, rotationPitch, rotationRoll);
      }

      return new BlockBoundingBoxes.RotatedVariantBoxes(newDetailBoxes);
   }

   @Nonnull
   protected static Box rotate(@Nonnull Box box, @Nonnull Rotation rotationYaw, @Nonnull Rotation rotationPitch, @Nonnull Rotation rotationRoll) {
      switch(rotationRoll) {
      case None:
      default:
         break;
      case Ninety:
         rotate90Z(box);
         break;
      case OneEighty:
         rotate180Z(box);
         break;
      case TwoSeventy:
         rotate270Z(box);
      }

      switch(rotationPitch) {
      case None:
      default:
         break;
      case Ninety:
         rotate90X(box);
         break;
      case OneEighty:
         rotate180X(box);
         break;
      case TwoSeventy:
         rotate270X(box);
      }

      switch(rotationYaw) {
      case None:
      default:
         break;
      case Ninety:
         rotate90Y(box);
         break;
      case OneEighty:
         rotate180Y(box);
         break;
      case TwoSeventy:
         rotate270Y(box);
      }

      box.normalize();
      return box;
   }

   private static void rotate90X(@Nonnull Box box) {
      double t = box.min.z;
      box.min.z = box.min.y;
      box.min.y = 1.0D - t;
      t = box.max.z;
      box.max.z = box.max.y;
      box.max.y = 1.0D - t;
   }

   private static void rotate180X(@Nonnull Box box) {
      box.min.z = 1.0D - box.min.z;
      box.min.y = 1.0D - box.min.y;
      box.max.z = 1.0D - box.max.z;
      box.max.y = 1.0D - box.max.y;
   }

   private static void rotate270X(@Nonnull Box box) {
      double t = box.min.z;
      box.min.z = 1.0D - box.min.y;
      box.min.y = t;
      t = box.max.z;
      box.max.z = 1.0D - box.max.y;
      box.max.y = t;
   }

   private static void rotate90Y(@Nonnull Box box) {
      double t = box.min.x;
      box.min.x = box.min.z;
      box.min.z = 1.0D - t;
      t = box.max.x;
      box.max.x = box.max.z;
      box.max.z = 1.0D - t;
   }

   private static void rotate180Y(@Nonnull Box box) {
      box.min.x = 1.0D - box.min.x;
      box.min.z = 1.0D - box.min.z;
      box.max.x = 1.0D - box.max.x;
      box.max.z = 1.0D - box.max.z;
   }

   private static void rotate270Y(@Nonnull Box box) {
      double t = box.min.x;
      box.min.x = 1.0D - box.min.z;
      box.min.z = t;
      t = box.max.x;
      box.max.x = 1.0D - box.max.z;
      box.max.z = t;
   }

   private static void rotate90Z(@Nonnull Box box) {
      double t = box.min.x;
      box.min.x = box.min.y;
      box.min.y = 1.0D - t;
      t = box.max.x;
      box.max.x = box.max.y;
      box.max.y = 1.0D - t;
   }

   private static void rotate180Z(@Nonnull Box box) {
      box.min.x = 1.0D - box.min.x;
      box.min.y = 1.0D - box.min.y;
      box.max.x = 1.0D - box.max.x;
      box.max.y = 1.0D - box.max.y;
   }

   private static void rotate270Z(@Nonnull Box box) {
      double t = box.min.x;
      box.min.x = 1.0D - box.min.y;
      box.min.y = t;
      t = box.max.x;
      box.max.x = 1.0D - box.max.y;
      box.max.y = t;
   }

   static {
      ROTATIONS = Rotation.VALUES.length * Rotation.VALUES.length * Rotation.VALUES.length;
      UNIT_BOX = new BlockBoundingBoxes("Full", 0.0D, 1.0D);
      UNIT_BOX_MAXIMUM_EXTENT = UNIT_BOX.variants[0].getBoundingBox().getMaximumExtent();
      CODEC = ((Builder)((Builder)AssetBuilderCodec.builder(BlockBoundingBoxes.class, BlockBoundingBoxes::new, Codec.STRING, (blockBoundingBoxes, s) -> {
         blockBoundingBoxes.id = s;
      }, (blockBoundingBoxes) -> {
         return blockBoundingBoxes.id;
      }, (asset, data) -> {
         asset.data = data;
      }, (asset) -> {
         return asset.data;
      }).appendInherited(new KeyedCodec("Boxes", new ArrayCodec(Box.CODEC, (x$0) -> {
         return new Box[x$0];
      }), true), (blockBoundingBoxes, boxes) -> {
         blockBoundingBoxes.baseDetailBoxes = boxes;
      }, (blockBoundingBoxes) -> {
         return blockBoundingBoxes.baseDetailBoxes;
      }, (blockBoundingBoxes, parent) -> {
         blockBoundingBoxes.baseDetailBoxes = parent.baseDetailBoxes;
      }).addValidator(Validators.nonEmptyArray()).add()).afterDecode(BlockBoundingBoxes::processConfig)).build();
      VALIDATOR_CACHE = new ValidatorCache(new AssetKeyValidator(BlockBoundingBoxes::getAssetStore));
      EMPTY_HITBOXES = new Hitbox[0];
   }

   public static class RotatedVariantBoxes {
      protected Box boundingBox;
      protected Box[] detailBoxes;

      public RotatedVariantBoxes(@Nullable Box[] boxes) {
         if (boxes == null || boxes.length == 0) {
            boxes = new Box[]{(new Box()).setMinMax(0.0D, 1.0D)};
         }

         if (boxes.length == 1) {
            this.boundingBox = boxes[0];
         } else {
            Box box = this.boundingBox = new Box();

            for(int i = 0; i < boxes.length; ++i) {
               box.union(boxes[i]);
            }
         }

         this.detailBoxes = boxes;
      }

      @Nonnull
      public Box getBoundingBox() {
         return this.boundingBox;
      }

      @Nonnull
      public Box[] getDetailBoxes() {
         return this.detailBoxes;
      }

      public boolean hasDetailBoxes() {
         return this.detailBoxes.length > 1;
      }

      public boolean containsPosition(double x, double y, double z) {
         if (this.hasDetailBoxes()) {
            Box[] var7 = this.detailBoxes;
            int var8 = var7.length;

            for(int var9 = 0; var9 < var8; ++var9) {
               Box detailBox = var7[var9];
               if (detailBox.containsPosition(x, y, z)) {
                  return true;
               }
            }

            return false;
         } else {
            return this.boundingBox.containsPosition(x, y, z);
         }
      }
   }
}
