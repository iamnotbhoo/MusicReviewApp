package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.utils.DataMigrator
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView

class DataMigrationFragment : Fragment() {

    private lateinit var migrateButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_data_migration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        migrateButton = view.findViewById(R.id.migrate_button)
        progressBar = view.findViewById(R.id.progress_bar)
        statusText = view.findViewById(R.id.status_text)

        migrateButton.setOnClickListener {
            startMigration()
        }
    }

    private fun startMigration() {
        migrateButton.isEnabled = false
        progressBar.visibility = View.VISIBLE
        statusText.text = "Migrating your data to cloud..."

        val dataMigrator = DataMigrator(requireContext())

        dataMigrator.migrateUserData { success, message ->
            progressBar.visibility = View.GONE
            migrateButton.isEnabled = true

            if (success) {
                statusText.text = "Migration successful! $message"
                migrateButton.text = "Migration Complete"
                migrateButton.isEnabled = false

                // Navigate to home after successful migration
                // findNavController().navigate(R.id.action_migrationFragment_to_homeFragment)
            } else {
                statusText.text = "Migration failed: $message"
                migrateButton.text = "Try Again"
            }
        }
    }
}