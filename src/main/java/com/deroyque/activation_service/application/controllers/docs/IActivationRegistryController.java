package com.deroyque.activation_service.application.controllers.docs;

import com.deroyque.activation_service.application.usecases.create.CreateActivationRequestDTO;
import com.deroyque.activation_service.application.usecases.create.CreateActivationResponseDTO;
import com.deroyque.activation_service.application.usecases.retrive.byid.GetActivationByIdResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

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

    ResponseEntity<GetActivationByIdResponseDTO> findById(@PathVariable UUID id);
}