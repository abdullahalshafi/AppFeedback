# AppFeedback

[![JitPack](https://jitpack.io/v/abdullahalshafi/AppFeedback.svg)](https://jitpack.io/#abdullahalshafi/AppFeedback)
![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)

A lightweight Android library for collecting in-app user feedback. Call it once on launch and it decides
when to ask. When a user's feedback is overdue, the dialog becomes mandatory until they respond.

<!-- Screenshot: <img src="docs/screenshot.png" width="320" alt="AppFeedback dialog"> -->

## Features

- **Two-line integration.** Initialise once in your `Application`, then call `check()` on launch.
- **Works in any app.** The dialog runs in its own translucent activity, so View/XML apps need no
  Jetpack Compose setup.
- **Periodic, enforced feedback.** Configurable interval (14 days by default). Once it has passed, the
  dialog can't be dismissed until feedback is sent.
- **On-demand feedback.** Open the dialog from any button or menu with `show()`.
- **Per-user tracking.** The last submission date is cached on the device for each user. The server is
  queried only when no local record exists.
- **Custom metadata.** Attach any user, device or screen information to each submission.
- **Material 3 UI.** Spring entrance animation, shake on empty submit, animated success state, and a
  1,000-character limit with a live counter.
- **Themable and localisable.** Uses your theme's `colorPrimary` by default. Every text can be
  overridden or translated.
- **Fail-safe.** Invalid configuration or a missing user is logged, never thrown. Survives configuration
  changes and process death.
- **No dependency injection required.** No Hilt or Dagger. R8/ProGuard rules are bundled with the AAR.

## Requirements

| | |
|---|---|
| minSdk | 21 |
| compileSdk | 34 or higher |
| Kotlin | 1.8 or higher |
| Java | 8 or higher |
| Backend | A server implementing the [Backend API](#backend-api) |

## Installation

Add the JitPack repository to `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.abdullahalshafi:AppFeedback:1.0.0")
}
```

<details>
<summary>Groovy</summary>

```groovy
// settings.gradle
maven { url 'https://jitpack.io' }

// app/build.gradle
implementation 'com.github.abdullahalshafi:AppFeedback:1.0.0'
```

</details>

## Quick start

### 1. Initialise

Call `init()` once in your `Application.onCreate()`:

```kotlin
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        AppFeedback.init(
            AppFeedbackConfig(
                baseUrl = BuildConfig.FEEDBACK_BASE_URL,
                apiKey = BuildConfig.FEEDBACK_API_KEY,
                userJson = {
                    JSONObject()
                        .put("user_id", session.userId)
                        .put("user_name", session.userName)
                },
            )
        )
    }
}
```

### 2. Check on launch

Call `check()` from your main screen. It opens the dialog only when feedback is due:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // ...
    AppFeedback.check(this)
}
```

### 3. Open on demand (optional)

```kotlin
binding.feedbackButton.setOnClickListener {
    AppFeedback.show(this)
}
```

## Configuration

| Parameter | Required | Default | Description |
|---|---|---|---|
| `baseUrl` | Yes | | Root URL of the feedback API. A trailing `/` is added if missing. |
| `apiKey` | Yes | | Sent with every request in the `X-API-KEY` header. Identifies your app to the server. |
| `userJson` | Yes | | Function returning a `JSONObject` that contains at least `user_id` and `user_name`. |
| `title` | No | `We'd love your feedback` | Dialog heading. |
| `subtitle` | No | `Share your thoughts, ideas or problems.` | Text below the heading. |
| `label` | No | `Feedback` | Label of the input field. |
| `forceAfterDays` | No | `14` | Days after the last submission before the dialog opens automatically and becomes mandatory. |
| `primaryColor` | No | Theme `colorPrimary` | Accent colour of the dialog (`@ColorInt`). |

> [!TIP]
> Keep `baseUrl` and `apiKey` out of source control, for example in `local.properties` exposed through
> `buildConfigField`. The sample app shows this setup.

## Sending user data

`userJson` is a function, not a value. It runs on every `check()` and `show()` call, so it always reflects
the user who is currently signed in, including after login, logout or an account switch.

- `user_id` and `user_name` are required. If either is missing or blank (for example, before login), the
  call is skipped and logged.
- Any other keys are forwarded to the server unchanged.
- `feedback` is reserved. The library sets it to the text the user typed.

To attach data to a single submission, pass an optional `extra` object. Its keys are merged into the user
data for that call and take precedence on conflicts:

```kotlin
AppFeedback.show(this, JSONObject().put("screen", "checkout"))
```

The resulting request body:

```json
{
  "user_id": "1024",
  "user_name": "Jane Doe",
  "screen": "checkout",
  "feedback": "The checkout button is hard to find."
}
```

## How it works

### `check(activity)`

1. If a last-submission date is cached for the user, the dialog opens as **mandatory** once
   `forceAfterDays` have passed. Otherwise nothing happens. No network request is made.
2. If no date is cached, the library calls `GET feedback/last`:
   - **No previous feedback:** the mandatory dialog opens.
   - **Previous feedback found:** the date is cached and step 1 is applied.
   - **Request failed:** nothing happens. The next launch tries again.

### `show(context)`

Always opens the dialog. The user can dismiss it, unless feedback is overdue.

### Mandatory mode

A mandatory dialog has no close button and ignores outside taps and the back button (the card shakes
instead). It reopens if the user leaves the app and returns, and it closes only after a successful submission.

### Local cache

The last submission date is stored with [Hawk](https://github.com/orhanobut/hawk), keyed by `user_id`.
If your app already initialises Hawk, the library uses the same instance. Otherwise it initialises Hawk
itself. Clearing Hawk (for example with `Hawk.deleteAll()` on logout) is safe: the next `check()` queries
the server again.

## Backend API

The library expects two endpoints relative to `baseUrl`. Every request includes the header
`X-API-KEY: <apiKey>`.

### Get last feedback

```http
GET {baseUrl}feedback/last?user_id={user_id}
```

Response `200 OK`:

```json
{
  "status": "success",
  "has_feedback": true,
  "last_feedback_at": "2026-10-01 14:30:00"
}
```

| Field | Type | Description |
|---|---|---|
| `has_feedback` | boolean | Whether the user has submitted feedback before. |
| `last_feedback_at` | string \| null | Time of the last submission, formatted `yyyy-MM-dd HH:mm:ss`. Interpreted in the device's time zone. |

Additional fields in the response are ignored.

### Submit feedback

```http
POST {baseUrl}feedback
Content-Type: application/json
```

The body is the object returned by `userJson`, merged with `extra`, plus the `feedback` field. See
[Sending user data](#sending-user-data).

Response `2xx`:

```json
{
  "status": "success",
  "message": "Feedback submitted."
}
```

A response with `"status": "error"` is treated as a failure and its `message` is shown to the user.
For `400`, `401` and `422` responses, the `message` field of the error body is shown when present.
Other errors show a generic message.

## Customisation

### Colour

The dialog uses the host theme's `colorPrimary`. To override it, set `primaryColor`:

```kotlin
AppFeedbackConfig(
    // ...
    primaryColor = ContextCompat.getColor(this, R.color.brand),
)
```

### Text and localisation

`title`, `subtitle` and `label` are set in the configuration. All other text is defined as string
resources prefixed with `app_feedback_`. To change or translate a string, define a resource with the
same name in your app, for example in `res/values-fr/strings.xml`:

```xml
<string name="app_feedback_submit">Envoyer</string>
```

| Resource | Default |
|---|---|
| `app_feedback_submit` | Submit |
| `app_feedback_close` | Close |
| `app_feedback_please_share_your_feedback_to_continue` | Please share your feedback to continue using the app. |
| `app_feedback_thank_you` | Thank you! |
| `app_feedback_your_feedback_has_been_submitted` | Your feedback has been submitted. |
| `app_feedback_no_internet_please_check_your_connection` | No internet! Please check your connection. |
| `app_feedback_something_went_wrong` | Something went wrong |

## Error handling

AppFeedback never crashes the host app. The following situations are logged under the tag `AppFeedback`,
and the call is skipped:

- `check()` or `show()` called before `init()`
- An invalid `baseUrl`, a blank `apiKey` or a negative `forceAfterDays`
- `userJson` throws, or returns no `user_id` or `user_name`

## Dependencies

The library brings in the following transitive dependencies. If your app already uses newer versions,
Gradle resolves to them.

| Library | Version |
|---|---|
| Jetpack Compose UI / Foundation | 1.6.6 |
| Compose Material 3 | 1.2.1 |
| AndroidX Activity | 1.8.0 |
| AndroidX Lifecycle | 2.7.0 |
| Retrofit + Gson converter | 2.9.0 |
| OkHttp logging interceptor | 4.9.0 |
| Hawk | 2.0.1 |

In a View-only app, Compose adds roughly 1.5 MB to the release APK after R8.

## Sample app

The `app` module is a minimal View-based sample. To run it, add your server details to
`local.properties` (this file is not committed):

```properties
feedback.base.url=https://example.com/api/
feedback.api.key=your-api-key
```

### Testing tips

- Set `forceAfterDays = 0` to make the dialog mandatory on every launch.
- Clear the app's data to remove cached dates and force a server check.
