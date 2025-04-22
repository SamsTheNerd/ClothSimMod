package com.samsthenerd.drapery;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public final class DraperyMod {
    public static final String MOD_ID = "drapery";

    public static Identifier modLoc(String path){
        return Identifier.of(MOD_ID, path);
    }

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, RegistryKeys.ITEM);


    public static Supplier<Item> DRAPERY_DEBUG_STICK = item("debugneedle", DebugNeedleItem::new);

    /*
     * Helper function for registering an item.
     * register the item supplied with the id `drapery:name`
     */
    public static <T extends Item> RegistrySupplier<T> item(String name, Supplier<T> item){
        Identifier itemId = Identifier.of(MOD_ID, name);
        return ITEMS.register(itemId, item);
    }

    public static void init() {
        ITEMS.register();
        // Write common init code here.
    }
}
