# DineOps - Restaurant Management System

## 📋 Project Overview

DineOps is a comprehensive Android-based restaurant management system that streamlines restaurant operations through multiple user roles. The application provides role-based access control, real-time inventory management, order tracking, and business analytics.

### **Key Features:**
- **Multi-Role Authentication:** Owner, Manager, Staff, and Customer roles with role-based dashboards
- **Menu Management:** Add, update, and delete menu items with categories
- **Inventory Management:** Track stock levels, low stock alerts, and automatic inventory deduction
- **Order Processing:** Real-time order placement, tracking, and status updates
- **Business Analytics:** Sales charts, peak hours analysis, revenue tracking
- **Category-Based Menu:** Filter menu items by food categories (Soup, Snacks, Gravy, Noodles, Rice)

### **Technology Stack:**
- **Language:** Java
- **Platform:** Android
- **Backend:** Firebase Realtime Database
- **Authentication:** Firebase Authentication
- **UI Components:** Material Design, RecyclerView, CardView
- **Charts:** MPAndroidChart for data visualization

---

## 🏗️ Architecture

The application follows a modular architecture with clear separation of concerns:

```
├── Authentication Layer (LoginActivity, SignUpActivity)
├── Data Models (User, Order, AdminMenuItem, InventoryItem, CartItem, etc.)
├── Managers (CartManager, InventoryManager)
├── Adapters (MenuAdapter, CartAdapter, StaffOrderAdapter)
├── Activities (Dashboard activities for each role)
└── UI Components (XML layouts with Material Design)
```

---

## 📂 Java Classes & Functions Explained

### **1. MainActivity.java**
**Purpose:** Splash screen that displays on app launch for 3 seconds before redirecting to login.

**UI Elements:** None (programmatic only)

**Methods:**
- `onCreate(Bundle savedInstanceState)`: Initializes the activity and sets up a Handler to delay navigation to LoginActivity by 3 seconds.

---

### **2. LoginActivity.java**
**Purpose:** Handles user authentication and role-based routing.

**UI Elements:**
- `etEmail` (EditText): Email input field
- `etPassword` (EditText): Password input field
- `btnLogin` (Button): Login button
- `signUpTextView` (TextView): Link to sign up page

**Methods:**
- `onCreate(Bundle savedInstanceState)`: Initializes Firebase Auth, sets up click listeners for login and sign up navigation.
- `btnLogin.onClick()`: 
  - Validates email and password fields
  - Uses Firebase Auth to sign in with credentials
  - Fetches user role from Firebase Realtime Database
  - Routes to appropriate dashboard based on role (Owner → OwnerDashboardActivity, Manager → ManagerDashboardActivity, Staff → StaffActivity, Customer → HomeActivity)

---

### **3. SignUpActivity.java**
**Purpose:** Handles new user registration with role selection.

**UI Elements:**
- `editTextFirstName`, `editTextLastName` (EditText): User name fields
- `editTextEmail` (EditText): Email field
- `editTextPassword` (EditText): Password field
- `spinner` (Spinner): Role selection dropdown (Owner, Customer, Staff, Manager)
- `buttonSignUp` (Button): Registration button
- `tvLoginLink` (TextView): Link to login page

**Methods:**
- `onCreate(Bundle savedInstanceState)`: Initializes Firebase Auth, sets up spinner with user roles, configures sign-up button listener.
- `buttonSignUp.onClick()`:
  - Validates all input fields
  - Creates Firebase Auth user with email and password
  - Stores additional user data (name, email, role) in Firebase Realtime Database under "Users/{uid}"
  - Routes to appropriate dashboard based on selected role

---

### **4. User.java**
**Purpose:** Data model representing a user in the system.

**Fields:**
- `firstName`: User's first name
- `lastName`: User's last name
- `email`: User's email address
- `role`: User's role (Owner, Manager, Staff, Customer)

**Methods:**
- Constructor with all fields for creating new users
- Default constructor required for Firebase deserialization
- Getters for all fields

