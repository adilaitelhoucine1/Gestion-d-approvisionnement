package com.tricol.gestionstock.mapper;

import com.tricol.gestionstock.dto.admin.UserResponseDTO;
import com.tricol.gestionstock.entity.security.UserApp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RoleMapper.class, UserPermissionMapper.class})
public interface UserMapper {

    @Mapping(source = "userPermissions", target = "customPermissions")
    UserResponseDTO toDTO(UserApp user);

    List<UserResponseDTO> toDTOList(List<UserApp> users);
}

