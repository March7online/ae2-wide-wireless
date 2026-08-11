package dev.codex.ae2widewireless;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = WideWirelessMod.MOD_ID, dist = Dist.CLIENT)
public final class WideWirelessMod {
    public static final String MOD_ID = "ae2_wide_wireless";

    public WideWirelessMod(IEventBus modEventBus) {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

}
