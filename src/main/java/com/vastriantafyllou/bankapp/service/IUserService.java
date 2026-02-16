package com.vastriantafyllou.bankapp.service;

import com.vastriantafyllou.bankapp.core.enums.Role;
import com.vastriantafyllou.bankapp.dto.ChangePasswordDTO;
import com.vastriantafyllou.bankapp.dto.RegisterDTO;
import com.vastriantafyllou.bankapp.dto.UpdateProfileDTO;
import com.vastriantafyllou.bankapp.dto.UserReadOnlyDTO;
import com.vastriantafyllou.bankapp.model.AppUser;

import java.util.List;

public interface IUserService {
    AppUser register(RegisterDTO dto);

    AppUser getByUsername(String username);
    void updateProfile(String username, UpdateProfileDTO dto);
    void changePassword(String username, ChangePasswordDTO dto);

    List<UserReadOnlyDTO> getAllUsers();
    UserReadOnlyDTO getUserById(Long id);
    void blockUser(Long userId);
    void unblockUser(Long userId);
    void assignRole(Long userId, Role role);
    void removeRole(Long userId, Role role);

    long countUsers();
}
