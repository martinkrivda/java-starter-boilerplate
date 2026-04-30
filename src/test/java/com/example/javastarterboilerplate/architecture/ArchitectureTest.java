package com.example.javastarterboilerplate.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

  private final JavaClasses productionClasses =
      new ClassFileImporter()
          .withImportOption(new ImportOption.DoNotIncludeTests())
          .importPackages("com.example.javastarterboilerplate");

  @Test
  void domainDoesNotDependOnFrameworksOrVendorSdks() {
    noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "io.micronaut..",
            "jakarta.persistence..",
            "software.amazon.awssdk..",
            "eu.europa..",
            "org.apache.pdfbox..")
        .check(productionClasses);
  }

  @Test
  void apiDoesNotDependOnInfrastructureOrVendorSdks() {
    noClasses()
        .that()
        .resideInAPackage("..api..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "..infrastructure..", "software.amazon.awssdk..", "eu.europa..", "org.apache.pdfbox..")
        .check(productionClasses);
  }
}
