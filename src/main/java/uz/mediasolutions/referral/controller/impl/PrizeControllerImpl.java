package uz.mediasolutions.referral.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;
import uz.mediasolutions.referral.controller.abs.PrizeController;
import uz.mediasolutions.referral.manual.ApiResult;
import uz.mediasolutions.referral.payload.PrizeDTO;
import uz.mediasolutions.referral.service.abs.PrizeService;

@RestController
@RequiredArgsConstructor
public class PrizeControllerImpl implements PrizeController {

    private final PrizeService prizeService;

    @Override
    public ApiResult<Page<PrizeDTO>> getAll(int page, int size, String search) {
        return prizeService.getAll(page, size, search);
    }

    @Override
    public ApiResult<PrizeDTO> getById(Long id) {
        return prizeService.getById(id);
    }

    @Override
    public ApiResult<?> add(PrizeDTO dto) {
        return prizeService.add(dto);
    }

    @Override
    public ApiResult<?> edit(Long id, PrizeDTO dto) {
        return prizeService.edit(id, dto);
    }

    @Override
    public ApiResult<?> delete(Long id) {
        return prizeService.delete(id);
    }
}
