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
import rsreu.WebDriverConfigs;

import java.time.Duration;
import java.util.List;

public class CatalogSteps {
    private WebDriver driver;
    private WebDriverWait wait;

    public CatalogSteps() {
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

    @Given("Открыта главная вебстраница сайта")
    public void openMainPage() {
        try {
            System.out.println("Открываю главную страницу: http://localhost:8080/");
            driver.get("http://localhost:8080");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при открытии главной страницы: " + e.getMessage());
        }
    }

    @When("Пользователь нажимает в каталоге на кнопку {string}")
    public void clickButton(String buttonText) {
        try {
            System.out.println("Пытаюсь нажать на кнопку: " + buttonText);
            // Ищем ссылку с текстом "Каталог" в навигации
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(text(), '" + buttonText + "')]")
            ));
            button.click();
            System.out.println("Нажал на кнопку: " + buttonText);

            // Ожидаем загрузки страницы каталога
            if (buttonText.equals("Каталог")) {
                wait.until(ExpectedConditions.or(
                        ExpectedConditions.urlContains("/catalog"),
                        ExpectedConditions.visibilityOfElementLocated(By.className("catalog-products")),
                        ExpectedConditions.presenceOfElementLocated(By.tagName("catalog"))
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при нажатии на кнопку '" + buttonText + "': " + e.getMessage());
        }
    }

    @Then("Отображается список всех доступных товаров")
    public void checkProductsListDisplayed() {
        try {
            System.out.println("Проверяю отображение списка товаров");

            // Ищем контейнер с товарами по классу из HTML
            WebElement productsContainer = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.className("catalog-products"))
            );

            // Проверяем, что контейнер отображается
            Assert.assertTrue("Контейнер товаров не отображается", productsContainer.isDisplayed());

            // Проверяем, что есть хотя бы один товар в списке
            List<WebElement> products = productsContainer.findElements(By.className("catalog-item"));
            Assert.assertFalse("Список товаров пуст", products.isEmpty());

            System.out.println("Найдено товаров: " + products.size());
            System.out.println("Список товаров успешно отображается");

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке списка товаров: " + e.getMessage());
        }
    }

//    @Then("В списке присутствуют товары из разных категорий (холодильники, микроволновые печи, телевизоры)")
//    public void checkProductsFromDifferentCategories() {
//        try {
//            System.out.println("Проверяю наличие товаров из разных категорий");
//
//            // Получаем все товары
//            List<WebElement> products = driver.findElements(By.className("catalog-item"));
//            Assert.assertFalse("Нет товаров для проверки категорий", products.isEmpty());
//
//            // Проверяем наличие товаров из реальных категорий из вашей базы данных
//            boolean hasTVs = checkCategoryExists(products, "TV", new String[]{"samsung tv", "телевизор", "tv"});
//            boolean hasFridges = checkCategoryExists(products, "FRIG", new String[]{"lg fridge", "холодильник", "fridge"});
//            boolean hasBake = checkCategoryExists(products, "Bake", new String[]{"bosch oven", "печь", "oven", "bake"});
//            boolean hasCoffeeMakers = checkCategoryExists(products, "CoffeeMakers", new String[]{"de'longhi coffee", "кофе", "coffee"});
//
//            System.out.println("Категория TV: " + hasTVs);
//            System.out.println("Категория FRIG: " + hasFridges);
//            System.out.println("Категория Bake: " + hasBake);
//            System.out.println("Категория CoffeeMakers: " + hasCoffeeMakers);
//
//            // Проверяем, что есть товары как минимум из 3 разных категорий
//            int foundCategories = 0;
//            if (hasTVs) foundCategories++;
//            if (hasFridges) foundCategories++;
//            if (hasBake) foundCategories++;
//            if (hasCoffeeMakers) foundCategories++;
//
//            Assert.assertTrue("Должны присутствовать товары как минимум из 3 разных категорий. Найдено: " + foundCategories,
//                    foundCategories >= 3);
//
//            System.out.println("Товары из разных категорий присутствуют (найдено категорий: " + foundCategories + ")");
//
//        } catch (Exception e) {
//            throw new RuntimeException("Ошибка при проверке категорий товаров: " + e.getMessage());
//        }
//    }
//
//    private boolean checkCategoryExists(List<WebElement> products, String categoryName, String[] keywords) {
//        for (WebElement product : products) {
//            if (product.isDisplayed()) {
//                String productText = product.getText().toLowerCase();
//
//                // Проверяем наличие ключевых слов в тексте товара
//                for (String keyword : keywords) {
//                    if (productText.contains(keyword.toLowerCase())) {
//                        System.out.println("Найден товар категории " + categoryName + ": " + productText);
//                        return true;
//                    }
//                }
//
//                // Дополнительно проверяем описание товара
//                try {
//                    WebElement description = product.findElement(By.xpath(".//p[contains(text(), 'Smart TV') or contains(text(), 'fridge') or contains(text(), 'oven') or contains(text(), 'coffee')]"));
//                    if (description != null && description.isDisplayed()) {
//                        System.out.println("Найден товар категории " + categoryName + " по описанию: " + description.getText());
//                        return true;
//                    }
//                } catch (Exception e) {
//                    // Игнорируем, если не нашли описание
//                }
//            }
//        }
//        return false;
//    }

