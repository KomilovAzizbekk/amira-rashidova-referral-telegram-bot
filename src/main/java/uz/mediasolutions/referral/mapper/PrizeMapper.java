package uz.mediasolutions.referral.mapper;

import org.mapstruct.Mapper;
import uz.mediasolutions.referral.entity.Prize;
import uz.mediasolutions.referral.payload.PrizeDTO;

@Mapper(componentModel = "spring")
public interface PrizeMapper {

    PrizeDTO toDTO(Prize prize);

    Prize toEntity(PrizeDTO dto);

}
