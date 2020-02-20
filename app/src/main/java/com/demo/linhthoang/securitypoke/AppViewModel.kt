package com.demo.linhthoang.securitypoke

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demo.linhthoang.securitypoke.Model.FriendRequestMessage
import com.demo.linhthoang.securitypoke.Model.User

class AppViewModel : ViewModel() {
    val users: MutableLiveData<Array<User>> by lazy {
        MutableLiveData<Array<User>>(arrayOf())
    }

    val profile: MutableLiveData<String> by lazy {
        MutableLiveData("")
    }

    val friendRequest: MutableLiveData<FriendRequestMessage> by lazy {
        MutableLiveData(FriendRequestMessage(arrayOf(), arrayOf()))
    }

    val messages: MutableLiveData<Array<String>> by lazy {
        MutableLiveData(emptyArray<String>())
    }

}