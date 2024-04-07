package uz.mediasolutions.referral.controller.abs;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import uz.mediasolutions.referral.manual.ApiResult;
import uz.mediasolutions.referral.payload.PromoUserDTO;
import uz.mediasolutions.referral.payload.TgUserDTO;
import uz.mediasolutions.referral.utills.constants.Rest;

@RequestMapping(UserController.USER)
public interface UserController {

    String USER = Rest.BASE_PATH + "users/";

    String GET_ALL = "get-all";
    String GET_ALL_PROMO_USERS = "get-all-promo-users";

    String GET_BY_ID = "get/{id}";

    String BAN = "ban/{id}";

    String UNBAN = "unban/{id}";


    @GetMapping(GET_ALL)
    ApiResult<Page<TgUserDTO>> getAll(@RequestParam(defaultValue = Rest.DEFAULT_PAGE_NUMBER) int page,
                                      @RequestParam(defaultValue = Rest.DEFAULT_PAGE_SIZE) int size,
                                      @RequestParam(defaultValue = "null", required = false) String search);

    @GetMapping(GET_ALL)
    ApiResult<Page<PromoUserDTO>> getAllPromoUsers(@RequestParam(defaultValue = Rest.DEFAULT_PAGE_NUMBER) int page,
                                                   @RequestParam(defaultValue = Rest.DEFAULT_PAGE_SIZE) int size,
                                                   @RequestParam(defaultValue = "null", required = false) String search);

    @GetMapping(GET_BY_ID)
    ApiResult<TgUserDTO> getById(@PathVariable Long id);

    @PutMapping(BAN)
    ApiResult<?> banUser(@PathVariable Long id);

    @PutMapping(UNBAN)
    ApiResult<?> unbanUser(@PathVariable Long id);

}
