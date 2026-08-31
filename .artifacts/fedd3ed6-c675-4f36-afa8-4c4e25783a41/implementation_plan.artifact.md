# Fix ModalBottomSheet compilation error

The `windowInsets` parameter in `ModalBottomSheet` has been deprecated and hidden in favor of `contentWindowInsets` in recent versions of Compose Material3 (1.3.0+). This change requires updating the parameter name and passing the value as a lambda.

## Proposed Changes

### UI Components

#### [MODIFY] [NowPlayingBottomSheet.kt](file:///Users/robin/.gemini/antigravity/scratch/MusicPlayer/app/src/main/java/com/mimica/musicplayer/ui/components/NowPlayingBottomSheet.kt)

- Update `ModalBottomSheet` call to use `contentWindowInsets` instead of `windowInsets`.
- Wrap the `WindowInsets(0, 0, 0, 0)` value in a lambda to match the expected `@Composable () -> WindowInsets` type.

## Verification Plan

### Automated Tests
- Run `:app:compileDebugKotlin` to verify the fix.
```bash
./gradlew :app:compileDebugKotlin
```
