package com.infinityraider.agricraft.plugins.bayoublues;

import com.google.common.collect.ImmutableList;
import com.infinityraider.agricraft.AgriCraft;
import com.infinityraider.agricraft.api.v1.AgriApi;
import com.infinityraider.agricraft.api.v1.client.PlantQuadBakeEvent;
import com.infinityraider.agricraft.api.v1.content.items.IAgriSeedItem;
import com.infinityraider.agricraft.api.v1.plugin.AgriPlugin;
import com.infinityraider.agricraft.api.v1.plugin.IAgriPlugin;
import com.infinityraider.agricraft.reference.Names;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Arrays;

@AgriPlugin(modId = Names.Mods.BAYOU_BLUES)
public class BayouBluesPlugin implements IAgriPlugin {

    /**
     * We replace the lily pad with the bayou blue lily flower on right click
     * We don't use agricraft seed right click behaviour (we don't check for the soil as it should be planted on lily pad only)
     */
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockPos pos = event.getPos();
        World world = event.getWorld();
        BlockState state = world.getBlockState(pos);
        System.out.println("click on block " + state);
        if (state.getBlock().equals(Blocks.LILY_PAD)) {
            PlayerEntity player = event.getPlayer();
            //If the plant have a registered soil, we receive the item from the offhand in the event
            //So we have to check if the item in the main hand, because that's the one we want first
            // TODO: 01/07/2021 find a way to cancel this behaviour (we want the event when the right click is on the main hand)
            Item mainItem = player.getHeldItem(Hand.MAIN_HAND).getItem();
            ItemStack stack = mainItem instanceof IAgriSeedItem ? player.getHeldItem(Hand.MAIN_HAND) : player.getHeldItem(Hand.OFF_HAND);
            System.out.println("using stack " + stack);
            if (!stack.isEmpty() && stack.getItem() instanceof IAgriSeedItem) {
                IAgriSeedItem seedItem = (IAgriSeedItem) stack.getItem();
                System.out.println("right click with item " + seedItem);
                if (!world.isRemote) {
                    BlockState newState = AgriCraft.instance.getModBlockRegistry().crop_plant.getDefaultState();
                    if (world.setBlockState(pos, newState, 11)) {
                        System.out.println("plant placed");
                        boolean success = AgriApi.getCrop(world, pos).map(crop ->
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
                            world.setBlockState(pos, Blocks.LILY_PAD.getDefaultState());
                        }
                    }
                }
            }
        }
    }

    public static void onQuadBakedVertex(PlantQuadBakeEvent event) {
        System.out.println("event catch");
//        IAgriRenderable renderplant = event.getPlant();
//        if (renderplant instanceof IAgriPlant) {
//            IAgriPlant agriPlant = ((IAgriPlant) renderplant);
//            System.out.println("getting plant id : " + agriPlant.getId());
//            if (agriPlant.isPlant() && agriPlant.getId().equals("upgrade_aquatic:purple_pickerelweed_plant")) {
//                System.out.println("baking purple pickerelweed");
//            }
//        }
        ImmutableList<BakedQuad> originalQuads = event.getOriginalQuads();
        event.clearQuads();// we clear the quads as we don't want the originals

        VertexFormat format = DefaultVertexFormats.BLOCK;
        int size = 1;
        int colorIndex = -1;
        //search for the index & size of the color element
        for (int i = 0; i < format.getElements().size(); i++) {
            if (format.getElements().get(i).getUsage() == VertexFormatElement.Usage.COLOR) {
                colorIndex = i;
                size = format.getElements().get(i).getSize() / 4;
            }
        }
        int offset = format.getOffset(colorIndex);
        int[] rgba = new int[]{0xFF, 0x00, 0x00, 0xFF};//the order should be r g b a, I hope so
        System.out.println("offset = " + offset);

        System.out.println("looping on quads");
        for (BakedQuad originalQuad : originalQuads) {
            //code adapted from https://forums.minecraftforge.net/topic/66893-112-is-there-a-way-to-color-bakedquads/?do=findComment&comment=321762
//            if (originalQuad.getTintIndex() >= 0) {
//                System.out.println("processing tint index " + originalQuad.getTintIndex());
            int[] vertex = originalQuad.getVertexData();
            System.out.println(Arrays.toString(vertex));
            for (int i = 0; i < 4; i++) {
                vertex[offset + size * i] = rgba[i];//we change the color
            }

            BakedQuad newQuad = new BakedQuad(vertex,
                    originalQuad.getTintIndex(),
                    originalQuad.getFace(),
                    originalQuad.getSprite(),
                    originalQuad.applyDiffuseLighting());
            event.injectQuads(newQuad);//we add the new quad.
//            } else {
//                System.out.println("injecting original");
//                event.injectQuads(originalQuads);
//            }
        }
        System.out.println("amount of quads " + event.getOutputQuads().size());
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
        return "Allows Bayou Blues flowers to be planted on lily pads.";
    }

    @Override
    public void onCommonSetupEvent(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(BayouBluesPlugin::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(BayouBluesPlugin::onQuadBakedVertex);
    }

    @Override
    public void onClientSetupEvent(FMLClientSetupEvent event) {
//        Minecraft.getInstance().getBlockColors().register((x, world, pos, u) -> world != null && pos !=null ? 2129968 : 7455580, AgriCraft.instance.getModBlockRegistry().crop_plant);
    }

}
