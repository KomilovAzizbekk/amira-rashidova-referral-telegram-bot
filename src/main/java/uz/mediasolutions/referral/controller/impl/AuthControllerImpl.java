package uz.mediasolutions.referral.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import uz.mediasolutions.referral.controller.abs.AuthController;
import uz.mediasolutions.referral.manual.ApiResult;
import uz.mediasolutions.referral.payload.SignInDTO;
import uz.mediasolutions.referral.payload.TokenDTO;
import uz.mediasolutions.referral.service.abs.AuthService;

@RestController
@RequiredArgsConstructor
public class AuthControllerImpl implements AuthController {

    private final AuthService authService;

    @Override
    public ApiResult<TokenDTO> signIn(SignInDTO dto) {
        return authService.signIn(dto);
    }
}
