package com.nbcamp.tripgo.view.search.attaction

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.nbcamp.tripgo.databinding.FragmentSearchAttractionsBinding
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.view.search.SearchKeywordUiState
import com.nbcamp.tripgo.view.search.SearchViewModel

// 문화생활
class AttractionsFragment : Fragment() {
    private val viewModel: AttractionsViewModel by viewModels { AttractionsViewModelFactory() }
    private val searchViewModel: SearchViewModel by activityViewModels()
    private var _binding: FragmentSearchAttractionsBinding? = null
    private val binding get() = _binding!!

    private var autoCompleteCityList: List<String>? = null
    private lateinit var searchText: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchAttractionsBinding.inflate(inflater, container, false)
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
            binding.attractionsSearchEdit.run {
                setAdapter(autoCompleteAdapter)
                setOnItemClickListener { _, _, _, _ ->
                    searchText = binding.attractionsSearchEdit.text.toString()
                }
            }
        }

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
        binding.attractionsSearchOk.setOnClickListener {
            searchText = binding.attractionsSearchEdit.text.toString()

            // 텍스트의 길이가 최소 두 글자인지 확인
            if (::searchText.isInitialized && searchText.length >= 2) {
                viewModel.fetchSearchResult(keyword = searchText)
                hideKeyboard() // 키보드 숨김
            } else {
                requireActivity().toast("두 글자 이상을 입력하십시오.")
            }
        }
        // 키보드 완료 버튼 클릭시
        binding.attractionsSearchEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchText = binding.attractionsSearchEdit.text.toString()

                // 텍스트의 길이가 최소 두 글자 인지 확인
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

        // 'attractionsSearchCloseImage' 클릭 시 EditText 내용을 지웁니다.
        binding.attractionsSearchCloseImage.setOnClickListener {
            binding.attractionsSearchEdit.text?.clear()
        }

        // 'edit_closeBtn' 색상 설정
        val closeButtonColor = ContextCompat.getColor(requireContext(), R.color.edit_closeBtn)
        binding.attractionsSearchCloseImage.setColorFilter(closeButtonColor)
        viewModel.searchUiState.observe(viewLifecycleOwner) { state ->
            if (state == SearchKeywordUiState.error()) {
                requireActivity().toast(getString(R.string.load_failed_data))
                return@observe
            }
            Log.d("키워드", "값 = $state")
            searchViewModel.sendSearchData(state.list)
        }
        // 전달받은 아이템 정보를 화면에 표시
        val clickedItem = arguments?.getString("clickedItem")
// clickedItem을 화면에 표시하는 EditText에 설정
        binding.attractionsSearchEdit.setText(clickedItem)
    }

    // 키보드를 숨기는 메서드
    private fun hideKeyboard() {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.attractionsSearchEdit.windowToken, 0)
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
