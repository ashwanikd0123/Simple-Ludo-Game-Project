package com.example.simpleludogame.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.simpleludogame.R
import com.example.simpleludogame.databinding.FragmentGameBinding
import com.example.simpleludogame.game.gamemodel.GameViewModel
import com.example.simpleludogame.ludoboardui.LudoBoardForeGroundView

class GameFragment : Fragment() {

    private val viewModel: GameViewModel by activityViewModels()
    private lateinit var binding: FragmentGameBinding

    private lateinit var ludoBoard: LudoBoardForeGroundView

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel.initGame(1)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameBinding.inflate(inflater, container, false)
        ludoBoard = binding.ludoBoardForeground

        setListeners()
        setObservers()
        return binding.root
    }

    fun setListeners() {
        binding.diceButton.setOnClickListener {
            viewModel.rollDice()
        }
    }

    fun setObservers() {
        viewModel.diceVal.observe(viewLifecycleOwner) {
            binding.diceButton.foreground = ContextCompat.getDrawable(requireContext(), getDrawableResource(it))
        }

        val players = viewModel.getAllPlayers()
        for (player in players) {
            for (pawn in player.pawns) {
                ludoBoard.updatePawn(pawn)
                pawn.cell.observe(viewLifecycleOwner) {
                    ludoBoard.updatePawn(pawn)
                }
            }
        }
    }

    fun getDrawableResource(diceVal: Int): Int {
        return when (diceVal) {
            1 -> R.drawable.dice_1
            2 -> R.drawable.dice_2
            3 -> R.drawable.dice_3
            4 -> R.drawable.dice_4
            5 -> R.drawable.dice_5
            6 -> R.drawable.dice_6
            else -> R.drawable.dice_1
        }
    }
}
