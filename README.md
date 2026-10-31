# Immunology — Forge 1.20.1 compatibility port

Target: Minecraft 1.20.1, Forge 47.2.0, Java 17.

This is a standalone compatibility reimplementation based on the supplied Immunology 1.0.17 compiled archive. Diet and Somnia are **not required**:
- Vanilla hunger replaces Diet integration.
- Vanilla sleeping replaces Somnia fatigue integration.

Included mechanics:
- Immunity 0–100 stored in player persistent NBT and retained through death/respawn.
- Nutritious foods restore immunity; rotten/unsafe foods reduce it.
- Low hunger slowly reduces immunity.
- Fishing can restore immunity.
- Sleeping restores immunity.
- Taking damage reduces immunity.
- 25–49: Weakness and Mining Fatigue.
- Below 25: Insanity effect and occasional hallucination messages.
- At 0: Deterioration starts; after a long period it becomes unrecoverable and eventually fatal.
- Action-bar immunity indicator.
- Original HUD frame and mob-effect texture assets are retained.

The legacy 1.18.2 HUD overlay renderer was not carried over verbatim; this first compatibility build uses an action-bar HUD. A polished 1.20.1 screen overlay can be added after the first successful runtime test.

## Build
Use JDK 17 and Gradle 8.8:
`gradle build`

The jar will be in `build/libs/`. GitHub Actions workflow is included in `.github/workflows/build.yml`.

Important: this source project has not been compiled or tested in a live Minecraft runtime here. It is a port/reimplementation from the provided compiled mod archive, not an official release by the original author. If GitHub Actions reports a compile error, send the full log and it can be corrected.
