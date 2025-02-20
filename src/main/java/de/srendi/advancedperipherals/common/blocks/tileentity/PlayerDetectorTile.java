package de.srendi.advancedperipherals.common.blocks.tileentity;

import de.srendi.advancedperipherals.common.addons.computercraft.peripheral.PlayerDetectorPeripheral;
import de.srendi.advancedperipherals.common.blocks.base.PeripheralTileEntity;
import de.srendi.advancedperipherals.common.setup.TileEntityTypes;
import net.minecraft.tileentity.TileEntityType;
import org.jetbrains.annotations.NotNull;

public class PlayerDetectorTile extends PeripheralTileEntity<PlayerDetectorPeripheral> {

    public PlayerDetectorTile() {
        this(TileEntityTypes.PLAYER_DETECTOR.get());
    }

    public PlayerDetectorTile(final TileEntityType<?> tileEntityType) {
        super(tileEntityType);
    }

    @NotNull
    @Override
    protected PlayerDetectorPeripheral createPeripheral() {
        return new PlayerDetectorPeripheral("playerDetector", this);
    }

}