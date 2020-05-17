package com.demo.linhthoang.humtum

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.util.*


@Serializable
data class HumtumMessage(val sender_id: String?, val description: String?, val app_id: String?,
                         val payload: JsonObject, val targets: Array<Long>?) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HumtumMessage

        if (sender_id != other.sender_id) return false
        if (description != other.description) return false
        if (app_id != other.app_id) return false
        if (payload != other.payload) return false
        if (targets != null) {
            if (other.targets == null) return false
            if (!targets.contentEquals(other.targets)) return false
        } else if (other.targets != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sender_id?.hashCode() ?: 0
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (app_id?.hashCode() ?: 0)
        result = 31 * result + payload.hashCode()
        result = 31 * result + (targets?.contentHashCode() ?: 0)
        return result
    }
}
