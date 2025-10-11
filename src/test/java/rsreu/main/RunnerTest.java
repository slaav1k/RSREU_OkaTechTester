package rsreu.main;


import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/registration.feature",
        glue = "rsreu.steps",
        tags = "@success or @fail",
        snippets = CucumberOptions.SnippetType.UNDERSCORE,
        plugin = {"pretty", "html:target/cucumber", "junit:target/surefire-reports/junit-report.xml"}
)
public class RunnerTest {
}