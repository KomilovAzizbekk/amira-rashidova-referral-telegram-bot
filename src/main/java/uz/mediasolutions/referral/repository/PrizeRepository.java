package uz.mediasolutions.referral.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.mediasolutions.referral.entity.Prize;

public interface PrizeRepository extends JpaRepository<Prize, Long> {

    Page<Prize> findAllByOrderByPointAsc(Pageable pageable);

    Page<Prize> findAllByNameUzContainsIgnoreCaseOrNameRuContainsIgnoreCaseOrderByPointAsc(
            String nameUz, String nameRu, Pageable pageable);

}
