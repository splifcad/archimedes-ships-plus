package darkevilmac.archimedes.common;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import darkevilmac.archimedes.ArchimedesShipMod;
import darkevilmac.archimedes.common.object.ArchimedesObjects;
import darkevilmac.movingworld.MovingWorld;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ArchimedesConfig {
    public static final int CONTROL_TYPE_VANILLA = 0, CONTROL_TYPE_ARCHIMEDES = 1;
    //Settings
    public boolean enableAirShips;
    public boolean enableSubmersibles;
    public int shipEntitySyncRate;
    //Mobile Chunk
    public int maxShipChunkBlocks;
    public float flyBalloonRatio;
    public float submersibleFillRatio;
    //Control
    public int shipControlType;
    public float turnSpeed;
    public float speedLimit;
    public float bankingMultiplier;
    @SideOnly(Side.CLIENT)
    public KeyBinding kbUp, kbDown, kbBrake, kbAlign, kbDisassemble, kbShipInv;
    public boolean disassembleOnDismount;
    public boolean enginesMandatory;
    public Set<String> balloonAlternatives;
    private Configuration config;

    public ArrayList<Block> seats;

    public ArchimedesConfig(Configuration configuration) {
        config = configuration;
        balloonAlternatives = new HashSet<String>();
        seats = new ArrayList<Block>();

        FMLCommonHandler.instance().bus().register(this); // For in game config reloads.
    }

    public void loadAndSave() {
        config.load();

        shipEntitySyncRate = config.get("settings", "sync_rate", 20, "The amount of ticks between a server-client synchronization. Higher numbers reduce network traffic. Lower numbers increase multiplayer experience. 20 ticks = 1 second").getInt();
        enableAirShips = config.get("settings", "enable_air_ships", true, "Enable or disable air ships.").getBoolean(true);
        enableSubmersibles = config.get("settings", "enable_submersibles", true, "Enable or disable the ability to submerse ships.").getBoolean(true);
        bankingMultiplier = (float) config.get("settings", "banking_multiplier", 3d, "A multiplier for how much ships bank while making turns. Set a positive value for passive banking or a negative value for active banking. 0 disables banking.").getDouble(3d);
        enginesMandatory = config.get("settings", "mandatory_engines", false, "Are engines required for a ship to move?").getBoolean();

        shipControlType = config.get("control", "control_type", CONTROL_TYPE_ARCHIMEDES, "Set to 0 to use vanilla boat controls, set to 1 to use the new Archimedes controls.").getInt();
        turnSpeed = (float) config.get("control", "turn_speed", 1D, "A multiplier of the ship's turn speed.").getDouble(1D);
        speedLimit = (float) config.get("control", "speed_limit", 30D, "The maximum velocity a ship can have, in objects per second. This does not affect acceleration.").getDouble(30D);
        speedLimit /= 20F;
        disassembleOnDismount = config.get("control", "decompile_on_dismount", false).getBoolean(false);

        maxShipChunkBlocks = config.get("mobile_chunk", "max_chunk_blocks", 2048, "The maximum amount of objects that a mobile ship chunk may contain.").getInt();
        //maxShipChunkBlocks = Math.min(maxShipChunkBlocks, 3400);
        flyBalloonRatio = (float) config.get("mobile_chunk", "airship_balloon_ratio", 0.4D, "The part of the total amount of objects that should be balloon objects in order to make an airship.").getDouble(0.4D);
        submersibleFillRatio = (float) config.get("mobile_chunk", "submersible_fill_ratio", 0.3D, "The part of the ship that needs to not be water fillable for it to be considered submersible.").getDouble(0.9D);

        String[] seatNames = (config.get("settings", "seat", new String[]{"archimedesshipsplus:seat", "end_portal_frame"}, "Blocks that are considered seats, (BlockSeat is hardcoded so this is just an example.)").getStringList());

        for (String seat : seatNames) {
            seats.add(Block.getBlockFromName(seat));
        }

        if (FMLCommonHandler.instance().getSide().isClient()) {
            loadKeybindings();
        }

        config.save();
    }

    public void addBlacklistWhitelistEntries() {
        MovingWorld.instance.mConfig.addBlacklistedBlock(ArchimedesObjects.blockBuffer);
        MovingWorld.instance.mConfig.addWhitelistedBlock(ArchimedesObjects.blockMarkShip);
        MovingWorld.instance.mConfig.addWhitelistedBlock(ArchimedesObjects.blockFloater);
        MovingWorld.instance.mConfig.addWhitelistedBlock(ArchimedesObjects.blockBalloon);
        MovingWorld.instance.mConfig.addWhitelistedBlock(ArchimedesObjects.blockGauge);
        MovingWorld.instance.mConfig.addWhitelistedBlock(ArchimedesObjects.blockSeat);
        MovingWorld.instance.mConfig.addWhitelistedBlock(ArchimedesObjects.blockEngine);
        MovingWorld.instance.mConfig.addWhitelistedBlock(ArchimedesObjects.blockAnchorPoint);
    }

    public void postLoad() {
        Block[] defaultBalloonBlocks = {ArchimedesObjects.blockBalloon};

        String[] balloonBlockNames = new String[defaultBalloonBlocks.length];
        for (int i = 0; i < defaultBalloonBlocks.length; i++) {
            balloonBlockNames[i] = Block.blockRegistry.getNameForObject(defaultBalloonBlocks[i]).toString();
        }

        config.load();

        String[] balloonBlocks = config.get("mobile_chunk", "balloon_blocks", balloonBlockNames, "A list of blocks that are taken into account for ship flight capability").getStringList();
        Collections.addAll(this.balloonAlternatives, balloonBlocks);

        config.save();
    }

    @SideOnly(Side.CLIENT)
    private void loadKeybindings() {
        kbUp = new KeyBinding("key.archimedes.up", getKeyIndex(config, "key_ascent", Keyboard.KEY_X), "Archimedes");
        kbDown = new KeyBinding("key.archimedes.down", getKeyIndex(config, "key_descent", Keyboard.KEY_Z), "Archimedes");
        kbBrake = new KeyBinding("key.archimedes.brake", getKeyIndex(config, "key_brake", Keyboard.KEY_C), "Archimedes");
        kbAlign = new KeyBinding("key.archimedes.align", getKeyIndex(config, "key_align", Keyboard.KEY_EQUALS), "Archimedes");
        kbDisassemble = new KeyBinding("key.archimedes.decompile", getKeyIndex(config, "key_decompile", Keyboard.KEY_BACKSLASH), "Archimedes");
        kbShipInv = new KeyBinding("key.archimedes.shipinv", getKeyIndex(config, "key_shipinv", Keyboard.KEY_K), "Archimedes");
        Minecraft mc = Minecraft.getMinecraft();
        mc.gameSettings.keyBindings = ArrayUtils.addAll(mc.gameSettings.keyBindings, kbUp, kbDown, kbBrake, kbAlign, kbDisassemble, kbShipInv);
    }

    @SideOnly(Side.CLIENT)
    private int getKeyIndex(Configuration config, String name, int defaultkey) {
        return Keyboard.getKeyIndex(config.get("control", name, Keyboard.getKeyName(defaultkey)).getString());
    }

    public boolean isBalloon(Block block) {
        return balloonAlternatives.contains(Block.blockRegistry.getNameForObject(block).toString());
    }

    @SubscribeEvent
    public void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equals(ArchimedesShipMod.MOD_ID)) {
            if (config.hasChanged())
                config.save();
            loadAndSave();
        }
    }

    public Configuration getConfig() {
        return config;
    }
}
