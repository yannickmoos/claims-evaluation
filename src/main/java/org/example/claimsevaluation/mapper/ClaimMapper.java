package org.example.claimsevaluation.mapper;
import org.example.claimsevaluation.domain.Claim;
import org.example.claimsevaluation.domain.ClaimDecision;
import org.example.claimsevaluation.domain.Policy;
import org.example.claimsevaluation.dto.ClaimRequestDto;
import org.example.claimsevaluation.dto.ClaimResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClaimMapper {
    Claim claimRequestToClaim(ClaimRequestDto request);

    @Mapping(target = "startDate", source = "policyStartDate")
    @Mapping(target = "endDate", source = "policyEndDate")
    Policy claimRequestToPolicy(ClaimRequestDto request);

    ClaimResponseDto claimDecisionToResponse(ClaimDecision decision);
}
