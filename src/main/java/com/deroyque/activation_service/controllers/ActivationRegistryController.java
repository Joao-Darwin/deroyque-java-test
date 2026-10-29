package com.deroyque.activation_service.controllers;

import com.deroyque.activation_service.controllers.docs.IActivationRegistryController;
import com.deroyque.activation_service.controllers.dtos.request.CreateActivationRequestDTO;
import com.deroyque.activation_service.controllers.dtos.response.CreateActivationResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/requests")
public class ActivationRegistryController implements IActivationRegistryController {

    @Override
    @PostMapping
    public ResponseEntity<CreateActivationResponseDTO> create(@RequestBody @Valid CreateActivationRequestDTO request) {
        return null;
    }
}
