# ClarkLaunch — product plan

## Product direction

ClarkLaunch is a calm, white-first Android launcher with soft blue accents and an optional whale wallpaper. The home screen should stay quiet; advanced controls belong in focused settings panels.

## Focus mode

### Current implementation (development 0.11.0)
- User-selected list of distracting apps.
- Per-launch choice of 5, 10 or 15 minutes.
- A background WorkManager reminder at the end of the selected interval.
- Notification permission request on Android 13+.
- Optional daily usage overview through UsageStatsManager after the user manually enables Android Usage Access. Results are local and informational; no daily limits or blocking are enforced.
- Settings action to request the Android HOME role through the official RoleManager prompt (Android 10+), with the system HOME settings screen as the older-version fallback.

### Important current limitation
The timer is a reminder scheduled from the moment the user chooses a duration. It is not foreground-usage accounting: time spent outside the selected app is still counted. It does not suspend or block the target app. Do not describe this as an enforced app limit.

### Planned focus features
1. **Conscious pause:** a short, optional pause before launching a selected app, with a clear continue/cancel choice.
2. **Daily budgets:** per-app daily allowance based on Android UsageStats, with a visible remaining-time indicator.
3. **Focus profiles:** Study, Rest and Custom profiles, each with its own selected apps and limits.
4. **Schedules:** optional recurring focus windows, configurable by the user.
5. **Session summary:** local-only daily and weekly totals, with clear reset controls.
6. **Break / emergency exit:** an intentional, non-punitive way to end a focus session.
7. **Actual blocking:** only after usage monitoring is accurate and permission behavior has been tested. Android does not grant ordinary launchers unrestricted control over other apps; any blocking must be transparent, user-authorized, and use supported platform mechanisms.

## Launcher capabilities inspired by established projects

Research references:
- Lawnchair: conventional home grid, app drawer, folders, widgets, icon packs, search and gesture customization.
- Niagara: one-handed, favorites-first navigation and widget stacks.
- KISS Launcher: fast universal search, tags, pinned shortcuts and configurable gestures.
- Kvaesitso: search-first navigation, filters, tags and customizable search results.
- Fokus Launcher: clock/date, weather, app search, configurable shortcuts, notes and Pomodoro-oriented tools.
- Fossify Launcher: familiar grid, folders, widgets and theme controls.

Implemented launcher improvements (0.7.0–0.10.0):
- Hide and restore apps from the ClarkLaunch library and favorites dock. Hidden apps remain installed and accessible outside ClarkLaunch.
- App drawer search matches both visible app labels and package names, ranks label-prefix matches first, then sorts alphabetically.
- Assign apps to Estudo, Trabalho, Pessoal or user-created folders, or leave them without a folder; custom folders can be created, renamed and deleted, with app assignments preserved on rename. Folder choices and custom folder names persist locally and can be included in backups.

Remaining planned ClarkLaunch additions, in priority order:
1. Per-app rename and icon overrides.
2. Configurable gestures with conflict-safe defaults.
3. Home-screen widgets and resizable placement.
4. Icon pack support and per-app icon overrides.
5. Optional widget/search integrations, disabled by default.

## Design and engineering principles
- Keep the default home screen uncluttered and responsive.
- Store personal settings and focus history locally by default.
- Request special Android permissions only when the user enables the related feature, explaining why.
- Do not promise system-wide app blocking until it is implemented and validated on real devices.
- Each release must pass CI compilation and produce a downloadable debug APK before being called test-ready.
