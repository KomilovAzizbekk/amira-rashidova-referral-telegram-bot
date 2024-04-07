package uz.mediasolutions.referral.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.mediasolutions.referral.entity.PromoCode;

import java.util.List;

public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {

    PromoCode findByName(String name);

    boolean existsByName(String name);

    boolean existsByOwnerChatId(String chatId);

    PromoCode findByOwnerChatId(String chatId);

    List<PromoCode> findAllByOrderByNameAsc();

}
