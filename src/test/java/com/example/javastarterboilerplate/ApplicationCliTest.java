package com.example.javastarterboilerplate;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ApplicationCliTest {

  @AfterEach
  void clearVersionOverride() {
    System.clearProperty("app.version");
  }

  @Test
  void startsServerWhenNoArgumentsAreProvided() {
    CliCommandResult result = ApplicationCli.handle(new String[0], outputStream(), outputStream());

    assertThat(result.startServer()).isTrue();
    assertThat(result.exitCode()).isZero();
    assertThat(result.serverArgs()).isEmpty();
  }

  @Test
  void startsServerWithRemainingArgumentsForServeCommand() {
    CliCommandResult result =
        ApplicationCli.handle(
            new String[] {"serve", "--port=8081"}, outputStream(), outputStream());

    assertThat(result.startServer()).isTrue();
    assertThat(result.serverArgs()).containsExactly("--port=8081");
  }

  @Test
  void printsHelp() {
    ByteArrayOutputStream stdout = output();

    CliCommandResult result =
        ApplicationCli.handle(new String[] {"help"}, stream(stdout), outputStream());

    assertThat(result.startServer()).isFalse();
    assertThat(result.exitCode()).isZero();
    assertThat(stdout.toString()).contains("Usage:");
    assertThat(stdout.toString()).contains("serve");
  }

  @Test
  void printsVersionFromSystemPropertyWhenAvailable() {
    System.setProperty("app.version", "1.2.3");
    ByteArrayOutputStream stdout = output();

    CliCommandResult result =
        ApplicationCli.handle(new String[] {"version"}, stream(stdout), outputStream());

    assertThat(result.exitCode()).isZero();
    assertThat(stdout.toString()).contains("java-starter-boilerplate 1.2.3");
  }

  @Test
  void printsEnvironmentDetails() {
    ByteArrayOutputStream stdout = output();

    CliCommandResult result =
        ApplicationCli.handle(new String[] {"env"}, stream(stdout), outputStream());

    assertThat(result.exitCode()).isZero();
    assertThat(stdout.toString()).contains("app=java-starter-boilerplate");
    assertThat(stdout.toString()).contains("version=");
    assertThat(stdout.toString()).contains("java=");
  }

  @Test
  void rejectsUnknownCommands() {
    ByteArrayOutputStream stderr = output();

    CliCommandResult result =
        ApplicationCli.handle(new String[] {"unknown"}, outputStream(), stream(stderr));

    assertThat(result.startServer()).isFalse();
    assertThat(result.exitCode()).isEqualTo(1);
    assertThat(stderr.toString()).contains("Unknown command: unknown");
    assertThat(stderr.toString()).contains("Usage:");
  }

  @Test
  void resolvesDefaultVersionWhenNoOverrideIsPresent() {
    assertThat(ApplicationCli.resolveVersion()).isEqualTo("0.1.0-SNAPSHOT");
  }

  @Test
  void resolvesManifestVersionWhenSystemPropertyIsMissing() {
    assertThat(ApplicationCli.resolveVersion(null, "2.0.0")).isEqualTo("2.0.0");
  }

  @Test
  void ignoresBlankSystemVersionWhenManifestVersionIsPresent() {
    assertThat(ApplicationCli.resolveVersion("   ", "2.1.0")).isEqualTo("2.1.0");
  }

  @Test
  void fallsBackToDefaultVersionWhenOverridesAreBlank() {
    assertThat(ApplicationCli.resolveVersion(" ", " ")).isEqualTo("0.1.0-SNAPSHOT");
  }

  @Test
  void exposesCliCommandResultFactories() {
    CliCommandResult startServer = CliCommandResult.startServer(new String[] {"a"});
    CliCommandResult exit = CliCommandResult.exit(7);

    assertThat(startServer.startServer()).isTrue();
    assertThat(startServer.serverArgs()).containsExactly("a");
    assertThat(exit.startServer()).isFalse();
    assertThat(exit.exitCode()).isEqualTo(7);
    assertThat(exit.serverArgs()).isEmpty();
  }

  private ByteArrayOutputStream output() {
    return new ByteArrayOutputStream();
  }

  private PrintStream outputStream() {
    return stream(output());
  }

  private PrintStream stream(ByteArrayOutputStream outputStream) {
    return new PrintStream(outputStream, true);
  }
}
