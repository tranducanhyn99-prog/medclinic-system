package com.medclinic;

import com.medclinic.entity.User;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.RoleRepository;
import com.medclinic.repository.UserRepository;
import com.medclinic.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceExtendedTest {
    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService service;

    private User user(long id) {        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setEmail("user" + id + "@example.com");
        user.setFullName("User " + id);
        user.setPassword("encoded-old");
        user.setIsActive(true);
        return user;
    }

    @Test
    void findsByUsernameThenEmailAndReportsMissingUser() {
        User byUsername = user(1L);
        User byEmail = user(2L);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(byUsername));
        when(userRepository.findByUsername("mail@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("mail@example.com")).thenReturn(Optional.of(byEmail));
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("missing")).thenReturn(Optional.empty());

        assertSame(byUsername, service.findByUsername("user1"));
        assertSame(byEmail, service.findByUsername("mail@example.com"));
        assertThrows(ResourceNotFoundException.class, () -> service.findByUsername("missing"));
        verify(userRepository, never()).findByEmail("user1");
    }

    @Test
    void delegatesFindAndSearchVariants() {
        User u = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(userRepository.findAll()).thenReturn(List.of(u));
        when(userRepository.searchUsers("abc")).thenReturn(List.of(u));

        assertSame(u, service.findById(1L));
        assertEquals(List.of(u), service.searchUsers(null));
        assertEquals(List.of(u), service.searchUsers("   "));
        assertEquals(List.of(u), service.searchUsers(" abc "));

        when(userRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(9L));
    }

    @Test
    void updatesProfileAndNormalizesOptionalFields() {
        User u = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(userRepository.findByPhone("0902")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updated = service.updateUserProfile(1L, "  Nguyễn An  ", " 0902 ", null, null, "  HCM  ");
        assertEquals("Nguyễn An", updated.getFullName());
        assertEquals("0902", updated.getPhone());
        assertEquals("HCM", updated.getAddress());

        User noOptional = service.updateUserProfile(1L, "Nguyễn An", " ", null, null, null);        assertNull(noOptional.getPhone());
        assertNull(noOptional.getAddress());
    }

    @Test
    void rejectsBlankNameAndPhoneOwnedByAnotherUser() {
        User u = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        assertThrows(BusinessLogicException.class,
                () -> service.updateUserProfile(1L, " ", null, null, null, null));

        User owner = user(2L);
        when(userRepository.findByPhone("0909")).thenReturn(Optional.of(owner));
        assertThrows(BusinessLogicException.class,
                () -> service.updateUserProfile(1L, "Valid", "0909", null, null, null));
    }

    @Test
    void allowsKeepingOwnPhone() {
        User u = user(1L); u.setPhone("0901");
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(userRepository.findByPhone("0901")).thenReturn(Optional.of(u));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals("0901", service.updateUserProfile(1L, "Valid", "0901", null, null, null).getPhone());
    }

    @Test
    void changesPasswordOnlyWhenOldPasswordMatches() {
        User u = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));        when(passwordEncoder.matches("old", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new");

        service.changePassword(1L, "old", "new-password");
        assertEquals("encoded-new", u.getPassword());
        verify(userRepository).save(u);

        u.setPassword("encoded-old");
        when(passwordEncoder.matches("bad", "encoded-old")).thenReturn(false);
        assertThrows(BusinessLogicException.class,
                () -> service.changePassword(1L, "bad", "another-password"));
    }

    @Test
    void togglesUserStatus() {
        User u = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        service.toggleUserStatus(1L);

        assertFalse(u.getIsActive());
        verify(userRepository).save(u);
    }
}
