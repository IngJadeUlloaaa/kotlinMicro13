package com.ingjadeulloaaa.micro13

import java.io.File
interface AudioRecorder {
    fun start(outputFile: File)
    fun stop()
}