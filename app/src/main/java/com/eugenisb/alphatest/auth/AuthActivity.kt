package com.eugenisb.alphatest.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.eugenisb.alphatest.profileAndHome.HomeActivity
import com.eugenisb.alphatest.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        title = "Log in"

        val loginButton = findViewById<Button>(R.id.loginButton)

        signup()
        loginButton.setOnClickListener{
            login()
        }

        recoverPassText.setOnClickListener {
            val resPassIntent = Intent(this, RecoverPasswordActivity::class.java)
            startActivity(resPassIntent)
        }


    }

    private fun signup(){

        val signupButton = findViewById<Button>(R.id.signupButton)

        signupButton.setOnClickListener{
            val signupIntent = Intent(this, SignUpActivity::class.java)
            startActivity(signupIntent)
        }
    }

    private fun login(){

        val email = findViewById<EditText>(R.id.emailEditText).text.toString()
        val password = findViewById<EditText>(R.id.passEditText).text.toString()

        if(email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (password.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener {

                    if (it.isSuccessful){
                        db.collection("users").whereEqualTo("email", email).get().addOnSuccessListener {
                            docs ->
                            for(doc in docs) {
                                val homeIntent = Intent(this, HomeActivity::class.java)
                                homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(homeIntent)
                            }
                        }
                    }
                    else {
                        val alertPassword = AlertDialog.Builder(this)
                        alertPassword.setTitle("Error log in")
                        alertPassword.setMessage(it.exception?.message)
                        alertPassword.setPositiveButton("Okay", null)
                        alertPassword.show()
                    }
                }

            }else{
                val alertPassword = AlertDialog.Builder(this)
                alertPassword.setTitle("Error password")
                alertPassword.setMessage("Password can not be empty, please try again")
                alertPassword.setPositiveButton("Okay", null)
                alertPassword.show()
            }
        } else {
                val alertEmail = AlertDialog.Builder(this)
                alertEmail.setTitle("Error email")
                alertEmail.setMessage("Incorrect email, please try again")
                alertEmail.setPositiveButton("Okay", null)
                alertEmail.show()
        }
    }
}

