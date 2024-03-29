package com.aghourservices.ui.viewModels

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aghourservices.data.model.Article
import com.aghourservices.data.network.RetrofitInstance.articlesApi
import com.aghourservices.data.network.RetrofitInstance.likeApi
import com.aghourservices.databinding.FragmentPublishedArticlesBinding
import com.aghourservices.ui.adapters.PublishedArticlesAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PublishedArticlesViewModel : ViewModel() {
    var newsLiveData = MutableLiveData<ArrayList<Article>>()
    var newsList: ArrayList<Article> = ArrayList()

    fun loadArticles(
        binding: FragmentPublishedArticlesBinding,
        userToken: String,
        fcmToken: String
    ) {
        val retrofitBuilder = articlesApi.loadArticles(userToken, fcmToken)

        retrofitBuilder.enqueue(object : Callback<ArrayList<Article>?> {
            override fun onResponse(
                call: Call<ArrayList<Article>?>,
                response: Response<ArrayList<Article>?>,
            ) {
                if (response.isSuccessful) {
                    newsLiveData.value = response.body()
                    newsList = newsLiveData.value!!
                }
            }

            override fun onFailure(call: Call<ArrayList<Article>?>, t: Throwable) {
                binding.apply {
                    newsShimmer.isVisible = false
                    noInternet.isVisible = true
                }
            }
        })
    }

    fun draftArticles(context: Context, userToken: String, fcmToken: String) {
        val retrofitBuilder = articlesApi.draftArticles(userToken, fcmToken)

        retrofitBuilder.enqueue(object : Callback<ArrayList<Article>?> {
            override fun onResponse(
                call: Call<ArrayList<Article>?>,
                response: Response<ArrayList<Article>?>,
            ) {
                if (response.isSuccessful) {
                    newsLiveData.value = response.body()
                    newsList = newsLiveData.value!!
                }
            }

            override fun onFailure(call: Call<ArrayList<Article>?>, t: Throwable) {}
        })
    }

    fun deleteArticle(
        context: Context,
        userToken: String,
        publishedArticlesAdapter: PublishedArticlesAdapter,
        position: Int,
        fcmToken: String
    ) {
        val articleId = publishedArticlesAdapter.getArticle(position).id

        val retrofitBuilder = articlesApi.deleteArticle(
            articleId,
            userToken,
            fcmToken
        )

        retrofitBuilder.enqueue(object : Callback<Article> {
            override fun onResponse(call: Call<Article>, response: Response<Article>) {
                if (response.isSuccessful) {
                    publishedArticlesAdapter.deleteArticle(position)
                    Toast.makeText(context, "تم مسح الخبر", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Article>, t: Throwable) {}
        })
    }

    fun likeArticle(
        userToken: String,
        publishedArticlesAdapter: PublishedArticlesAdapter,
        position: Int
    ) {
        val article = publishedArticlesAdapter.getArticle(position)
        article.liked = true

        val request = likeApi.likeArticle(article.id, userToken)

        request.enqueue(object : Callback<Article> {
            override fun onResponse(call: Call<Article>, response: Response<Article>) {
                if (response.isSuccessful) {
                    val updatedArticle = response.body()
                    if (updatedArticle != null) {
                        publishedArticlesAdapter.updateArticle(position, updatedArticle)
                    }
                }
            }

            override fun onFailure(call: Call<Article>, t: Throwable) {
                Log.e("LIKE", "onFailure: ${t.message}")
            }
        })
    }

    fun unLikeArticle(
        userToken: String,
        publishedArticlesAdapter: PublishedArticlesAdapter,
        position: Int
    ) {
        val article = publishedArticlesAdapter.getArticle(position)
        article.liked = false
        val request = likeApi.unLikeArticle(article.id, userToken)

        request.enqueue(object : Callback<Article> {
            override fun onResponse(call: Call<Article>, response: Response<Article>) {
                if (response.isSuccessful) {
                    val updatedArticle = response.body()
                    if (updatedArticle != null) {
                        publishedArticlesAdapter.updateArticle(position, updatedArticle)
                    }
                }
            }

            override fun onFailure(call: Call<Article>, t: Throwable) {
                Log.e("LIKE", "onFailure: ${t.message}")
            }
        })
    }
}