package com.nbcamp.tripgo.view.search.Festival

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.FragmentSearchFestivalBinding

class FestivalFragment : Fragment() {


    private var _binding: FragmentSearchFestivalBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSearchFestivalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.festivalSearchEdit.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.festivalSearchCloseImage.visibility =
                    if (s?.isNotEmpty() == true) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })  // 텍스트가 비어 있지 않으면 x 버튼 보이게 하고, 비어 있으면 x 버튼이 안 보이게 함.


        binding.festivalSearchCloseImage.setOnClickListener {
            binding.festivalSearchEdit.text?.clear()
        }  // x 버튼 클릭시 전부 지움.

        // edit_closeBtn 색상 변경
        val closeButtonColor = ContextCompat.getColor(requireContext(), R.color.edit_closeBtn)
        binding.festivalSearchCloseImage.setColorFilter(closeButtonColor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
