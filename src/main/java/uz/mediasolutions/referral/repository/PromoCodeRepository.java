package uz.mediasolutions.referral.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.mediasolutions.referral.entity.PromoCode;

public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {

    PromoCode findByName(String name);

    boolean existsByName(String name);

    boolean existsByOwnerChatId(String chatId);

    PromoCode findByOwnerChatId(String chatId);

}
