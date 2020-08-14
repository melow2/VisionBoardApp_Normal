package com.khs.visionboard.view.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.khs.visionboard.R
import com.khs.visionboard.databinding.DialogAudioPlayBinding
import com.khs.visionboard.extension.fadeInAnimation
import com.khs.visionboard.extension.fadeOutAnimation
import com.khs.visionboard.extension.formateMilliSeccond
import com.khs.visionboard.model.mediastore.MediaStoreAudio
import kotlinx.android.synthetic.main.dialog_audio_play.*
import java.util.concurrent.TimeUnit

class AudioPlayDialogFragment : DialogFragment() {

    private var mAudio: MediaStoreAudio? = null
    private lateinit var mPlayer: MediaPlayer
    private lateinit var mBinding: DialogAudioPlayBinding
    private var oTime: Int = 0
    private var sTime = 0
    private var eTime = 0
    private var fTime = 5000
    private var bTime = 5000
    private val handler = Handler(Looper.getMainLooper())

    private val UpdateSongTime: Runnable = object : Runnable {
        override fun run() {
            sTime = mPlayer.currentPosition
            mBinding.tvStartTime.text = sTime.toLong().formateMilliSeccond()
            mBinding.audioSeekBar.progress = sTime
            handler.postDelayed(this, 100)
        }
    }

    companion object {
        private const val ITEM_AUDIO = "ITEM_AUDIO"
        private const val DIALOG_WIDTH = 1000
        private const val DIALOG_HEIGHT = 1000
        fun newInstance(item: MediaStoreAudio?): AudioPlayDialogFragment {
            return AudioPlayDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ITEM_AUDIO, item)
                }
            }
        }
    }


    interface EventListener {
        fun onPlayClick()
        fun onPauseClick()
        fun onBackwardClick()
        fun onFrontwardClick()
    }

    override fun onStart() {
        super.onStart()
        if (dialog == null || dialog?.window == null) return
        dialog?.window?.setLayout(DIALOG_WIDTH, DIALOG_HEIGHT)
    } // #3


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mAudio = it.getParcelable(ITEM_AUDIO)
        }
        mPlayer = MediaPlayer.create(activity, mAudio?.contentUri)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle);
    } // #1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_audio_play, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 모서리 둥글게.
        mBinding.audio = mAudio
        mBinding.tvStartTime.text = 0L.formateMilliSeccond()
        return mBinding.root
    } // #2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listener = MediaPlayerListener(mBinding)
        btn_back_ward.setOnClickListener { listener.onBackwardClick() }
        btn_front_ward.setOnClickListener { listener.onFrontwardClick() }
        btn_play.setOnClickListener { listener.onPlayClick() }
        btn_pause.setOnClickListener { listener.onPauseClick() }
        audio_seek_bar.setOnSeekBarChangeListener(listener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPlayer.pause()
    }

    inner class MediaPlayerListener(private val mBinding: DialogAudioPlayBinding) : EventListener,SeekBar.OnSeekBarChangeListener {
        override fun onPlayClick() {
            mBinding.run {
                mPlayer.start()
                eTime = mPlayer.duration;
                sTime = mPlayer.currentPosition;
                if (oTime == 0) {
                    audioSeekBar.max = eTime;
                    oTime = 1;
                }
                tvStartTime.text = sTime.toLong().formateMilliSeccond()
                audioSeekBar.progress = sTime;
                handler.postDelayed(UpdateSongTime, 100);
                btnPause.fadeInAnimation()
                btnPlay.fadeOutAnimation()
            }

        }

        override fun onPauseClick() {
            mBinding.run{
                mPlayer.pause();
                btnPause.fadeOutAnimation()
                btnPlay.fadeInAnimation()
            }
        }

        override fun onBackwardClick() {
            if((sTime - bTime) > 0)
            {
                sTime -= bTime;
                mPlayer.seekTo(sTime);
            }
        }

        override fun onFrontwardClick() {
            if((sTime + fTime) <= eTime)
            {
                sTime += fTime;
                mPlayer.seekTo(sTime);
            }
        }

        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            mPlayer.pause()
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            seekBar?.progress?.run {
                mPlayer.seekTo(this)
                mPlayer.start()
            }

        }
    }
}