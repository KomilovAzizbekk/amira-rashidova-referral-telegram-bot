package uz.mediasolutions.referral.mapper;

import org.mapstruct.Mapper;
import uz.mediasolutions.referral.entity.Course;
import uz.mediasolutions.referral.payload.CourseDTO;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    CourseDTO toDTO(Course course);

    Course toEntity(CourseDTO dto);

}
