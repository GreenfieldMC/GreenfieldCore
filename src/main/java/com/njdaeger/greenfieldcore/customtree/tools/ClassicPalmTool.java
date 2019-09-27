package com.njdaeger.greenfieldcore.customtree.tools;

import com.njdaeger.greenfieldcore.Util;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.BlockTool;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.BooleanProperty;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.registry.BlockMaterial;

import java.util.Arrays;

public class ClassicPalmTool implements BlockTool {

    private static final int MIN_TRUNK = 8; //minimum tree trunk height
    private static final int LEAVES_HEIGHT = 3; //how far the leaves extend above the top of the trunk
    private static final int MAX_BOUND = 14;

    @Override
    public boolean actPrimary(Platform platform, LocalConfiguration localConfiguration, Player player, LocalSession localSession, Location location) {
        int trunkSize = generateSize(location, MAX_BOUND) - LEAVES_HEIGHT;
        if (trunkSize < 8) {
            player.printError("A tree can't go there.");
            return true;
        }

        BlockState log = BlockTypes.OAK_LOG.getDefaultState();
        BlockState leaves = BlockTypes.BIRCH_LEAVES.getDefaultState().with(new BooleanProperty("persistent", Arrays.asList(true, false)), true);

        BlockVector3 position = BlockVector3.at(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ());

        EditSession session =  localSession.createEditSession(player);
        try {
            for (int y = 0; y < trunkSize; y++) {
                session.setBlock(position.add(0, y, 0), log);
            }
            session.flushSession();
            position = position.add(0, trunkSize-1, 0);

            //Setting the first layer
            setArea(session, position, leaves, 1, true);
            setArea(session, position.add(0, 1, 0), leaves, 1, false);
            setArea(session, position.add(0, 2, 0), leaves, 2, true);
            setArea(session, position.add(0, 3, 0), leaves, 1, true);

        } catch (MaxChangedBlocksException e) {
            player.printError("Max blocks changed reached.");
        } finally {
            session.flushSession();
            localSession.remember(session);
        }

        return true;
    }

    @Override
    public boolean canUse(Actor actor) {
        return actor.hasPermission("worldedit.tool.tree");
    }

    private static int generateSize(Location location, int bound) {
        int treeHeight = MIN_TRUNK + (Util.RANDOM.nextInt(bound + 1)) + LEAVES_HEIGHT;

        if (bound < MAX_BOUND) return treeHeight; //We know if the bound is less than 14 the blocks below it have already been checked.

        BlockVector3 blockVector = BlockVector3.at(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ());
        Extent extent = location.getExtent();

        for (int y = 0; y < treeHeight; y++) {
            BlockType type = extent.getFullBlock(blockVector).getBlockType();
            boolean isAir = type.getMaterial().isAir() || BlockCategories.LEAVES.contains(type);
            if ((y < (MIN_TRUNK + LEAVES_HEIGHT)) && !isAir) return -1; //If we are less than the minimum height of a palm
            if (!isAir) return generateSize(location, (y + LEAVES_HEIGHT) - bound);
            else blockVector = blockVector.add(0, 1, 0);
        }
        return treeHeight;

    }

    private static <B extends BlockStateHolder<B>> void setArea(EditSession session, BlockVector3 around, B block, int radius, boolean cutCorners) throws MaxChangedBlocksException {
        around = around.subtract(radius, 0, radius);
        for (int x = 0; x < ((radius * 2) + 1); x++) {
            for (int z = 0; z < ((radius * 2) + 1); z++) {
                //If we are trimming corners...
                // - if x and z both == 0, cut corner
                // - if x or z == radius*2 AND (x + z == radius*2 OR x + z == 2 * (radius*radius)) cut the corner
                if (cutCorners && (x == 0 && z == 0 || (x == radius * 2 || z == radius * 2) && (x + z == radius * 2 || x + z == ((radius == 1 ? 2 : 1)*(2 * (radius * radius)))))) {
                    continue;
                }
                setBlockIfAir(session, around.add(x, 0, z), block);
            }
        }
    }

    private static <B extends BlockStateHolder<B>> void setBlockIfAir(EditSession session, BlockVector3 position, B block) throws MaxChangedBlocksException {
        BlockType type = session.getBlock(position).getBlockType();
        BlockMaterial material = type.getMaterial();
        if (material.isAir() || BlockCategories.LEAVES.contains(type)) session.setBlock(position, block);
    }

}
