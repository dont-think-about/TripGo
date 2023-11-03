package com.nbcamp.tripgo.view.signup

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.FragmentRulesBinding
import java.util.regex.Pattern

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
//                val redColor = Color.RED
//
//                val text = SpannableString(content)
//                val pattern = "이용"
//                val matcher = Pattern.compile(pattern).matcher(content)
//
//                while (matcher.find()) {
//                    val start = matcher.start()
//                    val end = matcher.end()
//                    text.setSpan(ForegroundColorSpan(redColor), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
//                }
//
//               binding.rulesContentTextView.text = text
//















//                binding.rulesContentTextView.text = content

            }
        }?.addOnFailureListener { exception ->
            Log.d("test1234555", "get failed with ", exception)
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