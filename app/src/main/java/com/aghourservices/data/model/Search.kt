package com.aghourservices.data.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class Search : RealmObject() {
    @PrimaryKey
    var id: Int = 0
    var category_id: Int = 0
    var name: String = ""
    var address: String = ""
    var description: String = ""
    var phone_number: String = ""
    var category_name: String = ""
}