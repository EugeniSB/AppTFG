package com.eugenisb.alphatest.auth

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.os.Bundle
import android.widget.Toast
import com.eugenisb.alphatest.R
import kotlinx.android.synthetic.main.activity_recover_password.*
import java.lang.Exception

class RecoverPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_password)


        sendPassButton.setOnClickListener {
           try {
               if(emailResetPassText.text.isNotEmpty()){
                   FirebaseAuth.getInstance().sendPasswordResetEmail(emailResetPassText.text.toString())
                   Toast.makeText(this, "Reset password email sent!!", Toast.LENGTH_SHORT).show()

               }
           }catch (e: Exception){
               Toast.makeText(this, "Reset password email sent!!", Toast.LENGTH_SHORT).show()
           }

        }

    }

}
