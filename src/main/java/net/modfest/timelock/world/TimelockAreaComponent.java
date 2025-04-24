package net.modfest.timelock.world;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.doublekekse.area_lib.component.AreaDataComponent;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.modfest.timelock.Timelock;

public class TimelockAreaComponent implements AreaDataComponent {
    private record Data(long ticks, boolean offset) {
        public static Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
               Codec.LONG.fieldOf("ticks").forGetter(Data::ticks),
                Codec.BOOL.fieldOf("offset").forGetter(Data::offset)
        ).apply(instance, Data::new));

        public static Data NONE = new Data(0, true);
    }

    private Data data = Data.NONE;

    public TimelockAreaComponent() {}
    public TimelockAreaComponent(long ticks, boolean offset) {
        this.data = new Data(ticks, offset);
    }

    @Override
    public void load(AreaSavedData areaSavedData, NbtCompound nbtCompound) {
        data = Data.CODEC.decode(NbtOps.INSTANCE, nbtCompound.get("timelock")).mapOrElse(
                Pair::getFirst,
                error -> {
                    Timelock.LOGGER.error("failed to deserialize area component: {}", error);
                    return Data.NONE;
                }
        );
    }

    @Override
    public NbtCompound save() {
        NbtCompound tag = new NbtCompound();
        tag.put("timelock", Data.CODEC.encode(this.data, NbtOps.INSTANCE, null).getOrThrow());
        return tag;
    }

    public long ticks() {
        return data.ticks;
    }

    public boolean offset() {
        return data.offset;
    }
}
