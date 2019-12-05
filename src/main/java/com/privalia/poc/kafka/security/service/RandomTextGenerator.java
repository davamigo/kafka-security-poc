package com.privalia.poc.kafka.security.service;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Random;

/**
 * Service to generate a random text
 *
 * @author david.amigo
 */
@Component
public class RandomTextGenerator
{
    private static final String source[] = {
            "Lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing", "elit", "Sed", "dui", "lorem",
            "auctor", "sed", "cursus", "vel", "maximus", "a", "erat", "Sed", "malesuada", "neque", "aliquet",
            "auctor", "varius", "Mauris", "consequat", "ante", "id", "quam", "auctor", "nec", "ultricies", "massa",
            "tristique", "Maecenas", "libero", "metus", "euismod", "sit", "amet", "placerat", "ac", "tempus",
            "ac", "nisi", "Vestibulum", "ante", "ipsum", "primis", "in", "faucibus", "orci", "luctus", "et",
            "ultrices", "posuere", "cubilia", "Curae", "Suspendisse", "porttitor", "egestas", "vehicula",
            "Aliquam", "maximus", "purus", "a", "orci", "ullamcorper", "eget", "ultricies", "diam", "dignissim"
    };

    /**
     * @return a random text
     */
    public String getRandomText() {
        Random random = new Random();
        int maxWords = source.length;
        int firstWord = random.nextInt(maxWords / 2);
        int lastWord = firstWord + random.nextInt(maxWords - firstWord);
        String[] subarray = Arrays.stream(source, firstWord, lastWord).toArray(String[]::new);
        return String.join(" ", subarray);
    }
}
