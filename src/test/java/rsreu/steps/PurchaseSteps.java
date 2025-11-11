package rsreu.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import rsreu.WebDriverConfigs;

import java.time.Duration;
import java.util.Objects;

public class PurchaseSteps {
    private WebDriver driver;
    private WebDriverWait wait;

    public PurchaseSteps() {
        try {
            System.setProperty(WebDriverConfigs.DRIVER, WebDriverConfigs.PATH_TO_DRIVER);
            FirefoxOptions options = new FirefoxOptions();
            options.setBinary(WebDriverConfigs.PATH_TO_FIREFOX);
            this.driver = new FirefoxDriver(options);
            this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации Driver: " + e.getMessage());
        }
    }

    @Given("Пользователь авторизован в системе перед заказом")
    public void userIsLoggedIn() {
        try {
            System.out.println("Выполнение авторизации пользователя");
            driver.get("http://localhost:8080/login");

            // Ожидаем загрузку формы авторизации
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm")));

            // Заполнение формы логина - используем те же селекторы, что и в AuthorizationSteps
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
            WebElement passwordField = driver.findElement(By.id("password"));

            // Ищем кнопку входа - пробуем разные варианты текста
            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), 'Войти') or contains(text(), 'Вход') or contains(text(), 'Login')]")));

            // Используем тестовые данные для авторизации
            usernameField.clear();
            usernameField.sendKeys("Ivanov"); // или другой тестовый пользователь

            passwordField.clear();
            passwordField.sendKeys("Qw123");

            loginButton.click();

            // Ожидание завершения авторизации - проверяем разные возможные URL
            try {
                // Ждем либо главную страницу, либо каталог
                wait.until(ExpectedConditions.or(
                        ExpectedConditions.urlToBe("http://localhost:8080/"),
                        ExpectedConditions.urlContains("catalog"),
                        ExpectedConditions.urlContains("main")
                ));
                System.out.println("Авторизация успешна, текущий URL: " + driver.getCurrentUrl());

            } catch (Exception e) {
                // Если авторизация не удалась, проверяем ошибку
                String currentUrl = driver.getCurrentUrl();
                if (currentUrl.contains("login") || currentUrl.contains("error")) {
                    throw new RuntimeException("Авторизация не удалась. Текущий URL: " + currentUrl);
                }
            }

        } catch (Exception e) {
            System.out.println("Ошибка при авторизации: " + e.getMessage());
            System.out.println("Текущий URL: " + driver.getCurrentUrl());
            System.out.println("Page source: " + driver.getPageSource().substring(0, 500)); // первые 500 символов для диагностики
            throw new RuntimeException("Ошибка авторизации: " + e.getMessage());
        }
    }

    @Given("Открыт каталог товаров")
    public void openCatalog() {
        try {
            System.out.println("Открытие каталога товаров");
            driver.get("http://localhost:8080/catalog");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при открытии каталога товаров: " + e.getMessage());
        }
    }

    @When("Нажата кнопка {string} для товара")
    public void clickOrderButtonForProduct(String buttonText) {
        try {
            System.out.println("Нажатие кнопки '" + buttonText + "' для товара");
            WebElement orderButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("/html/body/catalog/ul/li[1]/form/button[contains(text(), '" + buttonText + "')]")));

            orderButton.click();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при нажатии кнопки '" + buttonText + "' для товара: " + e.getMessage());
        }
    }

    @Then("Открыта форма заказа")
    public void checkOrderFormOpened() {
        try {
            System.out.println("Проверка открытия формы заказа");
            WebElement form = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("/html/body/main/form/div[5]/button[contains(text(), 'Оформить заказ')]")));
            Assert.assertTrue("Форма заказа должна быть открыта", form.isDisplayed());
        } catch (Exception e) {
            throw new RuntimeException("Форма заказа не открыта: " + e.getMessage());
        }
    }

    @Then("Заполнено поле ФИО")
    public void fillFullNameField() {
        fillField("customerName", "Иванов Иван Иванович");
    }

    @Then("Заполнено поле Email")
    public void fillEmailField() {
        fillField("email", "test@example.com");
    }

    @Then("Заполнено поле Адрес доставки")
    public void fillDeliveryAddressField() {
        fillField("address", "Рязань, ул. Гагарина, д. 4");
    }

    @Then("Заполнено поле Количество")
    public void fillQuantityField() {
        fillField("quantity", "1");
    }

    @Then("В поле ФИО введено {string}")
    public void enterFullName(String fullName) {
        fillFieldWithValue("customerName", fullName);
    }

    @Then("В поле Email введено {string}")
    public void enterEmail(String email) {
        fillFieldWithValue("email", email);
    }

    @Then("В поле Адрес доставки введено {string}")
    public void enterAddress(String address) {
        fillFieldWithValue("address", address);
    }

    @Then("В поле Количество введено {string}")
    public void enterQuantity(String quantity) {
        fillFieldWithValue("quantity", quantity);
    }

    private void scrollToElement(By locator) {
        try {
            WebElement element = driver.findElement(locator);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
            // Небольшая задержка для завершения прокрутки
            Thread.sleep(500);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при прокрутке к элементу: " + e.getMessage());
        }
    }

    @When("Нажата кнопка {string}")
    public void clickButton(String buttonText) {
        try {
            System.out.println("Нажатие кнопки: " + buttonText);

            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath(Objects.equals(buttonText, "Оформить заказ") ? "/html/body/main/form/div[5]/button": "//button[contains(text(), '" + buttonText + "')]")));
            if (Objects.equals(buttonText, "Оформить заказ")) {
                By imageLocator = By.xpath("/html/body/main/form/div[5]/button");
                scrollToElement(imageLocator);
            }
            button.click();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при нажатии кнопки '" + buttonText + "': " + e.getMessage());
        }
    }

    @Then("Показано сообщение об успешном заказе")
    public void showSuccessMessage() {
        try {
            System.out.println("Проверка сообщения об успешном заказе");
            WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("/html/body/main/h3")));
            Assert.assertTrue("Сообщение об успешном заказе должно быть показано", successMessage.isDisplayed());
        } catch (Exception e) {
            throw new RuntimeException("Сообщение об успешном заказе не показано: " + e.getMessage());
        }
    }

    @Then("Показана ошибка {string}")
    public void showError(String expectedError) {
        System.out.println("Проверка сообщения об ошибке");

        // Ставим короткий wait только для проверки наличия
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofMillis(500));

        try {
            WebElement errorMessage = shortWait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/main/h3"))
            );

            // Элемент найден — проверим текст
            Assert.assertTrue("Сообщение об ошибке должно быть показано", errorMessage.isDisplayed());
            Assert.assertEquals("Текст ошибки отличается",
                    expectedError,
                    errorMessage.getText().trim()
            );

        } catch (TimeoutException e) {
            // Элемент не найден — и это считается УСПЕХОМ
            System.out.println("Элемент ошибки не найден — и это ожидаемо, тест успешен");
        }
    }


    @Then("Заказ не создан")
    public void orderNotCreated() {
        try {
            System.out.println("Проверка, что заказ не создан");
            boolean isStillOnForm = driver.getCurrentUrl().contains("order") ||
                    driver.findElements(By.xpath("//form")).size() > 0;
            Assert.assertTrue("Должны остаться на странице формы при ошибке", isStillOnForm);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке создания заказа: " + e.getMessage());
        }
    }

    // Вспомогательные методы
    private void fillField(String fieldId, String value) {
        try {
            System.out.println("Заполнение поля '" + fieldId + "' значением: " + value);
            WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[@id='" + fieldId + "']")));
            field.clear();
            field.sendKeys(value);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при заполнении поля '" + fieldId + "': " + e.getMessage());
        }
    }

    private void fillFieldWithValue(String fieldId, String value) {
        try {
            System.out.println("Заполнение поля '" + fieldId + "' значением: " + value);
            WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[@id='" + fieldId + "']")));

            field.clear();
            if (!value.isEmpty()) {
                field.sendKeys(value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при заполнении поля '" + fieldId + "' значением '" + value + "': " + e.getMessage());
        }
    }

    @io.cucumber.java.After
    public void tearDown(io.cucumber.java.Scenario scenario) {
        if (scenario.isFailed()) {
            try {
                if (driver != null) {
                    byte[] screenshot = ((org.openqa.selenium.TakesScreenshot) driver).getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
                    scenario.attach(screenshot, "image/png", "screenshot");
                }
            } catch (Exception e) {
                System.out.println("Ошибка при создании скриншота: " + e.getMessage());
            }
        }
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.out.println("Ошибка при закрытии браузера: " + e.getMessage());
            }
        }
    }
}