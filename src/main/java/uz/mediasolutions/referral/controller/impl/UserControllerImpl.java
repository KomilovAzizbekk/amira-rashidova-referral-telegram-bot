package uz.mediasolutions.referral.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;
import uz.mediasolutions.referral.controller.abs.UserController;
import uz.mediasolutions.referral.manual.ApiResult;
import uz.mediasolutions.referral.payload.TgUserDTO;
import uz.mediasolutions.referral.service.abs.UserService;

@RestController
@RequiredArgsConstructor
public class UserControllerImpl implements UserController {

    private final UserService userService;

    @Override
    public ApiResult<Page<TgUserDTO>> getAll(int page, int size, String search) {
        return userService.getAll(page, size, search);
    }

    @Override
    public ApiResult<TgUserDTO> getById(Long id) {
        return userService.getById(id);
    }

    @Override
    public ApiResult<?> banUser(Long id) {
        return userService.banUser(id);
    }

    @Override
    public ApiResult<?> unbanUser(Long id) {
        return userService.unbanUser(id);
    }
}