---

### **5. HomeActivity.java**
**Purpose:** Main landing page for customers to access Menu and Order features.

**UI Elements:**
- `tvUserName` (TextView): Welcome message
- `btnMenu`, `btnNavMenu` (Button): Navigate to MenuActivity
- `btnOrder`, `btnNavOrder` (Button): Navigate to OrderActivity

**Methods:**
- `onCreate(Bundle savedInstanceState)`: Sets up click listeners for menu and order navigation.

---

### **6. MenuActivity.java**
**Purpose:** Displays menu items with category filtering and add-to-cart functionality.

**UI Elements:**
- `recyclerViewMenu` (RecyclerView): Displays menu items
- `tabLayoutCategories` (TabLayout): Category filter tabs (All, Soup, Snacks, Gravy, Noodles, Rice)
- `btnOrder` (Button): Navigate to cart/order page

**Methods:**
- `onCreate(Bundle savedInstanceState)`: Initializes RecyclerView, loads menu items from Firebase, sets up category tabs.
- `loadMenuItems()`: 
  - Fetches all menu items from Firebase "MenuItems" node
  - Collects unique categories from menu items
  - Creates tabs for each category dynamically
  - Initially displays all items
- `filterMenuByCategory(int position)`: Filters menu items based on selected category tab, updates RecyclerView.

---

### **7. menuitem.java**
**Purpose:** Data model for menu items displayed in the customer view.

**Fields:**
- `name`: Item name
- `price`: Item price
- `category`: Item category
- `imageResource`: Image resource ID (currently unused)

**Methods:**
- Constructor with all fields
- Getters for all fields

---

### **8. MenuAdapter.java**
**Purpose:** RecyclerView adapter for displaying menu items.

**Methods:**
- `onCreateViewHolder(ViewGroup parent, int viewType)`: Inflates menu item layout.
- `onBindViewHolder(MenuViewHolder holder, int position)`: Binds menu item data to views.
  - Sets item name and price
  - `orderNowButton.onClick()`: Navigates to OrderActivity with item details
  - `addToCartButton.onClick()`: Adds item to cart using CartManager with category information

---

### **9. OrderActivity.java**
**Purpose:** Cart management and order placement for customers.

**UI Elements:**
- `etUserName` (EditText): Customer name input
- `etPhoneNumber` (EditText): Phone number input
- `tvEmptyCart` (TextView): Empty cart message
- `tvCartTotal` (TextView): Total cart amount
- `btnConfirmOrder` (Button): Submit order
- `recyclerViewCart` (RecyclerView): Cart items list

**Methods:**
- `onCreate(Bundle savedInstanceState)`: Initializes CartManager, sets up RecyclerView, loads cart items.
- `loadCartItems()`: 
  - Loads cart items from CartManager
  - Shows/hides empty cart message
  - Sets up CartAdapter with remove button functionality
- `updateCartTotal()`: Calculates and displays total cart price.
- `sendOrderData()`:
  - Validates name and phone fields
  - Calls InventoryManager to deduct inventory for order items
  - Creates Order object with order details
  - Saves order to Firebase "Orders" node
  - Clears cart on successful order placement

---

### **10. CartItem.java**
**Purpose:** Data model for items in the shopping cart.

**Fields:**
- `name`: Item name
- `price`: Item price
- `category`: Item category
- `quantity`: Quantity in cart

**Methods:**
- Constructors with and without category
- `incrementQuantity()`: Increases quantity by 1
- `decrementQuantity()`: Decreases quantity if > 1
- Getters and setters for all fields

---

### **11. CartManager.java**
**Purpose:** Singleton class to manage shopping cart state across activities.

**Methods:**
- `getInstance()`: Returns singleton instance of CartManager.
- `addToCart(String name, String price, String category)`: 
  - Checks if item already exists in cart
  - If exists, increments quantity
  - If not, adds new CartItem with category
