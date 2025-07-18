package basostudio.basospark.features.follow

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import basostudio.basospark.data.model.FollowerInfo
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import basostudio.basospark.R
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FollowScreen(
    navController: NavController,
    viewModel: FollowViewModel = hiltViewModel()
) {
    val tabs = listOf("Người theo dõi", "Đang theo dõi")
    val pagerState = rememberPagerState(initialPage = viewModel.initialTabIndex) { tabs.size }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.username) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title) }
                    )
                }
            }
            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> {
                        val followers = viewModel.followers.collectAsLazyPagingItems()
                        FollowList(users = followers)
                    }
                    1 -> {
                        val followings = viewModel.followings.collectAsLazyPagingItems()
                        FollowList(users = followings)
                    }
                }
            }
        }
    }
}

@Composable
fun FollowList(users: androidx.paging.compose.LazyPagingItems<FollowerInfo>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            count = users.itemCount,
            key = { index -> users.peek(index)?.id ?: "" }
        ) { index ->
            val user = users[index]
            if (user != null) {
                UserItem(user = user)
            }
        }

        // Xử lý trạng thái tải của Paging
        users.apply {
            when {
                loadState.refresh is LoadState.Loading -> {
                    item { Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                }
                loadState.append is LoadState.Loading -> {
                    item { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) { CircularProgressIndicator() } }
                }
                loadState.refresh is LoadState.Error -> {
                    val e = loadState.refresh as LoadState.Error
                    item { Text("Error: ${e.error.localizedMessage}") }
                }
            }
        }
    }
}

@Composable
fun UserItem(user: FollowerInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        user.avatar?.replace("localhost", "172.20.10.6")
        AsyncImage(
            model = user.avatar ?: R.drawable.defaultavatar,
            contentDescription = "Avatar",
            modifier = Modifier.size(50.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(user.username, fontWeight = FontWeight.Bold)
            Text("${user.firstName} ${user.lastName}", style = MaterialTheme.typography.bodyMedium)
        }
        Button(
            onClick = { /* TODO: Xử lý Follow/Unfollow */ },
            shape = RoundedCornerShape(50)
        ) {
            Text(if (user.hasFollowedBack) "Following" else "Follow")
        }
    }
}