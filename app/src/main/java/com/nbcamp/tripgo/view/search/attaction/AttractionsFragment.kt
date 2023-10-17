package com.nbcamp.tripgo.view.search.attaction

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.FragmentSearchAttractionsBinding

class AttractionsFragment : Fragment() {


    private var _binding: FragmentSearchAttractionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSearchAttractionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.attractionsSearchEdit.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                binding.attractionsSearchCloseImage.visibility =

                    if (s?.isNotEmpty() == true) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })  // 텍스트가 비어 있지 않으면 x 버튼 보이게 하고, 비어 있으면 x 버튼이 안 보이게 함.




        binding.attractionsSearchCloseImage.setOnClickListener {
            binding.attractionsSearchEdit.text?.clear()
        }   // x 버튼 클릭시 전부 지움.


        val closeButtonColor = ContextCompat.getColor(requireContext(), R.color.edit_closeBtn)
        binding.attractionsSearchCloseImage.setColorFilter(closeButtonColor)
    }  // edit_closeBtn 색상 변경

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // ViewBinding 인스턴스를 메모리에서 해제
    }
}
