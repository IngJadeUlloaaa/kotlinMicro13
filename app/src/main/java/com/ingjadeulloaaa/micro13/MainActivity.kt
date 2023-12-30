package com.ingjadeulloaaa.micro13

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var currentStoryIndex = 0 // Índice de la historia actual
    private val stories by lazy {
        listOf(
            getString(R.string.initial_message),
            getString(R.string.second_message),
            getString(R.string.third_message)
        )
    }

    private val recorder by lazy {
        AndroidAudioRecorder(applicationContext)
    }

    private val player by lazy {
        AndroidAudioPlayer(applicationContext)
    }

    private var audioFile: File? = null
    private val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1

    private var isRecording = false
    private var timer: CountDownTimer? = null
    private var elapsedMillis: Long = 0
    private lateinit var textToSpeech: TextToSpeech
    private var isInitialQuestionAsked = false
    private lateinit var voiceGraphView: VoiceGraphView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textToSpeech = TextToSpeech(this, this)
        voiceGraphView = findViewById(R.id.voiceGraph)

        val startRecordingButton: Button = findViewById(R.id.startRecordingButton)
        val stopRecordingButton: Button = findViewById(R.id.stopRecordingButton)

        val timerTextView: TextView = findViewById(R.id.timerTextView)

        startRecordingButton.setOnClickListener {
            if (!isInitialQuestionAsked) {
                textToSpeech.speak(
                    "Hi, how are you? my name is carlos what about you where are you from",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
                isInitialQuestionAsked = true
            } else if (!textToSpeech.isSpeaking) { // Check if TTS is not speaking
                requestRecordAudioPermission()
                startTimer(timerTextView)
            }
        }

        stopRecordingButton.setOnClickListener {
            recorder.stop()

            stopTimer()
            startRecordingButton.visibility = View.VISIBLE
            stopRecordingButton.visibility = View.GONE

            createAudioModalDialog(this, audioFile)
        }
    }

    private fun requestRecordAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startRecording()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun startRecording() {
        File(cacheDir, "audio.mp3").also {
            recorder.start(it)
            audioFile = it
            findViewById<Button>(R.id.startRecordingButton).visibility = View.GONE
            findViewById<Button>(R.id.stopRecordingButton).visibility = View.VISIBLE
        }
    }

    private fun startTimer(timerTextView: TextView) {
        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                elapsedMillis += 1000
                val hours = TimeUnit.MILLISECONDS.toHours(elapsedMillis)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60

                timerTextView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                addDataToChart((Math.random() * 100).toFloat())
            }

            override fun onFinish() {
            }
        }
        timer?.start()
        isRecording = true
    }

    private fun addDataToChart(yValue: Float) {
        voiceGraphView.addDataPoint(yValue)
    }

    private fun stopTimer() {
        timer?.cancel()
        elapsedMillis = 0
        isRecording = false
    }

    private fun createAudioModalDialog(context: Context, file: File?) {
        if (file != null) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.activity_modal, null)
            val playButton = dialogView.findViewById<Button>(R.id.playAudioButton)
            val restartTextView = dialogView.findViewById<TextView>(R.id.restartTextView) // Aquí está tu botón Restart
            val exitBtn = dialogView.findViewById<ImageButton>(R.id.exitBtn)
            val continueTextView: TextView = dialogView.findViewById(R.id.continueTextView)
            val restartAllButton: Button = dialogView.findViewById(R.id.restartAllButton)

            val dialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            playButton.setOnClickListener {
                player.playFile(file)
            }

            restartTextView.setOnClickListener {
                dialog.dismiss()
                restartRecordingAtCurrentStory()  // Aquí llamamos a la función correcta
            }

            exitBtn.setOnClickListener {
                dialog.dismiss()
            }

            continueTextView.setOnClickListener {
                if (currentStoryIndex < stories.size - 1) {
                    currentStoryIndex++
                    voiceGraphView.clearData()
                    resetTimer(findViewById(R.id.timerTextView))
                    displayCurrentStory()
                    dialog.dismiss()
                }
            }

            restartAllButton.setOnClickListener {
                dialog.dismiss()
                restartAll()
            }

            // Configurar visibilidad basado en la historia actual
            if (currentStoryIndex == stories.size - 1) {
                continueTextView.visibility = View.GONE
                restartAllButton.visibility = View.VISIBLE
            } else {
                continueTextView.visibility = View.VISIBLE
                restartAllButton.visibility = View.GONE
            }

            dialog.show()
        }
    }

    private fun restartRecordingAtCurrentStory() {
        stopTimer()
        resetTimer(findViewById(R.id.timerTextView))
        voiceGraphView.clearData()

        val currentMessage = stories[currentStoryIndex]
        textToSpeech.speak(
            currentMessage,
            TextToSpeech.QUEUE_FLUSH,
            null,
            null
        )
        isInitialQuestionAsked = true
    }


    private fun restartAll() {
        currentStoryIndex = 0
        voiceGraphView.clearData()
        resetTimer(findViewById(R.id.timerTextView))
        displayCurrentStory()
    }

    private fun displayCurrentStory() {
        val currentStory = stories[currentStoryIndex]
        textToSpeech.speak(currentStory, TextToSpeech.QUEUE_FLUSH, null, null)
    }


    private fun restartRecording() {
        stopTimer()
        resetTimer(findViewById(R.id.timerTextView))
        voiceGraphView.clearData()

        val message = getString(R.string.initial_message)

        textToSpeech.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            null
        )
        isInitialQuestionAsked = true
    }

    private fun resetTimer(timerTextView: TextView) {
        stopTimer()
        timerTextView.text = "00:00:00"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startRecording()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result =
                textToSpeech.setLanguage(Locale.US) // Establecer el idioma en inglés de EE. UU.

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Manejar caso en que el idioma no sea soportado
                // Puedes mostrar un mensaje al usuario o tomar alguna otra acción
            } else {
                // Habilita el botón después de que TTS se inicialice correctamente
                findViewById<Button>(R.id.startRecordingButton).isEnabled = true

                // Ahora puedes hablar
                if (!isInitialQuestionAsked) {
                    val message = getString(R.string.initial_message)
                    textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
                    isInitialQuestionAsked = true
                }
                isInitialQuestionAsked = true
            }
        }
    }

    override fun onDestroy() {
        textToSpeech.stop()
        textToSpeech.shutdown()
        super.onDestroy()
    }
}