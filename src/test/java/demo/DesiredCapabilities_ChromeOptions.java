package demo;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.Test;

public class DesiredCapabilities_ChromeOptions {
	@Test
	public void localcapabilities_modernOptions_demo() throws InterruptedException {
		
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--start-maximized");
//		options.addArguments("--start-maximized");     // you already have
//		options.addArguments("--incognito");
//		options.addArguments("--disable-extensions");
//		options.addArguments("--disable-notifications");
//		options.addArguments("--disable-popup-blocking");
//		options.addArguments("--disable-gpu");         // helpful in CI/Linux
//		options.addArguments("--no-sandbox");          // helpful in containerized CI
//		options.addArguments("--remote-allow-origins=*");
		
		options.setStrictFileInteractability(true);

		// Headless (modern)
		options.addArguments("--headless=new");
		options.addArguments("--window-size=1920,1080");

		// Custom user agent
		options.addArguments("user-agent=MyTestAgent/1.0");
		options.setAcceptInsecureCerts(true);
	
		
		WebDriver driver = new ChromeDriver(options);
		//try {
			driver.get("https://the-internet.herokuapp.com");
			
			Capabilities cap = ((HasCapabilities) driver).getCapabilities();
			System.out.println("Browser : " + cap.getBrowserName());
			System.out.println("Browser version : " + cap.getBrowserVersion());
			System.out.println("Platform : " + cap.getPlatformName());
			System.out.println("AcceptanceCerts :" + cap.getCapability("acceptInsecureCerts"));
		//} finally {
			Thread.sleep(2000);
			driver.quit();
		}
	}


