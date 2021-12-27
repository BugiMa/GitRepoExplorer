package com.example.gitrepoexplorer.repo

import com.example.gitrepoexplorer.api.GitHubApi
import com.example.gitrepoexplorer.data.remote.GitRepos
import retrofit2.Response
import javax.inject.Inject

class GitRepoExplorerRepository @Inject constructor(
    private val api: GitHubApi
): IGitRepoExplorerRepository {

    override suspend fun getRepos(owner: String, page: Int, perPage: Int): Response<GitRepos> {
        return api.getRepos(owner, page, perPage)
    }
}