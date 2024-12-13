package com.soaresalex.ktunes.data.service

import co.touchlab.kermit.Logger
import com.soaresalex.ktunes.data.models.Track
import javazoom.jl.decoder.Bitstream
import javazoom.jl.decoder.Decoder
import javazoom.jl.decoder.Header
import javazoom.jl.decoder.SampleBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.sourceforge.jaad.aac.AACException
import java.io.File
import java.io.IOException
import javax.sound.sampled.*
import kotlin.Throws
import kotlin.coroutines.CoroutineContext
import net.sourceforge.jaad.SampleBuffer as AACSampleBuffer
import net.sourceforge.jaad.aac.Decoder as AACDecoder

/**
 * Implementation of [PlaybackService] using the SourceDataLine API for audio playback.
 *
 * @param coroutineContext The coroutine context for handling asynchronous operations.
 */
class SourceDataLinePlaybackService : PlaybackService {
	private val coroutineContext: CoroutineContext = Dispatchers.IO
	private var sourceDataLine: SourceDataLine? = null
	private var currentTrack: Track? = null
	private var isPaused: Boolean = false

	@Throws(
		UnsupportedAudioFileException::class, IOException::class, LineUnavailableException::class
	)
	private fun openAudioStream(track: Track): AudioInputStream? {
		val file = track.fileUri?.let { File(it) } ?: return null
		if (!file.exists()) {
			Logger.e { "File does not exist: ${file.absolutePath}" }
			return null
		}

		return when (file.extension.lowercase()) {
			"mp3" -> getMp3AudioStream(file)
			"m4a" -> getM4aAudioStream(file)
			"wav", "aiff", "au" -> getStandardAudioStream(file)
			else -> throw UnsupportedAudioFileException("Unsupported file type: ${file.extension}")
		}
	}

	private fun getStandardAudioStream(file: File): AudioInputStream? {
		val audioInputStream = AudioSystem.getAudioInputStream(file)
		val baseFormat = audioInputStream.format

		// Convert to a format supported by SourceDataLine (e.g., PCM signed)
		val decodedFormat = AudioFormat(
			AudioFormat.Encoding.PCM_SIGNED, baseFormat.sampleRate, 16, // 16-bit samples
			baseFormat.channels, baseFormat.channels * 2, // Frame size
			baseFormat.sampleRate, false // Little-endian
		)

		return AudioSystem.getAudioInputStream(decodedFormat, audioInputStream)
	}

	private fun getMp3AudioStream(file: File): AudioInputStream? {
		val bitstream = Bitstream(file.inputStream())
		val decoder = Decoder()
		val header: Header = bitstream.readFrame() ?: return null

		val sampleBuffer = decoder.decodeFrame(header, bitstream) as SampleBuffer
		bitstream.closeFrame()

		val audioFormat = AudioFormat(
			AudioFormat.Encoding.PCM_SIGNED,
			header.sample_frequency().toFloat(),
			16, // 16-bit samples
			2, // Stereo
			4, // Frame size (2 channels * 2 bytes/sample)
			header.sample_frequency().toFloat(),
			false // Little-endian
		)

		val byteArray = convertToByteArray(sampleBuffer)
		val inputStream = byteArray.inputStream()

		return object : AudioInputStream(inputStream, audioFormat, (byteArray.size / audioFormat.frameSize).toLong()) {}
	}

