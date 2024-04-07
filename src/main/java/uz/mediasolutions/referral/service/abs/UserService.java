package uz.mediasolutions.referral.service.abs;

import org.springframework.data.domain.Page;
import uz.mediasolutions.referral.manual.ApiResult;
import uz.mediasolutions.referral.payload.PromoUserDTO;
import uz.mediasolutions.referral.payload.TgUserDTO;

public interface UserService {
    ApiResult<Page<TgUserDTO>> getAll(int page, int size, String search);

    ApiResult<TgUserDTO> getById(Long id);

    ApiResult<?> banUser(Long id);

    ApiResult<?> unbanUser(Long id);

    ApiResult<Page<PromoUserDTO>> getAllPromoUsers(int page, int size, String search);

}
