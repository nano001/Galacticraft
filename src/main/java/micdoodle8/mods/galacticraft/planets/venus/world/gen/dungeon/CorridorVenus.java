package micdoodle8.mods.galacticraft.planets.venus.world.gen.dungeon;

import micdoodle8.mods.galacticraft.core.GCBlocks;
import micdoodle8.mods.galacticraft.core.blocks.BlockUnlitTorch;
import micdoodle8.mods.galacticraft.core.world.gen.dungeon.DungeonConfiguration;
import micdoodle8.mods.galacticraft.core.world.gen.dungeon.DungeonStart;
import micdoodle8.mods.galacticraft.core.world.gen.dungeon.Piece;
import micdoodle8.mods.galacticraft.core.world.gen.dungeon.SizedPiece;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import java.lang.reflect.Constructor;
import java.util.Random;

public class CorridorVenus extends SizedPieceVenus
{
    public CorridorVenus()
    {
    }

    public CorridorVenus(DungeonConfiguration configuration, Random rand, int blockPosX, int blockPosZ, int sizeX, int sizeY, int sizeZ, EnumFacing direction)
    {
        super(configuration, sizeX, sizeY, sizeZ, direction);
        this.coordBaseMode = EnumFacing.SOUTH;
        this.boundingBox = new StructureBoundingBox(blockPosX, configuration.getYPosition(), blockPosZ, blockPosX + sizeX, configuration.getYPosition() + sizeY, blockPosZ + sizeZ);
    }

    @Override
    public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
    {
        for (int i = 0; i < this.boundingBox.getXSize(); i++)
        {
            for (int j = 0; j < this.boundingBox.getYSize(); j++)
            {
                for (int k = 0; k < this.boundingBox.getZSize(); k++)
                {
                    if (j == 2 && this.getDirection().getAxis() == EnumFacing.Axis.Z && (k + 1) % 4 == 0 && k != this.boundingBox.getZSize() - 1)
                    {
                        if (i == 0 || i == this.boundingBox.getXSize() - 1)
                        {
                            this.setBlockState(worldIn, Blocks.lava.getDefaultState(), i, j, k, this.boundingBox);
                        }
                        else if (i == 1 || i == this.boundingBox.getXSize() - 2)
                        {
                            this.setBlockState(worldIn, Blocks.iron_bars.getDefaultState(), i, j, k, this.boundingBox);
                        }
                        else
                        {
                            this.setBlockState(worldIn, Blocks.air.getDefaultState(), i, j, k, this.boundingBox);
                        }
                    }
                    else if (j == 2 && this.getDirection().getAxis() == EnumFacing.Axis.X && (i + 1) % 4 == 0 && i != this.boundingBox.getXSize() - 1)
                    {
                        if (k == 0 || k == this.boundingBox.getZSize() - 1)
                        {
                            this.setBlockState(worldIn, Blocks.lava.getDefaultState(), i, j, k, this.boundingBox);
                        }
                        else if (k == 1 || k == this.boundingBox.getZSize() - 2)
                        {
                            this.setBlockState(worldIn, Blocks.iron_bars.getDefaultState(), i, j, k, this.boundingBox);
                        }
                        else
                        {
                            this.setBlockState(worldIn, Blocks.air.getDefaultState(), i, j, k, this.boundingBox);
                        }
                    }
                    else if ((this.getDirection().getAxis() == EnumFacing.Axis.Z && (i == 1 || i == this.boundingBox.getXSize() - 2)) ||
                            j == 0 || j == this.boundingBox.getYSize() - 1 ||
                            (this.getDirection().getAxis() == EnumFacing.Axis.X && (k == 1 || k == this.boundingBox.getZSize() - 2)))
                    {
                        DungeonConfigurationVenus venusConfig = (DungeonConfigurationVenus) this.configuration;
                        this.setBlockState(worldIn, j == 0 || j == this.boundingBox.getYSize() - 1 ? venusConfig.getBrickBlockFloor() : this.configuration.getBrickBlock(), i, j, k, this.boundingBox);
                    }
                    else if ((this.getDirection().getAxis() == EnumFacing.Axis.Z && (i == 0 || i == this.boundingBox.getXSize() - 1)) ||
                            (this.getDirection().getAxis() == EnumFacing.Axis.X && (k == 0 || k == this.boundingBox.getZSize() - 1)))
                    {
                        DungeonConfigurationVenus venusConfig = (DungeonConfigurationVenus) this.configuration;
                        this.setBlockState(worldIn, j == 0 || j == this.boundingBox.getYSize() - 1 ? venusConfig.getBrickBlockFloor() : this.configuration.getBrickBlock(), i, j, k, this.boundingBox);
                    }
                    else
                    {
                        this.setBlockState(worldIn, Blocks.air.getDefaultState(), i, j, k, this.boundingBox);
                    }
                }
            }
        }

        return true;
    }

