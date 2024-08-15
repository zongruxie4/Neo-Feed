CHANGELOG
=========

1.7.0 (15.08.2023) +120 Commits & +10 Translations
------------

#### Function

- Fix: Make sure that DataStore is single
- Fix: Restarting app
- Fix: Over-composition of dialogs in SourcesPage
- Fix: Feed sorting by time
- Update: Use Flow for articles in overlay
- Update: Revamp NavigationManager to use args-safe NavRoute
- Update: Inject repos and client
- Update: Migrate DI from KodeIn to Koin
- Update: Use Kotlin generator in Room
- CompileSDK 35
- Kotlin 2.0
- Dependency Catalogue

#### UI/UX

- Add: Bookmarks filter to OverlayPage
- Add: Pref to remove duplicate articles
- Add: Main pager with Feed, Settings & Feeds
- Add: Scroll to top button
- Add: Hint if no articles are present
- Add: Transparency & collapsable app bar
- Add: Share button as action to articles
- Fix: Applying updated Overlay theme
- Fix: Theming system
- Update: App icon
- Update: Use favorite instead of bookmark icon
- Update: Revamp preferences, articles and feed layouts
- Update: BookmarkItems use same layout as normal articles
- Update: Revamp & unify overlay layouts (xml & composable)
- Update: Overlay menu popup animator
- Remove: BookmarksPage
- Remove: Card background pref (for now)

1.6.0 (XX.XX.2023) Y Commits & Z Translations
------------

#### Function
- Add: Bookmarks page
- Fix: Opening links from article page
- Fix: Editing disabled Feeds
- Update: CompileSdk 34

#### UI
- Add: Dynamic-theming
- Add: Monochrome app icon
- Fix: StatusBar color
- Fix: About page shortcuts
- Update: Revamp Preferences & About pages
- Update: Article card layout
- Update: Return to top FAB color to match theme
- Update: Revamp all item components
- Update: Use Phosphor icons instead of Material
- Update: Drop Compose Material for Material3

#### UX
- Add: Share & open-in-browser buttons to article view
- Add: Bookmark button to article cards
- Fix: Showing real state of Switches e.g. in EditFeedPage
