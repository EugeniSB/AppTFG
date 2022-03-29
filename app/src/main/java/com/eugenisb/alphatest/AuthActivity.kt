package com.eugenisb.alphatest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        title = "Log in"
        val test = 1
        val loginButton = findViewById<Button>(R.id.loginButton)

        signup()
        loginButton.setOnClickListener{
            login()
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
                        val loginIntent = Intent(this, HomeActivity::class.java).apply {
                            putExtra("email", email)
                        }
                        startActivity(loginIntent)
                    }
                    else {
                        val alertPassword = AlertDialog.Builder(this)
                        alertPassword.setTitle("Error log in")
                        alertPassword.setMessage("Incorrect email or password, please try again")
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