package uz.mediasolutions.referral.service.abs;

import org.springframework.data.domain.Page;
import uz.mediasolutions.referral.manual.ApiResult;
import uz.mediasolutions.referral.payload.PrizeDTO;

public interface PrizeService {
    ApiResult<Page<PrizeDTO>> getAll(int page, int size, String search);

    ApiResult<PrizeDTO> getById(Long id);

    ApiResult<?> add(PrizeDTO dto);

    ApiResult<?> edit(Long id, PrizeDTO dto);

    ApiResult<?> delete(Long id);

}
