package uz.mediasolutions.referral.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.mediasolutions.referral.entity.Language;
import uz.mediasolutions.referral.enums.LanguageName;

public interface LanguageRepository extends JpaRepository<Language, Long> {

    Language findByName(LanguageName languageName);
}
