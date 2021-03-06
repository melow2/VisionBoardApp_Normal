package com.khs.lovelynote.extension

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.khs.lovelynote.extension.Constants.APPLICATION_HWP
import com.khs.lovelynote.extension.Constants.APPLICATION_MSWORD
import com.khs.lovelynote.extension.Constants.APPLICATION_PDF
import com.khs.lovelynote.extension.Constants.APPLICATION_PPTX
import com.khs.lovelynote.extension.Constants.APPLICATION_XLSX
import com.khs.lovelynote.extension.Constants.AUDIO_MIME_TYPE
import com.khs.lovelynote.extension.Constants.BMP
import com.khs.lovelynote.extension.Constants.DOC
import com.khs.lovelynote.extension.Constants.DOCX
import com.khs.lovelynote.extension.Constants.GIF
import com.khs.lovelynote.extension.Constants.HWP
import com.khs.lovelynote.extension.Constants.IMAGE_MIME_TYPE
import com.khs.lovelynote.extension.Constants.JPEG
import com.khs.lovelynote.extension.Constants.JPG
import com.khs.lovelynote.extension.Constants.MP3
import com.khs.lovelynote.extension.Constants.MP4
import com.khs.lovelynote.extension.Constants.PDF
import com.khs.lovelynote.extension.Constants.PNG
import com.khs.lovelynote.extension.Constants.PPT
import com.khs.lovelynote.extension.Constants.PPTX
import com.khs.lovelynote.extension.Constants.TXT
import com.khs.lovelynote.extension.Constants.TXT_MIME_TYPE
import com.khs.lovelynote.extension.Constants.VIDEO_MIME_TYPE
import com.khs.lovelynote.extension.Constants.XLS
import com.khs.lovelynote.extension.Constants.XLSX
import com.khs.lovelynote.model.mediastore.MediaStoreFileType
import com.khs.lovelynote.model.mediastore.MediaStoreItem
import org.apache.commons.io.IOUtils
import timber.log.Timber
import java.io.*

/**
 * 파일 확장자.
 *
 * - 파일이름에 확장자가 명시되 어있지 않다면 empty를 리턴.
 * @param fileStr 파일의 실제 uri 주소 string.
 * @author 권혁신
 * @version 1.0.0
 * @since 2020-08-23 오후 1:11
 **/
fun getExtension(fileStr: String): String {

    val name = fileStr.substringBeforeLast('.')
    val extension = fileStr.substringAfterLast('.')
    return if (name == extension) ""
    else extension
}

/**
 * 파일을 확장자에 맞는 앱으로 실행할 수 있도록 도움.
 *
 * @param filePath 대상이 되는 파일의 URI
 * @param fileName 대상이 되는 파일의 파일 이름. (pdf,hwp ..)
 * @author 권혁신
 * @version 1.0.0
 * @since 2020-08-23 오후 1:49
 **/
fun Context.viewFile(filePath: Uri?, fileName: String?) {
    val fileLinkIntent = Intent(Intent.ACTION_VIEW).apply {
        addCategory(Intent.CATEGORY_DEFAULT)
    }
    val file = File(filePath.toString(), fileName)
    val fileExtend = getExtension(file.absolutePath)
    if (fileExtend.equals(MP3, ignoreCase = true)) {
        fileLinkIntent.setDataAndType(filePath, AUDIO_MIME_TYPE)
    } else if (fileExtend.equals(MP4, ignoreCase = true)) {
        fileLinkIntent.setDataAndType(filePath, VIDEO_MIME_TYPE)
    } else if (fileExtend.equals(JPG, ignoreCase = true)
        || fileExtend.equals(JPEG, ignoreCase = true)
        || fileExtend.equals(GIF, ignoreCase = true)
        || fileExtend.equals(PNG, ignoreCase = true)
        || fileExtend.equals(BMP, ignoreCase = true)
    ) {
        fileLinkIntent.setDataAndType(filePath, IMAGE_MIME_TYPE)
    } else if (fileExtend.equals(TXT, ignoreCase = true)) {
        fileLinkIntent.setDataAndType(filePath, TXT_MIME_TYPE)
    } else if (fileExtend.equals(DOC, ignoreCase = true) || fileExtend.equals(
            DOCX,
            ignoreCase = true
        )
    ) {
        fileLinkIntent.setDataAndType(filePath, APPLICATION_MSWORD)
    } else if (fileExtend.equals(XLS, ignoreCase = true) || fileExtend.equals(
            XLSX,
            ignoreCase = true
        )
    ) {
        fileLinkIntent.setDataAndType(filePath, APPLICATION_XLSX)
    } else if (fileExtend.equals(PPT, ignoreCase = true) || fileExtend.equals(
            PPTX,
            ignoreCase = true
        )
    ) {
        fileLinkIntent.setDataAndType(filePath, APPLICATION_PPTX)
    } else if (fileExtend.equals(PDF, ignoreCase = true)) {
        fileLinkIntent.setDataAndType(filePath, APPLICATION_PDF)
    } else if (fileExtend.equals(HWP, ignoreCase = true)) {
        fileLinkIntent.setDataAndType(filePath, APPLICATION_HWP)
    }
    val pm: PackageManager = this.packageManager
    val list = pm.queryIntentActivities(
        fileLinkIntent,
        PackageManager.GET_META_DATA
    )
    if (list.size == 0) {
        Toast.makeText(this, "$fileName need an app that can run.", Toast.LENGTH_SHORT).show()
    } else {
        this.startActivity(fileLinkIntent.apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        })
    }
}


