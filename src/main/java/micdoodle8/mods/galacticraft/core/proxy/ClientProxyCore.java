package micdoodle8.mods.galacticraft.core.proxy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import micdoodle8.mods.galacticraft.api.client.tabs.InventoryTabVanilla;
import micdoodle8.mods.galacticraft.api.client.tabs.TabRegistry;
import micdoodle8.mods.galacticraft.api.vector.BlockVec3;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.core.Constants;
import micdoodle8.mods.galacticraft.core.GCBlocks;
import micdoodle8.mods.galacticraft.core.GCItems;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.blocks.BlockEnclosed;
import micdoodle8.mods.galacticraft.core.client.DynamicTextureProper;
import micdoodle8.mods.galacticraft.core.client.fx.EffectHandler;
import micdoodle8.mods.galacticraft.core.client.gui.screen.InventoryTabGalacticraft;
import micdoodle8.mods.galacticraft.core.client.model.ModelRocketTier1;
import micdoodle8.mods.galacticraft.core.client.render.entities.*;
import micdoodle8.mods.galacticraft.core.client.render.item.*;
import micdoodle8.mods.galacticraft.core.client.render.tile.*;
import micdoodle8.mods.galacticraft.core.entities.*;
import micdoodle8.mods.galacticraft.core.entities.player.IPlayerClient;
import micdoodle8.mods.galacticraft.core.entities.player.PlayerClient;
import micdoodle8.mods.galacticraft.core.fluid.FluidNetwork;
import micdoodle8.mods.galacticraft.core.inventory.InventoryExtended;
import micdoodle8.mods.galacticraft.core.tick.KeyHandlerClient;
import micdoodle8.mods.galacticraft.core.tick.TickHandlerClient;
import micdoodle8.mods.galacticraft.core.tile.*;
import micdoodle8.mods.galacticraft.core.util.ClientUtil;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.core.wrappers.BlockMetaList;
import micdoodle8.mods.galacticraft.core.wrappers.ModelTransformWrapper;
import micdoodle8.mods.galacticraft.core.wrappers.PlayerGearData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class ClientProxyCore extends CommonProxyCore
{
    public static List<String> flagRequestsSent = new ArrayList<>();
    public static Set<BlockVec3> valueableBlocks = Sets.newHashSet();
    public static HashSet<BlockMetaList> detectableBlocks = Sets.newHashSet();
    public static List<BlockVec3> leakTrace;
    public static Map<String, PlayerGearData> playerItemData = Maps.newHashMap();
    public static double playerPosX;
    public static double playerPosY;
    public static double playerPosZ;
    public static float playerRotationYaw;
    public static float playerRotationPitch;
    public static boolean lastSpacebarDown;
    public static HashMap<Integer, Integer> clientSpaceStationID = Maps.newHashMap();
    public static MusicTicker.MusicType MUSIC_TYPE_MARS;
    public static EnumRarity galacticraftItem = EnumHelper.addRarity("GCRarity", EnumChatFormatting.BLUE, "Space");
    public static Map<String, String> capeMap = new HashMap<>();
    public static InventoryExtended dummyInventory = new InventoryExtended();
    public static final ResourceLocation underOilTexture = new ResourceLocation(Constants.ASSET_PREFIX, "textures/misc/underoil.png");
    private static Map<String, ResourceLocation> capesMap = Maps.newHashMap();
    public static IPlayerClient playerClientHandler = new PlayerClient();
    public static Minecraft mc = FMLClientHandler.instance().getClient();
    public static List<String> gearDataRequests = Lists.newArrayList();
    public static DynamicTextureProper overworldTextureClient;
    public static DynamicTextureProper overworldTextureWide;
    public static DynamicTextureProper overworldTextureLarge;
    public static boolean overworldTextureRequestSent;
    public static boolean overworldTexturesValid;
    public static float PLAYER_Y_OFFSET = 1.6200000047683716F;
    public static final ResourceLocation saturnRingTexture = new ResourceLocation(Constants.ASSET_PREFIX, "textures/gui/celestialbodies/saturnRings.png");
    public static final ResourceLocation uranusRingTexture = new ResourceLocation(Constants.ASSET_PREFIX, "textures/gui/celestialbodies/uranusRings.png");
    private static List<Item> itemsToRegisterJson = Lists.newArrayList();
    private static ModelResourceLocation fuelLocation = new ModelResourceLocation(Constants.TEXTURE_PREFIX + "fuel", "fluid");
    private static ModelResourceLocation oilLocation = new ModelResourceLocation(Constants.TEXTURE_PREFIX + "oil", "fluid");

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        ClientProxyCore.registerEntityRenderers();

        OBJLoader.instance.addDomain(Constants.ASSET_PREFIX);
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        Class[][] commonTypes =
                {
                        { MusicTicker.MusicType.class, ResourceLocation.class, int.class, int.class },
                };
        MUSIC_TYPE_MARS = EnumHelper.addEnum(commonTypes, MusicTicker.MusicType.class, "MARS_JC", new ResourceLocation(Constants.ASSET_PREFIX, "galacticraft.musicSpace"), 12000, 24000);
        ClientProxyCore.registerHandlers();
        ClientProxyCore.registerTileEntityRenderers();
        ClientProxyCore.updateCapeList();
        ClientProxyCore.registerInventoryJsons();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        ClientProxyCore.registerInventoryTabs();
        ClientProxyCore.addVariants();

        MinecraftForge.EVENT_BUS.register(new TabRegistry());

        if (!Loader.isModLoaded("RenderPlayerAPI"))
        {
            try
            {
                Field field = RenderManager.class.getDeclaredField(GCCoreUtil.isDeobfuscated() ? "playerRenderer" : "field_178637_m");
                field.setAccessible(true);
                field.set(FMLClientHandler.instance().getClient().getRenderManager(), new RenderPlayerGC());

                field = RenderManager.class.getDeclaredField(GCCoreUtil.isDeobfuscated() ? "skinMap" : "field_178636_l");
                field.setAccessible(true);
                Map<String, RenderPlayer> skinMap = (Map<String, RenderPlayer>) field.get(FMLClientHandler.instance().getClient().getRenderManager());
                skinMap.put("default", new RenderPlayerGC());
                skinMap.put("slim", new RenderPlayerGC(true));
                field.set(FMLClientHandler.instance().getClient().getRenderManager(), skinMap);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void postRegisterItem(Item item)
    {
        if (!item.getHasSubtypes())
        {
            ClientProxyCore.itemsToRegisterJson.add(item);
        }
    }

    @Override
    public void registerVariants()
    {
        Item fuel = Item.getItemFromBlock(GCBlocks.fuel);
        ModelBakery.registerItemVariants(fuel, new ResourceLocation("galacticraftcore:fuel"));
        ModelLoader.setCustomMeshDefinition(fuel, (ItemStack stack) -> fuelLocation);
        ModelLoader.setCustomStateMapper(GCBlocks.fuel, new StateMapperBase()
        {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state)
            {
                return fuelLocation;
            }
        });
        Item oil = Item.getItemFromBlock(GCBlocks.crudeOil);
        ModelBakery.registerItemVariants(oil, new ResourceLocation("galacticraftcore:oil"));
        ModelLoader.setCustomMeshDefinition(oil, (ItemStack stack) -> oilLocation);
        ModelLoader.setCustomStateMapper(GCBlocks.crudeOil, new StateMapperBase()
        {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state)
            {
                return oilLocation;
            }
        });

        Item nasaWorkbench = Item.getItemFromBlock(GCBlocks.nasaWorkbench);
        ModelResourceLocation modelResourceLocation = new ModelResourceLocation("galacticraftcore:rocket_workbench", "inventory");
        ModelLoader.setCustomModelResourceLocation(nasaWorkbench, 0, modelResourceLocation);

        modelResourceLocation = new ModelResourceLocation("galacticraftcore:rocket_t1", "inventory");
        for (int i = 0; i < 5; ++i)
        {
            ModelLoader.setCustomModelResourceLocation(GCItems.rocketTier1, i, modelResourceLocation);
        }

        for (int i = 0; i < 4; ++i)
        {
            modelResourceLocation = new ModelResourceLocation("galacticraftcore:buggy_" + i, "inventory");
            ModelLoader.setCustomModelResourceLocation(GCItems.buggy, i, modelResourceLocation);
        }

        modelResourceLocation = new ModelResourceLocation("galacticraftcore:oil_canister_partial_0", "inventory");
        for (int i = 0; i < GCItems.oilCanister.getMaxDamage(); ++i)
        {
            ModelLoader.setCustomModelResourceLocation(GCItems.oilCanister, i, modelResourceLocation);
        }

        modelResourceLocation = new ModelResourceLocation("galacticraftcore:flag", "inventory");
        ModelLoader.setCustomModelResourceLocation(GCItems.flag, 0, modelResourceLocation);
    }

    @Override
    public World getClientWorld()
    {
        return ClientProxyCore.mc.theWorld;
    }

    @Override
    public void spawnParticle(String particleID, Vector3 position, Vector3 motion, Object[] otherInfo)
    {
        EffectHandler.spawnParticle(particleID, position, motion, otherInfo);
    }

    @Override
    public World getWorldForID(int dimensionID)
    {
        World world = ClientProxyCore.mc.theWorld;

        if (world != null && world.provider.getDimensionId() == dimensionID)
        {
            return world;
        }

        return null;
    }

    @Override
    public EntityPlayer getPlayerFromNetHandler(INetHandler handler)
    {
        if (handler instanceof NetHandlerPlayServer)
        {
            return ((NetHandlerPlayServer) handler).playerEntity;
        }
        else
        {
            return FMLClientHandler.instance().getClientPlayerEntity();
        }
    }

    @Override
    public void unregisterNetwork(FluidNetwork fluidNetwork)
    {
        super.unregisterNetwork(fluidNetwork);

        if (!FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            TickHandlerClient.removeFluidNetwork(fluidNetwork);
        }
    }

    @Override
    public void registerNetwork(FluidNetwork fluidNetwork)
    {
        super.registerNetwork(fluidNetwork);

        if (!FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            TickHandlerClient.addFluidNetwork(fluidNetwork);
        }
    }

    @Override
    public boolean isPaused()
    {
        if (FMLClientHandler.instance().getClient().isSingleplayer() && !FMLClientHandler.instance().getClient().getIntegratedServer().getPublic())
        {
            GuiScreen screen = FMLClientHandler.instance().getClient().currentScreen;

            if (screen != null)
            {
                return screen.doesGuiPauseGame();
            }
        }

        return false;
    }

    @SubscribeEvent
    public void onTextureStitchedPre(TextureStitchEvent.Pre event)
    {
        event.map.registerSprite(new ResourceLocation("galacticraftcore:blocks/assembly"));
        event.map.registerSprite(new ResourceLocation("galacticraftcore:model/rocketT1"));
        event.map.registerSprite(new ResourceLocation("galacticraftcore:model/buggyMain"));
        event.map.registerSprite(new ResourceLocation("galacticraftcore:model/buggyStorage"));
        event.map.registerSprite(new ResourceLocation("galacticraftcore:model/buggyWheels"));
        event.map.registerSprite(new ResourceLocation("galacticraftcore:model/flag0"));
        event.map.registerSprite(new ResourceLocation("galacticraftcore:model/frequencyModule"));
        event.map.registerSprite(new ResourceLocation("galacticraftcore:blocks/fluids/oxygen_gas"));
        event.map.registerSprite(new ResourceLocation("galacticraftcore:blocks/fluids/hydrogen_gas"));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onModelBakeEvent(ModelBakeEvent event)
    {
        replaceModelDefault(event, "rocket_workbench", "block/workbench.obj", ImmutableList.of("Cube"), ItemModelWorkbench.class, new TRSRTransformation(new javax.vecmath.Vector3f(0.6F, 0.04F, 0.0F), new javax.vecmath.Quat4f(), new javax.vecmath.Vector3f(0.42F, 0.42F, 0.42F), new javax.vecmath.Quat4f()));
        replaceModelDefault(event, "rocket_t1", "rocketT1.obj", ImmutableList.of("Rocket"), ItemModelRocket.class, TRSRTransformation.identity());

        for (int i = 0; i < 4; ++i)
        {
            ImmutableList<String> objects = ImmutableList.of("MainBody", "RadarDish_Dish", "Wheel_Back_Left", "Wheel_Back_Right", "Wheel_Front_Left", "Wheel_Front_Right");
            switch (i)
            {
            case 0:
                break;
            case 1:
                objects = ImmutableList.of("MainBody", "RadarDish_Dish", "Wheel_Back_Left", "Wheel_Back_Right", "Wheel_Front_Left", "Wheel_Front_Right", "CargoLeft");
                break;
            case 2:
                objects = ImmutableList.of("MainBody", "RadarDish_Dish", "Wheel_Back_Left", "Wheel_Back_Right", "Wheel_Front_Left", "Wheel_Front_Right", "CargoLeft", "CargoMid");
                break;
            case 3:
                objects = ImmutableList.of("MainBody", "RadarDish_Dish", "Wheel_Back_Left", "Wheel_Back_Right", "Wheel_Front_Left", "Wheel_Front_Right", "CargoLeft", "CargoMid", "CargoRight");
                break;
            }
            replaceModelDefault(event, "buggy_" + i, "buggyInv.obj", objects, ItemModelBuggy.class, TRSRTransformation.identity());
        }

        replaceModelDefault(event, "flag", "flag.obj", ImmutableList.of("Flag", "Pole"), ItemModelFlag.class, TRSRTransformation.identity());

        for (int i = 0; i < 7; ++i)
        {
            ModelResourceLocation modelResourceLocation = new ModelResourceLocation("galacticraftcore:oil_canister_partial_" + i, "inventory");
            IBakedModel object = event.modelRegistry.getObject(modelResourceLocation);
            if (object != null)
            {
                ItemLiquidCanisterModel modelFinal = new ItemLiquidCanisterModel(object);
                event.modelRegistry.putObject(modelResourceLocation, modelFinal);
            }
            modelResourceLocation = new ModelResourceLocation("galacticraftcore:fuel_canister_partial_" + i, "inventory");
            object = event.modelRegistry.getObject(modelResourceLocation);
            if (object != null)
            {
                ItemLiquidCanisterModel modelFinal = new ItemLiquidCanisterModel(object);
                event.modelRegistry.putObject(modelResourceLocation, modelFinal);
            }
        }
    }

    private void replaceModelDefault(ModelBakeEvent event, String resLoc, String objLoc, List<String> visibleGroups, Class<? extends ModelTransformWrapper> clazz, IModelState parentState)
    {
        ClientUtil.replaceModel(Constants.ASSET_PREFIX, event, resLoc, objLoc, visibleGroups, clazz, parentState);
    }

    public static void registerEntityRenderers()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityTier1Rocket.class, (RenderManager manager) -> new RenderTier1Rocket(manager, new ModelRocketTier1(), Constants.ASSET_PREFIX, "rocketT1"));
        RenderingRegistry.registerEntityRenderingHandler(EntityEvolvedSpider.class, (RenderManager manager) -> new RenderEvolvedSpider(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityEvolvedZombie.class, (RenderManager manager) -> new RenderEvolvedZombie(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityEvolvedCreeper.class, (RenderManager manager) -> new RenderEvolvedCreeper(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityEvolvedSkeleton.class, (RenderManager manager) -> new RenderEvolvedSkeleton(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntitySkeletonBoss.class, (RenderManager manager) -> new RenderEvolvedSkeletonBoss(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityMeteor.class, (RenderManager manager) -> new RenderMeteor(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityFlag.class, (RenderManager manager) -> new RenderFlag(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityParachest.class, (RenderManager manager) -> new RenderParaChest(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityAlienVillager.class, (RenderManager manager) -> new RenderAlienVillager(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityLander.class, (RenderManager manager) -> new RenderLander(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityCelestialFake.class, (RenderManager manager) -> new RenderEntityFake(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityBuggy.class, (RenderManager manager) -> new RenderBuggy(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityMeteorChunk.class, (RenderManager manager) -> new RenderMeteorChunk(manager));
//        RenderingRegistry.registerEntityRenderingHandler(EntityBubble.class, new RenderBubble(0.25F, 0.25F, 1.0F));
    }

    private static void registerHandlers()
    {
        TickHandlerClient tickHandlerClient = new TickHandlerClient();
        MinecraftForge.EVENT_BUS.register(tickHandlerClient);
        MinecraftForge.EVENT_BUS.register(new KeyHandlerClient());
        ClientRegistry.registerKeyBinding(KeyHandlerClient.galaxyMap);
        ClientRegistry.registerKeyBinding(KeyHandlerClient.openFuelGui);
        ClientRegistry.registerKeyBinding(KeyHandlerClient.toggleAdvGoggles);
        MinecraftForge.EVENT_BUS.register(GalacticraftCore.proxy);
    }

    private static void registerTileEntityRenderers()
    {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTreasureChest.class, new TileEntityTreasureChestRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySolar.class, new TileEntitySolarPanelRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityOxygenDistributor.class, new TileEntityBubbleProviderRenderer<>(0.25F, 0.25F, 1.0F));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityScreen.class, new TileEntityScreenRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFluidTank.class, new TileEntityFluidTankRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFluidPipe.class, new TileEntityFluidPipeRenderer());
//            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDish.class, new TileEntityDishRenderer());
//            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityThruster.class, new TileEntityThrusterRenderer());
//            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityArclamp.class, new TileEntityArclampRenderer());
//            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFluidPipe.class, new TileEntityOxygenPipeRenderer());
//            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityOxygenStorageModule.class, new TileEntityMachineRenderer());
//            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCircuitFabricator.class, new TileEntityMachineRenderer());
//            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityElectricIngotCompressor.class, new TileEntityMachineRenderer());
    }

    private static void registerInventoryJsons()
    {
        for (Item toReg : ClientProxyCore.itemsToRegisterJson)
        {
            ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, toReg);
        }

        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.canister, 0, "canister_tin");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.canister, 1, "canister_copper");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.rocketEngine, 0, "tier1engine");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.rocketEngine, 1, "tier1booster");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.parachute, 0, "parachute_plain");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.parachute, 1, "parachute_black");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.parachute, 2, "parachute_blue");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.parachute, 3, "parachute_lime");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.parachute, 4, "parachute_brown");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.parachute, 5, "parachute_darkblue");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.parachute, 6, "parachute_darkgray");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.parachute, 7, "parachute_darkgreen");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.parachute, 8, "parachute_gray");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.parachute, 9, "parachute_magenta");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.parachute, 10, "parachute_orange");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.parachute, 11, "parachute_pink");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.parachute, 12, "parachute_purple");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.parachute, 13, "parachute_red");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.parachute, 14, "parachute_teal");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.parachute, 15, "parachute_yellow");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.schematic, 0, "schematic_buggy");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.schematic, 1, "schematic_rocket_t2");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.key, 0, "key");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.partBuggy, 0, "wheel");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.partBuggy, 1, "seat");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.partBuggy, 2, "storage");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.basicItem, 0, "solar_module_0");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.basicItem, 1, "solar_module_1");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.basicItem, 2, "raw_silicon");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.basicItem, 3, "ingot_copper");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.basicItem, 4, "ingot_tin");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.basicItem, 5, "ingot_aluminum");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.basicItem, 6, "compressed_copper");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.basicItem, 7, "compressed_tin");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.basicItem, 8, "compressed_aluminum");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.basicItem, 9, "compressed_steel");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.basicItem, 10, "compressed_bronze");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.basicItem, 11, "compressed_iron");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.basicItem, 12, "wafer_solar");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.basicItem, 13, "wafer_basic");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.basicItem, 14, "wafer_advanced");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.basicItem, 15, "dehydrated_apple");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.basicItem, 16, "dehydrated_carrot");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.basicItem, 17, "dehydrated_melon");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.basicItem, 18, "dehydrated_potato");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.basicItem, 19, "frequency_module");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.basicItem, 20, "ambient_thermal_controller");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.itemBasicMoon, 0, "meteoric_iron_ingot");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.itemBasicMoon, 1, "compressed_meteoric_iron");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.itemBasicMoon, 2, "lunar_sapphire");
        for (int i = 0; i <= GCItems.oilCanister.getMaxDamage(); ++i)
        {
            int damage = 6 * i / GCItems.oilCanister.getMaxDamage();
            ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.oilCanister, i, "oil_canister_partial_" + (7 - damage - 1));
        }
        for (int i = 0; i <= GCItems.fuelCanister.getMaxDamage(); ++i)
        {
            int damage = 6 * i / GCItems.fuelCanister.getMaxDamage();
            ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.fuelCanister, i, "fuel_canister_partial_" + (7 - damage - 1));
        }
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.meteorChunk, 0, "meteor_chunk");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.meteorChunk, 1, "meteor_chunk_hot");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.buggy, 0, "buggy_0");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.buggy, 1, "buggy_1");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.buggy, 2, "buggy_2");
        ClientUtil.registerItemJson(Constants.TEXTURE_PREFIX, GCItems.buggy, 3, "buggy_3");

        // Blocks
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.breatheableAir);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.brightAir);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.brightBreatheableAir);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.brightLamp);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.treasureChestTier1);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.landingPad, 0, "landing_pad");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.landingPad, 1, "buggy_pad");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.unlitTorch);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.unlitTorchLit);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.oxygenDistributor);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.oxygenPipe);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.oxygenPipePull);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.oxygenCollector);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.oxygenCompressor, 0, "oxygen_compressor");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.oxygenCompressor, 4, "oxygen_decompressor");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.oxygenSealer);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.oxygenDetector);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.nasaWorkbench);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.fallenMeteor);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.basicBlock, 3, "deco_block_0");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.basicBlock, 4, "deco_block_1");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.basicBlock, 5, "ore_copper_gc");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.basicBlock, 6, "ore_tin_gc");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.basicBlock, 7, "ore_aluminum_gc");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.basicBlock, 8, "ore_silicon");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.basicBlock, 9, "block_copper_gc");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.basicBlock, 10, "block_tin_gc");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.basicBlock, 11, "block_aluminum_gc");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.basicBlock, 12, "block_meteoric_iron_gc");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.airLockFrame, 0, "air_lock_frame");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.airLockFrame, 1, "air_lock_controller");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.airLockSeal, 0, "air_lock_seal");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.refinery);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.fuelLoader);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.landingPadFull, 0, "landing_pad_full");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.landingPadFull, 1, "buggy_pad_full");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.spaceStationBase);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.fakeBlock);
        for (BlockEnclosed.EnumEnclosedBlockType type : BlockEnclosed.EnumEnclosedBlockType.values())
        {
            ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.sealableBlock, type.getMeta(), type.getName());
        }
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.cargoLoader, 0, "cargo_loader");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.cargoLoader, 4, "cargo_unloader");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.parachest);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.solarPanel, 0, "basic_solar");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.solarPanel, 4, "advanced_solar");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.machineBase, 0, "coal_generator");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.machineBase, 12, "ingot_compressor");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.machineBase2, 0, "electric_ingot_compressor");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.machineBase2, 4, "circuit_fabricator");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.machineBase2, 8, "oxygen_storage_module");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.machineTiered, 0, "energy_storage");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.machineTiered, 4, "electric_furnace");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.machineTiered, 8, "cluster_storage");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.machineTiered, 12, "arc_furnace");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.aluminumWire, 0, "aluminum_wire");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.aluminumWire, 1, "aluminum_wire_heavy");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.glowstoneTorch);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.blockMoon, 0, "ore_copper_moon");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.blockMoon, 1, "ore_tin_moon");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.blockMoon, 2, "ore_cheese_moon");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.blockMoon, 3, "moon_dirt_moon");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.blockMoon, 4, "moon_stone");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.blockMoon, 5, "moon_turf");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.blockMoon, 6, "ore_sapphire_moon");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.blockMoon, 14, "moon_dungeon_brick");
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.cheeseBlock);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.spinThruster);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.screen);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.telemetry);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.fluidTank);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.unlitTorch);
        ClientUtil.registerBlockJson(Constants.TEXTURE_PREFIX, GCBlocks.unlitTorchLit);
    }

    private static void addVariants()
    {
        addCoreVariant("air_lock_frame", "air_lock_frame", "air_lock_controller");
        addCoreVariant("basic_block_core", "deco_block_0", "deco_block_1", "ore_copper_gc", "ore_tin_gc", "ore_aluminum_gc", "ore_silicon", "block_copper_gc", "block_tin_gc", "block_aluminum_gc", "block_meteoric_iron_gc");
        addCoreVariant("air_lock_frame", "air_lock_frame", "air_lock_controller");
        addCoreVariant("landing_pad", "landing_pad", "buggy_pad");
        addCoreVariant("oxygen_compressor", "oxygen_compressor", "oxygen_decompressor");
        addCoreVariant("cargo", "cargo_loader", "cargo_unloader");
        addCoreVariant("enclosed", "enclosed_hv_cable", "enclosed_fluid_pipe", "enclosed_copper_cable", "enclosed_gold_cable", "enclosed_te_conduit", "enclosed_glass_fibre_cable", "enclosed_lv_cable", "enclosed_pipe_items_stone", "enclosed_pipe_items_cobblestone", "enclosed_pipe_fluids_stone", "enclosed_pipe_fluids_cobblestone", "enclosed_pipe_power_stone", "enclosed_pipe_power_gold", "enclosed_me_cable", "enclosed_aluminum_wire", "enclosed_heavy_aluminum_wire");
        addCoreVariant("solar", "advanced_solar", "basic_solar");
        addCoreVariant("machine", "coal_generator", "ingot_compressor");
        addCoreVariant("machine2", "circuit_fabricator", "oxygen_storage_module", "electric_ingot_compressor");
        addCoreVariant("machine_tiered", "energy_storage", "electric_furnace", "cluster_storage", "arc_furnace");
        addCoreVariant("basic_block_moon", "ore_copper_moon", "ore_tin_moon", "ore_cheese_moon", "moon_dirt_moon", "moon_stone", "moon_turf", "ore_sapphire_moon", "moon_dungeon_brick");
        addCoreVariant("canister", "canister_tin", "canister_copper");
        addCoreVariant("engine", "tier1engine", "tier1booster");
        addCoreVariant("parachute", "parachute_plain", "parachute_black", "parachute_blue", "parachute_lime", "parachute_brown", "parachute_darkblue", "parachute_darkgray", "parachute_darkgreen", "parachute_gray", "parachute_magenta", "parachute_orange", "parachute_pink", "parachute_purple", "parachute_red", "parachute_teal", "parachute_yellow");
        addCoreVariant("oil_canister_partial", "oil_canister_partial_0", "oil_canister_partial_1", "oil_canister_partial_2", "oil_canister_partial_3", "oil_canister_partial_4", "oil_canister_partial_5", "oil_canister_partial_6");
        addCoreVariant("fuel_canister_partial", "fuel_canister_partial_0", "fuel_canister_partial_1", "fuel_canister_partial_2", "fuel_canister_partial_3", "fuel_canister_partial_4", "fuel_canister_partial_5", "fuel_canister_partial_6");
        addCoreVariant("schematic", "schematic_buggy", "schematic_rocket_t2");
        addCoreVariant("key", "key");
        addCoreVariant("buggymat", "wheel", "seat", "storage");
        addCoreVariant("basic_item", "solar_module_0", "solar_module_1", "raw_silicon", "ingot_copper", "ingot_tin", "ingot_aluminum", "compressed_copper", "compressed_tin", "compressed_aluminum", "compressed_steel", "compressed_bronze", "compressed_iron", "wafer_solar", "wafer_basic", "wafer_advanced", "dehydrated_apple", "dehydrated_carrot", "dehydrated_melon", "dehydrated_potato", "frequency_module", "ambient_thermal_controller");
        addCoreVariant("item_basic_moon", "meteoric_iron_ingot", "compressed_meteoric_iron", "lunar_sapphire");
        addCoreVariant("aluminum_wire", "aluminum_wire", "aluminum_wire_heavy");
        addCoreVariant("meteor_chunk", "meteor_chunk", "meteor_chunk_hot");
        addCoreVariant("buggy", "buggy_0", "buggy_1", "buggy_2", "buggy_3");
    }

    private static void addCoreVariant(String name, String... variants)
    {
        Item itemBlockVariants = GameRegistry.findItem(Constants.MOD_ID_CORE, name);
        ResourceLocation[] variants0 = new ResourceLocation[variants.length];
        for (int i = 0; i < variants.length; ++i)
        {
            variants0[i] = new ResourceLocation(Constants.TEXTURE_PREFIX + variants[i]);
        }
        ModelBakery.registerItemVariants(itemBlockVariants, variants0);
    }

    private static void updateCapeList()
    {
        int timeout = 10000;
        URL capeListUrl;

        try
        {
            capeListUrl = new URL("https://raw.github.com/micdoodle8/Galacticraft/master/capes.txt");
        }
        catch (MalformedURLException e)
        {
            FMLLog.severe("Error getting capes list URL");
            e.printStackTrace();
            return;
        }

        URLConnection connection;

        try
        {
            connection = capeListUrl.openConnection();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }

        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        InputStream stream;

        try
        {
            stream = connection.getInputStream();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }

        InputStreamReader streamReader = new InputStreamReader(stream);
        BufferedReader reader = new BufferedReader(streamReader);

        String line;
        try
        {
            while ((line = reader.readLine()) != null)
            {
                if (line.contains(":"))
                {
                    int splitLocation = line.indexOf(":");
                    String username = line.substring(0, splitLocation);
                    String capeUrl = "https://raw.github.com/micdoodle8/Galacticraft/master/capes/" + line.substring(splitLocation + 1) + ".png";
                    ClientProxyCore.capeMap.put(username, capeUrl);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        try
        {
            streamReader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        try
        {
            stream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void registerInventoryTabs()
    {
        if (TabRegistry.getTabList().size() == 0)
        {
            TabRegistry.registerTab(new InventoryTabVanilla());
        }

        TabRegistry.registerTab(new InventoryTabGalacticraft());
    }

    public static class EventSpecialRender extends Event
    {
        public final float partialTicks;

        public EventSpecialRender(float partialTicks)
        {
            this.partialTicks = partialTicks;
        }
    }
}
