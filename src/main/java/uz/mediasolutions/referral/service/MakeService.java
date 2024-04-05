package uz.mediasolutions.referral.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.mediasolutions.referral.entity.Language;
import uz.mediasolutions.referral.entity.LanguagePs;
import uz.mediasolutions.referral.entity.LanguageSourcePs;
import uz.mediasolutions.referral.entity.TgUser;
import uz.mediasolutions.referral.enums.LanguageName;
import uz.mediasolutions.referral.enums.StepName;
import uz.mediasolutions.referral.repository.LanguageRepository;
import uz.mediasolutions.referral.repository.LanguageRepositoryPs;
import uz.mediasolutions.referral.repository.StepRepository;
import uz.mediasolutions.referral.repository.TgUserRepository;
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
    private final LanguageRepository languageRepository;

    public static final String UZ = "UZ";
    public static final String RU = "RU";
    public static final String COURSE_CHANNEL_ID = "-1001903287909";

    public String getMessage(String key, String language) {
        List<LanguagePs> allByLanguage = languageRepositoryPs.findAll();
        if (!allByLanguage.isEmpty()) {
            for (LanguagePs languagePs : allByLanguage) {
                for (LanguageSourcePs languageSourceP : languagePs.getLanguageSourcePs()) {
                    if (languageSourceP.getTranslation() != null &&
                            languageSourceP.getLanguage().equals(language) &&
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

    public String getUserLanguage(String chatId) {
        if (tgUserRepository.existsByChatId(chatId)) {
            TgUser tgUser = tgUserRepository.findByChatId(chatId);
            if (tgUser.getLanguage() == null)
                return "UZ";
            else
                return tgUser.getLanguage().getName().name();
        } else
            return "UZ";
    }

    public SendMessage whenStart(Update update) {
        String chatId = getChatId(update);
        SendMessage sendMessage = new SendMessage(chatId, getMessage(Message.LANG_SAME_FOR_2_LANG,
                getUserLanguage(chatId)));
        sendMessage.setReplyMarkup(forStart());
        TgUser tgUser = TgUser.builder().chatId(chatId)
                .admin(false)
                .registered(false)
                .banned(false)
                .username(getUsername(update))
                .chatId(chatId)
                .points(0)
                .build();
        tgUserRepository.save(tgUser);
        setUserStep(chatId, StepName.CHOOSE_LANG);
        return sendMessage;
    }

    private ReplyKeyboardMarkup forStart() {
        String chatId = getChatId(new Update());

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rowList = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();

        KeyboardButton button1 = new KeyboardButton();
        KeyboardButton button2 = new KeyboardButton();

        button1.setText(getMessage(Message.UZBEK, getUserLanguage(chatId)));
        button2.setText(getMessage(Message.RUSSIAN, getUserLanguage(chatId)));

        row1.add(button1);
        row1.add(button2);

        rowList.add(row1);
        markup.setKeyboard(rowList);
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        return markup;
    }

    public SendMessage whenUz(Update update) {
        return getSendMessage(update, LanguageName.UZ);
    }

    public SendMessage whenRu(Update update) {
        return getSendMessage(update, LanguageName.RU);
    }

    public SendMessage getSendMessage(Update update, LanguageName languageName) {
        String chatId = getChatId(update);
        TgUser tgUser = tgUserRepository.findByChatId(chatId);
        Language language = languageRepository.findByName(languageName);
        tgUser.setLanguage(language);
        tgUserRepository.save(tgUser);
        SendMessage sendMessage = new SendMessage(getChatId(update),
                getMessage(Message.ENTER_NAME, getUserLanguage(chatId)));
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        setUserStep(chatId, StepName.ENTER_NAME);
        return sendMessage;
    }

//    public ReplyKeyboardMarkup forMainMenu(String chatId) {
//        TgUser tgUser = tgUserRepository.findByChatId(chatId);
//
//        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
//        List<KeyboardRow> rowList = new ArrayList<>();
//        KeyboardRow row1 = new KeyboardRow();
//        KeyboardRow row2 = new KeyboardRow();
//        KeyboardRow row3 = new KeyboardRow();
//
//        KeyboardButton button1 = new KeyboardButton();
//        KeyboardButton button2 = new KeyboardButton();
//        KeyboardButton button3 = new KeyboardButton();
//        KeyboardButton button4 = new KeyboardButton();
//
//        button1.setText(getMessage(Message.MENU_WEBSITE, getUserLanguage(chatId)));
//        button2.setText(getMessage(Message.MENU_SUG_COMP, getUserLanguage(chatId)));
//        button3.setText(getMessage(Message.MENU_MY_ORDERS, getUserLanguage(chatId)));
//        button4.setText(getMessage(Message.MENU_SETTINGS, getUserLanguage(chatId)));
//
//        if (tgUser.getLanguage().getName().equals(LanguageName.UZ)) {
//            button1.setWebApp(new WebAppInfo(LINK + chatId + "/" + UZ));
//            button3.setWebApp(new WebAppInfo(LINK + "orders/" + chatId + "/" + UZ));
//        } else {
//            button1.setWebApp(new WebAppInfo(LINK + chatId + "/" + RU));
//            button3.setWebApp(new WebAppInfo(LINK + "orders/" + chatId + "/" + RU));
//        }
//
//
//        row1.add(button1);
//        row2.add(button2);
//        row2.add(button3);
//        row3.add(button4);
//
//        rowList.add(row1);
//        rowList.add(row2);
//        rowList.add(row3);
//
//        markup.setKeyboard(rowList);
//        markup.setSelective(true);
//        markup.setResizeKeyboard(true);
//        return markup;
//    }

    public SendMessage whenStartForExistedUser(Update update) {
        String chatId = getChatId(update);
        TgUser user = tgUserRepository.findByChatId(chatId);

        if (user.getLanguage() == null) {
            return whenStart(update);
        } else if (user.getName() == null) {
            return getSendMessage(update, user.getLanguage().getName());
        } else if (user.getPhoneNumber() == null) {
        }
        return new SendMessage();
    }

    public SendMessage whenEnterPhoneNumber(Update update) {
        String chatId = getChatId(update);

        TgUser user = tgUserRepository.findByChatId(chatId);
        user.setName(update.getMessage().getText());
        tgUserRepository.save(user);

        SendMessage sendMessage = new SendMessage(chatId, getMessage(Message.ENTER_PHONE_NUMBER, getUserLanguage(chatId)));
        sendMessage.setReplyMarkup(forPhoneNumber(chatId));
        setUserStep(chatId, StepName.ENTER_PHONE_NUMBER);
        return sendMessage;
    }

    ReplyKeyboardMarkup forPhoneNumber(String chatId) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rowList = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();

        KeyboardButton button1 = new KeyboardButton();

        button1.setText(getMessage(Message.SHARE_PHONE_NUMBER, getUserLanguage(chatId)));
        button1.setRequestContact(true);

        row1.add(button1);

        rowList.add(row1);
        markup.setKeyboard(rowList);
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        return markup;
    }

    ReplyKeyboardMarkup forExecutePhoneNumber(String chatId) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rowList = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();

        KeyboardButton button1 = new KeyboardButton();

        button1.setText(getMessage(Message.APPROVE, getUserLanguage(chatId)));

        row1.add(button1);

        rowList.add(row1);
        markup.setKeyboard(rowList);
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        return markup;
    }

    public SendMessage whenMenu(Update update) {
        String chatId = getChatId(update);
        TgUser user = tgUserRepository.findByChatId(chatId);

        SendMessage sendMessage = new SendMessage(chatId,
                String.format(getMessage(Message.WELCOME_TO_MENU, getUserLanguage(chatId)),
                        user.getName()));
        sendMessage.enableHtml(true);
        setUserStep(chatId, StepName.CHOOSE_FROM_MENU);
        return sendMessage;
    }
}
