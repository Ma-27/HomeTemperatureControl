package com.hometemperature.util.itembuild

import com.hometemperature.bean.flag.MessageType
import com.hometemperature.bean.flag.TransmissionStatus
import com.hometemperature.bean.item.DataItem

class DataItemBuilder {
    companion object {
        fun buildDataItem(data: String): DataItem {
            return DataItem(
                System.currentTimeMillis(),
                "",
                data,
                MessageType.PENDING,
                TransmissionStatus.UNKNOWN
            )
        }
    }
}