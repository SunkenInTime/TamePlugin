package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.codec.codecs.EnumCodec;

public enum BlockPlacementSettings$RotationMode {
   FACING_PLAYER,
   BLOCK_NORMAL,
   STAIR_FACING_PLAYER,
   DEFAULT;

   public static final EnumCodec<BlockPlacementSettings$RotationMode> CODEC = new EnumCodec(BlockPlacementSettings$RotationMode.class);

   // $FF: synthetic method
   private static BlockPlacementSettings$RotationMode[] $values() {
      return new BlockPlacementSettings$RotationMode[]{FACING_PLAYER, BLOCK_NORMAL, STAIR_FACING_PLAYER, DEFAULT};
   }
}
