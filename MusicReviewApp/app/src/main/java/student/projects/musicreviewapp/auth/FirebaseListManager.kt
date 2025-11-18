package student.projects.musicreviewapp.auth

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import student.projects.musicreviewapp.models.UserList
import student.projects.musicreviewapp.repositories.FirebaseRepository

class FirebaseListManager(private val context: Context) {

    private val repository = FirebaseRepository()
    private val authManager = AuthManager()
    private val localManager = ListManager(context)

    fun createList(userList: UserList, onComplete: (Boolean) -> Unit = {}) {
        // Save locally first for immediate UI update
        localManager.createList(userList)

        // Then sync with Firebase
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.saveList(userList)
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true)
                }
            } catch (e: Exception) {
                // Firebase failed, but local save worked
                e.printStackTrace()
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true)
                }
            }
        }
    }

    fun getUserLists(userId: String, onResult: (List<UserList>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val firebaseLists = repository.getUserLists(userId)
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(firebaseLists)
                }
            } catch (e: Exception) {
                // Fallback to local storage
                val localLists = localManager.getLists()
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(localLists)
                }
            }
        }
    }

    fun getLists(onResult: (List<UserList>) -> Unit) {
        val userId = authManager.getCurrentUid()

        if (userId != null) {
            getUserLists(userId, onResult)
        } else {
            // No user logged in, use local storage
            val localLists = localManager.getLists()
            onResult(localLists)
        }
    }

    fun getPublicLists(onResult: (List<UserList>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val lists = repository.getAllPublicLists()
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(lists)
                }
            } catch (e: Exception) {
                // Fallback to local storage
                val localLists = localManager.getLists().filter { it.isPublic }
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(localLists)
                }
            }
        }
    }

    fun getPopularLists(onResult: (List<UserList>) -> Unit) {
        getLists { lists ->
            onResult(lists.sortedByDescending { it.likes })
        }
    }

    fun deleteList(listId: String, userId: String, onComplete: (Boolean) -> Unit = {}) {
        // Delete locally first
        localManager.deleteList(listId)

        // Then sync with Firebase
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.deleteList(listId, userId)
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true)
                }
            } catch (e: Exception) {
                // Firebase failed, but local deletion worked
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true)
                }
            }
        }
    }

    // Additional utility methods matching your original ListManager
    fun getListById(listId: String, onResult: (UserList?) -> Unit) {
        getLists { lists ->
            onResult(lists.find { it.id == listId })
        }
    }

    fun addAlbumToList(listId: String, album: student.projects.musicreviewapp.models.Music, onComplete: (Boolean) -> Unit = {}) {
        // Update locally first
        localManager.addAlbumToList(listId, album)

        // Then sync with Firebase
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get current list
                val list = repository.getUserLists(authManager.getCurrentUid() ?: "").find { it.id == listId }
                list?.let { userList ->
                    val updatedAlbums = userList.albums.toMutableList().apply {
                        if (!any { it.id == album.id }) {
                            add(album)
                        }
                    }
                    val updatedList = userList.copy(albums = updatedAlbums)
                    repository.saveList(updatedList)
                }

                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true)
                }
            } catch (e: Exception) {
                // Firebase failed, but local update worked
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true)
                }
            }
        }
    }

    fun likeList(listId: String, onComplete: (Boolean) -> Unit = {}) {
        // Update locally first
        localManager.likeList(listId)

        val userId = authManager.getCurrentUid() ?: return

        // Then sync with Firebase
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.likeList(userId, listId)
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true)
                }
            } catch (e: Exception) {
                // Firebase failed, but local update worked
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true)
                }
            }
        }
    }

    fun unlikeList(listId: String, onComplete: (Boolean) -> Unit = {}) {
        // Update locally first
        localManager.unlikeList(listId)

        val userId = authManager.getCurrentUid() ?: return

        // Then sync with Firebase
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.unlikeList(userId, listId)
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true)
                }
            } catch (e: Exception) {
                // Firebase failed, but local update worked
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true)
                }
            }
        }
    }

    fun getListsByCreator(creator: String, onResult: (List<UserList>) -> Unit) {
        getLists { lists ->
            onResult(lists.filter { it.creator == creator })
        }
    }

    fun generateListId(): String {
        return localManager.generateListId()
    }

    fun getCurrentTimestamp(): String {
        return localManager.getCurrentTimestamp()
    }

    // Sync local lists to Firebase (for migration)
    fun syncLocalListsToFirebase(onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        val userId = authManager.getCurrentUid() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val localLists = localManager.getLists()
                localLists.forEach { list ->
                    // Update creator to current user if needed
                    val updatedList = if (list.creator != userId) {
                        list.copy(creator = userId)
                    } else {
                        list
                    }
                    repository.saveList(updatedList)
                }

                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true, "Lists synced successfully")
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(false, "Failed to sync lists: ${e.message}")
                }
            }
        }
    }
}