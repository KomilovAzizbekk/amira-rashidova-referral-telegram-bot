package uz.mediasolutions.referral.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import uz.mediasolutions.referral.entity.template.AbsLong;

import javax.persistence.*;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@EqualsAndHashCode(callSuper = true)
@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "promocodes")
public class Promocode extends AbsLong {

    @Column(name = "name", nullable = false)
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    private TgUser owner;

    @ManyToMany(fetch = FetchType.LAZY)
    private List<TgUser> promoUser;

    @Column(name = "discount_percent")
    private Integer discountPercent;

    @Column(name = "active", nullable = false)
    private boolean active;

}
