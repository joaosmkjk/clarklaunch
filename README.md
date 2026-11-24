# ClarkLaunch

Um launcher Android independente, construído com Kotlin e Jetpack Compose.

## Development 0.10.0

- Identidade visual clara em branco pérola, rosa suave e azul-gelo.
- Relógio digital e data em português na tela inicial.
- Ícones reais e biblioteca pesquisável de aplicativos instalados.
- Dock com até cinco favoritos, persistidos localmente.
- Pastas personalizadas para organizar aplicativos, com criação e exclusão; categorias padrão preservadas.
- Backup e restauração incluem os nomes das pastas personalizadas.
- Navegação por gesto horizontal: deslize para a esquerda para abrir a biblioteca e para a direita para voltar.
- Botão Voltar do Android retorna da biblioteca ou limpa a busca antes de sair.
- Seleção de papel de parede pela galeria.
- Intensidade do wallpaper persistida entre sessões.
- Configurações iniciais do launcher.
- Ação para solicitar ao Android que defina o ClarkLaunch como launcher padrão, usando o seletor oficial de função HOME.
- App Lock com PIN de 4 a 8 dígitos, hash PBKDF2 com salt aleatório e seleção individual de aplicativos protegidos.
- Modo Foco: seleção de aplicativos que distraem, sessões de 5, 10 ou 15 minutos e lembrete em segundo plano ao fim da sessão, usando WorkManager.
- Solicitação de PIN ao abrir apps protegidos pela biblioteca ou pela dock do ClarkLaunch.
- Atualização da lista de aplicativos ao retornar à tela inicial.
- Backup e restauração em JSON de favoritos, listas de apps e pastas; o PIN nunca é exportado.
- Visão local do uso diário dos aplicativos, mediante ativação manual de Acesso ao uso nas configurações do Android.
- Configuração do ClarkLaunch como tela inicial padrão pelo fluxo oficial do Android; a escolha permanece sob controle do usuário.
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

## Limite importante do App Lock

O App Lock intercepta aberturas iniciadas pela interface do ClarkLaunch. Ele não bloqueia notificações, links externos, configurações do Android, outro launcher ou outras formas de iniciar o aplicativo. Portanto, não deve ser tratado como bloqueio de sistema ou controle parental. Biometria e proteção contra caminhos externos ainda não estão implementadas.

## Limites do Modo Foco

O Modo Foco atual agenda um lembrete ao final do intervalo escolhido. Ele não mede o tempo real em primeiro plano, não interrompe o aplicativo nem impede que o usuário continue usando-o. O lembrete depende de notificações permitidas pelo Android. A tela Uso de hoje consulta estatísticas locais após autorização explícita em Acesso ao uso. Ela é informativa e não aplica limites nem bloqueios.

## Nota sobre visibilidade de aplicativos

O launcher usa `QUERY_ALL_PACKAGES` para exibir aplicativos instalados. Essa permissão é sujeita a políticas específicas caso o app seja publicado na Google Play; para distribuição direta, avalie as regras aplicáveis ao canal de distribuição.
