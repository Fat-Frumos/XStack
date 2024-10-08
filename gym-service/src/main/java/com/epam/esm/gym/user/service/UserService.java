package com.epam.esm.gym.user.service;

import com.epam.esm.gym.user.entity.User;
import com.epam.esm.gym.jms.dto.MessageResponse;
import com.epam.esm.gym.user.dto.profile.ProfileRequest;
import com.epam.esm.gym.user.dto.profile.UserResponse;
import com.epam.esm.gym.user.dto.trainee.PostTraineeRequest;
import com.epam.esm.gym.user.dto.trainer.TrainerRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Service interface for managing user accounts and operations related to users.
 * <p>
 * This interface provides methods for performing various user-related operations, including saving and updating user
 * profiles, managing user activation and deactivation, and handling authentication. It covers functionalities for both
 * trainers and trainees, allowing for comprehensive user management within the system.
 * Methods also handle password changes and user retrieval based on usernames.
 * </p>
 *
 * @author Pavlo Poliak
 * @version 1.0.0
 * @since 1.0
 */
public interface UserService {

    /**
     * Saves a new trainer profile based on the provided request.
     * <p>
     * This method processes a {@link TrainerRequest} to create and persist a new trainer profile in the system.
     * The method involves validating the request data and saving the trainer information to the database. T
     * he saved trainer profile is then returned as a {@link com.epam.esm.gym.user.dto.trainer.TrainerProfile} object,
     * which includes all relevant details about the trainer.
     * </p>
     *
     * @param dto the {@link TrainerRequest} containing the details of the trainer to be saved
     * @return the {@link com.epam.esm.gym.user.dto.trainer.TrainerProfile} representing the saved trainer
     */
    User createTrainerUser(TrainerRequest dto, String password);

    /**
     * Updates an existing user profile with new information.
     * <p>
     * This method updates the details of an existing user based on the provided {@link User} object.
     * The method involves validating and saving the updated user data to the database.
     * Any changes made to the user profile will be persisted,
     * and the updated user information will reflect in the system.
     * </p>
     *
     * @param user the {@link User} object containing updated user details
     */
    void updateUser(User user);

    /**
     * Deletes a user profile from the system based on the username.
     * <p>
     * This method removes the user identified by the given username from the system. The method performs the necessary
     * operations to delete the user account and associated data, ensuring that all references to the user are removed
     * from the database.
     * </p>
     *
     * @param username the username of the user to be deleted
     */
    ResponseEntity<Void> deleteUser(String username);

    /**
     * Retrieves a user profile based on the username.
     * This method fetches the user profile associated with the username.
     * The returned {@link com.epam.esm.gym.user.dto.profile.UserResponse} object contains detailed information
     * about the user, including their personal details and account status. The method is useful for displaying
     * user information or performing user-related operations based on the retrieved profile.
     *
     * @param username the username of the user whose profile is to be retrieved
     * @return the {@link com.epam.esm.gym.user.dto.profile.UserResponse} associated with the given username
     */
    UserResponse getUserByUsername(String username);

    /**
     * Changes the password for a user based on the provided request.
     * <p>
     * This method processes a {@link ProfileRequest} to change the user's password.
     * It involves validating the request data and updating the user's password in the system.
     * The method returns a {@link ResponseEntity} containing a message response indicating
     * the success or failure of the password change operation.
     * </p>
     *
     * @param user the {@link ProfileRequest} containing the current and new passwords
     * @return a {@link ResponseEntity} with a {@link MessageResponse} indicating the result of the password change
     */
    MessageResponse changePassword(ProfileRequest user);

    /**
     * Authenticates a user based on the provided username and password.
     * <p>
     * This method verifies the user's credentials and returns an authentication response.
     * The method involves checking the provided username and password against the stored credentials and returning
     * a {@link ResponseEntity} with a message response indicating the success or failure of the authentication attempt.
     * </p>
     *
     * @param username the username of the user to be authenticated
     * @param password the password of the user
     * @return a {@link ResponseEntity} with a {@link MessageResponse}
     * indicating the result of the authentication attempt
     */
    MessageResponse authenticate(String username, String password);

    /**
     * Saves a new trainee user based on the provided request.
     * <p>
     * This method processes a {@link com.epam.esm.gym.user.dto.trainee.PutTraineeRequest} to create and persist
     * a new trainee user profile in the system.
     * The method involves validating the request data and saving the trainee information to the database.
     * The saved trainee user is then returned as a {@link User} object.
     * </p>
     *
     * @param request the {@link com.epam.esm.gym.user.dto.trainee.PutTraineeRequest}
     *                containing the details of the trainee user to be saved
     * @return the {@link User} representing the saved trainee
     */
    User createTraineeUser(PostTraineeRequest request, String password);

    /**
     * Retrieves a user profile based on the username.
     * <p>
     * This method fetches the user profile associated with the specified username. The returned {@link User} object
     * contains detailed information about the user, including their personal details and account status. This method
     * is useful for displaying user information or performing user-related operations based on the retrieved profile.
     * </p>
     *
     * @param username the username of the user whose profile is to be retrieved
     * @return the {@link User} associated with the given username
     */
    User getUser(String username);

    /**
     * Generates a random password.
     *
     * <p>This method creates a random password using {@link java.security.SecureRandom}
     * and {@link java.util.Base64} encoding. The length of the password is determined
     * by the {@code PASSWORD_LENGTH} constant. </p>
     *
     * @return a randomly generated password.
     */
    String generateRandomPassword();

    /**
     * Encodes a plain-text password.
     *
     * <p>This method uses {@link org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder}
     * to hash the provided plain-text password securely. The encoded password
     * can be stored in the database for user authentication purposes. </p>
     *
     * @param password the plain-text password to be encoded.
     * @return the encoded password.
     */
    String encodePassword(String password);

    /**
     * Saves a {@link User} entity.
     *
     * <p>This method persists the provided {@link User} entity to the database using
     * the {@link com.epam.esm.gym.user.dao.JpaUserDao}. The user entity must be valid before saving. </p>
     *
     * @param user the {@link User} entity to be saved.
     * @return the saved {@link User} entity.
     */
    User saveUser(User user);

    /**
     * Retrieves a list of all users.
     * This method returns a list of `UserProfile` objects representing all users.
     *
     * @return a {@link List} containing a list of
     * {@link com.epam.esm.gym.user.dto.profile.UserResponse} for all trainers
     */
    List<UserResponse> findAll();

    /**
     * Retrieves a list of trainees by their usernames.
     *
     * @param usernames a {@link List} of usernames to search for.
     * @return a {@link List} of {@link UserResponse} containing the details of all matching trainees.
     */
    List<UserResponse> findAllTraineeByName(List<String> usernames);
}
