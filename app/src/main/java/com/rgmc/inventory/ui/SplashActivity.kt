package com.rgmc.inventory.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.rgmc.inventory.databinding.ActivitySplashBinding
import com.rgmc.inventory.ui.viewmodel.SplashViewModel
import com.rgmc.inventory.util.ThemeManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val vm: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySplashTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadLogoFromAssets()
        animateEntrance()

        vm.loadData()

        lifecycleScope.launch {
            vm.state.collectLatest { state ->
                binding.progressBar.progress = state.progress
                binding.tvStatus.text = state.status
                if (state.isComplete) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }
        }
    }

    private fun loadLogoFromAssets() {
        try {
            val bitmap = BitmapFactory.decodeStream(assets.open("app-logo.png"))
            binding.ivLogo.setImageBitmap(bitmap)
        } catch (_: Exception) {
            // mipmap fallback already set in XML
        }
    }

    private fun animateEntrance() {
        binding.ivLogo.apply { alpha = 0f; scaleX = 0.85f; scaleY = 0.85f }
        binding.tvTitle.alpha = 0f
        binding.tvSubtitle.alpha = 0f
        binding.progressContainer.apply { alpha = 0f; translationY = 20f }

        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.ivLogo, View.ALPHA, 0f, 1f).apply { duration = 700 },
                ObjectAnimator.ofFloat(binding.ivLogo, View.SCALE_X, 0.85f, 1f).apply { duration = 700 },
                ObjectAnimator.ofFloat(binding.ivLogo, View.SCALE_Y, 0.85f, 1f).apply { duration = 700 },
                ObjectAnimator.ofFloat(binding.tvTitle, View.ALPHA, 0f, 1f).apply { startDelay = 350; duration = 500 },
                ObjectAnimator.ofFloat(binding.tvSubtitle, View.ALPHA, 0f, 1f).apply { startDelay = 450; duration = 500 },
                ObjectAnimator.ofFloat(binding.progressContainer, View.ALPHA, 0f, 1f).apply { startDelay = 600; duration = 400 },
                ObjectAnimator.ofFloat(binding.progressContainer, View.TRANSLATION_Y, 20f, 0f).apply { startDelay = 600; duration = 400 }
            )
            start()
        }
    }
}
