package com.epam.esm.gym.service.profile;

import com.epam.esm.gym.dao.TraineeDao;
import com.epam.esm.gym.domain.Trainee;
import com.epam.esm.gym.domain.Trainer;
import com.epam.esm.gym.domain.Training;
import com.epam.esm.gym.domain.User;
import com.epam.esm.gym.dto.auth.BaseResponse;
import com.epam.esm.gym.dto.auth.MessageResponse;
import com.epam.esm.gym.dto.profile.ProfileRequest;
import com.epam.esm.gym.dto.profile.ProfileResponse;
import com.epam.esm.gym.dto.profile.UserProfile;
import com.epam.esm.gym.dto.trainee.TraineeRequest;
import com.epam.esm.gym.dto.trainer.SlimTrainerProfile;
import com.epam.esm.gym.dto.training.TrainingProfile;
import com.epam.esm.gym.dto.training.TrainingResponse;
import com.epam.esm.gym.exception.UserNotFoundException;
import com.epam.esm.gym.mapper.TraineeMapper;
import com.epam.esm.gym.service.TraineeService;
import com.epam.esm.gym.service.TrainerService;
import com.epam.esm.gym.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link TraineeService} for managing trainee profiles and related operations.
 * <p>
 * This class handles the core functionalities associated with trainee management, profile updates,
 * and retrieval of trainees' training sessions. It interacts with the underlying database and provides
 * business logic for operations like assigning trainers, validating user credentials, and managing activation status.
 * Additionally, it manages password changes and deletion of trainee profiles.
 * </p>
 */
@Service
@AllArgsConstructor
public class TraineeProfileService implements TraineeService {

    private final TraineeMapper mapper;
    private final TraineeDao traineeDao;
    private final UserService userService;
    private final TrainerService trainerService;

    /**
     * {@inheritDoc}
     * Finds and retrieves all trainee profiles in the system.
     */
    @Override
    public ResponseEntity<List<BaseResponse>> findAll() {
        return ResponseEntity.ok(traineeDao.findAll()
                .stream()
                .map(mapper::toTraineeProfile)
                .toList());
    }

