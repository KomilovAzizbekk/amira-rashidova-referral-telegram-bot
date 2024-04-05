package uz.mediasolutions.referral.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uz.mediasolutions.referral.entity.Course;
import uz.mediasolutions.referral.exceptions.RestException;
import uz.mediasolutions.referral.manual.ApiResult;
import uz.mediasolutions.referral.mapper.CourseMapper;
import uz.mediasolutions.referral.payload.CourseDTO;
import uz.mediasolutions.referral.repository.CourseRepository;
import uz.mediasolutions.referral.service.abs.CourseService;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    @Override
    public ApiResult<Page<CourseDTO>> getAll(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Course> courses;
        if (search.equals("null")) {
            courses = courseRepository.findAllByOrderByNumberAsc(pageable);
        } else {
            courses = courseRepository.findAllByNameUzContainsIgnoreCaseOrNameRuContainsIgnoreCaseOrderByNumberAsc(
                    search, search, pageable);
        }
        Page<CourseDTO> mapped = courses.map(courseMapper::toDTO);
        return ApiResult.success(mapped);
    }

    @Override
    public ApiResult<CourseDTO> getById(Long id) {
        Course course = courseRepository.findById(id).orElseThrow(
                () -> RestException.restThrow("COURSE NOT FOUND", HttpStatus.BAD_REQUEST));
        CourseDTO dto = courseMapper.toDTO(course);
        return ApiResult.success(dto);
    }

    @Override
    public ApiResult<?> add(CourseDTO dto) {

        if (courseRepository.existsByNumber(dto.getNumber())) {
            throw RestException.restThrow("NUMBER MUST BE UNIQUE", HttpStatus.BAD_REQUEST);
        } else {
            Course entity = courseMapper.toEntity(dto);
            courseRepository.save(entity);
            return ApiResult.success("SAVED SUCCESSFULLY");
        }
    }

    @Override
    public ApiResult<?> edit(Long id, CourseDTO dto) {
        Course course = courseRepository.findById(id).orElseThrow(
                () -> RestException.restThrow("COURSE NOT FOUND", HttpStatus.BAD_REQUEST));
        if (courseRepository.existsByNumber(dto.getNumber()) &&
                !courseRepository.existsByNumberAndId(dto.getNumber(), id)) {
            throw RestException.restThrow("NUMBER MUST BE UNIQUE", HttpStatus.BAD_REQUEST);
        } else {
            course.setNameUz(dto.getNameUz());
            course.setNameRu(dto.getNameRu());
            course.setNumber(dto.getNumber());
            course.setChannelId(dto.getChannelId());
            course.setPrice(dto.getPrice());
            course.setActive(dto.isActive());
            courseRepository.save(course);
            return ApiResult.success("EDITED SUCCESSFULLY");
        }
    }

    @Override
    public ApiResult<?> delete(Long id) {
        try {
            courseRepository.deleteById(id);
            return ApiResult.success("DELETED SUCCESSFULLY");
        } catch (Exception e) {
            e.printStackTrace();
            throw RestException.restThrow("CANNOT DELETE", HttpStatus.CONFLICT);
        }
    }
}
