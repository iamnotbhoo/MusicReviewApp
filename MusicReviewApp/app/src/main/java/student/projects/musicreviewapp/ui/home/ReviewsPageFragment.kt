package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.adapters.ReviewsAdapter
import student.projects.musicreviewapp.repositories.FirebaseRepository

class ReviewsPageFragment : Fragment() {

    private lateinit var reviewsRecycler: RecyclerView
    private lateinit var reviewAdapter: ReviewsAdapter
    private val repository = FirebaseRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reviews_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        loadReviews()
    }

    private fun setupRecyclerView(view: View) {
        reviewsRecycler = view.findViewById(R.id.reviews_recycler)
        reviewAdapter = ReviewsAdapter(
            onReviewClick = { review ->
                navigateToReviewDetail(review)
            },
            onLikeClick = { review, isLiked ->
                toggleReviewLike(review, isLiked)
            }
        )

        reviewsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reviewAdapter
        }
    }

    private fun loadReviews() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val reviews = repository.getAllReviews()
                reviewAdapter.submitList(reviews)
            } catch (e: Exception) {
                // Handle error
                showToast("Failed to load reviews")
            }
        }
    }

    private fun navigateToReviewDetail(review: student.projects.musicreviewapp.models.Review) {
        val bundle = Bundle().apply {
            putParcelable("review", review)
        }
        // Assuming you have this action in your nav graph
        // findNavController().navigate(R.id.action_reviewsPageFragment_to_reviewDetailFragment, bundle)
    }

    private fun toggleReviewLike(review: student.projects.musicreviewapp.models.Review, isLiked: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userId = getCurrentUserId() // You need to implement this
                if (isLiked) {
                    repository.likeReview(userId, review.id)
                } else {
                    repository.unlikeReview(userId, review.id)
                }
            } catch (e: Exception) {
                showToast("Failed to update like")
            }
        }
    }

    private fun getCurrentUserId(): String {
        // Implement this to get current user ID from your auth system
        return "user_id_placeholder"
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
}