- `removeFromCart(int position)`: Removes item from cart at specified position.
- `clearCart()`: Clears all items from cart.
- `getCartItems()`: Returns list of cart items.
- `getTotalPrice()`: Calculates total price of all items in cart.

---

### **12. CartAdapter.java**
**Purpose:** RecyclerView adapter for displaying cart items.

**Methods:**
- `onCreateViewHolder(ViewGroup parent, int viewType)`: Inflates cart item layout.
- `onBindViewHolder(CartViewHolder holder, int position)`: 
  - Binds cart item data (name, price, quantity)
  - `btnRemove.onClick()`: Removes item from cart via CartManager

---

### **13. Order.java**
**Purpose:** Data model representing a customer order.

**Fields:**
- `orderId`: Unique order identifier
- `userName`: Customer name
- `phoneNumber`: Customer phone number
- `dishName`: Name of dish(es) ordered
- `tableNumber`: Table number
- `size`: Order size (not used)
- `numberOfPlates`: Total number of plates
- `totalPrice`: Total order price
- `timestamp`: Order timestamp
- `status`: Order status (Pending, Preparing, Ready, Delivered, Completed)

**Methods:**
- Constructors with and without status
- Getters and setters for all fields

---

### **14. InventoryManager.java**
**Purpose:** Singleton class to manage inventory deduction when orders are placed.

**Methods:**
- `getInstance()`: Returns singleton instance.
- `deductInventoryForOrder(List<CartItem> cartItems, InventoryDeductionCallback callback)`:
  - Fetches all menu items and their ingredient mappings
  - Fetches current inventory levels
  - Calculates total inventory deductions for each cart item
  - Updates inventory in Firebase Realtime Database
  - Checks for insufficient stock
  - Calls callback with success/failure status and error messages

**Interface:**
- `InventoryDeductionCallback`: Callback interface with `onDeductionComplete(boolean success, String errorMessage)`

---

### **15. InventoryItem.java**
**Purpose:** Data model for inventory items.

**Fields:**
- `itemId`: Unique inventory item ID
- `name`: Item name
- `category`: Item category
- `quantity`: Current stock quantity
- `unit`: Unit of measurement (kg, L, pcs)
- `minThreshold`: Minimum stock threshold for low stock alert
- `lastUpdated`: Last update timestamp

**Methods:**
- Constructor with all fields
- `isLowStock()`: Returns true if quantity is below threshold
- Getters and setters for all fields

---

### **16. AdminMenuItem.java**
**Purpose:** Data model for menu items managed by admin/manager.

**Fields:**
- `itemId`: Unique menu item ID
- `name`: Item name
- `price`: Item price
- `category`: Item category
- `ingredients`: List of MenuItemIngredient objects required to prepare this item

**Methods:**
- Constructor with all fields
- Getters and setters for all fields

---

### **17. MenuItemIngredient.java**
**Purpose:** Data model linking menu items to inventory ingredients.

**Fields:**
- `inventoryItemId`: ID of inventory item
- `quantity`: Quantity of inventory item needed per serving
- `menuName`: Menu item name (for display)

**Methods:**
- Constructor with inventoryItemId and quantity
- Getters and setters for all fields

---

### **18. ManagerDashboardActivity.java**
**Purpose:** Dashboard for managers to manage menu, inventory, and view analytics.

**UI Elements:**
- `etMenuName`, `etMenuPrice`, `etMenuCategory` (EditText): Menu item input fields
- `btnAddMenuItem` (Button): Add menu item button
- `btnLogout` (Button): Logout button
- `btnCustomerView`, `btnStaffView` (Button): Navigation buttons
- `tvActiveOrders`, `tvRevenue`, `tvMenuItems` (TextView): Analytics metrics
- `tvLowStockAlert` (TextView): Low stock alert banner
- `rvMenuItems` (RecyclerView): Menu items list
- `rvInventory` (RecyclerView): Inventory items list
- `etMappingMenuItem`, `etMappingInventoryItem`, `etMappingQuantity` (EditText): Ingredient mapping fields
- `btnAddMapping` (Button): Add mapping button

