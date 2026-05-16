package com.codencanvas.bloghash.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


import com.codencanvas.bloghash.domain.post.Post;
import com.codencanvas.bloghash.dto.response.post.PostDetailResponse;
import com.codencanvas.bloghash.dto.response.post.PostSummaryResponse;

@Mapper(
    componentModel = "spring",
    uses = { UserMapper.class, TagMapper.class, CategoryMapper.class }
)
public interface PostMapper {
 
    @Mapping(target = "likesCount", ignore = true)
    @Mapping(target = "commentsCount", ignore = true)
    PostSummaryResponse toSummaryResponse(Post post);

    @Mapping(target = "likesCount", ignore = true)
    @Mapping(target = "commentsCount", ignore = true)
    @Mapping(target = "likedByCurrentUser", ignore = true)
    PostDetailResponse toDetailResponse(Post post);
    
}
