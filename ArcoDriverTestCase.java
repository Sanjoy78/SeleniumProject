package com.arco.util;
/*
 * @Author: Sanjoy
 * 
 */
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import com.arco.pages.asm.ASMHomePage;
import com.arco.pages.backoffice.BackofficeHomePage;
import com.arco.pages.cockpit.WCMSCockpitLoginPage;
import com.arco.pages.hac.HACLoginPage;
import com.arco.pages.storefront.HomePage;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.ChartLocation;
import com.aventstack.extentreports.reporter.configuration.Theme;



public abstract class ArcoDriverTestCase 
{
	private static final Logger logger = LoggerFactory.getLogger(ArcoDriverTestCase.class);

	public static Object action;
	protected Robot robot;
	public String domain = "";

	// Initialize objects
	public PropertyReaderArco propertyReader = new PropertyReaderArco();
	protected String browser = propertyReader.readApplicationFile("BROWSER");
	// Define objects
	protected WebDriver driver;
	private Runtime runtime = Runtime.getRuntime();
	private static final long MEGABYTE = 1024L * 1024L;
	private FirefoxProfile profile;
	private String path = "";
	private HashMap<String, Object> chromePrefs;
	private ChromeOptions options;
	private String geckoPath;
	private File currentFile;
	
	public static ExtentHtmlReporter htmlRepoter;
    public static ExtentReports extent;
    public static ExtentTest repoterLog;
    
    

	//This method will open, maximize, and delete cookies for a browser.
	@SuppressWarnings("deprecation")
	@BeforeClass
	public void setUp() throws Exception {
		if (("firefox").equalsIgnoreCase(browser)) {
			FirefoxProfile firefoxProfile = new FirefoxProfile();
	        firefoxProfile.setPreference("browser.private.browsing.autostart",true);
			DesiredCapabilities capabilities = new DesiredCapabilities();
			capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			capabilities.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
			geckoPath = propertyReader.readApplicationFile("gecko_path");
			if(StringUtils.isNotEmpty(geckoPath)) {
				System.setProperty("webdriver.gecko.driver", geckoPath);
			}
			driver = new FirefoxDriver();
		} else if (("IE").equalsIgnoreCase(browser)) {
			String path = getPath();
			File file = new File(path + "//propertyFile//IEDriverServer32.exe");
			System.setProperty("webdriver.ie.driver", file.getAbsolutePath());
			DesiredCapabilities capabilities = new DesiredCapabilities();
			capabilities.setCapability("nativeEvents", false);
			capabilities.setCapability("unexpectedAlertBehaviour", "accept");
			capabilities.setCapability("ignoreProtectedModeSettings", true);
			capabilities.setCapability("disable-popup-blocking", true);
			capabilities.setCapability("enablePersistentHover", true);
			capabilities.setCapability("ignoreZoomSetting", true);
			 capabilities.setCapability("ignoreZoomSetting", true);
	           capabilities.setCapability(InternetExplorerDriver.INITIAL_BROWSER_URL, "");
			capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
			capabilities.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, false);
			driver = new InternetExplorerDriver(capabilities);
			
		} else if (("chrome").equalsIgnoreCase(browser)) {
			final String path = getPath();
			System.out.println("path::::::" + path);
			final File file = new File(path + "//drivers//chromedriver.exe");
			System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
			@SuppressWarnings("static-access")
			final DesiredCapabilities capabilities = new DesiredCapabilities().chrome();
			capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			capabilities.setCapability(ChromeOptions.CAPABILITY, setChromePreferenceForFileDownload());
			driver = new ChromeDriver(capabilities);
		}
		// Maximize window
		driver.manage().window().maximize();

