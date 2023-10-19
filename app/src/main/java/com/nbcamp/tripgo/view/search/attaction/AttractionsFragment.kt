package com.nbcamp.tripgo.view.search.attaction

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.FragmentSearchAttractionsBinding

class AttractionsFragment : Fragment() {

    private lateinit var editText: EditText
    private lateinit var searchButton: ImageView
    private var _binding: FragmentSearchAttractionsBinding? = null
    private val binding get() = _binding!!

    // 인터페이스를 정의합니다.
    interface OnSearchListener {
        fun onSearch(searchText: String)
    }

    private var searchListener: OnSearchListener? = null

    // 인터페이스 리스너를 연결합니다.
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSearchListener) {
            searchListener = context
        }
    }

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

        editText = binding.attractionsSearchEdit
        searchButton = binding.attractionsSearchOk  // 'searchButton'이 OK 버튼이라고 가정합니다.

        // EditText의 텍스트 변경을 감지하는 TextWatcher를 추가합니다.
        binding.attractionsSearchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트가 비어 있지 않으면 'attractionsSearchCloseImage'를 보이도록 설정, 비어 있으면 숨김.
                binding.attractionsSearchCloseImage.visibility =
                    if (s?.isNotEmpty() == true) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 검색 버튼(ImageView) 클릭 시 동작 설정
        searchButton.setOnClickListener {
            val searchText = editText.text.toString()
            searchListener?.onSearch(searchText) // 검색 결과를 액티비티로 전달
            hideKeyboard() // 키보드 숨김
        }

        // 'attractionsSearchCloseImage' 클릭 시 EditText 내용을 지웁니다.
        binding.attractionsSearchCloseImage.setOnClickListener {
            editText.text?.clear()
        }

        // 'edit_closeBtn' 색상 설정
        val closeButtonColor = ContextCompat.getColor(requireContext(), R.color.edit_closeBtn)
        binding.attractionsSearchCloseImage.setColorFilter(closeButtonColor)
    }

    // 키보드를 숨기는 메서드
    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // ViewBinding 인스턴스를 메모리에서 해제
    }
}
