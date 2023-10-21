package com.nbcamp.tripgo.view.reviewwriting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseUser
import com.nbcamp.tripgo.databinding.FragmentReviewWritingBinding
import com.nbcamp.tripgo.view.main.MainViewModel

class ReviewWritingFragment : Fragment() {

    private var _binding: FragmentReviewWritingBinding? = null
    private val binding: FragmentReviewWritingBinding
        get() = _binding!!

    private val sharedViewModel: MainViewModel by activityViewModels()
    private lateinit var calendarUserEntity: CalendarUserEntity
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewWritingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initSharedViewModel()
    }

    private fun initViews() = with(binding) {
        reviewWritingButtonBack.setOnClickListener {
            parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            sharedViewModel.onClickBackButton()
        }
    }

    private fun initSharedViewModel() = with(sharedViewModel) {
        calendarToReviewModel.observe(viewLifecycleOwner) { model ->
            calendarUserEntity = model
            println("------------")
            println(model.model)
            println((model.currentUser as FirebaseUser).email)
            println("------------")
        }
    }


    companion object {
        fun newInstance() = ReviewWritingFragment()
    }
}
