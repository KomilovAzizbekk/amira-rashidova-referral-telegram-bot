package uz.mediasolutions.referral.controller.abs;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.mediasolutions.referral.entity.LanguagePs;
import uz.mediasolutions.referral.manual.ApiResult;
import uz.mediasolutions.referral.payload.TranslateDto;
import uz.mediasolutions.referral.utills.constants.Rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping(LanguageController.LANGUAGE)
public interface LanguageController {

    String LANGUAGE = Rest.BASE_PATH + "lang/";
    String ALL = "all";
    String ALL_BY_LANG = "by-lang";
    String CREATE_EDIT = "create-edit";
    String CREATE_MAIN_KEY = "create-with-key";
    String CREATE_KEY = "create-key";

    @GetMapping(ALL)
    ApiResult<Page<LanguagePs>> getAllPageable(@RequestParam(defaultValue = Rest.DEFAULT_PAGE_NUMBER) int page,
                                               @RequestParam(defaultValue = Rest.DEFAULT_PAGE_SIZE) int size,
                                               @RequestParam(defaultValue = "null", required = false) String key);

    @GetMapping(ALL_BY_LANG)
    ResponseEntity<Map<String, String>> getAllByLang(@RequestParam(defaultValue = "UZ") String language);

    @PostMapping(CREATE_EDIT)
    ApiResult<?> createTranslation(@RequestBody TranslateDto dto);

    @PostMapping(CREATE_MAIN_KEY)
    ApiResult<?> createMainKey(@RequestBody List<TranslateDto> dtos);

    @PostMapping(CREATE_KEY)
    ApiResult<?> createKey(@RequestBody HashMap<String, String> dto);

}
