package com.khs.lovelynote.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.khs.lovelynote.R
import com.khs.lovelynote.databinding.FragmentBoardDetailBinding
import com.khs.lovelynote.extension.Constants.TAG_PARCELABLE_BOARD
import com.khs.lovelynote.model.LovelyNote
import com.khs.lovelynote.viewmodel.BoardDetailVM
import com.khs.lovelynote.viewmodel.factory.BoardDetailVMFactory
import timber.log.Timber

class BoardDetailFragment : BaseFragment<FragmentBoardDetailBinding>() {

    private var board: LovelyNote? = null
    private lateinit var boardDetailVM: BoardDetailVM

    private val observer: Observer<LovelyNote?> =
        Observer { board: LovelyNote? ->
            board?.let {
                // mBinding?.board = board
            }
        }

    companion object {
        @JvmStatic
        fun newInstance(board: LovelyNote?) =
            BoardDetailFragment().apply {
                arguments = Bundle().apply {
                    // putParcelable(TAG_PARCELABLE_BOARD, board)
                }
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
           //  board = it.getParcelable(TAG_PARCELABLE_BOARD)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindView(inflater, container!!, R.layout.fragment_board_detail)
        return mBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        boardDetailVM =
            ViewModelProvider(this, BoardDetailVMFactory(requireActivity().application, 100)).get(
                BoardDetailVM::class.java
            )
        board?.let {

            this.lifecycle.addObserver(boardDetailVM)
        }
    }

    override fun onDestroyView() {
        Timber.d("onDestroyView()")
        super.onDestroyView()
    }

    override fun onDetach() {
        Timber.d("onDetach()")
        super.onDetach()
    }

    override fun onBackPressed(): Boolean {
        return false
    }
}