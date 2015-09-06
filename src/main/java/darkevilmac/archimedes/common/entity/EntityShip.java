package darkevilmac.archimedes.common.entity;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import darkevilmac.archimedes.ArchimedesShipMod;
import darkevilmac.archimedes.client.control.ShipControllerClient;
import darkevilmac.archimedes.common.ArchimedesConfig;
import darkevilmac.archimedes.common.control.ShipControllerCommon;
import darkevilmac.archimedes.common.object.block.AnchorPointLocation;
import darkevilmac.archimedes.common.tileentity.TileEntityEngine;
import darkevilmac.archimedes.common.tileentity.TileEntityHelm;
import darkevilmac.movingworld.common.chunk.assembly.AssembleResult;
import darkevilmac.movingworld.common.chunk.assembly.ChunkDisassembler;
import darkevilmac.movingworld.common.chunk.assembly.MovingWorldAssemblyInteractor;
import darkevilmac.movingworld.common.entity.EntityMovingWorld;
import darkevilmac.movingworld.common.entity.MovingWorldCapabilities;
import darkevilmac.movingworld.common.entity.MovingWorldHandlerCommon;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

import java.util.Set;

public class EntityShip extends EntityMovingWorld {

    public static final float BASE_FORWARD_SPEED = 0.005F, BASE_TURN_SPEED = 0.5F, BASE_LIFT_SPEED = 0.004F;
    public ShipCapabilities capabilities;
    private ShipControllerCommon controller;
    private MovingWorldHandlerCommon handler;
    private ShipAssemblyInteractor shipAssemblyInteractor;
    private boolean submerge;

    public EntityShip(World world) {
        super(world);
        capabilities = new ShipCapabilities(this, true);
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();

        if (worldObj != null) {
            if (!worldObj.isRemote) {
                boolean hasEngines = false;
                if (capabilities.getEngines() != null) {
                    if (capabilities.getEngines().isEmpty())
                        hasEngines = false;
                    else {
                        hasEngines = capabilities.getEnginePower() > 0;
                    }
                }
                if (ArchimedesShipMod.instance.modConfig.enginesMandatory)
                    getDataWatcher().updateObject(28, new Byte(hasEngines ? (byte) 1 : (byte) 0));
                else
                    getDataWatcher().updateObject(28, new Byte((byte) 1));

            } else {
                if (dataWatcher != null && !dataWatcher.getIsBlank() && dataWatcher.hasChanges()) {
                    submerge = dataWatcher.getWatchableObjectByte(26) == new Byte((byte) 1);
                }
            }
        }

    }

    public boolean getSubmerge() {
        return !dataWatcher.getIsBlank() ? (dataWatcher.getWatchableObjectByte(26) == (byte) 1) : false;
    }

    public void setSubmerge(boolean submerge) {
        this.submerge = submerge;
        if (worldObj != null && !worldObj.isRemote) {
            dataWatcher.updateObject(26, submerge ? new Byte((byte) 1) : new Byte((byte) 0));
            if (getMobileChunk().marker != null && getMobileChunk().marker.tileEntity != null && getMobileChunk().marker.tileEntity instanceof TileEntityHelm) {
                TileEntityHelm helm = (TileEntityHelm) getMobileChunk().marker.tileEntity;

                helm.submerge = submerge;
            }
        }
    }

    @Override
    public AxisAlignedBB getCollisionBox(Entity entity) {
        return entity instanceof EntitySeat || entity.ridingEntity instanceof EntitySeat || entity instanceof EntityLiving ? null : entity.boundingBox;
    }

    public int getBelowWater() {
        byte b0 = 5;
        int blocksPerMeter = (int) (b0 * (getBoundingBox().maxY - getBoundingBox().minY));
        AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(0D, 0D, 0D, 0D, 0D, 0D);
        int belowWater = 0;
        for (; belowWater < blocksPerMeter; belowWater++) {
            double d1 = getBoundingBox().minY + (getBoundingBox().maxY - getBoundingBox().minY) * belowWater / blocksPerMeter;
            double d2 = getBoundingBox().minY + (getBoundingBox().maxY - getBoundingBox().minY) * (belowWater + 1) / blocksPerMeter;
            axisalignedbb.setBounds(boundingBox.minX, d1, boundingBox.minZ, boundingBox.maxX, d2, boundingBox.maxZ);

            if (!isAABBInLiquidNotFall(worldObj, axisalignedbb)) {
                break;
            }
        }

        return belowWater;
    }

    public boolean areSubmerged() {
        int belowWater = getBelowWater();

        return getSubmerge() && belowWater > 0;
    }

