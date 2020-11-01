package trainerredstone7.chunkyexcavators;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.apache.logging.log4j.Logger;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.MultiblockHandler.MultiblockFormEvent;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBucketWheel;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockExcavator;
import blusunrize.immersiveengineering.common.util.Utils;

@Mod(modid = ChunkyExcavators.MODID, name = ChunkyExcavators.NAME, version = ChunkyExcavators.VERSION)
@Mod.EventBusSubscriber
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
//        logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }
    
    @SubscribeEvent
    public static void checkForExcavators(MultiblockFormEvent.Post event) {
    	logger.info("called!");
    	/*
    	 * TODO: Use chunk heightmap to reduce number of blocks that need to be checked
    	 * TODO: Dissassemble attached excavator if the full multiblock can't form
    	 */
    	String multiblockType = event.getMultiblock().getUniqueName();
    	logger.info("multiblock name is " + multiblockType);
    	if (multiblockType == "IE:Excavator") {
    		logger.info("excavator code block");
    		/*
    		 * Why dissassemble adjacent bucketwheels?
    		 * 
    		 * The check for already existing excavators is only performed on bucketwheel multiblock formation.
    		 * This is because it's difficult to get the chunk an excavator will mine from the excavator itself,
    		 * as the chunk is determined by the wheel position and you can't get excavator orientation information
    		 * from the assembly event. By dissassembling the wheel if it already exists, we can check for existing
    		 * excavators during wheel multiblock assembly which is begun upon successful excavator multiblock assembly.
    		 * Otherwise, a player could build several wheels which wouldn't be detected by the checking algorithm,
    		 * then build the excavators around them.
    		 */
    		
    		/*
    		 * Idea: just check for excavators in all chunks occupied by adjacent bucketwheels - it will only
    		 * happen more than once in very unusual circumstances and in those cases the players are probably
    		 * trying to game the system anyways
    		 */
    		
    		World world = event.getEntityPlayer().getEntityWorld();
    		BlockPos excavatorPos = event.getClickedBlock();
    		TileEntity[] tes = {world.getTileEntity(excavatorPos.north()),
    						    world.getTileEntity(excavatorPos.south()),
    						    world.getTileEntity(excavatorPos.east()),
    						    world.getTileEntity(excavatorPos.west())};
    		for (TileEntity te : tes) {
    			if (te instanceof TileEntityBucketWheel) {
    				//This seems to mess up the center block of the wheel
    				((TileEntityBucketWheel) te).master().disassemble();
        			logger.info("disassembled!");
    			}
    		}
    	}
    	if (multiblockType == "IE:BucketWheel") {
    		Chunk excavatorChunk = event.getEntityPlayer().getEntityWorld().getChunkFromBlockCoords(event.getClickedBlock());
    		BlockPos chunkCorner = getChunkCornerPos(event.getClickedBlock());
    		World world = event.getEntityPlayer().getEntityWorld();
    		for (int x = 0; x < positionCheckKey.length; x++) {
    			for (int z = 0; z < positionCheckKey[x].length; z++) {
    				if (positionCheckKey[x][z] == 0) continue;
    				//subtract 1 from starting y value because lowest block is at y = 0
    				for (int y = positionCheckKey[x][z] - 1; y < 256; y += positionCheckKey[x][z]) {
    					TileEntity te = world.getTileEntity(chunkCorner.add(x, y, z));
    					logger.info("checked coordinate " + (chunkCorner.getX() + x) + " " + (chunkCorner.getY() + y) + " " + (chunkCorner.getZ() + z) + " ");
    					if (minesFromChunk(te, excavatorChunk) && !sameExcavator(te, event.getClickedBlock())) {
    						logger.info("found excavator");
    						//prevent bucketwheel formation since there's already an excavator in the chunk
    						event.setCanceled(true);
    						if (!world.isRemote) {
    							event.getEntityPlayer().sendMessage(new TextComponentString("There is already an excavator in this chunk!"));
    						}
    						return;
    					}
    				}
    			}
    		}
    	}
    }
    private static boolean minesFromChunk(TileEntity te, Chunk chunk) {
    	if (te instanceof TileEntityExcavator) return minesFromChunk((TileEntityExcavator) te, chunk);
    	if (te instanceof TileEntityBucketWheel) return minesFromChunk((TileEntityBucketWheel) te, chunk);
    	return false;
    }
    
	private static boolean minesFromChunk(TileEntityExcavator te, Chunk chunk) {
	    //TODO verify this works
		BlockPos wheelCenter = te.getBlockPosForPos(31);
    	return wheelCenter.getX() >> 4 == chunk.x && wheelCenter.getZ() >> 4 == chunk.z; //bitshift to always round negative
    }
	
    private static boolean minesFromChunk(TileEntityBucketWheel te, Chunk chunk) {
    	BlockPos wheelCenter = te.getOrigin();
    	return wheelCenter.getX() >> 4 == chunk.x && wheelCenter.getZ() >> 4 == chunk.z;
	}
    
    /**
     * Checks if the excavator the tile entity belongs to has its bucketwheel at the specified position.
     */
    private static boolean sameExcavator(TileEntity te, BlockPos blockPos) {
        /*
         * If an excavator has the same bucketwheel position it's the same excavator,
         * and if it doesn't it's different
         */
    	if (te instanceof TileEntityExcavator) return sameExcavator((TileEntityExcavator) te, blockPos);
    	if (te instanceof TileEntityBucketWheel) return sameExcavator((TileEntityBucketWheel) te, blockPos);
    	return false;
    }
    
    private static boolean sameExcavator(TileEntityExcavator te, BlockPos blockPos) {
    	return te.getBlockPosForPos(31).equals(blockPos); //get excavator wheel center and compare positions
    }

    private static boolean sameExcavator(TileEntityBucketWheel te, BlockPos blockPos) {
    	return te.getOrigin().equals(blockPos);
    }
    
    /**
     * Gives the corner of the chunk containing the BlockPos with minimum x, y, and z coordinates.
     */
    private static BlockPos getChunkCornerPos(BlockPos pos) {
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
