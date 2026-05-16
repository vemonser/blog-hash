package com.codencanvas.bloghash.util;

 
import com.codencanvas.bloghash.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
 
import java.util.UUID;
 
@Component
@RequiredArgsConstructor
public class SlugGenerator {
 
    private final PostRepository postRepository;
 
    public String generateUniqueSlug(String title) {
        String base      = toSlugBase(title);
        String shortId   = generateShortId();
        String candidate = base.isEmpty()
            ? "post-" + shortId
            : base + "-" + shortId;
 
        // نادراً جداً يحصل collision بس نتعامل معاه
        int attempts = 0;
        while (postRepository.existsBySlug(candidate) && attempts < 5) {
            candidate = base + "-" + generateShortId();
            attempts++;
        }
 
        return candidate;
    }
 
    private String toSlugBase(String title) {
        if (title == null || title.isBlank()) return "";
 
        return title
            .toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")  
            .replaceAll("\\s+", "-")           
            .replaceAll("-{2,}", "-")          
            .replaceAll("^-|-$", "")           
            .substring(0, Math.min(
                title.replaceAll("[^a-z0-9\\s-]", "").length(), 80
            ));                              
    }
 
    private String generateShortId() {
        return UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 8);
    }
}
 
