package service

import io.github.bonigarcia.wdm.WebDriverManager
import model.UnverifiedUser
import model.VerifiedUser
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

class VerificationService(private val verificationUrl: String) {

    init {
        WebDriverManager.chromedriver().setup()
    }

    private val options: ChromeOptions = ChromeOptions().apply {
        addArguments("--headless", "--no-sandbox")
    }
    private val driver: WebDriver = ChromeDriver(options)
    private lateinit var wait: WebDriverWait


    fun verifyUser(user: UnverifiedUser): VerifiedUser {
        try {
            val verifiedNameXpath = """
                //*[@id="registration-form"]/div/div[1]/div[3]/div/h3
                """.trimIndent()
            val regIdNoXpath = """
                //*[@id="registration_id_number"]
                """.trimIndent()
            val regFirstNameXpath = """
                //*[@id="registration_first_name"]
                """.trimIndent()
            val idVerifyBtnXpath = """
                //*[@id="registration-form"]/div/div[1]/div[3]/div/button
                """.trimIndent()
            driver.get(verificationUrl)
            wait = WebDriverWait(driver, 10)
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(regIdNoXpath)))
            driver.findElement(By.xpath(regIdNoXpath)).sendKeys(user.idNumber)
            driver.findElement(By.xpath(regFirstNameXpath)).sendKeys(user.firstName)
            driver.findElement(By.xpath(idVerifyBtnXpath)).click()
            wait = WebDriverWait(driver, 5)
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(verifiedNameXpath)))
            val verifiedName = driver.findElement(By.xpath(verifiedNameXpath)).text
                .toString().split(" ").map { it.toLowerCase().capitalize() }
            return VerifiedUser(verifiedName[0], verifiedName[1], user.idNumber)
        } finally {
            driver.close()
        }
    }
}