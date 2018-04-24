# Ticker

Module which provides fiat conversion support for crypto currencies.

The default implementation can be found in `DefaultTickerRepository`.

That dependency requires:

`TickerApi` which can be a Retrofit interface:
```kotlin
val retrofit = Retrofit.Builder()
            .client(<OkHttpClient instance>)	
            .baseUrl(TickerApi.BASE_URL)	
            .addConverterFactory(MoshiConverterFactory.create(<Moshi instance>))	
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))	
            .build()	
retrofit.create(TickerApi::class.java)
```

And an Android Room Database - `TickerDatabase`:
```kotlin
Room.databaseBuilder(<Context>, TickerDatabase::class.java, TickerDatabase.DB_NAME).build()
```