    @Override
    public boolean isFlying() {
        return (capabilities.canFly() && (isFlying || controller.getShipControl() == 2)) || getSubmerge();
    }

    @Override
    public MovingWorldHandlerCommon getHandler() {
        if (handler == null) {
            if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
                handler = new ShipHandlerClient(this);
                handler.setMovingWorld(this);
            } else {
                handler = new ShipHandlerServer(this);
                handler.setMovingWorld(this);
            }
        }
        return handler;
    }

    @Override
    public void initMovingWorld() {
        getCapabilities();
        dataWatcher.addObject(29, 0F); // Engine power
        dataWatcher.addObject(28, new Byte((byte) 0)); // Do we have any engines
        dataWatcher.addObject(27, new Byte((byte) 0)); // Can we be submerged if wanted?
        dataWatcher.addObject(26, new Byte((byte) 0)); // Are we submerged?
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void initMovingWorldClient() {
        handler = new ShipHandlerClient(this);
        controller = new ShipControllerClient();
    }

    @Override
    public void initMovingWorldCommon() {
        handler = new ShipHandlerServer(this);
        controller = new ShipControllerCommon();
    }

    @Override
    public MovingWorldCapabilities getCapabilities() {
        return this.capabilities == null ? new ShipCapabilities(this, true) : this.capabilities;
    }

    @Override
    public void setCapabilities(MovingWorldCapabilities capabilities) {
        if (capabilities != null && capabilities instanceof ShipCapabilities) {
            this.capabilities = (ShipCapabilities) capabilities;
        }
    }

    /**
     * Aligns to the closest anchor within 16 blocks.
     *
     * @return
     */
    public boolean alignToAnchor() {
        if (capabilities.findClosestValidAnchor(16) != null) {
            AnchorPointLocation anchorPointLocation = capabilities.findClosestValidAnchor(16);
            ChunkPosition chunkAnchorPos = anchorPointLocation.shipAnchor.coords;
            ChunkPosition worldAnchorPos = anchorPointLocation.worldAnchor.coords;
            Vec3 worldPosForAnchor = Vec3.createVectorHelper(worldAnchorPos.chunkPosX, worldAnchorPos.chunkPosY + 2, worldAnchorPos.chunkPosZ);
            worldPosForAnchor = worldPosForAnchor.addVector(getMobileChunk().getCenterX(), getMobileChunk().minY(), getMobileChunk().getCenterZ());
            worldPosForAnchor = Vec3.createVectorHelper(worldPosForAnchor.xCoord - chunkAnchorPos.chunkPosX, worldPosForAnchor.yCoord, worldPosForAnchor.zCoord - chunkAnchorPos.chunkPosZ);
            setPosition(worldPosForAnchor.xCoord, worldPosForAnchor.yCoord, worldPosForAnchor.zCoord);
        }

        alignToGrid();
        return false;
    }

    @Override
    public boolean isBraking() {
        return controller.getShipControl() == 3;
    }

    @Override
    public MovingWorldAssemblyInteractor getNewAssemblyInteractor() {
        return new ShipAssemblyInteractor();
    }

    @Override
    public void writeMovingWorldNBT(NBTTagCompound compound) {
        compound.setBoolean("submerge", submerge);
    }

    @Override
    public void readMovingWorldNBT(NBTTagCompound compound) {
        setSubmerge(compound.getBoolean("submerge"));
    }


    @Override
    public void writeMovingWorldSpawnData(ByteBuf data) {
    }

    @Override
    public void handleControl(double horizontalVelocity) {
        capabilities.updateEngines();

        if (riddenByEntity == null) {
            if (prevRiddenByEntity != null) {
                if (ArchimedesShipMod.instance.modConfig.disassembleOnDismount) {
                    alignToAnchor();
                    updateRiderPosition(prevRiddenByEntity, riderDestinationX, riderDestinationY, riderDestinationZ, 1);
                    disassemble(false);
                } else {
                    if (!worldObj.isRemote && isFlying()) {
                        EntityParachute parachute = new EntityParachute(worldObj, this, riderDestinationX, riderDestinationY, riderDestinationZ);
                        if (worldObj.spawnEntityInWorld(parachute)) {
                            prevRiddenByEntity.mountEntity(parachute);
                            prevRiddenByEntity.setSneaking(false);
                        }
                    }
                }
                prevRiddenByEntity = null;
            }
        }

        if (riddenByEntity == null || !capabilities.canMove()) {
            if (isFlying()) {
                motionY -= BASE_LIFT_SPEED * 0.2F;
            }
        } else {
            handlePlayerControl();
            prevRiddenByEntity = riddenByEntity;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void spawnParticles(double horvel) {
        if (capabilities.getEngines() != null) {
            Vec3 vec = Vec3.createVectorHelper(0d, 0d, 0d);
            float yaw = (float) Math.toRadians(rotationYaw);
            for (TileEntityEngine engine : capabilities.getEngines()) {
                if (engine.isRunning()) {
                    vec.xCoord = engine.xCoord - getMovingWorldChunk().getCenterX() + 0.5f;
                    vec.yCoord = engine.yCoord;
                    vec.zCoord = engine.zCoord - getMovingWorldChunk().getCenterZ() + 0.5f;
                    vec.rotateAroundY(yaw);
                    worldObj.spawnParticle("smoke", posX + vec.xCoord, posY + vec.yCoord + 1d, posZ + vec.zCoord, 0d, 0d, 0d);
                }
            }
        }
    }

    @Override
    public void handleServerUpdate(double horizontalVelocity) {
        boolean submergeMode = getSubmerge();

        byte b0 = 5;
        int blocksPerMeter = (int) (b0 * (getBoundingBox().maxY - getBoundingBox().minY));
        float waterVolume = 0F;
        AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(0D, 0D, 0D, 0D, 0D, 0D);
        int belowWater = 0;
        for (; belowWater < blocksPerMeter; belowWater++) {
            double d1 = getBoundingBox().minY + (getBoundingBox().maxY - getBoundingBox().minY) * belowWater / blocksPerMeter;
            double d2 = getBoundingBox().minY + (getBoundingBox().maxY - getBoundingBox().minY) * (belowWater + 1) / blocksPerMeter;
            axisalignedbb.setBounds(getBoundingBox().minX, d1, getBoundingBox().minZ, getBoundingBox().maxX, d2, getBoundingBox().maxZ);

            if (!isAABBInLiquidNotFall(worldObj, axisalignedbb)) {
                break;
            }
        }
        if (belowWater > 0 && layeredBlockVolumeCount != null) {
            int k = belowWater / b0;
            for (int y = 0; y <= k && y < layeredBlockVolumeCount.length; y++) {
                if (y == k) {
                    waterVolume += layeredBlockVolumeCount[y] * (belowWater % b0) * 1F / b0;
                } else {
                    waterVolume += layeredBlockVolumeCount[y] * 1F;
                }
            }
        }

        if (onGround) {
            isFlying = false;
        }

        float gravity = 0.05F;
        if (waterVolume > 0F && !submergeMode) {
            isFlying = false;
            float buoyancyforce = 1F * waterVolume * gravity; //F = rho * V * g (Archimedes' principle)
            float mass = getCapabilities().getMass();
            motionY += buoyancyforce / mass;
        }

        if (!isFlying() || (submergeMode && belowWater <= (getMobileChunk().maxY() * 5 / 3 * 2))) {
            motionY -= gravity;
        }

        super.handleServerUpdate(horizontalVelocity);
    }

    @Override
    public void handleServerUpdatePreRotation() {
        if (ArchimedesShipMod.instance.modConfig.shipControlType == ArchimedesConfig.CONTROL_TYPE_VANILLA) {
            double newyaw = rotationYaw;
            double dx = prevPosX - posX;
            double dz = prevPosZ - posZ;

            if (riddenByEntity != null && !isBraking() && dx * dx + dz * dz > 0.01D) {
                newyaw = 270F - Math.toDegrees(Math.atan2(dz, dx)) + frontDirection * 90F;
            }

            double deltayaw = MathHelper.wrapAngleTo180_double(newyaw - rotationYaw);
            double maxyawspeed = 2D;
            if (deltayaw > maxyawspeed) {
                deltayaw = maxyawspeed;
            }
            if (deltayaw < -maxyawspeed) {
                deltayaw = -maxyawspeed;
            }

            rotationYaw = (float) (rotationYaw + deltayaw);
        }
    }

    @Override
    public void updateRiderPosition(Entity entity, int x, int y, int z, int flags) {
        super.updateRiderPosition(entity, x, y, z, flags);

        if (submerge && entity != null && entity instanceof EntityLivingBase && worldObj != null && !worldObj.isRemote) {
            if (!((EntityLivingBase) entity).isPotionActive(Potion.waterBreathing))
                ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.waterBreathing.id, 20, 1));
        }
    }

    @Override
    public boolean disassemble(boolean overwrite) {
        if (worldObj.isRemote) return true;

        alignToGrid();
        updateRiderPosition();
        ChunkDisassembler disassembler = getDisassembler();
        disassembler.overwrite = overwrite;

        if (!disassembler.canDisassemble(getNewAssemblyInteractor())) {
            if (prevRiddenByEntity instanceof EntityPlayer) {
                ChatComponentText c = new ChatComponentText("Cannot disassemble ship here");
                ((EntityPlayer) prevRiddenByEntity).addChatMessage(c);
            }
            return false;
        }

        AssembleResult result = disassembler.doDisassemble(getNewAssemblyInteractor());

        if (result.getShipMarker() != null) {
            TileEntity te = result.getShipMarker().tileEntity;
            if (te instanceof TileEntityHelm) {
                ((TileEntityHelm) te).setAssembleResult(result);
                ((TileEntityHelm) te).setInfo(getInfo());
            }
        }

        return true;
    }

    private void handlePlayerControl() {
        if (riddenByEntity instanceof EntityLivingBase && ((ShipCapabilities) getCapabilities()).canMove()) {
            double throttle = ((EntityLivingBase) riddenByEntity).moveForward;
            if (isFlying()) {
                throttle *= 0.5D;
            }

            if (ArchimedesShipMod.instance.modConfig.shipControlType == ArchimedesConfig.CONTROL_TYPE_ARCHIMEDES) {
                Vec3 vec = Vec3.createVectorHelper(riddenByEntity.motionX, 0D, riddenByEntity.motionZ);
                vec.rotateAroundY((float) Math.toRadians(riddenByEntity.rotationYaw));

                double steer = ((EntityLivingBase) riddenByEntity).moveStrafing;

                motionYaw += steer * BASE_TURN_SPEED * capabilities.getRotationMult() * ArchimedesShipMod.instance.modConfig.turnSpeed;

                float yaw = (float) Math.toRadians(180F - rotationYaw + frontDirection * 90F);
                vec.xCoord = motionX;
                vec.zCoord = motionZ;
                vec.rotateAroundY(yaw);
                vec.xCoord *= 0.9D;
                vec.zCoord -= throttle * BASE_FORWARD_SPEED * capabilities.getSpeedMult();
                vec.rotateAroundY(-yaw);

                motionX = vec.xCoord;
                motionZ = vec.zCoord;

            } else if (ArchimedesShipMod.instance.modConfig.shipControlType == ArchimedesConfig.CONTROL_TYPE_VANILLA) {
                if (throttle > 0.0D) {
                    double dsin = -Math.sin(Math.toRadians(riddenByEntity.rotationYaw));
                    double dcos = Math.cos(Math.toRadians(riddenByEntity.rotationYaw));
                    motionX += dsin * BASE_FORWARD_SPEED * capabilities.speedMultiplier;
                    motionZ += dcos * BASE_FORWARD_SPEED * capabilities.speedMultiplier;
                }
            }
        }

        if (controller.getShipControl() != 0) {
            if (controller.getShipControl() == 4) {
                alignToAnchor();
            } else if (isBraking()) {
                motionX *= capabilities.brakeMult;
                motionZ *= capabilities.brakeMult;
                if (isFlying()) {
                    motionY *= capabilities.brakeMult;
                }
            } else if (controller.getShipControl() < 3 && capabilities.canFly()) {
                int i;
                if (controller.getShipControl() == 2) {
                    isFlying = true;
                    i = 1;
                } else {
                    i = -1;
                }
                motionY += i * BASE_LIFT_SPEED * capabilities.getLiftMult();
            }
        }
    }

    @Override
    public void readMovingWorldSpawnData(ByteBuf data) {
    }

    @Override
    public float getXRenderScale() {
        return 1.000001F;
    }

    @Override
    public float getYRenderScale() {
        return 1.000001F;
    }

    @Override
    public float getZRenderScale() {
        return 1.000001F;
    }

    @Override
    public MovingWorldAssemblyInteractor getAssemblyInteractor() {
        return shipAssemblyInteractor;
    }

    @Override
    public void setAssemblyInteractor(MovingWorldAssemblyInteractor interactor) {
        //shipAssemblyInteractor = (ShipAssemblyInteractor) interactor;
        //interactor.transferToCapabilities(getCapabilities());
    }

    public void fillAirBlocks(Set<ChunkPosition> set, int x, int y, int z) {
        super.fillAirBlocks(set, x, y, z);
    }

    public ShipControllerCommon getController() {
        return controller;
    }

    public boolean canSubmerge() {
        return !dataWatcher.getIsBlank() ? dataWatcher.getWatchableObjectByte(27) == new Byte((byte) 1) : false;
    }
}