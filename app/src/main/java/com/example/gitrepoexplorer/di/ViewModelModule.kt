package com.example.gitrepoexplorer.di

import com.example.gitrepoexplorer.api.GitHubApi
import com.example.gitrepoexplorer.repo.GitRepoExplorerRepository
import com.example.gitrepoexplorer.repo.IGitRepoExplorerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @ViewModelScoped
    @Provides
    fun provideRepository(
        api: GitHubApi
    ) = GitRepoExplorerRepository(api) as IGitRepoExplorerRepository
}