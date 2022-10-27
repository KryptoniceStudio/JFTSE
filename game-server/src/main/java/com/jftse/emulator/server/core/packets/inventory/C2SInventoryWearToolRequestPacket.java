package com.jftse.emulator.server.core.packets.inventory;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class C2SInventoryWearToolRequestPacket extends Packet {
    private List<Integer> toolSlotList;

    public C2SInventoryWearToolRequestPacket(Packet packet) {
        super(packet);

        toolSlotList = new ArrayList<>();

        for (int i = 0; i < 5; ++i)
            toolSlotList.add(this.readInt());
    }
}