package com.timcritt.tfg.infrastructure.web;

import com.timcritt.tfg.application.port.inbound.UserUseCase;
import com.timcritt.tfg.domain.model.RoleType;
import com.timcritt.tfg.domain.model.User;
import com.timcritt.tfg.infrastructure.web.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserUseCase useCase;

    public UserController(UserUseCase useCase) {
        this.useCase = useCase;
    }



    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
        log.info("GET /api/users/{}", id);

        var user = useCase.getUserById(id);
        log.debug("Found user id={} username={} email={}", user.getId(), user.getUsername(), user.getEmail());

        return ResponseEntity.ok(UserDtoMapper.toDto(user));
    }

    @GetMapping("/teachers")
    public List<UserDto> getAllTeachers() {
        log.info("GET /api/users");

        List<User> users = useCase.getAllUsersByRoleType(RoleType.TEACHER);
        return users.stream().map(UserDtoMapper::toDto).collect(Collectors.toList());

    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable Long id, @RequestBody UserDto dto) {
        return ResponseEntity.ok(
                UserDtoMapper.toDto(
                        useCase.updateUser(id, dto.getUsername(), dto.getName(), dto.getSurname(), dto.getEmail())
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        useCase.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
