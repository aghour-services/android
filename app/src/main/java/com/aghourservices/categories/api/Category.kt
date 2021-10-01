package com.aghourservices.categories.api

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass


@RealmClass
open class Category :RealmObject() {
    @PrimaryKey
    var id:Int = 0
    var icon:String? = null
    var name : String? = null
}