//    @Then("Отображается сообщение об ошибке в каталоге {string}")
//    public void checkErrorMessage(String errorMessage) {
//        try {
//            System.out.println("Проверяю сообщение об ошибке: " + errorMessage);
//
//            // Ищем сообщение об ошибке в различных возможных элементах
//            List<WebElement> errorElements = driver.findElements(By.cssSelector(
//                    ".error, .error-message, .alert, .alert-danger, .notification, [role='alert']"
//            ));
//
//            boolean found = false;
//            for (WebElement errorElement : errorElements) {
//                if (errorElement.isDisplayed()) {
//                    String actualText = errorElement.getText().trim();
//                    System.out.println("Найденное сообщение: " + actualText);
//                    if (actualText.contains(errorMessage)) {
//                        found = true;
//                        break;
//                    }
//                }
//            }
//
//            // Если не нашли в специальных элементах, ищем по тексту страницы
//            if (!found) {
//                String pageText = driver.findElement(By.tagName("body")).getText();
//                if (pageText.contains(errorMessage)) {
//                    found = true;
//                }
//            }
//
//            Assert.assertTrue("Ожидаемое сообщение об ошибке '" + errorMessage + "' не найдено", found);
//            System.out.println("Сообщение об ошибке найдено: " + errorMessage);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Ошибка при проверке сообщения об ошибке: " + e.getMessage());
//        }
//    }

//    @Then("Список товаров не отображается")
//    public void checkProductsListNotDisplayed() {
//        try {
//            System.out.println("Проверяю, что список товаров не отображается");
//
//            // Проверяем, что контейнер товаров либо не существует, либо пуст, либо не виден
//            List<WebElement> productContainers = driver.findElements(By.className("catalog-products"));
//
//            boolean productsVisible = false;
//
//            if (!productContainers.isEmpty()) {
//                WebElement container = productContainers.get(0);
//                if (container.isDisplayed()) {
//                    // Если контейнер виден, проверяем что он пуст
//                    List<WebElement> products = container.findElements(By.className("catalog-item"));
//                    for (WebElement product : products) {
//                        if (product.isDisplayed()) {
//                            productsVisible = true;
//                            break;
//                        }
//                    }
//                }
//            }
//
//            // Дополнительная проверка - ищем отдельные товары
//            List<WebElement> individualProducts = driver.findElements(By.className("catalog-item"));
//            for (WebElement product : individualProducts) {
//                if (product.isDisplayed()) {
//                    productsVisible = true;
//                    break;
//                }
//            }
//
//            Assert.assertFalse("Товары не должны отображаться, но найдены видимые товары", productsVisible);
//            System.out.println("Список товаров не отображается - корректно");
//
//        } catch (Exception e) {
//            throw new RuntimeException("Ошибка при проверке отсутствия списка товаров: " + e.getMessage());
//        }
//    }
//
//    @Then("Страница не найдена")
//    public void checkPageNotFound() {
//        try {
//            System.out.println("Проверяю, что страница не найдена");
//
//            String currentUrl = driver.getCurrentUrl();
//            String pageTitle = driver.getTitle().toLowerCase();
//            String pageText = driver.findElement(By.tagName("body")).getText().toLowerCase();
//
//            // Проверяем различные признаки страницы "не найдено"
//            boolean isNotFound = currentUrl.contains("error") ||
//                    currentUrl.contains("404") ||
//                    pageTitle.contains("404") ||
//                    pageTitle.contains("not found") ||
//                    pageTitle.contains("ошибка") ||
//                    pageText.contains("404") ||
//                    pageText.contains("not found") ||
//                    pageText.contains("страница не найдена");
//
//            Assert.assertTrue("Страница должна иметь признаки 'не найдено'. URL: " + currentUrl + ", Title: " + pageTitle, isNotFound);
//            System.out.println("Страница имеет признаки 'не найдено' - корректно");
//
//        } catch (Exception e) {
//            throw new RuntimeException("Ошибка при проверке страницы 'не найдено': " + e.getMessage());
//        }
//    }

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