package de.srendi.advancedperipherals.common.addons.computercraft.peripheral;

import appeng.parts.reporting.ConversionMonitorPart;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.ICivilianData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.managers.interfaces.IBuildingManager;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.ILocalResearchTree;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.shared.util.NBTUtil;
import de.srendi.advancedperipherals.common.addons.computercraft.base.BasePeripheral;
import de.srendi.advancedperipherals.common.addons.computercraft.base.Converter;
import de.srendi.advancedperipherals.common.addons.minecolonies.MineColonies;
import de.srendi.advancedperipherals.common.blocks.base.PeripheralTileEntity;
import de.srendi.advancedperipherals.common.configuration.AdvancedPeripheralsConfig;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.ModList;

import java.util.*;
import java.util.stream.Collectors;

public class ColonyPeripheral extends BasePeripheral {

    protected boolean hasPermission = true;

    public ColonyPeripheral(String type, PeripheralTileEntity<?> tileEntity) {
        super(type, tileEntity);
    }

    @Override
    public boolean isEnabled() {
        return ModList.get().isLoaded("minecolonies") && AdvancedPeripheralsConfig.enableColonyIntegrator;
    }

    @LuaFunction(mainThread = true)
    public final boolean isInColony() throws LuaException {
        return getColonyWithoutPermission() != null;
    }

    @LuaFunction(mainThread = true)
    public final Object isWithin(Map<?, ?> pos) throws LuaException {
        IColony colony = getColonyWithoutPermission();

        if (!(pos.containsKey("x") && pos.containsKey("y") && pos.containsKey("z")))
            throw new LuaException("Coordinates expected");
        BlockPos p = new BlockPos(((Number) pos.get("x")).intValue(), ((Number) pos.get("y")).intValue(), ((Number) pos.get("z")).intValue());

        return colony.isCoordInColony(this.getWorld(), p);
    }

    @LuaFunction(mainThread = true)
    public final Object getCitizens() throws LuaException {
        IColony colony = getColony();

        List<Object> list = new ArrayList<>();
        colony.getCitizenManager().getCitizens().forEach(citizen -> {
            list.add(MineColonies.citizenToObject(citizen));
        });

        return list;
    }

    @LuaFunction(mainThread = true)
    public final int getColonyID() throws LuaException {
        IColony colony = getColony();

        return colony.getID();
    }

    @LuaFunction(mainThread = true)
    public final String getColonyName() throws LuaException {
        IColony colony = getColony();

        return colony.getName();
    }

    @LuaFunction(mainThread = true)
    public final String getColonyStyle() throws LuaException {
        IColony colony = getColony();

        return colony.getStyle();
    }

    @LuaFunction(mainThread = true)
    public final boolean isActive() throws LuaException {
        IColony colony = getColony();

        return colony.isActive();
    }

    @LuaFunction(mainThread = true)
    public final double getHappiness() throws LuaException {
        IColony colony = getColony();

        return colony.getOverallHappiness();
    }

    @LuaFunction(mainThread = true)
    public final Object getLocation() throws LuaException {
        IColony colony = getColony();

        return Converter.posToObject(colony.getCenter());
    }

    @LuaFunction(mainThread = true)
    public final boolean isUnderAttack() throws LuaException {
        IColony colony = getColony();

        return colony.isColonyUnderAttack();
    }

    @LuaFunction(mainThread = true)
    public final int amountOfCitizens() throws LuaException {
        IColony colony = getColony();

        return colony.getCitizenManager().getCurrentCitizenCount();
    }

    @LuaFunction(mainThread = true)
    public final int maxOfCitizens() throws LuaException {
        IColony colony = getColony();

        return colony.getCitizenManager().getMaxCitizens();
    }

    @LuaFunction(mainThread = true)
    public final int amountOfGraves() throws LuaException {
        IColony colony = getColony();

        return colony.getGraveManager().getGraves().size();
    }

    @LuaFunction(mainThread = true)
    public final Object getVisitors() throws LuaException {
        IColony colony = getColony();

        List<Object> list = new ArrayList<>();
        for (ICivilianData civilian : colony.getVisitorManager().getCivilianDataMap().values()) {
            if (!(civilian instanceof IVisitorData)) continue;
            list.add(MineColonies.visitorToObject((IVisitorData) civilian));
        }
        return list;
    }

