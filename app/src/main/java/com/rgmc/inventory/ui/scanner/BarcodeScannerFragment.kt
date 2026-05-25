package com.rgmc.inventory.ui.scanner

import android.annotation.SuppressLint
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.rgmc.inventory.databinding.FragmentBarcodeScannerBinding
import com.rgmc.inventory.ui.viewmodel.ScannerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
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
    private var mediaPlayer: MediaPlayer? = null
    private val scanner = BarcodeScanning.getClient()
    private var isTorchOn = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBarcodeScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()

        viewLifecycleOwner.lifecycleScope.launch {
            vm.lastScan.collectLatest { result ->
                result ?: return@collectLatest
                binding.tvBarcode.text = result.barcode
                binding.tvFormat.text = result.format
                when {
                    !result.isAccepted -> {
                        binding.statusHeader.setBackgroundColor(Color.parseColor("#EF4444"))
                        binding.tvStatus.text = "REJECTED"
                        binding.tvRejectionReason.text = result.rejectionReason
                        binding.tvRejectionReason.isVisible = true
                        binding.tvDescription.isVisible = false
                        binding.tvPrice.isVisible = false
                        binding.statsRow.isVisible = false
                    }
                    !result.inNavList -> {
                        binding.statusHeader.setBackgroundColor(Color.parseColor("#F59E0B"))
                        binding.tvStatus.text = "NOT IN NAV LIST"
                        binding.tvRejectionReason.isVisible = false
                        binding.tvDescription.isVisible = false
                        binding.tvPrice.isVisible = false
                        binding.statsRow.isVisible = false
                    }
                    else -> {
                        binding.statusHeader.setBackgroundColor(Color.parseColor("#22C55E"))
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
                binding.scanResultCard.isVisible = true
            }
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
                val barcode = barcodes.firstOrNull { it.rawValue != null } ?: return@addOnSuccessListener
                val raw = barcode.rawValue ?: return@addOnSuccessListener
                if (!isProcessing.compareAndSet(false, true)) return@addOnSuccessListener
                val fmt = barcode.format
                val fmtName = formatName(fmt)
                if (fmt == Barcode.FORMAT_EAN_13 || fmt == Barcode.FORMAT_CODE_128) {
                    onBarcodeAccepted(raw, fmtName)
                } else {
                    onBarcodeRejected(raw, fmtName)
                }
            }
            .addOnCompleteListener { imageProxy.close() }
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

    private fun onBarcodeAccepted(barcode: String, format: String) {
        mediaPlayer?.start()
        val deviceId = Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)
        val qty = binding.etQty.text.toString().toIntOrNull() ?: 1
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            vm.processBarcodeScan(barcode, format, qty, deviceId)
            isProcessing.set(false)
        }
    }

    private fun onBarcodeRejected(barcode: String, format: String) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            vm.reportScanRejected(barcode, format, "Only EAN-13 and Code 128 are accepted")
            delay(1500)
            isProcessing.set(false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        mediaPlayer?.release()
        scanner.close()
        _binding = null
    }
}
