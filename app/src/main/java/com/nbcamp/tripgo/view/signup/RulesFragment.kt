package com.nbcamp.tripgo.view.signup

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.FragmentRulesBinding

class RulesFragment : DialogFragment() {
    private var _binding: FragmentRulesBinding? = null
    private val binding: FragmentRulesBinding
        get() = _binding!!

    val fireStore = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRulesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = arguments?.getString("data")

        if (data == "terms") {
            binding.rulesTitleTextView.text = getString(R.string.terms_of_use)
        } else if (data == "privacy") {
            binding.rulesTitleTextView.text = getString(R.string.privacy_policy)
        }

        val docRef = data?.let { fireStore.collection("rules").document(it) }
        docRef?.get()?.addOnSuccessListener { document ->
            if (document != null) {
                val content = document.getString("content")
                val contentLineSort = content?.replace("*", "\n")

                binding.rulesContentTextView.text = contentLineSort

            }
        }?.addOnFailureListener { exception ->

        }

        binding.btnBack.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
