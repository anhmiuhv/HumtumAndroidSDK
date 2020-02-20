package com.demo.linhthoang.securitypoke.Model

data class FriendRequestMessage(
    var sent: Array<FriendRequest> = emptyArray(),
    var received: Array<FriendRequest> = emptyArray()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FriendRequestMessage

        if (!sent.contentEquals(other.sent)) return false
        if (!received.contentEquals(other.received)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sent.contentHashCode()
        result = 31 * result + received.contentHashCode()
        return result
    }
}