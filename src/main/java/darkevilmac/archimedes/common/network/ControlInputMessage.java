package darkevilmac.archimedes.common.network;

import darkevilmac.archimedes.common.entity.EntityShip;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

public class ControlInputMessage extends ShipMessage {
    public int control;

    public ControlInputMessage() {
        super();
        control = 0;
    }

    public ControlInputMessage(EntityShip entityship, int controlid) {
        super(entityship);
        control = controlid;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buf, Side side) {
        super.encodeInto(ctx, buf, side);
        buf.writeByte(control);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buf,  Side side) {
        super.decodeInto(ctx, buf, side);
        control = buf.readByte();
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
    }

    @Override
    public void handleServerSide(EntityPlayer player) {
        if (ship != null) {
            ship.getController().updateControl(ship, player, control);
        }
    }

}
