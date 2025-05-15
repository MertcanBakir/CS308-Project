# Bug Report: Refund Button Visibility Ignoring 30-Day Rule

## Description
The refund functionality checks the 30-day eligibility rule only on the backend. On the frontend order history, the `refund order` button is visible for all `DELIVERED` orders, even if it's older than 30 days. 

## Steps to Reproduce
1. Log in as a customer.
2. Go to the profile/order history page.
3. Locate a product that was delivered more than 30 days ago.
4. Click the Refund button.
5. Observe the backend error response.

## Expected Result
If the order is older than 30 days, the refund button should not be displayed on the frontend.

## Actual Result
The refund button is shown for all `DELIVERED` orders regardless of delivery date. When clicked, the backend responds with:
```json
{
  "message": "Refund not allowed."
}
```

## Root Cause
The frontend logic checks only the `status === "DELIVERED"` condition. It does not validate the `createdAt` timestamp against the 30-day rule.

## Fix
Update frontend logic to compare the order's creation date with the current date. Hide the `refund order` button if the order is older than 30 days.

```javascript
{order.status === "DELIVERED" &&
new Date(order.createdAt) >= new Date(Date.now() - 30 * 24 * 60 * 60 * 1000) && (
    <button
        onClick={() => handleRefundOrder(order.id)}
        className="refund-button"
    >
        Refund Order
    </button>
)}
```

## Impact
- Reduces backend errors from ineligible refund attempts
- Aligns frontend behavior with backend business logic

##  Status
**Fixed**

## Reported On
May 15, 2025

##  Reported By
Ece Gülkanat