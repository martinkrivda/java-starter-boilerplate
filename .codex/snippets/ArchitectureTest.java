package com.example;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

final class ArchitectureTest {

  private final JavaClasses classes = new ClassFileImporter().importPackages("com.example");

  @Test
  void domainDoesNotDependOnFrameworksOrInfrastructure() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "io.micronaut..",
                "jakarta.persistence..",
                "org.hibernate..",
                "software.amazon.awssdk..",
                "com.azure..",
                "com.google.cloud..");

    rule.check(classes);
  }
}