    private <T extends SizedPiece> T getRoom(Class<? extends T> clazz, DungeonStart startPiece, Random rand)
    {
        try
        {
            Constructor<? extends T> c0 = clazz.getConstructor(DungeonConfiguration.class, Random.class, Integer.TYPE, Integer.TYPE, EnumFacing.class);
            T dummy = c0.newInstance(this.configuration, rand, 0, 0, this.getDirection().getOpposite());
            StructureBoundingBox extension = getExtension(this.getDirection(), getDirection().getAxis() == EnumFacing.Axis.X ? dummy.getSizeX() : dummy.getSizeZ(), getDirection().getAxis() == EnumFacing.Axis.X ? dummy.getSizeZ() : dummy.getSizeX());
            if (startPiece.checkIntersection(extension))
            {
                return null;
            }
            int sizeX = extension.maxX - extension.minX;
            int sizeZ = extension.maxZ - extension.minZ;
            int sizeY = dummy.getSizeY();
            int blockX = extension.minX;
            int blockZ = extension.minZ;
            Constructor<? extends T> c1 = clazz.getConstructor(DungeonConfiguration.class, Random.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, EnumFacing.class);
            return c1.newInstance(this.configuration, rand, blockX, blockZ, sizeX, sizeY, sizeZ, this.getDirection().getOpposite());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Piece getNextPiece(DungeonStart startPiece, Random rand)
    {
        int bossRoomChance = Math.max((int) (1.0 / Math.pow(startPiece.attachedComponents.size() / 55.0, 2)), 5);
        boolean bossRoom = rand.nextInt(bossRoomChance) == 0;

        if (bossRoom)
        {
            try
            {
                return getRoom(this.configuration.getBossRoom(), startPiece, rand);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            if (startPiece.attachedComponents.size() > 2 && startPiece.attachedComponents.get(startPiece.attachedComponents.size() - 2) instanceof RoomBossVenus)
            {
                try
                {
                    return getRoom(this.configuration.getTreasureRoom(), startPiece, rand);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                StructureBoundingBox extension = getExtension(this.getDirection(), rand.nextInt(4) + 6, rand.nextInt(4) + 6);

                if (startPiece.checkIntersection(extension))
                {
                    return null;
                }

                int sizeX = extension.maxX - extension.minX;
                int sizeZ = extension.maxZ - extension.minZ;
                int sizeY = configuration.getRoomHeight();
                int blockX = extension.minX;
                int blockZ = extension.minZ;

                if (Math.abs(startPiece.getBoundingBox().maxZ - boundingBox.minZ) > 200)
                {
                    return null;
                }

                if (Math.abs(startPiece.getBoundingBox().maxX - boundingBox.minX) > 200)
                {
                    return null;
                }

                switch (rand.nextInt(3))
                {
                case 0:
                    return new RoomSpawnerVenus(this.configuration, rand, blockX, blockZ, sizeX, sizeY, sizeZ, this.getDirection().getOpposite());
                case 1:
                    return new RoomChestVenus(this.configuration, rand, blockX, blockZ, sizeX, sizeY, sizeZ, this.getDirection().getOpposite());
                default:
                case 2:
                    return new RoomEmptyVenus(this.configuration, rand, blockX, blockZ, sizeX, sizeY, sizeZ, this.getDirection().getOpposite());
                }
            }

        }

        return null;
    }
}