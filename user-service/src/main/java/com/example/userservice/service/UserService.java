package com.example.userservice.service;


import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;

import java.util.Iterator;

public interface UserService {
    UserDto createUser(UserDto userDto);

    UserDto getUserByuserId(String userId);
    Iterable<UserEntity> getUserByAll();
}
