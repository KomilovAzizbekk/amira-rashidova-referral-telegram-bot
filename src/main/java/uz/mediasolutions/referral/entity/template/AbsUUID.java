package uz.mediasolutions.referral.entity.template;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

@Getter
@Setter
@ToString
@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
public abstract class AbsUUID extends AbsAudit {

    @Id
    @GeneratedValue(generator = "uuid4")
    @GenericGenerator(name = "uuid4",strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id")
    private UUID id;

}