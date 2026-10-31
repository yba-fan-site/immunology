package dev.krysztal.immunology;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {
    private ModEffects() {}
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Immunology.MOD_ID);
    public static final RegistryObject<MobEffect> DETERIORATION = EFFECTS.register(
            "deterioration", () -> new MobEffect(MobEffectCategory.HARMFUL, 0x4A5A38) {});
    public static final RegistryObject<MobEffect> INSANITY = EFFECTS.register(
            "insanity", () -> new MobEffect(MobEffectCategory.HARMFUL, 0x7A4B9B) {});
    public static void register(IEventBus bus) { EFFECTS.register(bus); }
}
