# ClarkLaunch

Um launcher Android independente, construído com Kotlin e Jetpack Compose.

## Foundation 0.2.0

- Identidade visual clara em branco pérola, com detalhes rosa e azul.
- Ícones reais dos aplicativos instalados.
- Biblioteca com busca e acesso direto aos aplicativos.
- Dock de até cinco favoritos, com seleção por estrela e persistência local.
- Escolha de papel de parede pela galeria, com permissão persistente quando suportada pelo provedor.
- Registro como opção de aplicativo de início (HOME) do Android.
- GitHub Actions para compilar e publicar o APK de debug como artefato.

## Requisitos

- JDK 17
- Android SDK 35
- Gradle 8.9

## Compilar

```bash
gradle assembleDebug
```

O APK será gerado em `app/build/outputs/apk/debug/app-debug.apk`.

Também é possível executar o workflow **Android Build** na aba Actions do GitHub e baixar o artefato `ClarkLaunch-debug`.

## Identificador

`com.clarklaunch.app`

## Nota sobre visibilidade de aplicativos

O launcher usa `QUERY_ALL_PACKAGES` para exibir aplicativos instalados. Essa permissão é sujeita a políticas específicas caso o app seja publicado na Google Play; para distribuição direta, avalie as regras aplicáveis ao canal de distribuição.
