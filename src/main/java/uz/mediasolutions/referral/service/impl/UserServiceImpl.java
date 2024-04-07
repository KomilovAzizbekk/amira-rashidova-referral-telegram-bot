package uz.mediasolutions.referral.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uz.mediasolutions.referral.entity.PromoCode;
import uz.mediasolutions.referral.entity.TgUser;
import uz.mediasolutions.referral.exceptions.RestException;
import uz.mediasolutions.referral.manual.ApiResult;
import uz.mediasolutions.referral.mapper.TgUserMapper;
import uz.mediasolutions.referral.payload.PromoUserDTO;
import uz.mediasolutions.referral.payload.TgUserDTO;
import uz.mediasolutions.referral.repository.PromoCodeRepository;
import uz.mediasolutions.referral.repository.TgUserRepository;
import uz.mediasolutions.referral.service.abs.UserService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final TgUserRepository tgUserRepository;
    private final TgUserMapper tgUserMapper;
    private final PromoCodeRepository promoCodeRepository;

    @Override
    public ApiResult<Page<TgUserDTO>> getAll(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TgUser> tgUsers;
        if (!search.equals("null")) {
             tgUsers = tgUserRepository
                    .findAllByNameContainsIgnoreCaseOrUsernameContainsIgnoreCaseOrPhoneNumberContainsIgnoreCaseOrderByCreatedAtDesc(
                            search, search, search, pageable);
        } else {
            tgUsers = tgUserRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        Page<TgUserDTO> map = tgUsers.map(tgUserMapper::toDTO);
        return ApiResult.success(map);
    }

    @Override
    public ApiResult<TgUserDTO> getById(Long id) {
        TgUser tgUser = tgUserRepository.findById(id).orElseThrow(
                () -> RestException.restThrow("ID NOT FOUND", HttpStatus.BAD_REQUEST));
        TgUserDTO dto = tgUserMapper.toDTO(tgUser);
        return ApiResult.success(dto);
    }

    @Override
    public ApiResult<?> banUser(Long id) {
        TgUser tgUser = tgUserRepository.findById(id).orElseThrow(
                () -> RestException.restThrow("ID NOT FOUND", HttpStatus.BAD_REQUEST));
        tgUser.setBanned(true);
        tgUserRepository.save(tgUser);
        return ApiResult.success("BANNED");
    }

    @Override
    public ApiResult<?> unbanUser(Long id) {
        TgUser tgUser = tgUserRepository.findById(id).orElseThrow(
                () -> RestException.restThrow("ID NOT FOUND", HttpStatus.BAD_REQUEST));
        tgUser.setBanned(false);
        tgUserRepository.save(tgUser);
        return ApiResult.success("UNBANNED");
    }

    @Override
    public ApiResult<Page<PromoUserDTO>> getAllPromoUsers(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        List<PromoCode> promoCodes;
        if (search.equals("null")) {
            promoCodes = promoCodeRepository.findAllByOrderByNameAsc();
        } else {
            promoCodes = promoCodeRepository.findAllByNameContainsIgnoreCaseOrderByNameAsc(search);
        }

        List<PromoUserDTO> promoUserDTOS = new ArrayList<>();
            for (PromoCode promoCode : promoCodes) {
                for (TgUser promoUser : promoCode.getPromoUsers()) {
                    PromoUserDTO promoUserDTO = PromoUserDTO.builder()
                            .promoName(promoCode.getName())
                            .promoOwner(tgUserMapper.toDTO(promoCode.getOwner()))
                            .promoUser(tgUserMapper.toDTO(promoUser))
                            .build();
                    promoUserDTOS.add(promoUserDTO);
            }
        }
        return ApiResult.success(new PageImpl<>(promoUserDTOS, pageable, promoUserDTOS.size()));
    }
}
