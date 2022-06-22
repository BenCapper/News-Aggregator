package org.ben.news.helpers

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Transformation
import org.ben.news.R
import java.io.IOException


/**
 * `createLoader()` creates a `AlertDialog` with a custom layout and returns it.
 *
 * @param activity The activity that the loader will be displayed in.
 * @return An AlertDialog
 */
fun createLoader(activity: FragmentActivity) : AlertDialog {
    val loaderBuilder = AlertDialog.Builder(activity)
        .setCancelable(true) // 'false' if you want user to wait
        .setView(R.layout.loading)
    val loader = loaderBuilder.create()
    loader.setTitle(R.string.app_name)
    loader.setIcon(R.mipmap.ic_launcher_round)

    return loader
}

/**
 * If the loader is not showing, set the title to the message and show the loader.
 *
 * @param loader The loader to show.
 * @param message The message to be displayed in the loader.
 */
fun showLoader(loader: AlertDialog, message: String) {
    if (!loader.isShowing) {
        loader.setTitle(message)
        loader.show()
    }
}

/**
 * It hides the loader.
 *
 * @param loader AlertDialog
 */
fun hideLoader(loader: AlertDialog) {
    if (loader.isShowing)
        loader.dismiss()
}

/**
 * > This function takes an ActivityResultLauncher as a parameter and uses it to launch an intent to
 * select an image from the user's device
 *
 * @param intentLauncher This is the ActivityResultLauncher that you get from the
 * registerForActivityResult() method.
 */
fun showImagePicker(intentLauncher : ActivityResultLauncher<Intent>) {
    var chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT)
    chooseFile.type = "image/*"
    chooseFile = Intent.createChooser(chooseFile, R.string.select_profile_image.toString())
    //chooseFile.flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    intentLauncher.launch(chooseFile)
}

/**
 * If the result code is OK and the data is not null, then return the data's URI
 *
 * @param resultCode The result code of the activity.
 * @param data Intent? - The intent that was used to start the activity.
 * @return The URI of the image that was selected.
 */
fun readImageUri(resultCode: Int, data: Intent?): Uri? {
    var uri: Uri? = null
    if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
        try { uri = data.data }
        catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return uri
}

/**
 * It creates a custom transformation for Picasso.
 */
fun customTransformation() : Transformation =
    RoundedTransformationBuilder()
        .borderColor(Color.WHITE)
        .borderWidthDp(2F)
        .cornerRadiusDp(35F)
        .oval(false)
        .build()
