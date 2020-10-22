package com.example.projemanage.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projemanage.R
import com.example.projemanage.firebase.FirestoreClass
import com.example.projemanage.models.User
import com.example.projemanage.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.nav_header_main.*
import java.io.IOException


class MyProfileActivity : BaseActivity() {

    companion object{
        private const val READ_STORAGE_PERMISSION_CODE = 1
        private const val PICK_IMAGE_REQUEST_CODE = 2
    }

    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageURL: String = ""
    private lateinit var mUserDetails: User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
        setupActionBar()

        FirestoreClass().loadUserData(this)

        img_user_image_my_profile.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    ==PackageManager.PERMISSION_GRANTED){
                showImageChooser()

            }else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        btn_update_my_profile.setOnClickListener {
            if (mSelectedImageFileUri != null){
                uploadUserImage()
            }else{
                showprogressDialog(resources.getString(R.string.please_wait))

                updateUserProfileData()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // Todo show image chooser
                showImageChooser()
            }else{
                Toast.makeText(this,
                "Oops you just denied the permission for storage.",
                Toast.LENGTH_LONG).show()
            }
        }
    }
    private  fun showImageChooser(){
        var galleryIntent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK &&
            requestCode == PICK_IMAGE_REQUEST_CODE &&
            data!!.data != null) {
            mSelectedImageFileUri = data.data
            try {
                Glide
                    .with(this@MyProfileActivity)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(img_user_image_my_profile)
               }catch (e : IOException){
                e.printStackTrace()
            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_my_profile_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.my_profile_title)
        }
        toolbar_my_profile_activity.setNavigationOnClickListener { onBackPressed() }
    }

    fun setUserDataInUI(user : User){

        mUserDetails = user
        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(img_user_image_my_profile)

        edt_name_my_profile.setText(user.name)
        edt_email_my_profile.setText(user.email)
        if(user.mobile != 0L){
            edt_mobile_my_profile.setText(user.mobile.toString())
        }

    }

     private fun updateUserProfileData(){

         val userHashMap = HashMap<String, Any>()

         var anyChangesMade = false

         if (mProfileImageURL != null && mProfileImageURL != mUserDetails.image){
             userHashMap[Constants.IMAGE] = mProfileImageURL
             anyChangesMade = true
         }

         if (edt_name_my_profile.text.toString() != mUserDetails.name){
             userHashMap[Constants.NAME] = edt_name_my_profile.text.toString()
             anyChangesMade = true

         }

         if (edt_mobile_my_profile.text.toString() != mUserDetails.mobile.toString()){
             userHashMap[Constants.MOBILE] = edt_mobile_my_profile.text.toString().toLong()
             anyChangesMade = true

         }

         if (anyChangesMade){
             FirestoreClass().updateUserProfileData(this , userHashMap)
         }
    }

    private fun uploadUserImage(){
        showprogressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageFileUri != null){
            val sRef: StorageReference =
                FirebaseStorage.getInstance().reference
                    .child("User_IMAGE" +
                            System.currentTimeMillis() + "."+
                            getFileExtension(mSelectedImageFileUri))

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
                Log.i("Firebase Image URL",
                taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->
                    Log.i("Downladable Image URL", uri.toString())
                    mProfileImageURL = uri.toString()

                    hideProgressDialog()

                    updateUserProfileData()
                }
            }.addOnFailureListener {
                exception ->
                Toast.makeText(this@MyProfileActivity,
                    exception.message,
                    Toast.LENGTH_LONG).show()

                hideProgressDialog()
            }
        }
    }
    private fun getFileExtension(uri: Uri?): String?{
        return MimeTypeMap
            .getSingleton()
            .getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}