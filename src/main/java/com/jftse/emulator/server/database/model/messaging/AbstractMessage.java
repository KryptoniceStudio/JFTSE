package com.jftse.emulator.server.database.model.messaging;

import com.jftse.emulator.common.model.AbstractBaseModel;
import com.jftse.emulator.server.database.model.player.Player;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@MappedSuperclass
public class AbstractMessage extends AbstractBaseModel {
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    private Player sender;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "receiver_id", referencedColumnName = "id")
    private Player receiver;

    private String message;
    private Boolean seen;
    private Byte useTypeOption = 0;
}
