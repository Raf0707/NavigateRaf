package com.example.navigateraf.ui.counter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.navigateraf.R
import com.example.navigateraf.databinding.FragmentCounterBinding
import com.example.navigateraf.ui.counter.CounterDataStore.dataStore
import com.example.navigateraf.util.OnSwipeTouchListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


object CounterPreferencesKeys {
    val COUNTER_TITLE = stringPreferencesKey("counter_title")
    val COUNTER_TARGET = intPreferencesKey("counter_target")
    val COUNTER_PROGRESS = intPreferencesKey("counter_progress")
}

object CounterDataStore {
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "counter_data")
}

class CounterFragment : Fragment() {
    private lateinit var binding: FragmentCounterBinding
    private lateinit var handler: Handler
    private var counter = 0
    private lateinit var vibrator: Vibrator
    private lateinit var navController: NavController

    private val defaultValue = "10"
    private var maxValue: Int = 0
    private val dataStore by lazy { requireContext().dataStore }

    //private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "counter_data")

    private var isEditing = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentCounterBinding.inflate(layoutInflater, container, false)

        navController = findNavController()

        vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        handler = Handler()

        setupListeners()

        loadCounterData()

        return binding.root
    }

    private fun setupListeners() {
        binding.openSettingsBtn.setOnClickListener {
            navController.navigate(R.id.action_counterFragment_to_settingsFragment)
            saveData()
        }

        binding.deleteCounter.setOnClickListener {
            removeCounterAlert()
            saveData()
        }

        binding.openTutorialBtn.setOnClickListener {
            tutorialCounterAlert()
            saveData()
        }

        binding.editCounter.setOnClickListener {
            if (isEditing) {
                // Сохраняем изменения
                binding.counterTarget.setText(
                    binding.counterTarget.text.toString().replace("[\\.\\-,\\s]+", "")
                )

                binding.counterTarget.isCursorVisible = false
                binding.counterTarget.isFocusableInTouchMode = false
                binding.counterTarget.isEnabled = false

                binding.counterTitle.isCursorVisible = false
                binding.counterTitle.isFocusableInTouchMode = false
                binding.counterTitle.isEnabled = false

                if (binding.counterTarget.text.toString().isEmpty()) {
                    binding.counterTarget.setText(defaultValue)
                    maxValue = binding.counterTarget.text.toString().toInt()

                    Snackbar.make(
                        requireView(),
                        "Вы не ввели цель. По умолчанию: $defaultValue",
                        Snackbar.LENGTH_LONG
                    ).show()
                } else {
                    if (binding.counterTarget.text.toString().toInt() <= 0) {
                        Snackbar.make(
                            requireView(),
                            "Введите число больше нуля!",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                        Snackbar.make(
                            requireView(),
                            "Цель установлена",
                            Snackbar.LENGTH_LONG
                        ).show()

                        maxValue = binding.counterTarget.text.toString().toInt()
                    }
                }

                saveData()

                // Меняем иконку на карандаш и переключаем флаг
                binding.editCounter.setBackgroundResource(R.drawable.ic_baseline_edit_24) // Укажите ID ресурса карандаша
                isEditing = false
            } else {
                // Включаем режим редактирования
                binding.counterTarget.isCursorVisible = true
                binding.counterTarget.isFocusableInTouchMode = true
                binding.counterTarget.isEnabled = true

                binding.counterTitle.isCursorVisible = true
                binding.counterTitle.isFocusableInTouchMode = true
                binding.counterTitle.isEnabled = true

                binding.counterTarget.requestFocus()
                binding.counterTarget.setSelection(binding.counterTarget.text!!.length)

                requireActivity().window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                )
                requireActivity().window.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                )

                val imm = requireActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm?.showSoftInput(binding.counterTarget, InputMethodManager.SHOW_FORCED)

                // Меняем иконку на галочку и переключаем флаг
                binding.editCounter
                    .setBackgroundResource(R.drawable.ic_baseline_check_24) // Укажите ID ресурса галочки
                isEditing = true
            }
        }

        binding.counterGestureView.setOnTouchListener(object : OnSwipeTouchListener(binding.counterGestureView.context) {
            @SuppressLint("MissingPermission")
            override fun onClick() {
                vibrator.vibrate(50)
                counter++
                binding.gestureCounter.text = counter.toString()
                saveData()

                if (counter == binding.counterTarget.text.toString().toInt()) {
                    vibrator.vibrate(1000)
                    Snackbar.make(requireView(), "Цель достигнута! Да вознаградит вас Аллах!", Snackbar.LENGTH_LONG).show()
                }
            }

            @SuppressLint("MissingPermission")
            override fun onSwipeDown() {
                vibrator.vibrate(50)
                counter--
                binding.gestureCounter.text = counter.toString()
                saveData()
            }

            @SuppressLint("MissingPermission")
            override fun onLongClick() {
                vibrator.vibrate(200)
                if (counter != 0) onResetCounterAlert()
                saveData()
            }
        })
    }

    // Popup menu
    private fun tutorialCounterAlert() {
        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Жесты счетчика")
            .setMessage("Нажатие на экран: +1 \n Свайп вниз: -1 \n Долгое нажатие на экран: обновление счетчика до 0")
            .setPositiveButton("Понял") { _, _ ->
                vibrator.vibrate(200)
                counter = 0
                binding.gestureCounter.text = "0"
                Snackbar.make(binding.root, "Я рад за тебя", Snackbar.LENGTH_SHORT).show()
            }
            .setNeutralButton("Не понял") { dialog, _ ->
                Snackbar.make(binding.root, "Прочитай еще раз", Snackbar.LENGTH_LONG)
                    .setAction("Хорошо", View.OnClickListener {
                        tutorialCounterAlert()
                    })
                    .show()
                dialog.cancel()
            }
            .show()
    }



    private fun onResetCounterAlert() {
        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Reset")
            .setMessage("Вы уверены, что хотите обновить счетчик?")
            .setPositiveButton("Да") { _, _ ->
                vibrator.vibrate(200)
                counter = 0
                binding.gestureCounter.text = "0"
            }
            .setNeutralButton("Отмена") { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun removeCounterAlert() {
        vibrator.vibrate(200)
        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Remove")
            .setMessage("Вы уверены, что хотите очистить данные счетчика?")
            .setPositiveButton("Очистить") { _, _ ->
                vibrator.vibrate(200)
                binding.counterTitle.text?.clear()
                binding.counterTarget.text?.clear()
                counter = 0
                binding.gestureCounter.text = "0"
            }
            .setNeutralButton("Отмена") { dialog, _ -> dialog.cancel() }
            .show()
    }

    private suspend fun saveCounterData(title: String, target: Int, progress: Int) {
        dataStore.edit { preferences ->
            preferences[CounterPreferencesKeys.COUNTER_TITLE] = title
            preferences[CounterPreferencesKeys.COUNTER_TARGET] = target
            preferences[CounterPreferencesKeys.COUNTER_PROGRESS] = progress
        }
    }

    private fun loadCounterData() {
        lifecycleScope.launch {
            dataStore.data
                .map { preferences ->
                    val title = preferences[CounterPreferencesKeys.COUNTER_TITLE] ?: ""
                    val target = preferences[CounterPreferencesKeys.COUNTER_TARGET] ?: defaultValue.toInt()
                    val progress = preferences[CounterPreferencesKeys.COUNTER_PROGRESS] ?: 0
                    Triple(title, target, progress)
                }
                .collect { (title, target, progress) ->
                    binding.counterTitle.setText(title)
                    binding.counterTarget.setText(target.toString())
                    counter = progress
                    binding.gestureCounter.text = progress.toString()
                }
        }
    }

    private fun saveData() {
        if (binding.counterTitle.text.toString().isEmpty()) {
            binding.counterTitle.setText("Счетчик")
        }

        if (binding.counterTarget.text.toString().isEmpty()) {
            binding.counterTarget.setText(defaultValue)
        }

        lifecycleScope.launch {
            saveCounterData(
                binding.counterTitle.text.toString(),
                binding.counterTarget.text.toString().toInt(),
                counter
            )
        }
    }

}