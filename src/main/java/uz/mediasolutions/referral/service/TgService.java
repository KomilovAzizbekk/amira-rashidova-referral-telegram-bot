package uz.mediasolutions.referral.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.mediasolutions.referral.entity.Course;
import uz.mediasolutions.referral.entity.FileGif;
import uz.mediasolutions.referral.entity.Prize;
import uz.mediasolutions.referral.entity.TgUser;
import uz.mediasolutions.referral.enums.StepName;
import uz.mediasolutions.referral.exceptions.RestException;
import uz.mediasolutions.referral.repository.CourseRepository;
import uz.mediasolutions.referral.repository.FileGifRepository;
import uz.mediasolutions.referral.repository.PrizeRepository;
import uz.mediasolutions.referral.repository.TgUserRepository;
import uz.mediasolutions.referral.utills.constants.Message;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TgService extends TelegramLongPollingBot {

    private final TgUserRepository tgUserRepository;
    private final MakeService makeService;
    private final CourseRepository courseRepository;
    private final FileGifRepository fileGifRepository;
    private final PrizeRepository prizeRepository;

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
            execute(new SendMessage(chatId, makeService.getMessage(Message.YOU_ARE_BANNED)));
        } else {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String text = update.getMessage().getText();
                if (update.getMessage().getText().equals("/start") &&
                        !tgUserRepository.existsByChatId(chatId)) {
                    execute(makeService.whenStart(update));
                } else if (update.getMessage().getText().equals("/start") &&
                        tgUserRepository.existsByChatId(chatId)) {
                    execute(makeService.whenStartForExistedUser(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.ENTER_NAME)) {
                    execute(makeService.whenEnterPhoneNumber(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.ENTER_PHONE_NUMBER)) {
                    execute(whenEnterPhoneNumber2(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.INCORRECT_PHONE_FORMAT)) {
                    execute(whenIncorrectPhoneFormat(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.WAITING_APPROVE) &&
                        text.equals(makeService.getMessage(Message.APPROVE))) {
                    deleteMessage(update);
                    execute(makeService.whenMenu(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.ENTER_PROMO_CODE)) {
                    execute(makeService.whenEnteredPromoCode(update));
                } else if (text.equals(makeService.getMessage(Message.BACK))) {
                    deleteMessage(update);
                    execute(makeService.whenMenu(update));
                } else if (text.equals("/uploadGif")) {
                    execute(makeService.whenUpload(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.CONFIRM_PRIZE) &&
                        text.equals(makeService.getMessage(Message.YES))) {
                    execute(makeService.whenYesPrize(update));
                    execute(makeService.whenPrizeAppChannel(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.CONFIRM_PRIZE) &&
                        text.equals(makeService.getMessage(Message.NO))) {

                }
            } else if (update.hasMessage() && update.getMessage().hasContact()) {
                if (makeService.getUserStep(chatId).equals(StepName.ENTER_PHONE_NUMBER)) {
                    execute(whenEnterPhoneNumber2(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.INCORRECT_PHONE_FORMAT)) {
                    execute(whenIncorrectPhoneFormat(update));
                }
            } else if (update.hasCallbackQuery()) {
                String data = update.getCallbackQuery().getData();
                if (makeService.getUserStep(chatId).equals(StepName.CHOOSE_FROM_MENU) &&
                        data.equals("getPromoCode")) {
                    execute(makeService.deleteMessageForCallback(update));
                    execute(makeService.whenGetPromoCode(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.CHOOSE_FROM_MENU) &&
                        data.equals("usePromoCode")) {
                    execute(makeService.whenUsePromoCode(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.CHOOSE_FROM_MENU) &&
                        data.equals("myBalance")) {
                    execute(makeService.whenMyBalance(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.CHOOSE_COURSE) &&
                        getCourseName().contains(data)) {
                    execute(makeService.deleteMessageForCallback(update));
                    execute(makeService.whenChosenCourse(update, data));
                } else if ((makeService.getUserStep(chatId).equals(StepName.ENTER_PROMO_CODE) ||
                        makeService.getUserStep(chatId).equals(StepName.CHOOSE_FROM_MENU) ||
                        makeService.getUserStep(chatId).equals(StepName.CHOOSE_PRIZE)) &&
                        data.equals("back")) {
                    execute(makeService.whenMenuEdit(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.SEND_SCREENSHOT) &&
                        data.equals("back")) {
                    execute(makeService.deleteMessageForCallback(update));
                    execute(makeService.whenMenu(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.CHOOSE_PRIZE) &&
                        getPrizeNames(update).contains(data)) {
                    execute(makeService.deleteMessageForCallback(update));
                    execute(makeService.whenChosenPrize(update, data));
                } else if (data.startsWith("acceptPrize") ||
                        data.startsWith("rejectPrize")) {
                    execute(makeService.whenAcceptOrRejectPrizeApp(update, data));
                    execute(makeService.whenAcceptOrRejectPrizeAppChannel(update, data));
                }
            } else if (update.hasMessage() && update.getMessage().hasDocument()) {
                if (makeService.getUserStep(chatId).equals(StepName.UPLOAD_GIF)) {
                    execute(whenSaveGif(update));
                    execute(makeService.whenMenu(update));
                }
            }
        }
    }

    private SendMessage whenSaveGif(Update update) {
        String chatId = makeService.getChatId(update);
        String fileId = update.getMessage().getDocument().getFileId();
        FileGif gif;
        if (fileGifRepository.existsById(1L)) {
            gif = fileGifRepository.findById(1L).orElseThrow(
                    () -> RestException.restThrow("GIF NOT FOUND", HttpStatus.BAD_REQUEST));
            gif.setFileId(fileId);
        } else {
            gif = FileGif.builder()
                    .id(1L)
                    .fileId(fileId)
                    .build();
        }
        fileGifRepository.save(gif);
        return new SendMessage(chatId, makeService.getMessage(Message.GIF_SAVED));
    }

    public void deleteMessage(Update update) throws TelegramApiException {
        SendMessage sendMessageRemove = new SendMessage();
        sendMessageRemove.setChatId(update.getMessage().getChatId().toString());
        sendMessageRemove.setText(".");
        sendMessageRemove.setReplyMarkup(new ReplyKeyboardRemove(true));
        org.telegram.telegrambots.meta.api.objects.Message message = execute(sendMessageRemove);
        DeleteMessage deleteMessage = new DeleteMessage(update.getMessage().getChatId().toString(), message.getMessageId());
        execute(deleteMessage);
    }

    public List<String> getCourseName() {
        List<Course> courses = courseRepository.findAllByActiveIsTrueOrderByNumberAsc();
        List<String> courseNames = new ArrayList<>();
        for (Course course : courses) {
            courseNames.add(course.getName());
        }
        return courseNames;
    }

    public List<String> getPrizeNames(Update update) {
        List<Prize> prizes = prizeRepository.findAllByActiveIsTrueOrderByPointAsc();
        String chatId = makeService.getChatId(update);
        TgUser user = tgUserRepository.findByChatId(chatId);

        List<String> prizeNames = new ArrayList<>();
        for (Prize prize : prizes) {
            if (user.getPoints() >= prize.getPoint())
                prizeNames.add(prize.getName());
        }
        return prizeNames;
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
                        makeService.getMessage(Message.INCORRECT_PHONE_FORMAT));
                sendMessage.setReplyMarkup(makeService.forPhoneNumber());
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
                String.format(makeService.getMessage(Message.REGISTRATION_MESSAGE),
                        tgUser.getName()));
        sendMessage.enableHtml(true);
        sendMessage.setReplyMarkup(makeService.forExecutePhoneNumber());

        makeService.setUserStep(chatId, StepName.WAITING_APPROVE);
        return sendMessage;
    }
}
