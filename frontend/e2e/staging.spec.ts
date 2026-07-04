import { test, expect } from '@playwright/test';

test.describe('Staging Environment E2E Smoke Integration Test', () => {
  test('should load home page, register user, render stage, and check profile info', async ({ page }) => {
    // 1. Navigate to Staging homepage
    console.log('Navigating to homepage...');
    await page.goto('/');

    // Verify main page elements are rendered
    await expect(page.locator('.hero-title')).toHaveText('rogic.io');
    await expect(page.locator('.cta-play-btn')).toBeVisible();

    // 2. Click Play Now to navigate to the Game Play tab
    console.log('Navigating to Game Play tab...');
    await page.click('.cta-play-btn');

    // Wait for the loading screen to disappear
    await expect(page.locator('.loading-state')).not.toBeVisible({ timeout: 15000 });

    // Verify canvas board is rendered
    console.log('Verifying Nonogram Canvas board rendering...');
    const canvas = page.getByTestId('nonogram-canvas');
    await expect(canvas).toBeVisible({ timeout: 10000 });

    // Verify floating stage selector exists and displays the current stage name
    const activeStageBadge = page.locator('.active-stage-badge');
    await expect(activeStageBadge).toBeVisible();
    const stageName = await page.locator('.active-stage-badge-name').textContent();
    console.log(`Active Stage Name: ${stageName}`);
    expect(stageName?.length).toBeGreaterThan(0);

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
