package com.example.javastarterboilerplate;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import java.io.PrintStream;

/**
 * Application entry point.
 *
 * <p>Delegates CLI argument parsing to {@code ApplicationCli}. If the resolved command is {@code
 * serve}, or no arguments are given, the Micronaut runtime is started. Otherwise the process exits
 * with the code determined by the CLI handler.
 */
@OpenAPIDefinition(
    info =
        @Info(
            title = "${api.title}",
            version = "${api.version}",
            description = "${api.description}",
            license = @License(name = "MIT", url = "https://opensource.org/licenses/MIT")))
public final class Application {

  private Application() {}

  public static void main(String[] args) {
    CliCommandResult cliCommandResult = ApplicationCli.handle(args, System.out, System.err);
    if (cliCommandResult.startServer()) {
      Micronaut.run(Application.class, cliCommandResult.serverArgs());
      return;
    }
    exitIfNeeded(cliCommandResult.exitCode(), System.err);
  }

  private static void exitIfNeeded(int exitCode, PrintStream err) {
    if (exitCode == 0) {
      return;
    }
    err.flush();
    System.exit(exitCode);
  }
}
