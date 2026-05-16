package com.codencanvas.bloghash.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
 
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
@RequiredArgsConstructor
@Slf4j
public class TocGenerator {
 
    private final ObjectMapper objectMapper;
 
    private static final Pattern HEADING_PATTERN =
        Pattern.compile("^(#{1,6})\\s+(.+)$", Pattern.MULTILINE);
 
    public String generate(String markdown) {
        if (markdown == null || markdown.isBlank()) return "[]";
 
        try {
            List<TocItem> flat    = extractHeadings(markdown);
            List<TocItem> tree    = buildTree(flat);
            return objectMapper.writeValueAsString(tree);
        } catch (Exception e) {
            log.warn("Failed to generate ToC", e);
            return "[]";
        }
    }
 
    private List<TocItem> extractHeadings(String markdown) {
        List<TocItem>       items    = new ArrayList<>();
        Map<String, Integer> idCounts = new HashMap<>();
        Matcher             matcher  = HEADING_PATTERN.matcher(markdown);
 
        while (matcher.find()) {
            int    level = matcher.group(1).length(); 
            String text  = matcher.group(2).trim()
                .replaceAll("`[^`]*`", "")            
                .trim();
 
            String id = generateAnchorId(text, idCounts);
            items.add(new TocItem(id, text, level));
        }
 
        return items;
    }
 
    private List<TocItem> buildTree(List<TocItem> flat) {
        List<TocItem>       roots = new ArrayList<>();
        Deque<TocItem>      stack = new ArrayDeque<>(); // stack of ancestors
 
        for (TocItem item : flat) {
            while (!stack.isEmpty() && stack.peek().level() >= item.level()) {
                stack.pop();
            }
 
            if (stack.isEmpty()) {
                roots.add(item);             
            } else {
                stack.peek().children().add(item); 
            }
 
            stack.push(item);
        }
 
        return roots;
    }
 
    private String generateAnchorId(String text, Map<String, Integer> idCounts) {
        String id = text
            .toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-{2,}", "-")
            .replaceAll("^-|-$", "");
 
        if (id.isEmpty()) id = "heading";
 
        int count = idCounts.getOrDefault(id, 0);
        idCounts.put(id, count + 1);
 
        return count == 0 ? id : id + "-" + count; 
    }
 
    public static class TocItem {
        private final String     id;
        private final String     text;
        private final int        level;
        private final List<TocItem> children;
 
        public TocItem(String id, String text, int level) {
            this.id       = id;
            this.text     = text;
            this.level    = level;
            this.children = new ArrayList<>();
        }
 
        public String        id()       { return id; }
        public String        text()     { return text; }
        public int           level()    { return level; }
        public List<TocItem> children() { return children; }
    }
}
