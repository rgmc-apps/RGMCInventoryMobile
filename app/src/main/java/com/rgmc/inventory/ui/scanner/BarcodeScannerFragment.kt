package com.rgmc.inventory.ui.scanner

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.animation.LinearInterpolator
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.rgmc.inventory.R
import com.rgmc.inventory.databinding.FragmentBarcodeScannerBinding
import com.rgmc.inventory.ui.adapter.ScanHistoryAdapter
import com.rgmc.inventory.ui.adapter.ScanHistoryItem
import com.rgmc.inventory.ui.adapter.ScanStatus
import com.rgmc.inventory.ui.viewmodel.ScannerViewModel
import com.rgmc.inventory.util.resolveAttrColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class BarcodeScannerFragment : Fragment() {
    private var _binding: FragmentBarcodeScannerBinding? = null
    private val binding get() = _binding!!
    private val vm: ScannerViewModel by activityViewModels()
    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null
    private val isProcessing = AtomicBoolean(false)
    private var cooldownAnimator: ObjectAnimator? = null
    private var cooldownJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private val scanner = BarcodeScanning.getClient()
    private var isTorchOn = false

    private val scanHistory = mutableListOf<ScanHistoryItem>()
    private lateinit var historyAdapter: ScanHistoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBarcodeScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()

        // Scan history RecyclerView
        historyAdapter = ScanHistoryAdapter()
        binding.rvScanHistory.adapter = historyAdapter
        binding.rvScanHistory.layoutManager = LinearLayoutManager(requireContext())

        // Toggle history panel via badge and close button
        binding.btnToggleHistory.setOnClickListener { toggleHistoryPanel() }
        binding.btnCloseHistory.setOnClickListener { toggleHistoryPanel() }

        // RESCAN: cancel the running cooldown and immediately allow a new scan
        binding.btnRescan.setOnClickListener {
            cooldownJob?.cancel()
            cooldownJob = null
            cooldownAnimator?.cancel()
            binding.scanResultCard.isVisible = false
            binding.scanCooldownProgress.isVisible = false
            isProcessing.set(false)
        }

        // Observe scan results — use collect (not collectLatest) so every scan is recorded
        viewLifecycleOwner.lifecycleScope.launch {
            vm.lastScan.collect { result ->
                result ?: return@collect

                // Prepend to history and update adapter
                val status = when {
                    !result.isAccepted -> ScanStatus.REJECTED
                    !result.inNavList -> ScanStatus.NOT_IN_NAV
                    else -> ScanStatus.ACCEPTED
                }
                scanHistory.add(0, ScanHistoryItem(barcode = result.barcode, format = result.format, status = status))
                if (scanHistory.size > 50) scanHistory.removeAt(scanHistory.size - 1)
                historyAdapter.submitList(scanHistory.toList())
                binding.tvScanCount.text = scanHistory.size.toString()

                // Result card
                val ctx = requireContext()
                val statusColor: Int
                when {
                    !result.isAccepted -> {
                        statusColor = ctx.resolveAttrColor(R.attr.colorNegative)
                        binding.statusHeader.setBackgroundColor(statusColor)
                        binding.tvStatus.text = "REJECTED"
                        binding.tvRejectionReason.text = result.rejectionReason
                        binding.tvRejectionReason.isVisible = true
                        binding.tvDescription.isVisible = false
                        binding.tvPrice.isVisible = false
                        binding.statsRow.isVisible = false
                    }
                    !result.inNavList -> {
                        statusColor = ctx.resolveAttrColor(R.attr.colorWarning)
                        binding.statusHeader.setBackgroundColor(statusColor)
                        binding.tvStatus.text = "NOT IN NAV LIST"
                        binding.tvRejectionReason.isVisible = false
                        binding.tvDescription.isVisible = false
                        binding.tvPrice.isVisible = false
                        binding.statsRow.isVisible = false
                    }
                    else -> {
                        statusColor = ctx.resolveAttrColor(R.attr.colorPositive)
                        binding.statusHeader.setBackgroundColor(statusColor)
                        binding.tvStatus.text = "ACCEPTED"
                        binding.tvRejectionReason.isVisible = false
                        binding.tvDescription.text = result.description
                        binding.tvDescription.isVisible = true
                        binding.tvPrice.text = "₱ ${"%.2f".format(result.price)}"
                        binding.tvPrice.isVisible = true
                        binding.tvNavQty.text = "${result.navQty}"
                        binding.tvScannedQty.text = "${result.scannedQty}"
                        binding.tvVariance.text = "${result.variance}"
                        binding.statsRow.isVisible = true
                    }
                }
                binding.tvBarcode.text = result.barcode
                binding.tvFormat.text = result.format
                binding.scanResultCard.isVisible = true
                startCooldownAnimation(statusColor)
            }
        }

        binding.btnOk.setOnClickListener {
            binding.scanResultCard.isVisible = false
        }

        binding.btnTorch.setOnClickListener {
            isTorchOn = !isTorchOn
            camera?.cameraControl?.enableTorch(isTorchOn)
            binding.btnTorch.text = if (isTorchOn) "Torch OFF" else "Torch ON"
        }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        mediaPlayer = try {
            val afd = requireContext().assets.openFd("beep.mp3")
            MediaPlayer().apply { setDataSource(afd.fileDescriptor, afd.startOffset, afd.length); prepare() }
        } catch (e: Exception) { null }
    }

    private fun toggleHistoryPanel() {
        val nowVisible = !binding.scanHistoryPanel.isVisible
        binding.scanHistoryPanel.isVisible = nowVisible
        // Scroll to top when opening
        if (nowVisible && scanHistory.isNotEmpty()) binding.rvScanHistory.scrollToPosition(0)
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        if (!isProcessing.get()) processImage(imageProxy)
                        else imageProxy.close()
                    }
                }
            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(viewLifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
            } catch (e: Exception) { Log.e("Scanner", "Camera bind failed", e) }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val valid = barcodes.filter { it.rawValue != null }
                if (valid.isEmpty()) return@addOnSuccessListener
                if (!isProcessing.compareAndSet(false, true)) return@addOnSuccessListener

                when {
                    valid.size > 1 -> showBarcodeSelectionDialog(valid)
                    else -> dispatchBarcode(valid[0])
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    // Called from ML Kit success listener (main thread)
    private fun showBarcodeSelectionDialog(barcodes: List<Barcode>) {
        if (!isAdded) { isProcessing.set(false); return }
        val items = barcodes.map { b ->
            val fmt = formatName(b.format)
            val accepted = b.format == Barcode.FORMAT_EAN_13 || b.format == Barcode.FORMAT_CODE_128
            "${b.rawValue}  ($fmt)${if (!accepted) "  ✗" else ""}"
        }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("${barcodes.size} barcodes detected — pick one")
            .setItems(items) { _, which ->
                dispatchBarcode(barcodes[which])
            }
            .setNegativeButton("Cancel") { _, _ -> isProcessing.set(false) }
            .setOnCancelListener { isProcessing.set(false) }
            .show()
    }

    private fun dispatchBarcode(barcode: Barcode) {
        val raw = barcode.rawValue ?: run { isProcessing.set(false); return }
        val fmt = formatName(barcode.format)
        if (barcode.format == Barcode.FORMAT_EAN_13 || barcode.format == Barcode.FORMAT_CODE_128) {
            onBarcodeAccepted(raw, fmt)
        } else {
            onBarcodeRejected(raw, fmt)
        }
    }

    private fun formatName(format: Int): String = when (format) {
        Barcode.FORMAT_EAN_13 -> "EAN-13"
        Barcode.FORMAT_EAN_8 -> "EAN-8"
        Barcode.FORMAT_CODE_128 -> "Code 128"
        Barcode.FORMAT_CODE_39 -> "Code 39"
        Barcode.FORMAT_CODE_93 -> "Code 93"
        Barcode.FORMAT_QR_CODE -> "QR Code"
        Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
        Barcode.FORMAT_UPC_A -> "UPC-A"
        Barcode.FORMAT_UPC_E -> "UPC-E"
        Barcode.FORMAT_PDF417 -> "PDF417"
        Barcode.FORMAT_AZTEC -> "Aztec"
        Barcode.FORMAT_ITF -> "ITF"
        Barcode.FORMAT_CODABAR -> "Codabar"
        else -> "Unknown"
    }

    private fun startCooldownAnimation(color: Int) {
        cooldownAnimator?.cancel()
        binding.scanCooldownProgress.progressTintList = ColorStateList.valueOf(color)
        binding.scanCooldownProgress.progress = 0
        binding.scanCooldownProgress.isVisible = true
        cooldownAnimator = ObjectAnimator.ofInt(binding.scanCooldownProgress, "progress", 0, 100).apply {
            duration = 2000
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun onBarcodeAccepted(barcode: String, format: String) {
        mediaPlayer?.start()
        val deviceId = Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)
        val qty = binding.etQty.text.toString().toIntOrNull() ?: 1
        cooldownJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            vm.processBarcodeScan(barcode, format, qty, deviceId)
            delay(2000)
            binding.scanCooldownProgress.isVisible = false
            isProcessing.set(false)
        }
    }

    private fun onBarcodeRejected(barcode: String, format: String) {
        cooldownJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            vm.reportScanRejected(barcode, format, "Only EAN-13 and Code 128 are accepted")
            delay(2000)
            binding.scanCooldownProgress.isVisible = false
            isProcessing.set(false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cooldownJob?.cancel()
        cooldownAnimator?.cancel()
        cameraExecutor.shutdown()
        mediaPlayer?.release()
        scanner.close()
        _binding = null
    }
}
