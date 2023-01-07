package com.hometemperature.util.itembuild

import com.hometemperature.bean.flag.MessageType
import com.hometemperature.bean.flag.TransmissionStatus
import com.hometemperature.bean.item.DataItem
import timber.log.Timber

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

        //将+12.1 字符串去掉+号
        fun formatTemperature(temperature: String): String {
            var floatValue: String = ""
            try {
                floatValue = temperature.replace("+", "")
            } catch (e: NumberFormatException) {
                Timber.w("无法将字符串转换为浮点数")
            }
            return floatValue
        }

        //数据中心的提示信息，提示app发送或者接收了什么数据
        fun determineEventString(data: String, type: Int): String {
            var event: String = ""
            //发送数据时，显示提示信息设置
            if (type == MessageType.MESSAGE_SEND) {
                event = when (data) {
                    "1" -> "开启风扇"
                    "2" -> "关闭风扇"
                    "3" -> "房间加热系统开启"
                    "4" -> "房间加热系统关闭"
                    else -> "发送内容："
                }


                if (data.toIntOrNull() in 15..30) {
                    event = "设定室内温度：${data.toInt()}度"
                }

                //收到数据后，在数据中心显示的提示信息
            } else if (type == MessageType.MESSAGE_RECEIVE) {
                event = when (data) {
                    "1" -> "当前温度:"
                    else -> "收到新数据："
                }
            }

            return event
        }
    }
}