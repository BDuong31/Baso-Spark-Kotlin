package basostudio.basospark.features.feed.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import basostudio.basospark.data.model.Post
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import basostudio.basospark.R

@Composable
fun PostItem(
    post: Post,
    modifier: Modifier = Modifier,
    onPostClick: () -> Unit = {},
    onLikeClick: () -> Unit, // <-- Thêm callback
    onSaveClick: () -> Unit,
    onAuthorClick: (String) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onPostClick() },
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onAuthorClick(post.author.id) }, // Cho phép click vào header
            verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.author.avatar,
                    contentDescription = "Author Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onAuthorClick(post.author.id) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.author.username,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onAuthorClick(post.author.id) }
                    )
                    Text(text = "Topic: ${post.topic.name}", style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content
            Text(
                text = post.content,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyLarge
            )

            // Image
            if (!post.image.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                val imageUrl = post.image.replace("localhost", "192.168.1.152")
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_launcher_background),
                    error = painterResource(id = R.drawable.ic_launcher_foreground)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(onClick = onLikeClick) {
                    Icon(
                        imageVector = if (post.hasLiked == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.hasLiked == true) Color.Red else LocalContentColor.current
                    )
                }

                IconButton(onClick = { /* TODO: Handle Comment */ }) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comment")
                }
                IconButton(onClick = { /* TODO: Handle Share */ }) {
                    Icon(Icons.Outlined.Send, contentDescription = "Share")
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onSaveClick) {
                    Icon(
                        imageVector = if (post.hasSaved == true) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save"
                    )
                }
            }

            // Likes and Comments count
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${post.likedCount} likes",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "View all ${post.commentCount} comments",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}