package com.example.plugin;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class HelloPlugin extends JavaPlugin {

    public HelloPlugin(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    protected void setup(){
        this.getCommandRegistry().registerCommand(new HelloCommand("hello",
                "a command for gooners",
                false));

        this.getEntityStoreRegistry().registerSystem(new TameSystem());

        super.setup();
    }
}
