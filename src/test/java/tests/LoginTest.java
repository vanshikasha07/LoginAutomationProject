
package tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;

import pages.LoginPage;

public class LoginTest {

    private static final String VALID_USERNAME = "tomsmith";
    private static final String VALID_PASSWORD = "SuperSecretPassword!";

    private WebDriver driver;
    private WebDriverWait wait;
    private LoginPage loginPage;

    @BeforeMethod
    public void setup() {
        driver = WebDriverFactory.createDriver();
        driver.manage().window().maximize();

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        loginPage = new LoginPage(driver, Duration.ofSeconds(10));
        loginPage.open();
    }

    // Used by listener for screenshots
    public WebDriver getDriver() {
        return driver;
    }

    // TEST CASE 1
    @Test(description = "Login page displays expected elements")
    public void loginPageDisplaysExpectedElements() {
        Assert.assertEquals(loginPage.getTitle(), "The Internet");
        Assert.assertEquals(loginPage.getHeadingText(), "Login Page");
        Assert.assertTrue(loginPage.isUsernameDisplayed());
        Assert.assertTrue(loginPage.isPasswordDisplayed());
        Assert.assertEquals(loginPage.getSubmitButtonText(), "Login");
    }

    // TEST CASE 2
    @Test(description = "Password field masks characters")
    public void passwordFieldMasksCharacters() {
        loginPage.enterPassword(VALID_PASSWORD);

        Assert.assertEquals(loginPage.getPasswordInputType(), "password");
        Assert.assertEquals(loginPage.getPasswordInputValue(), VALID_PASSWORD);
    }

    // TEST CASE 3
    @Test(description = "Valid credentials open secure area")
    public void validLoginShowsSecureArea() {
        login(VALID_USERNAME, VALID_PASSWORD);
        loginPage.waitForSecureArea();

        Assert.assertEquals(loginPage.getHeadingText(), "Secure Area");

        Assert.assertTrue(
                loginPage.flashMessage().contains(
                        "You logged into a secure area!"));

        Assert.assertTrue(loginPage.isLogoutButtonDisplayed());
    }

    // TEST CASE 4
    @Test(description = "Login using Enter key")
    public void loginCanBeSubmittedWithEnterKey() {
        loginPage.loginWithEnter(VALID_USERNAME, VALID_PASSWORD);
        loginPage.waitForSecureArea();

        Assert.assertEquals(loginPage.getHeadingText(), "Secure Area");
    }

    // TEST CASE 5
    @Test(description = "Invalid username is rejected")
    public void invalidUsername() {
        login("vanshika", VALID_PASSWORD);
        assertLoginError("Your username is invalid!");
    }

    // TEST CASE 6
    @Test(description = "Invalid password is rejected")
    public void invalidPassword() {
        login(VALID_USERNAME, "hehehe");
        assertLoginError("Your password is invalid!");
    }

    // TEST CASE 7
    @Test(description = "Empty login is rejected")
    public void emptyLogin() {
        submitLogin();
        assertLoginError("Your username is invalid!");
    }

    // TEST CASE 8
    @Test(description = "Empty password is rejected")
    public void emptyPassword() {
        login(VALID_USERNAME, "");
        assertLoginError("Your password is invalid!");
    }

    // TEST CASE 9
    @Test(description = "Whitespace username is rejected")
    public void whitespaceUsernameIsRejected() {
        login("   ", VALID_PASSWORD);
        assertLoginError("Your username is invalid!");
    }

    // TEST CASE 10
    @Test(description = "Invalid login keeps user on login page")
    public void invalidLoginKeepsUserOnLoginPage() {
        login("vanshika", "hehehe");
        assertLoginError("Your username is invalid!");

        Assert.assertTrue(loginPage.getCurrentUrl().contains("/login"));
    }

    // TEST CASE 11
    @Test(description = "Login error can be dismissed")
    public void loginErrorCanBeDismissed() {
        login("vanshika", VALID_PASSWORD);
        assertLoginError("Your username is invalid!");

        loginPage.dismissError();

        Assert.assertTrue(loginPage.getCurrentUrl().contains("/login"));
    }

    // TEST CASE 12
    @Test(description = "Logout returns user to login page")
    public void logoutTest() {
        login(VALID_USERNAME, VALID_PASSWORD);
        loginPage.waitForSecureArea();

        loginPage.logout();

        Assert.assertTrue(
                loginPage.flashMessage().contains(
                        "You logged out of the secure area!"));

        Assert.assertTrue(loginPage.isUsernameDisplayed());
    }

