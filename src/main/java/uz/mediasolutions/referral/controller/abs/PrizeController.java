package uz.mediasolutions.referral.controller.abs;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import uz.mediasolutions.referral.manual.ApiResult;
import uz.mediasolutions.referral.payload.PrizeDTO;
import uz.mediasolutions.referral.utills.constants.Rest;

import javax.validation.Valid;

@RequestMapping(PrizeController.PRIZE)
public interface PrizeController {

    String PRIZE = Rest.BASE_PATH + "prize/";
    String GET_ALL = "get-all";
    String GET_BY_ID = "get/{id}";
    String ADD = "add";
    String EDIT = "edit/{id}";
    String DELETE = "delete/{id}";

    @GetMapping(GET_ALL)
    ApiResult<Page<PrizeDTO>> getAll(@RequestParam(defaultValue = Rest.DEFAULT_PAGE_NUMBER) int page,
                                     @RequestParam(defaultValue = Rest.DEFAULT_PAGE_SIZE) int size,
                                     @RequestParam(defaultValue = "null") String search);

    @GetMapping(GET_BY_ID)
    ApiResult<PrizeDTO> getById(@PathVariable Long id);

    @PostMapping(ADD)
    ApiResult<?> add(@RequestBody @Valid PrizeDTO dto);

    @PutMapping(EDIT)
    ApiResult<?> edit(@PathVariable Long id,
                      @RequestBody @Valid PrizeDTO dto);

    @DeleteMapping(DELETE)
    ApiResult<?> delete(@PathVariable Long id);
}
