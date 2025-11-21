//package com.tricol.gestionstock.service;
//
//
//import com.tricol.gestionstock.dto.fournisseur.FournisseurRequestDTO;
//import com.tricol.gestionstock.dto.fournisseur.FournisseurResponseDTO;
//import com.tricol.gestionstock.entity.Fournisseur;
//import com.tricol.gestionstock.exception.ResourceNotFoundException;
//import com.tricol.gestionstock.mapper.FournisseurMapper;
//import com.tricol.gestionstock.repository.FournisseurRepository;
//import com.tricol.gestionstock.service.impl.FournisseurServiceImpl;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//public class FOurnisseurTest {
//
//    @Mock
//    private FournisseurRepository fournisseurRepository;
//
//    @Mock
//    private FournisseurMapper fournisseurMapper;
//
//   @InjectMocks
//   private FournisseurServiceImpl fournisseurService;
//
//
////    {
////        "id": 2,
////            "raisonSociale": "Mondial Tissu",
////            "adresse": "Boulevard Hassan II, Immeuble 12",
////            "ville": "Rabat",
////            "personneContact": "Fatima Zahra Bennani",
////            "email": "info@mondialtissu.ma",
////            "telephone": "+212537556677",
////            "ice": "002999888777666",
////            "createdAt": "2025-11-11T11:20:03",
////            "updatedAt": "2025-11-11T11:20:03"
////    },
//    @Test
//   public  void  test(){
//       Fournisseur fournisseur=Fournisseur.builder()
//               .id(2L)
//               .raisonSociale("Mondial Tissu")
//               .adresse("Boulevard Hassan")
//               .ville("Rabat")
//               .personneContact("Ismail test")
//               .email("info@mondialtissu.ma")
//               .telephone("+212537556677")
//               .ice("002999888777666")
//               .build();
//       when(fournisseurRepository.findById(2L)).thenReturn(Optional.of(fournisseur));
//        Optional<Fournisseur> result=fournisseurRepository.findById(2L);
//
//        assertTrue(result.isPresent());
//
//   }
//
//@Test
//   public void testNotFound(){
//       Fournisseur fournisseur=Fournisseur.builder()
//               .id(2L)
//               .raisonSociale("Mondial Tissu")
//               .adresse("Boulevard Hassan")
//               .ville("Rabat")
//               .personneContact("Ismail test")
//               .email("info@mondialtissu.ma")
//               .telephone("+212537556677")
//               .ice("002999888777666")
//               .build();
//
//       when(fournisseurRepository.findById(299L)).thenReturn(Optional.empty());
//       Optional<Fournisseur> result = fournisseurRepository.findById(299L);
//       assertFalse(result.isPresent());
//   }
//
//
//       @Test
//        public  void testupdate(){
//           Fournisseur fournisseur=Fournisseur.builder()
//                   .id(2L)
//                   .raisonSociale("Mondial Tissu")
//                   .adresse("Boulevard Hassan")
//                   .ville("Rabat")
//                   .personneContact("Ismail test")
//                   .email("info@mondialtissu.ma")
//                   .telephone("+212537556677")
//                   .ice("002999888777666")
//                   .build();
//           Fournisseur fournisseurupdated=Fournisseur.builder()
//
//                   .ville("Rabat")
//
//                   .build();
//
//
//           FournisseurRequestDTO fournisseurRequestDTO = fournisseurMapper.toRequestDTO(fournisseur);
//            FournisseurResponseDTO fournisseurResponseDTO=fournisseurMapper.toResponseDTO(fournisseurupdated);
//           when(fournisseurService.updateFournisseur(fournisseur.getId(), fournisseurRequestDTO)).thenReturn(fournisseurResponseDTO);
//
//           assertNotNull(fournisseurResponseDTO);
//           assertEquals(fournisseurResponseDTO.getAdresse(),fournisseurRequestDTO.getAdresse());
//           assertEquals("Rabat",fournisseurResponseDTO.getVille());
//
//
//    }
//
//
//}
