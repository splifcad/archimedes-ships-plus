package darkevilmac.archimedes.common.tileentity;

import darkevilmac.movingworld.common.entity.EntityMovingWorld;
import darkevilmac.movingworld.common.tile.IMovingWorldTileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityAnchorPoint extends TileEntity implements IMovingWorldTileEntity {

    public AnchorPointInfo anchorPointInfo;
    private EntityMovingWorld activeShip;

    public TileEntityAnchorPoint() {
        super();
        activeShip = null;
    }

    public void setAnchorPointInfo(int x, int y, int z, boolean forShip) {
        if (anchorPointInfo == null)
            anchorPointInfo = new AnchorPointInfo();
        anchorPointInfo.setInfo(x, y, z, forShip);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (worldObj != null && tag.hasKey("vehicle") && worldObj != null) {
            int id = tag.getInteger("vehicle");
            Entity entity = worldObj.getEntityByID(id);
            if (entity != null && entity instanceof EntityMovingWorld) {
                activeShip = (EntityMovingWorld) entity;
            }
        }
        if (tag.getBoolean("hasAnchorInfo") && anchorPointInfo == null) {
            anchorPointInfo = new AnchorPointInfo(tag.getInteger("linkX"), tag.getInteger("linkY"), tag.getInteger("linkZ"), tag.getBoolean("forShip"));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        if (anchorPointInfo != null) {
            tag.setInteger("linkX", anchorPointInfo.x);
            tag.setInteger("linkY", anchorPointInfo.y);
            tag.setInteger("linkZ", anchorPointInfo.z);
            tag.setBoolean("forShip", anchorPointInfo.forShip);
            tag.setBoolean("hasAnchorInfo", true);
        } else {
            tag.setBoolean("hasAnchorInfo", false);
        }
        if (activeShip != null && !activeShip.isDead) {
            tag.setInteger("vehicle", activeShip.getEntityId());
        }
    }

    @Override
    public void setParentMovingWorld(EntityMovingWorld entityMovingWorld, int x, int y, int z) {
        activeShip = entityMovingWorld;
    }

    @Override
    public EntityMovingWorld getParentMovingWorld() {
        return activeShip;
    }

    @Override
    public void setParentMovingWorld(EntityMovingWorld entityMovingWorld) {
        setParentMovingWorld(entityMovingWorld, 0, 0, 0);
    }

    public class AnchorPointInfo {
        public int x, y, z;
        public boolean forShip;

        public AnchorPointInfo() {
            x = y = z = 0;
            forShip = false;
        }

        public AnchorPointInfo(int x, int y, int z, boolean forShip) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.forShip = forShip;
        }

        public void setInfo(int x, int y, int z, boolean forShip) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.forShip = forShip;
        }

        public AnchorPointInfo clone() {
            return new AnchorPointInfo(x, y, z, forShip);
        }
    }

}
