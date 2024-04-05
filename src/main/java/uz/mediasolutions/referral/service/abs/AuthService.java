package uz.mediasolutions.referral.service.abs;

import uz.mediasolutions.referral.entity.User;
import uz.mediasolutions.referral.manual.ApiResult;
import uz.mediasolutions.referral.payload.SignInDTO;
import uz.mediasolutions.referral.payload.TokenDTO;

public interface AuthService {

    ApiResult<TokenDTO> signIn(SignInDTO signInDTO);

    TokenDTO generateToken(User user);

    User checkUsernameAndPasswordAndEtcAndSetAuthenticationOrThrow(String username, String password);


}
