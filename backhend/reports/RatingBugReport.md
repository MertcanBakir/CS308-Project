
# Bug Report: Rating Visibility Delay after Comment Submission

##  Description
When a user submitted a comment with a rating, the rating was not immediately seen in the product page unless the comment was approved or rejected first. This delayed the updating of the ratings.

##  Steps to Reproduce
1. Log in as a user.
2. Submit a comment with a rating for a product using the `/comments/add` endpoint.
3. Without approving the comment, refresh the product page and observe the product rating.
4. Notice that the rating is unchanged, the new rating was not processed.

##  Expected Result
The submitted rating should immediately affect the product's average rating calculation, even if the content of the comment is pending approval.

##  Actual Result
The rating couldn't be seen alongside the comment and the product's rating remained unchanged until it was approved or rejected.

##  Root Cause
The system originally linked both rating visibility and comment content visibility to the `approved` part. Pending approval also blocked rating updates.

##  Fix
The logic in the `CommentController` was adjusted. In `/comments/product/{productId}`, all ratings are now included immediately when a comment is submitted, even if the comment is not yet approved.

```java
List<Map<String, Object>> commentList = comments.stream()
.map(comment -> {
Map<String, Object> commentMap = new HashMap<>();
    commentMap.put("rating", comment.getRating());
        return commentMap;
}).toList();
```
## Impact
- Ratings are now instantly updated with every comment submission.
- Product statistics and user experience are both improved and more reliable.

##  Status
**Fixed**

## Reported On
April 27, 2025

##  Reported By
Ece Gülkanat
