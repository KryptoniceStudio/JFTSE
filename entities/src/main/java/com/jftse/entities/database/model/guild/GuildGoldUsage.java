package com.jftse.entities.database.model.guild;

import com.jftse.entities.database.model.AbstractBaseModel;
import com.jftse.entities.database.model.player.Player;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.util.Date;

import javax.persistence.*;

@Getter
@Setter
@Audited
@Entity
public class GuildGoldUsage extends AbstractBaseModel {
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "guild_id", referencedColumnName = "id")
    private Guild guild;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "player_id", referencedColumnName = "id")
    private Player player;

    private Date date;
    private Integer goldUsed;
    private Integer goldRemaining;
}