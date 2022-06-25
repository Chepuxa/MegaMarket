package ru.megamarket.domain.statistic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.format.annotation.DateTimeFormat;
import ru.megamarket.domain.unit.ShopUnitEntity;
import ru.megamarket.dto.ShopUnitType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "shop_unit_statistic")
public class ShopUnitStatisticEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sus_seq")
    @SequenceGenerator(name = "sus_seq", sequenceName = "SUS_SEQ", allocationSize = 100)
    @Column(name =  "key", nullable = false, updatable = false)
    private Integer key;

    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String name;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @Column(nullable = false)
    private Timestamp date;

    @Column(name = "parent_id")
    private UUID parentId;

    @Enumerated(EnumType.STRING)
    private ShopUnitType type;

    private Long price;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = ShopUnitEntity.class)
    @JoinColumn(name = "id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ShopUnitEntity parent;
}
