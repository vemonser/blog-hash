package com.codencanvas.bloghash.domain.post;

import com.codencanvas.bloghash.domain.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tags", uniqueConstraints = {
        @UniqueConstraint(name = "uk_tags_slug", columnNames = "slug")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Tag extends BaseEntity {
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    // URL-friendly: "Spring Boot" → "spring-boot"
    @Column(name = "slug", nullable = false, unique = true, length = 60)
    private String slug;
}
