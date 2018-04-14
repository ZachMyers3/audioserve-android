package eu.zderadicka.audioserve.utils

import android.content.Context
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.net.NetworkInfo
import android.net.ConnectivityManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel


private const val LOG_TAG = "Utils"

enum class ConnectionState {
    Unknown,
    Disconnected,
    Wifi,
    Cellular,
    Other;

    companion object {
        fun fromNetworkInfo(info: NetworkInfo?): ConnectionState =
                if (info == null || !info.isConnectedOrConnecting)   ConnectionState.Disconnected
                else when(info.type) {
                    ConnectivityManager.TYPE_WIFI -> Wifi
                    ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_MOBILE_DUN -> Cellular
                    else  -> Other
                }
    }
}

fun connectionState(context: Context):ConnectionState {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    try {
        val activeNetwork = cm.activeNetworkInfo
        return ConnectionState.fromNetworkInfo(activeNetwork)
    } catch(e: Exception) {
        return ConnectionState.Unknown
    }
}

fun isNetworkConnected(context:Context):Boolean {

    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    try {
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    } catch(e: Exception) {
        return true
    }
}



fun isStoppedOrDead(state: PlaybackStateCompat): Boolean {
    return state.state == PlaybackStateCompat.STATE_NONE
            || state.state == PlaybackStateCompat.STATE_STOPPED
}

fun ifStoppedOrDead(state: PlaybackStateCompat?, then: ()-> Unit, else_: (() -> Unit)? = null) {
    if (state == null) return
    if (isStoppedOrDead(state)) {
        then()
    } else if (else_ != null) {
        else_()
    }
}

@Throws(IOException::class)
fun copyFile(sourceFile: File, destFile: File) {
    if (!destFile.exists()) {
        destFile.parentFile.mkdirs()
        destFile.createNewFile()
    }

    var source: FileChannel? = null
    var destination: FileChannel? = null

    try {
        source = FileInputStream(sourceFile).getChannel()
        destination = FileOutputStream(destFile).getChannel()
        destination!!.transferFrom(source, 0, source!!.size())
    } finally {
        if (source != null) {
            source!!.close()
        }
        if (destination != null) {
            destination!!.close()
        }
    }
}

