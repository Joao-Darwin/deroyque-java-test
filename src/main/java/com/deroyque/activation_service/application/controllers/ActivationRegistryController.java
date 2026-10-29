package com.deroyque.activation_service.application.controllers;

import com.deroyque.activation_service.application.controllers.docs.IActivationRegistryController;
import com.deroyque.activation_service.application.usecases.create.CreateActivationRequestDTO;
import com.deroyque.activation_service.application.usecases.create.CreateActivationResponseDTO;
import com.deroyque.activation_service.application.usecases.retrive.byid.GetActivationByIdResponseDTO;
import com.deroyque.activation_service.application.usecases.create.CreateActivationRegistryUseCase;
import com.deroyque.activation_service.application.usecases.retrive.byid.GetActivationByIdUseCase;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/requests")
public class ActivationRegistryController implements IActivationRegistryController {

    private final CreateActivationRegistryUseCase createActivationRegistryUseCase;
    private final GetActivationByIdUseCase getActivationByIdUseCase;

    @Autowired
    public ActivationRegistryController(CreateActivationRegistryUseCase createActivationRegistryUseCase, GetActivationByIdUseCase getActivationByIdUseCase) {
        this.createActivationRegistryUseCase = createActivationRegistryUseCase;
        this.getActivationByIdUseCase = getActivationByIdUseCase;
    }

    @Override
    @PostMapping
    public ResponseEntity<CreateActivationResponseDTO> create(@RequestBody @Valid CreateActivationRequestDTO request) {
        CreateActivationResponseDTO response = createActivationRegistryUseCase.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<GetActivationByIdResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(getActivationByIdUseCase.findById(id));
    }
}
