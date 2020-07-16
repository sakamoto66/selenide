package grid;

import integration.IntegrationTest;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.chrome.ChromeDriverInfo;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.GeckoDriverInfo;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.grid.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.closeWebDriver;
import static org.openqa.selenium.net.PortProber.findFreePort;

abstract class AbstractGridTest extends IntegrationTest {
  private static final Logger logger = LoggerFactory.getLogger(AbstractGridTest.class);

  int hubPort;

  @BeforeEach
  final void setUpGrid() {
    closeWebDriver();
    WebDriverManager.chromedriver().setup();

    logger.info("GeckoDriverInfo.isAvailable: ");
    logger.info("  -> {}", new GeckoDriverInfo().isAvailable());
    try {
      GeckoDriverService.createDefaultService();
    }
    catch (Exception e) {
      logger.error("Failed to check gecko driver service");
    }

    logger.info("ChromeDriverInfo.isAvailable: ");
    logger.info("  -> {}", new ChromeDriverInfo().isAvailable());
    try {
      ChromeDriverService.createDefaultService();
    }
    catch (Exception e) {
      logger.error("Failed to check chrome driver service");
    }

    hubPort = findFreePort();
    Main.main(new String[]{"standalone", "--port", String.valueOf(hubPort)});
  }

  @AfterEach
  final void tearDownGrid() {
    closeWebDriver();
  }
}
