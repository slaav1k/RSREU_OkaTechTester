package rsreu.steps;

import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.Assert;

import java.time.Duration;

public class AuthorizationSteps {
    private WebDriver driver;
    private WebDriverWait wait;

    public AuthorizationSteps() {
        try {
            // Указываем путь к geckodriver.exe
            System.setProperty("webdriver.gecko.driver", "C:\\Users\\arkhi\\Desktop\\RADIK M\\geckodriver.exe");
            // Настраиваем путь к Firefox
            FirefoxOptions options = new FirefoxOptions();
            options.setBinary("C:\\Program Files\\Mozilla Firefox\\firefox.exe");
            this.driver = new FirefoxDriver(options);
            this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации FirefoxDriver: " + e.getMessage());
        }
    }

    @Given("Открыта страница авторизации")
    public void openLoginPage() {
        try {
            System.out.println("Открываю страницу авторизации: http://localhost:8080/login");
            driver.get("http://localhost:8080/login");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm")));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при открытии страницы авторизации: " + e.getMessage());
        }
    }

    @When("Вводит никнейм {string} для авторизации")
    public void enterNicknameForLogin(String nickname) {
        try {
            System.out.println("Ввожу никнейм: " + nickname);
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
            element.clear();
            element.sendKeys(nickname);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при вводе никнейма '" + nickname + "': " + e.getMessage());
        }
    }

    @And("Вводит пароль {string} для авторизации")
    public void вводитПарольДляАвторизации(String password) {
        try {
            System.out.println("Ввожу пароль: " + password);
            var element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
            element.clear();
            element.sendKeys(password);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при вводе пароля: " + e.getMessage());
        }
    }

    @When("Пользователь кликает на кнопку {string}")
    public void clickButton(String buttonText) {
        try {
            System.out.println("Пытаюсь нажать на элемент с текстом: " + buttonText);
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), '" + buttonText + "')] | //button[contains(text(), '" + buttonText + "')]"))).click();
            System.out.println("Нажал на элемент: " + buttonText);
            if (buttonText.equals("Вход")) {
                wait.until(ExpectedConditions.urlContains("login")); // Ожидание перехода на страницу логина
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при нажатии на элемент '" + buttonText + "': " + e.getMessage());
        }
    }

    @Then("Пользователь перенаправлен на главную страницу")
    public void checkRedirectToMainPage() {
        try {
            System.out.println("Проверяю перенаправление на главную страницу");
            String currentUrl = driver.getCurrentUrl();
            Assert.assertTrue("URL должен быть главной страницей", currentUrl.equals("http://localhost:8080/"));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке перенаправления на главную страницу: " + e.getMessage());
        }
    }

    @Then("Авторизация не завершена")
    public void checkLoginNotCompleted() {
        try {
            System.out.println("Проверяю, что авторизация не завершена");
            String currentUrl = driver.getCurrentUrl();
            Assert.assertTrue("URL должен содержать 'login'", currentUrl.contains("login"));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке URL авторизации: " + e.getMessage());
        }
    }

    @io.cucumber.java.After
    public void tearDown(io.cucumber.java.Scenario scenario) {
        if (scenario.isFailed()) {
            try {
                byte[] screenshot = ((org.openqa.selenium.TakesScreenshot) driver).getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "screenshot");
            } catch (Exception e) {
                System.out.println("Ошибка при создании скриншота: " + e.getMessage());
            }
        }
        if (driver != null) {
            driver.quit();
        }
    }


}