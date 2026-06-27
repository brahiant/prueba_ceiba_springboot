package com.deportal.shared.sanitization;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StringSanitizerTest {

    private final StringSanitizer sanitizer = new StringSanitizer();

    @Test
    void shouldTrimCollapseSpacesAndRemoveAngleBrackets() {
        String result = sanitizer.clean("  Cancha   script>alert<  ");

        assertThat(result).isEqualTo("Cancha scriptalert");
    }

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(sanitizer.clean(null)).isNull();
    }
}
