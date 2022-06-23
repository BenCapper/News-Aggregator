package org.ben.news.ui.storyList

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.core.Context
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.ben.news.models.StoryModel
import org.ben.news.repository.Repo

class StoryListViewModel : ViewModel() {
    var stories: MutableLiveData<MutableList<StoryModel>> = MutableLiveData()

    fun init (context: Context) {
        if (stories.value != null)
            return
    }

    private val repo = Repo()
    fun fetchData(): MutableLiveData<MutableList<StoryModel>> {
        viewModelScope.launch(IO) {
            stories.postValue(repo.getStories())
        }
        return stories
    }
}