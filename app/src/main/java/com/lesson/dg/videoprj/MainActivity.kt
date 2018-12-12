package com.lesson.dg.videoprj

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaMuxer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.MediaController
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.jcodec.api.android.AndroidSequenceEncoder
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.io.SeekableByteChannel
import org.jcodec.common.model.Rational
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

const val REQUEST_READ = 0
const val REQUEST_READ_AUDIO = 1
const val REQUEST_WRITE = 2
const val REQUEST_OPEN_FILE = 3
const val REQUEST_OPEN_AUDIO_FILE = 4

class MainActivity : AppCompatActivity() {
    val imageUri = ""
    var audioPath = ""
    var imagesList = mutableListOf<Bitmap>()
    var imagesURI = mutableListOf<Uri>()

    var mediaController: MediaController? = null
    var position = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val adapter = CustomAdapter(this, imagesList)
        imageList.adapter = adapter


        mediaController = MediaController(this)
        mediaController!!.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        videoView.setOnPreparedListener {
            videoView.seekTo(position)
            if (position == 0) {
                videoView.start()
            }
        }

        try {
            //videoView.setVideoPath(videoFile.path)
            println(Environment.getExternalStorageDirectory (). getPath ())
            videoView.setVideoURI(Uri.parse("/storage/emulated/0/Movies/video.mp4"))

        } catch (e: Exception) {
            e.printStackTrace()
        }
        videoView.requestFocus()
    }

    // Find ID corresponding to the name of the resource (in the directory raw).
    fun getRawResIdByName(resName: String): Int {
        val pkgName = this.packageName
        // Return 0 if not found.
        val resID = this.resources.getIdentifier(resName, "raw", pkgName)
        return resID
    }


    // When you change direction of phone, this method will be called.
    // It store the state of video (Current position)
    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        // Store current position.
        savedInstanceState.putInt("CurrentPosition", videoView.currentPosition)
        videoView.pause()
    }


    // After rotating the phone. This method is called.
    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // Get saved position.
        position = savedInstanceState.getInt("CurrentPosition")
        videoView.seekTo(position)
    }

    fun openFile() {
        /*val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, REQUEST_OPEN_FILE)*/
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, REQUEST_OPEN_FILE)
    }

    fun openAudioFile() {
        /*val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, REQUEST_OPEN_FILE)*/
        val intent = Intent()
        intent.type = "audio/*"
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, REQUEST_OPEN_AUDIO_FILE)
    }

    private fun saveFile(imageName: String, bitmap: Bitmap) {
        /*if (imageName == "") {
            Toast.makeText(this, (imageUri ?: R.string.err_bad_file_name).toString(), Toast.LENGTH_SHORT).show()
        }
        val _imageName = imageName + ".png"
        val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), File.separator)
        path.mkdirs()
        val imageFile = File(path, _imageName)
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            Toast.makeText(this, R.string.image_saved, Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
        }*/
        genVideo()

    }

    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    fun runDialog() {
        val context = this
        val alert = AlertDialog.Builder(this)
        var editText: EditText? = null

        with(alert) {
            setTitle(R.string.ui_enter_file_name)

            editText = EditText(context)
            editText!!.hint = R.string.hint_file_name.toString()
            editText!!.inputType = InputType.TYPE_CLASS_TEXT
            editText!!.setText(Calendar.getInstance().time.toString("yyyy_MM_dd_HH_mm_ss"))

            setPositiveButton(R.string.ui_ok) { dialog, whichButton ->
                dialog.dismiss()
                genVideo()
                //saveFile(editText!!.text.toString(), (image.drawable as BitmapDrawable).bitmap)
            }

            setNegativeButton(R.string.ui_cancel) { dialog, whichButton ->
                dialog.dismiss()
            }
        }

        val dialog = alert.create()
        dialog.setView(editText)
        dialog.show()
    }

    fun openFileBtnOnClick(view: View) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ
                    )
                }
            } else {
                openFile()
            }
        } else {
            openFile()
        }
    }

    fun openAudioBtnOnClick(view: View) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_AUDIO
                    )
                }
            } else {
                openAudioFile()
            }
        } else {
            openAudioFile()
        }
    }

    fun saveFileBtnOnClick(view: View) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE
                    )
                }
            } else {
                runDialog()
            }
        } else {
            runDialog()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, result: IntArray) {
        when (requestCode) {
            REQUEST_READ -> {
                if ((result.isNotEmpty() && result[0] == PackageManager.PERMISSION_GRANTED)) {
                    openFile()
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                }
                return
            }
            REQUEST_READ_AUDIO -> {
                if ((result.isNotEmpty() && result[0] == PackageManager.PERMISSION_GRANTED)) {
                    openAudioFile()
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                }
                return
            }
            REQUEST_WRITE -> {
                if ((result.isNotEmpty() && result[0] == PackageManager.PERMISSION_GRANTED)) {
                    runDialog()
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                }
                return
            }
            else -> {
            }
        }
    }

    override fun onActivityResult(code: Int, result: Int, data: Intent?) {
        super.onActivityResult(code, result, data)
        if (code == REQUEST_OPEN_FILE && result == Activity.RESULT_OK) {
            if (data!!.getData() != null) {

                val imageUri = data.data

                val filePathColumns = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = contentResolver.query(imageUri!!, filePathColumns, null, null, null)
                cursor?.moveToFirst()
                val columnIndex = cursor?.getColumnIndex(filePathColumns[0])
                val filePath = cursor?.getString(columnIndex!!)
                cursor?.close()
                val bm = BitmapFactory.decodeFile("/storage/emulated/0/" + imageUri.path.split(":")[1])
                imagesList.add(bm)
            } else {
                if (code == REQUEST_OPEN_FILE && result == Activity.RESULT_OK) {
                    val images = data!!.clipData
                    Toast.makeText(this, (images.itemCount).toString(), Toast.LENGTH_SHORT).show()
                    imagesList.clear()
                    val filePathColumns = arrayOf(MediaStore.Images.Media.DATA)

                    for (i in 0..images.itemCount - 1) {
                        val item = images.getItemAt(i)
                        val uri = item.getUri()

                        val wholeID = DocumentsContract.getDocumentId(uri)

                        val id = wholeID.split(":")[1]

                        val column = arrayOf(MediaStore.Images.Media.DATA)

                        // where id is equal to
                        val sel = MediaStore.Images.Media._ID + "=?";

                        val cursor = getContentResolver().query(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            column, sel, arrayOf(id), null
                        );

                        var filePath = ""

                        val columnIndex = cursor.getColumnIndex(column[0]);

                        if (cursor.moveToFirst()) {
                            filePath = cursor.getString(columnIndex);
                        }
                        cursor.close();
                        val bm = BitmapFactory.decodeFile("/storage/emulated/0/" + uri.path.split(":")[1])
                        imagesList.add(bm)
                        /*val item = images.getItemAt(i)
                val uri = item.getUri()
                imagesURI.add(uri)

                val cursor = contentResolver.query(uri!!, filePathColumns, null, null, null)
                cursor?.moveToFirst()
                val columnIndex = cursor?.getColumnIndex(filePathColumns[0])
                val filePath = cursor?.getString(columnIndex!!)
                cursor?.close()
                val bm = BitmapFactory.decodeFile(filePath)
                println("!!!!!!")
                println(uri)
                if (filePath != null) {
                    imagesList.add(bm)
                }*/
                        //val bitmap = BitmapFactory.decodeFile(uri)

                        //imagesList.add(bitmap)

                    }
                    val adapter = CustomAdapter(this, imagesList)
                    imageList.adapter = adapter
                }
            }
        } else if (code == REQUEST_OPEN_AUDIO_FILE && result == Activity.RESULT_OK) {
            if (data!!.getData() != null) {

                val imageUri = data.data

                val filePathColumns = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = contentResolver.query(imageUri!!, filePathColumns, null, null, null)
                cursor?.moveToFirst()
                val columnIndex = cursor?.getColumnIndex(filePathColumns[0])
                val filePath = cursor?.getString(columnIndex!!)
                cursor?.close()
                audioPath = "/storage/emulated/0/" + imageUri.path.split(":")[1]
                infoText.setText(audioPath)
                //imagesList.add(bm)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun Bitmap.flip(x: Float, y: Float, cx: Float, cy: Float): Bitmap {
        val matrix = Matrix().apply { postScale(x, y, cx, cy) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_reflect_h -> {

                return true
            }
            R.id.action_reflect_v -> {

                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun genVideo() {
        val videoNmae = "video.mp4"

        val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), File.separator)
        path.mkdirs()
        val videoFile = File(path, videoNmae)
        val out: SeekableByteChannel = NIOUtils.writableFileChannel(videoFile.path)
        try {
            val encoder = AndroidSequenceEncoder(out, Rational.R(2, 1));
            var frameId = 0
            var fps = 0
            println("Frame SIZE")
            println(imagesList.size)
            while (frameId < imagesList.size) {
                try {
                    encoder.encodeImage(getResizedBitmap(imagesList.get(frameId), 640, 480, true))
                    println("Frame write!")
                    println(frameId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                fps++
                if (fps == 30) {
                    fps = 0
                    frameId++
                }
            }
            encoder.finish();
        } finally {
            NIOUtils.closeQuietly(out);
        }

        ///
        //try {

        val file = File(path, "final.mp4")
        file.createNewFile();
        val outputFile = file.getAbsolutePath();

        val videoExtractor = MediaExtractor();

        videoExtractor.setDataSource(videoFile.path)
        val audioExtractor =  MediaExtractor()
        //val afd = getAssets().openFd(audioPath)
        //val aFile = File(audioPath)
        //val fd = FileOutputStream(aFile)
        audioExtractor.setDataSource(audioPath)
        //val afdd = getAssets().openFd("audio.m4a");
      //  audioExtractor.setDataSource(Environment.getExternalStorageDirectory() + File.separator + "test_audio.ogg");
        println("LOAD AUDIO!!!!!")
        //Log.d(TAG, "Video Extractor Track Count " + videoExtractor.getTrackCount());
        //Log.d(TAG, "Audio Extractor Track Count " + audioExtractor.getTrackCount());

        val muxer = MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

        videoExtractor.selectTrack(0)
        val videoFormat = videoExtractor.getTrackFormat(0)
        val videoTrack = muxer.addTrack(videoFormat)

        audioExtractor.selectTrack(0);
        val audioFormat = audioExtractor.getTrackFormat(0);
        val  audioTrack = muxer.addTrack(audioFormat);

        var sawEOS = false
        var frameCount = 0
        val offset = 100
        val sampleSize = 256 * 1024
        val videoBuf = ByteBuffer.allocate(sampleSize);
        val audioBuf = ByteBuffer.allocate(sampleSize);
        val videoBufferInfo = MediaCodec.BufferInfo();
        val audioBufferInfo = MediaCodec.BufferInfo();

        videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

        muxer.start();

        while (!sawEOS) {
            videoBufferInfo.offset = offset;
            audioBufferInfo.offset = offset;

            videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);
            audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);

            if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {

                sawEOS = true;
                videoBufferInfo.size = 0;
                audioBufferInfo.size = 0;
            } else {
                videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                videoBufferInfo.flags = videoExtractor.getSampleFlags();
                muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
                videoExtractor.advance();

                audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                audioBufferInfo.flags = audioExtractor.getSampleFlags();
                muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
                audioExtractor.advance();

                frameCount++
            }
        }
        muxer.stop();
        muxer.release();


    //} catch (e: Exception) {
        //Log.d(TAG, "Mixer Error 1 " + e.getMessage());
    //} catch (e: Exception) {
        //Log.d(TAG, "Mixer Error 2 " + e.getMessage());
    //}

        try {
            //videoView.setVideoPath(videoFile.path)
            println(file.path)
            videoView.setVideoURI(Uri.parse(file.path))

        } catch (e: Exception) {
            e.printStackTrace()
        }
        videoView.requestFocus()

    }

    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int, isNecessaryToKeepOrig: Boolean): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
        if (!isNecessaryToKeepOrig) {
            bm.recycle()
        }
        return resizedBitmap
    }

}