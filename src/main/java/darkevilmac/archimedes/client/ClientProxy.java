package darkevilmac.archimedes.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import darkevilmac.archimedes.client.handler.ClientHookContainer;
import darkevilmac.archimedes.common.ArchimedesConfig;
import darkevilmac.archimedes.common.CommonProxy;
import darkevilmac.archimedes.common.object.block.BlockGauge;
import darkevilmac.archimedes.common.object.block.BlockSeat;
import darkevilmac.archimedes.common.tileentity.TileEntityGauge;
import darkevilmac.archimedes.client.control.ShipKeyHandler;
import darkevilmac.archimedes.common.entity.EntityParachute;
import darkevilmac.archimedes.common.entity.EntityShip;
import darkevilmac.archimedes.client.render.RenderBlockGauge;
import darkevilmac.archimedes.client.render.RenderBlockSeat;
import darkevilmac.archimedes.client.render.RenderParachute;
import darkevilmac.archimedes.client.render.TileEntityGaugeRenderer;
import darkevilmac.movingworld.client.render.RenderMovingWorld;

public class ClientProxy extends CommonProxy {
    public ShipKeyHandler shipKeyHandler;

    @Override
    public ClientHookContainer getHookContainer() {
        return new ClientHookContainer();
    }

    @Override
    public void registerKeyHandlers(ArchimedesConfig cfg) {
        shipKeyHandler = new ShipKeyHandler(cfg);
        FMLCommonHandler.instance().bus().register(shipKeyHandler);
    }

    @Override
    public void registerRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntityShip.class, new RenderMovingWorld());
        RenderingRegistry.registerEntityRenderingHandler(EntityParachute.class, new RenderParachute());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGauge.class, new TileEntityGaugeRenderer());
        //ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHelm.class, new TileEntityHelmRenderer());
        BlockGauge.gaugeBlockRenderID = RenderingRegistry.getNextAvailableRenderId();
        BlockSeat.seatBlockRenderID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(BlockSeat.seatBlockRenderID, new RenderBlockSeat());
        RenderingRegistry.registerBlockHandler(BlockGauge.gaugeBlockRenderID, new RenderBlockGauge());
    }
}
