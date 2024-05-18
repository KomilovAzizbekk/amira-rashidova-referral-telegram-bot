package uz.mediasolutions.referral.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.ChatInviteLink;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.mediasolutions.referral.entity.*;
import uz.mediasolutions.referral.enums.StepName;
import uz.mediasolutions.referral.exceptions.RestException;
import uz.mediasolutions.referral.repository.*;
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
//    private final PrizeRepository prizeRepository;
    private final CoursePaymentRepository coursePaymentRepository;
//    private final PromoCodeRepository promoCodeRepository;

    @Override
    public String getBotUsername() {
//        return "sakaka_bot";
        return "onglikod_bot";
//        return "Azzzyy_bot";
    }

    @Override
    public String getBotToken() {
//        return "6052104473:AAEscLILevwPMcG_00PYqAf-Kpb7eIUCIGg";
        return "6862539261:AAHzpuw_OrYkBbM1D4JrSQQ9ycRw5RHBVRM";
//        return "5618168254:AAELRZcKMHGbxHqgmmc5E6ucalGJ9sXvGhk";
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
//                } else if (makeService.getUserStep(chatId).equals(StepName.ENTER_PROMO_CODE) &&
//                        !text.equals(makeService.getMessage(Message.BACK))) {
//                    execute(makeService.whenEnteredPromoCode(update));
                } else if (text.equals(makeService.getMessage(Message.BACK))) {
                    deleteMessage(update);
                    execute(makeService.whenMenu(update));
                } else if (text.equals("/uploadGif")) {
                    execute(makeService.whenUpload(update));
                }
