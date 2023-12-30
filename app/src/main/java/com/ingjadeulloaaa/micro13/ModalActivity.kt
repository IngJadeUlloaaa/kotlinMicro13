package com.ingjadeulloaaa.micro13

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ingjadeulloaaa.micro13.databinding.ActivityModalBinding

class ModalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityModalBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}