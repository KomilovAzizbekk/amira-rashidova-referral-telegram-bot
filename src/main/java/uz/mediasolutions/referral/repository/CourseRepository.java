package uz.mediasolutions.referral.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.mediasolutions.referral.entity.Course;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Page<Course> findAllByNameContainsIgnoreCaseOrderByNumberAsc(String name, Pageable pageable);

    Page<Course> findAllByOrderByNumberAsc(Pageable pageable);

    boolean existsByNumber(Integer number);

    boolean existsByNumberAndId(Integer number, Long id);

    List<Course> findAllByActiveIsTrueOrderByNumberAsc();

    Course findByName(String name);

}
