package com.codencanvas.bloghash.domain.common;

import java.time.Instant;

import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
@SQLRestriction("is_deleted = false")

public abstract class SoftDeletableEntity extends BaseEntity {
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;
    @Column(name = "deleted_at")
    private Instant deletedAt;

    public void softDelete() {
        this.deleted = true;
        this.deletedAt = Instant.now();
    }

    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return this.deleted;
    }
}
