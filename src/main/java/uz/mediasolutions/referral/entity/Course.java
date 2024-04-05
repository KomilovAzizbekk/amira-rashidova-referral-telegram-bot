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
@Table(name = "courses")
public class Course extends AbsLong {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "number", nullable = false)
    private Integer number;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "channel_id", nullable = false)
    private String channelId;

    @Column(name = "active", nullable = false)
    private boolean active;

    @ManyToMany(fetch = FetchType.LAZY)
    private List<TgUser> users;

    @Column(name = "discount")
    private Integer discount;

}