		// Delete cookies
		driver.manage().deleteAllCookies();
		PropertyReaderArco.domainName = propertyReader.readApplicationFile("Server");
	}

	//This method will be used to open Arco store front application.
	public HomePage applicationSetup() throws Exception {
		driver.navigate().to(propertyReader.readApplicationFile(PropertyReaderArco.getDomain() + "_URL"));
		return PageFactory.initElements(driver, HomePage.class);
	}
	
	//This method will be used to open Arco backoffice application.
	public BackofficeHomePage applicationSetupBackoffice() throws Exception
	{
		driver.navigate().to(propertyReader.readApplicationFile(PropertyReaderArco.getDomain()+"_BackofficeURL"));
		return PageFactory.initElements(driver, BackofficeHomePage.class);
	}
	
	
	//This method will be used to open Arco ASM application.
	public ASMHomePage applicationSetupASM() throws Exception
	{
		driver.navigate().to(propertyReader.readApplicationFile(PropertyReaderArco.getDomain()+"_ASMURL"));
		return PageFactory.initElements(driver, ASMHomePage.class);
	}
	
	//This method will be used to open Arco WCMS cockpit application.
	public WCMSCockpitLoginPage applicationSetupWCSM() throws Exception
	{
		driver.navigate().to(propertyReader.readApplicationFile(PropertyReaderArco.getDomain()+"_CMSCockpit"));
		return PageFactory.initElements(driver, WCMSCockpitLoginPage.class);
	}
	
	public HACLoginPage applicationSetupHAC() throws Exception
	{
		driver.navigate().to(propertyReader.readApplicationFile(PropertyReaderArco.getDomain()+"_HAC"));
		return PageFactory.initElements(driver, HACLoginPage.class);
	}
	
	/*
	@BeforeSuite(alwaysRun = true)
	public void deleteAllScreenShots()
	{
		try {

			File file = new File(getPath() + "/screenshots/");
			String[] myFiles;
			if (file.isDirectory()) 
			{
				myFiles = file.list();
				for (int i = 0; i < myFiles.length; i++) 
				{
					File myFile = new File(file, myFiles[i]);
					myFile.delete();
				}
			}
			} catch(Exception e)
			{
				System.out.println(e.getMessage());
			}
	}
	*/
	
	@BeforeSuite(alwaysRun = true)
	public void revCreateDirectory()  {
		
		
		
        //To create single directory/folder
       
		Date date = new Date() ;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss") ;
		
		File dir = new File("ScreenShots/"+dateFormat.format(date));
		System.out.println(dateFormat.format(date));
		dir.mkdirs();
		setCurrentFile(dir);
		
        
        }
        

    
	

	
	//This method will be used to close browser.
	@AfterClass(alwaysRun = true)
	public void tearDown() throws IOException {
		logger.debug("Execution completed for test\n");
		logger.debug("*****************************************************************************");
		logger.debug("Memory after execution (in MB) " + runtime.totalMemory() / MEGABYTE);

		// Run the garbage collector
		runtime.gc();
		logger.debug("Memory freed after execution (in MB) " + runtime.freeMemory() / MEGABYTE);

		// Calculate the used memory
		final long memory = runtime.totalMemory() - runtime.freeMemory();
		logger.debug("Used memory (in MB) " + memory / MEGABYTE);
		try {
			if (!(driver == null)) {
				driver.quit();
			}
		} catch (final Exception e) 
		{
			throw e;
		}
		
		System.out.println("Aftet" + Runtime.getRuntime().freeMemory());
	}

	//This method will be used to set driver value.
	public void setWebDriver(WebDriver driver) {
		this.driver = driver;
	}

	// This method will be used to get parent window and switch the focus to parent window.
	public String switchPreviewWindow() {
		final Set<String> windows = driver.getWindowHandles();
		final Iterator<String> iter = windows.iterator();
		final String parent = iter.next();

		driver.switchTo().window(iter.next());
		return parent;
	}

	// Get absolute path
	public String getPath() {
		String path = "";
		final File file = new File("");
		final String absolutePathOfFirstFile = file.getAbsolutePath();
		path = absolutePathOfFirstFile.replaceAll("\\\\+", "/");
		return path;
	}

	// Capture Screenshot
	public void captureScreenshot(final String fileName) throws Exception {
		try {

			File file = new File(getPath() + "/screenshots/");
			String[] myFiles;
			if (file.isDirectory()) {
				myFiles = file.list();
				for (int i = 0; i < myFiles.length; i++) {
					//File myFile = new File(file, myFiles[i]);
					//myFile.delete();
					
				}
			}
			String screenshotName = this.getFileName(fileName);
			FileOutputStream out = new FileOutputStream("screenshots//" + screenshotName + ".jpg");
			out.write(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
			out.close();
			String path = getPath();
			String screen = "file://" + path + "/screenshots/" + screenshotName + ".jpg";
			System.setProperty("org.uncommons.reportng.escape-output", "false");
			Reporter.log("<a href= '" + screen + "'target='_blank' ><img src='" + screen
					+ "' height=\"42\" width=\"42\" >" + screenshotName + "</a>");
		} catch (Exception e) 
		{
			System.out.println(e.getMessage());
			throw e;

		}
	}
	
	
	
	public void takeScreenShot(WebDriver driver, String fileName)
	{
		try
		{
			TakesScreenshot tss = (TakesScreenshot)driver;
			File source = tss.getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(source, new File(getCurrentFile(), fileName+".png"));
		} catch(Exception e)
		{
			System.out.println("Exception while taking screenshot"+e.getMessage());
		}
	}

	// Creating file name
	public String getFileName(final String file) {
		final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat dateFormat1 = new SimpleDateFormat("hh-mm-ss");
		final Calendar cal = Calendar.getInstance();

		final String fileName = file + dateFormat.format(cal.getTime()) + "_" + dateFormat1.format(cal.getTime());

		return fileName;
	}

	// Switch frame
	public void switchFrame(final String[] arr) {
		for (final String element : arr) {
			driver.switchTo().frame(element);
		}
	}

	//Here we are mentioning driver type.
	enum DriverType {
		Firefox, IE, Chrome
	}

	//This method will be used to minimize all window.
	public void minimizeAllWindows() throws InterruptedException, AWTException {
		robot.keyPress(KeyEvent.VK_WINDOWS);
		Thread.sleep(1000);

		robot.keyPress(KeyEvent.VK_D);
		Thread.sleep(1000);
		robot.keyRelease(KeyEvent.VK_D);
		Thread.sleep(1000);

		robot.keyRelease(KeyEvent.VK_WINDOWS);
		Thread.sleep(1000);

	}

	//This method will be used to clear memory.
	public void clearMemory() {
		// Get the Java runtime
		final int MAXJVMMemoryUsage = 50;
		logger.debug("Initial Memory consumed (in MB) "
				+ (runtime.totalMemory() - runtime.freeMemory()) / runtime.totalMemory() * 100);
		if ((runtime.totalMemory() - runtime.freeMemory()) / runtime.totalMemory() * 100 >= MAXJVMMemoryUsage) {
			runtime.gc();
			runtime.gc();
			logger.debug("Memory Cleared");
		}
	}



	//This method will be used to get random number.
	public String getRandomNumber() {
		DateFormat format = new SimpleDateFormat("ddMMyyHHmmSS");
		Date date = new Date();
		String randomInteger = format.format(date);
		return randomInteger;
	}

	//This method will be used to add log.
	public void addLog(String message) {
		Reporter.log(message + "<br>");
	}


	//This method will be used to get random string.
	public String randomString(int len) {
		String AB = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		return sb.toString();
	}


	//This method will be used to set firefox preference.
	public FirefoxProfile setFirefoxPreferenceForFileDownload() {
		profile = new FirefoxProfile();
		path = propertyReader.readApplicationFile("File_Path");
		profile.setPreference("browser.download.folderList", 2);
		profile.setPreference("browser.download.dir", path);
		profile.setPreference("browser.download.manager.alertOnEXEOpen", false);
		profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
				"application/msword, application/csv, application/ris, text/csv, image/png, application/pdf, text/html, text/plain, application/zip, application/x-zip, application/x-zip-compressed, application/download, application/octet-stream");
		profile.setPreference("browser.download.manager.showWhenStarting", false);
		profile.setPreference("browser.download.manager.focusWhenStarting", false);
		profile.setPreference("browser.download.useDownloadDir", true);
		profile.setPreference("browser.helperApps.alwaysAsk.force", false);
		profile.setPreference("browser.download.manager.alertOnEXEOpen", false);
		profile.setPreference("browser.download.manager.closeWhenDone", true);
		profile.setPreference("browser.download.manager.showAlertOnComplete", false);
		profile.setPreference("browser.download.manager.useWindow", false);
		profile.setPreference("services.sync.prefs.sync.browser.download.manager.showWhenStarting", false);
		profile.setPreference("pdfjs.disabled", true);
		return profile;
	}


	//This method will be used to set chrome preference.
	public ChromeOptions setChromePreferenceForFileDownload() {
		path = propertyReader.readApplicationFile("File_Path");
		chromePrefs = new HashMap<String, Object>();
		chromePrefs.put("profile.default_content_settings.popups", 0);
		chromePrefs.put("download.default_directory", path);
		options = new ChromeOptions();
		options.setExperimentalOption("prefs", chromePrefs);
		return options;
	}

	public File getCurrentFile() {
		return currentFile;
	}

	public void setCurrentFile(File currentFile) {
		this.currentFile = currentFile;
	}
	
	
	@BeforeClass
    public void startReport()
    {
        htmlRepoter = new ExtentHtmlReporter(System.getProperty("user.dir")+"/test-output/learnReport.html");
        extent.attachReporter(htmlRepoter);
        extent.setSystemInfo("OS", "Window 7");
        extent.setSystemInfo("Host Name:", "Sanjoy");
        extent.setSystemInfo("Environment", "QA");
        extent.setSystemInfo("User Name:", "Sanjoy");
        
        htmlRepoter.config().setDocumentTitle("Automation demo");
        htmlRepoter.config().setReportName("My Own Report");
        htmlRepoter.config().setTestViewChartLocation(ChartLocation.TOP);
        htmlRepoter.config().setTheme(Theme.DARK);
    }
	
	
	@BeforeClass
	public static synchronized ExtentReports getExtent()
	{
		if(extent!=null)
		{
			return extent;
		}else
		{
			return extent = new ExtentReports();
		}
	}


	@AfterMethod
	public void getResult(ITestResult result)
	{
		if(result.getStatus()==ITestResult.FAILURE)
		{
			repoterLog.log(Status.FAIL, MarkupHelper.createLabel(result.getName()+"Test case failed", ExtentColor.RED));
			repoterLog.fail(result.getThrowable());
		}
		else if(result.getStatus()==ITestResult.SUCCESS)
		{
			repoterLog.log(Status.PASS, MarkupHelper.createLabel(result.getName()+"Test case pass", ExtentColor.GREEN));
		}
		else
		{
			repoterLog.log(Status.SKIP, MarkupHelper.createLabel(result.getName()+"Test case skiped", ExtentColor.YELLOW));
			repoterLog.skip(result.getThrowable());
		}

	}

	@AfterClass
	public void tearDown1()
	{
		extent.flush();

	}
	
		
	



}
