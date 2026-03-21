package com.timcritt.tfg.infrastructure.web;

import com.timcritt.tfg.application.port.inbound.UserUseCase;
import com.timcritt.tfg.infrastructure.web.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserUseCase useCase;

    public UserController(UserUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody UserDto dto) {
        return ResponseEntity.ok(
                UserDtoMapper.toDto(
                        useCase.createUser(dto.getUsername(), dto.getName(), dto.getSurname(), dto.getEmail())
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(UserDtoMapper.toDto(useCase.getUserById(id)));
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
