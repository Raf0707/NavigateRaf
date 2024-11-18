package com.example.navigateraf.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.navigateraf.R
import com.example.navigateraf.databinding.FragmentSettingsBinding
import com.example.navigateraf.util.SharedPreferencesUtils
import com.google.android.material.color.DynamicColors
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsFragment : Fragment() {
    private var binding: FragmentSettingsBinding? = null
    private var switchMaterial: SwitchMaterial? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        switchMaterial = binding!!.dynamicColorsSwitch
        binding!!.appThemeRadioGroup.check(
            SharedPreferencesUtils.getInteger(
                requireContext(),
                "checkedButton",
                R.id.setFollowSystemTheme
            )
        )
        binding!!.dynamicColorsSwitch.isEnabled = DynamicColors.isDynamicColorAvailable()
        switchMaterial!!.isChecked =
            SharedPreferencesUtils.getBoolean(requireContext(), "useDynamicColors")

        //int[] setNightModeDescription = {R.string.auto_theme_description, R.string.system_theme_description, R.string.light_theme_description, R.string.night_theme_description};
        //binding.themeDescription.setText(setNightModeDescription[SharedPreferencesUtils.getInteger(requireContext(), "nightMode", 1)]);
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding!!.appThemeRadioGroup.check(
            SharedPreferencesUtils.getInteger(
                requireContext(),
                "checkedButton",
                R.id.setFollowSystemTheme
            )
        )
        binding!!.appThemeRadioGroup.setOnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
            when (checkedId) {
                R.id.setFollowSystemTheme -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    SharedPreferencesUtils.saveInteger(
                        requireContext(),
                        "checkedButton",
                        R.id.setFollowSystemTheme
                    )
                    SharedPreferencesUtils.saveInteger(requireContext(), "nightMode", 1)
                    requireActivity().recreate()
                }

                R.id.setLightTheme -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    SharedPreferencesUtils.saveInteger(
                        requireContext(),
                        "checkedButton",
                        R.id.setLightTheme
                    )
                    SharedPreferencesUtils.saveInteger(requireContext(), "nightMode", 2)
                    requireActivity().recreate()
                }

                R.id.setNightTheme -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    SharedPreferencesUtils.saveInteger(
                        requireContext(),
                        "checkedButton",
                        R.id.setNightTheme
                    )
                    SharedPreferencesUtils.saveInteger(requireContext(), "nightMode", 3)
                    requireActivity().recreate()
                }
            }
        }

        switchMaterial!!.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                DynamicColors.applyToActivitiesIfAvailable(
                    requireActivity().application
                )
            } else {
                DynamicColors.applyToActivitiesIfAvailable(
                    requireActivity().application,
                    R.style.Base_Theme_NavigateRaf
                )
            }
            SharedPreferencesUtils.saveBoolean(requireContext(), "useDynamicColors", isChecked)
            requireActivity().recreate()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
