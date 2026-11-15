package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.AuthMode
import com.example.ui.OstharViewModel
import com.example.ui.components.LiquidFloatingNavBar
import com.example.ui.components.LiquidNavItem
import com.example.ui.components.OstharBrandLogo
import com.example.ui.components.OstharGridBackground
import com.example.ui.components.liquidPress
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LegalScreen
import com.example.ui.screens.OrderDetailScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReceiptScreen
import com.example.ui.screens.StoreScreen
import com.example.ui.screens.WalletHistoryScreen
import com.example.ui.screens.WalletOverviewScreen
import com.example.ui.screens.WalletTopUpScreen
import com.example.ui.theme.BgDeep
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GlassSpecularRim
import com.example.ui.theme.GlassSpecularSoft
import com.example.ui.theme.OstharTheme
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.util.Formatters
import kotlinx.coroutines.flow.MutableStateFlow

private object Routes {
    const val HOME = "home"
    const val STORE = "store"
    const val PRODUCT = "product/{productId}"
    const val CART = "cart"
    const val WALLET = "wallet"
    const val WALLET_TOPUP = "wallet_topup"
    const val WALLET_HISTORY = "wallet_history"
    const val RECEIPT = "receipt/{txId}"
    const val ORDERS = "orders"
    const val ORDER_DETAIL = "order/{orderId}"
    const val ACCOUNT = "account"
    const val AUTH = "auth"
    const val LEGAL = "legal/{type}"
    const val ADMIN = "admin"
}

