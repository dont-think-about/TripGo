package com.nbcamp.tripgo.view.search.tour

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

    private var autoCompleteCityList: List<String>? = null
    private lateinit var searchText: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchTourBinding.inflate(inflater, container, false)
        autoCompleteCityList = getJsonDataFromAsset()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        autoCompleteCityList?.let {
            val autoCompleteAdapter =
                ArrayAdapter(
                    requireActivity(),
                    R.layout.simple_list,
                    R.id.auto_complete_text_view,
                    it.toMutableList()
                )
            binding.tourSearchEdit.run {
                setAdapter(autoCompleteAdapter)
                setOnItemClickListener { _, _, _, _->
                    searchText = binding.tourSearchEdit.text.toString()
                }
            }
        }

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
            searchText = binding.tourSearchEdit.text.toString()
            if (::searchText.isInitialized && searchText.length >= 2) {
                viewModel.fetchSearchResult(keyword = searchText)
                hideKeyboard() // 키보드 숨김
            } else {
                requireActivity().toast("두 글자 이상을 입력하십시오.")
            }
        }
        binding.tourSearchEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchText = binding.tourSearchEdit.text.toString()
                if (::searchText.isInitialized && searchText.length >= 2) {
                    viewModel.fetchSearchResult(keyword = searchText)
                    hideKeyboard() // 키보드 숨김
                } else {
                    requireActivity().toast("두 글자 이상을 입력하십시오.")
                }
                return@setOnEditorActionListener true
            }
            false
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
            searchViewModel.sendSearchData(state.list)
        }
    }

    // 키보드를 숨기는 메서드
    private fun hideKeyboard() {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.tourSearchEdit.windowToken, 0)
    }

    private fun getJsonDataFromAsset(): List<String>? {
        val str: List<String>
        try {
            val jsonObject = requireActivity().assets.open("city_name.json")
                .reader().readText()
            val strListType = object : TypeToken<List<String>>() {}.type
            str = Gson().fromJson(jsonObject, strListType)
        } catch (e: Exception) {
            return null
        }
        return str
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // ViewBinding 인스턴스를 메모리에서 해제
    }
}
