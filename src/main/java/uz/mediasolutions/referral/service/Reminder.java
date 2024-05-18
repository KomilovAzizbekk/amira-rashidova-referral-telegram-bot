package uz.mediasolutions.referral.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.mediasolutions.referral.entity.TgUser;
import uz.mediasolutions.referral.repository.TgUserRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Reminder {

    private final TgUserRepository tgUserRepository;
    private final TgService tgService;

    @Scheduled(cron = "0 13 13 18 5 *")
    public void sendNewBotReminder() {
        List<TgUser> userList = tgUserRepository.findAll();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Азизлар, Сизга муҳим хабаримиз бор! \n" +
                "\n" +
                "▫\uFE0FАгар Сиз 17-май ва 18-май кунлари шу бот орқали тўлов қилиб;\n" +
                "▫\uFE0FТўлов чекининг скриншотини шу ботга юборган бўлсангиз;\n" +
                "▫\uFE0FСизнинг скриншотингиз ҳанузгача қабул қилинмаган бўлса\n" +
                "▫\uFE0FСиздан илтимос бизнинг менежер билан боғланинг! \n" +
                "▫\uFE0FБунинг учун пастдаги линк орқали ўтиб, менежерга чек скриншотини юборишни сўраймиз.\n" +
                " ➡\uFE0F @Energiya_Ongli_detoks ⬅\uFE0F");
        for (TgUser user : userList) {
            sendMessage.setChatId(user.getChatId());
            try {
                tgService.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace(); // Rethrow the exception to be handled by the caller
            }
        }
    }

}
