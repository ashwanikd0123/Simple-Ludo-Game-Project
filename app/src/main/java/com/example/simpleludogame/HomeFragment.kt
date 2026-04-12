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
import kotlin.getValue

class HomeFragment : Fragment() {

    private val viewModel: GameViewModel by activityViewModels()
    private lateinit var binding: FragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater)

        binding.playButton.setOnClickListener {
            viewModel.initGame(1)
            findNavController().navigate(R.id.action_homeFragment_to_gameFragment)
        }

        return binding.root
    }
}