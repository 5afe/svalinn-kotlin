# Accounts - Kethereum

Module which provides Ethereum account support and management using [Kethereum](https://github.com/walleth/kethereum).

The main class is `KethereumAccountsRepository` which requires an Android Room database (`AccountsDatabase`).

To build that dependency:

```kotlin
Room.databaseBuilder(<Context>, AccountsDatabase::class.java, AccountsDatabase.DB_NAME).build()
```
