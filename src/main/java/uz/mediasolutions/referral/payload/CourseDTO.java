package uz.mediasolutions.referral.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseDTO {

    private Long id;

    private Integer number;

    private String nameUz;

    private String nameRu;

    private Integer price;

    private boolean active;

    private String channelId;

}
