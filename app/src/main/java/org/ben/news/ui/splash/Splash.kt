package org.ben.news.ui.splash

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import org.ben.news.R
import org.ben.news.ui.auth.Login
import pl.droidsonroids.gif.GifImageView


class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val imageView = findViewById<GifImageView>(R.id.splashy)
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                Glide.with(this).load(R.drawable.animbla).into(imageView)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                Glide.with(this).load(R.drawable.animw).into(imageView)
            }
        }
        supportActionBar?.hide()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}