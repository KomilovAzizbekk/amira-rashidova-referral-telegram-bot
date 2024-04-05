package uz.mediasolutions.referral.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.mediasolutions.referral.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Page<Course> findAllByNameUzContainsIgnoreCaseOrNameRuContainsIgnoreCaseOrderByNumberAsc(
            String nameUz, String nameRu, Pageable pageable);

    Page<Course> findAllByOrderByNumberAsc(Pageable pageable);

    boolean existsByNumber(Integer number);

    boolean existsByNumberAndId(Integer number, Long id);

}
