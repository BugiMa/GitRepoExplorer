package com.example.gitrepoexplorer.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.gitrepoexplorer.GitRepoExplorerApplication
import com.example.gitrepoexplorer.data.remote.GitRepos
import com.example.gitrepoexplorer.repo.IGitRepoExplorerRepository
import com.example.gitrepoexplorer.util.Constants.ALLEGRO
import com.example.gitrepoexplorer.util.Constants.CONVERSION_ERROR
import com.example.gitrepoexplorer.util.Constants.NETWORK_FAILURE
import com.example.gitrepoexplorer.util.Constants.NO_INTERNET
import com.example.gitrepoexplorer.util.Constants.REPOS_PER_PAGE_LIMIT
import com.example.gitrepoexplorer.util.data.Resource
import com.example.gitrepoexplorer.util.data.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    app: Application,
    private val repository: IGitRepoExplorerRepository
): AndroidViewModel(app) {

    private var page = 0
    private var lastLoadedRepoCount = 0
    private val loadedRepos = GitRepos()
    private val _repos = SingleLiveEvent<Resource<GitRepos>>()
    val repos: LiveData<Resource<GitRepos>> get() = _repos

    init {
        loadRepos()
    }

    fun getLoadedRepos() : GitRepos {
        return loadedRepos
    }

    fun getLastLoadedReposCount(): Int {
        return lastLoadedRepoCount
    }

    fun loadRepos() = viewModelScope.launch {
        safeApiCall()
    }

    private suspend fun safeApiCall() {
        _repos.postValue(Resource.Loading())
        try {
            if (isNetworkAvailable()) {
                val response = repository.getRepos(ALLEGRO, page, REPOS_PER_PAGE_LIMIT)
                _repos.postValue(handleApiResponse(response))
            } else {
                _repos.postValue(Resource.Error(NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> _repos.postValue(Resource.Error(NETWORK_FAILURE))
                else -> _repos.postValue(Resource.Error(CONVERSION_ERROR))
            }
        }
    }

    private fun handleApiResponse(response: Response<GitRepos>): Resource<GitRepos> {
        if (response.isSuccessful) {
            response.body().let { resultResponse ->
                page++
                lastLoadedRepoCount = resultResponse!!.size
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun isNetworkAvailable(): Boolean {
        var result = false
        val connectivityManager =
            getApplication<GitRepoExplorerApplication>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)     -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }
                }
            }
        }

        return result
    }
}