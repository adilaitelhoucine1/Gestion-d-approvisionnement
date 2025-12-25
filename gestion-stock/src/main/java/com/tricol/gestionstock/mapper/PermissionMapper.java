package com.tricol.gestionstock.mapper;

import com.tricol.gestionstock.dto.admin.PermissionDTO;
import com.tricol.gestionstock.entity.security.Permission;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionDTO toDTO(Permission permission);

    List<PermissionDTO> toDTOList(List<Permission> permissions);

    Set<PermissionDTO> toDTOSet(Set<Permission> permissions);

    Permission toEntity(PermissionDTO dto);
}

