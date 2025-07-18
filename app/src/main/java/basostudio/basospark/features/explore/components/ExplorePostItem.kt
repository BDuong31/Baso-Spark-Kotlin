package basostudio.basospark.features.explore.components

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import basostudio.basospark.data.model.Post
import coil.compose.AsyncImage
import basostudio.basospark.R

@Composable
fun ExplorePostItem(post: Post, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(bottom = 16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            if (!post.image.isNullOrBlank()) {
                val imageUrl = post.image.replace("localhost", "172.20.10.6")
                Log.d("ExplorePostItem", "Image URL: $imageUrl")
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, top = 8.dp, end = 8.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.FillWidth,
                    placeholder = painterResource(R.drawable.ic_launcher_background), // Thay bằng placeholder của bạn
                    error = painterResource(id = R.drawable.ic_launcher_foreground) // Thay bằng ảnh lỗi của bạn
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val avatarUrl = post.author.avatar?.replace("localhost", "172.20.10.6") ?: post.author.avatar
                    Log.d("ExplorePostItem", "Avatar URL: $avatarUrl")
                    AsyncImage(
                        model = avatarUrl ?: R.drawable.defaultavatar,
                        contentDescription = "Author Avatar",
                        modifier = Modifier.size(24.dp).clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(post.author.username, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("·", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("9 ngày trước", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.weight(1f))
                    val topicColor = try {
                        Color(post.topic.color.toColorInt())
                    } catch (e: Exception) { Color.Gray }
                    Canvas(modifier = Modifier.size(8.dp)) { drawCircle(color = topicColor) }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(post.topic.name, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}