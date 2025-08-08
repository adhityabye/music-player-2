package by.tutorials.musicapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import by.tutorials.musicapp.data.model.Song
import by.tutorials.musicapp.databinding.ActivityMainBinding
import by.tutorials.musicapp.ui.adapter.SongAdapter
import by.tutorials.musicapp.ui.viewmodel.SongViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val songViewModel: SongViewModel by viewModels()
    private lateinit var songAdapter: SongAdapter
    private var exoPlayer: ExoPlayer? = null
    private var currentSongList: List<Song> = emptyList()
    private var currentPlayingIndex: Int = -1

    companion object {
        private const val TAG = "MainActivity"
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            binding.buttonPlayPause.setImageResource(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateNowPlayingUI()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                playNextSong()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupViewModelObservers()
        setupButtonClickListeners()
        initializePlayer()
    }

    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer?.addListener(playerListener)
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(emptyList()) { song ->
            playSong(song)
        }
        binding.recyclerViewSongs.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = songAdapter
        }
    }

    private fun setupViewModelObservers() {
        songViewModel.songs.observe(this) { songs ->
            songAdapter.updateSongs(songs)
            currentSongList = songs
            if (songs.isNotEmpty()) {
                binding.recyclerViewSongs.visibility = View.VISIBLE
            }
        }

        songViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        songViewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage != null) {
                binding.textViewError.text = errorMessage
                binding.textViewError.visibility = View.VISIBLE
                binding.recyclerViewSongs.visibility = View.GONE
            } else {
                binding.textViewError.visibility = View.GONE
                if (currentSongList.isNotEmpty()) {
                    binding.recyclerViewSongs.visibility = View.VISIBLE
                } else {
                    binding.recyclerViewSongs.visibility = View.GONE
                }
            }
        }
    }

    private fun setupButtonClickListeners() {
        binding.buttonSearch.setOnClickListener {
            val searchTerm = binding.editTextSearch.text.toString().trim()
            songViewModel.searchSongs(searchTerm, isInitialLoad = false)
        }

        binding.buttonPlayPause.setOnClickListener {
            Log.d(TAG, "Play/Pause button clicked")
            togglePlayPause()
        }

        binding.buttonNext.setOnClickListener {
            Log.d(TAG, "Next button clicked")
            playNextSong()
        }

        binding.buttonPrevious.setOnClickListener {
            playPreviousSong()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    exoPlayer?.seekTo(progress.toLong())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val seekBarUpdateHandler = android.os.Handler(mainLooper)
        val seekBarUpdateRunnable = object : Runnable {
            override fun run() {
                exoPlayer?.let {
                    if (it.isPlaying && it.duration > 0) {
                        binding.seekBar.max = it.duration.toInt()
                        binding.seekBar.progress = it.currentPosition.toInt()
                    }
                }
                seekBarUpdateHandler.postDelayed(this, 1000)
            }
        }
        seekBarUpdateHandler.post(seekBarUpdateRunnable)
    }

    private fun playSong(song: Song) {
        currentPlayingIndex = currentSongList.indexOf(song)
        if (currentPlayingIndex == -1) {
            return
        }

        val mediaItem = MediaItem.fromUri(song.previewUrl)
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.setPlaybackSpeed(1.0f)
        exoPlayer?.play()

        binding.playbackControlsLayout.visibility = View.VISIBLE
        updateNowPlayingUI()
    }


    private fun updateNowPlayingUI() {
        if (currentPlayingIndex != -1 && currentPlayingIndex < currentSongList.size) {
            val currentSong = currentSongList[currentPlayingIndex]
            binding.textViewNowPlayingTitle.text = currentSong.title
            binding.textViewNowPlayingArtist.text = currentSong.artist
        } else {
            binding.textViewNowPlayingTitle.text = "Select a song"
            binding.textViewNowPlayingArtist.text = ""
        }
    }

    private fun togglePlayPause() {
        exoPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                if (it.playbackState == Player.STATE_IDLE || it.playbackState == Player.STATE_ENDED) {
                    if (currentPlayingIndex != -1 && currentPlayingIndex < currentSongList.size) {
                        val songToPlay = currentSongList[currentPlayingIndex]
                        val mediaItem = MediaItem.fromUri(songToPlay.previewUrl)
                        it.setMediaItem(mediaItem)
                        it.prepare()
                        it.setPlaybackSpeed(1.0f)
                        it.play()
                    } else {
                        Log.w(TAG, "no valid song")
                    }
                } else {
                     it.play()
                }
            }
        }
    }

    private fun playNextSong() {
        if (currentSongList.isEmpty()) {
            return
        }
        currentPlayingIndex++
        if (currentPlayingIndex >= currentSongList.size) {
            currentPlayingIndex = 0
        }
        if (currentPlayingIndex < currentSongList.size) {
            playSong(currentSongList[currentPlayingIndex])
        }
    }

    private fun playPreviousSong() {
        if (currentSongList.isEmpty()) {
            return
        }
        currentPlayingIndex--
        if (currentPlayingIndex < 0) {
            currentPlayingIndex = currentSongList.size - 1
        }
        if (currentPlayingIndex < currentSongList.size && currentPlayingIndex >= 0) {
            playSong(currentSongList[currentPlayingIndex])
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.removeListener(playerListener)
        exoPlayer?.release()
        exoPlayer = null

    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }
}