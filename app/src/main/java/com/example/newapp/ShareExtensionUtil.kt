package com.example.newapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.io.File
import java.io.InputStream
import java.net.URI
import java.util.UUID
import java.io.ByteArrayOutputStream

class ShareExtensionUtil {
    val badReferers : List<String> = listOf("android-app://com.google.android.inputmethod.latin",
        "android-app://com.sec.android.inputmethod")

    var lastReferer : String = ""

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    fun setLastReferer(context : Context, message : String) {
        val activity: Activity = context as Activity
        lastReferer = activity.getReferrer().toString()
        Log.d("ShareExtension", message + lastReferer)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun maliciousAttempt(context : Context) {
        val activity: Activity = context as Activity
//        Toast.makeText(context, R.string.malicious_attempt, Toast.LENGTH_LONG).show()
        activity.finishAndRemoveTask()
    }

    fun setFilenameTmpResource(context: Context, mediaUri : Uri, mime : String? = null) : String {
        var fileName = ""
//
//        try {
//            fileName = ResourceCache.getInstance(context).createNameFromUri(mediaUri.toString(), context)
//        } catch (e : Exception) {
//            ENLog.i("ShareExtension", "No real name can be detected, generating a suitable one.")
//            fileName = mediaUri.lastPathSegment ?: "mediaFile"
//
//            // Adding an extension in case the filename doesn't have one
//            if (!MimeTypeMap.getSingleton().hasExtension(fileName.split('.').last()) && mime != null) {
//                val extension = mime.split("/").last()
//                fileName = "${fileName}.${extension}"
//            }
//        }

//        if(fileName.isTraversalFileAttack()){
//            maliciousAttempt(context)
//        } else {
//            ENLog.d("ShareExtension", "Final name set")
//            fileName = "TMP.${UUID.randomUUID()}.${fileName}"
//        }

        return fileName
    }

    fun closeInputStr(context : Context, mediaUri : Uri, sharedPath : File) {
//        AsyncTaskHelper.run(sharedPath.path, {
//            try {
//                val inputStr = context.getContentResolver().openInputStream(mediaUri) as InputStream
//                inputStr.toFile(sharedPath.path)
//                inputStr.close()
//            } catch (e: Exception) {
//                Log.e("ShareExtension", "Exception on accessing/writing content (${sharedPath.path}) > $e")
//                // TODO: Show warning?
//            }
//        })

   }

    fun findSharedPath(context : Context, fileName : String) : File {
        val cacheDir = context.cacheDir.toString()
        return File(cacheDir, fileName)
    }

    fun writeTmpResource(context: Context, uri : Parcelable, mime : String? = null) : Array<String> {
        val mediaUri = uri as Uri
        val mime = mime ?: context.getContentResolver().getType(mediaUri)
        var fileName = ""

//        if(mediaUri.isTraversalFileAttack() || mediaUri.toString().isTraversalFileAttack()){
//            maliciousAttempt(context)
//        } else {
        fileName = setFilenameTmpResource(context, mediaUri, mime)

//            if(!fileName.isTraversalFileAttack()){
        val sharedPath = findSharedPath(context, fileName)
        closeInputStr(context, mediaUri, sharedPath)
        return arrayOf(sharedPath.path ?: "", mime ?: "")
//            }
//        }
//        return arrayOf()
    }

    fun writeTmpResourceFromBytes(context: Context, byteArray : ByteArray, mime : String? = null, extension : String? = "PNG") : Array<String> {
        var fileName = UUID.randomUUID().toString() + "." + extension
        Log.d("ShareExtension", "Final name set $fileName")

        val sharedPath = findSharedPath(context, fileName)
        sharedPath.writeBytes(byteArray)
        return arrayOf(sharedPath.path ?: "", mime ?: "")
    }

    fun isBadReferer(referer : String) : Boolean {
        return badReferers.any { it == referer }
    }

    fun cleanChromeIntent(content: String?): String {
        content?.let {
            if (content.indexOf("#:~:text=") < 0) {
                return content
            }

            val text = content.substring(1, content.lastIndexOfAny(listOf("http://", "https://")) - 3)
            return text
        }
        return ""
    }


    open fun getDomainName(url: String?): String {
        return try{
            val uri = URI(url)
            val domain: String = uri.host
            if (domain.startsWith("www.")) domain.substring(4) else domain
        } catch (e :Exception){
            Log.d("ShareExtension", "error in extracting title")
            ""
        }

    }

    fun shareNeutronContent(context: Context, intent: Intent) {
        // Content from Neutron can only be shared as plain text on Android
        var neutronMediaType = intent.getStringExtra(Intent.EXTRA_SUBJECT)
        var isNeutronMedia = neutronMediaType !== null && neutronMediaType.startsWith("NeutronShare")
        if (isNeutronMedia) {
            neutronMediaType?.let {
                neutronMediaType = it.split(":")[1]
            }
        }
    }

    fun shareExtraScreenshot(context: Context, intent: Intent, arguments: Bundle): Bundle {
        val imageUri : Uri? = intent.getParcelableExtra<Uri?>("share_screenshot_as_stream")
        imageUri?.let {
            val activity: Activity = context as Activity
            val `is` = activity.contentResolver.openInputStream(it)
            val bitmap: Bitmap = BitmapFactory.decodeStream(`is`)
            `is`!!.close()
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
            val iconResourceData =
                writeTmpResourceFromBytes(context, byteArray, "image/png", "PNG")
            arguments.putString("intentDataIcon", iconResourceData[0])
        }
        return arguments
    }

    fun shareTextSingleContent(context: Context, intent: Intent, arguments: Bundle): Bundle {
        // Is a file?
        if (intent.getStringExtra(Intent.EXTRA_TEXT) == null) {
            var resource: Array<String> = arrayOf("", "")
            val stream = intent.getExtras()?.getParcelable<Parcelable>(Intent.EXTRA_STREAM)

            if (stream != null) {
                resource = writeTmpResource(context, stream)
            }

            arguments.putString("intentDataType", "[\"${intent.type}\"]")
            arguments.putString("intentDataContent", "[\"${resource[0]}\"]")
        } else {
            arguments.putString("intentDataType", intent.type)
            arguments.putString("intentDataContent",
                cleanChromeIntent(intent.getStringExtra(Intent.EXTRA_TEXT))
            )
        }

        arguments.putString("intentDataReferer", lastReferer)
        if(intent.hasExtra("share_screenshot_as_stream")){
            return shareExtraScreenshot(context, intent, arguments)
        }
        return arguments
    }

    fun shareNoTextSingleContent(context: Context, intent: Intent, arguments: Bundle): Bundle {
        var resource: Array<String> = arrayOf("", "")
        val neutronMediaType = intent.getStringExtra(Intent.EXTRA_SUBJECT)
        val isNeutronMedia = neutronMediaType !== null && neutronMediaType.startsWith("NeutronShare")

        if (isNeutronMedia) {
            val resourceUri = Uri.parse(intent.getStringExtra(Intent.EXTRA_TEXT))
            resource = writeTmpResource(context, resourceUri, neutronMediaType)
        } else {
            val stream = intent.getExtras()?.getParcelable<Parcelable>(Intent.EXTRA_STREAM)
            if (stream != null) {
                resource = writeTmpResource(context, stream)
            }
        }

        if (resource.isNotEmpty()) {
            arguments.putString("intentDataContent", "[\"${resource[0]}\"]")
            if (isNeutronMedia) {
                arguments.putString("intentDataType", "[\"${neutronMediaType}\"]")
            } else {
                arguments.putString("intentDataType", "[\"${resource[1]}\"]")
            }
        }

        arguments.putString("intentDataReferer", lastReferer)
        return arguments
    }

    fun shareSingleContent(context: Context, intent: Intent, arguments : Bundle): Bundle {
        return if (intent.type!!.startsWith(("text"))) {
            shareTextSingleContent(context, intent, arguments)
        } else {
            shareNoTextSingleContent(context, intent, arguments)
        }
    }

    fun shareMultipleContent(context: Context, intent: Intent, arguments : Bundle): Bundle {
        var content = "["
        var type = "["

        intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.forEach {
            val resource = writeTmpResource(context, it)
            content += "\"${resource[0]}\","
            type += "\"${resource[1]}\","
        }

        type = type.trimEnd(',') + ']'
        content = content.trimEnd(',') + ']'
        arguments.putString("intentDataType", type)
        arguments.putString("intentDataContent", content)
        arguments.putString("intentDataReferer", lastReferer)
        return arguments
    }

    fun setSubject(context: Context, intent: Intent) : String {
        if(intent.hasExtra(Intent.EXTRA_SUBJECT)){
            return (intent.getStringExtra(Intent.EXTRA_SUBJECT) ?: "").trim();
        } else if(intent.hasExtra(Intent.EXTRA_TEXT)){
            return getDomainName(intent.getStringExtra(Intent.EXTRA_TEXT))
        }
        return ""
    }

    fun setExtraData(context: Context, intent: Intent) : String {
        if(intent.hasExtra(Intent.EXTRA_TEXT)){
            return (intent.getStringExtra(Intent.EXTRA_TEXT) ?: "").trim();
        }
        return ""
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    fun createShareIntentData(context : Context, intent: Intent): Bundle {
        var arguments = Bundle()
        val intentType = intent.type;
        Log.d("ShareExtension", "Creating the intent data")

        if (!(((intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) == 0) or ((intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0))) {
            Log.d("ShareExtension", "App was launched (by multitask btn) after it was closed (ex: after os back btn)")
            return arguments
        }

        //First time
        if (lastReferer === "") {
            try {
                setLastReferer(context, "First intent referer: ")
            } catch (e : Exception) {
                Log.d("ShareExtension", "No intent referer detected.")
            }
        }

        Log.d("ShareExtension", "Intent type: " + intentType)

        if (isBadReferer(lastReferer)) {
            Log.d("ShareExtension", "Referer is not authorized to share content.")
            return arguments
        }

        // Content from Neutron can only be shared as plain text on Android
        shareNeutronContent(context, intent)

        if (Intent.ACTION_SEND == intent.action && intent.type != null) {
            arguments = shareSingleContent(context, intent, arguments)
            intent.type = null
        } else if (Intent.ACTION_SEND_MULTIPLE == intent.action && intentType != null) {
            arguments = shareMultipleContent(context, intent, arguments)
        }
        var subject = setSubject(context, intent)
        arguments.putString("title", subject)

        var extraData = setExtraData(context, intent)
        arguments.putString("extraData", extraData)

        return arguments
    }
}