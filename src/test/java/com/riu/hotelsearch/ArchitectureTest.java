package com.riu.hotelsearch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.riu.hotelsearch", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    @SuppressWarnings("unused")
    static final ArchRule HEXAGONAL_DEPENDENCIES = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Infrastructure").definedBy("..infrastructure..")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
            .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer();

    @ArchTest
    @SuppressWarnings("unused")
    static final ArchRule DOMAIN_MUST_NOT_DEPEND_ON_OUTER_LAYERS_OR_FRAMEWORKS = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..application..",
                    "..infrastructure..",
                    "org.springframework..",
                    "com.fasterxml.jackson..",
                    "jakarta..");

    @ArchTest
    @SuppressWarnings("unused")
    static final ArchRule APPLICATION_MUST_NOT_DEPEND_ON_INFRASTRUCTURE_OR_FRAMEWORKS = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..infrastructure..",
                    "org.springframework..",
                    "com.fasterxml.jackson..",
                    "jakarta..");

    @ArchTest
    @SuppressWarnings("unused")
    static final ArchRule INBOUND_ADAPTERS_MUST_NOT_DEPEND_ON_OUTBOUND_ADAPTERS = noClasses()
            .that().resideInAPackage("..infrastructure.adapter.in..")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure.adapter.out..");

    @ArchTest
    @SuppressWarnings("unused")
    static final ArchRule INBOUND_ADAPTERS_MUST_USE_INPUT_PORTS = noClasses()
            .that().resideInAPackage("..infrastructure.adapter.in..")
            .should().dependOnClassesThat()
            .resideInAPackage("..application.service..");

    @ArchTest
    @SuppressWarnings("unused")
    static final ArchRule OUTBOUND_ADAPTERS_MUST_NOT_USE_INPUT_PORTS = noClasses()
            .that().resideInAPackage("..infrastructure.adapter.out..")
            .should().dependOnClassesThat()
            .resideInAPackage("..application.port.in..");
}
