package com.codencanvas.bloghash.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.codencanvas.bloghash.domain.user.User;
import com.codencanvas.bloghash.dto.response.auth.UserProfileResponse;
import com.codencanvas.bloghash.dto.response.auth.UserSummaryResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {
 
    UserSummaryResponse toSummaryResponse(User user);

    @Mapping(target = "postsCount",     ignore = true) 
    @Mapping(target = "followersCount", ignore = true) 
    @Mapping(target = "followingCount", ignore = true) 
    @Mapping(target = "role",           expression = "java(user.getRole().name())")

    UserProfileResponse toProfileResponse(User user);
}
