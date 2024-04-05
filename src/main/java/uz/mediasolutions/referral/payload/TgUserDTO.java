package uz.mediasolutions.referral.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TgUserDTO {

    private Long id;

    private String chatId;

    private String name;

    private String phoneNumber;

    private String username;

    private boolean registered;

    private boolean admin;

    private boolean banned;

}
