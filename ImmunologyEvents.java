package dev.krysztal.immunology;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.List;

@Mod.EventBusSubscriber(modid = Immunology.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ImmunologyEvents {
    private static final String ROOT = "ImmunologyPort";
    private static final String VALUE = "Value";
    private static final String FOOD_TICKS = "FoodTicks";
    private static final String INSANITY_TICKS = "InsanityTicks";
    private static final String DETERIORATION_TICKS = "DeteriorationTicks";
    private static final String UNRECOVERABLE_TICKS = "UnrecoverableTicks";
    private static final String UNRECOVERABLE = "Unrecoverable";
    private static final int MAX = 100;
    private ImmunologyEvents() {}

    private static CompoundTag data(Player p) {
        CompoundTag persistent = p.getPersistentData();
        if (!persistent.contains(ROOT)) {
            CompoundTag fresh = new CompoundTag();
            fresh.putInt(VALUE, MAX);
            persistent.put(ROOT, fresh);
        }
        return persistent.getCompound(ROOT);
    }
    public static int getImmunity(Player p) { return data(p).getInt(VALUE); }
    public static void setImmunity(Player p, int value) {
        data(p).putInt(VALUE, Math.max(0, Math.min(MAX, value)));
    }
    private static void change(Player p, int amount) { setImmunity(p, getImmunity(p) + amount); }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone e) {
        CompoundTag old = e.getOriginal().getPersistentData();
        if (old.contains(ROOT)) e.getEntity().getPersistentData().put(ROOT, old.getCompound(ROOT).copy());
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer p)
                || p.level().isClientSide || p.isCreative() || p.isSpectator()) return;
        CompoundTag d = data(p);

        // Somnia replacement: sleeping naturally restores immunity.
        if (p.isSleeping()) change(p, 1);

        // Diet replacement: vanilla hunger is used, so Diet is not required.
        int foodTicks = d.getInt(FOOD_TICKS);
        if (p.getFoodData().getFoodLevel() < 5) {
            if (++foodTicks >= 80) { change(p, -5); foodTicks = 0; }
        } else foodTicks = 0;
        d.putInt(FOOD_TICKS, foodTicks);

        // Small chance to restore immunity while actively fishing.
        if (p.fishing != null && p.fishing.isInWater() && p.getRandom().nextInt(1000) < 5) {
            change(p, 5);
        }

        int immunity = getImmunity(p);
        if (immunity >= 25 && immunity < 50) {
            if (!p.hasEffect(MobEffects.WEAKNESS))
                p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1200, 0, false, true, true));
            if (!p.hasEffect(MobEffects.MINING_FATIGUE))
                p.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 1200, 0, false, true, true));
        }
        if (immunity > 0 && immunity < 25) {
            p.addEffect(new MobEffectInstance(ModEffects.INSANITY.get(), 1200, 0, false, false, true));
            int insanity = d.getInt(INSANITY_TICKS) + 1;
            if (insanity >= 2400) {
                insanity = 0;
                List<String> lines = List.of("§cWhere am I...?", "§cI hear something nearby...",
                        "§cThis place feels wrong...", "§cI need to get out of here...");
                p.displayClientMessage(Component.literal(lines.get(p.getRandom().nextInt(lines.size()))), false);
            }
            d.putInt(INSANITY_TICKS, insanity);
        } else d.putInt(INSANITY_TICKS, 0);

        if (immunity <= 0) {
            p.addEffect(new MobEffectInstance(ModEffects.DETERIORATION.get(), 1200, 0, false, false, true));
            int deterioration = d.getInt(DETERIORATION_TICKS) + 1;
            d.putInt(DETERIORATION_TICKS, deterioration);
            if (deterioration >= 600) d.putBoolean(UNRECOVERABLE, true);
            if (d.getBoolean(UNRECOVERABLE)) {
                int timer = d.getInt(UNRECOVERABLE_TICKS) + 1;
                d.putInt(UNRECOVERABLE_TICKS, timer);
                if (timer >= 600) {
                    d.putInt(UNRECOVERABLE_TICKS, 0);
                    d.putBoolean(UNRECOVERABLE, false);
                    p.hurt(p.damageSources().magic(), Float.MAX_VALUE);
                }
            }
        } else if (immunity >= 20) {
            d.putInt(DETERIORATION_TICKS, 0);
            d.putInt(UNRECOVERABLE_TICKS, 0);
            d.putBoolean(UNRECOVERABLE, false);
            p.removeEffect(ModEffects.DETERIORATION.get());
        }

        if (p.tickCount % 20 == 0) {
            int value = getImmunity(p);
            int bars = Math.round(value / 5.0f);
            StringBuilder bar = new StringBuilder("§8[");
            for (int i = 0; i < 20; i++)
                bar.append(i < bars ? (value <= 25 ? "§c|" : value <= 50 ? "§e|" : "§a|") : "§7·");
            bar.append("§8] §f").append(value).append("%");
            if (d.getBoolean(UNRECOVERABLE)) bar.append(" §4UNRECOVERABLE");
            p.displayClientMessage(Component.literal(bar.toString()), true);
        }
    }

    @SubscribeEvent
    public static void onFoodEaten(LivingEntityUseItemEvent.Finish e) {
        if (!(e.getEntity() instanceof Player p) || p.level().isClientSide) return;
        ItemStack s = e.getItem();
        if (s.is(net.minecraft.world.item.Items.GOLDEN_APPLE)
                || s.is(net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE)
                || s.is(net.minecraft.world.item.Items.COOKED_BEEF)
                || s.is(net.minecraft.world.item.Items.COOKED_PORKCHOP)
                || s.is(net.minecraft.world.item.Items.COOKED_CHICKEN)
                || s.is(net.minecraft.world.item.Items.COOKED_MUTTON)
                || s.is(net.minecraft.world.item.Items.COOKED_RABBIT)
                || s.is(net.minecraft.world.item.Items.BAKED_POTATO)
                || s.is(net.minecraft.world.item.Items.CARROT)
                || s.is(net.minecraft.world.item.Items.APPLE)
                || s.is(net.minecraft.world.item.Items.BREAD)) change(p, 5);
        else if (s.is(net.minecraft.world.item.Items.ROTTEN_FLESH)
                || s.is(net.minecraft.world.item.Items.SPIDER_EYE)
                || s.is(net.minecraft.world.item.Items.POISONOUS_POTATO)
                || s.is(net.minecraft.world.item.Items.RAW_CHICKEN)
                || s.is(net.minecraft.world.item.Items.PUFFERFISH)) change(p, -5);
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingDamageEvent e) {
        if (e.getEntity() instanceof Player p && !p.level().isClientSide) change(p, -5);
    }

    @SubscribeEvent
    public static void onAnimalTamed(AnimalTameEvent e) {
        if (e.getTamer() != null && !e.getTamer().level().isClientSide) change(e.getTamer(), 5);
    }
}
