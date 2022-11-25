package cc.tweaked_programs.cccbridge.peripherals;

import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.StationEditPacket;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.StationTileEntity;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.TrainEditPacket;
import com.simibubi.create.foundation.networking.AllPackets;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TrainPeripheral implements IPeripheral {
    private final World level;
    private final StationTileEntity station;

    public TrainPeripheral(@NotNull BlockPos pos, World level) {
        this.level = level;
        station = (StationTileEntity) level.getBlockEntity(pos);
    }

    @NotNull
    @Override
    public String getType() {
        return "train_station";
    }

    /**
     * Assembles a train.
     *
     * @return Whether it was successful or not with a message.
     */
    @LuaFunction
    public final MethodResult assemble() {
        if (station.getStation().getPresentTrain() != null) {
            return MethodResult.of(false, "Train is already assembled");
        }
        if (station.getStation().assembling) {
            AllPackets.channel.sendToServer(StationEditPacket.tryAssemble(station.getPos()));
            return MethodResult.of(true, "Train assembled");
        }
        return MethodResult.of(false, "Train could not be assembled");
    }

    /**
     * Disassembles a train.
     *
     * @return Whether it was successful or not with a message.
     */
    @LuaFunction
    public final MethodResult disassemble() {
        if (station.getStation().getPresentTrain() == null) {
            return MethodResult.of(false, "An assembled train is not present");
        }
        if (station.getStation().getPresentTrain().canDisassemble()) {
            AllPackets.channel.sendToServer(StationEditPacket.configure(station.getPos(), true, station.getStation().name));
            return MethodResult.of(true, "Train disassembled");
        }
        return MethodResult.of(false, "Could not disassemble train");
    }

    /**
     * Returns the current station name.
     *
     * @return Name of station.
     */
    @LuaFunction
    public String getStationName() {
        return station.getStation().name;
    }

    /**
     * Returns the current trains name.
     *
     * @return Whether it was successful or not with the train's name.
     */
    @LuaFunction
    public MethodResult getTrainName() {
        if (station.getStation().getPresentTrain() == null) {
            return MethodResult.of(false, "There is no assembled train to get the name of");
        }else {
            return MethodResult.of(true, Objects.requireNonNull(station.getStation().getPresentTrain()).name.getString());
        }
    }

    /**
     * Sets the stations name
     *
     * @param name The new name.
     * @return Whether it was successful or not.
     */
    @LuaFunction
    public final boolean setStationName(@NotNull String name) {
        if(station.getStation().assembling){
            AllPackets.channel.sendToServer(StationEditPacket.configure(station.getPos(), true, name));
        }else{
            AllPackets.channel.sendToServer(StationEditPacket.configure(station.getPos(), false, name));
        }
        return true;
    }

    /**
     * Sets the current trains name.
     *
     * @param name The new name.
     * @return Whether it was successful or not with a message.
     */
    @LuaFunction
    public final MethodResult setTrainName(@NotNull String name) {
        if (station.getStation().getPresentTrain() == null) {
            return MethodResult.of(false, "There is no train to set the name of");
        }
        if (!name.isBlank()) {
            Train train = station.getStation().getPresentTrain();
            AllPackets.channel.sendToServer(new TrainEditPacket(train.id, name, train.icon.getId()));
            return MethodResult.of(true, "Set train name to " + name);
        }
        return MethodResult.of(false, "Train name cannot be blank");
    }

    /**
     * Gets the number of Carriages attached to the current train.
     *
     * @return The number of Carriages.
     */
    @LuaFunction
    public int getCarriageCount() {
        if (station.getStation().getPresentTrain() == null) {
            return 0;
        }
        return station.getStation().getPresentTrain().carriages.size();
    }

    /**
     * Returns boolean whether a train os present or not.
     *
     * @return Whether it was successful or not.
     */
    @LuaFunction
    public boolean getPresentTrain() {
        return station.getStation().getPresentTrain() != null;
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        return this == iPeripheral;
    }
}