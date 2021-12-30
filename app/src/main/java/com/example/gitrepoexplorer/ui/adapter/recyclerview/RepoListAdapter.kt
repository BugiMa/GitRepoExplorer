package com.example.gitrepoexplorer.ui.adapter.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.gitrepoexplorer.R
import com.example.gitrepoexplorer.data.remote.GitRepos
import com.example.gitrepoexplorer.databinding.ItemRepoBinding
import com.example.gitrepoexplorer.ui.fragment.RepoListFragmentDirections

class RepoListAdapter(
    private val context: Context,
    private var gitRepos: GitRepos
): RecyclerView.Adapter<RepoListAdapter.GitRepoViewHolder>() {

    inner class GitRepoViewHolder(private val itemBinding: ItemRepoBinding): RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(name: String?, isFork: Boolean?) {
            itemBinding.repoName.text = name
            if (isFork != null && isFork) itemBinding.repoIcon.setImageResource(R.drawable.ic_round_fork_32)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GitRepoViewHolder {
        val itemBinding =
            ItemRepoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GitRepoViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: GitRepoViewHolder, position: Int) {
        val repo = gitRepos[position]
        holder.bind(repo.name, repo.fork)
        holder.itemView.setOnClickListener {
            val directions = RepoListFragmentDirections
                .actionRepoListFragmentToRepoDetailsFragment(position)
            it.findNavController().navigate(directions)
        }
    }

    override fun getItemCount(): Int {
        return gitRepos.size
    }

    fun updateData(data: GitRepos) {
        this.gitRepos.addAll(data)
        this.notifyItemRangeInserted(itemCount, data.size)
    }
}