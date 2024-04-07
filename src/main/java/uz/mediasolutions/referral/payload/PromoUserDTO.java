package uz.mediasolutions.referral.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PromoUserDTO {

    private Long id;

    private TgUserDTO promoOwner;

    private String promoName;

    private TgUserDTO promoUser;

}
