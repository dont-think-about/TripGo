package com.nbcamp.tripgo.view.search.festival

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.FragmentSearchFestivalBinding
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.view.search.SearchKeywordUiState
import com.nbcamp.tripgo.view.search.SearchViewModel
import java.util.Calendar

// 축제
class FestivalFragment : Fragment() {
    private lateinit var startDateString: String
    private val viewModel: FestivalViewModel by viewModels { FestivalViewModelFactory() }
    private val searchViewModel: SearchViewModel by activityViewModels()
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

        // EditText의 텍스트 변경을 감지하는 TextWatcher를 추가합니다.
        binding.festivalSearchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트가 비어 있지 않으면 'attractionsSearchCloseImage'를 보이도록 설정, 비어 있으면 숨김.
                binding.festivalSearchCloseImage.visibility =
                    if (s?.isNotEmpty() == true) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 검색 버튼(ImageView) 클릭 시 동작 설정
        binding.festivalSearchOk.setOnClickListener {
            val searchText = binding.festivalSearchEdit.text.toString()
            if (::startDateString.isInitialized) {
                if (searchText.length >= 2) {
                    viewModel.fetchSearchResult(keyword = searchText, startDate = startDateString)
                    hideKeyboard() // 키보드 숨김
                } else {
                    Toast.makeText(context, "두 글자 이상의 검색어를 입력해주세요!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "날짜를 입력해주세요!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.festivalSearchWeather.setOnClickListener {
            val cal = Calendar.getInstance()
            val dateSetListener =
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    Log.d("날짜", "$year, $month, $dayOfMonth")
                    val monthStr = if (month < 9) "0${month + 1}" else "${month + 1}"
                    val dayOfMonthStr = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                    startDateString = "$year$monthStr$dayOfMonthStr"
                    binding.festivalSearchWeather.text = "$year-$monthStr-$dayOfMonthStr"
                }
            DatePickerDialog(
                requireActivity(),
                AlertDialog.THEME_HOLO_LIGHT,
                dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        // 'FestivalSearchCloseImage' 클릭 시 EditText 내용을 지웁니다.
        binding.festivalSearchCloseImage.setOnClickListener {
            binding.festivalSearchEdit.text?.clear()
        }

        // 'edit_closeBtn' 색상 설정
        val closeButtonColor = ContextCompat.getColor(requireContext(), R.color.edit_closeBtn)
        binding.festivalSearchCloseImage.setColorFilter(closeButtonColor)
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
        imm.hideSoftInputFromWindow(binding.festivalSearchEdit.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // ViewBinding 인스턴스를 메모리에서 해제
    }
}
