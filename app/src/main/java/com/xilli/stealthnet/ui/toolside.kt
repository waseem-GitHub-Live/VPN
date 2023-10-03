package com.xilli.stealthnet.ui

import android.os.Bundle
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.android.play.core.review.ReviewManager
import com.infideap.drawerbehavior.Advance3DDrawerLayout
import com.xilli.stealthnet.R

abstract class toolside : AppCompatActivity() {
    private lateinit var manager :ReviewManager
    protected var toolbar: Toolbar? = null
        private set

    @get:LayoutRes
    protected abstract val layoutRes: Int

    private var mDrawerLayout: Advance3DDrawerLayout? = null
    private var navigationView: NavigationView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutRes)

//        toolbar = findViewById(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
        }


//        val category = findViewById<ImageView?>(R.id.category)
//        category?.setOnClickListener {
//            mDrawerLayout?.openDrawer(GravityCompat.START, true)
//        }

        mDrawerLayout?.setViewRotation(GravityCompat.START, 15F)
    }



    override fun onBackPressed() {
        if (mDrawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout?.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }



}