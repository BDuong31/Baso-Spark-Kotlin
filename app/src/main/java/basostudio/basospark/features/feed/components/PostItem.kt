// File: app/src/main/java/basostudio/basospark/features/feed/components/PostItem.kt
package basostudio.basospark.features.feed.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import basostudio.basospark.R
import basostudio.basospark.data.model.Post
import coil.compose.AsyncImage

@Composable
fun PostItem(
    post: Post,
    modifier: Modifier = Modifier,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onSaveClick: () -> Unit,
    onAuthorClick: (String) -> Unit,
    onMoreOptionsClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onPostClick)
            .padding(vertical = 16.dp, horizontal = 16.dp)
    ) {
        // Phần Header: Avatar, Tên, Thời gian, Nút tùy chọn
        PostHeader(
            username = post.author.username,
            avatarUrl = post.author.avatar,
            timestamp = "7 days ago", // TODO: Cần một hàm để chuyển đổi `createdAt` thành thời gian tương đối
            onAuthorClick = { onAuthorClick(post.author.id) },
            onMoreOptionsClick = onMoreOptionsClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Phần nội dung (chữ và ảnh) được lùi vào để thẳng hàng với header
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(58.dp)) // Khoảng trống = kích thước avatar (42dp) + khoảng cách (16dp)
            Column(modifier = Modifier.weight(1f)) {
                // Nội dung bài viết
                if (post.content.isNotBlank()) {
                    Text(
                        text = post.content,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // Ảnh bài viết
                if (!post.image.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val imageUrl = post.image.replace("localhost", "172.20.10.6")

                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Post Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.FillWidth,
                        placeholder = painterResource(R.drawable.ic_launcher_background), // Thay bằng placeholder của bạn
                        error = painterResource(id = R.drawable.ic_launcher_foreground) // Thay bằng ảnh lỗi của bạn
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Phần các nút tương tác (Like, Comment, Save)
        PostActions(
            likeCount = post.likedCount,
            commentCount = post.commentCount,
            isLiked = post.hasLiked == true,
            isSaved = post.hasSaved == true,
            onLikeClick = onLikeClick,
            onCommentClick = onCommentClick,
            onSaveClick = onSaveClick,
            modifier = Modifier.padding(start = 58.dp) // Căn lề với nội dung
        )
    }
}

@Composable
private fun PostHeader(
    username: String,
    avatarUrl: String?,
    timestamp: String,
    onAuthorClick: () -> Unit,
    onMoreOptionsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        avatarUrl?.replace("localhost", "172.20.10.6") ?: avatarUrl
        AsyncImage(
            model = avatarUrl ?: R.drawable.defaultavatar,
            contentDescription = "$username's avatar",
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .clickable(onClick = onAuthorClick)
        )
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                    append(username)
                }
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                    append("  ·  $timestamp")
                }
            },
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onMoreOptionsClick) {
            Icon(Icons.Default.MoreHoriz, contentDescription = "More options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PostActions(
    likeCount: Int,
    commentCount: Int,
    isLiked: Boolean,
    isSaved: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        // Nhóm Like và Comment
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            ActionIconWithText(
                icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                text = likeCount.toString(),
                onClick = onLikeClick,
                tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
            )
            ActionIconWithText(
                icon = Icons.Outlined.ChatBubbleOutline,
                text = commentCount.toString(),
                onClick = onCommentClick,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Nút Save
        IconButton(
            modifier = Modifier.padding(start = 10.dp, end = 0.dp),
            onClick = onSaveClick
        ) {
            Icon(
                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = "Save post",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionIconWithText(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    tint: Color
) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = tint
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}