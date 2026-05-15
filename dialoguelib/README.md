# DialogueLib

DialogueLib brings an asset-based dialogue system into Hytale. Out of the box, it features support for interactive, UI-based conversations between a player and an NPC. It also comes with some built-in utilities for state management within a conversation, easily allowing you implement complex branching and consequences for a player's choices.

## Add this library to your project

TODO

## Basic usage

While a lot can be achieved in DialogueLib as-is, we recommend developing a plugin that extends from it in order to facilitate more complex encounters, such as associated NPC behaviors. The library is intentionally pared down in order to preserve its scope, which is to offer utilities related to dialogue itself, and some foundational applications of it. We leave it up to the user to integrate it further into their projects.

The following examples demonstrate the usage of the asset system. Refer to the documentation for the API.

### Getting started - Dialogue

Everything starts with a dialogue asset. Currently, these can be of type Standard or Chain.

Below is a demonstration of **Standard**-type dialogue.

My_Conversation_1.json
```json
{
  "Type": "Standard",
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

Pay attention to the **filename**. The filename (excluding the .json extension) is used to assign a unique ID to each part of a conversation. This can then be referenced by the Advance Action to continue on to the next part of the dialogue, or an NPC to start the conversation.

As implied, the `Type` field differentiates the dialogue types.

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

To learn more about UI selectors in general, see [Hytale's documentation on this](https://hytalemodding.dev/en/docs/official-documentation/custom-ui#selector-based).

`Lifetime` lets you configure the `CustomPageLifetime` of each dialogue screen, defining how the player can close the window. This field is optional and defaults to `CantClose` if not present, effectively meaning that the player can't escape out of it other than through programmatic means (by using Advance actions to replace the window with a new one, or Close to force the player out of dialogue). Since this is part of Hytale's own API, you should consult their documentation for more information.

`Sprite` is the path to an image you want to show on the screen in middle of the dialogue. Like other elements of the UI, the path is relative to Common/UI/Custom. This is optional.

`Choices` is a collection of DialogueChoices that, when picked, should each perform some kind of action. Use Standard DialogueChoices, which enable you to define `Condition` and `Actions` fields. As the name might imply, `Condition` determines whether the player is shown a particular choice at all.

**Note:** Starting with CodecHelper 0.3.0, you can directly assign strings to `Line` and `Name` fields like so:

My_Conversation_1.json
```json
{
  "Type": "Standard",
  "Line": "Hello! How are you?",
  "Name": "My First NPC",
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

This sets up a DisplayChoice internally.

---

**Chain**-type dialogue can be used to quickly set up lots of dialogue windows that are all connected via single, repetitive choices: e.g. Continue.

My_Conversation_2.json
```json
{
  "Type": "Chain",
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
            "Value": "Knows_NPCs_Real_Identity"
          }
        },
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

There are some repeating fields in this one. Check the above Standard dialogue section for a refresher on those.

`Entries` simplifies the dialogue windows by reducing them to a collection of bare necessities. Each object contains a `Name`, `Line` and `Sprite`. Unlike in Standard dialogue, you directly pass text into `Name` and `Line`, rather than DialogueChoices. These are converted into appropriate DialogueChoices under the hood.

`AdvanceText` is the text put into the single choice given to each entry. This choice only has an Advance action that leads to the next entry, or the `End` dialogue if it's the last entry.

`End` is another Dialogue asset to show once all simplified entries are exhausted. Here you can define dialogue with more complex logic again, or even just one with a different choice from `AdvanceText`.

### Getting started - NPC

To add your dialogue to an NPC, you need to add the following Action (NPC Action, not that of dialogue - see Hytale's documentation for more on those) to whichever Instructions you define:

```json
{
  "Type": "OpenDialogue",
  "Dialogue": "My_Conversation_1"
}
```

OpenDialogue is a type of Action that starts the whole dialogue flow. The `Dialogue` field points to the filename of the first of the interconnected dialogue assets, or whichever other point you want to start from.

If you're unsure about how this should look, below is an example of a generic NPC where the dialogue is opened on right-click:

```json
{
  "Type": "Generic",
  "Parameters": {
    "Invulnerable": {
      "Value": false,
      "Description": "Whether this NPC is invulnerable."
    }
  },
  "StartState": "Idle",
  "DisplayNames": [
    "Klops Merchant",
    "Traveling Trader",
    "Wandering Merchant"
  ],
  "DefaultNPCAttitude": "Ignore",
  "DefaultPlayerAttitude": "Neutral",
  "Appearance": "Klops_Merchant",
  "DropList": "Drop_Klops_Merchant",
  "MaxHealth": 74,
  "DisableDamageGroups": [
    "Self"
  ],
  "BusyStates": [
    "$Interaction"
  ],
  "Invulnerable": {
    "Compute": "Invulnerable"
  },
  "KnockbackScale": 0.5,
  "MotionControllerList": [
    {
      "Type": "Walk",
      "MaxWalkSpeed": 7,
      "Gravity": 10,
      "RunThreshold": 0.3,
      "MaxFallSpeed": 15,
      "MaxRotationSpeed": 360,
      "Acceleration": 10
    }
  ],
  "Instructions": [
    {
      "Instructions": [
        {
          "$Comment": "Idle state - no player nearby",
          "Sensor": {
            "Type": "State",
            "State": "Idle"
          },
          "Instructions": [
            {
              "$Comment": "Greet player when they approach (Alerted animation)",
              "ActionsBlocking": true,
              "Sensor": {
                "Type": "Player",
                "Range": 8
              },
              "Actions": [
                {
                  "Type": "PlayAnimation",
                  "Slot": "Status",
                  "Animation": "Alerted"
                },
                {
                  "Type": "State",
                  "State": "Watching"
                }
              ]
            },
            {
              "Sensor": {
                "Type": "Any"
              },
              "BodyMotion": {
                "Type": "Nothing"
              }
            }
          ]
        },
        {
          "$Comment": "Watching state - player is nearby, just watch them",
          "Sensor": {
            "Type": "State",
            "State": "Watching"
          },
          "Instructions": [
            {
              "$Comment": "Watch nearby players with head",
              "Continue": true,
              "Sensor": {
                "Type": "Player",
                "Range": 12
              },
              "HeadMotion": {
                "Type": "Watch"
              }
            },
            {
              "$Comment": "Clear animation after delay",
              "Continue": true,
              "Sensor": {
                "Type": "Any"
              },
              "Actions": [
                {
                  "Type": "Timeout",
                  "Delay": [
                    2,
                    2
                  ],
                  "Action": {
                    "Type": "PlayAnimation",
                    "Slot": "Status"
                  }
                }
              ]
            },
            {
              "$Comment": "Return to Idle when player leaves (clear animation first)",
              "Sensor": {
                "Type": "Not",
                "Sensor": {
                  "Type": "Player",
                  "Range": 12
                }
              },
              "Actions": [
                {
                  "Type": "PlayAnimation",
                  "Slot": "Status"
                },
                {
                  "Type": "State",
                  "State": "Idle"
                }
              ]
            },
            {
              "Sensor": {
                "Type": "Any"
              },
              "BodyMotion": {
                "Type": "Nothing"
              }
            }
          ]
        },
        {
          "$Comment": "Interaction state - look at player while shop is open",
          "Sensor": {
            "Type": "State",
            "State": "$Interaction"
          },
          "Instructions": [
            {
              "Continue": true,
              "Sensor": {
                "Type": "Target",
                "Range": 10
              },
              "HeadMotion": {
                "Type": "Watch"
              }
            },
            {
              "$Comment": "Return to Watching after interaction ends",
              "Sensor": {
                "Type": "Any"
              },
              "Actions": [
                {
                  "Type": "Timeout",
                  "Delay": [
                    1,
                    1
                  ],
                  "Action": {
                    "Type": "Sequence",
                    "Actions": [
                      {
                        "Type": "ReleaseTarget"
                      },
                      {
                        "Type": "State",
                        "State": "Watching"
                      }
                    ]
                  }
                }
              ]
            }
          ]
        }
      ]
    }
  ],
  "InteractionInstruction": {
    "Instructions": [
      {
        "Sensor": {
          "Type": "Not",
          "Sensor": {
            "Type": "CanInteract",
            "ViewSector": 180
          }
        },
        "Actions": [
          {
            "Type": "SetInteractable",
            "Interactable": false
          }
        ]
      },
      {
        "Continue": true,
        "Sensor": {
          "Type": "Any"
        },
        "Actions": [
          {
            "Type": "SetInteractable",
            "Interactable": true,
            "Hint": "server.interactionHints.talk"
          }
        ]
      },
      {
        "Sensor": {
          "Type": "HasInteracted"
        },
        "Instructions": [
          {
            "Sensor": {
              "Type": "Not",
              "Sensor": {
                "Type": "State",
                "State": "$Interaction"
              }
            },
            "Actions": [
              {
                "Type": "LockOnInteractionTarget"
              },
              {
                "Type": "OpenDialogue",
                "Dialogue": "My_Conversation_1"
              },
              {
                "Type": "State",
                "State": "$Interaction"
              }
            ]
          }
        ]
      }
    ]
  },
  "NameTranslationKey": "server.npcRoles.Klops_Merchant.name"
}
```

### Getting started - DialogueChoices

There are three types of DialogueChoices that exist for the base library. First one is **DisplayChoice**, which has aforementioned uses for non-interactive elements of the UI.

```json
{
  "Id": "Display",
  "Text": "Any text you want here"
}
```

Second is **StandardChoice**, which enables you to set a `Condition` and `Actions`.

Note that DisplayChoice is effectively the same as a StandardChoice, but with `Condition` and `Actions` fields being unset.

```json
{
  "Id": "Standard",
  "Text": "Any text you want here",
  "Condition": {
    "Id": "Equals",
    "MetadataStoreKey": "MY_FIRST_CONVERSATION",
    "MetadataKey": "Knowledge_Status",
    "Metadata": {
      "Id": "String",
      "Value": "Knows_NPCs_Real_Identity"
    }
  },
  "Actions": [
    {
      "Id": "Advance",
      "Next": "Other_Dialogue_Id"
    }
  ]
}
```

Third is **SelectChoice**, which is only used in situations where the _displayed text_ for some dialogue should differ, but the choices are otherwise functionally the same. Its selection depends on string-based metadata (it won't work for any other kind of metadata).

```json
{
  "Id": "Select",
  "Default": {
    "Id": "Standard",
    "Text": "I don't satisfy any of the below values."
  },
  "MetadataStoreKey": "MY_FIRST_CONVERSATION",
  "MetadataKey": "NPC_Opinion",
  "Options": {
    "Dislike": {
	  "Id": "Standard",
	  "Text": "I hope you have a BAD day."
	},
	"Admire": {
	  "Id": "Standard",
	  "Text": "How are you so chill all the time?"
	}
  }
}
```

You can consider SelectChoice as a wrapper around other kinds of DialogueChoices, where only one can be shown to the player. It's similar to a switch-case statement, where the displayed choice is based on the current value of some metadata you read (see below section on metadata).

The `Options` field contains the possible values you want to check for, where you write each value as a *field* ("Dislike", "Admire" in the example). The value of that field is the DialogueChoice to display if this is the current value of your retrieved metadata.

`Default` is a fallback when the metadata is either not set, or is set to a value not contained within `Options`. While it's technically optional, you should always have a default DialogueChoice or else no text will be shown.

### Getting started - Metadata

Metadata allow you to persistently store simple forms of data associated with your dialogue, on the interacting player. This is designed to enable state management, an example being dialogue branching based only on _consequential_ choices as opposed to a more naive, flow-based approach.

There are three types of metadata: **StringMetadata** (for text), **IntegerMetadata** and **BooleanMetadata**. Decimal numbers are not covered, but it's very unlikely that you'll need them instead of other supported datatypes.

All ChoiceActions and ChoiceConditions that deal with metadata have the following fields:

- `MetadataStoreKey`: The field in the player's save data to keep associated metadata, meant primarily for categorically separating otherwise similar `MetadataKey`s. This field is optional everywhere; if left out, it defaults to a field that is "local" to the dialogue asset in which metadata is being handled. Otherwise, it can be accessed by any dialogue asset as long as you write it exactly the same.

- `MetadataKey`: The field in the player's save data that is part of a greater `MetadataStoreKey`. This one actually gets associated with a value and is **NOT** optional - leaving it out means that your ChoiceAction will do nothing, and ChoiceCondition always fails/succeeds depending on the type.

Some take an additional metadata-related field:

```json
"Metadata": {
  "Id": "String",
  "Value": "someTextHere"
}
```

`Id` here can be either String, Integer or Boolean. The associated `Value` is any data of that type.

### Getting started - ChoiceActions

**Advance** is likely the most common kind of ChoiceAction you're going to use.

```json

{
  "Id": "Advance",
  "Next": "My_Conversation_2"
}
```

`Next` should contain the filename of the next Dialogue asset to advance to.

---

If you rely on CantClose lifetimes, **Close** is your other friend. It programmatically closes the UI if you don't want to give the player the option to ESC out of it.

```json
{
  "Id": "Close"
}
```

---

To set some metadata based on a choice, use **SetMetadata**. You can set all kinds of metadata like this, but you should probably use **AdjustInteger** for integers instead.

```json
{
  "Id": "SetMetadata",
  "MetadataStoreKey": "MY_FIRST_CONVERSATION",
  "MetadataKey": "NPC_Opinion",
  "Metadata": {
    "Id": "String",
    "Value": "Hate"
  }
}
```

If you don't include the `Metadata` field, the metadata will be removed (unset) instead.

---

If you're working with integers, you should use **AdjustInteger** since that can add or subtract from an existing IntegerMetadata, or create a new one if it doesn't exist. This will give you a better workflow than naively hardcoding numeric values, unless you explicitly intend for a choice to force a particular value (use **SetMetadata** in that circumstance).

This will do nothing if the metadata key was already used for a non-integer metadata type.

```json
{
  "Id": "AdjustInteger",
  "MetadataStoreKey": "MY_FIRST_CONVERSATION",
  "MetadataKey": "Reputation",
  "Delta": 1,
  "Initial": 0
}
```

`Delta` is the amount to change the current value by. As you might expect, positive number = addition; negative number = subtraction. This field defaults to 1 if you leave it out.

`Initial` handles behavior when the metadata wasn't already created. It creates an IntegerMetadata with the given value, that is then adjusted with `Delta`. This field defaults to 0 if you leave it out.

There is currently no way to leave the metadata unset through this ChoiceAction.

---

The **Conditional** ChoiceAction is an unusual one in that it doesn't perform any action itself, but determines whether another ChoiceAction will execute. It enables you to use ChoiceConditions for ChoiceActions.

```json
{
  "Id": "Conditional",
  "Condition": {
    "Id": "Equals",
    "MetadataStoreKey": "MY_FIRST_CONVERSATION",
    "MetadataKey": "Knowledge_Status",
    "Metadata": {
      "Id": "String",
      "Value": "Knows_NPCs_Real_Identity"
    }
  },
  "IfMet": {
    "Id": "Advance",
    "Next": "My_Conversation_4"
  },
  "IfNotMet": {
    "Id": "Advance",
    "Next": "My_Conversation_5"
  }
}
```

`Condition` works similarly to the field in a Standard DialogueChoice, except you are able to refine its success and failure behaviors. If the condition succeeds, the ChoiceAction attached to the `IfMet` field is executed; otherwise, `IfNotMet` is executed.

You can leave either one of those two fields out if you e.g. only want to do something if the condition fails. If neither field exists, Conditional obviously does nothing.

### Getting started - ChoiceConditions

ChoiceConditions are generally based on persistent data (metadata), the support for which is enabled by DialogueLib. If you want to perform conditions on some kind of world state, you should extend from this library and add your ChoiceConditions. In the coming weeks, we'll add support for basic world state interactions ourselves, other than those already covered by vanilla NPCs.

As such, one important ChoiceCondition is **Equals**, which compares two metadata (one persisted, and a keyless one defined in this ChoiceCondition) and ensures that they are:
- of the same value
- of the same type (an IntegerMetadata of value 1 is not the same as StringMetadata of value "1").

```json
{
  "Id": "Equals",
  "MetadataStoreKey": "MY_FIRST_CONVERSATION",
  "MetadataKey": "NPC_Opinion",
  "Metadata": {
    "Id": "String",
    "Value": "Dislike"
  }
}
```

If you leave the `Metadata` field out, this ChoiceCondition instead checks that your MetadataKey is *unset* (deleted or never made).

In the future, this may be reworked so that it can also compare two persisted metadata.

---

**CompareInteger** ChoiceCondition is used to compare two integers through more boolean operators than just the equality check. This ChoiceCondition further differs from Equals in that it can already compare the values of two *persisted* metadata, as well as some configured value.

The following does a comparison on a configured constant.

```json
{
  "Id": "CompareInteger",
  "MetadataStoreKey": "MY_FIRST_CONVERSATION",
  "MetadataKey": "Reputation",
  "DefaultMetadataValue": 0,
  "Comparison": "GreaterThan",
  "Value": 5
}
```

The `MetadataKey` is always on the left side of the equation, e.g. [REPUTATION VALUE] > 5.

`DefaultMetadataValue` handles scenarios where a given metadata was not initialized yet. This ChoiceCondition does **not** initialize one itself, but uses an alternative value when doing the comparison. Defaults to 0 if you leave this field out.

`Comparison` is the boolean operator to use. Can be LessThan, GreaterThan, LessThanOrEqualTo, GreaterThanOrEqualTo, or EqualTo.

As you might expect, `Value` is a number to compare it against.

The following does a comparison on another persisted metadata.

```json
{
  "Id": "CompareInteger",
  "MetadataStoreKey": "MY_FIRST_CONVERSATION",
  "MetadataKey": "Reputation",
  "DefaultMetadataValue": 0,
  "Comparison": "LesserThan",
  "OtherMetadataStoreKey": "MY_FIRST_CONVERSATION",
  "OtherMetadataKey" : "Enmity",
  "DefaultOtherMetadataValue": 0
}
```

This is similar to the constant comparison, except you target another metadata instead. `OtherMetadataKey` is always on the right side of the equation, e.g. [REPUTATION VALUE] < [ENMITY VALUE]. Likewise, `DefaultOtherMetadataValue` provides an alternative if this metadata is not initialized, and defaults to 0 itself.

You shouldn't include `Value` when comparing two metadata, since that field will be ignored.

---

To compare multiple different conditions, use the **Boolean** ChoiceCondition.

```json
{
  "Id": "Boolean",
  "Kind": "And",
  "Conditions": [
    // your conditions here
  ]
}
```

`Conditions`, obviously, is a collection of ChoiceConditions.

`Kind` determines how these delegate ChoiceConditions are being evaluated. Use "And" if you want to ensure that everything in the collection succeeds, and "Or" to ensure that _at least one_ ChoiceCondition succeeds.

---

If you want to invert the outcome of another ChoiceCondition, e.g. check for failure, use the **Not** condition.

```json

{
  "Id": "Not",
  "Delegate": {
    "Id": "Equals",
    "MetadataStoreKey": "MY_FIRST_CONVERSATION",
    "MetadataKey": "NPC_Opinion",
    "Metadata": {
      "Id": "String",
      "Value": "Dislike"
    }
  }
}
```

## See also

DialogueLib was battle-tested by ourselves. It formed the backbone of an experimental WIP project we started with some friends, and now also serves as a demonstration of the library. If you require examples of:
- complex conversation chains developed with the above features
- DialogueLib being extended

Then take a look at Planting Your Roots (TODO: link on main branch). Assets can be found in various subdirectories of resources/Server. Note that this uses StandardChoices with unset fields rather than DisplayChoices for the non-interactive text.
