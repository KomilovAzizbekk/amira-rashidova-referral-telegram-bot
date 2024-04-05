package uz.mediasolutions.referral.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import uz.mediasolutions.referral.entity.template.AbsLong;

import javax.persistence.*;

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
@Table(name = "tg_users")
public class TgUser extends AbsLong {

    @Column(name = "chat_id")
    private String chatId;

    @Column(name = "name")
    private String name;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "username")
    private String username;

    @Column(name = "registered")
    private boolean registered;

    @Column(name = "admin")
    private boolean admin;

    @Column(name = "banned")
    private boolean banned;

    @ManyToOne(fetch = FetchType.LAZY)
    private Step step;

    @Column(name = "repetition")
    private Integer repetition;

    @Column(name = "points")
    private Integer points;

    @ManyToOne(fetch = FetchType.LAZY)
    private Course tempCourse;

    @Column(name = "course_student")
    private boolean courseStudent;

    @ManyToOne(fetch = FetchType.LAZY)
    private PromoCode usingPromo;
}
