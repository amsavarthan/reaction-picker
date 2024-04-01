[![GitHub license](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE) [![](https://jitpack.io/v/amsavarthan/reaction-picker.svg)](https://jitpack.io/#amsavarthan/reaction-picker)

# Reaction Picker 💙

Reaction picker is a library project for Android using Jetpack compose, which brings the similar functionality of Facebook's most popular reaction picker.

<img height=480 src="https://github.com/amsavarthan/reaction-picker/blob/main/art/demo.gif"/>

## 👨🏻‍💻 Installation

Step 1: Add the JitPack repository to your build file

```kotlin

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
      mavenCentral()
      maven { url = uri("https://jitpack.io") }
    }
}

```
<br/>

Step 2: Add the dependency  [![](https://jitpack.io/v/amsavarthan/reaction-picker.svg)](https://jitpack.io/#amsavarthan/reaction-picker)

```kotlin

dependencies {
  implementation("com.github.amsavarthan:reaction-picker:<latest_version>")
}

```

## 🤓 Basic Usage

> You can checkout the implementation [here](https://github.com/amsavarthan/reaction-picker/tree/main/app).

**Step 1:** Wrap your [App-level composable](https://developer.android.com/develop/ui/compose/layouts/adaptive#explicit-layout-changes) with `ReactionPickerLayout`

```kotlin

AppTheme {
  ReactionPickerLayout {
    Scaffold { padding ->
      //...
    }
  }
}

```

**Step 2:** Create a Reaction picker state using `rememberReactionPickerState`

> [!TIP]
> `Reaction` class offers multiple overloads. Choose the appropriate one based on your specific usage requirements.

```kotlin

val items = listOf(
    Reaction(label = "Love", painterResource = R.drawable.love),
    Reaction(label = "Care", painterResource = R.drawable.care),
    Reaction(label = "Wow", painterResource = R.drawable.wow),
    Reaction(label = "Haha", painterResource = R.drawable.haha),
    Reaction(label = "Sad", painterResource = R.drawable.sad),
    Reaction(label = "Angry", painterResource = R.drawable.angry),
)

val state = rememberReactionPickerState(
    reactions = items,
    initialSelection = null,
)

```

**Step 3:** Include `reactionPickerAnchor` in the modifier chain of the composable that you wish to use as an anchor.

```kotlin

IconButton(
    modifier = modifier.reactionPickerAnchor(state),
    onClick = state::toggleReaction
) {
    if (state.selectedReaction != null) {
        Image(
            modifier = Modifier.size(32.dp),
            painter = painterResource(id = state.selectedReaction!!.painterResource!!),
            contentDescription = "react to post",
        )
    } else {
        Icon(
            modifier = Modifier.size(32.dp),
            imageVector = Icons.Rounded.FavoriteBorder,
            contentDescription = "react to post",
        )
    }
}

```

**That's it.** Just long press on the anchor for the beautiful reaction picker 🥳. 

## 🎨 Customization

> [!NOTE]  
> Since this library is in its initial phase, I've only implemented basic customization features. **Suggestions for new ideas are highly encouraged and welcome.**

**Custom reaction item:**

The `label` and `icon` of a reaction are not restricted and can be freely utilized. For tailored customization of the label and icon, developers should leverage the `icon` and `label` slots provided by the `ReactionPickerLayout`.

```kotlin

ReactionPickerLayout(
  icon = { reaction ->  },
  label = { label -> }
){
  //..
}

```

**Customizing the reaction box:**

The shape, background color of the reaction box can be customized by passing the respective parameters available in `ReactionPickerLayout`

```kotlin

ReactionPickerLayout(
  containerColor = /*color*/, // Defaults to Color.Black
  containerShape = /*shape*/ // Defaults to CircleShape
){
  //..
}

```

**Customizing item size on hover:**

You can supply your custom `ReactionPickerProperties` to `ReactionPickerLayout`, specifying sizes for active (hovered), inactive (not currently hovered but something is), and idle states.

```kotlin

data class ReactionPickerProperties(
    val idleReactionSize: Dp = ReactionItemSize, // Defaults to 40.dp
    val activeReactionSize: Dp = ReactionItemSizeOnHover, // Defaults to 72.dp
    val inActiveReactionSize: Dp = ReactionItemSizeOnAnyHover, // Defaults to 34.dp
    val spaceBetweenReactions: Dp = SpacingBetweenReactions, // Defaults to 4.dp
    val spaceBetweenReactionAndLabel: Dp = SpacingBetweenReactionItemAndLabel, // Defaults to 8.dp
    val hapticFeedbackEnabled: Boolean = true, // Defaults to true 
    val dismissOnBackPress: Boolean = true, // Defaults to true
)

```

**Customizing the position:**

By default, the reaction picker will appear above the anchor, offset to the right based on the tapped position. If it exceeds the boundary, it will automatically shift to the left or move below. This behavior is managed by the default position provider from `ReactionPickerDefaults.rememberReactionPickerPositionProvider`. To customize this functionality, developers can implement the `ReactionPickerPositionProvider` interface.

```kotlin

interface ReactionPickerPositionProvider {

    fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
        anchorTappedPosition: IntOffset,
    ): IntOffset

}

```

## 📩 Contact

DM me at 👇

* Twitter: <a href="https://twitter.com/lvamsavarthan" target="_blank">@lvamsavarthan</a>
* Email: contact@amsavarthan.dev


## License 🔖
```
MIT License

Copyright (c) 2023 Amsavarthan Lv

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
