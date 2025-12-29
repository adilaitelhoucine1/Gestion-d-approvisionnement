package com.tricol.gestionstock.mapper;

import com.tricol.gestionstock.dto.audit.AuthAuditLogDTO;
import com.tricol.gestionstock.entity.AuthAuditLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthAuditMapper {

    AuthAuditLogDTO toDTO(AuthAuditLog entity);
}

