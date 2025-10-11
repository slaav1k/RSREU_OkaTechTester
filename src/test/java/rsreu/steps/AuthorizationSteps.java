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
import rsreu.WebDriverConfigs;

import java.time.Duration;
import java.util.List;

public class AuthorizationSteps {
    private WebDriver driver;
    private WebDriverWait wait;

    public AuthorizationSteps() {
        try {
            // Указываем путь к драйверу
            System.setProperty(WebDriverConfigs.DRIVER, WebDriverConfigs.PATH_TO_DRIVER);
            // Настраиваем путь к Firefox (Или хром аналогично, только меняйте на хром)
            FirefoxOptions options = new FirefoxOptions();
            options.setBinary(WebDriverConfigs.PATH_TO_FIREFOX);
            this.driver = new FirefoxDriver(options);
            this.wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации Driver: " + e.getMessage());
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
    public void enterPasswordForLogin(String password) {
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

    @Then("Отображается ошибка {string}")
    public void checkErrorMessage(String errorMessage) {
        try {
            System.out.println("Проверяю наличие сообщения об ошибке: " + errorMessage);
            WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), '" + errorMessage + "')]")
            ));
            Assert.assertTrue("Сообщение об ошибке не отображается", errorElement.isDisplayed());
            System.out.println("Сообщение об ошибке найдено: " + errorElement.getText());
        } catch (Exception e) {
            System.out.println("HTML страницы при ошибке: " + driver.getPageSource());
            throw new RuntimeException("Ошибка при проверке сообщения об ошибке: " + e.getMessage());
        }
    }

    @Then("Авторизация не завершена")
    public void checkLoginNotCompleted() {
        try {
            System.out.println("Проверяю, что авторизация не завершена");
            String currentUrl = driver.getCurrentUrl();
            System.out.println("Текущий URL: " + currentUrl);
            Assert.assertTrue("URL должен содержать 'login' или 'login?error', текущий URL: " + currentUrl,
                    currentUrl.contains("login"));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке URL: " + e.getMessage());
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