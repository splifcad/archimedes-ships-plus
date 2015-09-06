package darkevilmac.archimedes.common.object.block;

import darkevilmac.archimedes.ArchimedesShipMod;
import darkevilmac.archimedes.common.network.TranslatedChatMessage;
import darkevilmac.archimedes.common.tileentity.TileEntityAnchorPoint;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockAnchorPoint extends BlockContainer {

    public BlockAnchorPoint(Material material) {
        super(material);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
        if (world != null && player != null && !world.isRemote) {
            if (world.getTileEntity(x, y, z) != null && world.getTileEntity(x, y, z) instanceof TileEntityAnchorPoint) {
                TileEntityAnchorPoint tile = (TileEntityAnchorPoint) world.getTileEntity(x, y, z);
                if (tile.anchorPointInfo == null)
                    tile.setAnchorPointInfo(0, 0, 0, false);
                if (player.isSneaking()) {
                    tile.anchorPointInfo.forShip = !tile.anchorPointInfo.forShip;
                    ArchimedesShipMod.instance.network.sendTo(new TranslatedChatMessage("TR:" + (tile.anchorPointInfo.forShip ? "common.tile.anchor.changeModeShip" : "common.tile.anchor.changeModeGround") + "~ "), (EntityPlayerMP) player);
                } else {
                    if (tile.anchorPointInfo.forShip) {
                        if (player.getEntityData().getBoolean("SelectedShipData")) {
                            int[] selectedShipPos = player.getEntityData().getIntArray("SelectedShipAnchorPos");
                            tile.setAnchorPointInfo(selectedShipPos[0], selectedShipPos[1], selectedShipPos[2], tile.anchorPointInfo.forShip);
                            ArchimedesShipMod.instance.network.sendTo(new TranslatedChatMessage("TR:" + "common.tile.anchor.activateShip" + "~ X:" + selectedShipPos[0] + " Y:" + selectedShipPos[1] + " Z:" + selectedShipPos[2]), (EntityPlayerMP) player);
                        } else {
                            ArchimedesShipMod.instance.network.sendTo(new TranslatedChatMessage("TR:" + "common.tile.anchor.noGroundLink"), (EntityPlayerMP) player);
                        }
                    } else {
                        player.getEntityData().setIntArray("SelectedShipAnchorPos", new int[]{x, y, z});
                        player.getEntityData().setBoolean("SelectedShipData", true);
                        ArchimedesShipMod.instance.network.sendTo(new TranslatedChatMessage("TR:" + "common.tile.anchor.activateGround"), (EntityPlayerMP) player);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityAnchorPoint();
    }
}
