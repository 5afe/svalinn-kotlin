# Accounts - GoEthereum

Module which provides Ethereum account support and management using [go-ethereum](https://github.com/ethereum/go-ethereum).

The main classes are `GethAccountsRepository` and `GethAccountsManager` which require a `KeyStore`.

To build that dependency:

```kotlin
KeyStore(<location>, Geth.LightScryptN, Geth.LightScryptP)
```
