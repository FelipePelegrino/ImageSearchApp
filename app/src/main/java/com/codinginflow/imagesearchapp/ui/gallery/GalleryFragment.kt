package com.codinginflow.imagesearchapp.ui.gallery

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.codinginflow.imagesearchapp.R
import com.codinginflow.imagesearchapp.data.UnsplashPhoto
import com.codinginflow.imagesearchapp.databinding.FragmentGalleryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GalleryFragment : Fragment(R.layout.fragment_gallery), UnsplashPhotoAdapter.OnItemClickListener {

    private val viewModel by viewModels<GalleryViewModel>()
    private lateinit var adapter: UnsplashPhotoAdapter
    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGalleryBinding.bind(view)
        setAdapter()
        setObservers()
        setButtons()
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_gallery, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    binding.galleryRv.scrollToPosition(0)
                    viewModel.searchPhotos(query)
                    searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }

    override fun onItemClick(photo: UnsplashPhoto) {
        val action = GalleryFragmentDirections.actionGalleryFragmentToDetailsFragment2(photo)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setObservers() {
        viewModel.photos.observe(viewLifecycleOwner) {
            adapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
    }

    private fun setAdapter() {
        adapter = UnsplashPhotoAdapter(this)
        binding.apply {
            galleryRv.setHasFixedSize(true)
            galleryRv.itemAnimator = null
            galleryRv.adapter = adapter.withLoadStateHeaderAndFooter(
                header = UnsplashPhotoLoadStateAdapter { adapter.retry() },
                footer = UnsplashPhotoLoadStateAdapter { adapter.retry() }
            )
            adapter.addLoadStateListener { loadState -> handleLoadState(loadState) }
        }
    }

    private fun handleLoadState(loadState: CombinedLoadStates) {
        binding.apply {
            galleryPbr.isVisible = loadState.source.refresh is LoadState.Loading
            galleryRv.isVisible = loadState.source.refresh is LoadState.NotLoading
            retryBtn.isVisible = loadState.source.refresh is LoadState.Error
            errorTv.isVisible = loadState.source.refresh is LoadState.Error

            //empty view
            if (loadState.source.refresh is LoadState.NotLoading
                && loadState.append.endOfPaginationReached
                && adapter.itemCount < 1
            ) {
                galleryRv.isVisible = false
                emptyTv.isVisible = true
            } else {
                emptyTv.isVisible = false
            }
        }
    }

    private fun setButtons() {
        binding.retryBtn.setOnClickListener { adapter.retry() }
    }
}