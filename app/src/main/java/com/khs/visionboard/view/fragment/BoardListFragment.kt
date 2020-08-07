package com.khs.visionboard.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.khs.visionboard.R
import com.khs.visionboard.databinding.FragmentListBinding
import com.khs.visionboard.model.Board
import com.khs.visionboard.model.Constants.TAG_PARCELABLE_BOARD
import com.khs.visionboard.view.activity.BoardDetailActivity
import com.khs.visionboard.view.adapter.BoardListAdapter
import com.khs.visionboard.viewmodel.BoardListVM
import com.khs.visionboard.viewmodel.factory.FactoryBoardListVM
import timber.log.Timber

class BoardListFragment : BaseFragment<FragmentListBinding>() {

    private var param1: String? = null
    private var param2: String? = null

    private var recyclerView: RecyclerView? = null
    private var listAdapter: BoardListAdapter? = null
    private lateinit var boardListVM: BoardListVM

    private val observer: Observer<List<Board>?> =
        Observer { boards: List<Board>? ->
            boards?.let{
                listAdapter?.submitList(boards)
            }
        }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BoardListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindView(inflater, container!!, R.layout.fragment_list)
        recyclerView = mBinding?.rcvBoardList
        return mBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = view.context
        listAdapter = BoardListAdapter(context)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        // recyclerView?.layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        recyclerView?.adapter = listAdapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        boardListVM = ViewModelProvider(this, FactoryBoardListVM(requireActivity().application, 100)).get(BoardListVM::class.java)
        boardListVM.getBoardList().observe(viewLifecycleOwner,observer)
        this.lifecycle.addObserver(boardListVM)
        mBinding?.btnAdd?.setOnClickListener {
            boardListVM.setBoard()
        }
        setAdapterListener()
    }

    override fun onDestroyView() {
        Timber.d("onDestroyView()")
        super.onDestroyView()
    }

    override fun onDetach() {
        Timber.d("onDetach()")
        super.onDetach()
    }

    private fun setAdapterListener() {
        listAdapter?.addEventListener(object : BoardListAdapter.BoardListEvent {
            override fun onClick(position: Int) {
                val board = boardListVM.getBoardItem(position)
                val intent = Intent(context,BoardDetailActivity::class.java).apply {
                    putExtra(TAG_PARCELABLE_BOARD,board)
                }
                startActivity(intent)
            }

            override fun onDelete(position: Int) {
                boardListVM.deleteBoardItem(position)
            }
        })
    }

    override fun onBackPressed(): Boolean {
        return false
    }

}