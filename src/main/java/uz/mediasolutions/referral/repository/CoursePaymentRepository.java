package uz.mediasolutions.referral.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.mediasolutions.referral.entity.CoursePayment;

public interface CoursePaymentRepository extends JpaRepository<CoursePayment, Long> {

    CoursePayment findByFileId(String fileId);

}
