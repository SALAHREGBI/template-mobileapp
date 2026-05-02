package ma.daba;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Verifies that the Spring Modulith module boundaries declared in
 * each package-info.java are not violated at test time.
 *
 * <p>Run: {@code mvn test -Dtest=ModularityTest}
 */
class ModularityTest {

    ApplicationModules modules = ApplicationModules.of(DabaApplication.class);

    @Test
    void verifyModuleBoundaries() {
        modules.verify();
    }

    @Test
    @Disabled("Keeps boilerplate workspaces clean — enable locally to regenerate PlantUML")
    void writeDocumentationSnippets() {
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }
}