class MainActivity : ComponentActivity() {
    private val pendingOAuthUri = MutableStateFlow<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        captureOAuthUri(intent)
        setContent {
            OstharTheme {
                OstharApp(pendingOAuthUri = pendingOAuthUri)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        captureOAuthUri(intent)
    }

    private fun captureOAuthUri(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "osthartopup" && data.host == "auth") {
            pendingOAuthUri.value = data
            intent.data = null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OstharApp(
    pendingOAuthUri: MutableStateFlow<Uri?> = remember { MutableStateFlow(null) },
    viewModel: OstharViewModel = viewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val session by viewModel.session.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val productsState by viewModel.productsState.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val siteSettings by viewModel.siteSettings.collectAsStateWithLifecycle()
    val paymentMethods by viewModel.paymentMethods.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedGame by viewModel.selectedGame.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val inStockOnly by viewModel.inStockOnly.collectAsStateWithLifecycle()
    val sortMode by viewModel.sortMode.collectAsStateWithLifecycle()

    val walletBalance by viewModel.walletBalance.collectAsStateWithLifecycle()
    val balanceLoading by viewModel.balanceLoading.collectAsStateWithLifecycle()
    val transactionsState by viewModel.transactionsState.collectAsStateWithLifecycle()
    val topupRequests by viewModel.topupRequests.collectAsStateWithLifecycle()
    val receiptState by viewModel.receiptState.collectAsStateWithLifecycle()

    val ordersState by viewModel.ordersState.collectAsStateWithLifecycle()
    val orderStatusFilter by viewModel.orderStatusFilter.collectAsStateWithLifecycle()
    val orderSearchQuery by viewModel.orderSearchQuery.collectAsStateWithLifecycle()
    val orderDetailState by viewModel.orderDetailState.collectAsStateWithLifecycle()
    val cancellingOrder by viewModel.cancellingOrder.collectAsStateWithLifecycle()

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val checkoutForm by viewModel.checkoutState.collectAsStateWithLifecycle()
    val topUpForm by viewModel.topUpState.collectAsStateWithLifecycle()
    val authUiState by viewModel.authState.collectAsStateWithLifecycle()
    val adminToken by viewModel.adminToken.collectAsStateWithLifecycle()
    val adminState by viewModel.adminState.collectAsStateWithLifecycle()
    val oauthUri by pendingOAuthUri.collectAsStateWithLifecycle()

    LaunchedEffect(oauthUri) {
        val uri = oauthUri ?: return@LaunchedEffect
        pendingOAuthUri.value = null
        viewModel.handleOAuthCallback(uri) {
            if (navController.currentDestination?.route == Routes.AUTH) {
                navController.popBackStack()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.toastEvents.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val startGoogleSignIn: () -> Unit = {
        viewModel.startGoogleSignIn { url ->
            runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
    }

    val openWhatsAppSupport: () -> Unit = {
        val raw = siteSettings?.supportWhatsapp.orEmpty().filter { it.isDigit() }
        if (raw.isNotEmpty()) {
            runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$raw")))
            }
        }
    }

    val openEmailSupport: () -> Unit = {
        val email = siteSettings?.supportEmail.orEmpty()
        if (email.isNotEmpty()) {
            runCatching {
                context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")))
            }
        }
    }

    val cartItemCount = cart.sumOf { it.quantity }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val tabs = listOf(
        LiquidNavItem(Routes.HOME, "Home", Icons.Rounded.Home, Icons.Outlined.Home, "nav_home"),
        LiquidNavItem(Routes.STORE, "Store", Icons.Rounded.Storefront, Icons.Outlined.Storefront, "nav_store"),
        LiquidNavItem(Routes.WALLET, "Wallet", Icons.Rounded.Wallet, Icons.Outlined.Wallet, "nav_wallet"),
        LiquidNavItem(Routes.ORDERS, "Orders", Icons.Rounded.ReceiptLong, Icons.Outlined.ReceiptLong, "nav_orders"),
        LiquidNavItem(Routes.ACCOUNT, "Account", Icons.Rounded.Person, Icons.Outlined.Person, "nav_account")
    )

    val navigateTab = { route: String ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    OstharGridBackground {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 600.dp

            Row(modifier = Modifier.fillMaxSize()) {
                if (useRail) {
                    NavigationRail(
                        containerColor = SurfaceCard,
                        contentColor = TextPrimary,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(Modifier.size(16.dp))
                        tabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = CyanPrimary,
                                    selectedTextColor = CyanPrimary,
                                    indicatorColor = CyanPrimary.copy(alpha = 0.14f),
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        Column {
                            TopAppBar(
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color(0xD90A101E),
                                    titleContentColor = TextPrimary
                                ),
                                title = {
                                    OstharBrandLogo(
                                        modifier = Modifier.clickable { navigateTab(Routes.HOME) }
                                    )
                                },
                                actions = {
                                    if (session != null) {
                                        val walletInteraction = remember { MutableInteractionSource() }
                                        Surface(
                                            color = Color(0xCC0D182C),
                                            shape = RoundedCornerShape(999.dp),
                                            border = BorderStroke(1.dp, GlassSpecularRim),
                                            modifier = Modifier
                                                .liquidPress(walletInteraction, pressedScale = 0.94f)
                                                .clip(RoundedCornerShape(999.dp))
                                                .clickable(
                                                    interactionSource = walletInteraction,
                                                    indication = null,
                                                    onClick = { navigateTab(Routes.WALLET) }
                                                )
                                                .testTag("topbar_wallet_chip")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Wallet,
                                                    contentDescription = "Wallet balance",
                                                    tint = CyanPrimary,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Text(
                                                    text = Formatters.money(walletBalance),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = CyanPrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(4.dp))
                                    }

                                    IconButton(
                                        onClick = { navController.navigate(Routes.CART) },
                                        modifier = Modifier.testTag("topbar_cart_button")
                                    ) {
                                        BadgedBox(
                                            badge = {
                                                if (cartItemCount > 0) {
                                                    Badge(
                                                        containerColor = CyanPrimary,
                                                        contentColor = BgDeep
                                                    ) {
                                                        AnimatedContent(
                                                            targetState = cartItemCount,
                                                            transitionSpec = {
                                                                (scaleIn(initialScale = 0.6f) + fadeIn())
                                                                    .togetherWith(scaleOut(targetScale = 0.6f) + fadeOut())
                                                            },
                                                            label = "cart_badge"
                                                        ) { count ->
                                                            Text(count.toString(), fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.ShoppingCart,
                                                contentDescription = "Shopping cart",
                                                tint = TextPrimary
                                            )
                                        }
                                    }
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.Transparent,
                                                GlassSpecularSoft,
                                                CyanPrimary.copy(alpha = 0.25f),
                                                GlassSpecularSoft,
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                    },
                    bottomBar = {
                        if (!useRail) {
                            LiquidFloatingNavBar(
                                items = tabs,
                                currentRoute = currentRoute,
                                onSelect = navigateTab
                            )
                        }
                    }
                ) { innerPadding ->
                    val tabRoutes = remember {
                        setOf(Routes.HOME, Routes.STORE, Routes.WALLET, Routes.ORDERS, Routes.ACCOUNT)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Routes.HOME,
                            enterTransition = {
                                if (targetState.destination.route in tabRoutes && initialState.destination.route in tabRoutes) {
                                    fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                                        scaleIn(initialScale = 0.96f, animationSpec = tween(240, easing = FastOutSlowInEasing))
                                } else {
                                    slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { it / 4 } +
                                        fadeIn(tween(240)) +
                                        scaleIn(initialScale = 0.96f, animationSpec = tween(280, easing = FastOutSlowInEasing))
                                }
                            },
                            exitTransition = {
                                if (targetState.destination.route in tabRoutes && initialState.destination.route in tabRoutes) {
                                    fadeOut(tween(180)) +
                                        scaleOut(targetScale = 0.98f, animationSpec = tween(180))
                                } else {
                                    slideOutHorizontally(tween(260, easing = FastOutSlowInEasing)) { -it / 6 } +
                                        fadeOut(tween(200))
                                }
                            },
                            popEnterTransition = {
                                slideInHorizontally(tween(260, easing = FastOutSlowInEasing)) { -it / 6 } +
                                    fadeIn(tween(220)) +
                                    scaleIn(initialScale = 0.97f, animationSpec = tween(260))
                            },
                            popExitTransition = {
                                slideOutHorizontally(tween(260, easing = FastOutSlowInEasing)) { it / 4 } +
                                    fadeOut(tween(200)) +
                                    scaleOut(targetScale = 0.96f, animationSpec = tween(260))
                            }
                        ) {
                            composable(Routes.HOME) {
                                HomeScreen(
                                    productsState = productsState,
                                    settings = siteSettings,
                                    onOpenStore = { game ->
                                        viewModel.selectGameFilter(game)
                                        navigateTab(Routes.STORE)
                                    },
                                    onOpenProduct = { id ->
                                        navController.navigate("product/$id")
                                    },
                                    onAddToCart = { product -> viewModel.addToCart(product, 1) },
                                    onOpenTopUp = {
                                        if (session == null) {
                                            viewModel.setAuthMode(AuthMode.LOGIN)
                                            navController.navigate(Routes.AUTH)
                                        } else {
                                            navController.navigate(Routes.WALLET_TOPUP)
                                        }
                                    },
                                    onOpenRegister = {
                                        if (session == null) {
                                            viewModel.setAuthMode(AuthMode.REGISTER)
                                            navController.navigate(Routes.AUTH)
                                        } else {
                                            navController.navigate(Routes.WALLET_TOPUP)
                                        }
                                    },
                                    onOpenOrders = { navigateTab(Routes.ORDERS) },
                                    onOpenWhatsApp = openWhatsAppSupport,
                                    onOpenLegal = { type -> navController.navigate("legal/$type") },
                                    onOpenAdmin = { navController.navigate(Routes.ADMIN) }
                                )
                            }

                            composable(Routes.STORE) {
                                StoreScreen(
                                    productsState = productsState,
                                    filteredProducts = filteredProducts,
                                    searchQuery = searchQuery,
                                    selectedGame = selectedGame,
                                    selectedCategory = selectedCategory,
                                    inStockOnly = inStockOnly,
                                    sortMode = sortMode,
                                    onSearchChange = viewModel::updateSearchQuery,
                                    onSelectGame = viewModel::selectGameFilter,
                                    onSelectCategory = viewModel::selectCategoryFilter,
                                    onToggleInStock = viewModel::toggleInStockOnly,
                                    onSelectSort = viewModel::selectSort,
                                    onClearFilters = viewModel::clearStoreFilters,
                                    onRetry = viewModel::refreshCatalogAndSettings,
                                    onOpenProduct = { id -> navController.navigate("product/$id") },
                                    onAddToCart = { product -> viewModel.addToCart(product, 1) }
                                )
                            }

                            composable(
                                route = Routes.PRODUCT,
                                arguments = listOf(navArgument("productId") { type = NavType.StringType })
                            ) { entry ->
                                val productId = entry.arguments?.getString("productId").orEmpty()
                                ProductDetailScreen(
                                    productId = productId,
                                    productsState = productsState,
                                    isLoggedIn = session != null,
                                    walletBalance = walletBalance,
                                    checkoutForm = checkoutForm,
                                    onBack = { navController.popBackStack() },
                                    onAddToCart = { p, qty -> viewModel.addToCart(p, qty) },
                                    onOpenProduct = { id -> navController.navigate("product/$id") },
                                    onUidChange = viewModel::onCheckoutUidChanged,
                                    onVerifyNow = viewModel::verifyPlayerNow,
                                    onWhatsappChange = viewModel::onCheckoutWhatsappChanged,
                                    onNoteChange = viewModel::onCheckoutNoteChanged,
                                    onPay = { items ->
                                        viewModel.submitCheckout(items, clearCartOnSuccess = false) { orderId ->
                                            navController.navigate("order/$orderId") {
                                                popUpTo(Routes.STORE)
                                            }
                                        }
                                    },
                                    onLoginClick = {
                                        viewModel.setAuthMode(AuthMode.LOGIN)
                                        navController.navigate(Routes.AUTH)
                                    },
                                    onTopUpClick = { navController.navigate(Routes.WALLET_TOPUP) }
                                )
                            }

                            composable(Routes.CART) {
                                CartScreen(
                                    cart = cart,
                                    isLoggedIn = session != null,
                                    walletBalance = walletBalance,
                                    checkoutForm = checkoutForm,
                                    onBrowseStore = { navigateTab(Routes.STORE) },
                                    onOpenProduct = { id -> navController.navigate("product/$id") },
                                    onUpdateQuantity = viewModel::updateCartQuantity,
                                    onUidChange = viewModel::onCheckoutUidChanged,
                                    onVerifyNow = viewModel::verifyPlayerNow,
                                    onWhatsappChange = viewModel::onCheckoutWhatsappChanged,
                                    onNoteChange = viewModel::onCheckoutNoteChanged,
                                    onPay = {
                                        viewModel.submitCheckout(cart, clearCartOnSuccess = true) { orderId ->
                                            navController.navigate("order/$orderId") {
                                                popUpTo(Routes.HOME)
                                            }
                                        }
                                    },
                                    onLoginClick = {
                                        viewModel.setAuthMode(AuthMode.LOGIN)
                                        navController.navigate(Routes.AUTH)
                                    },
                                    onTopUpClick = { navController.navigate(Routes.WALLET_TOPUP) }
                                )
                            }

                            composable(Routes.WALLET) {
                                WalletOverviewScreen(
                                    isLoggedIn = session != null,
                                    balance = walletBalance,
                                    balanceLoading = balanceLoading,
                                    transactionsState = transactionsState,
                                    topupRequests = topupRequests,
                                    onLoginClick = {
                                        viewModel.setAuthMode(AuthMode.LOGIN)
                                        navController.navigate(Routes.AUTH)
                                    },
                                    onOpenTopUp = { navController.navigate(Routes.WALLET_TOPUP) },
                                    onOpenHistory = { navController.navigate(Routes.WALLET_HISTORY) },
                                    onOpenReceipt = { txId -> navController.navigate("receipt/$txId") },
                                    onOpenStore = { navigateTab(Routes.STORE) },
                                    onRefresh = viewModel::refreshUserData
                                )
                            }

                            composable(Routes.WALLET_TOPUP) {
                                WalletTopUpScreen(
                                    paymentMethods = paymentMethods,
                                    settings = siteSettings,
                                    topupRequests = topupRequests,
                                    form = topUpForm,
                                    onBack = { navController.popBackStack() },
                                    onSelectMethod = viewModel::selectTopUpMethod,
                                    onAmountChange = viewModel::updateTopUpAmount,
                                    onReferenceChange = viewModel::updateTopUpReference,
                                    onReceiptSelected = viewModel::setTopUpReceipt,
                                    onSubmit = viewModel::submitTopUp
                                )
                            }

                            composable(Routes.WALLET_HISTORY) {
                                WalletHistoryScreen(
                                    transactionsState = transactionsState,
                                    onBack = { navController.popBackStack() },
                                    onOpenReceipt = { txId -> navController.navigate("receipt/$txId") },
                                    onRetry = { viewModel.refreshTransactions(limit = 30) }
                                )
                            }

                            composable(
                                route = Routes.RECEIPT,
                                arguments = listOf(navArgument("txId") { type = NavType.StringType })
                            ) { entry ->
                                val txId = entry.arguments?.getString("txId").orEmpty()
                                LaunchedEffect(txId) {
                                    viewModel.loadReceipt(txId)
                                }
                                ReceiptScreen(
                                    receiptState = receiptState,
                                    onBack = { navController.popBackStack() },
                                    onRetry = { viewModel.loadReceipt(txId) }
                                )
                            }

                            composable(Routes.ORDERS) {
                                OrdersScreen(
                                    isLoggedIn = session != null,
                                    ordersState = ordersState,
                                    statusFilter = orderStatusFilter,
                                    searchQuery = orderSearchQuery,
                                    onSelectStatus = viewModel::setOrderStatusFilter,
                                    onSearchChange = viewModel::setOrderSearchQuery,
                                    onOpenOrder = { orderId -> navController.navigate("order/$orderId") },
                                    onBrowseStore = { navigateTab(Routes.STORE) },
                                    onLoginClick = {
                                        viewModel.setAuthMode(AuthMode.LOGIN)
                                        navController.navigate(Routes.AUTH)
                                    },
                                    onRetry = viewModel::refreshOrders
                                )
                            }

                            composable(
                                route = Routes.ORDER_DETAIL,
                                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                            ) { entry ->
                                val orderId = entry.arguments?.getString("orderId").orEmpty()
                                LaunchedEffect(orderId) {
                                    viewModel.loadOrderDetail(orderId)
                                }
                                OrderDetailScreen(
                                    orderDetailState = orderDetailState,
                                    cancelling = cancellingOrder,
                                    onBack = { navController.popBackStack() },
                                    onRetry = { viewModel.loadOrderDetail(orderId) },
                                    onCancelOrder = viewModel::cancelOrder
                                )
                            }

                            composable(Routes.ACCOUNT) {
                                val activeSession = session
                                if (activeSession == null) {
                                    AuthScreen(
                                        state = authUiState,
                                        onModeChange = viewModel::setAuthMode,
                                        onFieldChange = viewModel::updateAuthField,
                                        onSubmit = { viewModel.submitAuth() },
                                        onGoogleSignIn = startGoogleSignIn,
                                        onResendEmail = viewModel::resendConfirmationEmail,
                                        onOpenLegal = { type -> navController.navigate("legal/$type") },
                                        onOpenAdmin = { navController.navigate(Routes.ADMIN) }
                                    )
                                } else {
                                    ProfileScreen(
                                        session = activeSession,
                                        profileState = profileState,
                                        walletBalance = walletBalance,
                                        onOpenWallet = { navigateTab(Routes.WALLET) },
                                        onOpenOrders = { navigateTab(Routes.ORDERS) },
                                        onOpenForgotPassword = {
                                            viewModel.setAuthMode(AuthMode.FORGOT_PASSWORD)
                                            navController.navigate(Routes.AUTH)
                                        },
                                        onOpenWhatsApp = openWhatsAppSupport,
                                        onOpenEmailSupport = openEmailSupport,
                                        onOpenLegal = { type -> navController.navigate("legal/$type") },
                                        onOpenAdmin = { navController.navigate(Routes.ADMIN) },
                                        onSignOut = viewModel::signOut
                                    )
                                }
                            }

                            composable(Routes.AUTH) {
                                AuthScreen(
                                    state = authUiState,
                                    onModeChange = viewModel::setAuthMode,
                                    onFieldChange = viewModel::updateAuthField,
                                    onSubmit = {
                                        viewModel.submitAuth {
                                            navController.popBackStack()
                                        }
                                    },
                                    onGoogleSignIn = startGoogleSignIn,
                                    onResendEmail = viewModel::resendConfirmationEmail,
                                    onOpenLegal = { type -> navController.navigate("legal/$type") },
                                    onOpenAdmin = { navController.navigate(Routes.ADMIN) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(Routes.ADMIN) {
                                AdminPanelScreen(
                                    adminToken = adminToken,
                                    state = adminState,
                                    onBack = { navController.popBackStack() },
                                    onPasswordChange = viewModel::updateAdminPassword,
                                    onLogin = viewModel::adminLogin,
                                    onLogout = viewModel::adminLogout,
                                    onSelectTab = viewModel::selectAdminTab,
                                    onFilterChange = viewModel::updateAdminFilter,
                                    onRefresh = viewModel::refreshAdminData,
                                    onChangePage = viewModel::changeAdminPage,
                                    onUpdateOrderStatus = viewModel::adminUpdateOrderStatus,
                                    onSaveOrderNote = viewModel::adminSaveOrderNote,
                                    onReviewTopup = viewModel::adminReviewTopup,
                                    onViewReceipt = viewModel::adminOpenReceipt,
                                    onClearReceipt = viewModel::adminClearReceiptPreview,
                                    onAdjustWallet = viewModel::adminAdjustWallet,
                                    onSaveProduct = viewModel::adminSaveProduct,
                                    onDeleteProduct = viewModel::adminDeleteProduct,
                                    onRetryNotifications = viewModel::adminRetryNotifications
                                )
                            }

                            composable(
                                route = Routes.LEGAL,
                                arguments = listOf(navArgument("type") { type = NavType.StringType })
                            ) { entry ->
                                val type = entry.arguments?.getString("type") ?: "terms"
                                LegalScreen(
                                    type = type,
                                    settings = siteSettings,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
