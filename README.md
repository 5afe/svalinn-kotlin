# Svalinn Android

[![Jitpack](https://jitpack.io/v/gnosis/svalinn-kotlin.svg)](https://jitpack.io/#gnosis/svalinn-kotlin)
[![Build Status](https://travis-ci.org/gnosis/svalinn-kotlin.svg?branch=master)](https://travis-ci.org/gnosis/svalinn-kotlin)
[![codecov](https://codecov.io/gh/gnosis/svalinn-kotlin/branch/master/graph/badge.svg)](https://codecov.io/gh/gnosis/svalinn-kotlin)

🛡️❄️️ Kotlin libraries for Ethereum based Android development

**WARNING: Under development. Libraries right now target the Rinkeby test network. Switching to mainnet (or any other ethereum network) can be done by the user but it's its responsibility in doing so.**

### Goal

This is a collection of libraries that should make development of apps that interact with Ethereum easier. **Some functionality should be moved to [Kethereum](https://github.com/walleth/kethereum)**

### Download

Follow instructions on https://jitpack.io/#gnosis/svalinn-kotlin

In your Gradle file:

**Accounts Module**

Main repository to create an account and load active accounts and to sign data with those accounts.

Two main implementations are available:

`accounts-kethereum` - which uses some of the utilities of the Kethereum project to manage accounts.

```
implementation 'com.github.gnosis:svalinn-kotlin:accounts-kethereum:<version>'
```

`accounts-geth` - which uses [go-ethereum](https://github.com/ethereum/go-ethereum) to manage/create/sign transactions.

```
implementation 'com.github.gnosis:svalinn-kotlin:accounts-geth:<version>'
```

**Android Common Module**

This module has Android specific utilities that we use in our mobile applications. We are in the process of refactoring and abstracting this module even more.

```
implementation 'com.github.gnosis:svalinn-kotlin:android-common:<version>'
```

**Blockies Module**

Provides the Ethereum Blockies implementation. Also contains an Android `ImageView` so it can be used easily in Android applications.

```
implementation 'com.github.gnosis:svalinn-kotlin:blockies:<version>'
```

**Crypto Module**

Crypto specific module for key generation, signing and hashing.

```
implementation 'com.github.gnosis:svalinn-kotlin:crypto:<version>'
```

**Ethereum Modules**

Modules for easy interaction with the Ethereum blockchain

[More info](ethereum/README.md)

```
implementation 'com.github.gnosis:svalinn-kotlin:ethereum:<version>'
implementation 'com.github.gnosis:svalinn-kotlin:ethereum-rpc:<version>'
implementation 'com.github.gnosis:svalinn-kotlin:ethereum-rpc-retrofit:<version>'
```

**Mnemonic Module**

Implementation of BIP39 for mnemonic phrase generation. Words need to be provided by the user in the core module by implementing the `WordListProvider` interface.

```
implementation 'com.github.gnosis:svalinn-kotlin:mnemonic:<version>'
```

If you wish to use BIP39 on Android you only need to include the `mnemonic-android` module which already provides the English and Chinese word lists via Resources.raw.

```
implementation 'com.github.gnosis:svalinn-kotlin:mnemonic-android:<version>'
```

**Models Module**

Our internal model for representing entities in Ethereum and Android.

```
implementation 'com.github.gnosis:svalinn-kotlin:models:<version>'
```

**Security Module**

Android utils to encrypt/decrypt data and manage app security features such as unlocked status and fingerprint registry.

```
implementation 'com.github.gnosis:svalinn-kotlin:security:<version>'
```

**Ticker Module**

Our fiat conversion module.

```
implementation 'com.github.gnosis:svalinn-kotlin:ticker:<version>'
```

**Utils Module**

General utilities that we use across our apps.

```
implementation 'com.github.gnosis:svalinn-kotlin:utils:<version>'
```

**Utils Testing Module**

General utilities for testing our apps.

```
implementation 'com.github.gnosis:svalinn-kotlin:utils-testing:<version>'
```
