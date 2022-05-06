gitpackage com.example.newapp

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log

import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.newapp.ShareExtensionUtil


class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val  textView = findViewById<TextView>(R.id.textView2)
//        val  first_imageView=findViewById<ImageView>(R.id. first_imageView)

//        val  second_imageView=findViewById<ImageView>(R.id. second_imageView)
//        when {
//            intent?.action == Intent.ACTION_SEND -> {
//                when {
//                    intent.type == PLAIN_TEXT_MIME -> handleSendText(intent,textView)
//                    intent.type?.startsWith(MEDIA_IMAGE_MIME) == true -> handleSendImage(intent, first_imageView)
//                    intent.type?.startsWith(MEDIA_Audio_MIME) == true -> handleSendAudio(intent,textView)
//                }
//            }
//
//            intent?.action == Intent.ACTION_SEND_MULTIPLE -> {
//                when {
//                    intent.type?.startsWith(MEDIA_IMAGE_MIME) == true -> handleSendMultipleImages(intent,first_imageView,second_imageView)
//                    intent.type?.startsWith(MEDIA_Audio_MIME) == true -> handleSendMultipleAudios(intent,textView)
//                }
//
//            }
//        }


        val shareExtension = ShareExtensionUtil()


        var arguments = shareExtension.createShareIntentData(this, intent)
        if (arguments.containsKey("intentDataType") && arguments.getString("intentDataType") != null) {

            println("intentDataType=" + arguments.getString("intentDataType"))
            println("intentDataContent="+ arguments.getString("intentDataContent"))
            println("extraData="+ arguments.getString("extraData"))
            println("title=" + arguments.getString("title"))
            println("intentDataReferer=" + arguments.getString("intentDataReferer"))
            println("intentDataIcon=" + arguments.getString("intentDataIcon"))
        } else {
            Log.d("ShareExtension:", "Init intent type is null")
        }
        val  textView1 = findViewById<TextView>(R.id.textView1)
        val  textView2 = findViewById<TextView>(R.id.textView2)
        val  textView3 = findViewById<TextView>(R.id.textView3)
        val  textView4 = findViewById<TextView>(R.id.textView4)
        val  textView5 = findViewById<TextView>(R.id.textView5)
        val  textView6 = findViewById<TextView>(R.id.textView6)

        textView1.text = "intentDataType = " + arguments.getString("intentDataType")
        textView2.text = "intentDataContent = "+ arguments.getString("intentDataContent")
        textView3.text = "extraData = "+ arguments.getString("extraData")
        textView4.text = "title = " + arguments.getString("title")
        textView5.text = "intentDataReferer = " + arguments.getString("intentDataReferer")
        textView6.text = "intentDataIcon = " + arguments.getString("intentDataIcon")

    }




    // // // // // //
//    private fun handleSendText(intent: Intent,textView: TextView) {
//        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
//            textView.text = it
//        }
//    }
//
//    private fun handleSendImage(intent: Intent,first_imageView: ImageView) {
//        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
//            first_imageView.setImageURI(it)
//        }
//    }
//
//    @SuppressLint("SetTextI18n")
//    private fun handleSendAudio(intent: Intent, textView: TextView) {
//        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
//            textView.text = "Playing Audio"
//
//            MediaPlayer().apply {
//                setDataSource(applicationContext, it)
//                prepare()
//                start()
//            }
//        }
//    }
//
//    private fun handleSendMultipleImages(intent: Intent, first_imageView: ImageView,second_imageView: ImageView) {
//        intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let {
//            first_imageView.setImageURI(it[0] as? Uri)
//            second_imageView.setImageURI(it[1] as? Uri)
//        }
//    }
//
//    @SuppressLint("SetTextI18n")
//    private fun handleSendMultipleAudios(intent: Intent, textView: TextView) {
//        intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let {
//            textView.text = "Playing Multiple Audios"
//
//            it.forEachIndexed { index, _ ->
//                MediaPlayer().apply {
//                    setDataSource(applicationContext, it[index] as Uri)
//                    prepare()
//                    start()
//                }
//            }
//        }
//    }
//
//    companion object {
//        private const val PLAIN_TEXT_MIME = "text/plain"
//        private const val MEDIA_IMAGE_MIME = "image/"
//        private const val MEDIA_Audio_MIME = "audio/"
//    }

}
