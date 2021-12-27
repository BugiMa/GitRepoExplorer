package com.example.gitrepoexplorer.repo

import com.example.gitrepoexplorer.data.remote.GitRepos
import retrofit2.Response

interface IGitRepoExplorerRepository {

    suspend fun getRepos(owner: String, page: Int, perPage: Int): Response<GitRepos>
}