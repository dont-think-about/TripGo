package com.nbcamp.tripgo.view.search.tour

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.FragmentSearchTourBinding
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.view.search.SearchKeywordUiState
import com.nbcamp.tripgo.view.search.SearchViewModel

// 관광지
class TourFragment : Fragment() {

    private val viewModel: TourViewModel by viewModels { TourViewModelFactory() }
    private val searchViewModel: SearchViewModel by activityViewModels()
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

        // EditText의 텍스트 변경을 감지하는 TextWatcher를 추가합니다.
        binding.tourSearchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트가 비어 있지 않으면 'attractionsSearchCloseImage'를 보이도록 설정, 비어 있으면 숨김.
                binding.tourSearchCloseImage.visibility =
                    if (s?.isNotEmpty() == true) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 검색 버튼(ImageView) 클릭 시 동작 설정
        binding.tourSearchOk.setOnClickListener {
            val searchText = binding.tourSearchEdit.text.toString()

            // 텍스트의 길이가 최소 두 글자인지 확인
            if (searchText.length >= 2) {
                viewModel.fetchSearchResult(keyword = searchText)
                hideKeyboard() // 키보드 숨김
            } else {
                requireActivity().toast("두 글자 이상을 입력하십시오.")
            }
        }

        // 'tourSearchCloseImage' 클릭 시 EditText 내용을 지웁니다.
        binding.tourSearchCloseImage.setOnClickListener {
            binding.tourSearchEdit.text?.clear()
        }

        // 'edit_closeBtn' 색상 설정
        val closeButtonColor = ContextCompat.getColor(requireContext(), R.color.edit_closeBtn)
        binding.tourSearchCloseImage.setColorFilter(closeButtonColor)
        viewModel.searchUiState.observe(viewLifecycleOwner) { state ->
            if (state == SearchKeywordUiState.error()) {
                requireActivity().toast(getString(R.string.load_failed_data))
                return@observe
            }
            Log.d("키워드", "값 = $state")
            searchViewModel.sendSearchData(state.list)
        }
    }

    // 키보드를 숨기는 메서드
    private fun hideKeyboard() {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.tourSearchEdit.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // ViewBinding 인스턴스를 메모리에서 해제
    }
}