	private fun getM4aAudioStream(file: File): AudioInputStream? {
		val inStream = file.inputStream()

		// Read entire file content first
		val fileBytes = inStream.readBytes()

		try {
			// Log the first few bytes in various representations
			Logger.d("First 20 bytes (hex): ${fileBytes.take(20).joinToString(" ") { "%02X".format(it) }}")
			Logger.d("First 20 bytes (decimal): ${fileBytes.take(20).joinToString(" ")}")

			val aacDecoder = AACDecoder.create(fileBytes)
			var sampleBuffer = AACSampleBuffer()

			// Create a new input stream from file bytes for frame decoding
			val frameInputStream = fileBytes.inputStream()

			// Decode the first frame to get format information
			try {
				aacDecoder.decodeFrame(frameInputStream.readBytes(), sampleBuffer)
			} catch (e: AACException) {
				// Log extremely detailed error information
				Logger.e("Error decoding first M4A frame", e)

				// Print the full stack trace
				e.printStackTrace()

				return null
			}


			val isStereo = sampleBuffer.channels == 2
			val audioFormat = AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED, sampleBuffer.sampleRate.toFloat(), 16, // 16-bit samples
				if (isStereo) 2 else 1, // Determine mono or stereo
				if (isStereo) 4 else 2, // Frame size
				sampleBuffer.sampleRate.toFloat(), false // Little-endian
			)

			return object : AudioInputStream(inStream, audioFormat, AudioSystem.NOT_SPECIFIED.toLong()) {
				private var buffer = ByteArray(0)
				private var bufferIndex = 0

				override fun read(): Int {
					if (bufferIndex >= buffer.size) {
						// Try to decode a new frame when the buffer is empty
						if (!decodeNextFrame()) {
							return -1 // Return -1 if no more frames can be decoded
						}
					}
					val byte = buffer[bufferIndex]
					bufferIndex++
					return byte.toInt() and 0xFF
				}

				override fun read(b: ByteArray, off: Int, len: Int): Int {
					if (bufferIndex >= buffer.size) {
						if (!decodeNextFrame()) {
							return -1
						}
					}
					val bytesToCopy = minOf(len, buffer.size - bufferIndex)
					System.arraycopy(buffer, bufferIndex, b, off, bytesToCopy)
					bufferIndex += bytesToCopy
					return bytesToCopy
				}

				private fun decodeNextFrame(): Boolean {
					try {
						val frame = ByteArray(8192) // Buffer to read a frame
						val bytesRead = inStream.read(frame)
						if (bytesRead > 0) {
							sampleBuffer = AACSampleBuffer()
							aacDecoder.decodeFrame(frame.copyOf(bytesRead), sampleBuffer)
							buffer = sampleBuffer.data.copyOf(sampleBuffer.data.size)
							bufferIndex = 0
							return true
						}
					} catch (e: Exception) {
						Logger.e("Error decoding M4A frame", e)
						return false
					}
					return false
				}

				override fun close() {
					super.close()
					inStream.close()
				}
			}
		} catch (e: Exception) {
			Logger.e(e) { "Error creating AAC decoder: ${e.message}" }
			return null
		}
	}

	private fun convertToByteArray(sampleBuffer: SampleBuffer): ByteArray {
		val samples = sampleBuffer.buffer
		val numSamples = sampleBuffer.bufferLength * sampleBuffer.channelCount
		val byteArray = ByteArray(numSamples * 2) // 2 bytes per sample

		for (i in 0 until numSamples) {
			val sample = Math.max(-32768, Math.min(32767, samples[i].toInt()))
			byteArray[i * 2] = (sample and 0xFF).toByte()
			byteArray[i * 2 + 1] = (sample shr 8 and 0xFF).toByte()
		}

		return byteArray
	}

	private fun convertToByteArray(sampleBuffer: AACSampleBuffer): ByteArray {
		val samples = sampleBuffer.data
		val byteArray = ByteArray(samples.size * 2) // 2 bytes per sample

		for (i in samples.indices) {
			val sample = Math.max(-32768, Math.min(32767, samples[i].toInt()))
			byteArray[i * 2] = (sample and 0xFF).toByte()
			byteArray[i * 2 + 1] = (sample shr 8 and 0xFF).toByte()
		}

		return byteArray
	}

	override suspend fun play(track: Track) = withContext(coroutineContext) {
		if (isPaused && currentTrack == track) {
			resume()
			return@withContext
		}

		stop() // Stop any currently playing track

		currentTrack = track
		isPaused = false

		try {
			val audioStream = openAudioStream(track) ?: return@withContext

			val format = audioStream.format
			sourceDataLine = AudioSystem.getSourceDataLine(format).apply {
				open(format)
				start()
			}

			val buffer = ByteArray(sourceDataLine!!.bufferSize)
			var bytesRead: Int

			while (sourceDataLine!!.isOpen && !isPaused) {
				bytesRead = audioStream.read(buffer, 0, buffer.size)
				if (bytesRead == -1) break // End of stream
				sourceDataLine!!.write(buffer, 0, bytesRead)
			}

			if (!isPaused) {
				sourceDataLine!!.drain()
			}
		} catch (e: UnsupportedAudioFileException) {
			Logger.e(e, tag = "SourceDataLinePlaybackService") { "Unsupported audio file format: ${e.message}" }
			e.printStackTrace()
		} catch (e: IOException) {
			Logger.e(e, tag = "SourceDataLinePlaybackService") { "IO error during playback: ${e.message}" }
			e.printStackTrace()
		} catch (e: LineUnavailableException) {
			Logger.e(e, tag = "SourceDataLinePlaybackService") { "Audio line unavailable: ${e.message}" }
			e.printStackTrace()
		}
	}

	override suspend fun pause() = withContext(coroutineContext) {
		if (sourceDataLine != null && sourceDataLine!!.isRunning) {
			sourceDataLine!!.stop()
			isPaused = true
		}
	}

	override suspend fun resume() = withContext(coroutineContext) {
		if (sourceDataLine != null && !sourceDataLine!!.isRunning) {
			sourceDataLine!!.start()
			isPaused = false
		}
	}

	override suspend fun stop() = withContext(coroutineContext) {
		sourceDataLine?.apply {
			if (isRunning) stop()
			flush()
			close()
		}
		sourceDataLine = null
		currentTrack = null
		isPaused = false
	}
}