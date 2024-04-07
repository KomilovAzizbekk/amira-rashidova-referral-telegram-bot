package uz.mediasolutions.referral.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "course_payment")
public class CoursePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id")
    private String fileId;

    @Column(name = "accepted")
    private Boolean accepted = null;

    @ManyToOne(fetch = FetchType.LAZY)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    private TgUser tgUser;

}
