# User Model Bug Report

## Title  
Incorrect Role Assignment – All Users Defaulting to `User` Role

---

## Description  
In the initial version of the application, role assignment logic was incomplete. As a result, every user—regardless of their email domain—was being assigned the default `User` role. This caused a mismatch between backend logic, frontend expectations, and database records, particularly after `Sales Manager` and `Product Manager` roles were introduced into the system.

---

## Steps to Reproduce  
1. Start the backend and frontend servers.  
2. Register a new user or log in with an email ending in `@salesman.com` or `@prodman.com`.  
3. Observe the user role stored in the database and rendered on the frontend.  
4. Despite having a Sales/Product Manager email, the user is assigned the `User` role.

---

## Expected Behavior  
Users with specific email domains should be automatically assigned the correct role:
- `@salesman.com` → `Sales Manager`
- `@prodman.com` → `Product Manager`
- All others → `User`

---

## Actual Behavior  
All users are assigned the `User` role regardless of their email.

![Role Diagram](roles.png)


---

## Root Cause  
When the role assignment logic was initially implemented, only the `User` role existed. The backend’s `UserService` did not yet include logic to differentiate roles based on email domain. Although the frontend was later updated to expect and display different roles, the backend continued to return `User` for everyone, leading to inconsistencies.

---

## Resolution / Fix  
The issue was fixed by modifying the `UserService` in the backend to include role assignment logic based on email domains:

```java
private Role inferRoleFromEmail(String email) {
    String lower = email.toLowerCase();
    if (lower.endsWith("@salesman.com"))  
        return Role.salesManager;
    if (lower.endsWith("@prodman.com"))   
        return Role.productManager;
    return Role.User;
}
```
---

## Final Result
After applying these steps, the issue has been resolved successfully:
The admin pages for both `Sales Manager` and `Product Manager` now open correctly on the frontend based on the assigned roles.
The backend also returns the correct roles in responses, ensuring consistency between the system layers.

![Correct Role Diagram](/src/assets/images/correctroles.png)