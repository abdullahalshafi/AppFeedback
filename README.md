# AppFeedback

A feedback dialog that all of our Android apps share. Call it on every launch and it decides on its own
when to ask. If a user hasn't sent feedback for a while, they must send it before they can keep using the app.

- One multiline field (up to 1000 characters, with a counter), a **Submit** button and a close button. The dialog comes in on a spring animation,
  shakes when the user tries to submit an empty field, and shows an animated tick on success.
- It takes its accent colour from the host app's theme (`colorPrimary`).
- It works in XML apps and in Compose apps. The dialog runs in its own see-through activity, so the host
  app needs no Compose setup.

## Install

`settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

`app/build.gradle.kts`:

```kotlin
implementation("com.github.abdullahalshafi:AppFeedback:1.0.0")
```

Groovy: `maven { url 'https://jitpack.io' }` and `implementation 'com.github.abdullahalshafi:AppFeedback:1.0.0'`.

## Use

Once, in `MainApplication.onCreate`:

```kotlin
AppFeedback.init(
    AppFeedbackConfig(
        baseUrl = "<feedback api url>",
        apiKey = "<this app's secret key>",
        // runs on every check()/show(), so it always sends whoever is logged in at that moment
        userJson = {
            JSONObject()
                .put("user_id", AppUtil.getUserModel().userId)
                .put("user_name", AppUtil.getUserModel().userName)
        },
        // optional
        title = "We'd love your feedback",
        subtitle = "Share your thoughts, ideas or problems.",
        label = "Feedback",
        forceAfterDays = 14,
    )
)
```

In any screen:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    ...
    // every launch: shows the dialog only when it is due
    AppFeedback.check(this)

    // on demand, e.g. from a menu item or button
    binding.feedbackBtn.setOnClickListener { AppFeedback.show(this) }
}
```

`userJson` is a function, not a value: `init()` runs before login, so a fixed user would still be empty
after the user logs in, and stale after a different user logs in. Keys you add inside it are sent with every
feedback. `feedback` is added to a copy, so your objects are never changed.

Keys for one screen only go in the optional `extra`. They are merged into the user for that post and win
on a clash:

```kotlin
AppFeedback.show(this, JSONObject().put("screen", "invoice"))
```

`AppFeedbackConfig`:

| field | required | default | meaning |
|---|---|---|---|
| `baseUrl` | yes | | Root of the feedback api, ending in `/`. Kept in the app, not the library, so this repo never shows the server. |
| `apiKey` | yes | | Sent as `X-API-KEY`. The server uses the key to tell which app the feedback is for. |
| `userJson` | yes | | Function returning a `JSONObject` with `user_id` and `user_name`. Called on every `check()`/`show()`. |
| `title` | no | `We'd love your feedback` | Heading of the dialog. |
| `subtitle` | no | `Share your thoughts, ideas or problems.` | Line under the heading. |
| `label` | no | `Feedback` | Label of the text field. |
| `forceAfterDays` | no | `14` | Days after the last feedback when the dialog opens by itself and can't be closed. |
| `primaryColor` | no | theme `colorPrimary` | Overrides the colour of the dialog. |

## When the dialog shows

The last feedback date is stored in Hawk, separately for each `user_id`. The library uses the app's Hawk if
`MainApplication` already built it, and builds Hawk itself otherwise. When the app logs out with
`Hawk.deleteAll()` the date is cleared too, so the next launch asks the server again.

**`check()`**
1. If a date is stored and `forceAfterDays` have passed since then, the dialog opens and can't be closed.
   Otherwise nothing happens. No API call is made.
2. If no date is stored, it calls `GET feedback/last?user_id=`:
   - The user has never sent feedback: the dialog opens and can't be closed.
   - The user has sent feedback: the server's date is saved, then step 1 runs.
   - The call fails (no internet, server error): nothing happens, and the next launch tries again.

**`show()`** always opens the dialog. The user can close it unless feedback is overdue.

When feedback is submitted, the date is saved. A dialog that can't be closed has no close button, ignores
taps outside the card and the back button (the card shakes instead), and reopens if the user leaves the
app and comes back.

A bad config (invalid `baseUrl`, empty key), a missing `init()`, or a `userJson` function that throws or
returns no `user_id`/`user_name` (e.g. nobody logged in yet) is logged under the tag `AppFeedback` and
skipped. It never crashes the host app.

## Requirements

- minSdk 21, compileSdk 34 or higher, Kotlin 1.8 or higher.
- `check()` needs a `ComponentActivity`. `AppCompatActivity` and `FragmentActivity` both are one.
  `show()` accepts any `Context`.
- XML-only apps get Compose 1.6.6 as a dependency (about +1.5 MB after R8). Apps that already use a newer
  Compose keep their own version.
- Retrofit 2.9.0, OkHttp 4.9.0 and Hawk 2.0.1 are the same versions our apps already use, so nothing is upgraded.
- No Hilt is needed. The library builds its own Retrofit client, and its ProGuard rules come with the AAR.

## Code layout

The library follows the same structure as our apps:

```
com.shafi.app_feedback
  AppFeedback, AppFeedbackConfig   public entry points
  api/          ApiInterface, ApiClient (Retrofit + OkHttp, built without Hilt), Resource, ConnectivityInterceptor
  repository/   BaseRepository.doApiCall, MainRepository.repoGetLastFeedback / repoSubmitFeedback
  rest_model/   LastFeedbackResponseModel, GenericResponseModel, ErrorModel
  util/         AppConstants, AppUtil (Hawk), AppExtensions, ApiFailure, UiMessage, FormFieldType
  compose_ui/feedback/    FeedbackActivity, FeedbackState, FeedbackAction, FeedbackInfo, FeedbackViewModel
  compose_ui/components/  FeedbackBadge, AnimatedCheckmark, LoadingButton, ErrorBanner, ShakeController
  compose_ui/theme/       Color, Theme
```

Everything except `AppFeedback` and `AppFeedbackConfig` is `internal`. That way a host app's own
`MainRepository` or `AppUtil` never gets a second auto-import suggestion from the library.

## Text and language

All text is in `res/values/strings.xml` and every name starts with `app_feedback_`. To change the text or
add a Bangla version, define the same names in the host app (for example in `values-bn/strings.xml`).

## Testing

- `forceAfterDays = 0` makes the dialog required on every launch.
- Clearing the app's data removes the stored dates, so the status API is called again.
- Sample app: put `feedback.base.url=<api url>` and `feedback.api.key=<key>` in `local.properties`, which is not committed, then run `app`.
  Feedback you send from the sample is filed under whichever app owns that key.

## Releasing

1. Change `version` in `app-feedback/build.gradle.kts`.
2. Commit, push, then create a GitHub release or tag with the same version, e.g. `1.0.1`.
3. Open `https://jitpack.io/#abdullahalshafi/AppFeedback` and click **Get it** next to the tag to start the build.
