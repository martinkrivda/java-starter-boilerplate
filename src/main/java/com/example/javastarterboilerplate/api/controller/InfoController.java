package com.example.javastarterboilerplate.api.controller;

import com.example.javastarterboilerplate.api.dto.ApplicationInfoResponse;
import com.example.javastarterboilerplate.api.response.ApiResponse;
import com.example.javastarterboilerplate.api.response.ApiResponseFactory;
import com.example.javastarterboilerplate.application.ApplicationInfoService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Operations")
@Controller("${app.api.prefix}/info")
public class InfoController {

  private final ApplicationInfoService applicationInfoService;
  private final ApiResponseFactory responseFactory;

  public InfoController(
      ApplicationInfoService applicationInfoService, ApiResponseFactory responseFactory) {
    this.applicationInfoService = applicationInfoService;
    this.responseFactory = responseFactory;
  }

  @Get
  @Operation(
      summary = "Read application metadata",
      description =
          "Returns application version, active profiles and integration readiness placeholders.")
  public ApiResponse<ApplicationInfoResponse> info(HttpRequest<?> request) {
    return responseFactory.success(request, applicationInfoService.getInfo());
  }
}
