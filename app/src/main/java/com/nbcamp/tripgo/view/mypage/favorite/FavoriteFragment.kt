package com.nbcamp.tripgo.view.mypage.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.view.App

class FavoriteFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)
        favoritedata(view)

        val callback = object  : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        }

        val favoriteBackButton = view.findViewById<ImageView>(R.id.profile_favorite_back_imagebutton)

        favoriteBackButton.setOnClickListener {
            callback.handleOnBackPressed()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        return view
    }

    companion object {

        const val TAG = "Favorite_FRAGMENT"
        fun newInstance() = FavoriteFragment()

    }

    private fun favoritedata(view: View) {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val userEmail = user?.email
        val firestoredb = FirebaseFirestore.getInstance()
        val kakaouser = App.kakaoUser?.email

        val favorite_realstoredb = when {
            kakaouser == null -> firestoredb.collection("liked").document(userEmail.toString()).collection("like")
            kakaouser != null -> firestoredb.collection("liked").document(kakaouser.toString()).collection("like")
            else -> {
                // Handle the case when neither Google nor Kakao login is available
                return
            }
        }

        favorite_realstoredb.get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val favoriteItems = ArrayList<FavoriteItem>()
                for (document in querySnapshot) {
                    val favoritedata = document.data
                    val favoritetitle = favoritedata["title"] as? String ?: ""
                    val favoriteimageUrl = favoritedata["imageUrl"] as? String ?: ""
                    val favoritedescription = favoritedata["description"] as? String ?: ""
                    val favoriteaddress = favoritedata["mainAddress"] as? String ?: ""
                    val collectionPath = document.reference.path

                    val favoriteItem = FavoriteItem(
                        favoritetitle,
                        favoriteimageUrl,
                        favoritedescription,
                        favoriteaddress,
                        isSelected = false,
                        collectionPath = collectionPath
                    )
                    favoriteItems.add(favoriteItem)
                }
                view.findViewById<TextView>(R.id.notice_no_favorite)?.isVisible = favoriteItems.isEmpty()

                val favorite_recyclerview = view.findViewById<RecyclerView>(R.id.favorite_recyclerview)
                val favoriteAdapter = FavoriteAdapter()
                favoriteAdapter.setData(favoriteItems)

                favorite_recyclerview.adapter = favoriteAdapter
                favorite_recyclerview.layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }
}
