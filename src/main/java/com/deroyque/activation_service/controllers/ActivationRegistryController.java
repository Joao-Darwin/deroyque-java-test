package com.deroyque.activation_service.controllers;

import com.deroyque.activation_service.business.ActivationRegistryBusiness;
import com.deroyque.activation_service.controllers.docs.IActivationRegistryController;
import com.deroyque.activation_service.controllers.dtos.request.CreateActivationRequestDTO;
import com.deroyque.activation_service.controllers.dtos.response.CreateActivationResponseDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/requests")
public class ActivationRegistryController implements IActivationRegistryController {

    private final ActivationRegistryBusiness activationRegistryBusiness;

    @Autowired
    public ActivationRegistryController(ActivationRegistryBusiness activationRegistryBusiness) {
        this.activationRegistryBusiness = activationRegistryBusiness;
    }

    @Override
    @PostMapping
    public ResponseEntity<CreateActivationResponseDTO> create(@RequestBody @Valid CreateActivationRequestDTO request) {
        CreateActivationResponseDTO response = activationRegistryBusiness.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
