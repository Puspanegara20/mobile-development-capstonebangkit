package com.example.capstone.ui.explore

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone.data.Result
import com.example.capstone.databinding.FragmentExploreBinding
import com.example.capstone.pref.SessionManager
import com.example.capstone.ui.factory.ViewModelFactory

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val viewModel: SearchViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }
    private lateinit var searchAdapter: SearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        val token = sessionManager.getAuthToken().toString()

        searchAdapter = SearchAdapter { selectedItem ->
            Toast.makeText(requireContext(), "Clicked: ${selectedItem.name} with id ${selectedItem.id}", Toast.LENGTH_SHORT).show()
        }

        binding.rcSearch.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }
        with(binding) {
            searchView.setupWithSearchBar(searchBar)
            searchView
                .editText
                .setOnEditorActionListener { textView, actionId, event ->
                    val foodName = textView.text.toString()
                    if (foodName.isNotBlank()) {
                        searchBar.setText(foodName)
                        searchView.hide()
                        performSearch(token, foodName)
                    } else {
                        Toast.makeText(requireContext(), "Input tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
            val recommendations = listOf<TextView>(
                recommendation1, recommendation2, recommendation3
            )

            recommendations.forEach { textView ->
                textView.setOnClickListener {
                    val selectedText = textView.text.toString()
                    searchView.editText.setText(selectedText)
                    searchBar.setText(selectedText)
                    searchView.hide()
                    performSearch(token, selectedText)
                }
            }
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }
        viewModel.searchResult.observe(viewLifecycleOwner) { result ->
            when(result) {
                is Result.Loading -> showLoading(true)
                is Result.Error -> {
                    Toast.makeText(requireContext(), result.error, Toast.LENGTH_SHORT).show()
                }
                is Result.Success -> {
                    val foods = result.data.data
                    if (foods.isEmpty()) {
                        Toast.makeText(requireContext(), "Tidak ada data ditemukan.", Toast.LENGTH_SHORT).show()
                    } else {
                        searchAdapter.submitList(foods)
                    }

                }
            }

        }
    }

    private fun performSearch(token: String, foodName: String) {
        viewModel.searchFood(token, page = 1, limit = 10, name = foodName, tags = null)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
