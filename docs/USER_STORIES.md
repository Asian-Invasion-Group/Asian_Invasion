# User Stories and Acceptance Criteria

These stories describe the implemented course-project scope. Priority uses **Must**, **Should**, and **Could**.

## Authentication

### US-A1 — Create an account (Must)
As a new user, I want to register with an email and password so that my sustainability records are private to me.

**Given** the registration form is open  
**When** I submit a valid, unused email and password  
**Then** an account is created and I can sign in with those credentials.

### US-A2 — Sign in and sign out (Must)
As a returning user, I want to sign in and sign out so that I can safely access and leave my dashboard.

**Given** an existing account  
**When** I enter the correct credentials and submit  
**Then** the main application opens; when I choose sign out, protected data is no longer shown.

## Overview

### US-O1 — See a sustainability snapshot (Must)
As a user, I want one overview screen so that I can quickly see what needs attention.

**Given** I am signed in  
**When** the overview loads  
**Then** it presents current grocery priorities, electricity insight, and weather action/status in one place.

## Food

### US-F1 — Track a grocery item (Must)
As a user, I want to record stock and expiry information so that I can reduce food waste.

**Given** the grocery form is open  
**When** I enter a name, category, positive quantity, unit, purchase date, and valid expiry date  
**Then** the item is saved to my inventory and appears in the table.

### US-F2 — Use food before it expires (Must)
As a user, I want expiry priorities and recommendations so that I know what to use first.

**Given** inventory items have expiry dates  
**When** the inventory is displayed  
**Then** items are labelled expired, today, expiring soon, or fresh, and a use-first recommendation is shown.

### US-F3 — Find an item (Should)
As a user, I want to search my inventory so that I can find a food quickly.

**Given** multiple grocery items exist  
**When** I type a search term  
**Then** the table filters to matching items.

## Electricity

### US-E1 — Record a monthly bill (Must)
As a user, I want to enter monthly kWh and bill amount so that I can monitor electricity use.

**Given** the electricity form is open  
**When** I submit a billing month, positive kWh, and non-negative bill amount  
**Then** the record is saved for my account and the chart/table refreshes.

### US-E2 — Receive reduction advice (Must)
As a user, I want a comparison and target so that I can reduce consumption.

**Given** at least two monthly records exist  
**When** electricity insights are calculated  
**Then** the app shows the month-over-month change and a next-month target 10% below the latest usage, with practical advice.

## Weather

### US-W1 — Load a seven-day forecast (Must)
As a user, I want to search a location and see a forecast so that recommendations reflect my local weather.

**Given** I enter a city or suburb  
**When** I request the forecast with an available network connection  
**Then** the app resolves the location and displays seven daily weather rows and recommendation statuses.

### US-W2 — Choose a drying day (Must)
As a user, I want clothes-drying guidance so that I can use natural energy instead of a dryer when practical.

**Given** a forecast is loaded  
**When** a day satisfies the drying thresholds  
**Then** it is marked suitable and the app explains the rain, sunshine, wind, and temperature reasoning.

### US-W3 — Choose a walking day (Should)
As a user, I want walking guidance so that I can plan a comfortable outdoor activity.

**Given** a forecast is loaded  
**When** a day satisfies the walking thresholds  
**Then** it is marked suitable, with UV/heat timing advice when relevant.

## Out of scope

- Mobile or web clients, push notifications, smart-meter hardware, automatic bill import, and utility-provider integrations.
- Receipt scanning, barcode recognition, recipe generation, nutrition advice, or food-safety guarantees.
- Cloud-hosted accounts, cross-device synchronisation, social sharing, and remote backups.
- Guaranteed offline forecasts, medical exercise advice, or weather alerts beyond the seven-day API response.
