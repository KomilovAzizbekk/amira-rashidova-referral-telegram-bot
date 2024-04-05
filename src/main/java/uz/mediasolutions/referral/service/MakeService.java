package uz.mediasolutions.referral.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
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

    public static final String UZ = "UZ";
    public static final String COURSE_CHANNEL_ID = "-1001903287909";

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

            //Adding user to promoUsers list
            user.setUsingPromo(promoCode);
            tgUserRepository.save(user);

            //Adding 1 point to the owner of the promoCode
//            TgUser owner = promoCode.getOwner();
//            Integer points = owner.getPoints();
//            owner.setPoints(points + 1);
//            tgUserRepository.save(owner);
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

        FileGif fileGif = new FileGif();
        if (fileGifRepository.existsById(1L)) {
            fileGif = fileGifRepository.findById(1L).orElseThrow(
                    () -> RestException.restThrow("GIF NOT FOUND", HttpStatus.BAD_REQUEST));
            sendDocument.setDocument(new InputFile(fileGif.getFileId()));
        } else {
            sendDocument.setDocument(new InputFile("mm.mp4"));
        }

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
        button3.setCallbackData("back");


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
}

