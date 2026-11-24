# ClarkLaunch

Um launcher Android independente, construído com Kotlin e Jetpack Compose.

## Estado atual

**Foundation 0.1.0**
- Tela inicial com identidade visual escura em tom vinho.
- Busca e listagem de aplicativos instalados.
- Abertura de aplicativos diretamente pelo launcher.
- Registro como opção de aplicativo de início (HOME) do Android.
- GitHub Actions configurado para compilar o APK de debug.

## Requisitos

- JDK 17
- Android SDK 35
- Gradle 8.9

## Compilar

Na raiz do projeto:

```bash
gradle assembleDebug
```

O APK será gerado em `app/build/outputs/apk/debug/app-debug.apk`.

Também é possível executar o workflow **Android Build** na aba Actions do GitHub e baixar o artefato `ClarkLaunch-debug`.

## Identificador

`com.clarklaunch.app`
