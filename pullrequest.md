# Opaque Overlay Transition

The transition is now positional instead of a window crossfade. The overlay window stays transparent, with visibility and window alpha changing only at lifecycle endpoints. The configured feed background and opacity are applied to the translated foreground panel, so the launcher remains visible ahead of the moving feed edge.

Touch-driven closes remain visible through the final translated frame and hide at offset `0`. An accepted programmatic Home/app-close from a fully open panel hides immediately while the internal panel state settles.

Verification completed:

- `./gradlew :google-gsa:testDebugUnitTest :app:testDebugUnitTest` passed.
- No APK build was run.

Device transition scenarios, including launcher progress comparison and transparent-layer behavior, were not run in this environment and remain to be validated on a protocol-compatible launcher.
