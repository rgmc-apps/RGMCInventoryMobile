package com.rgmc.inventory.ui.scanner

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
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
                binding.tvDescription.text = result.description
                binding.tvPrice.text = "₱ ${"%.2f".format(result.price)}"
                binding.tvNavQty.text = "${result.navQty}"
                binding.tvScannedQty.text = "${result.scannedQty}"
                binding.tvVariance.text = "${result.variance}"
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
                for (barcode in barcodes) {
                    val raw = barcode.rawValue ?: continue
                    if (barcode.format == Barcode.FORMAT_EAN_13 && raw.length == 13) {
                        if (isProcessing.compareAndSet(false, true)) {
                            onBarcodeDetected(raw)
                        }
                    }
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun onBarcodeDetected(barcode: String) {
        mediaPlayer?.start()
        val deviceId = Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)
        val qty = binding.etQty.text.toString().toIntOrNull() ?: 1
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            vm.processBarcodeScan(barcode, qty, deviceId)
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
