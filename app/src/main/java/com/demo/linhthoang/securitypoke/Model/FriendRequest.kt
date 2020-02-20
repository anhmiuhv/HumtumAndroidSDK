package com.demo.linhthoang.securitypoke.Model

// {"sent":[],
// "received":[{"id":3,"status":"PENDING","relationship_type":"FRIEND",
// "sender":{"id":4,"name":"d@gmail.com","email":"d@gmail.com","avatar":null
// "receiver":{"id":1,"name":"anhmiuhv@yahoo.com.vn","email":"anhmiuhv@yahoo.com.vn","avatar":null}}]}
data class FriendRequest(
    var id: Int?, var status: String?,
    var relationship_type: String?,
    var sender: User?,
    var receiver: User?
)