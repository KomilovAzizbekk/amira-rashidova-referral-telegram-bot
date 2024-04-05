package uz.mediasolutions.referral.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.mediasolutions.referral.entity.TgUser;
import uz.mediasolutions.referral.enums.StepName;
import uz.mediasolutions.referral.repository.TgUserRepository;
import uz.mediasolutions.referral.utills.constants.Message;

@Service
@RequiredArgsConstructor
public class TgService extends TelegramLongPollingBot {

    private final TgUserRepository tgUserRepository;
    private final MakeService makeService;

    @Override
    public String getBotUsername() {
        return "sakaka_bot";
    }

    @Override
    public String getBotToken() {
        return "6052104473:AAEscLILevwPMcG_00PYqAf-Kpb7eIUCIGg";
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        String chatId = makeService.getChatId(update);
        TgUser tgUser = tgUserRepository.findByChatId(chatId);
        boolean existsByChatId = tgUserRepository.existsByChatId(chatId);

        System.out.println(update);

        if (existsByChatId && tgUser.isBanned()) {
            execute(new SendMessage(chatId, makeService.getMessage(Message.YOU_ARE_BANNED,
                    makeService.getUserLanguage(chatId))));
        } else if (update.hasMessage() && update.getMessage().hasText() &&
                update.getMessage().getText().equals("/start") &&
                !tgUserRepository.existsByChatId(chatId)) {
            execute(makeService.whenStart(update));
        } else if (update.hasMessage() && update.getMessage().hasText() &&
                update.getMessage().getText().equals("/start") &&
                tgUserRepository.existsByChatId(chatId)) {
            execute(makeService.whenStartForExistedUser(update));
        } else {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String text = update.getMessage().getText();
                if (makeService.getUserStep(chatId).equals(StepName.CHOOSE_LANG)) {
                    if (text.equals(makeService.getMessage(Message.UZBEK, makeService.getUserLanguage(chatId))))
                        execute(makeService.whenUz(update));
                    else if (text.equals(makeService.getMessage(Message.UZBEK, makeService.getUserLanguage(chatId))))
                        execute(makeService.whenRu(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.ENTER_NAME)) {
                    execute(makeService.whenEnterPhoneNumber(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.ENTER_PHONE_NUMBER)) {
                    execute(whenEnterPhoneNumber2(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.INCORRECT_PHONE_FORMAT)) {
                    execute(whenIncorrectPhoneFormat(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.WAITING_APPROVE) &&
                        text.equals(makeService.getMessage(Message.APPROVE, makeService.getUserLanguage(chatId)))) {
                    execute(makeService.whenMenu(update));
                }
            } else if (update.hasMessage() && update.getMessage().hasContact()) {
                if (makeService.getUserStep(chatId).equals(StepName.ENTER_PHONE_NUMBER)) {
                    execute(whenEnterPhoneNumber2(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.INCORRECT_PHONE_FORMAT)) {
                    execute(whenIncorrectPhoneFormat(update));
                }
            }
        }
    }

    public SendMessage whenEnterPhoneNumber2(Update update) throws TelegramApiException {
        String chatId = makeService.getChatId(update);
        TgUser tgUser = tgUserRepository.findByChatId(chatId);

        if (update.getMessage().hasText()) {
            if (MakeService.isValidPhoneNumber(update.getMessage().getText())) {
                String phoneNumber = update.getMessage().getText();
                tgUser.setPhoneNumber(phoneNumber);
                tgUserRepository.save(tgUser);
                return executeChangePhoneNumber(update);
            } else {
                SendMessage sendMessage = new SendMessage(makeService.getChatId(update),
                        makeService.getMessage(Message.INCORRECT_PHONE_FORMAT,
                                makeService.getUserLanguage(chatId)));
                sendMessage.setReplyMarkup(makeService.forPhoneNumber(chatId));
                makeService.setUserStep(chatId, StepName.INCORRECT_PHONE_FORMAT);
                return sendMessage;
            }
        } else {
            String phoneNumber = update.getMessage().getContact().getPhoneNumber();
            phoneNumber = phoneNumber.startsWith("+") ? phoneNumber : "+" + phoneNumber;
            tgUser.setPhoneNumber(phoneNumber);
            tgUserRepository.save(tgUser);
            return executeChangePhoneNumber(update);
        }
    }


    public SendMessage whenIncorrectPhoneFormat(Update update) throws TelegramApiException {
        return whenEnterPhoneNumber2(update);
    }

    private SendMessage executeChangePhoneNumber(Update update) throws TelegramApiException {
        String chatId = makeService.getChatId(update);

        TgUser tgUser = tgUserRepository.findByChatId(chatId);
        if (tgUser.getName() != null) {
            tgUser.setRegistered(true);
        }
        tgUserRepository.save(tgUser);

        GetChatMember getChatMember = new GetChatMember(MakeService.COURSE_CHANNEL_ID,
                update.getMessage().getChatId());
        ChatMember member = execute(getChatMember);

        if (member.getStatus().equals("member")) {
            tgUser.setCourseStudent(true);
            tgUserRepository.save(tgUser);
        }
        SendMessage sendMessage = new SendMessage(chatId,
                String.format(makeService.getMessage(Message.REGISTRATION_MESSAGE,
                                makeService.getUserLanguage(chatId)),
                        tgUser.getName()));
        sendMessage.enableHtml(true);
        sendMessage.setReplyMarkup(makeService.forExecutePhoneNumber(chatId));

        makeService.setUserStep(chatId, StepName.WAITING_APPROVE);
        return sendMessage;
    }
}
