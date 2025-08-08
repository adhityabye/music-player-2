package by.tutorials.musicapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.tutorials.musicapp.R
import by.tutorials.musicapp.data.model.Song
import com.bumptech.glide.Glide

class SongAdapter(
    private var songs: List<Song>,
    private val onItemClicked: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.bind(song)
        holder.itemView.setOnClickListener {
            onItemClicked(song)
        }
    }

    override fun getItemCount(): Int = songs.size

    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trackName: TextView = itemView.findViewById(R.id.textViewTrackName)
        private val artistName: TextView = itemView.findViewById(R.id.textViewArtistName)
        private val albumName: TextView = itemView.findViewById(R.id.textViewAlbumName)
        private val artwork: ImageView = itemView.findViewById(R.id.imageViewArtwork)

        fun bind(song: Song) {
            trackName.text = song.title
            artistName.text = song.artist
            albumName.text = song.album

            Glide.with(itemView.context)
                .load(song.artworkUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .into(artwork)
        }
    }
}
