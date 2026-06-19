package com.mobilesco.mobilesco_back.modules.color.application.usecases;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;

final class ColorCodeGenerator {

    private static final int MAX_LENGTH = 10;
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private ColorCodeGenerator() {
    }

    static String generate(String colorName, Collection<String> existingCodes) {
        List<String> words = normalizeWords(colorName);
        Set<String> usedCodes = normalizeExistingCodes(existingCodes);

        List<String> candidates = buildNameCandidates(words);
        for (String candidate : candidates) {
            if (!usedCodes.contains(candidate)) {
                return candidate;
            }
        }

        String seed = candidates.get(candidates.size() - 1);
        String fallback = firstAlphabeticFallback(seed, usedCodes);
        if (fallback != null) {
            return fallback;
        }

        throw new BadRequestException("No hay codigos alfabeticos disponibles para el color");
    }

    private static List<String> normalizeWords(String colorName) {
        if (colorName == null || colorName.isBlank()) {
            throw new BadRequestException("El nombre es obligatorio para generar el codigo del color");
        }

        String normalized = Normalizer.normalize(colorName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z]+", " ")
                .trim();

        if (normalized.isBlank()) {
            throw new BadRequestException("El nombre del color debe contener letras para generar el codigo");
        }

        return List.of(normalized.split("\\s+"));
    }

    private static Set<String> normalizeExistingCodes(Collection<String> existingCodes) {
        if (existingCodes == null) {
            return Set.of();
        }

        return existingCodes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(code -> !code.isBlank())
                .map(code -> code.toUpperCase(Locale.ROOT))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static List<String> buildNameCandidates(List<String> words) {
        Set<String> candidates = new LinkedHashSet<>();
        String base = complete(words.get(0), 2);

        if (words.size() == 1) {
            addExpandedCandidate(candidates, base, remainingLetters(words.get(0), 2));
            return new ArrayList<>(candidates);
        }

        StringBuilder initials = new StringBuilder(base);
        StringBuilder extraLetters = new StringBuilder();

        for (int i = 1; i < words.size(); i++) {
            String word = words.get(i);
            initials.append(word.charAt(0));
            extraLetters.append(remainingLetters(word, 1));
        }

        addExpandedCandidate(candidates, initials.toString(), extraLetters.toString());
        return new ArrayList<>(candidates);
    }

    private static void addExpandedCandidate(Set<String> candidates, String initial, String extraLetters) {
        String current = truncate(initial);
        candidates.add(current);

        for (int i = 0; i < extraLetters.length() && current.length() < MAX_LENGTH; i++) {
            current = current + extraLetters.charAt(i);
            candidates.add(current);
        }
    }

    private static String remainingLetters(String word, int usedLetters) {
        if (word.length() <= usedLetters) {
            return "";
        }
        return word.substring(usedLetters);
    }

    private static String complete(String word, int length) {
        StringBuilder result = new StringBuilder(word);
        while (result.length() < length) {
            result.append(word.charAt(result.length() % word.length()));
        }
        return result.substring(0, length);
    }

    private static String truncate(String value) {
        return value.length() <= MAX_LENGTH ? value : value.substring(0, MAX_LENGTH);
    }

    private static String firstAlphabeticFallback(String seed, Set<String> usedCodes) {
        for (int suffixLength = 1; suffixLength < MAX_LENGTH; suffixLength++) {
            int prefixLength = Math.min(seed.length(), MAX_LENGTH - suffixLength);
            if (prefixLength <= 0) {
                continue;
            }

            String prefix = seed.substring(0, prefixLength);
            String available = firstWithSuffix(prefix, suffixLength, "", usedCodes);
            if (available != null) {
                return available;
            }
        }

        return null;
    }

    private static String firstWithSuffix(String prefix, int remaining, String suffix, Set<String> usedCodes) {
        if (remaining == 0) {
            String candidate = prefix + suffix;
            return usedCodes.contains(candidate) ? null : candidate;
        }

        for (int i = 0; i < ALPHABET.length(); i++) {
            String available = firstWithSuffix(prefix, remaining - 1, suffix + ALPHABET.charAt(i), usedCodes);
            if (available != null) {
                return available;
            }
        }

        return null;
    }
}
