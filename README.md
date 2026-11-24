# ClarkLaunch

Um launcher Android independente, construído com Kotlin e Jetpack Compose.

## Foundation 0.3.0

- Interface clara em branco pérola, com detalhes rosa e azul-gelo.
- Relógio digital e data em português na tela inicial.
- Ícones reais e biblioteca pesquisável de aplicativos instalados.
- Dock com até cinco favoritos, persistidos localmente.
- Atualização da lista de aplicativos ao retornar ao launcher.
- Seleção de papel de parede pela galeria, com persistência de acesso quando suportada.
- Controle de intensidade visual do wallpaper e opção para removê-lo.
- Configurações iniciais do launcher.
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
