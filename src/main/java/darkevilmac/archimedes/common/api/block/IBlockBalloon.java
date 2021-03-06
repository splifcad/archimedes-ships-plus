package darkevilmac.archimedes.common.api.block;

import net.minecraft.tileentity.TileEntity;

public interface IBlockBalloon {

    /**
     * How many balloon blocks is this block equivalent to?
     *
     * @param tileEntity null if not applicable.
     * @return
     */
    int getBalloonWorth(TileEntity tileEntity);

}
