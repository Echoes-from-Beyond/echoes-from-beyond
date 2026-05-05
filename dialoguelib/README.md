# DialogueLib

DialogueLib brings an asset-based dialogue system into Hytale. Out of the box, it features support for interactive, UI-based conversations between a player and an NPC. It also comes with some built-in utilities for state management within a conversation, easily allowing you implement complex branching and consequences for a player's choices.

## Add this library to your project

TODO

## Basic usage

While a lot can be achieved in DialogueLib as-is, we recommend developing a plugin that extends from it in order to facilitate more complex encounters, such as associated NPC behaviors. The library is intentionally pared down in order to preserve its scope, which is to offer utilities related to dialogue itself, and some foundational applications of it. We leave it up to the user to integrate it further into their projects.

The following examples demonstrate the usage of the asset system. Refer to the documentation for the API.

### Get started - Dialogue

Everything starts with a dialogue asset. Currently, these can be of type **Standard** or **Chain**.

Below is a demonstration of Standard-type dialogue.

```
{
  "Type": "Standard",
  "Id": "My_Conversation_1",
  "Line": {
    "Id": "Standard",
    "Text": "Hello! How are you?"
  },
  "Name": {
    "Id": "Standard",
    "Text": "My First NPC"
  },
  "UiPage": "My_Custom_Dialogue.ui",
  "UiFragment": "My_Custom_Dialogue_Fragment.ui",
  "Choices": [
    {
      "Id": "Standard",
      "Text": "Continue",
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
