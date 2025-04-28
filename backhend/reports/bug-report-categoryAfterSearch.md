#  Bug Report

## Title
**After searching a product, category buttons do not work**

## Environment
- **Operating System:** Windows 10
- **Browser:** Chrome 123.0.0
- **URL:** `localhost:3000`

## Preconditions
- A product must have been searched using the search bar on the homepage.

## Steps to Reproduce
1. Open the homepage.
2. Search for any product using the search bar.
3. After the search results are displayed, click on a category button to change the category.

## Expected Result
- Upon selecting a new category, products related to the selected category should be displayed.

## Actual Result
- The category buttons do not update the product list.
- The previously searched products stay visible on the screen.

## Severity
- **Medium**

## Root Cause
- After a search, the `searchResults` state is not cleared.
- When a category is selected, only `selectedCategory` was updated, but `searchResults` was still overriding the product list display.

## Fix
- In `Home.js`, within the `<Categories />` component, added `setSearchResults(null)` after setting the new category.
- This ensures that previous search results are cleared when a new category is selected.

### Fixed Code Snippet
```jsx
<Categories
  setSelectedCategory={(id) => {
    setSelectedCategory(id);
    setSearchResults(null); // Clear previous search results
  }}
/>
