package com.rgmc.inventory.ui.main

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rgmc.inventory.R
import com.rgmc.inventory.databinding.FragmentMainBinding
import com.rgmc.inventory.ui.viewmodel.ScannerViewModel
import com.google.android.material.R as MatR
import com.rgmc.inventory.util.ThemeManager
import com.rgmc.inventory.util.resolveAttrColor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val vm: ScannerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnScanner.setOnClickListener { findNavController().navigate(R.id.action_main_to_scannerSetup) }
        binding.btnProducts.setOnClickListener { findNavController().navigate(R.id.action_main_to_productMain) }
        binding.btnNotes.setOnClickListener { findNavController().navigate(R.id.action_main_to_notes) }
        binding.btnSigning.setOnClickListener { findNavController().navigate(R.id.action_main_to_signingList) }
        binding.btnExport.setOnClickListener { findNavController().navigate(R.id.action_main_to_export) }
        binding.btnOpenSessions.setOnClickListener { findNavController().navigate(R.id.action_main_to_openSessions) }
        binding.btnActiveCutoffs.setOnClickListener { findNavController().navigate(R.id.action_main_to_activeCutoffs) }
        binding.btnUpdateApp.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/folders/1uJxDnvHUz_s9qd6l0vs1tmmkoTMp8sFy?usp=drive_link")))
        }

        setupThemeToggle()

        viewLifecycleOwner.lifecycleScope.launch {
            vm.openSessions.collectLatest { sessions ->
                val count = sessions.size
                binding.tvSessionCount.text = count.toString()
                binding.tvSessionSubtitle.text = if (count == 1) "1 active session" else "$count active sessions"
            }
        }
    }

    private fun setupThemeToggle() {
        val currentMode = ThemeManager.getMode(requireContext())
        updateToggleUI(currentMode)
        binding.btnThemeMin.setOnClickListener { switchTheme(ThemeManager.MODE_MINIMALIST) }
        binding.btnThemeLgt.setOnClickListener { switchTheme(ThemeManager.MODE_LIGHT) }
        binding.btnThemeDrk.setOnClickListener { switchTheme(ThemeManager.MODE_DARK) }
    }

    private fun switchTheme(mode: Int) {
        if (ThemeManager.getMode(requireContext()) == mode) return
        ThemeManager.setMode(requireContext(), mode)
        requireActivity().recreate()
    }

    private fun updateToggleUI(activeMode: Int) {
        val ctx = requireContext()
        val primaryColor = ctx.resolveAttrColor(MatR.attr.colorPrimary)
        val secondaryText = ctx.resolveAttrColor(R.attr.colorTextSecondary)

        listOf(
            binding.btnThemeMin to ThemeManager.MODE_MINIMALIST,
            binding.btnThemeLgt to ThemeManager.MODE_LIGHT,
            binding.btnThemeDrk to ThemeManager.MODE_DARK
        ).forEach { (btn, mode) ->
            if (mode == activeMode) {
                btn.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(primaryColor)
                    cornerRadius = dpToPx(4f)
                }
                btn.setTextColor(Color.WHITE)
                btn.alpha = 1f
            } else {
                btn.setBackgroundColor(Color.TRANSPARENT)
                btn.setTextColor(secondaryText)
                btn.alpha = 0.65f
            }
        }
    }

    private fun dpToPx(dp: Float): Float =
        dp * requireContext().resources.displayMetrics.density

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
