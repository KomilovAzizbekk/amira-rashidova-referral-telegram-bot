package uz.mediasolutions.referral.service.abs;

import org.springframework.data.domain.Page;
import uz.mediasolutions.referral.manual.ApiResult;
import uz.mediasolutions.referral.payload.CourseDTO;

public interface CourseService {
    ApiResult<Page<CourseDTO>> getAll(int page, int size, String search);

    ApiResult<CourseDTO> getById(Long id);

    ApiResult<?> add(CourseDTO dto);

    ApiResult<?> edit(Long id, CourseDTO dto);

    ApiResult<?> delete(Long id);
}
