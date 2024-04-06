package com.marindulja.mentalhealthbackend.services.disorders;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.DisorderDto;
import com.marindulja.mentalhealthbackend.dtos.MostCommonDisordersDto;
import com.marindulja.mentalhealthbackend.dtos.mapping.ModelMappingUtility;
import com.marindulja.mentalhealthbackend.models.AnxietyRecord;
import com.marindulja.mentalhealthbackend.models.Disorder;
import com.marindulja.mentalhealthbackend.models.UserProfile;
import com.marindulja.mentalhealthbackend.repositories.DisorderRepository;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DisorderServiceImpl implements DisorderService {

    private final ModelMappingUtility mapper;

    private final DisorderRepository disorderRepository;

    private final ProfileRepository userProfileRepository;

    @Autowired
    private EntityManager entityManager;


    @Override
    public List<DisorderDto> getAllDisorders() {
        return disorderRepository.findAll().stream().map(disorder -> mapper.map(disorder, DisorderDto.class)).collect(Collectors.toList());
    }

    @Override
    public void assignDisordersToUser(Long userId, List<Long> disorderIds) {
        final var patientProfile = Utilities.getPatientProfileIfBelongsToTherapist(userId, userProfileRepository);
        final var disorders = disorderRepository.findAllById(disorderIds);

        if (disorders.size() != disorderIds.size()) {
            // handle cases where some disorder IDs are invalid
            throw new IllegalArgumentException("Some disorder IDs are invalid");
        }

        patientProfile.getDisorders().addAll(disorders);
        userProfileRepository.save(patientProfile);
    }

    @Override
    public void updateDisordersToUser(Long patientId, Collection<Long> disorderIds) {
        final var patientProfile = Utilities.getPatientProfileIfBelongsToTherapist(patientId, userProfileRepository);
        final var newDisorders = disorderRepository.findAllById(new HashSet<>(disorderIds));

        if (newDisorders.size() != disorderIds.size()) {
            throw new IllegalArgumentException("Some disorders IDs are invalid");
        }

        final var currentDisordersList = patientProfile.getDisorders();

        // Filter out medications from current list that are not in the new list
        final var retainedDisorders = currentDisordersList.stream().filter(newDisorders::contains).collect(Collectors.toList());

        // Get medications from the new list that are not in the current list
        final var disordersToAdd = newDisorders.stream().filter(medication -> !retainedDisorders.contains(medication)).toList();

        // Combine the two lists
        retainedDisorders.addAll(disordersToAdd);

        // Set the modified medications back to the patient profile
        patientProfile.setDisorders(retainedDisorders);

        userProfileRepository.save(patientProfile);
    }

    @Override
    public void removeDisordersFromPatient(Long patientId, List<Long> disorderIds) {
        final var patientProfile = Utilities.getPatientProfileIfBelongsToTherapist(patientId, userProfileRepository);

        final var disorders = disorderRepository.findAllById(disorderIds);
        if (disorders.size() != disorderIds.size()) {
            // handle cases where some disorder IDs are invalid
            throw new IllegalArgumentException("Some disorder IDs are invalid");
        }
        patientProfile.getDisorders().removeAll(disorders);
        userProfileRepository.save(patientProfile);
    }

    @Override
    public List<MostCommonDisordersDto> findCommonDisordersAmongHighAnxietyPatients() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Subquery to find UserProfiles with high anxiety levels
        CriteriaQuery<Long> subQuery = cb.createQuery(Long.class);
        Root<AnxietyRecord> anxietyRecordRoot = subQuery.from(AnxietyRecord.class);
        subQuery.select(anxietyRecordRoot.get("user").get("id"))
                .groupBy(anxietyRecordRoot.get("user").get("id"))
                .having(cb.gt(cb.avg(anxietyRecordRoot.get("anxietyLevel")), 5.0));

        List<Long> highAnxietyUserIds = entityManager.createQuery(subQuery).getResultList();

        CriteriaQuery<Object[]> mainQuery = cb.createQuery(Object[].class);
        Root<Disorder> disorderRoot = mainQuery.from(Disorder.class);
        Join<Disorder, UserProfile> userProfilesJoin = disorderRoot.join("patientProfiles");

        // Assuming 'userProfiles' is the collection in Disorder entity pointing to UserProfile
        mainQuery.multiselect(disorderRoot.get("name"), cb.count(disorderRoot.get("id")))
                .where(userProfilesJoin.get("id").in(highAnxietyUserIds))
                .groupBy(disorderRoot.get("name"))
                .orderBy(cb.desc(cb.count(disorderRoot.get("id"))));

        List<Object[]> disorderCounts = entityManager.createQuery(mainQuery).getResultList();
        List<MostCommonDisordersDto> dtos = disorderCounts.stream().map(result -> {
            MostCommonDisordersDto dto = new MostCommonDisordersDto();
            dto.setDisorderName((String) result[0]);
            dto.setDisorderFrequency(((Long) result[1]));

            // Placeholder for average anxiety level calculation
            // You'll need to implement logic to calculate this based on the user IDs associated with each disorder
            double avgAnxietyLevel = calculateAverageAnxietyLevelForDisorder((String) result[0], highAnxietyUserIds);
            dto.setAvgAnxietyLevel(avgAnxietyLevel);

            return dto;
        }).collect(Collectors.toList());

        return dtos;
    }


    public double calculateAverageAnxietyLevelForDisorder(String disorderName, List<Long> highAnxietyUserIds) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Define the query to calculate the average anxiety level for a given disorder
        CriteriaQuery<Double> query = cb.createQuery(Double.class);
        Root<AnxietyRecord> anxietyRecordRoot = query.from(AnxietyRecord.class);
        Join<AnxietyRecord, UserProfile> userProfileJoin = anxietyRecordRoot.join("user");
        Join<UserProfile, Disorder> disorderJoin = userProfileJoin.join("disorders");

        // Specify the selection (average of anxiety levels)
        query.select(cb.avg(anxietyRecordRoot.get("anxietyLevel")))
                .where(cb.and(
                        disorderJoin.get("name").in(disorderName),
                        userProfileJoin.get("id").in(highAnxietyUserIds)
                ));

        Double result = entityManager.createQuery(query).getSingleResult();

        // Return the result or 0.0 if no average was calculated (e.g., no matching records)
        return result != null ? result : 0.0;
    }
}