**Methods:**
- `onCreate(Bundle savedInstanceState)`: Initializes Firebase references, sets up RecyclerViews, loads data.
- `loadMenuItems()`: Fetches menu items from Firebase, updates analytics.
- `loadInventory()`: Fetches inventory items, checks for low stock, updates alert banner.
- `loadAnalytics()`: Fetches orders, calculates total orders and revenue.
- `addMenuItem()`: Validates input, creates new AdminMenuItem, saves to Firebase.
- `MenuItemsAdapter`: Inner RecyclerView adapter for menu items with delete functionality.
- `InventoryAdapter`: Inner RecyclerView adapter for inventory items with update/delete functionality.
- `showUpdateStockDialog(InventoryItem item)`: Shows dialog to update inventory quantity.

---

### **19. OwnerDashboardActivity.java**
**Purpose:** Dashboard for owners with business analytics and decision-making insights.

**UI Elements:**
- `tvTotalOrders`, `tvTotalRevenue`, `tvTotalCustomers`, `tvAvgOrderValue`, `tvTopCategory` (TextView): Key metrics
- `barChart` (BarChart): Sales by food category chart
- `tvPeakHour1/2/3`, `tvPeakHour1/2/3Orders` (TextView): Peak hours analysis
- `btnManagerView`, `btnLogout` (Button): Navigation buttons

**Methods:**
- `onCreate(Bundle savedInstanceState)`: Initializes Firebase references, sets up bar chart, loads data.
- `loadMenuItems()`: Loads menu items to build category lookup cache.
- `fetchOrdersData()`: 
  - Fetches all orders from Firebase
  - Calculates total orders, revenue, unique customers
  - Tracks sales by category using menu item category cache
  - Analyzes peak hours from order timestamps
  - Updates UI with metrics and charts
- `updateMetricsDisplay()`: Updates all metric TextViews.
- `updateBarChart()`: Configures and updates bar chart with category sales data and custom colors.
- `updatePeakHoursDisplay()`: Identifies and displays top 3 peak hours.
- `getHourFromTimestamp(long timestamp)`: Converts timestamp to hour of day (0-23).
- `getTimePeriod(int hour)`: Converts hour to AM/PM format.

---

### **20. StaffActivity.java**
**Purpose:** Dashboard for staff to view and manage active orders.

**UI Elements:**
- `recyclerViewOrders` (RecyclerView): Active orders list
- `btnLogout` (Button): Logout button

**Methods:**
- `onCreate(Bundle savedInstanceState)`: Initializes Firebase reference, sets up RecyclerView, loads orders.
- `loadOrders()`: 
  - Fetches orders from Firebase
  - Filters out completed orders
  - Updates RecyclerView with StaffOrderAdapter
- `btnLogout.onClick()`: Signs out Firebase user, navigates to LoginActivity.

---

### **21. StaffOrderAdapter.java**
**Purpose:** RecyclerView adapter for displaying staff orders with status management.

**Methods:**
- `onCreateViewHolder(ViewGroup parent, int viewType)`: Inflates staff order item layout.
- `onBindViewHolder(OrderViewHolder holder, int position)`:
  - Binds order data (dish name, customer, table, quantity, price)
  - Sets status badge color based on current status
  - Dynamically changes button text and action based on status:
    - Pending → "Preparing" button → changes to Preparing
    - Preparing → "Ready" button → changes to Ready
    - Ready → "Delivered" button → changes to Delivered
    - Delivered → "Completed" button → changes to Completed
- `updateOrderStatus(String orderId, String newStatus)`: Updates order status in Firebase.

---

### **22. StaffOrderAdapter.OrderViewHolder**
**Purpose:** ViewHolder pattern for staff order items.

