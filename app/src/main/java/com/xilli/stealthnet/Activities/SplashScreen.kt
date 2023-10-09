package com.xilli.stealthnet.Activities

import android.animation.Animator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieDrawable
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xilli.stealthnet.Activities.MainActivity
import com.xilli.stealthnet.R
import com.xilli.stealthnet.Utils.Constants
import com.xilli.stealthnet.databinding.ActivitySplashBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import top.oneconnectapi.app.api.OneConnect
import java.io.IOException

class SplashScreen : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.lottieAnimationViewsplash.setAnimation(R.raw.loading_animation)
        binding.lottieAnimationViewsplash.repeatCount = LottieDrawable.INFINITE
        binding.lottieAnimationViewsplash.addAnimatorListener(object :
            Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {

                Handler().postDelayed({
                    startActivity(Intent(this@SplashScreen, MainActivity::class.java))
                    finish()
                }, 3000)
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })

        binding.lottieAnimationViewsplash.playAnimation()


        GlobalScope.launch(Dispatchers.IO) {

            Thread.sleep(5000)
            try {
                Log.d("MyApp", "Coroutine started")
                val oneConnect = OneConnect()
                oneConnect.initialize(
                    this@SplashScreen,
                    "2w0QvmDcWoNfZWTmEukdXSF8JAu4zLka2Br7u1iIvr9UE8Y6lw"
                )
                val response = oneConnect.fetch(true)
                Log.d("MyApp", "API Response: $response")
                try {
                    Constants.FREE_SERVERS = oneConnect.fetch(true)
                    Constants.PREMIUM_SERVERS = oneConnect.fetch(true)
                    Log.d(
                        "MyApp",
                        "Data fetched successfully"
                    ) // Log that data was fetched successfully
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e("MyApp", "IOException: " + e.message) // Log any IOException
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("MyApp", "Exception: " + e.message) // Log any other exceptions
            }
            Handler(Looper.getMainLooper()).post {
                startActivity(Intent(this@SplashScreen, MainActivity::class.java))
                finish()
            }
        }
    }
}
