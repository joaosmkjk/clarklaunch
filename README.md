# ClarkLaunch

Um launcher Android independente, construído com Kotlin e Jetpack Compose.

## Foundation 0.4.0

- Identidade visual clara em branco pérola, rosa suave e azul-gelo.
- Relógio digital e data em português na tela inicial.
- Ícones reais e biblioteca pesquisável de aplicativos instalados.
- Dock com até cinco favoritos, persistidos localmente.
- Navegação por gesto horizontal: deslize para a esquerda para abrir a biblioteca e para a direita para voltar.
- Botão Voltar do Android retorna da biblioteca ou limpa a busca antes de sair.
- Seleção de papel de parede pela galeria.
- Intensidade do wallpaper persistida entre sessões.
- Configurações iniciais do launcher.
- Atualização da lista de aplicativos ao retornar à tela inicial.
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
