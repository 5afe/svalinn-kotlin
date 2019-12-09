package pm.gnosis.ticker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pm.gnosis.ticker.data.db.models.CurrencyDb

@Database(entities = [CurrencyDb::class], version = 1)
@TypeConverters(BigDecimalConverter::class, FiatSymbolConverter::class)
abstract class TickerDatabase : RoomDatabase() {
    companion object {
        const val DB_NAME = "gnosis-ticker-db"
    }

    abstract fun tickerDao(): TickerDao
}
