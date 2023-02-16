package com.jftse.entities.database.model.player;

import com.jftse.entities.database.model.AbstractBaseModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;

@Getter
@Setter
@Audited
@Entity
public class CardSlotEquipment extends AbstractBaseModel {
    private Integer slot1 = 0;
    private Integer slot2 = 0;
    private Integer slot3 = 0;
    private Integer slot4 = 0;
}