package com.deportal.shared.sanitization;

import org.springframework.stereotype.Component;

@Component
public class StringSanitizer {

    public String clean(String value) {
        if (value == null) {
            return null;
        }

        return value
                .trim()
                .replaceAll("\\s+", " ")
                .replace("<", "")
                .replace(">", "");
    }
}
