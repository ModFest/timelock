package net.modfest.timelock.world;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.item.map.MapState;
import net.modfest.timelock.Timelock;
import net.modfest.timelock.TimelockValue;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TimelockData extends PersistentState {

    private final Map<Identifier, TimelockValue> zones;
    private final Multimap<Identifier, ChunkPos> chunks;

    public TimelockData() {
        zones = new HashMap<>();
        chunks = HashMultimap.create();
    }

    public TimelockData(Map<Identifier, TimelockValue> zones, Multimap<Identifier, ChunkPos> chunks) {
        this.zones = zones;
        this.chunks = chunks;
    }

    public static PersistentState.Type<TimelockData> getPersistentStateType() {
        return new PersistentState.Type<>(TimelockData::new, TimelockData::fromNbt, null);
    }

    public Map<Identifier, TimelockValue> zones() {
        return zones;
    }

    public Multimap<Identifier, ChunkPos> chunks() {
        return chunks;
    }

    public Map<ChunkPos, TimelockValue> getData() {
        return chunks.entries().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, e -> zones.get(e.getKey())));
    }

    public static TimelockData fromNbt(NbtCompound nbt) {
        var zoneNbt = nbt.getCompound("Zones");
        Map<Identifier, TimelockValue> zones = new HashMap<>();
        for (var key : zoneNbt.getKeys()) {
            var value = zoneNbt.getCompound(key);
            zones.put(new Identifier(key), TimelockValue.fromNbt(value));
        }
        var chunkNbt = nbt.getCompound("Chunks");
        Multimap<Identifier, ChunkPos> chunks = HashMultimap.create();
        for (var key : chunkNbt.getKeys()) {
            var array = chunkNbt.getLongArray(key);
            var list = Arrays.stream(array)
                    .mapToObj(ChunkPos::new)
                    .toList();
            chunks.putAll(new Identifier(key), list);
        }
        return new TimelockData(zones, chunks);
    }

    public static TimelockData get(ServerWorld world) {
        return world.getPersistentStateManager()
                .getOrCreate(TimelockData.getPersistentStateType(), Timelock.ID);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        var zoneNbt = new NbtCompound();
        for (var pair : zones.entrySet()) {
            zoneNbt.put(pair.getKey().toString(), pair.getValue().toNbt());
        }
        nbt.put("Zones", zoneNbt);
        var chunkNbt = new NbtCompound();
        for (var key : chunks.keySet()) {
            var list = chunks.get(key).stream()
                    .map(ChunkPos::toLong)
                    .toList();
            chunkNbt.putLongArray(key.toString(), list);
        }
        nbt.put("Chunks", chunkNbt);
        return nbt;
    }
}
