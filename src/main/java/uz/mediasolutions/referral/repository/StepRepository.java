package uz.mediasolutions.referral.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.mediasolutions.referral.entity.Step;
import uz.mediasolutions.referral.enums.StepName;

public interface StepRepository extends JpaRepository<Step, Long> {

    Step findByName(StepName stepName);

}
