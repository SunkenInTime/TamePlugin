package com.example.plugin;


import com.hypixel.hytale.builtin.mounts.*;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerInput;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TameSystem extends RefChangeSystem<EntityStore, NPCMountComponent> {
    private final Query<EntityStore> query = NPCMountComponent.getComponentType();

    @NonNullDecl
    @Override
    public ComponentType<EntityStore, NPCMountComponent> componentType() {
        return NPCMountComponent.getComponentType();
    }


    public void onComponentAdded(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl NPCMountComponent npcMountComponent, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
    Universe.get().sendMessage(Message.raw("We mounted a huge horse chat"));
    }


    public void onComponentSet(@NonNullDecl Ref<EntityStore> ref, @NullableDecl NPCMountComponent npcMountComponent, @NonNullDecl NPCMountComponent t1, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        Universe.get().sendMessage(Message.raw("We mounted a huge horse chat"));
    }


    public void onComponentRemoved(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl NPCMountComponent npcMountComponent, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {

    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return query;
    }
}
