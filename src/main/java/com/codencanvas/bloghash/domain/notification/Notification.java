package com.codencanvas.bloghash.domain.notification;

import java.time.Instant;
import java.util.UUID;

import com.codencanvas.bloghash.domain.common.BaseEntity;
import com.codencanvas.bloghash.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notif_recipient_read_created", columnList = "recipient_id, is_read, created_at"),
        @Index(name = "idx_notif_group_key", columnList = "group_key")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Notification extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", foreignKey = @ForeignKey(name = "fk_notifications_actor", foreignKeyDefinition = "FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE SET NULL"))
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    private NotificationEntityType entityType;

    @Column(name = "entity_id", nullable = false, columnDefinition = "UUID")
    private UUID entityId;
    @Column(name = "group_key", length = 255)
    private String groupKey;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column(name = "read_at")
    private Instant readAt;

    // Business methods
    public void markAsRead() {
        this.read = true;
        this.readAt = Instant.now();
    }
}
