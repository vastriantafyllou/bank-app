package com.vastriantafyllou.bankapp.service;

import com.vastriantafyllou.bankapp.core.exception.UsernameAlreadyExistsException;
import com.vastriantafyllou.bankapp.dto.RegisterDTO;
import com.vastriantafyllou.bankapp.model.AppUser;

public interface IUserService {
    AppUser register(RegisterDTO dto) throws UsernameAlreadyExistsException;
}