/**
 * content uri을 읽어 대상 파일을 복사 시킴.
 *
 * - content Uri만 가능.
 * - storage 주소가 들어오면 contentResolver가 동작하지 않음.
 * @param sourceUri 복사 대상 파일의 uri
 * @param destFile 복사 완료 시 저장 될 파일.
 * @author 권혁신
 * @version 1.0.0
 * @since 2020-08-23 오후 1:50
 **/
fun Context.copyContentUri(sourceUri: Uri?, destFile: File): Uri? {
    try {
        val inputStream: InputStream =
            sourceUri?.let { contentResolver.openInputStream(it) } ?: return null
        val outputStream: OutputStream = FileOutputStream(destFile)
        IOUtils.copy(inputStream, outputStream)
        inputStream.close()
        outputStream.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return if (Build.VERSION.SDK_INT < 24) {
        Uri.fromFile(destFile)
    } else {
        FileProvider.getUriForFile(this, applicationContext.packageName + ".fileprovider", destFile)
    }
}


/**
 * storage uri을 읽어 대상 파일을 복사 시킴.
 *
 * - storage Uri만 가능.
 * - content 주소가 들어오면 동작하지 않음.
 * @param uri 복사 대상 파일의 uri
 * @param destination 복사 완료 시 저장 될 파일.
 * @author 권혁신
 * @version 1.0.0
 * @since 2020-08-23 오후 1:50
 **/
fun Context.copyStorageUri(uri: Uri?, destination: File): Uri? {
    try {
        val filePath = uri?.let { getPath(this, it) }
        val source = File(filePath);
        val src = FileInputStream(source).channel;
        val dst = FileOutputStream(destination).channel;
        dst.transferFrom(src, 0, src.size());
        src.close();
        dst.close();
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return if (Build.VERSION.SDK_INT < 24) {
        Uri.fromFile(destination)
    } else {
        FileProvider.getUriForFile(
            this,
            applicationContext.packageName + ".fileprovider",
            destination
        )
    }
}

/**
 * 복사대상의 임의파일을 생성.
 *
 * @param dirId 복사할 곳의 디렉토리 ID
 * @param mediaItem 복사할 파일의 이름.
 * @author 권혁신
 * @version 1.0.0
 * @since 2020-08-23 오후 1:50
 **/
fun Context.createMediaFile(dirId: String, mediaItem: MediaStoreItem?): File {
    val dirPath = File(getExternalFilesDir(null), dirId)
    if (!dirPath.exists()) {
        dirPath.mkdirs()
    }
    val file = File(Uri.parse(mediaItem?.contentUri).path)
    var fileName = file.name.substringBeforeLast('.')
    var fileExtend = getExtension(file.absolutePath)
    if (fileExtend.isEmpty()) {
        fileName = mediaItem?.displayName?.substringBeforeLast('.').toString()
        fileExtend = when (mediaItem?.type) {
            MediaStoreFileType.IMAGE -> PNG
            MediaStoreFileType.AUDIO -> MP3
            MediaStoreFileType.VIDEO -> MP4
            else -> {
                mediaItem?.displayName?.substringAfterLast('.').toString()
            }
        }
    }
    return File(dirPath, "${fileName}.${fileExtend}")
}

/**
 * contentUri의 파일을 삭제.
 *
 * @author 권혁신
 * @version 1.0.0
 * @since 2020-08-23 오후 1:59
 **/
fun Uri.delete(contentResolver: ContentResolver?) {
    contentResolver?.delete(this, null, null)
}

/**
 * 디렉토리 삭제.
 *
 * @param mRootPath 디렉토리 경로
 * @author 권혁신
 * @version 1.0.0
 * @since 2020-09-02 오전 10:42
 **/
fun Context.clearDirData(dirId: String) {
    val dirPath = File(getExternalFilesDir(null), dirId)
    val childList = dirPath.listFiles()
    if (dirPath.exists()) {
        for (file in childList) {
            if (file.isDirectory) {
                clearDirData(file.absolutePath)
            } else {
                file.delete()
            }
        }
        dirPath.delete()
    }
}

/**
 * 모든 캐시 데이터를 삭제.
 *
 * - 녹음 시 캐시데이터가 생성되기때문에 삭제해야 함.
 * @author 권혁신
 * @version 1.0.0
 * @since 2020-08-23 오후 2:00
 **/
fun Context.clearCacheData() {
    val cache = externalCacheDir
    if (cache != null) {
        if (cache.isDirectory) {
            val children: Array<String> = cache.list()
            for (i in children.indices) {
                File(cache, children[i]).delete()
            }
        }
    }
}

/**
 * 파일이 존재하는지 확인.
 * @param dirId 디렉토리 id
 * @param fName 파일 이름.
 * @author 권혁신
 * @version 1.0.0
 * @since 2020-09-02 오후 4:51
 **/
fun Context.isFileExist(dirId: String, fName: String): Boolean {
    val dirPath = File(getExternalFilesDir(null), dirId)
    val childList = dirPath.listFiles()
    if (dirPath.exists()) {
        for (file in childList) {
            if (file.name == fName)
                return true
        }
    }
    return false
}

/**
 * 사용할 수 있는 contentUri인지(uri에 파일이 존재하는지 확인)
 * @author 권혁신
 * @version 1.0.0
 * @since 2020-09-02 오후 4:56
 **/
fun Context.isContentUriAvailable(uri: Uri?): Boolean {
    var cursor: Cursor? = null
    try {
        cursor = contentResolver.query(
            uri!!, null, null, null,
            null
        )
        if (cursor != null && cursor.moveToFirst()) {
            return true
        }
    } finally {
        cursor?.close()
    }
    return false
}

/**
 * 파일의 실제 경로를 읽어 옴.
 *
 * - content파일의 실제 경로.
 * - content파일이 아닐 경우 원래의 uri를 리턴.
 * - 오디오,이미지,비디오 파일의 경우 실제 storage 경로를 통해서 saveFile로 저장하지만,
 * - pdf,hwp,xls 등의 문서파일의 경우 copyFileUri 로 파일을 복사한다.
 * @param uri 대상의 content uri
 * @author 권혁신
 * @version 1.0.0
 * @since 2020-08-23 오후 2:02
 **/
fun getPath(context: Context, uri: Uri): String? {
    val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
    val scheme = uri.scheme
    // DocumentProvider
    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
        // ExternalStorageProvider
        if (isExternalStorageDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).toTypedArray()
            val type = split[0]
            if ("primary".equals(type, ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            }
            // TODO handle non-primary volumes
        } else if (isDownloadsDocument(uri)) {
            val id = DocumentsContract.getDocumentId(uri)
            val contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"),
                java.lang.Long.valueOf(id)
            )
            return getDataColumn(context, contentUri, null, null)
        } else if (isMediaDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).toTypedArray()
            val type = split[0]
            var contentUri: Uri? = null
            if ("image" == type) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else if ("video" == type) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else if ("audio" == type) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            val selection = "_id=?"
            val selectionArgs = arrayOf(
                split[1]
            )
            return getDataColumn(context, contentUri, selection, selectionArgs)
        }
    } else if ("content".equals(uri.scheme, ignoreCase = true)) {
        return getDataColumn(context, uri, null, null)
    } else if ("file".equals(uri.scheme, ignoreCase = true)) {
        Timber.d("getPath, context: $context, uri: $uri")
        return uri.path
    }
    return uri.path // scheme이 null 일 경우 storage.
}

fun Context.getPath(string: String?): String? {
    return getPath(this, Uri.parse(string))
}


/**
 * Get the value of the data column for this Uri. This is useful for
 * MediaStore Uris, and other file-based ContentProviders.
 *
 * @param context The context.
 * @param uri The Uri to query.
 * @param selection (Optional) Filter used in the query.
 * @param selectionArgs (Optional) Selection arguments used in the query.
 * @return The value of the _data column, which is typically a file path.
 */
fun getDataColumn(
    context: Context, uri: Uri?, selection: String?,
    selectionArgs: Array<String>?
): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(
        column
    )
    try {
        cursor = context.contentResolver.query(
            uri!!, projection, selection, selectionArgs,
            null
        )
        if (cursor != null && cursor.moveToFirst()) {
            val column_index: Int = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(column_index)
        }
    } finally {
        cursor?.close()
    }
    return null
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is ExternalStorageProvider.
 */
fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is DownloadsProvider.
 */
fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is MediaProvider.
 */
fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}