package com.infinityraider.agricraft.plugins.bayoublues;

import com.infinityraider.agricraft.AgriCraft;
import com.infinityraider.agricraft.api.v1.AgriApi;
import com.infinityraider.agricraft.api.v1.content.items.IAgriSeedItem;
import com.infinityraider.agricraft.api.v1.plugin.AgriPlugin;
import com.infinityraider.agricraft.api.v1.plugin.IAgriPlugin;
import com.infinityraider.agricraft.reference.Names;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@AgriPlugin(modId = Names.Mods.BAYOU_BLUES)
public class BayouBluesPlugin implements IAgriPlugin {

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockPos eventPos = event.getPos();
        World world = event.getWorld();
        BlockState state = world.getBlockState(eventPos);
        System.out.println("click on block " + state);
        if (state.getBlock().equals(Blocks.LILY_PAD)) {
            PlayerEntity player = event.getPlayer();
            Hand hand = event.getHand();
            ItemStack playerstack = player.getHeldItem(hand);
            ItemStack stack = event.getItemStack();
            System.out.println("with stack " + stack);
            System.out.println("with stack " + playerstack);
            if (!stack.isEmpty() && stack.getItem() instanceof IAgriSeedItem) {
                IAgriSeedItem seedItem = (IAgriSeedItem) stack.getItem();
                System.out.println("right click with item " + seedItem);
                if (!world.isRemote) {
                    System.out.println("on server side");
                    BlockPos up = eventPos.up();
                    BlockState newState = AgriCraft.instance.getModBlockRegistry().crop_plant.getDefaultState();
                    System.out.println("state get " + newState);
                    if (newState != null && world.setBlockState(up, newState, 11)) {
                        System.out.println("placed plant");
                        boolean success = AgriApi.getCrop(world, up).map(crop ->
                                seedItem.getGenome(stack).map(crop::spawnGenome).map(result -> {
                                    if (result) {
                                        if (event.getPlayer() == null || !event.getPlayer().isCreative()) {
                                            stack.shrink(1);
                                        }
                                    }
                                    return result;
                                }).orElse(false)).orElse(false);
                        if (success) {
                            System.out.println("success");
//                                    event.setResult(Event.Result.ALLOW);
//                                    return ActionResultType.SUCCESS;
                        } else {
                            System.out.println("no success");
                            world.setBlockState(up, Blocks.AIR.getDefaultState());
                        }
                    }
                }
            }
        }
    }

    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        System.out.println("event fired");
