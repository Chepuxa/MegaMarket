package ru.megamarket.domain.unit;

import ru.megamarket.dto.ShopUnitType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "shop_unit")
public class ShopUnitEntity {

    @Id
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
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ShopUnitEntity parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private Set<ShopUnitEntity> children;
}
