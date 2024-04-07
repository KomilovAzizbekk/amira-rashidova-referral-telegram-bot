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
public class PrizeApp extends AbsLong {

    @ManyToOne(fetch = FetchType.LAZY)
    private TgUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Prize prize;

    @Column(name = "accepted")
    private Boolean accepted = null;

}
