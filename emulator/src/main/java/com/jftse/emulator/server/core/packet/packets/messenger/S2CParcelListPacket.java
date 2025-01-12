package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.entities.database.model.messenger.Parcel;
import com.jftse.emulator.server.core.item.EItemCategory;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class S2CParcelListPacket extends Packet {
    public S2CParcelListPacket(byte listType, List<Parcel> parcels) {
        super(PacketOperations.S2CParcelListAnswer.getValueAsChar());

        this.write(listType);
        this.write((byte) parcels.size());
        for (Parcel parcel : parcels) {
            this.write(parcel.getId().intValue());
            this.write(listType == (byte) 0 ? parcel.getSender().getName() : parcel.getReceiver().getName());
            this.write(parcel.getMessage());

            EItemCategory category = EItemCategory.valueOf(parcel.getCategory());
            this.write(category.getValue());
            this.write(parcel.getItemIndex());
            this.write((byte) 0); //UNK
            this.write(parcel.getItemCount());
            this.write(parcel.getEParcelType().getValue());
            this.write(parcel.getGold());
            this.write((byte) 0); // UNK
            this.write(parcel.getCreated());
            this.write((byte) 0); // UNK
            this.write((byte) 0); // UNK
            this.write((byte) 0); // UNK
            this.write((byte) 0); // UNK
            this.write((byte) 0); // UNK
            this.write((byte) 0); // UNK
        }
    }
}
