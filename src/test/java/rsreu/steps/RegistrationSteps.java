package rsreu.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.Assert;

import java.time.Duration;
import java.util.List;

public class RegistrationSteps {
    private WebDriver driver;
    private WebDriverWait wait;

    public RegistrationSteps() {
        try {
            // Указываем путь к geckodriver.exe
            System.setProperty("webdriver.gecko.driver", "C:\\Users\\arkhi\\Desktop\\RADIK M\\geckodriver.exe");
            // Настраиваем путь к Firefox
            FirefoxOptions options = new FirefoxOptions();
            options.setBinary("C:\\Program Files\\Mozilla Firefox\\firefox.exe");
            this.driver = new FirefoxDriver(options);
            this.wait = new WebDriverWait(driver, Duration.ofSeconds(15)); // Увеличен таймаут для надёжности
        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации FirefoxDriver: " + e.getMessage());
        }
    }

    @Given("Открыта главная страница сайта")
    public void openMainPage() {
        try {
            System.out.println("Открываю главную страницу: http://localhost:8080/");
            driver.get("http://localhost:8080/");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при открытии главной страницы: " + e.getMessage());
        }
    }

    @Given("Пользователь с почтой {string} и никнеймом {string} не зарегистрирован в системе")
    public void checkUserNotRegistered(String email, String nickname) {
        System.out.println("Предполагаем, что пользователь с почтой " + email + " и никнеймом " + nickname + " не зарегистрирован");
    }

    @When("Пользователь нажимает на кнопку {string}")
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

    @When("Нажимает кнопку {string}")
    public void clickButtonAlt(String buttonText) {
        clickButton(buttonText); // Делегируем выполнение существующему методу
    }

    @When("Вводит никнейм {string}")
    public void enterNickname(String nickname) {
        try {
            System.out.println("Ввожу никнейм: " + nickname);
            var element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
            element.clear();
            element.sendKeys(nickname);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при вводе никнейма '" + nickname + "': " + e.getMessage());
        }
    }

    @When("Вводит пароль {string}")
    public void enterPassword(String password) {
        try {
            System.out.println("Ввожу пароль: " + password);
            var element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
            element.clear();
            element.sendKeys(password);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при вводе пароля: " + e.getMessage());
        }
    }

    @When("Подтверждает пароль {string}")
    public void confirmPassword(String password) {
        try {
            System.out.println("Подтверждаю пароль: " + password);
            var element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("confirm")));
            element.clear();
            element.sendKeys(password);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при подтверждении пароля: " + e.getMessage());
        }
    }

    @When("Вводит почту {string}")
    public void enterEmail(String email) {
        try {
            System.out.println("Ввожу почту: " + email);
            var element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
            element.clear();
            element.sendKeys(email);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при вводе почты '" + email + "': " + e.getMessage());
        }
    }

    @When("Заполняет адрес доставки {string}")
    public void enterAddress(String address) {
        try {
            System.out.println("Ввожу адрес: " + address);
            var element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("address")));
            element.clear();
            element.sendKeys(address);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при вводе адреса '" + address + "': " + e.getMessage());
        }
    }

    @Then("Отображается сообщение {string}")
    public void checkSuccessMessage(String message) {
        try {
            System.out.println("Проверяю сообщение: " + message);
            String actualMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("success-message"))).getText();
            Assert.assertEquals("Ожидаемое сообщение не совпадает", message, actualMessage);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке сообщения об успехе: " + e.getMessage());
        }
    }

    @Then("Отображается сообщение об ошибке {string}")
    public void checkErrorMessage(String errorMessage) {
        try {
            System.out.println("Проверяю сообщение об ошибке: " + errorMessage);
            List<WebElement> errors = driver.findElements(By.className("validationError"));
            boolean found = false;
            for (WebElement error : errors) {
                String actualMessage = error.getText();
                System.out.println("Найденное сообщение: " + actualMessage);
                if (actualMessage.equals(errorMessage)) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue("Ожидаемое сообщение об ошибке '" + errorMessage + "' не найдено", found);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке сообщения об ошибке: " + e.getMessage());
        }
    }

    @Then("Перенаправлен на страницу авторизации")
    public void checkRedirectToLogin() {
        try {
            System.out.println("Проверяю перенаправление на страницу авторизации");
            String currentUrl = driver.getCurrentUrl();
            Assert.assertTrue("URL должен содержать 'login'", currentUrl.contains("login"));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке URL авторизации: " + e.getMessage());
        }
    }

    @Then("Отображается форма авторизации")
    public void checkLoginFormDisplayed() {
        try {
            System.out.println("Проверяю отображение формы авторизации");
            boolean isLoginFormDisplayed = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm"))).isDisplayed();
            Assert.assertTrue("Форма авторизации не отображается", isLoginFormDisplayed);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке формы авторизации: " + e.getMessage());
        }
    }

    @Then("Регистрация не завершена")
    public void checkRegistrationNotCompleted() {
        try {
            String currentUrl = driver.getCurrentUrl();
            System.out.println("Проверяю URL: " + currentUrl);
            Assert.assertTrue("URL должен содержать 'register'", currentUrl.contains("register"));
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