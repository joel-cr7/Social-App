package com.example.socialapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.socialapp.daos.UserDao
import com.example.socialapp.databinding.ActivitySigninBinding
import com.example.socialapp.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

open class SigninActivity : AppCompatActivity() {

    lateinit var binding: ActivitySigninBinding
    private val RC_SIGN_IN: Int = 123
    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var authenticate: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authenticate = Firebase.auth

        //most of sign in code is from documentation for firebase google sign in

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.signinButton.setOnClickListener{
            signIn()
        }

        binding.btnFacebook.setOnClickListener{
            val intent = Intent(this, FacebookAuthActivity::class.java)
            startActivity(intent)
        }

    }

    //lifecycle method which is called after onCreate
    //checking if the user is already signed in or not. That checking will ultimately be done by updateUI() function
    //if not Signedin then 'user' variable will be null
    override fun onStart() {
        super.onStart()
        val user = authenticate.currentUser
        updateUI(user)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //add sha1 key to firebase before using any Signin function
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception
            if(task.isSuccessful){
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d("sign in Activity", "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w("sign in Activity", "Google sign in failed", e)
                }
            }
            else{
                Log.w("sign in Activity", exception.toString())
            }
        }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {

        val credentials = GoogleAuthProvider.getCredential(idToken,null)

        binding.signinButton.visibility = View.GONE
        binding.progressBar.visibility  = View.VISIBLE

        //now authenticate the user
        //done in background thread (use coroutines)
        GlobalScope.launch(Dispatchers.IO) {
            val auth = authenticate.signInWithCredential(credentials).await()   //await allows you to execute functions asynchronously while waiting for their results at a later point.
            val Firebaseuser = auth.user

            //update the ui (do it in main thread or else u get error)
            //shifting from background thread to main thread
            withContext(Dispatchers.Main){
                updateUI(Firebaseuser)
            }
        }

    }

    fun updateUI(firebaseuser: FirebaseUser?) {
        if (firebaseuser!=null){
            //these attributes of firebaseuser are inbuilt, and used to get info about the user who is logged in
            val user = firebaseuser.email?.let {
                User(firebaseuser.uid, firebaseuser.displayName,firebaseuser.photoUrl.toString(), it )
            }
            val usersDao = UserDao()
            if (user != null) {
                usersDao.addUser(user)
            }

            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        //if user not Loggedin
        else{
            binding.signinButton.visibility = View.VISIBLE
            binding.progressBar.visibility  = View.GONE
        }
    }
}