    /**
     * {@inheritDoc}
     * Registers a new trainee based on the provided request and returns the profile response.
     */
    @Override
    public ResponseEntity<ProfileResponse> register(TraineeRequest dto) {
        String rawPassword = userService.generateRandomPassword();
        String password = userService.encodePassword(rawPassword);
        User user = userService.createTraineeUser(dto, password);
        User savedUser = userService.saveUser(user);
        Trainee trainee = mapper.toTrainee(savedUser, dto);
        Trainee savedTrainee = traineeDao.save(trainee);
        ProfileResponse response = mapper.toProfile(savedTrainee.getUser().getUsername(), rawPassword);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * {@inheritDoc}
     * Deletes a trainee profile based on the provided username.
     */
    @Override
    public ResponseEntity<Void> deleteTrainee(String username) {
        Optional<Trainee> traineeOptional = traineeDao.findByUsername(username);
        if (traineeOptional.isPresent()) {
            traineeDao.delete(traineeOptional.get());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * {@inheritDoc}
     * Retrieves a trainee profile by the given username, returning a detailed profile if found.
     */
    @Override
    public ResponseEntity<BaseResponse> getTraineeProfileByName(String username) {
        return traineeDao.findByUsername(username)
                .map(trainee -> ResponseEntity.ok(mapper.toTraineeProfile(trainee)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse(String.format("User by name: %s not found", username))));
    }

    /**
     * {@inheritDoc}
     * Updates an existing trainee profile with the provided request data.
     */
    @Override
    public ResponseEntity<BaseResponse> updateTrainee(
            String username, TraineeRequest request) {
        return traineeDao.findByUsername(username)
                .map(trainee -> ResponseEntity.ok(mapper.toTraineeProfile(
                        traineeDao.update(mapper.update(request, trainee)))))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * {@inheritDoc}
     * Validates user credentials based on the provided profile request.
     */
    @Override
    public boolean validateUser(ProfileRequest request) {
        UserProfile user = userService.getUserByUsername(request.getUsername());
        return request.getOldPassword().equals(user.getPassword()) || user.getActive();
    }

    /**
     * {@inheritDoc}
     * Changes the password for the trainee based on the provided profile request.
     */
    @Override
    public void changePassword(ProfileRequest request) {
        userService.changePassword(request);
    }

    /**
     * {@inheritDoc}
     * Updates the list of trainers assigned to a trainee identified by their username.
     */
    @Override
    public ResponseEntity<List<SlimTrainerProfile>> updateTraineeTrainersByName(
            String username, List<String> trainersUsernames) {
        List<SlimTrainerProfile> trainerProfiles = trainersUsernames.stream()
                .map(trainerService::getTrainer)
                .map(mapper::toTrainerProfile)
                .collect(Collectors.toList());
        Trainee trainee = getTrainee(username);
        trainee.setTrainers(mapper.toTrainers(trainerProfiles));
        traineeDao.save(trainee);
        return ResponseEntity.ok(trainerProfiles);
    }

    /**
     * {@inheritDoc}
     * Retrieves the training sessions of a trainee based on the provided username and filtering parameters.
     */
    @Override
    public ResponseEntity<List<TrainingResponse>> getTraineeTrainingsByName(
            String username, Map<String, String> params) {
        return traineeDao.findByUsername(username)
                .map(trainee -> ResponseEntity.ok(trainee.getTrainings()
                        .stream()
                        .filter(training -> matches(training, getProfile(params)))
                        .map(mapper::toResponse).toList()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * {@inheritDoc}
     * Activates or deactivates a trainee's profile based on the provided status.
     */
    @Override
    public ResponseEntity<Void> activateDeactivateProfile(String username, Boolean active) {
        Trainee trainee = getTrainee(username);
        trainee.getUser().setActive(active);
        traineeDao.save(trainee);
        return ResponseEntity.ok().build();
    }

    /**
     * {@inheritDoc}
     * Retrieves trainers that are not assigned to a specific trainee identified by username.
     */
    @Override
    public ResponseEntity<List<SlimTrainerProfile>> getNotAssignedTrainers(String username) {
        List<Trainer> trainers = traineeDao.findNotAssignedTrainers(username);
        return ResponseEntity.ok(trainers.stream()
                .map(mapper::toTrainerProfile)
                .toList());
    }

    /**
     * {@inheritDoc}
     * Retrieves a trainee by their username, throwing an exception if not found.
     *
     * @throws UserNotFoundException If no trainee with the given username is found.
     */
    @Override
    public Trainee getTrainee(String username) {
        return traineeDao.findByUsername(username).orElseThrow(
                () -> new UserNotFoundException("Trainee not found: " + username));
    }

    /**
     * Constructs a {@link TrainingProfile} object from the provided parameters.
     * <p>
     * This method builds a {@link TrainingProfile} instance using parameters from
     * the provided map. It parses date strings for the training period, sets the
     * trainer name, and specifies the training type, if available in the map. If
     * certain parameters are not present, corresponding fields in the profile
     * will be set to null.
     * </p>
     *
     * @param params a map containing keys and values for period dates, trainer
     *               name, and training type.
     * @return a {@link TrainingProfile} built from the provided parameters.
     */
    private TrainingProfile getProfile(Map<String, String> params) {
        return TrainingProfile.builder()
                .periodFrom(params.containsKey("periodFrom")
                        ? LocalDate.parse(params.get("periodFrom")) : null)
                .periodTo(params.containsKey("periodTo")
                        ? LocalDate.parse(params.get("periodTo")) : null)
                .trainerName(params.get("trainerName"))
                .trainingType(params.get("trainingType"))
                .build();
    }

    /**
     * Checks if a given training matches the specified filter criteria.
     * <p>
     * This method evaluates whether a {@link Training} object meets the conditions
     * defined in a {@link TrainingProfile} filter. It compares the training dates,
     * trainer name, and training type against the filter values, returning true if
     * all criteria match. If any condition does not align, the training is
     * considered a non-match.
     * </p>
     *
     * @param training the training session to be evaluated against the filter.
     * @param filter   the {@link TrainingProfile} containing filter criteria.
     * @return true if the training matches all filter conditions; otherwise, false.
     */
    private boolean matches(Training training, TrainingProfile filter) {
        if (filter.getPeriodFrom() != null && training.getTrainingDate().isBefore(filter.getPeriodFrom())) {
            return false;
        }
        if (filter.getPeriodTo() != null && training.getTrainingDate().isAfter(filter.getPeriodTo())) {
            return false;
        }
        if (filter.getTrainerName() != null && !training.getTrainer().getUser().getUsername()
                .equalsIgnoreCase(filter.getTrainerName())) {
            return false;
        }
        return filter.getTrainingType() == null || training.getType().getSpecialization().name()
                .equalsIgnoreCase(filter.getTrainingType());
    }
}
