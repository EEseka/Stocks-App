package com.example.stocks.data.csv

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.stocks.data.local.IntradayInfoEntity
import com.example.stocks.data.mapper.toIntradayInfo
import com.example.stocks.domain.model.IntradayInfo
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntradayInfosParser @Inject constructor() : CSVParser<IntradayInfo> {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun parse(stream: InputStream): List<IntradayInfo> {
        val csvReader = CSVReader(InputStreamReader(stream))
        return withContext(Dispatchers.IO) {
            csvReader.use { reader ->
                reader
                    .readAll()
                    .drop(1)  // Skip the header row
                    .mapNotNull { line ->
                        val timeStamp = line.getOrNull(0) ?: return@mapNotNull null
                        val close = line.getOrNull(4) ?: return@mapNotNull null
                        val dto = IntradayInfoEntity(
                            timeStamp = timeStamp,
                            close = close.toDouble(),
                            symbol = "",
                            localTimestamp = 0L
                        )
                        dto.toIntradayInfo()
                    }
                    .filter { info ->
                        info.date.toLocalDate() == getLastTradingDay()
                    }
                    .sortedBy { it.date.hour }
                // Using .use on CSVReader:
                // Automatically handles closing the reader even if an exception occurs
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getLastTradingDay(): LocalDate {
        val today = LocalDate.now()
        return when (today.dayOfWeek) {
            DayOfWeek.SUNDAY -> today.minusDays(2)
            DayOfWeek.MONDAY -> today.minusDays(3)
            else -> today.minusDays(1)
        }
    }

}
