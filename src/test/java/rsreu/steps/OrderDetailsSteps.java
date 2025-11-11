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

public class OrderDetailsSteps {
    private WebDriver driver;
    private WebDriverWait wait;

    public OrderDetailsSteps() {
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

    @When("Пользователь завершил процесс оформления заказа на товар")
    public void userCompletedOrderProcess() {
        try {
            System.out.println("Запуск процесса оформления заказа");

            // Шаг 1: Авторизация (если еще не выполнена)
            try {
                // Проверяем, не авторизованы ли мы уже
                driver.get("http://localhost:8080/");
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));

                // Если на главной есть кнопка выхода или профиль - значит авторизованы
                try {
                    WebElement logoutButton = driver.findElement(By.xpath("//a[contains(text(), 'Выйти')]"));
                    System.out.println("Пользователь уже авторизован");
                } catch (Exception e) {
                    // Если не авторизованы - выполняем авторизацию
                    System.out.println("Выполнение авторизации пользователя");
                    driver.get("http://localhost:8080/login");

                    WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
                    WebElement passwordField = driver.findElement(By.id("password"));
                    WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[contains(text(), 'Войти') or contains(text(), 'Вход') or contains(text(), 'Login')]")));

                    usernameField.clear();
                    usernameField.sendKeys("Ivanov");
                    passwordField.clear();
                    passwordField.sendKeys("Qw123");
                    loginButton.click();

                    // Ожидаем завершения авторизации
                    wait.until(ExpectedConditions.or(
                            ExpectedConditions.urlToBe("http://localhost:8080/"),
                            ExpectedConditions.urlContains("catalog"),
                            ExpectedConditions.urlContains("main")
                    ));
                }
            } catch (Exception e) {
                throw new RuntimeException("Ошибка при авторизации: " + e.getMessage());
            }

            // Шаг 2: Открываем каталог товаров
            System.out.println("Открытие каталога товаров");
            driver.get("http://localhost:8080/catalog");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));

            // Шаг 3: Нажимаем кнопку заказа для товара
            System.out.println("Нажатие кнопки 'Заказать' для товара");
            WebElement orderButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("/html/body/catalog/ul/li[1]/form/button[contains(text(), 'Заказать')]")));
            orderButton.click();

            // Шаг 4: Проверяем, что открыта форма заказа и заполняем её
            System.out.println("Проверка открытия формы заказа");
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("/html/body/main/form/div[5]/button[contains(text(), 'Оформить заказ')]")));

            // Заполняем обязательные поля
            fillField("customerName", "Иванов Иван Иванович");
            fillField("email", "test@example.com");
            fillField("address", "Рязань, ул. Гагарина, д. 4");
            fillField("quantity", "1");

            // Шаг 5: Нажимаем кнопку оформления заказа
            System.out.println("Нажатие кнопки 'Оформить заказ'");
            WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("/html/body/main/form/div[5]/button")));

            By imageLocator = By.xpath("/html/body/main/form/div[5]/button");
            scrollToElement(imageLocator);

            submitButton.click();

            // Шаг 6: Проверяем, что заказ успешно создан и мы на странице подтверждения
            System.out.println("Проверка успешного создания заказа");
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("order"),
                    ExpectedConditions.urlContains("confirmation"),
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//*[contains(text(), 'Ваш заказ оформлен') or contains(text(), 'Order confirmed')]"))
            ));

            System.out.println("Процесс оформления заказа успешно завершен");

        } catch (Exception e) {
            System.out.println("Ошибка при оформлении заказа: " + e.getMessage());
            System.out.println("Текущий URL: " + driver.getCurrentUrl());
            throw new RuntimeException("Процесс оформления заказа не завершен: " + e.getMessage());
        }
    }

    // Вспомогательный метод для заполнения полей (добавьте в класс если его нет)
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

    @Then("Система перенаправляет пользователя на страницу с детализацией заказа")
    public void systemRedirectsToOrderDetails() {
        try {
            System.out.println("Проверка перенаправления на страницу детализации заказа");
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlToBe("http://localhost:8080/orders/confirm"),
                    ExpectedConditions.urlContains("orders/confirm")
            ));
            Assert.assertTrue("URL должен содержать информацию о заказе",
                    Objects.requireNonNull(driver.getCurrentUrl()).contains("confirm"));
        } catch (Exception e) {
            throw new RuntimeException("Перенаправление на страницу детализации не выполнено: " + e.getMessage());
        }
    }

    @Then("Отображается статусное сообщение: {string}")
    public void verifyStatusMessage(String expectedMessage) {
        try {
            System.out.println("Проверка статусного сообщения: " + expectedMessage);
            WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(), '" + expectedMessage + "')]")));
            Assert.assertTrue("Статусное сообщение должно отображаться", message.isDisplayed());
        } catch (Exception e) {
            throw new RuntimeException("Статусное сообщение не отображается: " + e.getMessage());
        }
    }

    @Then("В блоке деталей заказа присутствует название товара")
    public void verifyProductNameInOrderDetails() {
        try {
            System.out.println("Проверка отображения названия товара в деталях заказа");
            WebElement productName = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("/html/body/main/ul/li[1]/span[contains(text(), 'Samsung TV')]")));
            Assert.assertTrue("Название товара должно отображаться в блоке деталей заказа", productName.isDisplayed());
            Assert.assertFalse("Название товара не должно быть пустым",
                    productName.getText().trim().isEmpty());
        } catch (Exception e) {
            throw new RuntimeException("Название товара не отображается в деталях заказа: " + e.getMessage());
        }
    }

    @Then("Указана цена за единицу товара")
    public void verifyProductPrice() {
        try {
            System.out.println("Проверка отображения цены товара");
            WebElement price = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("/html/body/main/ul/li[2]/span[contains(text(), '500.0')]")));
            Assert.assertTrue("Цена товара должна отображаться", price.isDisplayed());
        } catch (Exception e) {
            throw new RuntimeException("Цена товара не отображается: " + e.getMessage());
        }
    }

    @Then("Указано количество выбранного товара")
    public void verifyProductQuantity() {
        try {
            System.out.println("Проверка отображения количества товара");
            WebElement quantity = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("/html/body/main/ul/li[3]/span[contains(text(), '1')]")));
            Assert.assertTrue("Количество товара должно отображаться", quantity.isDisplayed());
        } catch (Exception e) {
            throw new RuntimeException("Количество товара не отображается: " + e.getMessage());
        }
    }

    @Then("Показывается итоговая стоимость заказа")
    public void verifyTotalOrderCost() {
        try {
            System.out.println("Проверка отображения итоговой стоимости заказа");
            WebElement totalCost = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("/html/body/main/ul/li[4]/span[contains(text(), '500.0')]")));
            Assert.assertTrue("Итоговая стоимость заказа должна отображаться", totalCost.isDisplayed());
        } catch (Exception e) {
            throw new RuntimeException("Итоговая стоимость заказа не отображается: " + e.getMessage());
        }
    }

    @Then("Отображаются ФИО пользователя, связанные с заказом")
    public void verifyUserFullName() {
        try {
            System.out.println("Проверка отображения ФИО пользователя");
            WebElement fullName = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("/html/body/main/ul/li[5]/span[contains(text(), 'Иванов Иван Иванович')]")));
            Assert.assertTrue("ФИО пользователя должно отображаться", fullName.isDisplayed());
        } catch (Exception e) {
            throw new RuntimeException("ФИО пользователя не отображается: " + e.getMessage());
        }
    }

    @Then("Присутствует проверенный адрес доставки")
    public void verifyDeliveryAddress() {
        try {
            System.out.println("Проверка отображения адреса доставки");
            WebElement address = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("/html/body/main/ul/li[6]/span[contains(text(), 'Рязань, ул. Гагарина, д. 4')]")));
            Assert.assertTrue("Адрес доставки должен отображаться", address.isDisplayed());
        } catch (Exception e) {
            throw new RuntimeException("Адрес доставки не отображается: " + e.getMessage());
        }
    }

    @Then("Загружено изображение товара из карточки заказа")
    public void verifyProductImage() {
        try {
            System.out.println("Проверка отображения изображения товара");
            WebElement image = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("/html/body/main/p[1]/img[contains(@alt, 'Изображение товара')]")));
            Assert.assertTrue("Изображение товара должно отображаться", image.isDisplayed());
            Assert.assertTrue("Изображение товара должно быть загружено",
                    image.getAttribute("src") != null && !image.getAttribute("src").isEmpty());
        } catch (Exception e) {
            throw new RuntimeException("Изображение товара не отображается: " + e.getMessage());
        }
    }

    @Given("В системе отсутствует авторизованный пользователь")
    public void ensureNoAuthenticatedUser() {
        try {
            System.out.println("Проверка отсутствия авторизованного пользователя");

            // Проверяем, не авторизован ли пользователь
            try {
                WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("/html/body/nav/div[2]/span/form/button[contains(text(), 'Выход') or contains(text(), 'Logout')]")));
                // Если кнопка выхода найдена, выходим из системы
                logoutButton.click();
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("/html/body/nav/div[2]/span/form/button[contains(text(), 'Вход') or contains(text(), 'Logout')]")));
            } catch (TimeoutException e) {
                // Пользователь не авторизован - это нормально
                System.out.println("Пользователь не авторизован (ожидаемое состояние)");
            }

        } catch (Exception e) {
            throw new RuntimeException("Не удалось обеспечить состояние неавторизованного пользователя: " + e.getMessage());
        }
    }

    @When("Выполнен переход по URL страницы детализации заказа")
    public void navigateToOrderDetailsPage() {
        try {
            System.out.println("Переход по URL страницы детализации заказа");
            driver.get("http://localhost:8080/order/details");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при переходе на страницу детализации заказа: " + e.getMessage());
        }
    }

    @Then("Система перенаправляет на страницу входа в аккаунт")
    public void verifyRedirectToLoginPage() {
        try {
            System.out.println("Проверка перенаправления на страницу входа");
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlToBe("http://localhost:8080/login"),
                    ExpectedConditions.urlContains("login")
            ));
            Assert.assertTrue("Должен быть выполнен redirect на страницу авторизации",
                    driver.getCurrentUrl().contains("login"));
        } catch (Exception e) {
            throw new RuntimeException("Перенаправление на страницу входа не выполнено: " + e.getMessage());
        }
    }

    @Then("Содержимое страницы заказа скрыто")
    public void verifyOrderDetailsHidden() {
        try {
            System.out.println("Проверка скрытия содержимого страницы заказа");

            // Проверяем, что элементы деталей заказа отсутствуют
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofMillis(1000));

            try {
                WebElement orderDetails = shortWait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[contains(@class, 'order-details') or contains(text(), 'Ваш заказ')]")));
                // Если элемент найден и видим - это ошибка
                if (orderDetails.isDisplayed()) {
                    throw new RuntimeException("Содержимое заказа не должно отображаться для неавторизованного пользователя");
                }
            } catch (TimeoutException e) {
                // Элемент не найден - это ожидаемо и правильно
                System.out.println("Содержимое заказа скрыто (ожидаемое поведение)");
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке скрытия содержимого заказа: " + e.getMessage());
        }
    }

    @Then("Сообщение о необходимости авторизации отображается в интерфейсе")
    public void verifyAuthorizationMessageDisplayed() {
        try {
            System.out.println("Проверка отображения сообщения о необходимости авторизации");
            WebElement authMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("/html/body/main/form/div[3]/button[contains(text(), 'Войти')]")));
            Assert.assertTrue("Сообщение о необходимости авторизации должно отображаться", authMessage.isDisplayed());
        } catch (Exception e) {
            throw new RuntimeException("Сообщение о необходимости авторизации не отображается: " + e.getMessage());
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