package uz.mediasolutions.referral.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;
import uz.mediasolutions.referral.controller.abs.CourseController;
import uz.mediasolutions.referral.manual.ApiResult;
import uz.mediasolutions.referral.payload.CourseDTO;
import uz.mediasolutions.referral.service.abs.CourseService;

@RestController
@RequiredArgsConstructor
public class CourseControllerImpl implements CourseController {

    private final CourseService courseService;

    @Override
    public ApiResult<Page<CourseDTO>> getAll(int page, int size, String search) {
        return courseService.getAll(page, size, search);
    }

    @Override
    public ApiResult<CourseDTO> getById(Long id) {
        return courseService.getById(id);
    }

    @Override
    public ApiResult<?> add(CourseDTO dto) {
        return courseService.add(dto);
    }

    @Override
    public ApiResult<?> edit(Long id, CourseDTO dto) {
        return courseService.edit(id, dto);
    }

    @Override
    public ApiResult<?> delete(Long id) {
        return courseService.delete(id);
    }
}
