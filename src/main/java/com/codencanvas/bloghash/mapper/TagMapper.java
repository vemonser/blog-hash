package com.codencanvas.bloghash.mapper;

import org.mapstruct.Mapper;

import com.codencanvas.bloghash.domain.post.Tag;
import com.codencanvas.bloghash.dto.response.post.TagResponse;

@Mapper(componentModel = "spring")
public interface TagMapper {
    TagResponse toResponse(Tag tag);
}
