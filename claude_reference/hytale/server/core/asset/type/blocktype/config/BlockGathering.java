package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec.Builder;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.schema.metadata.AllowEmptyObject;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;

public class BlockGathering implements NetworkSerializable<com.hypixel.hytale.protocol.BlockGathering> {
   public static final BuilderCodec<BlockGathering> CODEC;
   protected BlockBreakingDropType breaking;
   protected HarvestingDropType harvest;
   protected SoftBlockDropType soft;
   protected PhysicsDropType physics;
   protected BlockGathering.BlockToolData[] toolDataRaw;
   @Nonnull
   protected Map<String, BlockGathering.BlockToolData> toolData = Collections.emptyMap();
   protected boolean useDefaultDropWhenPlaced = false;

   protected BlockGathering() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.BlockGathering toPacket() {
      com.hypixel.hytale.protocol.BlockGathering packet = new com.hypixel.hytale.protocol.BlockGathering();
      if (this.breaking != null) {
         packet.breaking = this.breaking.toPacket();
      }

      if (this.harvest != null) {
         packet.harvest = this.harvest.toPacket();
      }

      if (this.soft != null) {
         packet.soft = this.soft.toPacket();
      }

      return packet;
   }

   public BlockBreakingDropType getBreaking() {
      return this.breaking;
   }

   public HarvestingDropType getHarvest() {
      return this.harvest;
   }

   public SoftBlockDropType getSoft() {
      return this.soft;
   }

   public boolean isHarvestable() {
      return this.harvest != null;
   }

   public boolean isSoft() {
      return this.soft != null;
   }

   public PhysicsDropType getPhysics() {
      return this.physics;
   }

   public boolean shouldUseDefaultDropWhenPlaced() {
      return this.useDefaultDropWhenPlaced;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.breaking);
      return "BlockGathering{breaking=" + var10000 + ", harvest=" + String.valueOf(this.harvest) + ", harvest=" + String.valueOf(this.soft) + "}";
   }

   @Nonnull
   public Map<String, BlockGathering.BlockToolData> getToolData() {
      return this.toolData;
   }

   static {
      CODEC = ((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)BuilderCodec.builder(BlockGathering.class, BlockGathering::new).append(new KeyedCodec("Breaking", BlockBreakingDropType.CODEC), (blockGathering, o) -> {
         blockGathering.breaking = o;
      }, (blockGathering) -> {
         return blockGathering.breaking;
      }).metadata(AllowEmptyObject.INSTANCE).add()).append(new KeyedCodec("Harvest", HarvestingDropType.CODEC), (blockGathering, o) -> {
         blockGathering.harvest = o;
      }, (blockGathering) -> {
         return blockGathering.harvest;
      }).metadata(AllowEmptyObject.INSTANCE).add()).append(new KeyedCodec("Soft", SoftBlockDropType.CODEC), (blockGathering, o) -> {
         blockGathering.soft = o;
      }, (blockGathering) -> {
         return blockGathering.soft;
      }).metadata(AllowEmptyObject.INSTANCE).add()).append(new KeyedCodec("Physics", PhysicsDropType.CODEC), (blockGathering, o) -> {
         blockGathering.physics = o;
      }, (blockGathering) -> {
         return blockGathering.physics;
      }).metadata(AllowEmptyObject.INSTANCE).add()).append(new KeyedCodec("Tools", new ArrayCodec(BlockGathering.BlockToolData.CODEC, (x$0) -> {
         return new BlockGathering.BlockToolData[x$0];
      })), (blockGathering, o) -> {
         blockGathering.toolDataRaw = o;
      }, (blockGathering) -> {
         return blockGathering.toolDataRaw;
      }).metadata(AllowEmptyObject.INSTANCE).add()).appendInherited(new KeyedCodec("UseDefaultDropWhenPlaced", Codec.BOOLEAN), (o, v) -> {
         o.useDefaultDropWhenPlaced = v;
      }, (o) -> {
         return o.useDefaultDropWhenPlaced;
      }, (o, p) -> {
         o.useDefaultDropWhenPlaced = p.useDefaultDropWhenPlaced;
      }).documentation("If this is set then player placed blocks will use the default drop behaviour instead of using the droplists.").add()).afterDecode((g) -> {
         if (g.toolDataRaw != null) {
            g.toolData = new Object2ObjectOpenHashMap();
            BlockGathering.BlockToolData[] arr$ = g.toolDataRaw;
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               BlockGathering.BlockToolData t = arr$[i$];
               g.toolData.put(t.getTypeId(), t);
            }
         }

      })).build();
   }

   public static class BlockToolData {
      public static final BuilderCodec<BlockGathering.BlockToolData> CODEC;
      protected String typeId;
      protected String stateId;
      protected String itemId;
      protected String dropListId;

      public String getTypeId() {
         return this.typeId;
      }

      public String getStateId() {
         return this.stateId;
      }

      public String getItemId() {
         return this.itemId;
      }

      public String getDropListId() {
         return this.dropListId;
      }

      static {
         CODEC = ((Builder)((Builder)((Builder)((Builder)BuilderCodec.builder(BlockGathering.BlockToolData.class, BlockGathering.BlockToolData::new).append(new KeyedCodec("Type", Codec.STRING), (toolData, o) -> {
            toolData.typeId = o;
         }, (toolData) -> {
            return toolData.typeId;
         }).metadata(AllowEmptyObject.INSTANCE).add()).append(new KeyedCodec("State", Codec.STRING), (toolData, o) -> {
            toolData.stateId = o;
         }, (toolData) -> {
            return toolData.stateId;
         }).metadata(AllowEmptyObject.INSTANCE).add()).append(new KeyedCodec("ItemId", Codec.STRING), (toolData, s) -> {
            toolData.itemId = s;
         }, (toolData) -> {
            return toolData.itemId;
         }).addValidatorLate(() -> {
            return Item.VALIDATOR_CACHE.getValidator().late();
         }).add()).append(new KeyedCodec("DropList", new ContainedAssetCodec(ItemDropList.class, ItemDropList.CODEC)), (toolData, s) -> {
            toolData.dropListId = s;
         }, (toolData) -> {
            return toolData.dropListId;
         }).addValidatorLate(() -> {
            return ItemDropList.VALIDATOR_CACHE.getValidator().late();
         }).add()).build();
      }
   }
}
