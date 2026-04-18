package com.example.simpleludogame

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.simpleludogame.databinding.FragmentHomeBinding
import com.example.simpleludogame.game.gamemodel.GameViewModel

class HomeFragment : Fragment() {

    private val viewModel: GameViewModel by activityViewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        if (!viewModel.hasPrevGameEnded()) {
            binding.resumeButton.visibility = View.VISIBLE
        }

        binding.resumeButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_gameFragment)
        }

        binding.playButton.setOnClickListener {
            // Spinner positions: 0 -> 2 players, 1 -> 3 players, 2 -> 4 players
            val playerCount = binding.playerCountSpinner.selectedItemPosition + 2
            viewModel.initGame(playerCount)
            findNavController().navigate(R.id.action_homeFragment_to_gameFragment)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}