package uz.mediasolutions.referral.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uz.mediasolutions.referral.entity.Prize;
import uz.mediasolutions.referral.exceptions.RestException;
import uz.mediasolutions.referral.manual.ApiResult;
import uz.mediasolutions.referral.mapper.PrizeMapper;
import uz.mediasolutions.referral.payload.PrizeDTO;
import uz.mediasolutions.referral.repository.PrizeRepository;
import uz.mediasolutions.referral.service.abs.PrizeService;

@Service
@RequiredArgsConstructor
public class PrizeServiceImpl implements PrizeService {

    private final PrizeRepository prizeRepository;
    private final PrizeMapper prizeMapper;

    @Override
    public ApiResult<Page<PrizeDTO>> getAll(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Prize> prizes;
        if (search.equals("null")) {
            prizes = prizeRepository.findAllByOrderByPointAsc(pageable);
        } else {
            prizes = prizeRepository.findAllByNameUzContainsIgnoreCaseOrNameRuContainsIgnoreCaseOrderByPointAsc(
                    search, search, pageable);
        }
        Page<PrizeDTO> mapped = prizes.map(prizeMapper::toDTO);
        return ApiResult.success(mapped);
    }

    @Override
    public ApiResult<PrizeDTO> getById(Long id) {
        Prize prize = prizeRepository.findById(id).orElseThrow(
                () -> RestException.restThrow("PRIZE NOT FOUND", HttpStatus.BAD_REQUEST));
        PrizeDTO dto = prizeMapper.toDTO(prize);
        return ApiResult.success(dto);
    }

    @Override
    public ApiResult<?> add(PrizeDTO dto) {
        Prize entity = prizeMapper.toEntity(dto);
        prizeRepository.save(entity);
        return ApiResult.success("SAVED SUCCESSFULLY");
    }

    @Override
    public ApiResult<?> edit(Long id, PrizeDTO dto) {
        Prize prize = prizeRepository.findById(id).orElseThrow(
                () -> RestException.restThrow("PRIZE NOT FOUND", HttpStatus.BAD_REQUEST));
        prize.setNameUz(dto.getNameUz());
        prize.setNameRu(dto.getNameRu());
        prize.setPoint(dto.getPoint());
        prize.setActive(dto.isActive());
        prizeRepository.save(prize);
        return ApiResult.success("EDITED SUCCESSFULLY");
    }

    @Override
    public ApiResult<?> delete(Long id) {
        try {
            prizeRepository.deleteById(id);
            return ApiResult.success("DELETED SUCCESSFULLY");
        } catch (Exception e) {
            e.printStackTrace();
            throw RestException.restThrow("CANNOT DELETE", HttpStatus.CONFLICT);
        }
    }
}
