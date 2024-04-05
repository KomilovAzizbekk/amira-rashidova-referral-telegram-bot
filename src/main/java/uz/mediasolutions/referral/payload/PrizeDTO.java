package uz.mediasolutions.referral.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PrizeDTO {

    private Long id;

    private String nameUz;

    private String nameRu;

    private Integer point;

    private boolean active;

}
