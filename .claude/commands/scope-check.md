---
description: Check whether a proposed task is in scope for the current starter phase.
---

Compare the user's request against the **out-of-scope** list in `CLAUDE.md` section 1:

- real PDF sealing
- real PDF signing
- signature validation
- HSM integration
- certificate workflows
- queues
- orchestration-heavy process managers

If the requested task implies any of the above (including "just stub it", "add a placeholder", "prepare the interface"), stop and ask the user to confirm the scope change before producing code. Do not silently scaffold abstractions for future features.

If the task is in scope, restate it briefly in your own words and proceed.
