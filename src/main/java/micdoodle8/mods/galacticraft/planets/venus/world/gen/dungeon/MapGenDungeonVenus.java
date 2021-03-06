package micdoodle8.mods.galacticraft.planets.venus.world.gen.dungeon;

import micdoodle8.mods.galacticraft.core.world.gen.dungeon.DungeonConfiguration;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;

import java.util.List;
import java.util.Random;

public class MapGenDungeonVenus extends MapGenStructure
{
    private static boolean initialized;
    private DungeonConfigurationVenus configuration;

    static
    {
        try
        {
            MapGenDungeonVenus.initiateStructures();
        }
        catch (Throwable e)
        {

        }
    }

    public MapGenDungeonVenus(DungeonConfigurationVenus configuration)
    {
        this.configuration = configuration;
    }

    public static void initiateStructures() throws Throwable
    {
        if (!MapGenDungeonVenus.initialized)
        {
            MapGenStructureIO.registerStructure(MapGenDungeonVenus.Start.class, "VenusDungeon");
            MapGenStructureIO.registerStructureComponent(DungeonStartVenus.class, "VenusDungeonStart");
            MapGenStructureIO.registerStructureComponent(CorridorVenus.class, "VenusDungeonCorridor");
            MapGenStructureIO.registerStructureComponent(RoomEmptyVenus.class, "VenusDungeonEmptyRoom");
            MapGenStructureIO.registerStructureComponent(RoomBossVenus.class, "VenusDungeonBossRoom");
            MapGenStructureIO.registerStructureComponent(RoomTreasureVenus.class, "VenusDungeonTreasureRoom");
            MapGenStructureIO.registerStructureComponent(RoomSpawnerVenus.class, "VenusDungeonSpawnerRoom");
            MapGenStructureIO.registerStructureComponent(RoomChestVenus.class, "VenusDungeonChestRoom");
        }

        MapGenDungeonVenus.initialized = true;
    }

    @Override
    public String getStructureName()
    {
        return "GC_Dungeon_Venus";
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
        final byte numChunks = 44;
        int i = chunkX;
        int j = chunkZ;

        if (chunkX < 0)
        {
            chunkX -= numChunks - 1;
        }

        if (chunkZ < 0)
        {
            chunkZ -= numChunks - 1;
        }

        int k = chunkX / numChunks;
        int l = chunkZ / numChunks;
        Random random = this.worldObj.setRandomSeed(k, l, 10387312);
        k = k * numChunks;
        l = l * numChunks;
        k = k + random.nextInt(numChunks);
        l = l + random.nextInt(numChunks);

        return i == k && j == l;
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ)
    {
        return new MapGenDungeonVenus.Start(this.worldObj, this.rand, chunkX, chunkZ, this.configuration);
    }

    public static class Start extends StructureStart
    {
        private DungeonConfiguration configuration;

        public Start()
        {
        }

        public Start(World worldIn, Random rand, int chunkX, int chunkZ, DungeonConfiguration configuration)
        {
            super(chunkX, chunkZ);
            this.configuration = configuration;
            DungeonStartVenus startPiece = new DungeonStartVenus(worldIn, configuration, rand, (chunkX << 4) + 2, (chunkZ << 4) + 2);
            startPiece.buildComponent(startPiece, this.components, rand);
            List<StructureComponent> list = startPiece.attachedComponents;

            while (!list.isEmpty())
            {
                int i = rand.nextInt(list.size());
                StructureComponent structurecomponent = list.remove(i);
                structurecomponent.buildComponent(startPiece, this.components, rand);
            }

            this.updateBoundingBox();
        }
    }
}
