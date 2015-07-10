package micdoodle8.mods.galacticraft.core.blocks;

import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.items.ItemBlockDesc;
import micdoodle8.mods.galacticraft.core.tile.TileEntityArclamp;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.core.util.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class BlockBrightLamp extends BlockAdvanced implements ItemBlockDesc.IBlockShiftDesc
{
    public static final PropertyDirection FACING = PropertyDirection.create("facing");

    //Metadata: bits 0-2 are the side of the base plate using standard side convention (0-5)

    protected BlockBrightLamp(String assetName)
    {
        super(Material.glass);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.UP));
        this.setHardness(0.1F);
        this.setStepSound(Block.soundTypeWood);
        //this.setBlockTextureName("stone");
        this.setUnlocalizedName(assetName);
        this.setLightLevel(1.0F);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state)
    {
        double boundsMin = 0.2D;
        double boundsMax = 0.8D;
        return AxisAlignedBB.fromBounds(pos.getX() + boundsMin, pos.getY() + boundsMin, pos.getZ() + boundsMin, pos.getX() + boundsMax, pos.getY() + boundsMax, pos.getZ() + boundsMax);
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean isFullCube()
    {
        return false;
    }

    @Override
    public int getRenderType()
    {
        return GalacticraftCore.proxy.getBlockRender(this);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        for (EnumFacing side : EnumFacing.values())
        {
            BlockPos offsetPos = pos.offset(side);
            IBlockState state = worldIn.getBlockState(offsetPos);
            if (state.getBlock().isSideSolid(worldIn, offsetPos, EnumFacing.getFront(side.getIndex() ^ 1)))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        EnumFacing opposite = EnumFacing.values()[facing.getIndex() ^ 1];
        BlockPos offsetPos = pos.offset(opposite);
        IBlockState state = worldIn.getBlockState(offsetPos);
        if (state.getBlock().isSideSolid(worldIn, offsetPos, facing))
        {
            return this.getDefaultState().withProperty(FACING, opposite);
        }

        return this.getDefaultState().withProperty(FACING, facing);
    }

    /**
     * Lets the block know when one of its neighbor changes. Doesn't know which
     * neighbor changed (coordinates passed are their own) Args: x, y, z,
     * neighbor blockID
     */
    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        EnumFacing side = (EnumFacing)state.getValue(FACING);

        BlockPos offsetPos = pos.offset(side);
        IBlockState state1 = worldIn.getBlockState(offsetPos);
        if (state1.getBlock().isSideSolid(worldIn, offsetPos, EnumFacing.getFront(side.getIndex() ^ 1)))
        {
            return;
        }

        this.dropBlockAsItem(worldIn, pos, state, 0);
        worldIn.setBlockToAir(pos);
    }

    /**
     * Ray traces through the blocks collision from start vector to end vector
     * returning a ray trace hit. Args: world, x, y, z, startVec, endVec
     */
    @Override
    public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end)
    {
        EnumFacing side = (EnumFacing)worldIn.getBlockState(pos).getValue(FACING);
        float var8 = 0.3F;

        if (side == EnumFacing.WEST)
        {
            this.setBlockBounds(0.0F, 0.2F, 0.5F - var8, var8 * 2.0F, 0.8F, 0.5F + var8);
        }
        else if (side == EnumFacing.EAST)
        {
            this.setBlockBounds(1.0F - var8 * 2.0F, 0.2F, 0.5F - var8, 1.0F, 0.8F, 0.5F + var8);
        }
        else if (side == EnumFacing.NORTH)
        {
            this.setBlockBounds(0.5F - var8, 0.2F, 0.0F, 0.5F + var8, 0.8F, var8 * 2.0F);
        }
        else if (side == EnumFacing.SOUTH)
        {
            this.setBlockBounds(0.5F - var8, 0.2F, 1.0F - var8 * 2.0F, 0.5F + var8, 0.8F, 1.0F);
        }
        else if (side == EnumFacing.DOWN)
        {
            this.setBlockBounds(0.5F - var8, 0.0F, 0.5F - var8, 0.5F + var8, 0.6F, 0.5F + var8);
        }
        else
        {
            this.setBlockBounds(0.5F - var8, 0.4F, 0.5F - var8, 0.5F + var8, 1.0F, 0.5F + var8);
        }

        return super.collisionRayTrace(worldIn, pos, start, end);
    }

    @Override
    public boolean onUseWrench(World world, BlockPos pos, EntityPlayer entityPlayer, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote)
        {
	    	TileEntity tile = world.getTileEntity(pos);
	        if (tile instanceof TileEntityArclamp)
	        {
	            ((TileEntityArclamp) tile).facingChanged();
	        }
        }
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEntityArclamp();
    }

    @Override
    public CreativeTabs getCreativeTabToDisplayOn()
    {
        return GalacticraftCore.galacticraftBlocksTab;
    }

    @Override
    public String getShiftDescription(int meta)
    {
        return GCCoreUtil.translate(this.getUnlocalizedName() + ".description");
    }

    @Override
    public boolean showDescription(int meta)
    {
        return true;
    }

    public IBlockState getStateFromMeta(int meta)
    {
        EnumFacing enumfacing = EnumFacing.getFront(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    public int getMetaFromState(IBlockState state)
    {
        return ((EnumFacing)state.getValue(FACING)).getIndex();
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] {FACING});
    }
}
