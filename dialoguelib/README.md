# DialogueLib

DialogueLib brings an asset-based dialogue system into Hytale. Out of the box, it features support for interactive, UI-based conversations between a player and an NPC. It also comes with some built-in utilities for state management within a conversation, easily allowing you implement complex branching and consequences for a player's choices.

## Add this library to your project

TODO

## Basic usage

While a lot can be achieved in DialogueLib as-is, we recommend developing a plugin that extends from it in order to facilitate more complex encounters, such as associated NPC behaviors. The library is intentionally pared down in order to preserve its scope, which is to offer utilities related to dialogue itself, and some foundational applications of it. We leave it up to the user to integrate it further into their projects.

The following examples demonstrate the usage of the asset system. Refer to the documentation for the API.

### Getting started - Dialogue

Everything starts with a dialogue asset. Currently, these can be of type **Standard** or **Chain**.

Below is a demonstration of Standard-type dialogue.

```json
{
  "Type": "Standard",
  "Id": "My_Conversation_1",
  "Line": {
    "Id": "Display",
    "Text": "Hello! How are you?"
  },
  "Name": {
    "Id": "Display",
    "Text": "My First NPC"
  },
  "UiPage": "My_Custom_Dialogue.ui",
  "UiFragment": "My_Custom_Dialogue_Fragment.ui",
  "Lifetime": "CantClose",
  "Sprite": "Friendly_Fellow.png",
  "Choices": [
    {
      "Id": "Standard",
      "Text": "I'm doing good. What about you?",
      "Actions": [
        {
          "Id": "Advance",
          "Next": "My_Conversation_2"
        }
      ]
    }
  ]
}
```

As implied, the `Type` field differentiates the two dialogue types. However, pay attention to the top-level `Id` field: while `Id` is otherwise used to determine the kind of an Action, Condition, or Choice asset, the top-level `Id` is used to assign a unique, internal ID to this specific part of a conversation. This same ID can be referenced by the `Advance` Action to continue on to the next part, or an NPC to start the conversation.

`Line` determines the unselectable text that is displayed in the dialogue window. This is handled by a DialogueChoice, which will be explained in more detail later. For the moment, notice the `Id` being `Display`, and a `Text` field that contains whatever you want to be shown to the player: `Display` is a trimmed-down kind of choice that is effectively inert, by not placing any kind of condition on it showing up and not being capable of performing any actions. This field is optional, in case you only want to show a series of choices.

`Name` functions in a similar way as `Line`, but it defines a _different_ kind of unselectable text: the one that shows up as the name of the speaker. In the default UI shipped with this library, this maps to the nameplate above the actual dialogue. Likewise, this field is also optional.

In `UiPage`, you can define your own UI file to use as the main body of the dialogue interface. In your resources folder, this file must be contained somewhere in Common/UI/Custom or a subdirectory thereof. It should have following selectors:
  - `#Sprite`, a container for the image found in `Sprite`;
  - `#DialogueName`, which is the Label where the contents of the `Name` field are appended to;
  - `#DialogueLine`, likewise but for `Line`;
  - `#DialogueChoices`, which is a Group that can contain multiple `Choices` via appending multiple instances of `UiFragment`.

To see an example of how you might set these up, the library is packaged with Classic_Dialogue.ui - a simple UI to get you started, but you probably shouldn't use as-is. This is also what the library defaults to when you don't define a `UiPage`.

If you feel that you don't need some of these selectors, leave them out. Make sure that your asset files reflect that, however, as inclusion of certain fields, without matching selectors, can cause issues. For example, if you have no need for sprites, you don't need a `#Sprite` selector - but if at least one of your assets contains a defined `Sprite` field, the client will error and be kicked off your server.

`UiFragment` has a similar role, except it affects individual choices. Again, this file must be contained somewhere in Common/UI/Custom. It should have following selectors:
  - `#DialogueButton`, a Button that gets associated with each choice,
  - `#DialogueLabel`, a Label contained within said button that will hold the text associated with the choice.

To see an example of this one, check out Classic_Dialogue_Fragment.ui, which is also the default value of this field.

`Lifetime` lets you configure the `CustomPageLifetime` of each dialogue screen, defining how the player can close the window. This field is optional and defaults to `CantClose` if not present, effectively meaning that the player can't escape out of it other than through programmatic means (by using `Advance` actions to replace the window with a new one, or `Close` to force the player out of dialogue). Since this is part of Hytale's own API, you should consult their documentation for more information.

`Sprite` is the path to an image you want to show on the screen in middle of the dialogue. Like other elements of the UI, the path is relative to Common/UI/Custom. This is optional.

`Choices` is a collection of DialogueChoices that, when picked, should each perform some kind of action. Use `Standard` DialogueChoices, which enable you to define `Condition` and `Actions` fields; as the name might imply `Condition` determines whether the player is shown a particular choice at all.

---

Chain-type dialogue can be used to quickly set up lots of dialogue windows that are all connected via single, repetitive choices: e.g. Continue.

```json
{
  "Type": "Chain",
  "Id": "My_Conversation_2",
  "Entries": [
    {
      "Name": "My First NPC",
      "Line": "That's good to hear. I'm decent as well.",
      "Sprite": "Friendly_Fellow.png"
    },
    {
      "Name": "My First NPC",
      "Line": "Had some trouble with bunnies trampling all over my garden today, but it's all been sorted out.",
      "Sprite": "Friendly_Fellow.png"
    }
  ],
  "AdvanceText": "Continue",
  "UiPage": "My_Custom_Dialogue.ui",
  "UiFragment": "My_Custom_Dialogue_Fragment.ui",
  "Lifetime": "CantClose",
  "End": {
    "Type": "Standard",
    "Line": {
      "Id": "Standard",
      "Text": "Now that the pleasantries are over with, what do you do?"
    },
    "Name": {
      "Id": "Standard",
      "Text": "Your Inner Voice"
    },
    "UiPage": "My_Custom_Dialogue.ui",
    "UiFragment": "My_Custom_Dialogue_Fragment.ui",
    "Lifetime": "CantClose",
    "Sprite": "Friendly_Fellow.png",
    "Choices": [
      {
        "Id": "Standard",
        "Text": "Alright, see you.",
        "Actions": [
          {
            "Id": "Close"
          }
        ]
      },
      {
        "Id": "Standard",
        "Text": "Hold on, you're the chosen one.",
        "Condition": {
          "Id": "Equals",
          "MetadataStoreKey": "MY_FIRST_CONVERSATION",
          "MetadataKey": "Knowledge_Status",
          "Metadata": {
            "Id": "String",
            
          }
        }
        "Actions": [
          {
            "Id": "Advance",
            "Next": "My_Conversation_3"
          }
        ]
      }
    ]
  }
}

```
