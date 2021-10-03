package com.nobodyatall.simplesum

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fun readBytes(context: Context, uri: Uri): ByteArray? =
            context.contentResolver.openInputStream(uri)?.buffered()?.use { it.readBytes() }

        fun generateChecksum(data: ByteArray?, type: String): String{
            val md = MessageDigest.getInstance(type)
            md.update(data)
            val digest: ByteArray = md.digest()

            val hexString = StringBuffer()
            for (element in digest) {
                hexString.append(Integer.toHexString(0xFF and element.toInt()))
            }
            return hexString.toString()
        }

        fun getFileName(uri: Uri): String {
            var result = ""
            if (uri.scheme.equals("content")) {
                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor.use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                }
            }
            return result
        }

        val fileName: TextView = findViewById(R.id.FileName)
        val chooseButton: Button = findViewById(R.id.choose_file_button)
        val sha1: TextView = findViewById(R.id.SHA1Checksum)
        val sha256: TextView = findViewById(R.id.SHA256Checksum)
        val md5: TextView = findViewById(R.id.MD5Checksum)

        val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri ->
            fileName.text = getFileName(uri)
            val bytearray: ByteArray? = readBytes(this, uri)
            sha1.text= generateChecksum(bytearray, "SHA-1")
            sha256.text = generateChecksum(bytearray, "SHA-256")
            md5.text = generateChecksum(bytearray, "MD5")
        }

        chooseButton.setOnClickListener{
            getContent.launch("*/*")
        }



    }

}

