# 🥗 Gestão Alimentação - App Android (MVVM + Jetpack Compose)

Um aplicativo nativo Android moderno desenvolvido em **Kotlin** e **Jetpack Compose** para gerenciamento de refeições diárias, acompanhamento de metas calóricas, consulta de tabela nutricional e receitas saudáveis.

---

## 📱 Telas e Funcionalidades

- 🔐 **Autenticação Local (Login & Cadastro)**:
  - Cadastro de novos usuários e login com validação de credenciais e gerenciamento de sessão local.
  - Não há usuário pré-cadastrado: crie uma conta na tela de cadastro para entrar.

- 📊 **Diário de Hoje (Home)**:
  - Barra de progresso reativa de consumo de calorias diárias (ex: `1450/2000 kcal`).
  - Campo de busca em tempo real para filtragem por nome, categoria ou ingredientes.
  - Lista de refeições diárias (Café da manhã, Almoço, Jantar) com marcação/desmarcação de consumo.
  - Botão de acesso rápido para adicionar novas refeições.

- 📝 **Adicionar / Editar Alimento (CRUD)**:
  - Formulário interativo para cadastro e edição de alimentos.
  - Seleção de categoria (Proteína, Carboidrato, Vegetal), porção/unidade (g, ml, porção), calorias e macros (Carboidratos, Proteínas e Gorduras).
  - Interruptor de lembrete diário.

- 🔬 **Informação Nutricional (Detalhes)**:
  - Visualização detalhada de qualquer refeição cadastrada.
  - Destaque para calorias totais, status de consumo, tabela nutricional completa e anotações.
  - Ações para **Editar** ou **Excluir** o registro.

- 🥗 **Receitas**:
  - Sugestões de refeições saudáveis com busca integrada.
  - Informações de tempo de preparo, calorias e categoria.
  - Detalhes expansíveis contendo ingredientes e modo de preparo passo a passo.

- 👤 **Perfil**:
  - Exibição dos dados do usuário conectado, resumo nutricional pessoal e ação de logout.

---

## 🏗️ Arquitetura e Tecnologias

O projeto utiliza a arquitetura **MVVM (Model-View-ViewModel)** com fluxo de dados reativo:

* **Linguagem**: Kotlin
* **UI**: Jetpack Compose com Material 3
* **Navegação**: Jetpack Navigation Compose
* **Gerenciamento de Estado**: Kotlin Coroutines + `StateFlow`
* **Camada de Dados**: Repositórios locais orientados ao padrão Singleton e reatividade em tempo real.

```
com.example.gestaoalimentacao/
├── data/
│   ├── dal/         # Acesso ao SQLite (UserDao, MealDao, RecipeDao, WaterDao, WeightDao)
│   ├── local/       # AppDatabase (SQLiteOpenHelper)
│   ├── model/       # Data classes (User, Meal, Recipe, WaterIntake, WeightEntry)
│   └── remote/      # Retrofit (TheMealDB) para sugestões de receitas
├── ui/
│   ├── navigation/  # NavHost, rotas e barra de navegação inferior
│   ├── screens/     # Componentes de UI Jetpack Compose (Login, Register, DailyLog, AddEditMeal, etc.)
│   ├── theme/       # Cores, tipografia e temas Material 3
│   └── viewmodel/   # ViewModels (AuthViewModel, MealViewModel, RecipeViewModel)
└── MainActivity.kt  # Ponto de entrada da aplicação
```

---

## 🛠️ Pré-requisitos

Para compilar e executar este projeto em sua máquina local, você precisará de:

* **Android Studio** (Ladybug, Jellyfish ou versão mais recente)
* **JDK 17** ou superior (exigido pelo Android Gradle Plugin 9)
* **Android SDK**:
  * `compileSdk`: 37
  * `minSdk`: 24 (Android 7.0 Nougat ou superior)
* Um emulador Android (AVD) ou dispositivo físico conectado via USB com **Depuração USB** ativada.

---

## 🚀 Como Rodar o Projeto

### 1. Clonar ou Abrir o Projeto no Android Studio
1. Abra o **Android Studio**.
2. Selecione **File ➔ Open...** e navegue até a pasta do projeto: `GestaoAlimentacao`.
3. Aguarde o Android Studio realizar o download das dependências e sincronizar com o Gradle.

### 2. Sincronizar o Gradle (Se necessário)
Caso o projeto peça sincronização manual, clique no ícone **Sync Project with Gradle Files** (ou acesse `File ➔ Sync Project with Gradle Files`).

### 3. Executar o Aplicativo
1. Selecione um emulador ativo ou seu dispositivo USB na barra superior.
2. Certifique-se de que o módulo selecionado é `app`.
3. Clique no botão de Play **Run 'app'** (`Shift + F10`).

### 4. Compilar via Linha de Comando (Terminal / PowerShell)
Você também pode compilar ou instalar o APK via terminal:

* **Windows (PowerShell/CMD)**:
  ```powershell
  .\gradlew.bat assembleDebug
  ```
  Para instalar diretamente no dispositivo conectado:
  ```powershell
  .\gradlew.bat installDebug
  ```

* **Linux / macOS**:
  ```bash
  ./gradlew assembleDebug
  ./gradlew installDebug
  ```

### 5. Rodar os testes
```powershell
.\gradlew.bat testDebugUnitTest
```

> **Windows: caminhos com espaço.** Mantenha o projeto numa pasta sem espaços (ex.: `C:\kotlin\...`) — o Robolectric falha nos testes de banco se o caminho do projeto tiver espaços.
> Se o Gradle falhar com `Unable to establish loopback connection` (pasta do usuário com espaço, ex.: `C:\Users\Nome Sobrenome`), rode antes no PowerShell:
> ```powershell
> $env:JAVA_TOOL_OPTIONS="-Djdk.net.unixdomain.tmpdir=C:\Users\Public"
> ```

---

## 📄 Licença

Este projeto foi desenvolvido para fins educacionais e de demonstração de arquitetura Android moderna. Sinta-se à vontade para estudar e modificar!
