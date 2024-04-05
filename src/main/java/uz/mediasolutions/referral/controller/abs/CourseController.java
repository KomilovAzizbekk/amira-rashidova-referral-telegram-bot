package uz.mediasolutions.referral.controller.abs;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import uz.mediasolutions.referral.manual.ApiResult;
import uz.mediasolutions.referral.payload.CourseDTO;
import uz.mediasolutions.referral.utills.constants.Rest;

import javax.validation.Valid;

@RequestMapping(CourseController.COURSE)
public interface CourseController {

    String COURSE = Rest.BASE_PATH + "course/";
    String GET_ALL = "get-all";
    String GET_BY_ID = "get/{id}";
    String ADD = "add";
    String EDIT = "edit/{id}";
    String DELETE = "delete/{id}";

    @GetMapping(GET_ALL)
    ApiResult<Page<CourseDTO>> getAll(@RequestParam(defaultValue = Rest.DEFAULT_PAGE_NUMBER) int page,
                                      @RequestParam(defaultValue = Rest.DEFAULT_PAGE_SIZE) int size,
                                      @RequestParam(defaultValue = "null") String search);

    @GetMapping(GET_BY_ID)
    ApiResult<CourseDTO> getById(@PathVariable Long id);

    @PostMapping(ADD)
    ApiResult<?> add(@RequestBody @Valid CourseDTO dto);

    @PutMapping(EDIT)
    ApiResult<?> edit(@PathVariable Long id,
                      @RequestBody @Valid CourseDTO dto);

    @DeleteMapping(DELETE)
    ApiResult<?> delete(@PathVariable Long id);
}
