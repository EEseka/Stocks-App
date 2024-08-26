package com.example.stocks.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.stocks.data.local.IntradayInfoEntity
import com.example.stocks.domain.model.IntradayInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
fun IntradayInfoEntity.toIntradayInfo(): IntradayInfo {
    val pattern = "yyyy-MM-dd HH:mm:ss"
    val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
    val localDateTime = LocalDateTime.parse(timeStamp, formatter)
    return IntradayInfo(
        date = localDateTime,
        close = close
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun IntradayInfo.toIntradayInfoEntity(symbol: String, localTimestamp: Long): IntradayInfoEntity {
    val pattern = "yyyy-MM-dd HH:mm:ss"
    val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
    return IntradayInfoEntity(
        timeStamp = date.format(formatter),
        close = close,
        symbol = symbol,
        localTimestamp = localTimestamp
    )
}