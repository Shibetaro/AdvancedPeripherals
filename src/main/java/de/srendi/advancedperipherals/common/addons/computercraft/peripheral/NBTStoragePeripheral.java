package de.srendi.advancedperipherals.common.addons.computercraft.peripheral;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.shared.util.NBTUtil;
import de.srendi.advancedperipherals.common.addons.computercraft.base.BasePeripheral;
import de.srendi.advancedperipherals.common.blocks.base.PeripheralTileEntity;
import de.srendi.advancedperipherals.common.blocks.tileentity.NBTStorageTile;
import de.srendi.advancedperipherals.common.configuration.AdvancedPeripheralsConfig;
import de.srendi.advancedperipherals.common.util.CountingWipingStream;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public class NBTStoragePeripheral extends BasePeripheral {

    public NBTStoragePeripheral(String type, PeripheralTileEntity<?> tileEntity) {
        super(type, tileEntity);
    }

    @Override
    public boolean isEnabled() {
        return AdvancedPeripheralsConfig.enableNBTStorage;
    }

    @LuaFunction(mainThread = true)
    public final Map<String, Integer> getConfiguration() {
        Map<String, Integer> result = new HashMap<>();
        result.put("maxSize", AdvancedPeripheralsConfig.nbtStorageMaxSize);
        return result;
    }

    @LuaFunction
    public final MethodResult read() {
        return MethodResult.of(NBTUtil.toLua(((NBTStorageTile) tileEntity).getStored()));
    }

    @LuaFunction
    public final MethodResult writeJson(String jsonData){
        if (jsonData.length() > AdvancedPeripheralsConfig.nbtStorageMaxSize) {
            return MethodResult.of(null, "JSON size is bigger than allowed");
        }
        CompoundNBT parsedData;
        try {
            parsedData = JsonToNBT.parseTag(jsonData);
        } catch (CommandSyntaxException ex) {
            return MethodResult.of(null, String.format("Cannot parse json: %s", ex.getMessage()));
        }
        ((NBTStorageTile) tileEntity).setStored(parsedData);
        return MethodResult.of(true);
    }

    @LuaFunction
    public final MethodResult writeTable(Map<?, ?> data) {
        CountingWipingStream countingStream = new CountingWipingStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(countingStream);
            objectOutputStream.writeObject(data);
            objectOutputStream.close();
        } catch (IOException e){
            return MethodResult.of(null, String.format("No idea, how this happened, but java IO Exception appear %s", e.getMessage()));
        }
        CompoundNBT parsedData = (CompoundNBT) de.srendi.advancedperipherals.common.util.NBTUtil.toDirectNBT(data);
        ((NBTStorageTile) tileEntity).setStored(parsedData);
        return MethodResult.of(true);
    }
}
