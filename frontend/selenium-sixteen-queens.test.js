/* eslint-env node */
/* eslint-disable no-undef */

import { Builder, By, until } from "selenium-webdriver";
import chrome from "selenium-webdriver/chrome.js";

const FRONTEND_URL = process.env.FRONTEND_URL || "http://localhost:5173";
const TEST_TIMEOUT_MS = Number(process.env.TEST_TIMEOUT_MS || 120000);
const WRONG_SOLUTION = "0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15";

const results = [];
let driver;

function logPass(name, details = "") {
  results.push({ step: name, status: "PASS", details });
  console.log(`✅ PASS: ${name}${details ? ` -> ${details}` : ""}`);
}

function logFail(name, error) {
  const details = error?.message || String(error);
  results.push({ step: name, status: "FAIL", details });
  console.error(`❌ FAIL: ${name} -> ${details}`);
}

async function runStep(name, action) {
  try {
    console.log(`▶ ${name}`);
    await action();
    logPass(name);
  } catch (error) {
    logFail(name, error);
    throw error;
  }
}

function printSummary() {
  console.log("\n================ SELENIUM TEST SUMMARY ================");
  for (const row of results) {
    console.log(`${row.status.padEnd(5)} | ${row.step} | ${row.details || "-"}`);
  }
  const passCount = results.filter((item) => item.status === "PASS").length;
  const failCount = results.filter((item) => item.status === "FAIL").length;
  console.log("-------------------------------------------------------");
  console.log(`Total Steps: ${results.length}`);
  console.log(`Passed     : ${passCount}`);
  console.log(`Failed     : ${failCount}`);
  console.log("=======================================================\n");
}

async function clickButtonByText(text) {
  const button = await driver.wait(
    until.elementLocated(By.xpath(`//button[normalize-space()="${text}"]`)),
    10000
  );
  await driver.wait(until.elementIsVisible(button), 5000);
  await driver.executeScript("arguments[0].scrollIntoView({ block: 'center' });", button);
  await driver.wait(until.elementIsEnabled(button), 5000);
  await button.click();
}

async function findInputByPlaceholder(placeholder) {
  const element = await driver.wait(
    until.elementLocated(By.css(`[placeholder="${placeholder}"]`)),
    10000
  );
  await driver.wait(until.elementIsVisible(element), 5000);
  return element;
}

async function findSixteenQueensCard() {
  const possibleLocators = [
    By.xpath("//*[normalize-space()='Sixteen Queens']"),
    By.xpath("//*[normalize-space()='Sixteen queens']"),
    By.xpath("//*[contains(normalize-space(.), 'Sixteen Queens')]"),
    By.xpath("//*[contains(normalize-space(.), 'Sixteen queens')]"),
  ];

  for (const locator of possibleLocators) {
    const matches = await driver.findElements(locator);
    if (matches.length > 0) {
      return matches[0];
    }
  }

  throw new Error("Sixteen Queens card/title not found on landing page");
}

async function clickOpenGameForSixteenQueens() {
  const possibleButtons = [
    "//h3[normalize-space()='Sixteen Queens']/following::button[normalize-space()='Open Game'][1]",
    "//h3[normalize-space()='Sixteen queens']/following::button[normalize-space()='Open Game'][1]",
    "//*[contains(normalize-space(.), 'Sixteen Queens')]/following::button[normalize-space()='Open Game'][1]",
    "//*[contains(normalize-space(.), 'Sixteen queens')]/following::button[normalize-space()='Open Game'][1]",
  ];

  for (const xpath of possibleButtons) {
    const buttons = await driver.findElements(By.xpath(xpath));
    if (buttons.length > 0) {
      await driver.executeScript("arguments[0].scrollIntoView({ block: 'center' });", buttons[0]);
      await driver.wait(until.elementIsVisible(buttons[0]), 3000);
      await buttons[0].click();
      return;
    }
  }

  const card = await findSixteenQueensCard();
  await driver.executeScript("arguments[0].scrollIntoView({ block: 'center' });", card);
  await card.click();
}

async function runSequentialFast() {
  await clickButtonByText("Run Sequential");
  await driver.sleep(500);
}

async function runThreadedFast() {
  await clickButtonByText("Run Threaded");
  await driver.sleep(500);
}

function shuffle(array) {
  const copy = [...array];
  for (let i = copy.length - 1; i > 0; i -= 1) {
    const j = Math.floor(Math.random() * (i + 1));
    [copy[i], copy[j]] = [copy[j], copy[i]];
  }
  return copy;
}

