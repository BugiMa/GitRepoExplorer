package com.example.gitrepoexplorer.api

import com.example.gitrepoexplorer.data.remote.GitRepos
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubApi {

    @GET("/orgs/{owner}/repos")
    suspend fun getRepos(
        @Path("owner") owner: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<GitRepos>
}