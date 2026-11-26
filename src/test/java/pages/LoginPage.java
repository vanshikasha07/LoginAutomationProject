package pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {

    public static final String LOGIN_URL =
            "https://the-internet.herokuapp.com/login";
    public static final String SECURE_URL =
            "https://the-internet.herokuapp.com/secure";

    private static final Logger LOGGER = LogManager.getLogger(LoginPage.class);
    private static final By USERNAME = By.id("username");
    private static final By PASSWORD = By.id("password");
    private static final By SUBMIT_BUTTON =
            By.cssSelector("button[type='submit']");
    private static final By HEADING = By.tagName("h2");
    private static final By FLASH_MESSAGE = By.id("flash");
    private static final By DISMISS_FLASH =
            By.cssSelector("#flash .close");
    private static final By LOGOUT_BUTTON = By.cssSelector("a.button");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public LoginPage(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
    }

    public void open() {
        driver.get(LOGIN_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME));
    }

    public String getTitle() {
        return driver.getTitle();
    }

    public String getHeadingText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(HEADING))
                .getText();
    }

    public boolean isUsernameDisplayed() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME))
                .isDisplayed();
    }

    public boolean isPasswordDisplayed() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(PASSWORD))
                .isDisplayed();
    }

    public String getSubmitButtonText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                SUBMIT_BUTTON)).getText();
    }

    public void enterPassword(String password) {
        WebElement passwordField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(PASSWORD));
        passwordField.clear();
        passwordField.sendKeys(password);
    }

    public String getPasswordInputType() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(PASSWORD))
                .getAttribute("type");
    }

    public String getPasswordInputValue() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(PASSWORD))
                .getAttribute("value");
    }

    public void login(String username, String password) {
        LOGGER.info("Attempting login with username '{}'", username);
        WebElement usernameField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(USERNAME));
        WebElement passwordField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(PASSWORD));
        usernameField.clear();
        usernameField.sendKeys(username);
        passwordField.clear();
        passwordField.sendKeys(password);
        submitLogin();
    }

    public void loginWithEnter(String username, String password) {
        LOGGER.info("Attempting login with username '{}' using Enter", username);
        WebElement usernameField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(USERNAME));
        WebElement passwordField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(PASSWORD));
        usernameField.clear();
        usernameField.sendKeys(username);
        passwordField.clear();
        passwordField.sendKeys(password, Keys.ENTER);
    }

    public void submitLogin() {
        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BUTTON)).click();
    }

    public void waitForSecureArea() {
        wait.until(ExpectedConditions.urlToBe(SECURE_URL));
    }

    public String flashMessage() {
        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(FLASH_MESSAGE))
                .getText();
    }

    public void dismissError() {
        wait.until(ExpectedConditions.elementToBeClickable(DISMISS_FLASH)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(FLASH_MESSAGE));
    }

    public boolean isLogoutButtonDisplayed() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                LOGOUT_BUTTON)).isDisplayed();
    }

    public void logout() {
        LOGGER.info("Logging out of the secure area");
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON)).click();
        wait.until(ExpectedConditions.urlContains("/login"));
    }

    public void openSecureArea() {
        driver.get(SECURE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(FLASH_MESSAGE));
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
