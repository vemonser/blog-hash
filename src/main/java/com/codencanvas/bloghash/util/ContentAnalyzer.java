package com.codencanvas.bloghash.util;
import org.springframework.stereotype.Component;

@Component
public class ContentAnalyzer {
 
    private static final int  WORDS_PER_MINUTE        = 200;
    private static final int  CODE_BLOCK_EXTRA_SECONDS = 30;
 
    public int countWords(String markdown) {
        if (markdown == null || markdown.isBlank()) return 0;
 
        // شيل الـ markdown syntax قبل العد
        String text = markdown
            .replaceAll("```[\\s\\S]*?```", " ")     // code blocks
            .replaceAll("`[^`]+`", " ")               // inline code
            .replaceAll("!\\[.*?\\]\\(.*?\\)", " ")  // images
            .replaceAll("\\[.*?\\]\\(.*?\\)", " ")   // links
            .replaceAll("[#*_~>|]", " ")              // markdown symbols
            .replaceAll("\\s+", " ")
            .trim();
 
        if (text.isEmpty()) return 0;
        return text.split("\\s+").length;
    }
 
    public int calculateReadingTimeMins(String markdown) {
        int words      = countWords(markdown);
        int codeBlocks = countCodeBlocks(markdown);
 
        double minutes = (double) words / WORDS_PER_MINUTE
            + (codeBlocks * CODE_BLOCK_EXTRA_SECONDS / 60.0);
 
        return Math.max(1, (int) Math.ceil(minutes)); // minimum 1 minute
    }
 
    private int countCodeBlocks(String markdown) {
        if (markdown == null) return 0;
        int count = 0;
        int index = 0;
        while ((index = markdown.indexOf("```", index)) != -1) {
            count++;
            index += 3;
        }
        return count / 2; 
    }
}
