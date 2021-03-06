package darkevilmac.archimedes.common.network;

import darkevilmac.archimedes.client.gui.ContainerHelm;
import darkevilmac.archimedes.common.entity.ShipAssemblyInteractor;
import darkevilmac.movingworld.common.chunk.assembly.AssembleResult;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

public class AssembleResultMessage extends ArchimedesShipsMessage {
    public AssembleResult result;
    public ShipAssemblyInteractor interactor;
    public boolean prevFlag;

    public AssembleResultMessage() {
        result = null;
        prevFlag = false;
    }

    public AssembleResultMessage(AssembleResult compileResult, boolean prev) {
        result = compileResult;
        prevFlag = prev;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buf, Side side) {
        buf.writeBoolean(prevFlag);
        if (result == null) {
            buf.writeByte(AssembleResult.RESULT_NONE);
        } else {
            buf.writeByte(result.getCode());
            buf.writeInt(result.getBlockCount());
            buf.writeInt(result.getTileEntityCount());
            buf.writeFloat(result.getMass());
            result.assemblyInteractor.toByteBuf(buf);
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buf, Side side) {
        if (side.isClient()) {
            prevFlag = buf.readBoolean();
            byte resultCode = buf.readByte();
            result = new AssembleResult(resultCode, buf);
            result.assemblyInteractor = new ShipAssemblyInteractor().fromByteBuf(resultCode, buf);
        }
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
        if (player != null && player.openContainer instanceof ContainerHelm) {
            if (prevFlag) {
                ((ContainerHelm) player.openContainer).tileEntity.setPrevAssembleResult(result);
                ((ContainerHelm) player.openContainer).tileEntity.getPrevAssembleResult().assemblyInteractor = result.assemblyInteractor;
            } else {
                ((ContainerHelm) player.openContainer).tileEntity.setAssembleResult(result);
                ((ContainerHelm) player.openContainer).tileEntity.getAssembleResult().assemblyInteractor = result.assemblyInteractor;
            }
        }
    }

    @Override
    public void handleServerSide(EntityPlayer player) {
    }

}

