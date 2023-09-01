package com.aghourservices.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.aghourservices.R
import com.aghourservices.data.model.Profile
import com.aghourservices.data.network.RetrofitInstance.userApi
import com.aghourservices.databinding.ActivityCreateArticleBinding
import com.aghourservices.utils.helper.AlertDialogs
import com.aghourservices.utils.helper.Constants.Companion.GALLERY_CODE
import com.aghourservices.utils.helper.Constants.Companion.REQUEST_CODE
import com.aghourservices.utils.helper.Intents
import com.aghourservices.utils.helper.Intents.getRealPathFromURI
import com.aghourservices.utils.helper.Intents.loadProfileImage
import com.aghourservices.utils.services.ArticleService
import com.aghourservices.utils.services.cache.UserInfo
import com.aghourservices.utils.services.cache.UserInfo.getProfile
import com.aghourservices.utils.services.cache.UserInfo.getUserData
import com.aghourservices.utils.services.cache.UserInfo.isUserLoggedIn
import com.aghourservices.utils.services.cache.UserInfo.saveProfile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class CreateArticleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateArticleBinding
    private lateinit var permissions: Array<String>
    private val isUserLogin by lazy { isUserLoggedIn(this@CreateArticleActivity) }
    private val user by lazy { getUserData(this@CreateArticleActivity) }
    private val profile by lazy { getProfile(this@CreateArticleActivity) }
    private var imageUri: Uri? = null
    private var imagePart: MultipartBody.Part? = null
    private var isVerified: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initPermissions()
        requestPermissions()
        getUserProfile()
        initUserClick()
        binding.userLayout.isVisible = isUserLogin
    }

    private fun openGallery() {
        val galleryIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent(MediaStore.ACTION_PICK_IMAGES)
        } else {
            Intent(Intent.ACTION_PICK)
        }
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, GALLERY_CODE)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_CODE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data!!
            val file = File(getRealPathFromURI(this, imageUri!!)!!)
            Intents.compressFile(this, file)
            val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            imagePart =
                MultipartBody.Part.createFormData("article[attachment]", file.name, requestBody)
            binding.articleImg.setImageURI(imageUri)
            binding.addImageBtn.text = "تغيير الصورة"
        }
    }

    private fun initPermissions() {
        permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_MEDIA_LOCATION,
        )
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == (PackageManager.PERMISSION_GRANTED)
        } else {
            true
        }
    }

    private fun initUserClick() {
        binding.addImageBtn.setOnClickListener {
            if (!checkStoragePermission()) {
                requestPermissions()
            } else {
                openGallery()
            }
        }
        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.apply {
            binding.articleEdt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun afterTextChanged(s: Editable?) {}

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    val articleTxt = binding.articleEdt.text.toString().trim()

                    if (TextUtils.isEmpty(articleTxt)) {
                        binding.publishBtn.isEnabled = false
                    } else {
                        binding.publishBtn.isEnabled = true
                        binding.publishBtn.setOnClickListener {
                            if (!isUserLogin) {
                                AlertDialogs.createAccount(
                                    this@CreateArticleActivity,
                                    "لإضافة خبر أعمل حساب الأول"
                                )
                            } else {
                                createArticle(articleTxt)
                            }
                        }
                    }
                }
            })
        }
    }

    private fun createArticle(description: String) {
        val articleService = ArticleService()
        articleService.create(
            this@CreateArticleActivity,
            user.token,
            UserInfo.getFCMToken(this@CreateArticleActivity),
            description,
            imagePart,
            isVerified
        )
        setArticleEmpty()
    }

    private fun getUserProfile() {
        val retrofitInstance = userApi.userProfile(user.token)
        retrofitInstance.enqueue(object : Callback<Profile> {
            override fun onResponse(
                call: Call<Profile>,
                response: Response<Profile>
            ) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    if (profile != null) {
                        saveProfile(
                            this@CreateArticleActivity,
                            profile.id!!,
                            profile.name,
                            profile.verified
                        )
                        loadProfileImage(
                            this@CreateArticleActivity,
                            profile.url,
                            binding.avatarImage
                        )
                        isVerified = profile.verified
                        binding.userName.apply {
                            text = profile.name
                            if (profile.verified && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                tooltipText = context.getString(R.string.verified)
                            } else {
                                setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                            }
                            visibility = View.VISIBLE
                        }
                    }
                }
            }

            override fun onFailure(
                call: Call<Profile>,
                t: Throwable
            ) {
                binding.userName.apply {
                    text = profile.name
                    if (profile.verified && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        tooltipText = context.getString(R.string.verified)
                    } else {
                        setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    }
                    visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setArticleEmpty() {
        binding.articleEdt.text!!.clear()
        binding.articleImg.setImageURI(null)
        onBackPressedDispatcher.onBackPressed()
    }
}