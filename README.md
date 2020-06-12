# LibraryFind
Update *api* and *libraryGroupId* in Main.java

Open the command line:

Run `mvn compile`

Run `mvn exec:java`

# Limits
https://developer.github.com/v3/#rate-limiting

With this code you can only request 60 times per minute

If you want to call more then use this line,
```java
con.setRequestProperty("Authorization", "token my_access_token");
```
Replace `my_access_token` with you github access token 
