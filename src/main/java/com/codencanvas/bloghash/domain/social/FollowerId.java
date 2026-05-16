package com.codencanvas.bloghash.domain.social;

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
public class FollowerId implements Serializable {
 
    @Column(name = "follower_id", columnDefinition = "UUID")
    private UUID followerId;  
 
    @Column(name = "following_id", columnDefinition = "UUID")
    private UUID followingId; 
}