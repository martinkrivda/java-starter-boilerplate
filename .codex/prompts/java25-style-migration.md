# Java 25 / Style Migration Prompt

```text
Align this Micronaut project with Java 25 LTS and repository code style.

Goals:
- Gradle toolchain languageVersion = 25.
- JavaCompile options.release = 25.
- Spotless with google-java-format as formatting authority.
- Checkstyle/ArchUnit/Jacoco gates preserved.
- No preview features unless explicitly requested.
- Documentation updated for Java 25 and local developer setup.

Do not change business behavior unless required for compilation or tests.
```
