package com.codencanvas.bloghash.auth.service;

 
import com.codencanvas.bloghash.domain.auth.EmailVerificationToken;
import com.codencanvas.bloghash.domain.user.User;
import com.codencanvas.bloghash.exception.InvalidVerificationTokenException;
import com.codencanvas.bloghash.repository.EmailVerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {
 
    private final EmailVerificationTokenRepository tokenRepository;
    private final JavaMailSender                   mailSender;
 
    @Transactional
    public void sendVerificationEmail(User user) {
        // حذف أي token قديم للـ user ده
        tokenRepository.deleteByUserId(user.getId());
 
        // إنشاء token جديد
        String rawToken  = generateSecureToken();
        String tokenHash = hashToken(rawToken);
 
        EmailVerificationToken token = EmailVerificationToken.builder()
            .user(user)
            .tokenHash(tokenHash)
            .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
            .build();
 
        tokenRepository.save(token);
 
        String verificationLink =
            "http://localhost:4200/verify-email?token=" + rawToken;
 
        sendEmail(
            user.getEmail(),
            "Verify your CodeNCanvas account",
            buildEmailBody(user.getUsername(), verificationLink)
        );

        log.info("Verification email sent to: {}", user.getEmail());
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        String tokenHash = hashToken(rawToken);

        EmailVerificationToken token = tokenRepository
            .findByTokenHash(tokenHash)
            .orElseThrow(InvalidVerificationTokenException::new);

        if (!token.isValid()) {
            throw new InvalidVerificationTokenException();
        }

        token.markAsUsed();
        token.getUser().activate();

        tokenRepository.save(token);
        // User entity بيتـ save تلقائياً (في نفس الـ transaction)
        log.info("Email verified for user: {}", token.getUser().getEmail());
    }

    @Transactional
    public void resendVerificationEmail(User user) {
        sendVerificationEmail(user); 
    }

    // ── Private Helpers ──────────────────────────────────────────

    private String generateSecureToken() {
        byte[] bytes = new byte[32];       
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder()
                     .withoutPadding()   
                     .encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[]        hash   = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
 
    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
 
    private String buildEmailBody(String username, String link) {
        return """
            Hi %s,
            
            Welcome to CodeNCanvas! Please verify your email by clicking the link below:
            
            %s
            
            This link expires in 24 hours.
            
            If you didn't create an account, please ignore this email.
            
            — The CodeNCanvas Team
            """.formatted(username, link);
    }
}
 
