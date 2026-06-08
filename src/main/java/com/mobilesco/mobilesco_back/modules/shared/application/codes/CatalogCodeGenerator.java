package com.mobilesco.mobilesco_back.modules.shared.application.codes;

import java.text.Normalizer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;

public final class CatalogCodeGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private CatalogCodeGenerator() {
    }

    public static String generate(String name, Collection<String> existingCodes) {
        String base = normalize(name);
        Set<String> usedCodes = existingCodes.stream()
                .filter(Objects::nonNull)
                .map(code -> code.toUpperCase(Locale.ROOT))
                .collect(Collectors.toCollection(HashSet::new));

        String first = base.substring(0, 1);
        String firstTwo = complete(base, 2);
        String firstThree = complete(base, 3);

        String available = firstAvailable(usedCodes, first, firstTwo, firstThree);
        if (available != null) {
            return available;
        }

        for (int i = 0; i < CHARACTERS.length(); i++) {
            available = firstTwo + CHARACTERS.charAt(i);
            if (!usedCodes.contains(available)) {
                return available;
            }
        }

        for (int i = 0; i < CHARACTERS.length(); i++) {
            for (int j = 0; j < CHARACTERS.length(); j++) {
                available = first + CHARACTERS.charAt(i) + CHARACTERS.charAt(j);
                if (!usedCodes.contains(available)) {
                    return available;
                }
            }
        }

        for (int i = 0; i < CHARACTERS.length(); i++) {
            for (int j = 0; j < CHARACTERS.length(); j++) {
                for (int k = 0; k < CHARACTERS.length(); k++) {
                    available = "" + CHARACTERS.charAt(i) + CHARACTERS.charAt(j) + CHARACTERS.charAt(k);
                    if (!usedCodes.contains(available)) {
                        return available;
                    }
                }
            }
        }

        throw new BadRequestException("No hay codigos disponibles de hasta 3 caracteres");
    }

    private static String normalize(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("El nombre es obligatorio para generar el codigo");
        }

        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "");

        if (normalized.isBlank()) {
            throw new BadRequestException("El nombre debe contener letras o numeros para generar el codigo");
        }
        return normalized;
    }

    private static String complete(String base, int length) {
        StringBuilder result = new StringBuilder(base);
        while (result.length() < length) {
            result.append(base.charAt(result.length() % base.length()));
        }
        return result.substring(0, length);
    }

    private static String firstAvailable(Set<String> usedCodes, String... candidates) {
        for (String candidate : candidates) {
            if (!usedCodes.contains(candidate)) {
                return candidate;
            }
        }
        return null;
    }
}
