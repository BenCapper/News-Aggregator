package org.ben.news.firebase

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import org.ben.news.helpers.customTransformation
import timber.log.Timber
import java.io.ByteArrayOutputStream

object FirebaseImageManager {

    private var storage = FirebaseStorage.getInstance().reference
    var imageUri = MutableLiveData<Uri>()



    /**
     * Check if the user has a profile picture, if they do, set the imageUri to the download url of the
     * image, if they don't, set the imageUri to an empty Uri
     *
     * @param userid The user's id
     */
    fun checkStorageForExistingProfilePic(userid: String) {
        val imageRef = storage.child("user-images").child("${userid}.jpg")

        imageRef.metadata.addOnSuccessListener { //File Exists
            imageRef.downloadUrl.addOnCompleteListener { task ->
                imageUri.value = task.result!!
            }
            //File Doesn't Exist
        }.addOnFailureListener {
            imageUri.value = Uri.EMPTY
        }
    }

    /**
     * This function uploads an image to Firebase Storage and returns the download URL of the image
     *
     * @param userid The user's id
     * @param bitmap The bitmap of the image you want to upload
     * @param updating Boolean - This is a boolean that is used to determine if the user is updating
     * their profile or not.
     */
    fun uploadImageToFirebase(userid: String, bitmap: Bitmap, updating : Boolean) {
        // Get the data from an ImageView as bytes
        val imageRef = storage.child("user-images").child("${userid}.jpg")
        //val bitmap = (imageView as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        lateinit var uploadTask: UploadTask

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        imageRef.metadata.addOnSuccessListener { //File Exists
            if(updating) // Update existing Image
            {
                uploadTask = imageRef.putBytes(data)
                uploadTask.addOnSuccessListener {
                    it.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                        imageUri.value = task.result!!
                        StoryManager.updateImageRef(userid,imageUri.value.toString())
                    }
                }
            }
        }.addOnFailureListener { //File Doesn't Exist
            uploadTask = imageRef.putBytes(data)
            uploadTask.addOnSuccessListener {
                it.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                    imageUri.value = task.result!!
                }
            }
        }
    }


    /**
     * It uploads an image to firebase storage.
     *
     * @param userid The user's id
     * @param imageUri The Uri of the image to be uploaded
     * @param imageView ImageView - The ImageView that will display the image
     * @param updating Boolean - This is a flag to determine if the user is updating their profile or
     * not.
     */
    fun updateUserImage(userid: String, imageUri : Uri?, imageView: ImageView, updating : Boolean) {
        Picasso.get().load(imageUri)
            .resize(200, 200)
            .transform(customTransformation())
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .centerCrop()
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?,
                                            from: Picasso.LoadedFrom?
                ) {
                    Timber.i("DX onBitmapLoaded $bitmap")
                    uploadImageToFirebase(userid, bitmap!!,updating)
                    imageView.setImageBitmap(bitmap)
                }

                override fun onBitmapFailed(e: java.lang.Exception?,
                                            errorDrawable: Drawable?) {
                    Timber.i("DX onBitmapFailed $e")
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    Timber.i("DX onPrepareLoad $placeHolderDrawable")
                    //uploadImageToFirebase(userid, defaultImageUri.value,updating)
                }
            })
    }

    /**
     * It uploads a default image to Firebase Storage.
     *
     * @param userid The user's id
     * @param resource The resource ID of the image to be loaded.
     * @param imageView ImageView - the imageView that will be updated with the image
     */
    fun updateDefaultImage(userid: String, resource: Int, imageView: ImageView) {
        Picasso.get().load(resource)
                .resize(200, 200)
                .transform(customTransformation())
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .centerCrop()
                .into(object : Target {
                    override fun onBitmapLoaded(bitmap: Bitmap?,
                                                from: Picasso.LoadedFrom?
                    ) {
                        Timber.i("DX onBitmapLoaded $bitmap")
                        uploadImageToFirebase(userid, bitmap!!,false)
                        imageView.setImageBitmap(bitmap)
                    }

                    override fun onBitmapFailed(e: java.lang.Exception?,
                                                errorDrawable: Drawable?) {
                        Timber.i("DX onBitmapFailed $e")
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                        Timber.i("DX onPrepareLoad $placeHolderDrawable")
                        //uploadImageToFirebase(userid, defaultImageUri.value,updating)
                    }
                })
    }
}