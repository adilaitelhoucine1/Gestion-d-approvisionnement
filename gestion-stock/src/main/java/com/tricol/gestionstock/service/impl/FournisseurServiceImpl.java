package com.tricol.gestionstock.service.impl;

import com.tricol.gestionstock.dto.fournisseur.FournisseurRequestDTO;
import com.tricol.gestionstock.dto.fournisseur.FournisseurResponseDTO;
import com.tricol.gestionstock.entity.Fournisseur;
import com.tricol.gestionstock.exception.DuplicateResourceException;
import com.tricol.gestionstock.exception.ResourceNotFoundException;
import com.tricol.gestionstock.mapper.FournisseurMapper;
import com.tricol.gestionstock.repository.FournisseurRepository;
import com.tricol.gestionstock.service.FournisseurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FournisseurServiceImpl implements FournisseurService {

    private final FournisseurRepository fournisseurRepository;
    private final FournisseurMapper fournisseurMapper;

    @Override
    public FournisseurResponseDTO createFournisseur(FournisseurRequestDTO requestDTO) {

        if (fournisseurRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("email", requestDTO.getEmail());
        }

        if (fournisseurRepository.existsByIce(requestDTO.getIce())) {
            throw new DuplicateResourceException("ICE", requestDTO.getIce());
        }

        // Convertir le DTO en entite bach n saviih 
        Fournisseur fournisseur = fournisseurMapper.toEntity(requestDTO);
        Fournisseur savedFournisseur = fournisseurRepository.save(fournisseur);

        return fournisseurMapper.toResponseDTO(savedFournisseur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FournisseurResponseDTO> getAllFournisseurs() {
        log.debug("Recuperation de tous les fournisseurs");
        List<Fournisseur> fournisseurs = fournisseurRepository.findAll();
        return fournisseurMapper.toResponseDTOList(fournisseurs);
    }

    @Override
    @Transactional(readOnly = true)
    public FournisseurResponseDTO getFournisseurById(Long id) {
        log.debug("Recuperation du fournisseur avec l'ID: {}", id);
        Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur", "id", id));
        return fournisseurMapper.toResponseDTO(fournisseur);
    }

    @Override
    public FournisseurResponseDTO updateFournisseur(Long id, FournisseurRequestDTO requestDTO) {
        log.debug("Mise a jour du fournisseur avec l'ID: {}", id);

        // kan9alb wach four kayn
        Fournisseur existingFournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur", "id", id));

        // kan9lb lunicit dyal email wach deffrent
        if (!existingFournisseur.getEmail().equals(requestDTO.getEmail()) &&
                fournisseurRepository.existsByEmailAndIdNot(requestDTO.getEmail(), id)) {
            throw new DuplicateResourceException("email", requestDTO.getEmail());
        }

        // kan9lb lunicit dyal lICE wach deffrent f acctuel
        if (!existingFournisseur.getIce().equals(requestDTO.getIce()) &&
                fournisseurRepository.existsByIceAndIdNot(requestDTO.getIce(), id)) {
            throw new DuplicateResourceException("ICE", requestDTO.getIce());
        }

        // wahed update l entity
        fournisseurMapper.updateEntityFromDTO(requestDTO, existingFournisseur);
        Fournisseur updatedFournisseur = fournisseurRepository.save(existingFournisseur);

        log.info("Fournisseur mis a jour avec succes - ID: {}", id);
        return fournisseurMapper.toResponseDTO(updatedFournisseur);
    }

    @Override
    public void deleteFournisseur(Long id) {
        log.debug("Suppression du fournisseur avec l'ID: {}", id);

        // Verifier que le fournisseur existe
        if (!fournisseurRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fournisseur", "id", id);
        }

        fournisseurRepository.deleteById(id);
        log.info("Fournisseur supprime avec succes - ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FournisseurResponseDTO> searchByVille(String ville) {
        log.debug("Recherche de fournisseurs par ville: {}", ville);
        List<Fournisseur> fournisseurs = fournisseurRepository.findByVilleContainingIgnoreCase(ville);
        return fournisseurMapper.toResponseDTOList(fournisseurs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FournisseurResponseDTO> searchByRaisonSociale(String raisonSociale) {
        log.debug("Recherche de fournisseurs par raison sociale: {}", raisonSociale);
        List<Fournisseur> fournisseurs = fournisseurRepository.findByRaisonSocialeContainingIgnoreCase(raisonSociale);
        return fournisseurMapper.toResponseDTOList(fournisseurs);
    }



    @Override
    @Transactional(readOnly = true)
    public FournisseurResponseDTO getFournisseurByEmail(String email) {
        Fournisseur fournisseur = fournisseurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur", "email", email));
        return fournisseurMapper.toResponseDTO(fournisseur);
    }

    @Override
    @Transactional(readOnly = true)
    public FournisseurResponseDTO getFournisseurByIce(String ice) {

        Fournisseur fournisseur = fournisseurRepository.findByIce(ice)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur", "ICE", ice));
        return fournisseurMapper.toResponseDTO(fournisseur);
    }
}