function generateValidSolution(size = 16) {
  const cols = new Set();
  const diag1 = new Set();
  const diag2 = new Set();
  const board = new Array(size).fill(-1);

  function backtrack(row) {
    if (row === size) return true;

    for (const col of shuffle([...Array(size).keys()])) {
      const d1 = row - col;
      const d2 = row + col;

      if (cols.has(col) || diag1.has(d1) || diag2.has(d2)) {
        continue;
      }

      board[row] = col;
      cols.add(col);
      diag1.add(d1);
      diag2.add(d2);

      if (backtrack(row + 1)) {
        return true;
      }

      board[row] = -1;
      cols.delete(col);
      diag1.delete(d1);
      diag2.delete(d2);
    }

    return false;
  }

  if (!backtrack(0)) {
    throw new Error("Failed to generate a valid 16-queens answer");
  }

  return board.join(",");
}

function extractAnswerSequencesFromText(text) {
  return new Set(text.match(/\b(?:[0-9]|1[0-5])(?:,(?:[0-9]|1[0-5])){15}\b/g) || []);
}

async function getBodyText() {
  const body = await driver.findElement(By.css("body"));
  return body.getText();
}

async function getPlayerInput() {
  return findInputByPlaceholder("Enter player name");
}

async function getAnswerTextarea() {
  return findInputByPlaceholder(
    "Example: 1,3,5,7,9,11,13,15,0,2,4,6,8,10,12,14"
  );
}

async function fillAnswerForm(playerName, answer) {
  const playerInput = await getPlayerInput();
  const answerTextarea = await getAnswerTextarea();

  await driver.executeScript("arguments[0].scrollIntoView({ block: 'center' });", playerInput);
  await playerInput.clear();
  await playerInput.sendKeys(playerName);

  await driver.executeScript("arguments[0].scrollIntoView({ block: 'center' });", answerTextarea);
  await answerTextarea.clear();
  await answerTextarea.sendKeys(answer);
}

async function waitForPopup() {
  const popup = await driver.wait(
    until.elementLocated(By.css(".popup-overlay")),
    8000,
    "Popup overlay not found"
  );

  await driver.wait(until.elementIsVisible(popup), 5000);

  await driver.wait(
    async () => {
      try {
        const text = (await popup.getText()).trim();
        if (text.length > 0) return true;

        const innerCandidates = await popup.findElements(
          By.css(".popup-message, .popup-text, .popup-content, .popup-box, h1, h2, h3, p, span, div")
        );

        for (const el of innerCandidates) {
          const t = (await el.getText()).trim();
          if (t.length > 0) return true;
        }

        return false;
      } catch {
        return false;
      }
    },
    5000
  ).catch(() => {});
}

async function getPopupText() {
  const popup = await driver.findElement(By.css(".popup-overlay"));

  let text = (await popup.getText()).trim();
  if (text) return text;

  const possibleTextSelectors = [
    ".popup-message",
    ".popup-text",
    ".popup-content",
    ".popup-box",
    "h1",
    "h2",
    "h3",
    "p",
    "span",
    "div",
  ];

  for (const selector of possibleTextSelectors) {
    const elements = await popup.findElements(By.css(selector));
    for (const el of elements) {
      const candidate = (await el.getText()).trim();
      if (candidate) {
        return candidate;
      }
    }
  }

  return "";
}

async function closePopupIfPresent() {
  const okLocators = [
    By.xpath("//button[normalize-space()='OK']"),
    By.xpath("//button[normalize-space()='Ok']"),
    By.xpath("//button[normalize-space()='Close']"),
    By.xpath("//button[contains(normalize-space(.), 'OK')]"),
    By.xpath("//button[contains(normalize-space(.), 'Close')]"),
    By.css(".popup-close-btn"),
    By.css(".popup-ok-btn"),
  ];

  for (const locator of okLocators) {
    const buttons = await driver.findElements(locator);
    if (buttons.length > 0) {
      await driver.executeScript("arguments[0].scrollIntoView({ block: 'center' });", buttons[0]);
      await driver.wait(until.elementIsVisible(buttons[0]), 3000);
      await buttons[0].click();

      await driver.wait(
        async () => (await driver.findElements(By.css(".popup-overlay"))).length === 0,
        5000
      ).catch(() => {});

      await driver.sleep(300);
      return;
    }
  }
}

async function submitAndReadPopup(playerName, answer) {
  await fillAnswerForm(playerName, answer);
  await clickButtonByText("Submit Answer");
  await waitForPopup();
  const popupText = await getPopupText();
  console.log("Popup text:", popupText || "[EMPTY]");
  await closePopupIfPresent();
  return popupText;
}

async function findFreshCorrectSolution(maxAttempts = 10) {
  for (let i = 0; i < maxAttempts; i += 1) {
    const currentText = await getBodyText();
    const existingAnswers = extractAnswerSequencesFromText(currentText);
    const candidate = generateValidSolution();

    if (!existingAnswers.has(candidate)) {
      return candidate;
    }
  }

  return generateValidSolution();
}

