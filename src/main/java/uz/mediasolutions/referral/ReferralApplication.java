package uz.mediasolutions.referral;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReferralApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReferralApplication.class, args);
	}

}
