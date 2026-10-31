package dev.krysztal.immunology;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Immunology.MOD_ID)
public class Immunology {
    public static final String MOD_ID = "immunology";

    public Immunology() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEffects.register(modBus);
        // ImmunologyEvents is registered automatically through @Mod.EventBusSubscriber.
    }
}