async function findAlreadyFoundSolution() {
  const bodyText = await getBodyText();
  const existingAnswers = [...extractAnswerSequencesFromText(bodyText)];

  if (existingAnswers.length > 0) {
    return existingAnswers[0];
  }

  throw new Error("Could not find an already stored solution on the page");
}

function isSuccessPopup(popupText) {
  const text = popupText.toLowerCase();
  return (
    text.includes("congrat") ||
    text.includes("correct") ||
    text.includes("success") ||
    text.includes("already found") ||
    text.includes("already")
  );
}

function isWrongPopup(popupText) {
  const text = popupText.toLowerCase();
  return (
    text.includes("wrong") ||
    text.includes("invalid") ||
    text.includes("try again") ||
    text.includes("incorrect")
  );
}

function isDuplicatePopup(popupText) {
  const text = popupText.toLowerCase();
  return (
    text.includes("already found") ||
    text.includes("already") ||
    text.includes("recognized")
  );
}

async function main() {
  console.log("Starting Selenium test...");
  console.log(`Target URL: ${FRONTEND_URL}`);

  const options = new chrome.Options();
  options.setChromeBinaryPath("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");
  options.addArguments("--start-maximized");
  options.addArguments("--disable-gpu");
  options.addArguments("--no-sandbox");
  options.addArguments("--disable-dev-shm-usage");
  options.addArguments("--remote-allow-origins=*");

  console.log("Launching Chrome browser...");

  driver = await new Builder()
    .forBrowser("chrome")
    .setChromeOptions(options)
    .build();

  console.log("Chrome launched successfully.");

  await driver.manage().setTimeouts({
    implicit: 2000,
    pageLoad: TEST_TIMEOUT_MS,
    script: TEST_TIMEOUT_MS,
  });

  try {
    let freshCorrectAnswer = "";
    let alreadyFoundAnswer = "";

    await runStep("Open landing page", async () => {
      await driver.get(FRONTEND_URL);
      await driver.wait(until.titleMatches(/Vite|React|dsa-game-hub/i), 10000).catch(() => {});
      await findSixteenQueensCard();
    });

    await runStep("Verify Sixteen Queens game card exists", async () => {
      await findSixteenQueensCard();
    });

    await runStep("Open Sixteen Queens page from Home.jsx", async () => {
      await clickOpenGameForSixteenQueens();

      await driver.wait(
        async () => {
          const url = await driver.getCurrentUrl();
          return url.includes("/sixteen-queens") || url.includes("/queens");
        },
        10000,
        "Did not navigate to the Sixteen Queens page"
      );

      await driver.wait(
        until.elementLocated(
          By.xpath("//*[contains(.,'Sixteen Queens Puzzle') or contains(.,'Sixteen queens')]")
        ),
        10000
      );
    });

    await runStep("Run sequential algorithm fast", async () => {
      await runSequentialFast();
    });

    await runStep("Run threaded algorithm fast", async () => {
      await runThreadedFast();
    });

    await runStep("Generate fresh correct answer automatically", async () => {
      freshCorrectAnswer = await findFreshCorrectSolution();
      console.log(`Generated correct answer: ${freshCorrectAnswer}`);
    });

    await runStep("Submit correct answer", async () => {
      const popupText = await submitAndReadPopup(
        `Correct User ${Date.now()}`,
        freshCorrectAnswer
      );

      if (!isSuccessPopup(popupText)) {
        throw new Error(`Unexpected correct-answer popup: ${popupText}`);
      }
    });

    await runStep("Submit wrong answer", async () => {
      const popupText = await submitAndReadPopup(
        `Wrong User ${Date.now()}`,
        WRONG_SOLUTION
      );

      if (!isWrongPopup(popupText)) {
        throw new Error(`Unexpected wrong-answer popup: ${popupText}`);
      }
    });

    await runStep("Find already found answer automatically", async () => {
      alreadyFoundAnswer = await findAlreadyFoundSolution();
      console.log(`Using already found answer: ${alreadyFoundAnswer}`);
    });

    await runStep("Submit already found answer", async () => {
      const popupText = await submitAndReadPopup(
        `Duplicate User ${Date.now()}`,
        alreadyFoundAnswer
      );

      if (!isDuplicatePopup(popupText)) {
        throw new Error(`Unexpected already-found popup: ${popupText}`);
      }
    });

    await runStep("Verify algorithm runs section visible", async () => {
      await driver.findElement(By.xpath("//h2[normalize-space()='Algorithm Runs']"));
    });

    await runStep("Verify player answers section visible", async () => {
      await driver.findElement(By.xpath("//h2[normalize-space()='Player Answers']"));
    });
  } finally {
    printSummary();
    if (driver) {
      console.log("Closing browser...");
      await driver.quit();
    }
  }
}

main().catch((error) => {
  console.error("\nSelenium suite finished with errors.");
  console.error(error);
  process.exitCode = 1;
});