package com.paradoxdevs.dollar.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class AuthResponse {
    @JsonInclude(Include.NON_NULL)
    private String username;
    @JsonInclude(Include.NON_NULL)
    private String token;
}
