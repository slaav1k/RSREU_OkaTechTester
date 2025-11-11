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
import java.util.List;

public class CatalogGroupingSteps {
    private WebDriver driver;
    private WebDriverWait wait;

    public CatalogGroupingSteps() {
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

    @Given("Пользователь находится в каталоге товаров")
    public void userIsInCatalog() {
        try {
            System.out.println("Перехожу в каталог товаров");
            driver.get("http://localhost:8080/catalog");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("catalog-products")));
            System.out.println("Успешно перешел в каталог товаров");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при переходе в каталог товаров: " + e.getMessage());
        }
    }

    @When("Пользователь выбирает категорию {string}")
    public void selectCategory(String categoryName) {
        try {
            System.out.println("Выбираю категорию: " + categoryName);

            // Находим выпадающий список категорий
            WebElement categorySelect = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("category"))
            );

            // Создаем объект Select для работы с выпадающим списком
            Select categoryDropdown = new Select(categorySelect);

            // Выбираем категорию по видимому тексту
            categoryDropdown.selectByVisibleText(categoryName);
            System.out.println("Категория " + categoryName + " выбрана в выпадающем списке");

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
            throw new RuntimeException("Ошибка при выборе категории '" + categoryName + "': " + e.getMessage());
        }
    }

    @Then("Отображается список товаров только из категории {string}")
    public void checkProductsFromSpecificCategory(String expectedCategory) {
        try {
            System.out.println("Проверяю, что отображаются товары только из категории: " + expectedCategory);

            // Получаем все товары
            List<WebElement> products = driver.findElements(By.className("catalog-item"));
            Assert.assertFalse("Список товаров пуст", products.isEmpty());

            // Определяем ключевые слова для проверки категории
            String[] categoryKeywords = getCategoryKeywords(expectedCategory);

            // Проверяем, что все товары принадлежат указанной категории
            for (WebElement product : products) {
                if (product.isDisplayed()) {
                    String productText = product.getText().toLowerCase();
                    boolean belongsToCategory = false;

                    for (String keyword : categoryKeywords) {
                        if (productText.contains(keyword.toLowerCase())) {
                            belongsToCategory = true;
                            break;
                        }
                    }

                    Assert.assertTrue(
                            "Товар не принадлежит категории " + expectedCategory + ": " + product.getText(),
                            belongsToCategory
                    );
                }
            }

            System.out.println("Все товары принадлежат категории: " + expectedCategory);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке категории товаров: " + e.getMessage());
        }
    }

    private String[] getCategoryKeywords(String categoryName) {
        switch (categoryName) {
            case "TV":
                return new String[]{"samsung tv", "телевизор", "tv", "smart tv"};
            case "FRIG":
                return new String[]{"lg fridge", "холодильник", "fridge", "refrigerator"};
            case "Bake":
                return new String[]{"bosch oven", "печь", "oven", "bake"};
            case "CoffeeMakers":
                return new String[]{"de'longhi coffee", "кофе", "coffee", "coffee maker", "coffee machine"};
            default:
                return new String[]{categoryName.toLowerCase()};
        }
    }

    @Then("Товары из других категорий не отображаются")
    public void checkOtherCategoriesNotDisplayed() {
        try {
            System.out.println("Проверяю, что товары из других категорий не отображаются");

            // Получаем все товары
            List<WebElement> products = driver.findElements(By.className("catalog-item"));

            // Определяем, какая категория сейчас выбрана
            WebElement categorySelect = driver.findElement(By.id("category"));
            Select categoryDropdown = new Select(categorySelect);
            String selectedCategory = categoryDropdown.getFirstSelectedOption().getText();

            System.out.println("Выбранная категория: " + selectedCategory);

            if (!selectedCategory.isEmpty()) {
                // Получаем ключевые слова для исключения (текущей категории)
                String[] currentCategoryKeywords = getCategoryKeywords(selectedCategory);

                // Определяем ключевые слова для других категорий
                String[][] otherCategoriesKeywords = {
                        getCategoryKeywords("TV"),
                        getCategoryKeywords("FRIG"),
                        getCategoryKeywords("Bake"),
                        getCategoryKeywords("CoffeeMakers")
                };

                // Проверяем, что нет товаров из других категорий
                for (WebElement product : products) {
                    if (product.isDisplayed()) {
                        String productText = product.getText().toLowerCase();

                        // Проверяем, что товар не содержит ключевых слов других категорий
                        for (String[] otherCategoryKeywords : otherCategoriesKeywords) {
                            // Пропускаем текущую категорию
                            if (arraysEqual(currentCategoryKeywords, otherCategoryKeywords)) {
                                continue;
                            }

                            for (String keyword : otherCategoryKeywords) {
                                if (productText.contains(keyword.toLowerCase())) {
                                    Assert.fail("Найден товар из другой категории: " + product.getText() +
                                            ". Ожидалась только категория: " + selectedCategory);
                                }
                            }
                        }
                    }
                }
            }

            System.out.println("Товары из других категорий не отображаются - корректно");

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке отсутствия товаров из других категорий: " + e.getMessage());
        }
    }

    private boolean arraysEqual(String[] arr1, String[] arr2) {
        if (arr1.length != arr2.length) return false;
        for (int i = 0; i < arr1.length; i++) {
            if (!arr1[i].equals(arr2[i])) return false;
        }
        return true;
    }

    @Then("Отображается сообщение об ошибке при группировке {string}")
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

    @Then("Список товаров при группировке не отображается")
    public void checkProductsListNotDisplayed() {
        try {
            System.out.println("Проверяю, что список товаров не отображается");

            List<WebElement> productContainers = driver.findElements(By.className("catalog-products"));
            boolean productsVisible = false;

            if (!productContainers.isEmpty()) {
                WebElement container = productContainers.get(0);
                if (container.isDisplayed()) {
                    List<WebElement> products = container.findElements(By.className("catalog-item"));
                    for (WebElement product : products) {
                        if (product.isDisplayed()) {
                            productsVisible = true;
                            break;
                        }
                    }
                }
            }

            // Дополнительная проверка - ищем отдельные товары
            List<WebElement> individualProducts = driver.findElements(By.className("catalog-item"));
            for (WebElement product : individualProducts) {
                if (product.isDisplayed()) {
                    productsVisible = true;
                    break;
                }
            }

            Assert.assertFalse("Товары не должны отображаться, но найдены видимые товары", productsVisible);
            System.out.println("Список товаров не отображается - корректно");

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке отсутствия списка товаров: " + e.getMessage());
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