//        BlockPos pos = event.getPos();
        ItemStack itemstack = event.getItemStack();
        World worldIn = event.getWorld();
        PlayerEntity playerIn = event.getPlayer();
        RayTraceResult raytraceresult = rayTrace(worldIn, playerIn, RayTraceContext.FluidMode.ANY);
        if (raytraceresult.getType() == RayTraceResult.Type.MISS) {
//            return ActionResult.resultPass(itemstack);
        } else {
            if (raytraceresult.getType() == RayTraceResult.Type.BLOCK) {
                BlockPos raytracePos = new BlockPos(raytraceresult.getHitVec().x, raytraceresult.getHitVec().y, raytraceresult.getHitVec().z);
                System.out.println("raytracing pos : " + raytracePos.toString());
                BlockState raytraceState = worldIn.getBlockState(raytracePos);
                System.out.println("raytracing blockstate : " + raytraceState.toString());
                ItemStack heldItemStack = event.getItemStack();
                if (heldItemStack.getItem() instanceof IAgriSeedItem) {
                    System.out.println("I am agriseed");
//                    System.out.println("block is " + event.getWorld().getBlockState(pos).getBlock());
                    if (raytraceState.getBlock().equals(Blocks.WATER) && raytraceState.get(FlowingFluidBlock.LEVEL).equals(0)) {
                        IAgriSeedItem seedItem = (IAgriSeedItem) heldItemStack.getItem();
                        System.out.println("right click with item " + seedItem);
                        if (!worldIn.isRemote) {
                            System.out.println("on server side");
                            BlockPos up = raytracePos.up();
                            BlockState newState = AgriCraft.instance.getModBlockRegistry().crop_plant.getDefaultState();
                            System.out.println("state get " + newState);
                            if (newState != null && worldIn.setBlockState(up, newState, 11)) {
                                System.out.println("placed plant");
                                boolean success = AgriApi.getCrop(worldIn, up).map(crop ->
                                        seedItem.getGenome(heldItemStack).map(crop::spawnGenome).map(result -> {
                                            if (result) {
                                                if (playerIn == null || !playerIn.isCreative()) {
                                                    heldItemStack.shrink(1);
                                                }
                                            }
                                            return result;
                                        }).orElse(false)).orElse(false);
                                if (success) {
                                    System.out.println("success");
//                                    event.setResult(Event.Result.ALLOW);
//                                    return ActionResultType.SUCCESS;
                                } else {
                                    System.out.println("no success");
                                    worldIn.setBlockState(up, Blocks.AIR.getDefaultState());
                                }
                            }
                        }
                    }
                }

//                BoatEntity boatentity = new BoatEntity(worldIn, raytraceresult.getHitVec().x, raytraceresult.getHitVec().y, raytraceresult.getHitVec().z);
//                boatentity.setBoatType(this.type);
//                boatentity.rotationYaw = playerIn.rotationYaw;
//                if (!worldIn.hasNoCollisions(boatentity, boatentity.getBoundingBox().grow(-0.1D))) {
//                    return ActionResult.resultFail(itemstack);
//                } else {
//                    if (!worldIn.isRemote) {
//                        worldIn.addEntity(boatentity);
//                        if (!playerIn.abilities.isCreativeMode) {
//                            itemstack.shrink(1);
//                        }
//                    }
//
//                    playerIn.addStat(Stats.ITEM_USED.get(this));
//                    return ActionResult.func_233538_a_(itemstack, worldIn.isRemote());
//                }
            } else {
                System.out.println("raytracing other");
//                return ActionResult.resultPass(itemstack);
            }
        }
    }

    /**
     * Copy of {@link net.minecraft.item.Item#rayTrace(World, PlayerEntity, RayTraceContext.FluidMode)}
     * don't want to setup reflection nor access transformers
     */
    protected static BlockRayTraceResult rayTrace(World worldIn, PlayerEntity player, RayTraceContext.FluidMode fluidMode) {
        float f = player.rotationPitch;
        float f1 = player.rotationYaw;
        Vector3d vector3d = player.getEyePosition(1.0F);
        float f2 = MathHelper.cos(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f3 = MathHelper.sin(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f4 = -MathHelper.cos(-f * ((float)Math.PI / 180F));
        float f5 = MathHelper.sin(-f * ((float)Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double d0 = player.getAttribute(net.minecraftforge.common.ForgeMod.REACH_DISTANCE.get()).getValue();;
        Vector3d vector3d1 = vector3d.add((double)f6 * d0, (double)f5 * d0, (double)f7 * d0);
        return worldIn.rayTraceBlocks(new RayTraceContext(vector3d, vector3d1, RayTraceContext.BlockMode.OUTLINE, fluidMode, player));
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getId() {
        return Names.Mods.BAYOU_BLUES;
    }

    @Override
    public String getDescription() {
        return "Allows Bayou Blues flowers to be planted on water.";
    }

    @Override
    public void onCommonSetupEvent(FMLCommonSetupEvent event) {
        System.out.println("registering plugin");
//        MinecraftForge.EVENT_BUS.addListener(BayouBluesPlugin::onRightClickItem);
        MinecraftForge.EVENT_BUS.addListener(BayouBluesPlugin::onRightClickBlock);
    }

    @Override
    public void onClientSetupEvent(FMLClientSetupEvent event) {

//        IAgriPlant agriPlant = AgriApi.getPlantRegistry().get("bayou_blues:blue_lily").orElse(AgriApi.getPlantRegistry().getNoPlant());
//        agriPlant.asBlockState(agriPlant.getInitialGrowthStage()).get().getBlock()
        Minecraft.getInstance().getBlockColors().register((x, world, pos, u) -> world != null && pos !=null ? 2129968 : 7455580, AgriCraft.instance.getModBlockRegistry().crop_plant);
    }

}
