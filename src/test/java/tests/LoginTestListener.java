
package tests;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import org.testng.IExecutionListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.util.UUID;

public class LoginTestListener implements ITestListener, IExecutionListener {

    private static final Logger LOGGER =
            LogManager.getLogger(LoginTestListener.class);

    private static final Path SCREENSHOT_DIRECTORY =
            Paths.get("target", "screenshots");

    private static final Path REPORT_FILE =
            Paths.get("target", "ExtentReports", "ExtentReport.html");

    private static final ThreadLocal<ExtentTest> CURRENT_TEST =
            new ThreadLocal<>();

    private static ExtentReports extentReports;

    // INITIALIZE REPORT
    @Override
    public void onExecutionStart() {

        try {
            Files.createDirectories(SCREENSHOT_DIRECTORY);
            Files.createDirectories(REPORT_FILE.getParent());

            // DELETE OLD REPORT
            Files.deleteIfExists(REPORT_FILE);

            System.out.println("OLD REPORT DELETED");
            System.out.println("CREATING NEW EXTENT REPORT");

            ExtentSparkReporter reporter =
                    new ExtentSparkReporter(REPORT_FILE.toString());

            reporter.config().setDocumentTitle(
                    "Login Automation Test Report");

            reporter.config().setReportName(
                    "Login Automation Results");

            reporter.config().setTheme(Theme.STANDARD);

            extentReports = new ExtentReports();

            extentReports.attachReporter(reporter);

            extentReports.setSystemInfo(
                    "Browser",
                    System.getProperty("browser", "chrome"));

            extentReports.setSystemInfo(
                    "Framework",
                    "Selenium WebDriver + TestNG");

            LOGGER.info("Extent report initialized");

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Could not initialize report or screenshot folder",
                    exception);
        }
    }

    // FLUSH REPORT
    @Override
    public void onExecutionFinish() {

        if (extentReports != null) {

            extentReports.flush();

            LOGGER.info("Extent report generated at {}",
                    REPORT_FILE.toAbsolutePath());

            System.out.println("EXTENT REPORT UPDATED: "
                    + REPORT_FILE.toAbsolutePath());
        }
    }

    // TEST START
    @Override
    public void onTestStart(ITestResult result) {

        String description = result.getMethod().getDescription();

        ExtentTest test = extentReports.createTest(
                result.getMethod().getMethodName(),
                description == null ? "" : description);

        CURRENT_TEST.set(test);

        LOGGER.info("Starting test: {}", result.getName());
    }

    // TEST SUCCESS
    @Override
    public void onTestSuccess(ITestResult result) {

        long duration =
                result.getEndMillis() - result.getStartMillis();

        LOGGER.info("Test passed: {} ({} ms)",
                result.getName(), duration);

        ExtentTest test = CURRENT_TEST.get();

        if (test != null) {
            test.log(Status.PASS, "Passed in " + duration + " ms");
        }

        CURRENT_TEST.remove();
    }

    // TEST FAILURE
    @Override
    public void onTestFailure(ITestResult result) {

        System.out.println("TEST FAILURE DETECTED: "
                + result.getName());

        long duration =
                result.getEndMillis() - result.getStartMillis();

        Throwable failure = result.getThrowable();

        LOGGER.error("Test failed: {} ({} ms)",
                result.getName(), duration, failure);

        ExtentTest test = CURRENT_TEST.get();

        if (test != null) {

            test.log(Status.FAIL, "Failed in " + duration + " ms");

            if (failure != null) {
                test.fail(failure);
            }
        }

        Path screenshot = captureScreenshot(result);

        if (screenshot != null && test != null) {

            test.addScreenCaptureFromPath(
                    "../screenshots/" + screenshot.getFileName());

            System.out.println("SCREENSHOT SAVED: "
                    + screenshot.toAbsolutePath());
        }

        CURRENT_TEST.remove();
    }

    // TEST SKIPPED
    @Override
    public void onTestSkipped(ITestResult result) {

        LOGGER.warn("Test skipped: {}", result.getName());

        ExtentTest test = CURRENT_TEST.get();

        if (test != null) {
            test.log(Status.SKIP, "Skipped");
        }

        CURRENT_TEST.remove();
    }

    // CAPTURE FAILURE SCREENSHOT
    private Path captureScreenshot(ITestResult result) {

        Object instance = result.getInstance();

        if (!(instance instanceof LoginTest)) {

            System.out.println(
                    "ERROR: Test instance is not LoginTest");

            return null;
        }

        LoginTest loginTest = (LoginTest) instance;

        WebDriver driver = loginTest.getDriver();

        if (driver == null) {

            System.out.println("ERROR: WebDriver is NULL");

            return null;
        }

        if (!(driver instanceof TakesScreenshot)) {

            System.out.println(
                    "ERROR: Driver does not support screenshots");

            return null;
        }

        TakesScreenshot screenshotDriver =
                (TakesScreenshot) driver;

        String safeTestName =
                result.getMethod().getMethodName()
                        .replaceAll("[^A-Za-z0-9._-]", "_");

        String fileName = safeTestName + "_"
                + System.currentTimeMillis() + "_"
                + UUID.randomUUID() + ".png";

        Path destination =
                SCREENSHOT_DIRECTORY.resolve(fileName);

        try {

            Files.createDirectories(SCREENSHOT_DIRECTORY);

            System.out.println("CAPTURING SCREENSHOT: "
                    + destination.toAbsolutePath());

            File source =
                    screenshotDriver.getScreenshotAs(OutputType.FILE);

            Files.copy(
                    source.toPath(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING);

            LOGGER.info("Failure screenshot saved to {}",
                    destination);

            return destination;

        } catch (IOException | WebDriverException exception) {

            System.out.println("SCREENSHOT CAPTURE FAILED!");

            LOGGER.error(
                    "Could not capture screenshot for test {}",
                    result.getName(), exception);

            exception.printStackTrace();

            return null;
        }
    }
}
