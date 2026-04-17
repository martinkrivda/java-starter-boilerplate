package com.example.javastarterboilerplate;

record CliCommandResult(boolean startServer, int exitCode, String[] serverArgs) {

    static CliCommandResult startServer(String[] serverArgs) {
        return new CliCommandResult(true, 0, serverArgs);
    }

    static CliCommandResult exit(int exitCode) {
        return new CliCommandResult(false, exitCode, new String[0]);
    }
}