//                else if (makeService.getUserStep(chatId).equals(StepName.CONFIRM_PRIZE) &&
//                        text.equals(makeService.getMessage(Message.YES))) {
//                    execute(makeService.whenYesPrize(update));
//                    execute(makeService.whenPrizeAppChannel(update));
//                } else if (makeService.getUserStep(chatId).equals(StepName.CONFIRM_PRIZE) &&
//                        text.equals(makeService.getMessage(Message.NO))) {
//                    deleteMessage(update);
//                    execute(makeService.whenMyBalanceInNo(update));
//                }
                else if (makeService.getUserStep(chatId).equals(StepName.SEND_SCREENSHOT)) {
                    execute(makeService.whenNotScreenshot(update));
                }
            } else if (update.hasMessage() && update.getMessage().hasContact()) {
                if (makeService.getUserStep(chatId).equals(StepName.ENTER_PHONE_NUMBER)) {
                    execute(whenEnterPhoneNumber2(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.INCORRECT_PHONE_FORMAT)) {
                    execute(whenIncorrectPhoneFormat(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.SEND_SCREENSHOT)) {
                    execute(makeService.whenNotScreenshot(update));
                }
            } else if (update.hasCallbackQuery()) {
                String data = update.getCallbackQuery().getData();
                if (data.startsWith("acceptPayment") ||
                        data.startsWith("rejectPayment")) {
                    execute(makeService.deleteMessageForCallback(update));
                    execute(whenAcceptOrRejectPaymentApp(data));
                    execute(makeService.whenAcceptOrRejectPaymentAppChannel(data));
                }
//                else if (data.startsWith("acceptPrize") ||
//                        data.startsWith("rejectPrize")) {
//                    execute(makeService.whenAcceptOrRejectPrizeApp(data));
//                    execute(makeService.whenAcceptOrRejectPrizeAppChannel(update, data));
//                } else if (data.equals("getPromoCode")) {
//                    execute(makeService.whenGetPromoCode(update));
//                } else if (data.equals("usePromoCode")) {
//                    execute(makeService.whenUsePromoCode(update));
//                } else if (data.equals("myBalance")) {
//                    execute(makeService.whenMyBalance(update));
//                }
                else if (data.equals("pay")) {
                    execute(makeService.whenEnteredPromoCode(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.CHOOSE_COURSE) &&
                        data.equals("goPayment")) {
                    if (fileGifRepository.existsById(1L)) {
                        execute(makeService.deleteMessageForCallback(update));
                        execute(makeService.whenChosenCourse(update, data));
                    } else {
                        execute(makeService.whenChosenCourse1(update, data));
                    }
                } else if (data.equals("back")) {
                    execute(makeService.whenMenuEdit(update));
                } else if (data.equals("back1")) {
                    execute(makeService.deleteMessageForCallback(update));
                    execute(makeService.whenMenu(update));
                }
//                else if (makeService.getUserStep(chatId).equals(StepName.CHOOSE_PRIZE) &&
//                        getPrizeNames(update).contains(data)) {
//                    execute(makeService.deleteMessageForCallback(update));
//                    execute(makeService.whenChosenPrize(update, data));
//                }
            } else if (update.hasMessage() && update.getMessage().hasDocument()) {
                if (makeService.getUserStep(chatId).equals(StepName.UPLOAD_GIF)) {
                    execute(whenSaveGif(update));
                } else if (makeService.getUserStep(chatId).equals(StepName.SEND_SCREENSHOT)) {
                    execute(makeService.whenNotScreenshot(update));
                }
            } else if (update.hasMessage() && update.getMessage().hasPhoto()) {
                if (makeService.getUserStep(chatId).equals(StepName.SEND_SCREENSHOT)) {
                    execute(makeService.whenSendScreenshot(update));
                    execute(makeService.whenSendPaymentAppToChannel(update));
                }
            }
        }
    }

    public SendMessage whenAcceptOrRejectPaymentApp(String data) throws TelegramApiException {
        Long paymentId = Long.valueOf(data.substring(13));
        String action = data.substring(0, 13);
        CoursePayment coursePayment = coursePaymentRepository.findById(paymentId).orElseThrow(
                () -> RestException.restThrow("PAYMENT NOT FOUND", HttpStatus.BAD_REQUEST));

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(coursePayment.getTgUser().getChatId());

        if (action.equals("acceptPayment")) {
            coursePayment.setAccepted(true);
            coursePaymentRepository.save(coursePayment);
            sendMessage.setText(String.format(makeService.getMessage(Message.ACCEPT_PAYMENT_APP), coursePayment.getId()));
            sendMessage.setReplyMarkup(forCourseLink(coursePayment.getCourse()));

            //Adding +1 point to the owner of the promo code
//            TgUser owner = coursePayment.getTgUser().getUsingPromo().getOwner();
//            if (!Objects.equals(coursePayment.getTgUser().getChatId(), owner.getChatId())) {
//                owner.setPoints(owner.getPoints() + 1);
//                tgUserRepository.save(owner);
//            }

            //Adding user to course
            Course course = coursePayment.getCourse();
            List<TgUser> users = course.getUsers();
            users.add(coursePayment.getTgUser());
            course.setUsers(users);
            courseRepository.save(course);

            //Adding user to promo user's list
//            PromoCode usingPromo = coursePayment.getTgUser().getUsingPromo();
//            List<TgUser> promoUsers = usingPromo.getPromoUsers();
//            promoUsers.add(coursePayment.getTgUser());
//            usingPromo.setPromoUsers(promoUsers);
//            promoCodeRepository.save(usingPromo);

        } else {
            coursePayment.setAccepted(false);
            coursePaymentRepository.save(coursePayment);
            sendMessage.setText(String.format(makeService.getMessage(Message.REJECT_PAYMENT_APP), coursePayment.getId()));
        }
        sendMessage.enableHtml(true);
        return sendMessage;
    }

    private String generateInviteLink(String channelId) throws TelegramApiException {
        CreateChatInviteLink link = new CreateChatInviteLink(channelId);
        link.setMemberLimit(1);
        ChatInviteLink execute = execute(link);
        return execute.getInviteLink();
    }

    private InlineKeyboardMarkup forCourseLink(Course course) throws TelegramApiException {

        String s = generateInviteLink(course.getChannelId());

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();

        button1.setText(makeService.getMessage(Message.ENTER_TO_CHANNEL));
        button1.setUrl(s);

        row1.add(button1);
        rowsInline.add(row1);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
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

//    public List<String> getPrizeNames(Update update) {
//        List<Prize> prizes = prizeRepository.findAllByActiveIsTrueOrderByPointAsc();
//        String chatId = makeService.getChatId(update);
//        TgUser user = tgUserRepository.findByChatId(chatId);
//
//        List<String> prizeNames = new ArrayList<>();
//        for (Prize prize : prizes) {
//            if (user.getPoints() >= prize.getPoint())
//                prizeNames.add(prize.getName());
//        }
//        return prizeNames;
//    }

    public SendMessage whenEnterPhoneNumber2(Update update) {
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


    public SendMessage whenIncorrectPhoneFormat(Update update) {
        return whenEnterPhoneNumber2(update);
    }

    private SendMessage executeChangePhoneNumber(Update update) {
        String chatId = makeService.getChatId(update);

        TgUser tgUser = tgUserRepository.findByChatId(chatId);
        if (tgUser.getName() != null) {
            tgUser.setRegistered(true);
        }
        tgUserRepository.save(tgUser);

        SendMessage sendMessage = new SendMessage(chatId,
                String.format(makeService.getMessage(Message.REGISTRATION_MESSAGE),
                        tgUser.getName(),
                        makeService.getMessage(Message.MENU_TELEGRAPH_LINK)));
        sendMessage.enableHtml(true);
        sendMessage.setReplyMarkup(makeService.forExecutePhoneNumber());

        makeService.setUserStep(chatId, StepName.WAITING_APPROVE);
        return sendMessage;
    }

//    @SneakyThrows
//    public void check(Update update) {
//        String chatId = makeService.getChatId(update);
//
//        TgUser tgUser = tgUserRepository.findByChatId(chatId);
//
////        String COURSE_CHANNEL_ID_1 = "-1001903287909";
////        ChatMember member1 = getChatMember(COURSE_CHANNEL_ID_1, update);
//
//        String COURSE_CHANNEL_ID_1 = "-1001991925073";
//        ChatMember member1 = getChatMember(COURSE_CHANNEL_ID_1, update);
//        ArrayList<ChatMember> admin1 = getChatAdmin(COURSE_CHANNEL_ID_1);
//        String COURSE_CHANNEL_ID_2 = "-1002038255157";
//        ChatMember member2 = getChatMember(COURSE_CHANNEL_ID_2, update);
//        ArrayList<ChatMember> admin2 = getChatAdmin(COURSE_CHANNEL_ID_1);
//        String COURSE_CHANNEL_ID_3 = "-1001713012851";
//        ChatMember member3 = getChatMember(COURSE_CHANNEL_ID_3, update);
//        ArrayList<ChatMember> admin3 = getChatAdmin(COURSE_CHANNEL_ID_1);
//        String COURSE_CHANNEL_ID_4 = "-1002132650471";
//        ChatMember member4 = getChatMember(COURSE_CHANNEL_ID_4, update);
//        ArrayList<ChatMember> admin4 = getChatAdmin(COURSE_CHANNEL_ID_1);
//
//        if (member1.getStatus().equals("member") ||
//                member2.getStatus().equals("member") ||
//                member3.getStatus().equals("member") ||
//                member4.getStatus().equals("member")
//        ) {
//            tgUser.setCourseStudent(true);
//            tgUserRepository.save(tgUser);
//        }
//
//        CheckAdmins(chatId, tgUser, admin1, admin2);
//        CheckAdmins(chatId, tgUser, admin3, admin4);
//    }
//
//    private void CheckAdmins(String chatId, TgUser tgUser, ArrayList<ChatMember> admin3, ArrayList<ChatMember> admin4) {
//        for (ChatMember admin : admin3) {
//            if (admin.getUser().getId().toString().equals(chatId)) {
//                tgUser.setCourseStudent(true);
//                tgUserRepository.save(tgUser);
//            }
//        }
//        for (ChatMember admin : admin4) {
//            if (admin.getUser().getId().toString().equals(chatId)) {
//                tgUser.setCourseStudent(true);
//                tgUserRepository.save(tgUser);
//            }
//        }
//    }
//
//    private ArrayList<ChatMember> getChatAdmin(String channelId) throws TelegramApiException {
//        GetChatAdministrators getChatAdministrators = new GetChatAdministrators(channelId);
//        return execute(getChatAdministrators);
//    }
//
//    private ChatMember getChatMember(String channelId, Update update) throws TelegramApiException {
//        GetChatMember getChatMember = new GetChatMember(channelId,
//                update.getMessage().getChatId());
//        return execute(getChatMember);
//    }
}
