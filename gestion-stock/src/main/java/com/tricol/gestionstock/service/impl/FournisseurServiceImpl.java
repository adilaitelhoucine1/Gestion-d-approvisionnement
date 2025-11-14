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
import java.util.stream.Collectors;

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

        Fournisseur fournisseur = fournisseurMapper.toEntity(requestDTO);
        Fournisseur savedFournisseur = fournisseurRepository.save(fournisseur);

        return fournisseurMapper.toResponseDTO(savedFournisseur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FournisseurResponseDTO> getAllFournisseurs() {
         List<Fournisseur> fournisseurs = fournisseurRepository.findAll();
        return fournisseurMapper.toResponseDTOList(fournisseurs);
    }

    @Override
    @Transactional(readOnly = true)
    public FournisseurResponseDTO getFournisseurById(Long id) {
         Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur", "id", id));
        return fournisseurMapper.toResponseDTO(fournisseur);
    }

    @Override
    public FournisseurResponseDTO updateFournisseur(Long id, FournisseurRequestDTO requestDTO) {

        Fournisseur existingFournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur", "id", id));

        if (!existingFournisseur.getEmail().equals(requestDTO.getEmail()) &&
                fournisseurRepository.existsByEmailAndIdNot(requestDTO.getEmail(), id)) {
            throw new DuplicateResourceException("email", requestDTO.getEmail());
        }

        if (!existingFournisseur.getIce().equals(requestDTO.getIce()) &&
                fournisseurRepository.existsByIceAndIdNot(requestDTO.getIce(), id)) {
            throw new DuplicateResourceException("ICE", requestDTO.getIce());
        }

        fournisseurMapper.updateEntityFromDTO(requestDTO, existingFournisseur);
        Fournisseur updatedFournisseur = fournisseurRepository.save(existingFournisseur);

        return fournisseurMapper.toResponseDTO(updatedFournisseur);
    }

    @Override
    public void deleteFournisseur(Long id) {

    Fournisseur fournisseur = fournisseurRepository.findById(id).
            orElseThrow(()->
                    new DuplicateResourceException("Fournsisseur deja existe")
            );

        fournisseurRepository.delete(fournisseur);
    }

    @Override
    @Transactional(readOnly = true)
        public List<FournisseurResponseDTO> searchByVille(String ville) {
        List<Fournisseur> fournisseurs = fournisseurRepository.findByVilleContainingIgnoreCase(ville);
        return fournisseurMapper.toResponseDTOList(fournisseurs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FournisseurResponseDTO> searchByRaisonSociale(String raisonSociale) {
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

    @Override
    public List<FournisseurResponseDTO> filtered(){
      return   this.getAllFournisseurs().stream().
                filter(f->f.getEmail().endsWith("gmail.com"))
                .toList();

    }

}
