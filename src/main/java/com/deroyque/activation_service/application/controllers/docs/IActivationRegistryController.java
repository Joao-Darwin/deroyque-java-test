package com.deroyque.activation_service.application.controllers.docs;

import com.deroyque.activation_service.application.usecases.create.CreateActivationRequestDTO;
import com.deroyque.activation_service.application.usecases.create.CreateActivationResponseDTO;
import com.deroyque.activation_service.application.usecases.retrive.byid.GetActivationByIdResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tag(
        name = "Activation Requests",
        description = "Endpoints for managing activation requests"
)
public interface IActivationRegistryController {

    @Operation(
            summary = "Create an activation request",
            description = """
                    Creates a new activation request.
                    The request is identified by its externalId, which ensures
                    idempotency when the same request is submitted multiple times.
                    """
    )
    @ApiResponse(
            responseCode = "201",
            description = "Activation request successfully created",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateActivationResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "200",
            description = "An equivalent activation request already exists",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateActivationResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "The externalId is already associated with a different activation request",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Unexpected internal server error",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    ResponseEntity<CreateActivationResponseDTO> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Activation request data",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateActivationRequestDTO.class)
                    )
            )
            @RequestBody CreateActivationRequestDTO request
    );

    @Operation(
            summary = "Retrieve an activation request by ID",
            description = "Retrieves the details and current status of an activation request using its unique identifier."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Activation request successfully retrieved",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = GetActivationByIdResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid UUID format",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Activation request not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Unexpected internal server error",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    ResponseEntity<GetActivationByIdResponseDTO> findById(
            @Parameter(
                    description = "Unique identifier of the activation request",
                    example = "4bf92f35-7b34-4da6-a3ce-929d0e0e4736",
                    required = true
            )
            @PathVariable UUID id
    );
}