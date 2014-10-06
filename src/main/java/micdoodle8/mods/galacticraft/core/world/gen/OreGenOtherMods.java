package micdoodle8.mods.galacticraft.core.world.gen;

import micdoodle8.mods.galacticraft.api.event.wgen.GCCoreEventPopulate;
import micdoodle8.mods.galacticraft.api.vector.BlockTuple;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.blocks.GCBlocks;
import micdoodle8.mods.galacticraft.core.dimension.WorldProviderMoon;
import micdoodle8.mods.galacticraft.core.dimension.WorldProviderOrbit;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;
import micdoodle8.mods.galacticraft.core.util.GCLog;
import micdoodle8.mods.galacticraft.planets.mars.blocks.MarsBlocks;
import micdoodle8.mods.galacticraft.planets.mars.dimension.WorldProviderMars;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.ArrayList;
import java.util.Random;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class OreGenOtherMods
{
    private World worldObj;
    private Random randomGenerator;

    private int chunkX;
    private int chunkZ;

    private WorldGenerator oreGen;
    public static ArrayList<OreGenData> data = new ArrayList<OreGenData>();
    
    static
    {
        for (final String str : ConfigManagerCore.oregenIDs)
        {
            try
            {
            	int slash = str.indexOf('/');
            	String s;
            	int rarity = 0;  //0 = common  1 = uncommon  2 = rare
            	int depth = 0;   //0 = even   1 = deep   2 = shallow
            	boolean single = false;

            	if (slash >= 0)
            	{
            		s = str.substring(0, slash);
            		String params = str.substring(slash).toUpperCase();
            		if (params.contains("UNCOMMON")) rarity = 1;
            		else if (params.contains("RARE")) rarity = 2;
           		
            		if (params.contains("DEEP")) depth = 1;
            		else if (params.contains("SHALLOW")) depth = 2;
            		
            		if (params.contains("SINGLE")) single = true;
            	}
            	else s = str;
            	
            	BlockTuple bt = ConfigManagerCore.stringToBlock(s, "External Sealable IDs"); 
            	if (bt == null) continue;

    			int meta = bt.meta;
    			if (meta == -1) meta = 0;
    			
    			OreGenOtherMods.addOre(bt.block, meta, rarity, depth, single);
            }
            catch (final Exception e)
            {
                GCLog.severe("[config] External Sealable IDs: error parsing '" + str + "'. Must be in the form Blockname or BlockName:metadata followed by / parameters ");
            }
        }
    }
    
    public static void addOre(Block block, int meta, int rarity, int depth, boolean single)
    {
    	int clusters = 12;
    	int size = 4;
    	int min = 0;
    	int max = 64;
    	
    	switch(depth)
    	{
    	case 0:
    		//Evenly distributed
    		size = 6;
        	clusters = 20;
        	max = 80;
    		if (rarity == 1)
    		{
    			clusters = 9;
    			size = 4;
    		}
        	else if (rarity == 2)
        	{	
        		clusters = 6;
        		size = 3;
        		max = 96;
        	}
    		break;
    	case 1:
    		//Deep
    		size = 5;
        	clusters = 12;
        	max = 32;
    		if (rarity == 1)
    		{
    			clusters = 6;
    			size = 4;
    			max = 20;
    		}
        	else if (rarity == 2)
        	{	
        		clusters = 2;
        		size = 3;
        		max = 16;
        	}
    		break;
    	case 2:
    		//Shallow
    		size = 6;
        	clusters = 15;
        	min = 32;
        	max = 80;
    		if (rarity == 1)
    		{
    			clusters = 8;
    			size = 4;
    			min = 32;
    			max = 72;
    		}
        	else if (rarity == 2)
        	{	
        		clusters = 3;
        		size = 3;
        		min = 40;
        		max = 64;
        	}
    	}
    	
    	if (single)
    	{
    		size = 1;
    		clusters = (3 * clusters) / 2;
    	}
    	
    	OreGenData ore = new OreGenData(block, meta, clusters, size, min, max);
    	OreGenOtherMods.data.add(ore);
    }

    @SubscribeEvent
    public void onPlanetDecorated(GCCoreEventPopulate.Post event)
    {
    	World worldObj = event.worldObj;
    	Random rand = event.rand;
    	int chunkX = event.chunkX;
    	int chunkZ = event.chunkZ;
    	
    	WorldProvider prov = worldObj.provider;
    	if (!(prov instanceof IGalacticraftWorldProvider) || (prov instanceof WorldProviderOrbit))
    		return;
    	
    	Block stoneBlock = null;
    	int stoneMeta = 0;
    	
    	if (prov instanceof WorldProviderMoon)
    	{
    		stoneBlock = GCBlocks.blockMoon;
    		stoneMeta = 4;
    	}
    	else if (GalacticraftCore.isPlanetsLoaded && prov instanceof WorldProviderMars)
    	{
    		stoneBlock = MarsBlocks.marsBlock;
    		stoneMeta = 9;
    	}

    	if (stoneBlock == null) return;

    	for (OreGenData ore : OreGenOtherMods.data)
    	{
	        this.oreGen = new WorldGenMinableMeta(ore.oreBlock, ore.sizeCluster, ore.oreMeta, true, stoneBlock, stoneMeta);
	        this.genStandardOre1(ore.numClusters, this.oreGen, ore.minHeight, ore.maxHeight);
    	}    	
    }

    void genStandardOre1(int amountPerChunk, WorldGenerator worldGenerator, int minY, int maxY)
    {
        for (int var5 = 0; var5 < amountPerChunk; ++var5)
        {
            final int var6 = this.chunkX + this.randomGenerator.nextInt(16);
            final int var7 = this.randomGenerator.nextInt(maxY - minY) + minY;
            final int var8 = this.chunkZ + this.randomGenerator.nextInt(16);
            worldGenerator.generate(this.worldObj, this.randomGenerator, var6, var7, var8);
        }
    }
    
    public static class OreGenData
    {
    	public Block oreBlock = GCBlocks.blockMoon;
    	public int oreMeta = 0;
    	public int sizeCluster = 4;
    	public int numClusters = 8;
    	public int minHeight = 0;
    	public int maxHeight = 128;
    	
    	public OreGenData(Block block, int meta, int num, int cluster, int min, int max)
    	{
    		this.oreBlock = block;
    		this.oreMeta = meta;
    		this.sizeCluster = cluster;
    		this.numClusters = num;
    		this.minHeight = min;
    		this.maxHeight = max;
    	}
    	
    	public OreGenData(Block block, int meta, int num, int cluster)
    	{
    		this.oreBlock = block;
    		this.oreMeta = meta;
    		this.sizeCluster = cluster;
    		this.numClusters = num;
    		this.minHeight = 0;
    		this.maxHeight = 128;
    	}
    	
    	public OreGenData(Block block, int meta, int num)
    	{
    		this.oreBlock = block;
    		this.oreMeta = meta;
    		this.sizeCluster = 4;
    		this.numClusters = num;
    		this.minHeight = 0;
    		this.maxHeight = 128;
    	}
    	
    	public OreGenData readString(String s)
    	{
    		Block block = null;
    		int meta = 0;
    		int cluster = 4;
    		int num = 12;
    		int min = 0;
    		int max = 128;
    		
    		return new OreGenData(block, meta, num, cluster, min, max);	
    	}
    }
}
