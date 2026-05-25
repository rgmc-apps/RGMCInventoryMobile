package com.rgmc.inventory.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.rgmc.inventory.R
import com.rgmc.inventory.databinding.ActivityMainBinding
import com.rgmc.inventory.ui.viewmodel.ScannerViewModel
import com.rgmc.inventory.util.ErrorReporter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val PERM_REQUEST_CODE = 100
    private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    private val scannerViewModel: ScannerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController
        requestPermissionsIfNeeded()
        observeErrorReports()
    }

    override fun onStop() {
        super.onStop()
        scannerViewModel.updateSessionActivity()
    }

    private fun observeErrorReports() {
        lifecycleScope.launch {
            scannerViewModel.errorReport.collectLatest { report ->
                val intent = ErrorReporter.buildEmailIntent(report)
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(Intent.createChooser(intent, "Send Error Report"))
                }
            }
        }
    }

    private fun requestPermissionsIfNeeded() {
        val denied = REQUIRED_PERMISSIONS.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (denied.isNotEmpty()) ActivityCompat.requestPermissions(this, denied.toTypedArray(), PERM_REQUEST_CODE)
    }

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp() || super.onSupportNavigateUp()
}