    // TEST CASE 13
    @Test(description = "Secure area requires authentication")
    public void secureAreaRequiresAuthentication() {
        loginPage.openSecureArea();

        wait.until(ExpectedConditions.urlContains("/login"));

        Assert.assertTrue(
                loginPage.flashMessage().contains(
                        "You must login to view the secure area!"));
    }

    // TEST CASE 14
    @Test(description = "Secure area unavailable after logout")
    public void secureAreaIsUnavailableAfterLogout() {
        login(VALID_USERNAME, VALID_PASSWORD);
        loginPage.waitForSecureArea();

        loginPage.logout();
        loginPage.openSecureArea();

        wait.until(ExpectedConditions.urlContains("/login"));

        Assert.assertTrue(
                loginPage.flashMessage().contains(
                        "You must login to view the secure area!"));
    }

    // DATA PROVIDER 1
    @DataProvider(name = "invalidCredentialCases")
    public Object[][] invalidCredentialCases() {
        return new Object[][]{
                {"uppercase username", "TomSmith", VALID_PASSWORD,
                        "Your username is invalid!"},

                {"username with leading whitespace", " tomsmith",
                        VALID_PASSWORD, "Your username is invalid!"},

                {"username with trailing whitespace", "tomsmith ",
                        VALID_PASSWORD, "Your username is invalid!"},

                {"uppercase password", VALID_USERNAME,
                        "supersecretpassword!", "Your password is invalid!"},

                {"password with leading whitespace", VALID_USERNAME,
                        " SuperSecretPassword!", "Your password is invalid!"},

                {"password with trailing whitespace", VALID_USERNAME,
                        "SuperSecretPassword! ", "Your password is invalid!"}
        };
    }

    // TEST CASE 15
    @Test(
            dataProvider = "invalidCredentialCases",
            description = "Invalid credential combinations are rejected"
    )
    public void invalidCredentialCombinationsAreRejected(
            String scenario,
            String username,
            String password,
            String expectedMessage) {

        login(username, password);

        Assert.assertTrue(
                loginPage.flashMessage().contains(expectedMessage),
                "Unexpected validation message for " + scenario);

        Assert.assertTrue(loginPage.getCurrentUrl().contains("/login"));
    }

    // DATA PROVIDER 2
    @DataProvider(name = "credentialCombinations")
    public Object[][] credentialCombinations() {
        return new Object[][]{
                {"valid credentials", VALID_USERNAME, VALID_PASSWORD, true, ""},

                {"invalid username and password", "vanshika", "hehehe",
                        false, "Your username is invalid!"},

                {"valid username and invalid password", VALID_USERNAME,
                        "hehehe", false, "Your password is invalid!"},

                {"invalid username with valid password", "vanshika",
                        VALID_PASSWORD, false, "Your username is invalid!"}
        };
    }

    @Test(
            dataProvider = "credentialCombinations",
            description = "Credential combinations produce expected outcome"
    )
    public void credentialCombinationsBehaveAsExpected(
            String scenario,
            String username,
            String password,
            boolean loginExpected,
            String expectedMessage) {

        login(username, password);

        if (loginExpected) {
            loginPage.waitForSecureArea();

            Assert.assertEquals(loginPage.getHeadingText(), "Secure Area");

            loginPage.logout();

        } else {
            Assert.assertTrue(
                    loginPage.flashMessage().contains(expectedMessage),
                    "Unexpected validation message for " + scenario);

            Assert.assertTrue(loginPage.getCurrentUrl().contains("/login"));
        }
    }

    // TEST CASE 16: INTENTIONAL FAILURE DEMONSTRATION
    @Test(description = "Demonstrates failed heading validation")
    public void demoFailedTest() {
        login(VALID_USERNAME, VALID_PASSWORD);
        loginPage.waitForSecureArea();

        // Intentional wrong expected heading to demonstrate failure
        Assert.assertEquals(
                loginPage.getHeadingText(),
                "Dashboard",
                "Intentional failure: expected Dashboard but actual heading is Secure Area"
        );
    }

    // CLOSE BROWSER AFTER EVERY TEST
    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // REUSABLE LOGIN METHOD
    private void login(String username, String password) {
        loginPage.login(username, password);
    }

    // REUSABLE SUBMIT METHOD
    private void submitLogin() {
        loginPage.submitLogin();
    }

    // REUSABLE ERROR ASSERTION
    private void assertLoginError(String expectedMessage) {
        Assert.assertTrue(flashMessage().contains(expectedMessage));
    }

    // REUSABLE FLASH MESSAGE METHOD
    private String flashMessage() {
        return loginPage.flashMessage();
    }
}
