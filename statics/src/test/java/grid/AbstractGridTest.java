package grid;

import integration.IntegrationTest;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.grid.Main;

import static com.codeborne.selenide.Selenide.closeWebDriver;
import static org.openqa.selenium.net.PortProber.findFreePort;

abstract class AbstractGridTest extends IntegrationTest {
  int hubPort;

  @BeforeEach
  final void setUpGrid() throws Exception {
    closeWebDriver();

    hubPort = findFreePort();
    Main.main(new String[]{"standalone", "--port", String.valueOf(hubPort)});

    WebDriverManager.chromedriver().setup();
  }

  @AfterEach
  final void tearDownGrid() {
    closeWebDriver();
  }
}
