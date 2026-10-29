package com.deroyque.activation_service.controllers.docs;

import com.deroyque.activation_service.controllers.dtos.request.CreateActivationRequestDTO;
import com.deroyque.activation_service.controllers.dtos.response.CreateActivationResponseDTO;
import org.springframework.http.ResponseEntity;

public interface IActivationRegistryController {

    ResponseEntity<CreateActivationResponseDTO> create(CreateActivationRequestDTO request);
}
