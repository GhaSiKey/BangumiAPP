package com.example.bangumi.homepage

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bangumi.TestActivity
import com.example.bangumi.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        try {
            val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val versionName = packageInfo.versionName
            binding.tvAppVersion.text = "版本: $versionName"
        } catch (e: PackageManager.NameNotFoundException) {
            binding.tvAppVersion.text = "版本: 未知"
        }
    }

    private fun setupClickListeners() {
        binding.btnDeveloperOptions.setOnClickListener {
            val intent = Intent(requireContext(), TestActivity::class.java)
            startActivity(intent)
        }

        binding.btnAbout.setOnClickListener {
            Toast.makeText(requireContext(), "Bangumi圣地巡礼应用\n致力于为动漫爱好者提供最佳的圣地巡礼体验", Toast.LENGTH_LONG).show()
        }

        binding.btnFeedback.setOnClickListener {
            Toast.makeText(requireContext(), "感谢您的反馈！请通过应用商店或官方渠道联系我们", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}