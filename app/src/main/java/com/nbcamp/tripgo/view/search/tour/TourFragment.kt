package com.nbcamp.tripgo.view.search.tour

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.FragmentSearchTourBinding

// 관광지
class TourFragment : Fragment() {

    private var _binding: FragmentSearchTourBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchTourBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tourSearchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tourSearchCloseImage.visibility =
                    if (s?.isNotEmpty() == true) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        })  // 텍스트가 비어 있지 않으면 x 버튼 보이게 하고, 비어 있으면 x 버튼이 안 보이게 함.

        binding.tourSearchCloseImage.setOnClickListener {
            binding.tourSearchEdit.text?.clear()
        }   // x 버튼 클릭시 글자 삭제

        // edit_closeBtn 색상 변경
        val closeButtonColor = ContextCompat.getColor(requireContext(), R.color.edit_closeBtn)
        binding.tourSearchCloseImage.setColorFilter(closeButtonColor)  // x 버튼 색상 변경
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
