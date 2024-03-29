package com.aghourservices.ui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aghourservices.data.model.Tag
import com.aghourservices.data.network.RetrofitInstance.tagsApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TagsViewModel : ViewModel() {
    var tagsLiveData = MutableLiveData<ArrayList<Tag>>()
    var tagsList: ArrayList<Tag> = ArrayList()

    fun loadTags(categoryId: Int) {
        val retrofitBuilder = tagsApi.loadTags(categoryId)
        retrofitBuilder.enqueue(object : Callback<ArrayList<Tag>?> {
            override fun onResponse(
                call: Call<ArrayList<Tag>?>,
                response: Response<ArrayList<Tag>?>,
            ) {
                if (response.isSuccessful) {
                    tagsLiveData.value = response.body()
                    tagsList = tagsLiveData.value!!
                }
            }

            override fun onFailure(call: Call<ArrayList<Tag>?>, t: Throwable) {}
        })
    }
}