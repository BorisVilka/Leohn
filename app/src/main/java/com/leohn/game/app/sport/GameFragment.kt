package com.leohn.game.app.sport

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.leohn.game.app.sport.databinding.FragmentGameBinding
import kotlinx.coroutines.launch
import kotlin.streams.toList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GameFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private lateinit var binding: FragmentGameBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentGameBinding.inflate(inflater,container,false)
        val navController = Navigation.findNavController(requireActivity(),R.id.fragmentContainerView)
        binding.imageView2.setOnClickListener {
            navController.popBackStack()
        }
        binding.imageView3.setOnClickListener {
            binding.textView4.visibility = if(!binding.game.paused) View.VISIBLE else View.INVISIBLE
            binding.game.togglePause()
        }
        binding.textView5.setOnClickListener {
            navController.navigate(R.id.action_gameFragment_self)
        }
        val prefs = requireActivity().getSharedPreferences("prefs",Context.MODE_PRIVATE)
        binding.game.setEndListener(object : GameView.Companion.EndListener {
            override fun end() {
                lifecycleScope.launch {
                    val set = prefs.getStringSet("score",HashSet<String>())!!
                    val set1 = HashSet<String>()
                    set1.addAll(set)
                    if(!set1.contains(binding.game.score.toString())) set1.add(binding.game.score.toString())
                    prefs.edit().putStringSet("score",set1).apply()
                    binding.textView4.text = "SCORE"
                    binding.textView4.visibility = View.VISIBLE
                    binding.imageView3.setOnClickListener(null)
                    binding.textView6.text = binding.game.score.toString()
                    binding.textView6.visibility = View.VISIBLE
                    binding.textView5.visibility = View.VISIBLE
                    binding.imageView4.visibility = View.VISIBLE
                }
            }

            override fun score(score: Int) {

            }

        })
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GameFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GameFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}