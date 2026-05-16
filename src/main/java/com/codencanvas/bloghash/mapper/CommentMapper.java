package com.codencanvas.bloghash.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.codencanvas.bloghash.domain.comment.Comment;
import com.codencanvas.bloghash.dto.response.comment.CommentResponse;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface CommentMapper {
 
    @Mapping(target = "parentId",           source = "parent.id")
    @Mapping(target = "likesCount",         ignore = true)  
    @Mapping(target = "repliesCount",       ignore = true)  
    @Mapping(target = "likedByCurrentUser", ignore = true)  
    @Mapping(target = "deleted",            source = "deleted")
    CommentResponse toResponse(Comment comment);
}
