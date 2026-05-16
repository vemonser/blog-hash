package com.codencanvas.bloghash.domain.comment;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CommentReactionId implements Serializable {
 
    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;
 
    @Column(name = "comment_id", columnDefinition = "UUID")
    private UUID commentId;
}
