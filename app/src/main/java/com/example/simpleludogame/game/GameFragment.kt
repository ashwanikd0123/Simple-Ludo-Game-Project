package com.example.simpleludogame.game

import android.content.res.Configuration
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.simpleludogame.R
import com.example.simpleludogame.databinding.FragmentGameBinding
import com.example.simpleludogame.game.gamemodel.GameViewModel
import com.example.simpleludogame.game.gamemodel.ludomodel.player.PlayerColors
import com.example.simpleludogame.ludoboardui.LudoBoardForeGroundView

class GameFragment : Fragment() {

    private val viewModel: GameViewModel by activityViewModels()
    private lateinit var binding: FragmentGameBinding

    private lateinit var ludoBoard: LudoBoardForeGroundView

    override fun onCreate(savedInstanceState: Bundle?) {
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

        viewModel.currentPlayer.observe(viewLifecycleOwner) { playerIndex ->
            val player = viewModel.getAllPlayers().getOrNull(playerIndex) ?: return@observe
            updateDiceButtonPosition(player.colors)
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

    private fun updateDiceButtonPosition(color: PlayerColors) {
        val constraintLayout = binding.gameLayout
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        constraintSet.clear(R.id.dice_button, ConstraintSet.TOP)
        constraintSet.clear(R.id.dice_button, ConstraintSet.BOTTOM)
        constraintSet.clear(R.id.dice_button, ConstraintSet.START)
        constraintSet.clear(R.id.dice_button, ConstraintSet.END)

        if (isLandscape) {
            when (color) {
                PlayerColors.GREEN -> {
                    constraintSet.connect(R.id.dice_button, ConstraintSet.END, R.id.board_container, ConstraintSet.START, 16)
                    constraintSet.connect(R.id.dice_button, ConstraintSet.TOP, R.id.board_container, ConstraintSet.TOP)
                }
                PlayerColors.YELLOW -> {
                    constraintSet.connect(R.id.dice_button, ConstraintSet.START, R.id.board_container, ConstraintSet.END, 16)
                    constraintSet.connect(R.id.dice_button, ConstraintSet.TOP, R.id.board_container, ConstraintSet.TOP)
                }
                PlayerColors.RED -> {
                    constraintSet.connect(R.id.dice_button, ConstraintSet.END, R.id.board_container, ConstraintSet.START, 16)
                    constraintSet.connect(R.id.dice_button, ConstraintSet.BOTTOM, R.id.board_container, ConstraintSet.BOTTOM)
                }
                PlayerColors.BLUE -> {
                    constraintSet.connect(R.id.dice_button, ConstraintSet.START, R.id.board_container, ConstraintSet.END, 16)
                    constraintSet.connect(R.id.dice_button, ConstraintSet.BOTTOM, R.id.board_container, ConstraintSet.BOTTOM)
                }
            }
        } else {
            when (color) {
                PlayerColors.GREEN -> {
                    constraintSet.connect(R.id.dice_button, ConstraintSet.BOTTOM, R.id.board_container, ConstraintSet.TOP, 16)
                    constraintSet.connect(R.id.dice_button, ConstraintSet.START, R.id.board_container, ConstraintSet.START)
                }
                PlayerColors.YELLOW -> {
                    constraintSet.connect(R.id.dice_button, ConstraintSet.BOTTOM, R.id.board_container, ConstraintSet.TOP, 16)
                    constraintSet.connect(R.id.dice_button, ConstraintSet.END, R.id.board_container, ConstraintSet.END)
                }
                PlayerColors.RED -> {
                    constraintSet.connect(R.id.dice_button, ConstraintSet.TOP, R.id.board_container, ConstraintSet.BOTTOM, 16)
                    constraintSet.connect(R.id.dice_button, ConstraintSet.START, R.id.board_container, ConstraintSet.START)
                }
                PlayerColors.BLUE -> {
                    constraintSet.connect(R.id.dice_button, ConstraintSet.TOP, R.id.board_container, ConstraintSet.BOTTOM, 16)
                    constraintSet.connect(R.id.dice_button, ConstraintSet.END, R.id.board_container, ConstraintSet.END)
                }
            }
        }

        TransitionManager.beginDelayedTransition(constraintLayout)
        constraintSet.applyTo(constraintLayout)
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
