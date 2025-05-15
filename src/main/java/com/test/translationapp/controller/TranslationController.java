package com.test.translationapp.controller;


import com.test.translationapp.service.TranslationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TranslationController {

    private final TranslationService translationService;

    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @PostMapping(value = "/translate", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String translate(
            @RequestParam String text, @RequestParam String source , @RequestParam String target) {
        String response= translationService.translate(text, source, target);
        System.out.println(response);
        return response;
    }


}