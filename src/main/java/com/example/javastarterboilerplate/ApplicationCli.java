package com.example.javastarterboilerplate;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Locale;

final class ApplicationCli {

  private static final String APPLICATION_NAME = "java-starter-boilerplate";
  private static final String DEFAULT_VERSION = "0.1.0-SNAPSHOT";

  private ApplicationCli() {}

  static CliCommandResult handle(String[] args, PrintStream out, PrintStream err) {
    if (args.length == 0) {
      return CliCommandResult.startServer(args);
    }

    String command = args[0].toLowerCase(Locale.ROOT);
    return switch (command) {
      case "serve" -> CliCommandResult.startServer(Arrays.copyOfRange(args, 1, args.length));
      case "help", "--help", "-h" -> {
        printHelp(out);
        yield CliCommandResult.exit(0);
      }
      case "version", "--version", "-v" -> {
        out.printf("%s %s%n", APPLICATION_NAME, resolveVersion());
        yield CliCommandResult.exit(0);
      }
      case "env" -> {
        out.printf("app=%s%n", APPLICATION_NAME);
        out.printf("version=%s%n", resolveVersion());
        out.printf("java=%s%n", System.getProperty("java.version", "unknown"));
        out.printf(
            "micronaut.environments=%s%n",
            System.getenv().getOrDefault("MICRONAUT_ENVIRONMENTS", "not-set"));
        yield CliCommandResult.exit(0);
      }
      default -> {
        err.printf("Unknown command: %s%n%n", args[0]);
        printHelp(err);
        yield CliCommandResult.exit(1);
      }
    };
  }

  static String resolveVersion() {
    String implementationVersion = Application.class.getPackage().getImplementationVersion();
    return resolveVersion(System.getProperty("app.version"), implementationVersion);
  }

  static String resolveVersion(String systemVersion, String implementationVersion) {
    if (systemVersion != null && !systemVersion.isBlank()) {
      return systemVersion;
    }
    if (implementationVersion != null && !implementationVersion.isBlank()) {
      return implementationVersion;
    }
    return DEFAULT_VERSION;
  }

  private static void printHelp(PrintStream out) {
    out.println("Usage:");
    out.println("  java -jar java-starter-boilerplate-<version>-all.jar serve");
    out.println("  java -jar java-starter-boilerplate-<version>-all.jar version");
    out.println("  java -jar java-starter-boilerplate-<version>-all.jar env");
    out.println("  java -jar java-starter-boilerplate-<version>-all.jar help");
    out.println();
    out.println("Commands:");
    out.println("  serve    Start the Micronaut HTTP service");
    out.println("  version  Print the application version");
    out.println("  env      Print basic runtime environment details");
    out.println("  help     Print this help");
    out.println();
    out.println("Behavior:");
    out.println("  Running without arguments starts the HTTP service.");
  }
}
