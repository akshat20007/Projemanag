package com.example.projemanage.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.example.projemanage.R
import com.example.projemanage.models.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*


class SignInActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        setupActionBar()

        auth = FirebaseAuth.getInstance()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        btnSignIn.setOnClickListener {
            checkUser() }
    }

    fun signInSuccess(user: User){
        hideProgressDialog()
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_sign_in_activity)

        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)

        }


        toolbar_sign_in_activity.setNavigationOnClickListener { onBackPressed() }
    }
    private fun validateForm( email: String, password: String) : Boolean {

        return when {

            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter a email address")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password")
                false
            }
            else -> {
                true
            }
        }
    }

    private fun checkUser(){

        val email: String = edtEmailSignIn.text.toString().trim{ it <= ' '}
        val password: String = edtPasswordSignIn.text.toString().trim{ it <= ' '}

        if (validateForm(email,password)){
            showprogressDialog(resources.getString(R.string.please_wait))
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener{
                task->
                hideProgressDialog()
                if (task.isSuccessful){
                Toast.makeText(this,"Sign In Successful",
                    Toast.LENGTH_SHORT).show()
                val user = auth.currentUser
                    startActivity(Intent(this,MainActivity::class.java))


            }else{
                Toast.makeText(this,"Sign In Failed",
                Toast.LENGTH_SHORT).show()
            }
            }
        }
    }
}