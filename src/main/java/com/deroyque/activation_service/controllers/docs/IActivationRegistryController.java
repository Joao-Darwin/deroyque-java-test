package com.deroyque.activation_service.controllers.docs;

import com.deroyque.activation_service.controllers.dtos.request.CreateActivationRequestDTO;
import com.deroyque.activation_service.controllers.dtos.response.CreateActivationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(
        name = "Activation Registry",
        description = "Endpoints for managing activation requests"
)
public interface IActivationRegistryController {

    @Operation(
            summary = "Create an activation request",
            description = """
            Creates a new activation request.
            The request is registered for processing according
            to the activation workflow.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Activation request created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "An activation request with the same external ID already exists"
            )
    })
    ResponseEntity<CreateActivationResponseDTO> create(
            @Valid @RequestBody CreateActivationRequestDTO request
    );
}