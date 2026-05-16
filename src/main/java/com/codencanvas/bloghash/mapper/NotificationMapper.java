package com.codencanvas.bloghash.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.codencanvas.bloghash.domain.notification.Notification;
import com.codencanvas.bloghash.dto.response.notification.NotificationResponse;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface NotificationMapper {

    @Mapping(target = "message", ignore = true)
    @Mapping(target = "type", expression = "java(notification.getType().name())")
    @Mapping(target = "entityType", expression = "java(notification.getEntityType().name())")
    NotificationResponse toResponse(Notification notification);
}