**UI Elements:**
- `tvDishName`, `tvCustomerName`, `tvTableNumber`, `tvQuantity`, `tvPrice`, `tvStatus` (TextView)
- `btnPreparing` (Button): Status update button

---

## 🎨 UI Layouts & Components

### **Authentication Screens:**
- **activity_login.xml**: Login form with email/password fields and sign-up link
- **activity_sign_up.xml**: Registration form with name, email, password, and role spinner

### **Customer Screens:**
- **activity_home.xml**: Welcome screen with Menu and Order navigation
- **menu.xml**: Menu display with category tabs (TabLayout) and RecyclerView
- **activity_order.xml**: Cart with item list, customer details form, and confirm button
- **item_cart.xml**: Cart item card with name, quantity, price, and remove button

### **Dashboard Screens:**
- **activity_manager_dashboard.xml**: 
  - Header with DineOps logo and logout
  - Metrics cards (Active Orders, Revenue, Menu Items)
  - Low stock alert banner
  - Menu management form and list
  - Inventory list
  - Menu-to-ingredient mapping form
  - Customer/Staff view navigation buttons

- **activity_owner_dashboard.xml**:
  - Header with logo and logout
  - Key metrics in grid layout (Total Orders, Revenue, Customers, Avg Order Value, Top Category)
  - Bar chart for sales by food category with legend
  - Peak hours analysis section
  - Manager view navigation button

- **activity_staff.xml**:
  - Header with logo and logout
  - Active orders list with RecyclerView
  - Order cards with status badges and action buttons

### **Admin/Manager Screens:**
- **activity_inventory_management.xml**: 
  - Header with logo and logout
  - Add inventory form (Item Name, Quantity, Unit, Category, Min Threshold)
  - Inventory list with RecyclerView

### **Item Layouts:**
- **item_admin_menu.xml**: Menu item card with name, price, category, and delete button
- **item_inventory.xml**: Inventory item card with name, category, quantity, threshold, and update/delete buttons
- **item_staff_order.xml**: Order card with dish name, customer info, table, quantity, price, status badge, and action button

---

## 🔧 How It Works

### **User Flow:**
1. **Splash Screen (MainActivity)** → Login
2. **Login (LoginActivity)** → Role-based routing
3. **Sign Up (SignUpActivity)** → Create account with role
4. **Customer Flow:** Home → Menu (add to cart) → Order (place order)
5. **Manager Flow:** Dashboard → Manage Menu/Inventory → View Analytics
6. **Owner Flow:** Dashboard → View Business Analytics & Reports
7. **Staff Flow:** Dashboard → View Orders → Update Order Status

### **Data Flow:**
1. **Menu Items:** Stored in Firebase "MenuItems" node with category information
2. **Orders:** Stored in Firebase "Orders" node with status tracking
3. **Inventory:** Stored in Firebase "Inventory" node with quantity tracking
4. **Users:** Stored in Firebase "Users" node with role information
5. **Cart:** Managed locally by CartManager singleton
6. **Inventory Deduction:** Automatic deduction when order is placed via InventoryManager

### **Category-Based Menu System:**
- Menu items are stored with category field (Soup, Snacks, Gravy, Noodles, Rice)
- MenuActivity dynamically creates tabs based on available categories
- CartManager stores category with each cart item
- OwnerDashboard uses category cache to track sales by category
- Top category is determined by highest sales count

---

## 🎯 Key Features Implementation

### **Role-Based Access Control:**
- Implemented in LoginActivity and SignUpActivity
- User role stored in Firebase Realtime Database
- Role determines which dashboard/activity the user can access

### **Real-Time Inventory Management:**
- InventoryManager singleton manages stock levels
- Automatic deduction when orders are placed
- Low stock alerts in Manager Dashboard
- Inventory items have minimum threshold for alerts

### **Order Status Workflow:**
- Orders progress through: Pending → Preparing → Ready → Delivered → Completed
- Staff can update status with single button click
- Button text and action change dynamically based on current status
- Completed orders are filtered out from staff view

