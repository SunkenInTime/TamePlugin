package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.codec.codecs.EnumCodec;

public enum BlockPlacementSettings$BlockPreviewVisibility {
   ALWAYS_VISIBLE,
   ALWAYS_HIDDEN,
   DEFAULT;

   public static final EnumCodec<BlockPlacementSettings$BlockPreviewVisibility> CODEC = new EnumCodec(BlockPlacementSettings$BlockPreviewVisibility.class);

   // $FF: synthetic method
   private static BlockPlacementSettings$BlockPreviewVisibility[] $values() {
      return new BlockPlacementSettings$BlockPreviewVisibility[]{ALWAYS_VISIBLE, ALWAYS_HIDDEN, DEFAULT};
   }
}