    @LuaFunction(mainThread = true)
    public final Object getBuildings() throws LuaException {
        IColony colony = getColony();

        IBuildingManager manager = colony.getBuildingManager();
        List<Object> buildingData = new ArrayList<>();
        for (Map.Entry<BlockPos, IBuilding> building : manager.getBuildings().entrySet()) {
            buildingData.add(MineColonies.buildingToObject(manager, building.getValue(), building.getKey()));
        }

        return buildingData;
    }

    @LuaFunction(mainThread = true)
    public final Object getWorkOrders() throws LuaException {
        IColony colony = getColony();

        List<Object> worksData = new ArrayList<>();
        for (IWorkOrder workOrder : colony.getWorkManager().getWorkOrders().values()) {
            CompoundNBT nbt = new CompoundNBT();
            workOrder.write(nbt);
            worksData.add(NBTUtil.toLua(nbt));
        }

        return worksData;
    }

    @LuaFunction(mainThread = true)
    public final Object getResearch() throws LuaException {
        IColony colony = getColony();

        IGlobalResearchTree globalTree = IGlobalResearchTree.getInstance();

        Map<String, Object> result = new HashMap<>();
        for (ResourceLocation branch : globalTree.getBranches())
            result.put(branch.toString(), MineColonies.getResearch(branch, globalTree.getPrimaryResearch(branch), colony));

        return result;
    }

    @LuaFunction(mainThread = true)
    public final Object getWorkOrderResources(int id) throws LuaException {
        IColony colony = getColony();

        IWorkOrder workOrder = colony.getWorkManager().getWorkOrder(id);
        if (workOrder == null)
            return null;

        return MineColonies.builderResourcesToObject(colony, workOrder.getClaimedBy());
    }

    @LuaFunction(mainThread = true)
    public final Object getBuilderResources(Map<?, ?> pos) throws LuaException {
        IColony colony = getColony();

        if (!(pos.containsKey("x") && pos.containsKey("y") && pos.containsKey("z")))
            throw new LuaException("Coordinates expected");
        BlockPos blockPos = new BlockPos(((Number) pos.get("x")).intValue(), ((Number) pos.get("y")).intValue(), ((Number) pos.get("z")).intValue());

        return MineColonies.builderResourcesToObject(colony, blockPos);
    }

    @LuaFunction(mainThread = true)
    public final Object getRequests() throws LuaException {
        IColony colony = getColony();

        IRequestManager requestManager = colony.getRequestManager();
        //All requests assigned to players
        IPlayerRequestResolver playerResolver = requestManager.getPlayerResolver();
        //All requests not assigned to players
        IRetryingRequestResolver requestResolver = requestManager.getRetryingRequestResolver();

        Set<IToken<?>> tokens = new HashSet<>();
        tokens.addAll(playerResolver.getAllAssignedRequests());
        tokens.addAll(requestResolver.getAllAssignedRequests());

        List<IRequest<?>> requests = new ArrayList<>();
        for(IToken<?> token : tokens) {
            IRequest<?> request = requestManager.getRequestForToken(token);
            if(request != null && !(request instanceof IDeliverable))
                requests.add(request);
        }

        List<Object> result = new ArrayList<>();
        requests.forEach(request -> {
            IDeliverable deliverableRequest = (IDeliverable) request.getRequest();
            Map<String, Object> map = new HashMap<>();
            map.put("id", request.getId().getIdentifier().toString());
            map.put("name", TextFormatting.stripFormatting(request.getShortDisplayString().getString()));
            map.put("desc", TextFormatting.stripFormatting(request.getLongDisplayString().getString()));
            map.put("state", request.getState().toString());
            map.put("count", deliverableRequest.getCount());
            map.put("minCount", deliverableRequest.getMinimumCount());
            map.put("items", request.getDisplayStacks().stream().map(Converter::stackToObject)
                    .collect(Collectors.toList()));
            map.put("target", request.getRequester().getRequesterDisplayName(requestManager, request).getString());
            result.add(map);
        });
        return result;
    }

    private IColony getColony() throws LuaException {
        IMinecoloniesAPI api = IMinecoloniesAPI.getInstance();
        IColony colony = api.getColonyManager().getColonyByPosFromWorld(getWorld(), getPos());
        if (colony == null || !this.hasPermission)
            throw new LuaException("Here is no colony or you don't have the right permissions");
        return colony;
    }

    private IColony getColonyWithoutPermission() {
        IMinecoloniesAPI api = IMinecoloniesAPI.getInstance();
        return api.getColonyManager().getColonyByPosFromWorld(getWorld(), getPos());
    }
}
