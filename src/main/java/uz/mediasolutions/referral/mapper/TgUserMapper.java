package uz.mediasolutions.referral.mapper;

import org.mapstruct.Mapper;
import uz.mediasolutions.referral.entity.TgUser;
import uz.mediasolutions.referral.payload.TgUserDTO;

@Mapper(componentModel = "spring")
public interface TgUserMapper {

    TgUserDTO toDTO(TgUser user);

}
