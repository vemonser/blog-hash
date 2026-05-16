package com.codencanvas.bloghash.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codencanvas.bloghash.domain.social.Follower;
import com.codencanvas.bloghash.domain.social.FollowerId;
 

@Repository
public interface FollowerRepository extends JpaRepository<Follower, FollowerId> {
 
    long countByIdFollowingId(UUID followingId);  // عدد followers اليوزر ده
    long countByIdFollowerId(UUID followerId);    // عدد اللي بيتابعهم
}
 
 