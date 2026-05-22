package com.rgmc.inventory.ui.signing

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.rgmc.inventory.databinding.FragmentSigningBinding
import com.rgmc.inventory.ui.viewmodel.SigningViewModel

class SigningFragment : Fragment() {
    private var _binding: FragmentSigningBinding? = null
    private val binding get() = _binding!!
    private val vm: SigningViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSigningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val personnelId = arguments?.getInt("personnelId") ?: 0
        val name = arguments?.getString("name") ?: ""
        binding.tvPersonnelName.text = name
        binding.btnClear.setOnClickListener { binding.signatureView.clear() }
        binding.btnSave.setOnClickListener {
            val bytes = binding.signatureView.getSignatureBytes()
            if (bytes != null) {
                vm.saveSignature(personnelId, bytes)
                findNavController().popBackStack()
            }
        }
        binding.btnCancel.setOnClickListener { findNavController().popBackStack() }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
