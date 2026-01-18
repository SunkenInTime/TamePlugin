package com.hypixel.hytale.server.core.asset.type.ambiencefx.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec.Builder;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.protocol.Range;
import com.hypixel.hytale.protocol.Rangeb;
import com.hypixel.hytale.protocol.Rangef;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.asset.type.fluidfx.config.FluidFX;
import com.hypixel.hytale.server.core.asset.type.tagpattern.config.TagPattern;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class AmbienceFXConditions implements NetworkSerializable<com.hypixel.hytale.protocol.AmbienceFXConditions> {
   public static final BuilderCodec<AmbienceFXConditions> CODEC;
   public static final Range DEFAULT_ALTITUDE;
   public static final Rangeb DEFAULT_WALLS;
   public static final Rangeb DEFAULT_LIGHT_LEVEL;
   public static final Rangef DEFAULT_DAY_TIME;
   protected boolean never;
   protected String[] environmentIds;
   protected transient int[] environmentIndices;
   protected String[] weatherIds;
   protected transient int[] weatherIndices;
   protected String environmentTagPattern;
   protected String weatherTagPattern;
   protected String[] fluidFXIds;
   protected transient int[] fluidFXIndices;
   protected AmbienceFXBlockSoundSet[] surroundingBlockSoundSets;
   protected Range altitude;
   protected Rangeb walls;
   protected boolean roof;
   protected String roofMaterialTagPattern;
   protected boolean floor;
   protected Rangeb sunLightLevel;
   protected Rangeb torchLightLevel;
   protected Rangeb globalLightLevel;
   protected Rangef dayTime;

   protected AmbienceFXConditions() {
      this.altitude = DEFAULT_ALTITUDE;
      this.walls = DEFAULT_WALLS;
      this.sunLightLevel = DEFAULT_LIGHT_LEVEL;
      this.torchLightLevel = DEFAULT_LIGHT_LEVEL;
      this.globalLightLevel = DEFAULT_LIGHT_LEVEL;
      this.dayTime = DEFAULT_DAY_TIME;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.AmbienceFXConditions toPacket() {
      com.hypixel.hytale.protocol.AmbienceFXConditions packet = new com.hypixel.hytale.protocol.AmbienceFXConditions();
      packet.never = this.never;
      if (this.environmentIndices != null && this.environmentIndices.length > 0) {
         packet.environmentIndices = this.environmentIndices;
      }

      if (this.environmentTagPattern != null) {
         packet.environmentTagPatternIndex = TagPattern.getAssetMap().getIndex(this.environmentTagPattern);
      } else {
         packet.environmentTagPatternIndex = -1;
      }

      if (this.weatherIndices != null && this.weatherIndices.length > 0) {
         packet.weatherIndices = this.weatherIndices;
      }

      if (this.weatherTagPattern != null) {
         packet.weatherTagPatternIndex = TagPattern.getAssetMap().getIndex(this.weatherTagPattern);
      } else {
         packet.weatherTagPatternIndex = -1;
      }

      if (this.fluidFXIndices != null) {
         packet.fluidFXIndices = this.fluidFXIndices;
      }

      if (this.surroundingBlockSoundSets != null && this.surroundingBlockSoundSets.length > 0) {
         packet.surroundingBlockSoundSets = (com.hypixel.hytale.protocol.AmbienceFXBlockSoundSet[])ArrayUtil.copyAndMutate(this.surroundingBlockSoundSets, AmbienceFXBlockSoundSet::toPacket, (x$0) -> {
            return new com.hypixel.hytale.protocol.AmbienceFXBlockSoundSet[x$0];
         });
      }

      packet.altitude = this.altitude;
      packet.walls = this.walls;
      packet.roof = this.roof;
      if (this.roofMaterialTagPattern != null) {
         packet.roofMaterialTagPatternIndex = TagPattern.getAssetMap().getIndex(this.roofMaterialTagPattern);
      } else {
         packet.roofMaterialTagPatternIndex = -1;
      }

      packet.floor = this.floor;
      packet.sunLightLevel = this.sunLightLevel;
      packet.torchLightLevel = this.torchLightLevel;
      packet.globalLightLevel = this.globalLightLevel;
      packet.dayTime = this.dayTime;
      return packet;
   }

   public boolean isNever() {
      return this.never;
   }

   public String[] getEnvironmentIds() {
      return this.environmentIds;
   }

   public int[] getEnvironmentIndices() {
      return this.environmentIndices;
   }

   public String[] getWeatherIds() {
      return this.weatherIds;
   }

   public int[] getWeatherIndices() {
      return this.weatherIndices;
   }

   public String[] getFluidFXIds() {
      return this.fluidFXIds;
   }

   public int[] getFluidFXIndices() {
      return this.fluidFXIndices;
   }

   public AmbienceFXBlockSoundSet[] getSurroundingBlockSoundSets() {
      return this.surroundingBlockSoundSets;
   }

   public Range getAltitude() {
      return this.altitude;
   }

   public Rangeb getWalls() {
      return this.walls;
   }

   public boolean getRoof() {
      return this.roof;
   }

   public boolean getFloor() {
      return this.floor;
   }

   public Rangeb getSunLightLevel() {
      return this.sunLightLevel;
   }

   public Rangeb getTorchLightLevel() {
      return this.torchLightLevel;
   }

   public Rangeb getGlobalLightLevel() {
      return this.globalLightLevel;
   }

   public Rangef getDayTime() {
      return this.dayTime;
   }

   public boolean isRoof() {
      return this.roof;
   }

   public boolean isFloor() {
      return this.floor;
   }

   protected void processConfig() {
      int i;
      if (this.environmentIds != null) {
         this.environmentIndices = new int[this.environmentIds.length];

         for(i = 0; i < this.environmentIds.length; ++i) {
            this.environmentIndices[i] = Environment.getAssetMap().getIndex(this.environmentIds[i]);
         }
      }

      if (this.weatherIds != null) {
         this.weatherIndices = new int[this.weatherIds.length];

         for(i = 0; i < this.weatherIds.length; ++i) {
            this.weatherIndices[i] = Weather.getAssetMap().getIndex(this.weatherIds[i]);
         }
      }

      if (this.fluidFXIds != null) {
         this.fluidFXIndices = new int[this.fluidFXIds.length];

         for(i = 0; i < this.fluidFXIds.length; ++i) {
            this.fluidFXIndices[i] = FluidFX.getAssetMap().getIndex(this.fluidFXIds[i]);
         }
      }

   }

   @Nonnull
   public String toString() {
      boolean var10000 = this.never;
      return "AmbienceFXConditions{,never=" + var10000 + ",environmentIds=" + Arrays.toString(this.environmentIds) + ", environmentIndices=" + Arrays.toString(this.environmentIndices) + ", environmentTagPattern=" + this.environmentTagPattern + ", weatherIds=" + Arrays.toString(this.weatherIds) + ", weatherIndices=" + Arrays.toString(this.weatherIndices) + ", fluidFXIds=" + Arrays.toString(this.fluidFXIds) + ", fluidFXIndices=" + Arrays.toString(this.fluidFXIndices) + ", surroundingBlockSoundSets=" + Arrays.toString(this.surroundingBlockSoundSets) + ", altitude=" + String.valueOf(this.altitude) + ", walls=" + String.valueOf(this.walls) + ", roof=" + this.roof + ", roofMaterialTagPattern=" + this.roofMaterialTagPattern + ", floor=" + this.floor + ", sunLightLevel=" + String.valueOf(this.sunLightLevel) + ", torchLightLevel=" + String.valueOf(this.torchLightLevel) + ", globalLightLevel=" + String.valueOf(this.globalLightLevel) + ", dayTime=" + String.valueOf(this.dayTime) + "}";
   }

   static {
      CODEC = ((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)BuilderCodec.builder(AmbienceFXConditions.class, AmbienceFXConditions::new).appendInherited(new KeyedCodec("Never", Codec.BOOLEAN), (ambienceFXConditions, l) -> {
         ambienceFXConditions.never = l;
      }, (ambienceFXConditions) -> {
         return ambienceFXConditions.never;
      }, (ambienceFXConditions, parent) -> {
         ambienceFXConditions.never = parent.never;
      }).documentation("If true, this Ambience will never conditionally trigger (but can be set server-side, for example).").add()).appendInherited(new KeyedCodec("EnvironmentIds", Codec.STRING_ARRAY), (ambienceFXConditions, l) -> {
         ambienceFXConditions.environmentIds = l;
      }, (ambienceFXConditions) -> {
         return ambienceFXConditions.environmentIds;
      }, (ambienceFXConditions, parent) -> {
         ambienceFXConditions.environmentIds = parent.environmentIds;
      }).addValidator(Environment.VALIDATOR_CACHE.getArrayValidator()).add()).appendInherited(new KeyedCodec("EnvironmentTagPattern", TagPattern.CHILD_ASSET_CODEC), (ambienceFxConditions, t) -> {
         ambienceFxConditions.environmentTagPattern = t;
      }, (ambienceFXConditions) -> {
         return ambienceFXConditions.environmentTagPattern;
      }, (ambienceFXConditions, parent) -> {
         ambienceFXConditions.environmentTagPattern = parent.environmentTagPattern;
      }).addValidator(TagPattern.VALIDATOR_CACHE.getValidator()).documentation("A tag pattern to use for matching environments.").add()).appendInherited(new KeyedCodec("WeatherTagPattern", TagPattern.CHILD_ASSET_CODEC), (ambienceFxConditions, t) -> {
         ambienceFxConditions.weatherTagPattern = t;
      }, (ambienceFXConditions) -> {
         return ambienceFXConditions.weatherTagPattern;
      }, (ambienceFXConditions, parent) -> {
         ambienceFXConditions.weatherTagPattern = parent.weatherTagPattern;
      }).addValidator(TagPattern.VALIDATOR_CACHE.getValidator()).documentation("A tag pattern to use for matching weathers.").add()).appendInherited(new KeyedCodec("WeatherIds", Codec.STRING_ARRAY), (ambienceFXConditions, l) -> {
         ambienceFXConditions.weatherIds = l;
      }, (ambienceFXConditions) -> {
         return ambienceFXConditions.weatherIds;
      }, (ambienceFXConditions, parent) -> {
         ambienceFXConditions.weatherIds = parent.weatherIds;
      }).addValidator(Weather.VALIDATOR_CACHE.getArrayValidator()).add()).appendInherited(new KeyedCodec("FluidFXIds", Codec.STRING_ARRAY), (ambienceFXConditions, l) -> {
         ambienceFXConditions.fluidFXIds = l;
      }, (ambienceFXConditions) -> {
         return ambienceFXConditions.fluidFXIds;
      }, (ambienceFXConditions, parent) -> {
         ambienceFXConditions.fluidFXIds = parent.fluidFXIds;
      }).addValidator(FluidFX.VALIDATOR_CACHE.getArrayValidator()).add()).appendInherited(new KeyedCodec("SurroundingBlockSoundSets", new ArrayCodec(AmbienceFXBlockSoundSet.CODEC, (x$0) -> {
         return new AmbienceFXBlockSoundSet[x$0];
      })), (ambienceFXConditions, l) -> {
         ambienceFXConditions.surroundingBlockSoundSets = l;
      }, (ambienceFXConditions) -> {
         return ambienceFXConditions.surroundingBlockSoundSets;
      }, (ambienceFXConditions, parent) -> {
         ambienceFXConditions.surroundingBlockSoundSets = parent.surroundingBlockSoundSets;
      }).add()).appendInherited(new KeyedCodec("Altitude", ProtocolCodecs.RANGE), (ambienceFXBlockEnvironment, o) -> {
         ambienceFXBlockEnvironment.altitude = o;
      }, (ambienceFXBlockEnvironment) -> {
         return ambienceFXBlockEnvironment.altitude;
      }, (ambienceFXConditions, parent) -> {
         ambienceFXConditions.altitude = parent.altitude;
      }).add()).appendInherited(new KeyedCodec("Walls", ProtocolCodecs.RANGEB), (ambienceFXBlockEnvironment, o) -> {
         ambienceFXBlockEnvironment.walls = o;
      }, (ambienceFXBlockEnvironment) -> {
         return ambienceFXBlockEnvironment.walls;
      }, (ambienceFXConditions, parent) -> {
         ambienceFXConditions.walls = parent.walls;
      }).add()).appendInherited(new KeyedCodec("Roof", Codec.BOOLEAN), (ambienceFXConditions, aBoolean) -> {
         ambienceFXConditions.roof = aBoolean;
      }, (ambienceFXConditions) -> {
         return ambienceFXConditions.roof;
      }, (ambienceFXConditions, parent) -> {
         ambienceFXConditions.roof = parent.roof;
      }).add()).appendInherited(new KeyedCodec("RoofMaterialTagPattern", TagPattern.CHILD_ASSET_CODEC), (ambienceFxConditions, t) -> {
         ambienceFxConditions.roofMaterialTagPattern = t;
      }, (ambienceFXConditions) -> {
         return ambienceFXConditions.roofMaterialTagPattern;
      }, (ambienceFXConditions, parent) -> {
         ambienceFXConditions.roofMaterialTagPattern = parent.roofMaterialTagPattern;
      }).addValidator(TagPattern.VALIDATOR_CACHE.getValidator()).documentation("A tag pattern to use for matching roof material. If Roof is not required, will only be matched if a roof is present.").add()).appendInherited(new KeyedCodec("Floor", Codec.BOOLEAN), (ambienceFXConditions, aBoolean) -> {
         ambienceFXConditions.floor = aBoolean;
      }, (ambienceFXConditions) -> {
         return ambienceFXConditions.floor;
      }, (ambienceFXConditions, parent) -> {
         ambienceFXConditions.floor = parent.floor;
      }).add()).appendInherited(new KeyedCodec("SunLightLevel", ProtocolCodecs.RANGEB), (ambienceFXBlockEnvironment, o) -> {
         ambienceFXBlockEnvironment.sunLightLevel = o;
      }, (ambienceFXBlockEnvironment) -> {
         return ambienceFXBlockEnvironment.sunLightLevel;
      }, (ambienceFXConditions, parent) -> {
         ambienceFXConditions.sunLightLevel = parent.sunLightLevel;
      }).add()).appendInherited(new KeyedCodec("TorchLightLevel", ProtocolCodecs.RANGEB), (ambienceFXBlockEnvironment, o) -> {
         ambienceFXBlockEnvironment.torchLightLevel = o;
      }, (ambienceFXBlockEnvironment) -> {
         return ambienceFXBlockEnvironment.torchLightLevel;
      }, (ambienceFXConditions, parent) -> {
         ambienceFXConditions.torchLightLevel = parent.torchLightLevel;
      }).add()).appendInherited(new KeyedCodec("GlobalLightLevel", ProtocolCodecs.RANGEB), (ambienceFXBlockEnvironment, o) -> {
         ambienceFXBlockEnvironment.globalLightLevel = o;
      }, (ambienceFXBlockEnvironment) -> {
         return ambienceFXBlockEnvironment.globalLightLevel;
      }, (ambienceFXConditions, parent) -> {
         ambienceFXConditions.globalLightLevel = parent.globalLightLevel;
      }).add()).appendInherited(new KeyedCodec("DayTime", ProtocolCodecs.RANGEF), (ambienceFXBlockEnvironment, o) -> {
         ambienceFXBlockEnvironment.dayTime = o;
      }, (ambienceFXBlockEnvironment) -> {
         return ambienceFXBlockEnvironment.dayTime;
      }, (ambienceFXConditions, parent) -> {
         ambienceFXConditions.dayTime = parent.dayTime;
      }).add()).afterDecode(AmbienceFXConditions::processConfig)).build();
      DEFAULT_ALTITUDE = new Range(0, 512);
      DEFAULT_WALLS = new Rangeb((byte)0, (byte)4);
      DEFAULT_LIGHT_LEVEL = new Rangeb((byte)0, (byte)15);
      DEFAULT_DAY_TIME = new Rangef(0.0F, 24.0F);
   }
}
