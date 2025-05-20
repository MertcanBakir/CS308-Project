# Bug Report: Unauthorized product deletion by SalesManager

## Description
The system allows users with the `SalesManager` role to delete products, even though they should not have this permission. This leads to unauthorized data loss and breaks separation of responsibilities between roles.

## Steps to Reproduce
1. Log in as a user with the `SalesManager` role.
2. Make a DELETE request to `/products/{id}` for any existing product.
3. Observe that the deletion is processed successfully and the product is removed.

## Expected Result
Only users with the `ProductManager` role should be able to delete products. A user with any other role should receive a `403 Forbidden` response.

## Actual Result
SalesManager is able to delete products without restriction. No role-based access control is enforced.

## Root Cause
There is no authorization check (such as role verification) in the controller or service layer for the delete operation.

## Suggested Fix
Add a role check using either:
- Spring Security's `@PreAuthorize("hasRole('ProductManager')")`
- Or a manual role validation in the controller method.

## Impact
- High-risk data loss.
- Breach of business logic and role boundaries.
- Reduces confidence in the access control model.

## Status
Fixed

## Reported On
May 21, 2025

## Reported By
Defne Koçulu
