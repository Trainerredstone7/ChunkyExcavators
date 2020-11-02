package trainerredstone7.chunkyexcavators;

import org.apache.logging.log4j.Logger;

import blusunrize.immersiveengineering.api.MultiblockHandler.MultiblockFormEvent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBucketWheel;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = ChunkyExcavators.MODID, name = ChunkyExcavators.NAME, version = ChunkyExcavators.VERSION)
@Mod.EventBusSubscriber
public class ChunkyExcavators
{
	public static final String MODID = "chunkyexcavators";
    public static final String NAME = "Chunky Excavators";
    public static final String VERSION = "1.0";
    public static final int EXCAVATOR_WHEEL_CENTER_POS = 31;
	
    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }
    
    /**
     * Checks if there's already another excavator in the chunk the new excavator will mine from,
     * and if so cancels the formation event.
     */
    @SubscribeEvent
    public static void excavatorRestrictionCheck(MultiblockFormEvent.Post event) {
    	String multiblockType = event.getMultiblock().getUniqueName();
    	if (multiblockType == "IE:Excavator") {
    		/*
    		 * Check for excavators in chunks occupied by adjacent bucketwheels, and cancel formation
    		 * if one is found
    		 */
    		World world = event.getEntityPlayer().getEntityWorld();
    		BlockPos excavatorPos = event.getClickedBlock();
    		TileEntity[] tes = {world.getTileEntity(excavatorPos.north()),
    						    world.getTileEntity(excavatorPos.south()),
    						    world.getTileEntity(excavatorPos.east()),
    						    world.getTileEntity(excavatorPos.west())};
    		for (TileEntity te : tes) {
    			if (te instanceof TileEntityBucketWheel) {
    				BlockPos wheelPos = ((TileEntityBucketWheel) te).getPos().add(-((TileEntityBucketWheel) te).offset[0], -((TileEntityBucketWheel) te).offset[1], -((TileEntityBucketWheel) te).offset[2]);
    				if (checkForOtherExcavators(wheelPos, world)) {
    					cancelFormation(event);
    					return;
    				}
    			}
    		}
    	}
    	if (multiblockType == "IE:BucketWheel") {
    		World world = event.getEntityPlayer().getEntityWorld();
    		if (checkForOtherExcavators(event.getClickedBlock(), world)) {
				cancelFormation(event);
				return;
    		}
    	}
    }

	private static void cancelFormation(MultiblockFormEvent.Post event) {
		event.setCanceled(true);
		logger.info("canceled excavator formation at " + event.getClickedBlock() + " in dim " + event.getEntityPlayer().getEntityWorld().provider.getDimension());
		if (!event.getEntityPlayer().getEntityWorld().isRemote) {
			event.getEntityPlayer().sendMessage(new TextComponentString("There is already an excavator in this chunk!"));
		}
	}
    
    /**
     * Checks for other excavators in the chunk, excluding ones with bucket wheels centered on the 
     * provided BlockPos. 
     */
    private static boolean checkForOtherExcavators(BlockPos wheelPos, World world) {
    	Chunk chunk = world.getChunkFromBlockCoords(wheelPos);
    	BlockPos chunkCorner = new BlockPos(chunk.x*16, 0, chunk.z*16);
		for (int x = 0; x < positionCheckKey.length; x++) {
			for (int z = 0; z < positionCheckKey[x].length; z++) {
				if (positionCheckKey[x][z] == 0) continue;
				//Positions above precipitation heightmap don't need to be checked (they can't have solid blocks)
				int maxHeight = chunk.getPrecipitationHeight(new BlockPos(x, 0, z)).getY();
				//subtract 1 from starting y value because lowest block is at y = 0
				for (int y = positionCheckKey[x][z] - 1; y < maxHeight; y += positionCheckKey[x][z]) {
					TileEntity te = world.getTileEntity(chunkCorner.add(x, y, z));
					if (minesFromChunk(te, chunk) && !sameExcavator(te, wheelPos)) {
						return true;
					}
				}
			}
		}
    	return false;
    }
    
    private static boolean minesFromChunk(TileEntity te, Chunk chunk) {
    	if (te instanceof TileEntityExcavator) return minesFromChunk((TileEntityExcavator) te, chunk);
    	if (te instanceof TileEntityBucketWheel) return minesFromChunk((TileEntityBucketWheel) te, chunk);
    	return false;
    }
    
	private static boolean minesFromChunk(TileEntityExcavator te, Chunk chunk) {
		BlockPos wheelCenter = te.getBlockPosForPos(EXCAVATOR_WHEEL_CENTER_POS);
    	return wheelCenter.getX() >> 4 == chunk.x && wheelCenter.getZ() >> 4 == chunk.z; //bitshift to always round negative
    }
	
    private static boolean minesFromChunk(TileEntityBucketWheel te, Chunk chunk) {
    	BlockPos wheelCenter = te.getPos().add(-te.offset[0], -te.offset[1], -te.offset[2]);
    	return wheelCenter.getX() >> 4 == chunk.x && wheelCenter.getZ() >> 4 == chunk.z;
	}
    
    /**
     * Checks if the excavator the tile entity belongs to has its bucketwheel at the specified position.
     */
    private static boolean sameExcavator(TileEntity te, BlockPos wheelPos) {
    	if (te instanceof TileEntityExcavator) return sameExcavator((TileEntityExcavator) te, wheelPos);
    	if (te instanceof TileEntityBucketWheel) return sameExcavator((TileEntityBucketWheel) te, wheelPos);
    	return false;
    }
    
    private static boolean sameExcavator(TileEntityExcavator te, BlockPos wheelPos) {
    	return te.getBlockPosForPos(EXCAVATOR_WHEEL_CENTER_POS).equals(wheelPos); //get excavator wheel center and compare positions
    }

    private static boolean sameExcavator(TileEntityBucketWheel te, BlockPos wheelPos) {
    	return te.getPos().add(-te.offset[0], -te.offset[1], -te.offset[2]).equals(wheelPos);
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
