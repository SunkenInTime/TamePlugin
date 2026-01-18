package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockNeighbor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum BlockFace {
   UP(BlockFace.FaceConnectionType.FLIP, BlockNeighbor.Up, Vector3i.UP),
   DOWN(BlockFace.FaceConnectionType.FLIP, BlockNeighbor.Down, Vector3i.DOWN),
   NORTH(BlockFace.FaceConnectionType.FLIP, BlockNeighbor.North, Vector3i.NORTH),
   EAST(BlockFace.FaceConnectionType.FLIP, BlockNeighbor.East, Vector3i.EAST),
   SOUTH(BlockFace.FaceConnectionType.FLIP, BlockNeighbor.South, Vector3i.SOUTH),
   WEST(BlockFace.FaceConnectionType.FLIP, BlockNeighbor.West, Vector3i.WEST),
   UP_NORTH(BlockFace.FaceConnectionType.ROTATE_X, BlockNeighbor.UpNorth, new BlockFace[]{UP, NORTH}),
   UP_SOUTH(BlockFace.FaceConnectionType.ROTATE_X, BlockNeighbor.UpSouth, new BlockFace[]{UP, SOUTH}),
   UP_EAST(BlockFace.FaceConnectionType.ROTATE_Z, BlockNeighbor.UpEast, new BlockFace[]{UP, EAST}),
   UP_WEST(BlockFace.FaceConnectionType.ROTATE_Z, BlockNeighbor.UpWest, new BlockFace[]{UP, WEST}),
   DOWN_NORTH(BlockFace.FaceConnectionType.ROTATE_X, BlockNeighbor.DownNorth, new BlockFace[]{DOWN, NORTH}),
   DOWN_SOUTH(BlockFace.FaceConnectionType.ROTATE_X, BlockNeighbor.DownSouth, new BlockFace[]{DOWN, SOUTH}),
   DOWN_EAST(BlockFace.FaceConnectionType.ROTATE_Z, BlockNeighbor.DownEast, new BlockFace[]{DOWN, EAST}),
   DOWN_WEST(BlockFace.FaceConnectionType.ROTATE_Z, BlockNeighbor.DownWest, new BlockFace[]{DOWN, WEST}),
   NORTH_EAST(BlockFace.FaceConnectionType.ROTATE_Y, BlockNeighbor.NorthEast, new BlockFace[]{NORTH, EAST}),
   SOUTH_EAST(BlockFace.FaceConnectionType.ROTATE_Y, BlockNeighbor.SouthEast, new BlockFace[]{SOUTH, EAST}),
   SOUTH_WEST(BlockFace.FaceConnectionType.ROTATE_Y, BlockNeighbor.SouthWest, new BlockFace[]{SOUTH, WEST}),
   NORTH_WEST(BlockFace.FaceConnectionType.ROTATE_Y, BlockNeighbor.NorthWest, new BlockFace[]{NORTH, WEST}),
   UP_NORTH_EAST(BlockFace.FaceConnectionType.ROTATE_ALL, BlockNeighbor.UpNorthEast, new BlockFace[]{UP, NORTH, EAST}),
   UP_SOUTH_EAST(BlockFace.FaceConnectionType.ROTATE_ALL, BlockNeighbor.UpSouthEast, new BlockFace[]{UP, SOUTH, EAST}),
   UP_SOUTH_WEST(BlockFace.FaceConnectionType.ROTATE_ALL, BlockNeighbor.UpSouthWest, new BlockFace[]{UP, SOUTH, WEST}),
   UP_NORTH_WEST(BlockFace.FaceConnectionType.ROTATE_ALL, BlockNeighbor.UpNorthWest, new BlockFace[]{UP, NORTH, WEST}),
   DOWN_NORTH_EAST(BlockFace.FaceConnectionType.ROTATE_ALL, BlockNeighbor.DownNorthEast, new BlockFace[]{DOWN, NORTH, EAST}),
   DOWN_SOUTH_EAST(BlockFace.FaceConnectionType.ROTATE_ALL, BlockNeighbor.DownSouthEast, new BlockFace[]{DOWN, SOUTH, EAST}),
   DOWN_SOUTH_WEST(BlockFace.FaceConnectionType.ROTATE_ALL, BlockNeighbor.DownSouthWest, new BlockFace[]{DOWN, SOUTH, WEST}),
   DOWN_NORTH_WEST(BlockFace.FaceConnectionType.ROTATE_ALL, BlockNeighbor.DownNorthWest, new BlockFace[]{DOWN, NORTH, WEST});

   public static final EnumCodec<BlockFace> CODEC = new EnumCodec(BlockFace.class);
   public static final BlockFace[] VALUES = values();
   @Nonnull
   private static final Map<Vector3i, BlockFace> DIRECTION_MAP = new Object2ObjectOpenHashMap();
   private final BlockFace.FaceConnectionType faceConnectionType;
   @Nonnull
   private final BlockFace[] components;
   private final Vector3i direction;
   private final BlockNeighbor blockNeighbor;
   private BlockFace[] connectingFaces;
   private Vector3i[] connectingFaceOffsets;

   private BlockFace(BlockFace.FaceConnectionType param3, BlockNeighbor param4, Vector3i param5) {
      this.faceConnectionType = faceConnectionType;
      this.direction = direction;
      this.blockNeighbor = blockNeighbor;
      this.components = new BlockFace[0];
   }

   private BlockFace(BlockFace.FaceConnectionType param3, BlockNeighbor param4, @Nonnull BlockFace... param5) {
      this.faceConnectionType = faceConnectionType;
      this.components = components;
      BlockFace[] var6 = components;
      int var7 = components.length;

      int var8;
      BlockFace component;
      for(var8 = 0; var8 < var7; ++var8) {
         component = var6[var8];
         if (component.components.length > 0) {
            throw new IllegalArgumentException("Only the base BlockFace's can be used as components to make other block faces");
         }
      }

      this.direction = new Vector3i();
      var6 = components;
      var7 = components.length;

      for(var8 = 0; var8 < var7; ++var8) {
         component = var6[var8];
         this.direction.add(component.direction);
      }

      this.blockNeighbor = blockNeighbor;
   }

   public BlockFace.FaceConnectionType getFaceConnectionType() {
      return this.faceConnectionType;
   }

   @Nonnull
   public BlockFace[] getComponents() {
      return this.components;
   }

   public Vector3i getDirection() {
      return this.direction;
   }

   public BlockFace[] getConnectingFaces() {
      return this.connectingFaces;
   }

   public Vector3i[] getConnectingFaceOffsets() {
      return this.connectingFaceOffsets;
   }

   @Nonnull
   private BlockFace[] getConnectingFaces0() {
      BlockFace[] blockFaces;
      switch(this.faceConnectionType.ordinal()) {
      case 0:
         return new BlockFace[]{flip(this)};
      case 1:
         blockFaces = new BlockFace[]{rotate(this, Rotation.Ninety, Rotation.None, Rotation.None), rotate(this, Rotation.OneEighty, Rotation.None, Rotation.None), rotate(this, Rotation.TwoSeventy, Rotation.None, Rotation.None)};
         return blockFaces;
      case 2:
         blockFaces = new BlockFace[]{rotate(this, Rotation.None, Rotation.Ninety, Rotation.None), rotate(this, Rotation.None, Rotation.OneEighty, Rotation.None), rotate(this, Rotation.None, Rotation.TwoSeventy, Rotation.None)};
         return blockFaces;
      case 3:
         blockFaces = new BlockFace[]{rotate(this, Rotation.None, Rotation.None, Rotation.Ninety), rotate(this, Rotation.None, Rotation.None, Rotation.OneEighty), rotate(this, Rotation.None, Rotation.None, Rotation.TwoSeventy)};
         return blockFaces;
      case 4:
         blockFaces = new BlockFace[]{rotate(this, Rotation.Ninety, Rotation.None, Rotation.None), rotate(this, Rotation.OneEighty, Rotation.None, Rotation.None), rotate(this, Rotation.None, Rotation.Ninety, Rotation.None), rotate(this, Rotation.None, Rotation.OneEighty, Rotation.None), rotate(this, Rotation.None, Rotation.TwoSeventy, Rotation.None), rotate(this, Rotation.None, Rotation.None, Rotation.OneEighty), flip(this)};
         return blockFaces;
      default:
         throw new IllegalArgumentException("Unknown FaceConnectionType " + String.valueOf(this.faceConnectionType));
      }
   }

   @Nonnull
   private Vector3i directionTo(@Nonnull BlockFace connectingFace) {
      Vector3i vector3i = new Vector3i();
      if (this.direction.getX() == -connectingFace.direction.getX()) {
         vector3i.setX(this.direction.getX());
      }

      if (this.direction.getY() == -connectingFace.direction.getY()) {
         vector3i.setY(this.direction.getY());
      }

      if (this.direction.getZ() == -connectingFace.direction.getZ()) {
         vector3i.setZ(this.direction.getZ());
      }

      return vector3i;
   }

   public static BlockFace lookup(Vector3i direction) {
      return (BlockFace)DIRECTION_MAP.get(direction);
   }

   public static BlockFace rotate(@Nonnull BlockFace blockFace, @Nonnull Rotation rotationYaw, @Nonnull Rotation rotationPitch) {
      Vector3i rotate = Rotation.rotate(blockFace.direction, rotationYaw, rotationPitch);
      return lookup(rotate);
   }

   public static BlockFace rotate(@Nonnull BlockFace blockFace, @Nonnull Rotation rotationX, @Nonnull Rotation rotationY, @Nonnull Rotation rotationZ) {
      Vector3i rotate = Rotation.rotate(blockFace.direction, rotationX, rotationY, rotationZ);
      return lookup(rotate);
   }

   public static BlockFace flip(@Nonnull BlockFace blockFace) {
      Vector3i flipped = blockFace.direction.clone().scale(-1);
      return lookup(flipped);
   }

   public BlockNeighbor toProtocolBlockNeighbor() {
      return this.blockNeighbor;
   }

   @Nullable
   public static BlockFace fromProtocolFace(@Nonnull com.hypixel.hytale.protocol.BlockFace face) {
      BlockFace var10000;
      switch(face) {
      case Up:
         var10000 = UP;
         break;
      case Down:
         var10000 = DOWN;
         break;
      case North:
         var10000 = NORTH;
         break;
      case South:
         var10000 = SOUTH;
         break;
      case East:
         var10000 = EAST;
         break;
      case West:
         var10000 = WEST;
         break;
      case None:
         var10000 = null;
         break;
      default:
         throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   @Nonnull
   public static com.hypixel.hytale.protocol.BlockFace toProtocolFace(@Nullable BlockFace face) {
      if (face == null) {
         return com.hypixel.hytale.protocol.BlockFace.None;
      } else {
         com.hypixel.hytale.protocol.BlockFace var10000;
         switch(face.ordinal()) {
         case 0:
            var10000 = com.hypixel.hytale.protocol.BlockFace.Up;
            break;
         case 1:
            var10000 = com.hypixel.hytale.protocol.BlockFace.Down;
            break;
         case 2:
            var10000 = com.hypixel.hytale.protocol.BlockFace.North;
            break;
         case 3:
            var10000 = com.hypixel.hytale.protocol.BlockFace.East;
            break;
         case 4:
            var10000 = com.hypixel.hytale.protocol.BlockFace.South;
            break;
         case 5:
            var10000 = com.hypixel.hytale.protocol.BlockFace.West;
            break;
         default:
            throw new IllegalArgumentException("Invalid BlockFace");
         }

         return var10000;
      }
   }

   // $FF: synthetic method
   private static BlockFace[] $values() {
      return new BlockFace[]{UP, DOWN, NORTH, EAST, SOUTH, WEST, UP_NORTH, UP_SOUTH, UP_EAST, UP_WEST, DOWN_NORTH, DOWN_SOUTH, DOWN_EAST, DOWN_WEST, NORTH_EAST, SOUTH_EAST, SOUTH_WEST, NORTH_WEST, UP_NORTH_EAST, UP_SOUTH_EAST, UP_SOUTH_WEST, UP_NORTH_WEST, DOWN_NORTH_EAST, DOWN_SOUTH_EAST, DOWN_SOUTH_WEST, DOWN_NORTH_WEST};
   }

   static {
      BlockFace[] var0 = VALUES;
      int var1 = var0.length;

      int var2;
      BlockFace blockFace;
      for(var2 = 0; var2 < var1; ++var2) {
         blockFace = var0[var2];
         DIRECTION_MAP.put(blockFace.direction, blockFace);
      }

      var0 = VALUES;
      var1 = var0.length;

      for(var2 = 0; var2 < var1; ++var2) {
         blockFace = var0[var2];
         blockFace.connectingFaces = blockFace.getConnectingFaces0();
         blockFace.connectingFaceOffsets = new Vector3i[blockFace.connectingFaces.length];

         for(int i = 0; i < blockFace.connectingFaces.length; ++i) {
            blockFace.connectingFaceOffsets[i] = blockFace.directionTo(blockFace.connectingFaces[i]);
         }
      }

   }

   static enum FaceConnectionType {
      FLIP,
      ROTATE_X,
      ROTATE_Y,
      ROTATE_Z,
      ROTATE_ALL;

      // $FF: synthetic method
      private static BlockFace.FaceConnectionType[] $values() {
         return new BlockFace.FaceConnectionType[]{FLIP, ROTATE_X, ROTATE_Y, ROTATE_Z, ROTATE_ALL};
      }
   }
}
