package com.tricol.gestionstock.mapper;

import com.tricol.gestionstock.dto.admin.RoleDTO;
import com.tricol.gestionstock.entity.security.RoleApp;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public interface RoleMapper {

    RoleDTO toDTO(RoleApp role);

    List<RoleDTO> toDTOList(List<RoleApp> roles);

    RoleApp toEntity(RoleDTO dto);
}

