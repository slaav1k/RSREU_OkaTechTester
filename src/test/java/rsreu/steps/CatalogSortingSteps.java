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
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.Assert;
import rsreu.WebDriverConfigs;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class CatalogSortingSteps {
    private WebDriver driver;
    private WebDriverWait wait;
    private List<Double> originalPrices;

    public CatalogSortingSteps() {
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

    @Given("Пользователь находится в каталоге товаров перед сортировкой")
    public void userIsInCatalog() {
        try {
            System.out.println("Перехожу в каталог товаров");
            driver.get("http://localhost:8080/catalog");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("catalog-products")));

            // Сохраняем оригинальные цены для проверки в сценарии fail
            originalPrices = getProductPrices();
            System.out.println("Успешно перешел в каталог товаров. Найдено товаров: " + originalPrices.size());
            System.out.println("Оригинальные цены: " + originalPrices);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при переходе в каталог товаров: " + e.getMessage());
        }
    }

    @When("Пользователь выбирает сортировку {string}")
    public void selectSorting(String sortingType) {
        try {
            System.out.println("Выбираю сортировку: " + sortingType);

            // Находим выпадающий список сортировки
            WebElement sortSelect = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("sort"))
            );

            // Создаем объект Select для работы с выпадающим списком
            Select sortDropdown = new Select(sortSelect);

            // Выбираем сортировку в зависимости от текста
            if (sortingType.equals("По возрастанию цены")) {
                sortDropdown.selectByValue("asc");
                System.out.println("Выбрана сортировка по возрастанию (asc)");
            } else if (sortingType.equals("По убыванию цены")) {
                sortDropdown.selectByValue("desc");
                System.out.println("Выбрана сортировка по убыванию (desc)");
            } else {
                sortDropdown.selectByVisibleText(sortingType);
                System.out.println("Выбрана сортировка: " + sortingType);
            }

            // Нажимаем кнопку "Применить"
            WebElement applyButton = driver.findElement(By.xpath("//button[contains(text(), 'Применить')]"));
            applyButton.click();
            System.out.println("Нажата кнопка 'Применить'");

            // Ожидаем обновления списка товаров
            wait.until(ExpectedConditions.stalenessOf(
                    driver.findElement(By.className("catalog-products"))
            ));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("catalog-products")));

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при выборе сортировки '" + sortingType + "': " + e.getMessage());
        }
    }

    @Then("Список товаров отображается в порядке от дешевых к дорогим")
    public void checkProductsSortedAscending() {
        try {
            System.out.println("Проверяю сортировку по возрастанию цены");

            // Получаем цены после сортировки
            List<Double> sortedPrices = getProductPrices();
            Assert.assertFalse("Список товаров пуст", sortedPrices.isEmpty());

            // Проверяем, что цены отсортированы по возрастанию
            for (int i = 0; i < sortedPrices.size() - 1; i++) {
                Assert.assertTrue(
                        "Цены не отсортированы по возрастанию. " + sortedPrices.get(i) + " > " + sortedPrices.get(i + 1),
                        sortedPrices.get(i) <= sortedPrices.get(i + 1)
                );
            }

            System.out.println("Товары отсортированы по возрастанию цены: " + sortedPrices);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке сортировки по возрастанию: " + e.getMessage());
        }
    }

    @Then("Список товаров отображается в порядке от дорогих к дешевым")
    public void checkProductsSortedDescending() {
        try {
            System.out.println("Проверяю сортировку по убыванию цены");

            // Получаем цены после сортировки
            List<Double> sortedPrices = getProductPrices();
            Assert.assertFalse("Список товаров пуст", sortedPrices.isEmpty());

            // Проверяем, что цены отсортированы по убыванию
            for (int i = 0; i < sortedPrices.size() - 1; i++) {
                Assert.assertTrue(
                        "Цены не отсортированы по убыванию. " + sortedPrices.get(i) + " < " + sortedPrices.get(i + 1),
                        sortedPrices.get(i) >= sortedPrices.get(i + 1)
                );
            }

            System.out.println("Товары отсортированы по убыванию цены: " + sortedPrices);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке сортировки по убыванию: " + e.getMessage());
        }
    }

    @Then("Отображается сообщение об ошибке при сортировке {string}")
    public void checkErrorMessage(String errorMessage) {
        try {
            System.out.println("Проверяю сообщение об ошибке: " + errorMessage);

            // Ищем сообщение об ошибке в различных возможных элементах
            List<WebElement> errorElements = driver.findElements(By.cssSelector(
                    ".error, .error-message, .alert, .alert-danger, .notification, [role='alert']"
            ));

            boolean found = false;
            for (WebElement errorElement : errorElements) {
                if (errorElement.isDisplayed()) {
                    String actualText = errorElement.getText().trim();
                    System.out.println("Найденное сообщение: " + actualText);
                    if (actualText.contains(errorMessage)) {
                        found = true;
                        break;
                    }
                }
            }

            // Если не нашли в специальных элементах, ищем по тексту страницы
            if (!found) {
                String pageText = driver.findElement(By.tagName("body")).getText();
                if (pageText.contains(errorMessage)) {
                    found = true;
                }
            }

            Assert.assertTrue("Ожидаемое сообщение об ошибке '" + errorMessage + "' не найдено", found);
            System.out.println("Сообщение об ошибке найдено: " + errorMessage);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке сообщения об ошибке: " + e.getMessage());
        }
    }

    @Then("Список товаров не изменяется")
    public void checkProductsListNotChanged() {
        try {
            System.out.println("Проверяю, что список товаров не изменился");

            // Получаем текущие цены
            List<Double> currentPrices = getProductPrices();

            // Проверяем, что количество товаров не изменилось
            Assert.assertEquals(
                    "Количество товаров изменилось",
                    originalPrices.size(),
                    currentPrices.size()
            );

            // Проверяем, что порядок товаров остался прежним
            // (в реальном приложении здесь нужно сравнивать не только цены, но и порядок товаров)
            boolean orderChanged = false;
            for (int i = 0; i < originalPrices.size(); i++) {
                if (!originalPrices.get(i).equals(currentPrices.get(i))) {
                    orderChanged = true;
                    break;
                }
            }

            Assert.assertFalse("Порядок товаров изменился после неудачной сортировки", orderChanged);
            System.out.println("Список товаров не изменился - корректно");

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке неизменности списка товаров: " + e.getMessage());
        }
    }

    private List<Double> getProductPrices() {
        List<Double> prices = new ArrayList<>();
        List<WebElement> products = driver.findElements(By.className("catalog-item"));

        for (WebElement product : products) {
            if (product.isDisplayed()) {
                try {
                    // Ищем элемент с ценой - предполагаем, что цена находится в последнем <p> перед формой
                    List<WebElement> paragraphs = product.findElements(By.tagName("p"));
                    if (paragraphs.size() >= 3) {
                        String priceText = paragraphs.get(2).getText(); // Третий <p> содержит цену
                        double price = Double.parseDouble(priceText);
                        prices.add(price);
                        System.out.println("Найдена цена товара: " + price);
                    }
                } catch (Exception e) {
                    System.out.println("Не удалось извлечь цену товара: " + e.getMessage());
                }
            }
        }

        return prices;
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