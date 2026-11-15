package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.example.data.model.UiState
import com.example.ui.CheckoutFormState
import com.example.ui.StoreSort
import com.example.ui.components.CheckoutPanel
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ErrorStateCard
import com.example.ui.components.LiquidEntrance
import com.example.ui.components.LiquidFloatingHeroCard
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidIconCircleButton
import com.example.ui.components.LiquidShimmerBox
import com.example.ui.components.OstharPrimaryButton
import com.example.ui.components.OstharProductImage
import com.example.ui.components.OstharSecondaryButton
import com.example.ui.components.ProductCard
import com.example.ui.theme.BgDeep
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RedError
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.util.Formatters

@Composable
fun StoreScreen(
    productsState: UiState<List<Product>>,
    filteredProducts: List<Product>,
    searchQuery: String,
    selectedGame: String?,
    selectedCategory: String?,
    inStockOnly: Boolean,
    sortMode: StoreSort,
    onSearchChange: (String) -> Unit,
    onSelectGame: (String?) -> Unit,
    onSelectCategory: (String?) -> Unit,
    onToggleInStock: (Boolean) -> Unit,
    onSelectSort: (StoreSort) -> Unit,
    onClearFilters: () -> Unit,
    onRetry: () -> Unit,
    onOpenProduct: (String) -> Unit,
    onAddToCart: (Product) -> Unit
) {
    val allProducts = (productsState as? UiState.Success)?.data.orEmpty()
    val games = remember(allProducts) { allProducts.map { it.game }.distinct() }
    val categories = remember(allProducts) { allProducts.map { it.category }.distinct() }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val columns = when {
            maxWidth >= 900.dp -> 4
            maxWidth >= 600.dp -> 3
            else -> 2
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(columns) }) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text("THE STORE", style = MaterialTheme.typography.labelMedium, color = CyanPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text("Find your next advantage.", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Singapore server packages. Fair LKR prices. Instant verification.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        singleLine = true,
                        placeholder = { Text("Search games, diamonds, packages...", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = TextMuted) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Clear search", tint = TextMuted)
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("store_search_input")
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedGame == null,
                                onClick = { onSelectGame(null) },
                                label = { Text("All Games") },
                                colors = chipColors()
                            )
                        }
                        items(games) { game ->
                            FilterChip(
                                selected = selectedGame == game,
                                onClick = { onSelectGame(if (selectedGame == game) null else game) },
                                label = { Text(game) },
                                colors = chipColors()
                            )
                        }
                        item {
                            FilterChip(
                                selected = inStockOnly,
                                onClick = { onToggleInStock(!inStockOnly) },
                                label = { Text("In stock") },
                                colors = chipColors()
                            )
                        }
                        item {
                            Box {
                                FilterChip(
                                    selected = sortMode != StoreSort.RECOMMENDED,
                                    onClick = { sortMenuExpanded = true },
                                    label = { Text(sortMode.label) },
                                    colors = chipColors()
                                )
                                DropdownMenu(
                                    expanded = sortMenuExpanded,
                                    onDismissRequest = { sortMenuExpanded = false }
                                ) {
                                    StoreSort.entries.forEach { sort ->
                                        DropdownMenuItem(
                                            text = { Text(sort.label) },
                                            onClick = {
                                                onSelectSort(sort)
                                                sortMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (categories.size > 1) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = {
                                        onSelectCategory(if (selectedCategory == category) null else category)
                                    },
                                    label = { Text(category) },
                                    colors = chipColors()
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${filteredProducts.size} packages available",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        if (searchQuery.isNotEmpty() || selectedGame != null || selectedCategory != null || inStockOnly) {
                            Text(
                                text = "Clear filters",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanPrimary,
                                modifier = Modifier
                                    .clickable(onClick = onClearFilters)
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }

            when (productsState) {
                is UiState.Loading -> {
                    items(4) {
                        LiquidShimmerBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            cornerRadius = 20.dp
                        )
                    }
                }
                is UiState.Error -> {
                    item(span = { GridItemSpan(columns) }) {
                        ErrorStateCard(
                            title = "The store couldn't load",
                            message = productsState.message,
                            onRetry = onRetry
                        )
                    }
                }
                is UiState.Success -> {
                    if (filteredProducts.isEmpty()) {
                        item(span = { GridItemSpan(columns) }) {
                            EmptyStateCard(
                                title = "No matches this time.",
                                description = "Try another keyword or clear your filters.",
                                actionText = "Reset Filters",
                                onAction = onClearFilters
                            )
                        }
                    } else {
                        items(filteredProducts, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                onOpen = { onOpenProduct(product.id) },
                                onAddToCart = { onAddToCart(product) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }

            item(span = { GridItemSpan(columns) }) {
                Surface(
                    color = CyanPrimary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "All packages are for the Singapore server. Your Player UID is verified before checkout.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    containerColor = SurfaceCard,
    labelColor = TextMuted,
    selectedContainerColor = CyanPrimary.copy(alpha = 0.16f),
    selectedLabelColor = CyanPrimary
)

@Composable
fun ProductDetailScreen(
    productId: String,
    productsState: UiState<List<Product>>,
    isLoggedIn: Boolean,
    walletBalance: Double,
    checkoutForm: CheckoutFormState,
    onBack: () -> Unit,
    onAddToCart: (Product, Int) -> Unit,
    onOpenProduct: (String) -> Unit,
    onUidChange: (String) -> Unit,
    onVerifyNow: () -> Unit,
    onWhatsappChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onPay: (List<CartItem>) -> Unit,
    onLoginClick: () -> Unit,
    onTopUpClick: () -> Unit
) {
    val allProducts = (productsState as? UiState.Success)?.data.orEmpty()
    val product = allProducts.find { it.id == productId }
    var quantity by remember(productId) { mutableIntStateOf(1) }
    var showInstantCheckout by remember(productId) { mutableStateOf(false) }

    if (productsState is UiState.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CyanPrimary)
        }
        return
    }

    if (product == null) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            OstharSecondaryButton(text = "Back to Store", onClick = onBack, icon = Icons.AutoMirrored.Rounded.ArrowBack)
            Spacer(Modifier.height(16.dp))
            EmptyStateCard(
                title = "Package not found",
                description = "This package is no longer available."
            )
        }
        return
    }

    val related = remember(allProducts, productId) {
        allProducts.filter { it.id != productId }.take(4)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiquidIconCircleButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack,
                    modifier = Modifier.testTag("product_back_button")
                )
                Text(
                    text = "${product.game} · ${product.category}",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyanPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            LiquidEntrance {
                LiquidFloatingHeroCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    OstharProductImage(
                        imagePath = product.image,
                        contentDescription = product.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.25f)
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(product.name, style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
                if (product.shortDescription.isNotBlank()) {
                    Text(product.shortDescription, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = Formatters.money(product.effectivePrice),
                        style = MaterialTheme.typography.headlineLarge,
                        color = CyanPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (product.salePrice != null && product.salePrice < product.regularPrice) {
                        Text(
                            text = Formatters.money(product.regularPrice),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextMuted,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpecBox(Icons.Rounded.Diamond, "Package", "${product.diamondAmount} diamonds", Modifier.weight(1f))
                SpecBox(Icons.Rounded.Public, "Server", product.supportedServer, Modifier.weight(1f))
                SpecBox(Icons.Rounded.Schedule, "Delivery", product.processingTime, Modifier.weight(1f))
            }
        }

        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Quantity", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(BgDeep)
                                .padding(horizontal = 4.dp)
                        ) {
                            IconButton(
                                onClick = { if (quantity > 1) quantity-- },
                                enabled = quantity > 1
                            ) {
                                Icon(Icons.Rounded.Remove, contentDescription = "Decrease quantity", tint = TextPrimary)
                            }
                            Text(
                                text = quantity.toString(),
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { if (quantity < 99) quantity++ },
                                enabled = quantity < 99
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Increase quantity", tint = TextPrimary)
                            }
                        }
                    }

                    Text(
                        text = if (product.isInStock) "● In stock" else "● Out of stock",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (product.isInStock) EmeraldSuccess else RedError
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OstharSecondaryButton(
                    text = "Add to Cart",
                    onClick = { onAddToCart(product, quantity) },
                    enabled = product.isInStock,
                    icon = Icons.Rounded.ShoppingCart,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("product_add_to_cart_button")
                )
                OstharPrimaryButton(
                    text = "Buy Now",
                    onClick = { showInstantCheckout = !showInstantCheckout },
                    enabled = product.isInStock,
                    icon = Icons.Rounded.FlashOn,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("product_buy_now_button")
                )
            }
        }

        item {
            AnimatedVisibility(
                visible = showInstantCheckout,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                CheckoutPanel(
                    items = listOf(CartItem(product, quantity)),
                    isLoggedIn = isLoggedIn,
                    walletBalance = walletBalance,
                    form = checkoutForm,
                    onUidChange = onUidChange,
                    onVerifyNow = onVerifyNow,
                    onWhatsappChange = onWhatsappChange,
                    onNoteChange = onNoteChange,
                    onPay = { onPay(listOf(CartItem(product, quantity))) },
                    onLoginClick = onLoginClick,
                    onTopUpClick = onTopUpClick
                )
            }
        }

        if (product.fullDescription.isNotBlank()) {
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("The details", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        Text(product.fullDescription, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Shield, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Verified player delivery · Secure wallet payment",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        if (related.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("More ways to power up", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(related, key = { it.id }) { item ->
                            ProductCard(
                                product = item,
                                onOpen = { onOpenProduct(item.id) },
                                onAddToCart = { onAddToCart(item, 1) },
                                modifier = Modifier.width(180.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier,
        cornerRadius = 16.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
