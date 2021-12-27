package com.example.gitrepoexplorer.ui.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.example.gitrepoexplorer.R
import com.example.gitrepoexplorer.ui.adapter.recyclerview.RepoListAdapter
import com.example.gitrepoexplorer.data.remote.GitRepos
import com.example.gitrepoexplorer.databinding.FragmentRepoListBinding
import com.example.gitrepoexplorer.ui.viewmodel.SharedViewModel
import com.example.gitrepoexplorer.util.Constants.ALLEGRO
import com.example.gitrepoexplorer.util.Constants.ALLEGRO_GITHUB_AVATAR_URL
import com.example.gitrepoexplorer.util.Constants.NO_INTERNET
import com.example.gitrepoexplorer.util.Constants.REPOS_PER_PAGE_LIMIT
import com.example.gitrepoexplorer.util.data.Resource
import com.example.gitrepoexplorer.util.glide.GlideApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RepoListFragment : Fragment() {

    private var _binding: FragmentRepoListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRepoListBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        val progressBar = binding.progressBar
        val repoRecyclerView = binding.repositoryList
        val repoListAdapter = RepoListAdapter(requireContext(), viewModel.getLoadedRepos())

        GlideApp.with(requireContext())
            .load(ALLEGRO_GITHUB_AVATAR_URL)
            .placeholder(CircularProgressDrawable(requireContext()).apply {
                this.strokeWidth = 16f
                this.centerRadius = 32f
                this.start()
            })
            .centerInside()
            .into(binding.githubAvatar)

        repoRecyclerView.apply {
            this.layoutManager = LinearLayoutManager(requireContext())
            this.adapter = repoListAdapter
            this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val lastVisiblePosition: Int = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                    if (lastVisiblePosition == recyclerView.adapter!!.itemCount - 1) {
                        if (viewModel.getLastLoadedReposCount() == REPOS_PER_PAGE_LIMIT) {
                            viewModel.loadRepos()
                            Toast.makeText(requireActivity(),"Loading more repositories...", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireActivity(),"No more repositories to load.", Toast.LENGTH_SHORT).show()
                        } // TODO: Tosty do naprawy
                    }
                }
            })
        }

        viewModel.repos.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    progressBar.visibility = View.GONE
                    response.data?.let { data ->
                        if (data.size > 0) {
                            repoListAdapter.updateData(data)
                            Toast.makeText(requireActivity(),"Repositories loaded successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireActivity(),"User has no repositories!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                is Resource.Error -> {
                    progressBar.visibility = View.GONE
                    response.message?.let { message ->
                        if (message == NO_INTERNET)
                            noInternetDialog() else Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun noInternetDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Oops!")
            .setIcon(R.drawable.ic_round_error_outline_24)
            .setMessage("It looks like You don't have internet connection. Would You like to turn internet on now?")
            .setPositiveButton("Mobile Data") { _, _ ->
                startActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS))
            }
            .setNegativeButton("Wi-Fi") { _, _ ->
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
            .setNeutralButton("No") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }
}