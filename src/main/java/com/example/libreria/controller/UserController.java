package com.example.libreria.controller;

import com.example.libreria.dto.UserRequestDTO;
import com.example.libreria.dto.UserResponseDTO;
import com.example.libreria.service.UserService;
import com.sun.jdi.VoidType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO requestDTO) {
       // TODO: Implementar la creación de un usuario
        try {
            UserResponseDTO DTO = userService.createUser(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(DTO);
        } catch(Exception e) {
            UserResponseDTO DTO =  new UserResponseDTO();
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(DTO);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        // TODO: Implementar la obtención de un usuario por su ID
        try {
            UserResponseDTO DTO = userService.getUserById(id);
            return ResponseEntity.status(HttpStatus.FOUND).body(DTO);
        } catch(Exception e) {
            UserResponseDTO DTO =new UserResponseDTO();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(DTO);
        }
    }
    
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        //TODO: Implementar la obtención de todos los usuarios
        try {
           List<UserResponseDTO>  DTO = userService.getAllUsers();
            return ResponseEntity.status(HttpStatus.FOUND).body(DTO);
        } catch(Exception e) {
           List<UserResponseDTO>  DTO =new ArrayList<>();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(DTO);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO requestDTO) {
        //TODO: Implementar la actualización de un usuario
        try {
            UserResponseDTO DTO = userService.updateUser(id,requestDTO);
            return ResponseEntity.status(HttpStatus.OK).body(DTO);
        } catch(Exception e) {
            UserResponseDTO DTO =new UserResponseDTO();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(DTO);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        //TODO: Implementar la eliminación de un usuario

             userService.deleteUser(id);
            return ResponseEntity.noContent().build();

    }
}

