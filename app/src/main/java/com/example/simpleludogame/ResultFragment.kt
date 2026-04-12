package com.example.simpleludogame

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.simpleludogame.databinding.FragmentResultBinding
import com.example.simpleludogame.databinding.ItemResultPlayerBinding
import com.example.simpleludogame.game.gamemodel.GameViewModel
import com.example.simpleludogame.game.gamemodel.ludomodel.player.Player
import com.example.simpleludogame.game.gamemodel.ludomodel.player.PlayerColors
import com.example.simpleludogame.game.gamemodel.ludomodel.player.PlayerStatus

class ResultFragment : Fragment() {
    private val viewModel: GameViewModel by activityViewModels()
    private lateinit var binding: FragmentResultBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        displayResults()

        binding.homeButton.setOnClickListener {
            findNavController().navigate(R.id.action_resultFragment_to_homeFragment)
        }
    }

    private fun displayResults() {
        val players = viewModel.getAllPlayers()
        
        // Sort players by rank
        val sortedPlayers = players.sortedBy { player ->
            when (player.getStatus()) {
                PlayerStatus.RANK_1 -> 1
                PlayerStatus.RANK_2 -> 2
                PlayerStatus.RANK_3 -> 3
                PlayerStatus.WON -> 4
                PlayerStatus.PLAYING -> 5
                PlayerStatus.LOSE -> 6
            }
        }

        binding.resultsContainer.removeAllViews()

        sortedPlayers.forEachIndexed { index, player ->
            val itemBinding = ItemResultPlayerBinding.inflate(layoutInflater, binding.resultsContainer, false)
            
            val rank = index + 1
            itemBinding.rankText.text = when (rank) {
                1 -> getString(R.string.rank_1)
                2 -> getString(R.string.rank_2)
                3 -> getString(R.string.rank_3)
                else -> getString(R.string.rank_4)
            }

            itemBinding.playerIcon.setImageResource(getPlayerIcon(player.colors))
            itemBinding.playerName.text = getString(R.string.player_format, player.colors.name)
            
            if (rank > 3) {
                itemBinding.rankImage.visibility = View.INVISIBLE
            }

            binding.resultsContainer.addView(itemBinding.root)
        }
    }

    private fun getPlayerIcon(color: PlayerColors): Int {
        return when (color) {
            PlayerColors.GREEN -> R.drawable.ic_pawn_green
            PlayerColors.YELLOW -> R.drawable.ic_pawn_yellow
            PlayerColors.RED -> R.drawable.ic_pawn_red
            PlayerColors.BLUE -> R.drawable.ic_pawn_blue
        }
    }
}