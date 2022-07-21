package com.example.userservice.service;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.vo.RequestUpdateUser;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseOrder;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService{
    UserRepository userRepository;

    BCryptPasswordEncoder passwordEncoder;

    Environment env;
    OrderServiceClient orderServiceClient;

    CircuitBreakerFactory circuitBreakerFactory;

    ModelMapper modelMapper;




    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(username);

        if(userEntity == null){
            throw new UsernameNotFoundException(username);
        }

        return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(), true, true, true, true, new ArrayList<>());
    }
    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           Environment env, OrderServiceClient orderServiceClient,
                           CircuitBreakerFactory circuitBreakerFactory,
                           ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.env = env;
        this.orderServiceClient = orderServiceClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.modelMapper = modelMapper;
    }




    @Override                    // 입력시 Dto 로 들어오는 건 당연하다.
    public UserDto createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity = mapper.map(userDto, UserEntity.class);
        userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));

        userRepository.save(userEntity);
        UserDto returnUserDto = mapper.map(userEntity, UserDto.class);
        return returnUserDto;
    }

    @Override
    public UserDto getUserByuserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null){
            throw new UsernameNotFoundException("User not found");
        }

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);
//        List<ResponseOrder> orders = new ArrayList<>();
//        String orderUrl = ""

//        userDto.setOrders(orders);

        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");



        /*Using a feign client*/
        List<ResponseOrder> ordersList = orderServiceClient.getOrders(userId);
        userDto.setOrders(ordersList);


        return userDto;
    }

    @Override
    public Iterable<UserEntity> getUserByAll() {
        return userRepository.findAll();
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null)
            throw new UsernameNotFoundException(email);

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = mapper.map(userEntity, UserDto.class);
        return userDto;
    }

    @Override
    public UserDto updateUsers(RequestUser param) {
        UserEntity userEntity = userRepository.findByEmail(param.getEmail());
        if (userEntity == null){
            throw new UsernameNotFoundException("User not found");
        }

        userEntity.setEncryptedPwd(passwordEncoder.encode(param.getPwd()));
        UserEntity returnEntity = userRepository.save(userEntity);
        return modelMapper.map(returnEntity, UserDto.class);
    }

    @Override
    public UserDto deleteUser(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null){
            throw new UsernameNotFoundException("User not found");
        }
        userRepository.deleteById(userEntity.getId());
        return modelMapper.map(userEntity, UserDto.class);
    }
}
