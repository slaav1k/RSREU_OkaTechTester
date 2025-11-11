package rsreu.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import rsreu.WebDriverConfigs;

import java.time.Duration;
import java.util.List;

public class PurchaseSteps {
    private WebDriver driver;
    private WebDriverWait wait;

    public PurchaseSteps() {
        try {
            // Указываем путь к драйверу
            System.setProperty(WebDriverConfigs.DRIVER, WebDriverConfigs.PATH_TO_DRIVER);
            // Настраиваем путь к Firefox (Или хром аналогично, только меняйте на хром)
            FirefoxOptions options = new FirefoxOptions();
            options.setBinary(WebDriverConfigs.PATH_TO_FIREFOX);
            this.driver = new FirefoxDriver(options);
            this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации Driver: " + e.getMessage());
        }
    }

    @When("Пользователь переходит в раздел {string}")
    public void clickButton(String sectionName) {
        try {
            System.out.println("Пытаюсь перейти в раздел: " + sectionName);
            // Ожидаем видимости <nav>
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("nav")));
            // Выводим все ссылки на странице для отладки
            List<WebElement> allLinks = driver.findElements(By.tagName("a"));
            System.out.println("Найдено ссылок на странице: " + allLinks.size());
            for (WebElement link : allLinks) {
                System.out.println("Ссылка: " + link.getText() + " | URL: " + link.getAttribute("href"));
            }
            // Выводим все ссылки в <nav>
            List<WebElement> navLinks = driver.findElements(By.xpath("//nav//a"));
            System.out.println("Найдено ссылок в <nav>: " + navLinks.size());
            for (WebElement link : navLinks) {
                System.out.println("Ссылка в <nav>: " + link.getText() + " | URL: " + link.getAttribute("href"));
            }
            // Пробуем найти ссылку "Контакты" альтернативными способами
            WebElement link = null;
            try {
                // Способ 1: Поиск по точному тексту с normalize-space
                System.out.println("Попытка 1: Поиск по точному тексту");
                link = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[normalize-space(text())='" + sectionName + "']")));
            } catch (Exception e1) {
                System.out.println("Способ 1 не сработал: " + e1.getMessage());
                try {
                    // Способ 2: Поиск по частичному тексту (игнорируя регистр)
                    System.out.println("Попытка 2: Поиск по частичному тексту");
                    link = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(translate(text(), 'АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ', 'абвгдеёжзийклмнопрстуфхцчшщъыьэюя'), 'контакты')]")));
                } catch (Exception e2) {
                    System.out.println("Способ 2 не сработал: " + e2.getMessage());
                    // Способ 3: Поиск по href
                    System.out.println("Попытка 3: Поиск по href");
                    link = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='http://localhost:8080/contacts']")));
                }
            }
            if (link == null) {
                throw new RuntimeException("Не удалось найти ссылку для раздела '" + sectionName + "'");
            }
            System.out.println("Найдена ссылка: " + link.getText() + " | URL: " + link.getAttribute("href"));
            link.click();
            System.out.println("Перешёл в раздел: " + sectionName);
        } catch (Exception e) {
            System.out.println("HTML страницы: " + driver.getPageSource());
            throw new RuntimeException("Ошибка при переходе в раздел '" + sectionName + "': " + e.getMessage());
        }
    }

    @Then("Отображается страница с контактной информацией компании")
    public void checkContactsPageDisplayed() {
        try {
            System.out.println("Проверяю отображение страницы контактов");
            String currentUrl = driver.getCurrentUrl();
            Assert.assertTrue("URL должен содержать 'contacts'", currentUrl.contains("contacts"));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке URL страницы контактов: " + e.getMessage());
        }
    }

    @Then("На странице присутствуют телефон и адрес")
    public void checkContactInfoPresent() {
        try {
            System.out.println("Проверяю наличие контактной информации");
            System.out.println("HTML страницы контактов: " + driver.getPageSource());
            WebElement phone1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//li[contains(., 'Телефон 1')]//span")));
            WebElement phone2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//li[contains(., 'Телефон 2')]//span")));
            WebElement address = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//li[contains(., 'Адресс')]//span")));
            System.out.println("Найден телефон 1: " + phone1.getText());
            System.out.println("Найден телефон 2: " + phone2.getText());
            System.out.println("Найден адрес: " + address.getText());
            Assert.assertTrue("Элемент телефона 1 должен быть отображен", phone1.isDisplayed());
            Assert.assertTrue("Элемент телефона 2 должен быть отображен", phone2.isDisplayed());
            Assert.assertTrue("Элемент адреса должен быть отображен", address.isDisplayed());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке контактной информации: " + e.getMessage());
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

    @Given("Открыта главная страница")
    public void openMainPage() {
        try {
            System.out.println("Открываю главную страницу: http://localhost:8080/");
            driver.get("http://localhost:8080/");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при открытии главной страницы: " + e.getMessage());
        }
    }
}
