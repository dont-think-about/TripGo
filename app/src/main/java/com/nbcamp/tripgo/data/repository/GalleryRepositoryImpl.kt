package com.nbcamp.tripgo.data.repository

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.nbcamp.tripgo.data.repository.model.GalleryPhotoEntity
import com.nbcamp.tripgo.view.reviewwriting.gallery.GalleryPhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GalleryRepositoryImpl(
    private val context: Context
) : GalleryPhotoRepository {
    override suspend fun getAllPhotos(): MutableList<GalleryPhotoEntity> =
        withContext(Dispatchers.IO) {
            val galleryPhotoList = mutableListOf<GalleryPhotoEntity>()

            // 외장 메모리 (휴대폰 갤러리)에서 사진을 전부 가져와 새로운 액티비티에 가져와서 보여줘야하기 때문에
            // 사진을 가져올 외부 컨텐츠 uri를 가져옴
            val uriExternal: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            // 쿼리에서 커서를 옮기면서 사진을 찾아 넣어야해서 쿼리(이미지 정보 등)를 가져옴
            val query: Cursor?

            // 가져올 정보를 담은 배열
            val projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                arrayOf(
                    MediaStore.Images.ImageColumns.DISC_NUMBER,
                    MediaStore.Images.ImageColumns.SIZE,
                    MediaStore.Images.ImageColumns.DATE_TAKEN,
                    MediaStore.Images.ImageColumns.DATE_ADDED,
                    MediaStore.Images.ImageColumns._ID
                )
            } else {
                arrayOf(
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.SIZE,
                    MediaStore.Images.ImageColumns.DATE_TAKEN,
                    MediaStore.Images.ImageColumns.DATE_ADDED,
                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
                )
            }

            // 연결하기위한 content resolver(provider)
            val resolver = context.contentResolver
            // 쿼리와 연결
            query = resolver?.query(
                uriExternal,
                projection,
                null,
                null,
                "${MediaStore.Images.ImageColumns.DATE_ADDED} DESC"
            )

            // 커서를 사용하여 정보를 받아와서 정보를 GalleryPhoto 객체로 만들고 배열에 집어넣어 전체 사진 리스트 완성
            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
                val nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_ADDED)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.SIZE)
                val dateColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN)
                // 커서를 계속 움직이면서 이미지가 없을 때 까지 계속 배열에 집어넣음
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val size = cursor.getInt(sizeColumn)
                    val date = cursor.getString(dateColumn)

                    val contentUri = ContentUris.withAppendedId(uriExternal, id)

                    galleryPhotoList.add(
                        GalleryPhotoEntity(
                            id,
                            uri = contentUri,
                            name = name,
                            date = date ?: "",
                            size = size
                        )
                    )
                }
            }
            return@withContext galleryPhotoList
        }
}
