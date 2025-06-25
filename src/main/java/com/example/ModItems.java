package com.example;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.function.Function;

public class ModItems
{
    public static Item register(String name, Function<Item.Settings,Item> itemFactory,Item.Settings settings)
    {
        //Create the item key
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MyTestMod.MOD_ID,name));

        Item item = itemFactory.apply(settings);

        Registry.register(Registries.ITEM,itemKey,item);

        return item;
    }
    public static class LightningStick extends Item {
        public LightningStick(Settings settings) {
            super(settings);
        }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            if (world.isClient) {
                return new TypedActionResult<>(ActionResult.PASS, user.getStackInHand(hand));
            }

            // 获取玩家视线方向的目标位置
            Vec3d eyePos = user.getEyePos();
            Vec3d lookVec = user.getRotationVec(1.0F);
            double maxDistance = 15.0; // 最大检测距离
            Vec3d endPos = eyePos.add(lookVec.multiply(maxDistance));

            // 执行射线检测，寻找玩家指向的方块
            BlockHitResult hitResult = world.raycast(new RaycastContext(
                    eyePos,
                    endPos,
                    RaycastContext.ShapeType.OUTLINE,
                    RaycastContext.FluidHandling.NONE,
                    user
            ));

            Vec3d spawnPos;
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                // 如果命中方块，在方块位置上方召唤闪电
                spawnPos = Vec3d.of(hitResult.getBlockPos().up());
            } else {
                // 如果没有命中方块（距离超过15格），在视线终点位置召唤闪电
                spawnPos = endPos;
            }

            // 生成闪电
            LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
            lightning.setPosition(spawnPos);
            world.spawnEntity(lightning);
            world.playSound(null, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(),
                    SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 1.0F, 1.0F);
            return new TypedActionResult<>(ActionResult.SUCCESS, user.getStackInHand(hand));
        }
    }
    public static class CrimsonBurstFruit extends Item
    {
        public CrimsonBurstFruit(Settings settings) {
            super(settings);
        }
        @Override
        public ItemStack finishUsing(ItemStack itemStack, World world, LivingEntity entity)
        {
            Vec3d pos = entity.getPos();
            if(!world.isClient && entity.isPlayer() ) {

                world.getOtherEntities(entity, new Box(
                                pos.x - 3, pos.y - 3, pos.z - 3,
                                pos.x + 3, pos.y + 3, pos.z + 3))
                        .forEach(e -> {
                            if (e instanceof LivingEntity && e != entity) {
                                e.damage(world.getDamageSources().explosion(entity, e), 5f);
                            }
                            Vec3d knockback = e.getPos().subtract(pos).normalize();
                            e.addVelocity(knockback.x * 0.5, 0.2, knockback.z * 0.5);
                        });
                PlayerEntity player = (PlayerEntity)entity;
                ItemStack stack = new ItemStack(SUSPICIOUS_SUBSTANCE);
                stack.setCount(2);
                player.getInventory().offerOrDrop(stack);
            }
            if(world.isClient)
            {
                for(int i = 0;i < 25;i++) {
                    world.addParticle(CRIMSON_BURST_PARTICLE,
                            pos.x + world.random.nextGaussian() * 0.5,
                            pos.y + world.random.nextGaussian() * 0.5 ,
                            pos.z + world.random.nextGaussian() * 0.5 ,
                            world.random.nextGaussian() * 0.5,
                            world.random.nextGaussian() * 0.5,
                            world.random.nextGaussian() * 0.5);
                }
            }
            return super.finishUsing(itemStack,world,entity);
        }

    }

    public static final Item SUSPICIOUS_SUBSTANCE = register("suspicious_substance",Item::new,new Item.Settings());
    public static final CrimsonBurstFruit CRIMSON_BURST_FRUIT = new CrimsonBurstFruit
            (new Item.Settings().food(new FoodComponent.Builder().nutrition(4*2).saturationModifier(5.5f)
                    .alwaysEdible().build()));
    public static final LightningStick LIGHTNING_STICK = new LightningStick(new Item.Settings());
    public static final SimpleParticleType CRIMSON_BURST_PARTICLE = FabricParticleTypes.simple();

    public static void initialize() {
        // Get the event for modifying entries in the ingredients group.
        // And register an event handler that adds our suspicious item to the ingredients group.
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> itemGroup.add(ModItems.SUSPICIOUS_SUBSTANCE));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register((itemGroup) -> itemGroup.add(ModItems.LIGHTNING_STICK));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK)
                .register((itemGroup) -> itemGroup.add(ModItems.CRIMSON_BURST_FRUIT));
        // Add the suspicious substance to the composting registry with a 30% chance of increasing the composter's level.
        CompostingChanceRegistry.INSTANCE.add(ModItems.SUSPICIOUS_SUBSTANCE, 0.3f);
        // Add the suspicious substance to the registry of fuels, with a burn time of 30 seconds.
// Remember, Minecraft deals with logical based-time using ticks.
// 20 ticks = 1 second.
        FuelRegistry.INSTANCE.add(ModItems.SUSPICIOUS_SUBSTANCE, 30 * 20);
    }
}

