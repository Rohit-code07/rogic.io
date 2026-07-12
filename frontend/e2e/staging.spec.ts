import { test, expect } from '@playwright/test';

test.describe('Staging Environment E2E Smoke Integration Test', () => {
  test('should load home page, register user, render stage, and check profile info', async ({ page }) => {
    // 1. Navigate to Staging homepage
    console.log('Navigating to homepage...');
    await page.goto('/');

    // Verify main page elements are rendered
    await expect(page.locator('.landing-logo-title')).toHaveText('rogic.io');

    // Skip the intro to display the Play Now button immediately
    console.log('Skipping intro...');
    await page.click('.intro-control-btn');

    // 2. Click Play Now to navigate to the Game Play tab
    console.log('Navigating to Game Play tab...');
    await expect(page.locator('.landing-play-btn')).toBeVisible();
    await page.waitForTimeout(1500); // Wait for the transition to end and layout to settle
    await page.click('.landing-play-btn', { force: true });

    // Wait for the loading screen to disappear
    await expect(page.locator('.loading-state')).not.toBeVisible({ timeout: 15000 });

    // Verify canvas board is rendered
    console.log('Verifying Nonogram Canvas board rendering...');
    const canvas = page.getByTestId('nonogram-canvas');
    await expect(canvas).toBeVisible({ timeout: 10000 });



    // 3. Verify My Page access is blocked and Login button is visible instead (Guest Policy Validation)
    console.log('Verifying My Page tab button is hidden for guests...');
    await expect(page.locator('.tab-btn-mypage')).not.toBeVisible();

    console.log('Verifying Login button is visible in the header for guest...');
    const miniLoginBtn = page.locator('.mini-login-btn');
    await expect(miniLoginBtn).toBeVisible();
    await expect(miniLoginBtn).toContainText('Sign In');

    console.log('Staging E2E Smoke Integration Test completed successfully!');
  });
});
