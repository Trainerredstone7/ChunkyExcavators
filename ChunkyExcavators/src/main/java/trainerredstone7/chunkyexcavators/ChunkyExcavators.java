package trainerredstone7.chunkyexcavators;

import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.MultiblockHandler.MultiblockFormEvent;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockExcavator;
import blusunrize.immersiveengineering.common.util.Utils;

@Mod(modid = ChunkyExcavators.MODID, name = ChunkyExcavators.NAME, version = ChunkyExcavators.VERSION)
public class ChunkyExcavators
{
    public static final String MODID = "chunkyexcavators";
    public static final String NAME = "Chunky Excavators";
    public static final String VERSION = "0.1";

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
        logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }
    
    @EventHandler
    private void checkForExcavators(MultiblockFormEvent.Post event) {
    	/*
    	 * Check on the bucketwheel, not the excavator since the center of the bucketwheel
    	 * dictates the chunk the excavator mines from
    	 * TODO: Use chunk heightmap to reduce number of blocks that need to be checked
    	 * TODO: Dissassemble attached excavator if the full multiblock can't form
    	 */
    	if (event.getMultiblock().getUniqueName() == "IE:BucketWheel") {
//    		Chunk excavatorChunk = event.getEntityPlayer().getEntityWorld().getChunkFromBlockCoords(event.getClickedBlock());
    		BlockPos chunkCorner = getChunkCornerPos(event.getClickedBlock());
    		World world = event.getEntityPlayer().getEntityWorld();
    	}
    }
    
    /**
     * Gives the corner of the chunk containing the BlockPos with minimum x, y, and z coordinates.
     */
    private BlockPos getChunkCornerPos(BlockPos pos) {
    	return new BlockPos(pos.getX() - Math.floorMod(pos.getX(), 16), 0, pos.getZ() - Math.floorMod(pos.getZ(), 16));
    }
    
    /*
     * gives vertical spacing between positions that need to be checked within the chunk
     * 0's indicate that no blocks need to be checked in that vertical column
     */
    private static int[][] positionCheckKey = 
    	{{7, 0, 0, 0, 7, 0, 7, 0, 0, 0, 7, 0, 7, 0, 0, 0},
    	 {0, 0, 3, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 0},
    	 {3, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0},
    	 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0},
    	 {7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    	 {0, 0, 3, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 0},
    	 {3, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0},
    	 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0},
    	 {7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    	 {0, 0, 3, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 0},
    	 {3, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0},
    	 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0},
    	 {7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    	 {0, 0, 3, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 0},
    	 {3, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0},
    	 {0, 0, 0, 7, 0, 0, 0, 7, 0, 7, 0, 0, 0, 0, 3, 0}};
}
