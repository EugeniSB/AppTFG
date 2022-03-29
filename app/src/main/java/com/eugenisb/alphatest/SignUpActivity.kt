package com.eugenisb.alphatest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        title = "Sign up"

        val cancelButton = findViewById<Button>(R.id.cancelSignupButton)
        cancelButton.setOnClickListener {
            //cancelSignUp()
            onBackPressed()
        }

        val signupButton = findViewById<Button>(R.id.loginButton)
        signupButton.setOnClickListener{
            signUp()
        }

    }


    private fun cancelSignUp(){

        val authIntent = Intent(this, AuthActivity::class.java)
        startActivity(authIntent)
    }


    private fun signUp(){

        if(validateFullName() && validateUsername() && validateEmail()
            && validatePhoneNumber() && validatePassword()) {

            val email = findViewById<EditText>(R.id.emailEditText).text.toString()
            val name = findViewById<EditText>(R.id.fullnameEditText).text.toString()
            val username = findViewById<EditText>(R.id.usernameEditText).text.toString()
            val phone = findViewById<EditText>(R.id.editTextPhone).text.toString()
            val password = findViewById<EditText>(R.id.passEditText).text.toString()

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if(it.isSuccessful) {

                    db.collection("users").document(email).set(
                        hashMapOf("name" to name, "username" to username, "phone" to phone)
                    )
                    val homeIntent = Intent(this, HomeActivity::class.java).apply {
                        putExtra("email", email)
                    }
                    startActivity(homeIntent)
                } else {
                    val alertFullname = AlertDialog.Builder(this)
                    alertFullname.setTitle("Sign up error")
                    alertFullname.setMessage(it.exception?.message)
                    alertFullname.setPositiveButton("Okay", null)
                    alertFullname.show()
                }
            }
        }
    }

    private fun validateFullName() : Boolean {
        val fullName = findViewById<EditText>(R.id.fullnameEditText).text.toString()
        //var fullNamePattern:Pattern = Pattern.compile("/^[a-zA-ZàáâäãåąčćęèéêëėįìíîïłńòóôöõøùúûüųūÿýżźñçčšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕØÙÚÛÜŲŪŸÝŻŹÑßÇŒÆČŠŽ∂ð ,.'-]+\$/u")
        //var itsName:Matcher = fullNamePattern.matcher(fullName)

        if(fullName.isNotEmpty()){
            return true
        }else{
            val alertFullname = AlertDialog.Builder(this)
            alertFullname.setTitle("Error full name")
            alertFullname.setMessage("Full name can not be empty, please try again")
            alertFullname.setPositiveButton("Okay", null)
            alertFullname.show()
            return false
        }
    }

    private fun validateUsername(): Boolean{
        val username = findViewById<EditText>(R.id.usernameEditText).text.toString()

        if(username.isNotEmpty()){
            return true
        }else{
            val alertFullname = AlertDialog.Builder(this)
            alertFullname.setTitle("Error username")
            alertFullname.setMessage("Username can not be empty, please try again")
            alertFullname.setPositiveButton("Okay", null)
            alertFullname.show()
            return false
        }
    }

    private fun validateEmail() : Boolean {

        val email = findViewById<EditText>(R.id.emailEditText).text.toString()
        if(email.isNotEmpty() &&
            Patterns.EMAIL_ADDRESS.matcher(email).matches()){ return true
        }else{
            val alertEmail = AlertDialog.Builder(this)
            alertEmail.setTitle("Error email")
            alertEmail.setMessage("Incorrect email, please try again")
            alertEmail.setPositiveButton("Okay", null)
            alertEmail.show()
            return false
        }
    }

    private fun validatePhoneNumber() : Boolean{
        val phoneNum = findViewById<EditText>(R.id.editTextPhone).text.toString()

        if(phoneNum.isNotEmpty() && Patterns.PHONE.matcher(phoneNum).matches())
            return true
        else{
            val alertPhoneNum = AlertDialog.Builder(this)
            alertPhoneNum.setTitle("Error phone number")
            alertPhoneNum.setMessage("Incorrect phone number, please try again")
            alertPhoneNum.setPositiveButton("Okay", null)
            alertPhoneNum.show()
            return false
        }
    }

    private fun validatePassword(): Boolean{
        val password = findViewById<EditText>(R.id.passEditText).text.toString()

        if(password.isNotEmpty()){
            return true
        }else{
            val alertFullname = AlertDialog.Builder(this)
            alertFullname.setTitle("Error password")
            alertFullname.setMessage("Password can not be empty, please try again")
            alertFullname.setPositiveButton("Okay", null)
            alertFullname.show()
            return false
        }
    }

}