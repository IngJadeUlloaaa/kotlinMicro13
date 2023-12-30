package com.ingjadeulloaaa.micro13

import java.io.File

interface AudioPlayer {
    fun playFile(file: File)
    fun stop()
}