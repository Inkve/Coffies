import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.coffies.database.AppDatabase
import com.example.coffies.ui.addmood.MoodInputViewModel

class MoodInputViewModelFactory(
    private val db: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoodInputViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MoodInputViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}