### **Business Analytics:**
- Owner Dashboard displays comprehensive metrics
- Bar chart shows sales by food category with custom colors
- Peak hours analysis identifies busiest times
- Category-based sales tracking using menu item cache

### **Category-Based Menu:**
- TabLayout provides category filtering
- Categories are dynamically loaded from menu items
- All items shown when "All" tab is selected
- Category information stored with cart items for analytics

---

## 📊 Firebase Database Structure

```
restaurantapp/
├── Users/
│   └── {uid}/
│       ├── firstName
│       ├── lastName
│       ├── email
│       └── role
├── MenuItems/
│   └── {itemId}/
│       ├── name
│       ├── price
│       ├── category
│       └── ingredients[]
├── Inventory/
│   └── {itemId}/
│       ├── name
│       ├── category
│       ├── quantity
│       ├── unit
│       ├── minThreshold
│       └── lastUpdated
└── Orders/
    └── {orderId}/
        ├── userName
        ├── phoneNumber
        ├── dishName
        ├── tableNumber
        ├── numberOfPlates
        ├── totalPrice
        ├── timestamp
        └── status
```

---

## 🚀 Build & Run

### **Prerequisites:**
- Android Studio
- Java 11 or higher
- Firebase project configured
- Firebase SDK dependencies in build.gradle

### **Steps:**
1. Clone the repository
2. Open in Android Studio
3. Configure Firebase (add google-services.json)
4. Build and run on emulator or device

---

## 📝 Notes

- The app uses Material Design components for a modern, clean UI
- All screens follow a consistent design pattern with DineOps logo header
- Color scheme is consistent across all screens (Blue #2563EB, Green #16A34A, Red #EF4444)
- RecyclerView adapters use ViewHolder pattern for efficient scrolling
- Singleton pattern used for CartManager and InventoryManager
- Firebase Realtime Database enables real-time updates across all dashboards
- Inventory deduction is asynchronous with callback for error handling
- Order status updates are real-time and visible to staff immediately

---

## 🎨 UI Design Highlights

- **Consistent Header:** All screens feature DineOps logo in top-left with logout button (except customer screens)
- **Material Cards:** All lists use MaterialCardView with rounded corners (12dp) and subtle shadows
- **Color Scheme:** Professional color palette with semantic meaning (green for success, red for errors/danger, blue for primary actions)
- **Clean Typography:** Sans-serif fonts with clear hierarchy (bold for titles, regular for body text)
- **Responsive Layouts:** Uses CoordinatorLayout and NestedScrollView for proper scrolling
- **Bottom Navigation:** Consistent bottom navigation bar with Menu and Order buttons
- **Modern Inputs:** Material TextInputLayout with rounded corners and outlined style

---

## 🔐 Security

- Firebase Authentication handles user authentication
- Role-based access control ensures users can only access features relevant to their role
- Passwords are securely managed by Firebase Auth
- No sensitive data stored locally on device

---

## 📱 Screen Flow Diagram

```
MainActivity (Splash)
    ↓
LoginActivity
    ↓
SignUpActivity (for new users)
    ↓
┌───────────┬───────────┬───────────┬───────────┐
│           │           │           │           │
Owner       Manager    Staff      Customer
Dashboard   Dashboard   Dashboard   HomeActivity
│           │           │           │           │
│           │           │           │      MenuActivity
│           │           │           │           │
│           │           │           │      OrderActivity
└───────────┴───────────┴───────────┴───────────┘
```

---

## 🛠️ Future Enhancements

- Table assignment system
- Order history and analytics for customers
- Kitchen display system integration
- Payment gateway integration
- Inventory prediction and auto-reordering
- Customer loyalty program
- Receipt generation and printing
- Multi-location support

---

## 👥 Team & Credits

Developed as a comprehensive restaurant management solution using Android, Java, and Firebase Realtime Database.

---

## 📄 License

Proprietary - All rights reserved
