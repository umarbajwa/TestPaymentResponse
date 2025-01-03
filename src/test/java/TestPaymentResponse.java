import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;

public class TestPaymentResponse {

    public static void main(String[] args) { 
        // Set up the ChromeDriver
        WebDriverManager.chromedriver().setup();

        // Create a new instance of ChromeDriver and WebDriverWait
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));

        try {
            // Go to the payment page
            driver.get("https://demo.dev.tap.company/v2/sdk/card");
            driver.manage().window().maximize();

            // Change the currency and scope
            changeCurrency(driver, wait, "BHD");
            changeScope(driver, wait, "Authenticated Token");

            // Process the first card payment
            System.out.println("Processing card : 5123450000000008 ");
            processCard(driver, wait, "5123450000000008", "01/39", "100");

            System.out.println("\n \n Processing card : 4508750015741019 ");
            processCard(driver, wait, "4508750015741019", "01/39", "100");

        } finally {
            // Close the browser
            driver.quit();
        }
    }

    private static void processCard(WebDriver driver, WebDriverWait wait, String cardNumber, String expiryDate, String cvv) {
        // Switch to the iframe for card details
        WebDriver iframeDriver = switchToIframe(driver, wait);
        fillCardDetails(iframeDriver, cardNumber, expiryDate, cvv, wait);

        // Go back to the main content
        driver.switchTo().defaultContent();

        // Click the button to generate a token
        WebElement generateTokenButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("token_button")));
        generateTokenButton.click();
        
     // Wait for the loader to become invisible (opacity = 0)
        wait.until(d -> {
            WebElement loader = d.findElement(By.cssSelector("[data-testid='sdk-Loader']"));
            return loader.getCssValue("opacity").equals("0");
        });

        // Switch to the authentication iframe
        iframeDriver = switchToIframe(driver, wait);
        
        
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("tap-card-iframe-authentication"))); //3ds auth iframe
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("challengeFrame"))); // Auth Challenge iframe with submit button and options dropdown.

        // Wait for the submit button and click it
        WebElement submitButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("acssubmit")));
        wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitButton); // scrolling the button in view to prevent failed clicks.
        submitButton.click();

        // Go back to the main content
        driver.switchTo().defaultContent();



        // Wait for the response data to show up
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='CodeBlock-content']")));
        printResponseData(driver, wait);

        WebElement clearLogButton = driver.findElement(By.xpath("//button[contains(@class, 'token_button') and text()='Clear Logs']")); // Clear response log after printing 
        clearLogButton.click();
    }

    private static void changeCurrency(WebDriver driver, WebDriverWait wait, String currency) {
        // Click the currency dropdown and select the desired currency
        WebElement currencyDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("currency")));
        currencyDropdown.click();
        WebElement currencyOption = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//option[text()='" + currency + "']")));
        currencyOption.click();
    }

    private static void changeScope(WebDriver driver, WebDriverWait wait, String scope) {
        // Click the scope dropdown and select the desired scope
        WebElement scopeDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("scope")));
        scopeDropdown.click();
        WebElement scopeOption = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//option[text()='" + scope + "']")));
        scopeOption.click();
    }

    private static WebDriver switchToIframe(WebDriver driver, WebDriverWait wait) {
        // Switch to the specified iframe
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("tap-card-iframe")));
        return driver;
    }

    private static void fillCardDetails(WebDriver driver, String cardNumber, String expiryDate, String cvv, WebDriverWait wait) {
        // Wait for the card input container and fill in the details
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("card-inputs-container")));
        WebElement cardNumberField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("card_input_mini")));
        cardNumberField.sendKeys(cardNumber);

        // Fill in the expiry date and CVV
        WebElement expiryDateField = driver.findElement(By.id("date_input"));
        expiryDateField.sendKeys(expiryDate);
        WebElement cvvField = driver.findElement(By.id("cvv_input"));
        cvvField.sendKeys(cvv);

        WebElement nameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cardHolderName_input")));
        nameField.clear();
        nameField.sendKeys("AHMED");
    }

    private static void printResponseData(WebDriver driver, WebDriverWait wait) {
        // Click the radio button for the response
        WebElement responseTab = driver.findElement(By.cssSelector("input[type='radio'][value='response']"));
        responseTab.click();

        // Wait for the response data to appear
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='CodeBlock-content']")));

        // Get and print the response data
        WebElement responseData = driver.findElement(By.cssSelector("[data-testid='CodeBlock-content']"));
        System.out.println(responseData.getText());
    }
}
