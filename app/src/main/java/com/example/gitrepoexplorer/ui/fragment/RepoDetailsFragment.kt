package com.example.gitrepoexplorer.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.gitrepoexplorer.R
import com.example.gitrepoexplorer.databinding.FragmentRepoDetailsBinding
import com.example.gitrepoexplorer.ui.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.content.Intent
import android.net.Uri
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.example.gitrepoexplorer.util.glide.GlideApp
import com.google.android.material.chip.Chip


@AndroidEntryPoint
class RepoDetailsFragment : Fragment() {

    private var _binding: FragmentRepoDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: RepoDetailsFragmentArgs by navArgs()

    private lateinit var viewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRepoDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        val repo = viewModel.getRepoByIndex(args.repoIndex)

        // basic info card
        val typeIcon = binding.cardBasicInfo.iconRepoImageView
        val nameTextView = binding.cardBasicInfo.repoName
        val languageTextView = binding.cardBasicInfo.repoMainLanguage
        val creationDateTextView = binding.cardBasicInfo.repoCreationDate
        val lastUpdateTextView = binding.cardBasicInfo.repoLastUpdateDate
        val sizeTextView = binding.cardBasicInfo.repoSize

        nameTextView.text = repo.name
        creationDateTextView.text = getString(
            R.string.creation_date,
            repo.created_at.dropLast(10).replace("-", " ")
        )
        lastUpdateTextView.text = getString(
            R.string.last_update_date,
            repo.updated_at.dropLast(10).replace("-", " ")
        )
        sizeTextView.text = getString(
            R.string.size,
            bytesSizeConverter(repo.size.toDouble())
        )
        if (repo.fork) {
            typeIcon.setImageResource(R.drawable.ic_round_fork_32)
        }

        if (repo.language != null) {
            languageTextView.text = getString(
                R.string.main_language,
                repo.language
            )
        } else {
            languageTextView.visibility = View.GONE
        }

        // topics description card
        val descriptionCard = binding.cardTopicsDescription.card
        val topicsChipGroup = binding.cardTopicsDescription.topicsChipGroup
        val descriptionTextView = binding.cardTopicsDescription.repoDescription
        val descriptionLabelTextView = binding.cardTopicsDescription.descriptionLabel

        if (repo.description == null && repo.topics == null) {
            descriptionCard.visibility = View.GONE
        } else {
            if (repo.description != null) {
                descriptionTextView.text = repo.description
            } else {
                descriptionTextView.visibility = View.GONE
                descriptionLabelTextView.text = getString(R.string.topics)
            }

            if (repo.topics != null) {
                for (topic in repo.topics) {
                    val topicChip = Chip(requireContext()).apply {
                        this.text = topic
                        this.textSize = 14f
                        this.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    }
                    topicsChipGroup.addView(topicChip)
                }
            } else {
                topicsChipGroup.visibility = View.GONE
            }
        }



        // numbers card
        val forksCountTextView = binding.cardNumbers.forksCount
        val watchersCountTextView = binding.cardNumbers.watchersCount
        val stargazersCountTextView = binding.cardNumbers.stargazersCount
        val openIssuesCountTextView = binding.cardNumbers.openIssuesCount

        forksCountTextView.text = repo.forks_count.toString()
        watchersCountTextView.text = repo.watchers_count.toString()
        stargazersCountTextView.text = repo.stargazers_count.toString()
        openIssuesCountTextView.text = repo.open_issues_count.toString()

        // owner card
        val ownerAvatarImageView = binding.cardOwner.ownerAvatarImageView
        val ownerNameTextView = binding.cardOwner.ownerLogin
        val ownerTypeTextView = binding.cardOwner.ownerType

        val progressBar = CircularProgressDrawable(requireContext()).apply {
            this.strokeWidth = 16f
            this.centerRadius = 32f
            this.start()
        }
        GlideApp.with(requireContext())
            .load(repo.owner.avatar_url)
            .placeholder(progressBar)
            .centerInside()
            .into(ownerAvatarImageView)

        ownerNameTextView.text = repo.owner.login.replaceFirstChar(Char::titlecase)
        ownerTypeTextView.text = repo.owner.type.replaceFirstChar(Char::titlecase)

        // license card
        val licenseCard = binding.cardLicense.card
        val licenseTextView = binding.cardLicense.license

        if (repo.license?.name != null) {
            licenseTextView.text = repo.license.name
        } else {
            licenseCard.visibility = View.GONE
        }

        // GithubPageButton
        val githubPageButton = binding.githubPageButton

        githubPageButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(repo.html_url))
            startActivity(browserIntent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bytesSizeConverter(bytes: Double) = when {
        bytes >= 1 shl 30 -> "%.1f GB".format(bytes / (1 shl 30))
        bytes >= 1 shl 20 -> "%.1f MB".format(bytes / (1 shl 20))
        bytes >= 1 shl 10 -> "%.0f kB".format(bytes / (1 shl 10))
        else -> "$bytes B"
    }
}