package com.acikek.timelock.world;

import com.acikek.timelock.Timelock;
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

public class TimelockData extends PersistentState {

    private final Map<Identifier, Long> zones;
    private final Multimap<Identifier, ChunkPos> chunks;

    public TimelockData() {
        zones = new HashMap<>();
        chunks = HashMultimap.create();
    }

    public TimelockData(Map<Identifier, Long> zones, Multimap<Identifier, ChunkPos> chunks) {
        this.zones = zones;
        this.chunks = chunks;
    }

    public Map<Identifier, Long> zones() {
        return zones;
    }

    public Multimap<Identifier, ChunkPos> chunks() {
        return chunks;
    }

    public static TimelockData fromNbt(NbtCompound nbt) {
        var zoneNbt = nbt.getCompound("Zones");
        Map<Identifier, Long> zones = new HashMap<>();
        for (var key : zoneNbt.getKeys()) {
            var time = zoneNbt.getLong(key);
            zones.put(new Identifier(key), time);
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
                .getOrCreate(TimelockData::fromNbt, TimelockData::new, Timelock.ID);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        var zoneNbt = new NbtCompound();
        for (var pair : zones.entrySet()) {
            zoneNbt.putLong(pair.getKey().toString(), pair.getValue());
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
