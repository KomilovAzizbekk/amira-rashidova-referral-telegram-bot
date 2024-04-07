package uz.mediasolutions.referral.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.ExportChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.mediasolutions.referral.entity.*;
import uz.mediasolutions.referral.enums.StepName;
import uz.mediasolutions.referral.exceptions.RestException;
import uz.mediasolutions.referral.repository.*;
import uz.mediasolutions.referral.utills.constants.Message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MakeService {

    private final LanguageRepositoryPs languageRepositoryPs;
    private final TgUserRepository tgUserRepository;
    private final StepRepository stepRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final CourseRepository courseRepository;
    private final PrizeRepository prizeRepository;
    private final FileGifRepository fileGifRepository;
    private final PrizeAppRepository prizeAppRepository;
    private final CoursePaymentRepository coursePaymentRepository;

    public static final String UZ = "UZ";
    public static final String COURSE_CHANNEL_ID = "-1001903287909";
    public static final String CHANNEL_ID = "-1001903287909";

    public String getMessage(String key) {
        List<LanguagePs> allByLanguage = languageRepositoryPs.findAll();
        if (!allByLanguage.isEmpty()) {
            for (LanguagePs languagePs : allByLanguage) {
                for (LanguageSourcePs languageSourceP : languagePs.getLanguageSourcePs()) {
                    if (languageSourceP.getTranslation() != null &&
                            languageSourceP.getLanguage().equals(UZ) &&
                            languagePs.getKey().equals(key)) {
                        return languageSourceP.getTranslation();
                    }
                }
            }
        }
        return null;
    }

    public void setUserStep(String chatId, StepName stepName) {
        TgUser tgUser = tgUserRepository.findByChatId(chatId);
        tgUser.setStep(stepRepository.findByName(stepName));
        tgUserRepository.save(tgUser);
    }

    public StepName getUserStep(String chatId) {
        TgUser tgUser = tgUserRepository.findByChatId(chatId);
        return tgUser.getStep().getName();
    }

    static boolean isValidPhoneNumber(String phoneNumber) {
        String regex = "\\+998[1-9]\\d{8}";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(phoneNumber);

        return matcher.matches();
    }

    public String getChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId().toString();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId().toString();
        }
        return "";
    }

    public String getUsername(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom().getUserName();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom().getUserName();
        }
        return "";
    }


    public SendMessage whenStart(Update update) {
        String chatId = getChatId(update);
        if (!tgUserRepository.existsByChatId(chatId)) {
            TgUser tgUser = TgUser.builder().chatId(chatId)
                    .admin(false)
                    .registered(false)
                    .banned(false)
                    .username(getUsername(update))
                    .chatId(chatId)
                    .points(0)
                    .build();
            tgUserRepository.save(tgUser);
        }
        SendMessage sendMessage = new SendMessage(getChatId(update),
                getMessage(Message.ENTER_NAME));
        setUserStep(chatId, StepName.ENTER_NAME);
        return sendMessage;
    }


    public SendMessage whenStartForExistedUser(Update update) {
        String chatId = getChatId(update);
        TgUser user = tgUserRepository.findByChatId(chatId);

        if (user.getName() == null) {
            return whenStart(update);
        } else if (user.getPhoneNumber() == null) {
            return whenEnterPhoneNumber2(update);
        } else {
            return whenMenu(update);
        }
    }

    public SendMessage whenEnterPhoneNumber(Update update) {
        String chatId = getChatId(update);

        TgUser user = tgUserRepository.findByChatId(chatId);
        user.setName(update.getMessage().getText());
        user.setRepetition(tgUserRepository.countAllByNameContainsIgnoreCase(update.getMessage().getText())+1);
        tgUserRepository.save(user);

        SendMessage sendMessage = new SendMessage(chatId, getMessage(Message.ENTER_PHONE_NUMBER));
        sendMessage.setReplyMarkup(forPhoneNumber());
        setUserStep(chatId, StepName.ENTER_PHONE_NUMBER);
        return sendMessage;
    }

    public SendMessage whenEnterPhoneNumber2(Update update) {
        String chatId = getChatId(update);

        SendMessage sendMessage = new SendMessage(chatId, getMessage(Message.ENTER_PHONE_NUMBER));
        sendMessage.setReplyMarkup(forPhoneNumber());
        setUserStep(chatId, StepName.ENTER_PHONE_NUMBER);
        return sendMessage;
    }

    ReplyKeyboardMarkup forPhoneNumber() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rowList = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();

        KeyboardButton button1 = new KeyboardButton();

        button1.setText(getMessage(Message.SHARE_PHONE_NUMBER));
        button1.setRequestContact(true);

        row1.add(button1);

        rowList.add(row1);
        markup.setKeyboard(rowList);
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        return markup;
    }

    ReplyKeyboardMarkup forExecutePhoneNumber() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rowList = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();

        KeyboardButton button1 = new KeyboardButton();

        button1.setText(getMessage(Message.APPROVE));

        row1.add(button1);

        rowList.add(row1);
        markup.setKeyboard(rowList);
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        return markup;
    }

    public EditMessageText whenMenuEdit(Update update) {
        String chatId = getChatId(update);
        TgUser user = tgUserRepository.findByChatId(chatId);

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        if (user.isCourseStudent()) {
            editMessageText.setText(String.format(getMessage(Message.WELCOME_TO_MENU_STUDENT),
                    user.getName()));
        } else {
            editMessageText.setText(String.format(getMessage(Message.WELCOME_TO_MENU_STRANGER),
                    user.getName(),
                    "Bu yerda Telegraph link bo'lishi mumkin edi:)"));
        }
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        editMessageText.enableHtml(true);
        editMessageText.setReplyMarkup(forMenu(user.isCourseStudent()));
        setUserStep(chatId, StepName.CHOOSE_FROM_MENU);
        return editMessageText;
    }

    public SendMessage whenMenu(Update update) {
        String chatId = getChatId(update);
        TgUser user = tgUserRepository.findByChatId(chatId);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        if (user.isCourseStudent()) {
            sendMessage.setText(String.format(getMessage(Message.WELCOME_TO_MENU_STUDENT),
                    user.getName()));
        } else {
            sendMessage.setText(String.format(getMessage(Message.WELCOME_TO_MENU_STRANGER),
                    user.getName(),
                    "Bu yerda Telegraph link bo'lishi mumkin edi:)"));
        }

        sendMessage.enableHtml(true);
        sendMessage.setReplyMarkup(forMenu(user.isCourseStudent()));
        setUserStep(chatId, StepName.CHOOSE_FROM_MENU);
        return sendMessage;
    }

    private InlineKeyboardMarkup forMenu(boolean courseStudent) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        InlineKeyboardButton button4 = new InlineKeyboardButton();

        if (courseStudent) {
            button1.setText(getMessage(Message.GET_PRIVATE_PROMO_CODE));
            button2.setText(getMessage(Message.PRIZES_LIST));
            button3.setText(getMessage(Message.MENU_SUG_COMP));
            button4.setText(getMessage(Message.MY_BALANCE));

            button1.setCallbackData("getPromoCode");
            button2.setUrl("https://t.me/nexrp");
            button3.setUrl("https://t.me/nexrp");
            button4.setCallbackData("myBalance");

            row1.add(button1);
            row2.add(button2);
            row3.add(button3);
            row3.add(button4);
        } else {
            button1.setText(getMessage(Message.USE_PROMO_CODE));
            button2.setText(getMessage(Message.GET_PRIVATE_PROMO_CODE));
            button3.setText(getMessage(Message.MENU_SUG_COMP));

            button1.setCallbackData("usePromoCode");
            button2.setCallbackData("getPromoCode");
            button3.setUrl("https://t.me/nexrp");

            row1.add(button1);
            row2.add(button2);
            row3.add(button3);
        }

        rowsInline.add(row1);
        rowsInline.add(row2);
        rowsInline.add(row3);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }

    private String getPromoNumber(Integer number) {
        return number < 10 ? "0" + number : String.valueOf(number);
    }

    public SendMessage whenGetPromoCode(Update update) {
        String chatId = getChatId(update);
        TgUser user = tgUserRepository.findByChatId(chatId);
        String promoNumber = getPromoNumber(user.getRepetition());
        PromoCode saved;
        if (!promoCodeRepository.existsByOwnerChatId(chatId)) {
            PromoCode promocode = PromoCode.builder()
                    .active(true)
                    .owner(user)
                    .name(user.getName().toUpperCase() + promoNumber)
                    .build();
            saved = promoCodeRepository.save(promocode);
        } else {
            saved = promoCodeRepository.findByOwnerChatId(chatId);
        }
        SendMessage sendMessage = new SendMessage(chatId,
                String.format(getMessage(Message.PROMO_MESSAGE),
                        saved.getName()));
        sendMessage.enableHtml(true);
        sendMessage.setReplyMarkup(forGetPromoCode());
        setUserStep(chatId, StepName.GET_PROMO);
        return sendMessage;
    }

    ReplyKeyboardMarkup forGetPromoCode() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rowList = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();

        KeyboardButton button1 = new KeyboardButton();

        button1.setText(getMessage(Message.BACK));

        row1.add(button1);

        rowList.add(row1);
        markup.setKeyboard(rowList);
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        return markup;
    }

    public DeleteMessage deleteMessageForCallback(Update update) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(getChatId(update));
        deleteMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        return deleteMessage;
    }

    public EditMessageText whenUsePromoCode(Update update) {
        String chatId = getChatId(update);

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        editMessageText.setText(getMessage(Message.ENTER_PROMO_CODE));
        setUserStep(chatId, StepName.ENTER_PROMO_CODE);
        return editMessageText;
    }

    public SendMessage whenEnteredPromoCode(Update update) {
        String chatId = getChatId(update);
        TgUser user = tgUserRepository.findByChatId(chatId);
        String name = update.getMessage().getText();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        if (!promoCodeRepository.existsByName(name)) {
            sendMessage.setText(getMessage(Message.WRONG_PROMO_CODE));
        } else {
            PromoCode promoCode = promoCodeRepository.findByName(name);

            //Adding promo to user temporarily
            user.setUsingPromo(promoCode);
            tgUserRepository.save(user);

            if (courseRepository.findAllByActiveIsTrueOrderByNumberAsc().isEmpty()) {
                sendMessage.setText(getMessage(Message.NO_ACTIVE_COURSE));
                sendMessage.setReplyMarkup(forNotActiveCourse());
            } else {
                sendMessage.setText(String.format(getMessage(Message.CHOOSE_COURSE),
                        coursesWithDiscount()));
                sendMessage.setReplyMarkup(forEnteredPromoCode());
                sendMessage.enableHtml(true);
                setUserStep(chatId, StepName.CHOOSE_COURSE);
            }
        }
        return sendMessage;
    }

    private InlineKeyboardMarkup forNotActiveCourse() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();

        button1.setText(getMessage(Message.BACK));
        button1.setCallbackData("back");

        row1.add(button1);
        rowsInline.add(row1);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }

    private String coursesWithDiscount() {
        StringBuilder format = new StringBuilder();
        List<Course> courses = courseRepository.findAllByActiveIsTrueOrderByNumberAsc();
        for (Course course : courses) {
            format.append(String.format(getMessage(Message.COURSE_FORMAT),
                    course.getName(),
                    course.getPrice(),
                    course.getDiscount())).append("\n\n");
        }
        return format.toString();
    }

    private InlineKeyboardMarkup forEnteredPromoCode() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<Course> courses = courseRepository.findAllByActiveIsTrueOrderByNumberAsc();

        for (Course course : courses) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(course.getName());
            button.setCallbackData(course.getName());
            row.add(button);
            rowsInline.add(row);
        }

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }


    public SendDocument whenChosenCourse(Update update, String data) {
        String chatId = getChatId(update);
        TgUser user = tgUserRepository.findByChatId(chatId);

        Course course = courseRepository.findByName(data);
        user.setTempCourse(course);
        tgUserRepository.save(user);

        SendDocument sendDocument = new SendDocument();

        FileGif fileGif = fileGifRepository.findById(1L).orElseThrow(
                    () -> RestException.restThrow("GIF NOT FOUND", HttpStatus.BAD_REQUEST));

        sendDocument.setDocument(new InputFile(fileGif.getFileId()));
        sendDocument.setChatId(chatId);
        sendDocument.setCaption(String.format(getMessage(Message.CHOSEN_COURSE_MSG),
                course.getName(),
                course.getPrice(),
                course.getPrice() - course.getDiscount()));
        sendDocument.setReplyMarkup(forChosenCourse());
        sendDocument.setParseMode("HTML");
        setUserStep(chatId, StepName.SEND_SCREENSHOT);
        return sendDocument;
    }

    public EditMessageText whenChosenCourse1(Update update, String data) {
        String chatId = getChatId(update);
        TgUser user = tgUserRepository.findByChatId(chatId);

        Course course = courseRepository.findByName(data);
        user.setTempCourse(course);
        tgUserRepository.save(user);

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setText(String.format(getMessage(Message.CHOSEN_COURSE_MSG),
                course.getName(),
                course.getPrice(),
                course.getPrice() - course.getDiscount()));
        editMessageText.setReplyMarkup(forChosenCourse());
        editMessageText.enableHtml(true);
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        setUserStep(chatId, StepName.SEND_SCREENSHOT);
        return editMessageText;
    }

    private InlineKeyboardMarkup forChosenCourse() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();

        button1.setText(getMessage(Message.PAY_CLICK));
        button2.setText(getMessage(Message.PAY_PAYME));
        button3.setText(getMessage(Message.BACK));

        button1.setUrl("https://t.me/nexrp");
        button2.setUrl("https://t.me/nexrp");
        button3.setCallbackData("back1");


        row1.add(button1);
        row1.add(button2);
        row2.add(button3);

        rowsInline.add(row1);
        rowsInline.add(row2);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }

    public SendMessage whenUpload(Update update) {
            String chatId = getChatId(update);
//            if (Objects.equals(chatId, "285710521") || Objects.equals(chatId, "6931160281")
//                    || Objects.equals(chatId, "1302908674")) {
//                setUserStep(chatId, StepName.UPLOAD_GIF);
//                return new SendMessage(chatId, getMessage(Message.UPLOAD_FILE));
//            } else {
//                return new SendMessage(chatId, getMessage(Message.CANNOT_SAVE_FILE));
//            }
        setUserStep(chatId, StepName.UPLOAD_GIF);
        return new SendMessage(chatId, getMessage(Message.UPLOAD_GIF));
        }

    public EditMessageText whenMyBalance(Update update) {
        String chatId = getChatId(update);
        TgUser user = tgUserRepository.findByChatId(chatId);
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        if (prizeRepository.findAllByActiveIsTrueOrderByPointAsc().isEmpty()) {
            editMessageText.setText(String.format(getMessage(Message.NO_ACTIVE_PRIZES), user.getPoints()));
            editMessageText.setReplyMarkup(forNotActiveCourse());
        } else {
            editMessageText.setText(String.format(getMessage(Message.BALANCE_MSG), user.getPoints()));
            editMessageText.setReplyMarkup(forMyBalance(chatId));
        }
        editMessageText.enableHtml(true);
        setUserStep(chatId, StepName.CHOOSE_PRIZE);
        return editMessageText;
    }

    public SendMessage whenMyBalanceInNo(Update update) {
        String chatId = getChatId(update);
        TgUser user = tgUserRepository.findByChatId(chatId);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        if (prizeRepository.findAllByActiveIsTrueOrderByPointAsc().isEmpty()) {
            sendMessage.setText(String.format(getMessage(Message.NO_ACTIVE_PRIZES), user.getPoints()));
            sendMessage.setReplyMarkup(forNotActiveCourse());
        } else {
            sendMessage.setText(String.format(getMessage(Message.BALANCE_MSG), user.getPoints()));
            sendMessage.setReplyMarkup(forMyBalance(chatId));
        }
        sendMessage.enableHtml(true);
        setUserStep(chatId, StepName.CHOOSE_PRIZE);
        return sendMessage;
    }

    private InlineKeyboardMarkup forMyBalance(String chatId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<Prize> prizes = prizeRepository.findAllByActiveIsTrueOrderByPointAsc();
        TgUser user = tgUserRepository.findByChatId(chatId);

        for (Prize prize : prizes) {
            if (user.getPoints() >= prize.getPoint()) {
                List<InlineKeyboardButton> row = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(String.format(getMessage(Message.PRIZE_FORMAT),
                        prize.getName(),
                        prize.getPoint()));
                button.setCallbackData(prize.getName());
                row.add(button);
                rowsInline.add(row);
            }
        }
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText(getMessage(Message.BACK));
        button1.setCallbackData("back");
        row1.add(button1);
        rowsInline.add(row1);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }

    public SendMessage whenChosenPrize(Update update, String data) {
        String chatId = getChatId(update);

        Prize prize = prizeRepository.findByName(data);

        TgUser user = tgUserRepository.findByChatId(chatId);
        user.setUsingPrize(prize);
        tgUserRepository.save(user);
        SendMessage sendMessage = new SendMessage(chatId,
                String.format(getMessage(Message.CONFIRM_PRIZE),
                        user.getPoints(),
                        prize.getName()));
        sendMessage.enableHtml(true);
        sendMessage.setReplyMarkup(forChosenPrize());
        setUserStep(chatId, StepName.CONFIRM_PRIZE);
        return sendMessage;
    }

    ReplyKeyboardMarkup forChosenPrize() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rowList = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();

        KeyboardButton button1 = new KeyboardButton();
        KeyboardButton button2 = new KeyboardButton();

        button1.setText(getMessage(Message.YES));
        button2.setText(getMessage(Message.NO));

        row1.add(button1);
        row1.add(button2);

        rowList.add(row1);
        markup.setKeyboard(rowList);
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        return markup;
    }

    public SendMessage whenYesPrize(Update update) {
        String chatId = getChatId(update);

        SendMessage sendMessage = new SendMessage(chatId,
                getMessage(Message.APP_RECEIVED));
        sendMessage.setReplyMarkup(forGetPromoCode());
        setUserStep(chatId, StepName.PENDING_PRIZE_APP);
        return sendMessage;
    }


    public SendMessage whenPrizeAppChannel(Update update) {
        String chatId = getChatId(update);
        TgUser user = tgUserRepository.findByChatId(chatId);
        PrizeApp prizeApp = PrizeApp.builder()
                .prize(user.getUsingPrize())
                .user(user)
                .build();
        PrizeApp saved = prizeAppRepository.save(prizeApp);
        PromoCode promoCode = promoCodeRepository.findByOwnerChatId(chatId);

        SendMessage sendMessage = new SendMessage(CHANNEL_ID,
                String.format(getMessage(Message.PRIZE_APP),
                        saved.getId(),
                        saved.getUser().getName(),
                        promoCode != null ? promoCode.getName() : getMessage(Message.NO_PROMO),
                        saved.getUser().getPhoneNumber(),
                        saved.getUser().getPoints(),
                        promoCode == null ? getMessage(Message.NO_PROMO) : getReferralUsers(promoCode),
                        saved.getPrize().getName(),
                        saved.getPrize().getPoint(),
                        getMessage(Message.PENDING)));
        sendMessage.enableHtml(true);
        sendMessage.setReplyMarkup(forPrizeAppChannel(saved.getId()));
        return sendMessage;
    }

    private InlineKeyboardMarkup forPrizeAppChannel(Long prizeAppId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();

        button1.setText(getMessage(Message.ACCEPT));
        button2.setText(getMessage(Message.REJECT));

        button1.setCallbackData("acceptPrize" + prizeAppId);
        button2.setCallbackData("rejectPrize" + prizeAppId);


        row1.add(button1);
        row1.add(button2);

        rowsInline.add(row1);
        rowsInline.add(row2);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }


    public SendMessage whenAcceptOrRejectPrizeApp(String data) {
        Long prizeAppId = Long.valueOf(data.substring(11));
        String action = data.substring(0, 11);

        PrizeApp prizeApp = prizeAppRepository.findById(prizeAppId).orElseThrow(
                () -> RestException.restThrow("APP NOT FOUND", HttpStatus.BAD_REQUEST));

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(prizeApp.getUser().getChatId());

        if (action.equals("acceptPrize")) {
            prizeApp.setAccepted(true);
            sendMessage.setText(String.format(getMessage(Message.ACCEPT_PRIZE_APP), prizeAppId));
        } else {
            prizeApp.setAccepted(false);
            sendMessage.setText(String.format(getMessage(Message.REJECT_PRIZE_APP), prizeAppId));
        }
        prizeAppRepository.save(prizeApp);
        sendMessage.enableHtml(true);
        return sendMessage;
    }

    public EditMessageText whenAcceptOrRejectPrizeAppChannel(Update update, String data) {
        Long prizeAppId = Long.valueOf(data.substring(11));
        PrizeApp prizeApp = prizeAppRepository.findById(prizeAppId).orElseThrow(
                () -> RestException.restThrow("APP NOT FOUND", HttpStatus.BAD_REQUEST));

        PromoCode promoCode = promoCodeRepository.findByOwnerChatId(prizeApp.getUser().getChatId());

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(CHANNEL_ID);
        editMessageText.setText(String.format(getMessage(Message.PRIZE_APP),
                prizeApp.getId(),
                prizeApp.getUser().getName(),
                promoCode != null ? promoCode.getName() : getMessage(Message.NO_PROMO),
                prizeApp.getUser().getPhoneNumber(),
                prizeApp.getUser().getPoints(),
                promoCode == null ? getMessage(Message.NO_PROMO) : getReferralUsers(promoCode),
                prizeApp.getPrize().getName(),
                prizeApp.getPrize().getPoint(),
                prizeApp.getAccepted() ? getMessage(Message.ACCEPTED) : getMessage(Message.REJECTED)));
        editMessageText.enableHtml(true);
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        return editMessageText;
    }

    private String getReferralUsers(PromoCode promoCode) {
        List<TgUser> promoUsers = promoCode.getPromoUsers();
        StringBuilder forma = new StringBuilder();
        if (promoUsers.isEmpty()) {
            forma = new StringBuilder(getMessage(Message.NO_PROMO));
        } else {
            for (int i = 0; i < promoUsers.size(); i++) {
                forma.append(String.format(getMessage(Message.PROMO_USERS),
                        i + 1,
                        promoUsers.get(i).getName(),
                        promoUsers.get(i).getPhoneNumber())).append("\n");
            }
        }
        return forma.toString();
    }

    public SendMessage whenSendScreenshot(Update update) {
        String chatId = getChatId(update);
        TgUser tgUser = tgUserRepository.findByChatId(chatId);
        String fileId = update.getMessage().getPhoto().get(0).getFileId();
        CoursePayment coursePayment = CoursePayment.builder()
                .fileId(fileId)
                .tgUser(tgUser)
                .course(tgUser.getTempCourse())
                .build();
        CoursePayment saved = coursePaymentRepository.save(coursePayment);

        SendMessage sendMessage = new SendMessage(chatId,
                String.format(getMessage(Message.COURSE_PAYMENT_APP), saved.getId()));
        sendMessage.enableHtml(true);
        sendMessage.setReplyMarkup(forGetPromoCode());
        return sendMessage;
    }

    public SendMessage whenNotScreenshot(Update update) {
        String chatId = getChatId(update);
        return new SendMessage(chatId, getMessage(Message.RETRY));
    }

    public SendPhoto whenSendPaymentAppToChannel(Update update) {
        String fileId = update.getMessage().getPhoto().get(0).getFileId();
        CoursePayment payment = coursePaymentRepository.findByFileId(fileId);

        TgUser user = payment.getTgUser();
        TgUser owner = user.getUsingPromo().getOwner();
        Course tempCourse = payment.getTgUser().getTempCourse();

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(CHANNEL_ID);
        sendPhoto.setPhoto(new InputFile(fileId));
        sendPhoto.setReplyMarkup(forPaymentAppChannel(payment.getId()));
        sendPhoto.setParseMode("HTML");
        sendPhoto.setCaption(String.format(getMessage(Message.PAYMENT_APP),
                payment.getId(),
                user.getName(),
                user.getPhoneNumber(),
                user.getUsingPromo().getName(),
                owner.getName(),
                user.getUsingPromo().getName(),
                owner.getPhoneNumber(),
                owner.getPoints(),
                tempCourse.getName(),
                tempCourse.getPrice(),
                tempCourse.getDiscount(),
                tempCourse.getPrice() - tempCourse.getDiscount(),
                getMessage(Message.PENDING)
        ));
        return sendPhoto;
    }

    private InlineKeyboardMarkup forPaymentAppChannel(Long paymentId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();

        button1.setText(getMessage(Message.ACCEPT));
        button2.setText(getMessage(Message.REJECT));

        button1.setCallbackData("acceptPayment" + paymentId);
        button2.setCallbackData("rejectPayment" + paymentId);


        row1.add(button1);
        row1.add(button2);

        rowsInline.add(row1);
        rowsInline.add(row2);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }

    public SendPhoto whenAcceptOrRejectPaymentAppChannel(String data) {
        Long paymentId = Long.valueOf(data.substring(13));
        CoursePayment payment = coursePaymentRepository.findById(paymentId).orElseThrow(
                () -> RestException.restThrow("PAYMENT NOT FOUND", HttpStatus.BAD_REQUEST));

        TgUser user = payment.getTgUser();
        TgUser owner = user.getUsingPromo().getOwner();
        Course tempCourse = payment.getTgUser().getTempCourse();

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(CHANNEL_ID);
        sendPhoto.setPhoto(new InputFile(payment.getFileId()));
        sendPhoto.setParseMode("HTML");
        sendPhoto.setCaption(String.format(getMessage(Message.PAYMENT_APP),
                payment.getId(),
                user.getName(),
                user.getPhoneNumber(),
                user.getUsingPromo().getName(),
                owner.getName(),
                user.getUsingPromo().getName(),
                owner.getPhoneNumber(),
                owner.getPoints(),
                tempCourse.getName(),
                tempCourse.getPrice(),
                tempCourse.getDiscount(),
                tempCourse.getPrice() - tempCourse.getDiscount(),
                payment.getAccepted() ? getMessage(Message.ACCEPTED) : getMessage(Message.REJECTED)
        ));
        return sendPhoto;
    }
}

