package uk.co.devinity.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import uk.co.devinity.controllers.mvc.AdminController;
import uk.co.devinity.entities.User;
import uk.co.devinity.entities.WorkoutPlan;
import uk.co.devinity.entities.WorkoutType;
import uk.co.devinity.repositories.UserRepository;
import uk.co.devinity.services.WorkoutService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AdminController underTest;
    private WorkoutService workoutService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        workoutService = mock(WorkoutService.class);
        passwordEncoder = new BCryptPasswordEncoder();
        underTest = new AdminController(userRepository, passwordEncoder, workoutService);
    }

    @Test
    void whenCreateUser_thenPasswordIsEncodedAndDefaultRoleAdded() {
        User u = new User();
        u.setEmail("bob@example.com");
        u.setPassword("plaintext");
        u.setRoles(null);

        underTest.createUser(u);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("bob@example.com");
        assertThat(saved.getPassword()).isNotEqualTo("plaintext");
        assertThat(saved.getRoles()).contains("ROLE_USER");
    }

    @Test
    void whenNewUserForm_thenModelPrepared() {
        String view = underTest.newUserForm(mock(Model.class));
        assertThat(view).isEqualTo("admin/new-user");
    }

    @Test
    void whenListUsers_thenUsersAddedToModel() {
        String view = underTest.listUsers(mock(Model.class));
        assertThat(view).isEqualTo("admin/users");
    }

    @Test
    void whenActivateUser_thenUserIsActivated() {
        Long userId = 1L;
        User user = new User();
        user.setActive(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        String view = underTest.activateUser(userId);

        assertThat(user.isActive()).isTrue();
        verify(userRepository, times(1)).save(user);
        assertThat(view).isEqualTo("redirect:/admin/users");
    }

    @Test
    void whenDeactivateUser_thenUserIsDeactivated() {
        Long userId = 1L;
        User user = new User();
        user.setActive(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        String view = underTest.deactivateUser(userId);

        assertThat(user.isActive()).isFalse();
        verify(userRepository, times(1)).save(user);
        assertThat(view).isEqualTo("redirect:/admin/users");
    }

    @Test
    void whenResetPasswordForm_thenUserIsLoadedAndPasswordClearedAndAddedToModel() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setPassword("oldPassword");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Model model = new ConcurrentModel();
        String view = underTest.resetPasswordForm(userId, model);

        assertThat(view).isEqualTo("admin/reset-password");
        assertThat(((User) model.getAttribute("user")).getId()).isEqualTo(userId);
        assertThat(((User) model.getAttribute("user")).getPassword()).isEmpty();
    }

    @Test
    void whenResetPasswordForm_withInvalidUserId_thenThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.resetPasswordForm(99L, new ConcurrentModel()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid user Id:99");
    }

    @Test
    void whenResetPassword_thenPasswordIsEncodedAndUserSaved() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setPassword("oldPassword");
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        User inputUser = new User();
        inputUser.setId(userId);
        inputUser.setPassword("newPassword");

        String view = underTest.resetPassword(inputUser);

        assertThat(view).isEqualTo("redirect:/admin/users");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();
        assertThat(savedUser.getId()).isEqualTo(userId);
        assertThat(savedUser.getPassword()).isNotEqualTo("newPassword");
        assertThat(passwordEncoder.matches("newPassword", savedUser.getPassword())).isTrue();
    }

    @Test
    void whenResetPassword_withInvalidUserId_thenDoesNothing() {
        User inputUser = new User();
        inputUser.setId(99L);
        inputUser.setPassword("newPassword");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        String view = underTest.resetPassword(inputUser);

        assertThat(view).isEqualTo("redirect:/admin/users");
        verify(userRepository, never()).save(any());
    }

    @Test
    void whenLoadWorkoutTypes_thenWorkoutTypesAddedToModel() {
        WorkoutType wt = new WorkoutType();
        when(workoutService.getAllWorkoutTypes()).thenReturn(List.of(wt));

        Model model = new ConcurrentModel();
        String view = underTest.loadWorkoutTypes(model);

        assertThat(view).isEqualTo("admin/workout-types");
        assertThat(model.getAttribute("workoutTypes")).isEqualTo(List.of(wt));
    }

    @Test
    void whenLoadNewWorkoutType_thenModelPrepared() {
        Model model = new ConcurrentModel();

        String view = underTest.loadNewWorkoutType(model);

        assertThat(view).isEqualTo("admin/new-workout-type");
        assertThat(model.getAttribute("workoutType")).isInstanceOf(WorkoutType.class);
    }

    @Test
    void whenCreateNewWorkoutType_thenDelegatesToService() {
        WorkoutType workoutType = new WorkoutType();

        String view = underTest.createNewWorkoutType(workoutType);

        verify(workoutService, times(1)).saveWorkoutType(workoutType);
        assertThat(view).isEqualTo("redirect:/admin/workouts/workout-types");
    }

    @Test
    void whenLoadWorkouts_thenWorkoutsAddedToModel() {
        WorkoutPlan plan = new WorkoutPlan();
        when(workoutService.getAllWorkouts()).thenReturn(List.of(plan));

        Model model = new ConcurrentModel();
        String view = underTest.loadWorkouts(model);

        assertThat(view).isEqualTo("admin/workouts");
        assertThat(model.getAttribute("workouts")).isEqualTo(List.of(plan));
    }

    @Test
    void whenLoadNewWorkout_thenModelPrepared() {
        WorkoutType wt = new WorkoutType();
        when(workoutService.getAllWorkoutTypes()).thenReturn(List.of(wt));

        Model model = new ConcurrentModel();
        String view = underTest.loadNewWorkout(model);

        assertThat(view).isEqualTo("admin/new-workout");
        assertThat(model.getAttribute("workoutPlan")).isInstanceOf(WorkoutPlan.class);
        assertThat(model.getAttribute("allWorkoutTypes")).isEqualTo(List.of(wt));
    }

    @Test
    void whenCreateNewWorkout_thenDelegatesToService() {
        WorkoutPlan plan = new WorkoutPlan();

        String view = underTest.createNewWorkout(plan);

        verify(workoutService, times(1)).saveWorkout(plan);
        assertThat(view).isEqualTo("redirect:/admin/workouts");
    }

    @Test
    void whenViewWorkoutDetails_thenWorkoutPlanReturned() {
        WorkoutPlan plan = new WorkoutPlan();
        plan.setId(1L);
        when(workoutService.getWorkoutById(1L)).thenReturn(plan);

        Model model = new ConcurrentModel();
        String view = underTest.viewWorkoutDetails(1L, model);

        assertThat(view).isEqualTo("admin/workout-details");
        assertThat(model.getAttribute("workoutPlan")).isEqualTo(plan);
    }

    @Test
    void whenViewWorkoutDetails_withInvalidId_thenRedirects() {
        when(workoutService.getWorkoutById(99L)).thenReturn(null);

        String view = underTest.viewWorkoutDetails(99L, new ConcurrentModel());

        assertThat(view).isEqualTo("redirect:/admin/workouts");
    }
}
