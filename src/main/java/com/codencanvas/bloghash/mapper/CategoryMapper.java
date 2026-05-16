package com.codencanvas.bloghash.mapper;



import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.codencanvas.bloghash.domain.post.Category;
import com.codencanvas.bloghash.dto.response.post.CategoryResponse;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

@Mapping(target = "parentId", expression = "java(category.getParent() == null ? null : category.getParent().getId())")
@Mapping(target = "parentName", expression = "java(category.getParent() == null ? null : category.getParent().getName())")
CategoryResponse toResponse(Category category);
}
