package com.marindulja.mentalhealthbackend.services.medications;

import com.marindulja.mentalhealthbackend.dtos.MedicationDto;

import java.util.List;

public interface MedicationService {
     List<MedicationDto> getAllMedications();

     void assignMedicationsToUser(Long patientId, List<Long> medicationIds);

     void updateMedicationsToUser(Long patientId, List<Long> medicationIds);

     void removeMedicationsFromPatient(Long patientId, List<Long> medicationsIds);
}
