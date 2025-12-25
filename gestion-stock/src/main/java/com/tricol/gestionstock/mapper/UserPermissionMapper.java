package com.tricol.gestionstock.mapper;

import com.tricol.gestionstock.dto.admin.UserPermissionDTO;
import com.tricol.gestionstock.entity.security.UserPermission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserPermissionMapper {

    @Mapping(source = "permission.id", target = "permissionId")
    @Mapping(source = "permission.name", target = "permissionName")
    @Mapping(source = "permission.category", target = "permissionCategory")
    UserPermissionDTO toDTO(UserPermission userPermission);

    List<UserPermissionDTO> toDTOList(List<UserPermission> userPermissions);
}

