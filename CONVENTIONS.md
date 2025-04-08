# ReadeckApp - Developer Conventions

This document outlines the coding conventions and best practices for the ReadeckApp project. Adhering to these conventions will help maintain code quality, readability, and consistency across the codebase.

## 1. Code Style

*   **Language:** Kotlin
*   **Formatting:**
    *   Use the standard Kotlin code style as defined by IntelliJ IDEA (or Android Studio).  This includes indentation (4 spaces), line breaks, and spacing.  Use the IDE's auto-formatting features frequently.
    *   Maximum line length: 120 characters.
*   **Naming Conventions:**
    *   **Classes and Interfaces:** PascalCase (e.g., `BookmarkRepositoryImpl`).
    *   **Functions and Variables:** camelCase (e.g., `getBookmarks`, `bookmarkList`).
    *   **Constants:** UPPER\_SNAKE\_CASE (e.g., `MAX_PAGE_SIZE`).
    *   **Private Members:** Use the `private` keyword.
    *   **Boolean variables:** Use `is` or `has` prefixes (e.g., `isFavorite`, `hasArticle`).
*   **Comments:**
    *   Use KDoc for public functions, classes, and interfaces to provide clear documentation.
    *   Explain complex logic or non-obvious code sections with inline comments.
    *   Avoid redundant comments that simply restate the code.
*   **Imports:**
    *   Organize imports automatically using the IDE's features.
    *   Avoid wildcard imports (e.g., `import de.readeckapp.domain.*`) unless absolutely necessary.
*   **Code Organization:**
    *   Keep related code together (e.g., data classes in the same file or a dedicated `model` package).
    *   Use meaningful package names that reflect the application's structure.

## 2. Architecture

*   **Architecture Pattern:**  MVVM (Model-View-ViewModel)
    *   **Model:** Represents the data and business logic.  Includes data classes, repositories, and use cases.
    *   **View:**  The UI (Compose Composable functions).  Responsible for displaying data and handling user input.
    *   **ViewModel:**  Acts as an intermediary between the View and the Model.  Prepares data for the UI, handles user actions, and manages the application's state.
*   **Clean Architecture Principles:**
    *   Separate concerns into distinct layers (Presentation, Domain, Data).
    *   Dependencies should point inwards (outer layers depend on inner layers).
    *   Use interfaces to define contracts between layers.

## 3. Testing

*   **Types of Tests:**
    *   **Unit Tests:** Test individual components (e.g., ViewModels, Use Cases) in isolation.  Use MockK for mocking dependencies.
    *   **Integration Tests:** Test the interaction between multiple components (e.g., ViewModel and Repository).
    *   **UI Tests:** Test the UI and user interactions.  Use Compose UI testing.
*   **Test Structure:**
    *   Follow a consistent test structure (Arrange-Act-Assert).
    *   Use descriptive test names that clearly indicate the scenario being tested.
*   **Test Coverage:**
    *   Strive for high test coverage to ensure code quality and prevent regressions.
*   **Test Doubles:**
    *   Use mock objects to isolate the unit under test.

## 4. Error Handling

*   **Exception Handling:**
    *   Use `try-catch` blocks to handle exceptions.
    *   Log exceptions using Timber with appropriate log levels (e.g., `Timber.e()`).
    *   Provide user-friendly error messages in the UI.
*   **Result Types:**
    *   Consider using a `sealed class` or `Result` type to represent the outcome of operations (success, failure, error).

## 5. Dependency Injection

*   **Dependency Injection Framework:** Hilt
*   **Module Structure:**
    *   Use Hilt modules to provide dependencies.
    *   Use `@InstallIn` to specify the component scope (e.g., `SingletonComponent`, `ViewModelComponent`).
    *   Use `@Inject` to inject dependencies into classes.
    *   Use `@Provides` to provide dependencies from modules.
*   **Component Scopes:**
    *   Use appropriate component scopes to manage the lifecycle of dependencies.

## 6. Networking

*   **Networking Library:** Retrofit with Kotlin Serialization
*   **HTTP Client:** OkHttp
*   **Authentication:**
    *   Use an `AuthInterceptor` to add authentication headers (e.g., Bearer token) to network requests.
    *   Store authentication tokens securely (e.g., EncryptedSharedPreferences).
*   **Error Handling:**
    *   Handle network errors (e.g., connection timeouts, HTTP status codes) gracefully.
    *   Provide retry mechanisms for transient errors.
*   **Base URL:**
    *   Use a base URL interceptor to dynamically set the base URL.

## 7. Data Storage

*   **Database:** Room
*   **DataStore:** For storing simple key-value pairs (e.g., user preferences).
*   **Database Design:**
    *   Use a well-defined database schema with appropriate data types and relationships.
    *   Use data access objects (DAOs) to interact with the database.
*   **Data Synchronization:**
    *   Implement mechanisms to synchronize data between the local database and the remote server.

## 8. UI Development (Compose)

*   **Composable Functions:**
    *   Create reusable composable functions for UI elements.
    *   Use descriptive names for composable functions.
    *   Pass data and callbacks as parameters to composable functions.
*   **State Management:**
    *   Use `remember`, `mutableStateOf`, and `collectAsState` to manage UI state.
    *   Use `ViewModel` to hold and manage UI state.
*   **Theming:**
    *   Use `MaterialTheme` to apply a consistent theme to the UI.
    *   Define custom colors, typography, and shapes in the theme.
*   **Layout:**
    *   Use Compose layout components (e.g., `Column`, `Row`, `Box`, `LazyColumn`) to structure the UI.
    *   Use modifiers to customize the appearance and behavior of UI elements.
*   **Previews:**
    *   Use `@Preview` annotations to create previews of composable functions in the IDE.

## 9. Kotlin Specifics

*   **Data Classes:** Use data classes to represent data models.
*   **Extension Functions:** Use extension functions to add functionality to existing classes.
*   **Coroutines:** Use coroutines for asynchronous operations.
*   **Flow:** Use `Flow` for reactive data streams.
*   **Null Safety:**  Use Kotlin's null safety features to avoid null pointer exceptions.
*   **Immutability:** Favor immutable data structures (e.g., `val` instead of `var`).

## 10. Git and Version Control

*   **Version Control System:** Git
*   **Branching Strategy:**
    *   Use a feature branch workflow.
    *   Create a new branch for each new feature or bug fix.
    *   Use pull requests to merge changes into the main branch.
*   **Commit Messages:**
    *   Write clear and concise commit messages.
    *   Use the imperative mood (e.g., "Add feature", "Fix bug").
    *   Include a brief description of the changes.
    *   Reference related issues or pull requests.
*   **Code Reviews:**
    *   All code changes should be reviewed by another developer before merging.

## 11. Code Style (Specifics from existing code)

*   Use `Timber` for logging.
*   Use `Hilt` for dependency injection.
*   Use `Coil` for image loading.
*   Use `Kotlinx.datetime` for date and time.
*   Use `androidx.compose.ui.viewinterop.AndroidView` to integrate `WebView`.

This document will be updated as the project evolves.
