Finální Shrnutí Architektury a Kódu Aplikace FokusFlow

  Celkový Obraz: Architektura (View - ViewModel - Model)


  Od začátku jsme se snažili aplikaci budovat na moderních principech. Tím nejdůležitějším je Oddělení zodpovědností (Separation of Concerns), které
  nyní v aplikaci máme:


   * View (Vrstva pro zobrazení): `MainActivity.kt`
       * Účel: Stará se pouze o zobrazení uživatelského rozhraní. Je "hloupá" – neobsahuje žádnou složitou logiku ani data. Jen přijímá data od
         ViewModelu a zobrazuje je. Když uživatel na něco klikne, MainActivity to jen oznámí ViewModelu.
       * Analogie se Springem: Toto je vaše View (např. Thymeleaf šablona).


   * ViewModel (Logická vrstva): `TaskViewModel.kt`
       * Účel: Je to "mozek" aplikace. Drží a spravuje stav (data – v našem případě seznam všech úkolů) a obsahuje veškerou business logiku (jak přidat
         úkol, jak ho smazat, jak úkoly filtrovat).
       * Analogie se Springem: Toto je kombinace vašeho @Controller a @Service.


   * Model (Datová vrstva): `Task.kt`
       * Účel: Definuje, jak vypadají naše data. Je to jen "šablona" pro úkol, která říká, jaké má vlastnosti.
       * Analogie se Springem: Toto je vaše Entity nebo DTO třída.

  ---

  Průvodce Soubory: Kde Co Hledat?


  `app/src/main/java/.../fokusflow/Task.kt` (Model)
   * `data class Task`: Určuje, z čeho se skládá úkol: id, name, description, priority a dueDate.
   * `enum class Priority`: Pomocný typ, který omezuje prioritu jen na hodnoty Low, Medium a High.


  `app/src/main/java/.../fokusflow/TaskViewModel.kt` (ViewModel - Logika)
   * `_tasks`: Soukromý, "master" seznam VŠECH úkolů. Je to jediný zdroj pravdy o našich datech.
   * `deadlineTasks` a `freeTasks`: Dva veřejné seznamy, které jsou automaticky odvozené z _tasks. Nefiltrují se pokaždé ručně, systém se o to stará
     sám, kdykoliv se _tasks změní.
   * `addTask(...)`: Funkce, která obsahuje logiku pro vytvoření nového úkolu a jeho přidání do _tasks.
   * `deleteTask(...)`: Funkce, která obsahuje logiku pro odebrání úkolu z _tasks.


  `app/src/main/java/.../fokusflow/MainActivity.kt` (View - Zobrazení)
  Toto je náš největší soubor, pojďme si ho rozdělit:
   * Třída `MainActivity` a metoda `onCreate`: Vstupní bod obrazovky. Jediné, co dělá, je, že získá instanci TaskViewModel a spustí naše UI.
   * Hlavní UI (`Scaffold`):
       * `Row { ... }`: Hlavní kontejner, který umisťuje dva sloupce (seznamy) vedle sebe.
       * `Column(Modifier.weight(1f))`: Dva hlavní sloupce, kde každý zabírá polovinu obrazovky a obsahuje nadpis a TaskList.
       * `FloatingActionButton`: Plovoucí tlačítko "+", které slouží k zobrazení dialogu pro přidání úkolu.
   * Stavové proměnné (`taskToDelete`, `showAddTaskDialog`):
       * Toto jsou proměnné, které řídí pouze stav UI (např. viditelnost dialogů). Neobsahují business data. Proto jsou zde, ve View vrstě, a ne ve
         ViewModelu.
   * Dialogy (`DeleteConfirmationDialog`, `AddTaskDialog`):
       * Jsou to samostatné, znovupoužitelné @Composable funkce, což zpřehledňuje kód.
       * Zobrazí se jen tehdy, když jim to jejich stavová proměnná dovolí.
       * Používají klíčový princip: "Stav teče dolů, události tečou nahoru". To znamená, že dialog dostane data k zobrazení, a když se v něm něco stane
         (klik na "Potvrdit"), oznámí to přes "callback" (onConfirm) svému rodiči.
   * `TaskList`:
       * Naše komponenta pro zobrazení seznamu. Nyní obsahuje contentPadding (vnitřní mezeru) na spodní straně, aby bylo jasné, kde seznam končí.

  ---
  Aktualizovaná Tabulka s Analogií



  ┌────────────────────────┬─────────────────────────┬──────────────────┬───────────────────────────────┐
  │ Spring MVC Komponenta  │ Android (Naše Aplikace) │ Kde to najdu?    │ Účel                          │
  ├────────────────────────┼─────────────────────────┼──────────────────┼───────────────────────────────┤
  │ View (Thymeleaf/JSP)   │ @Composable funkce      │ MainActivity.kt  │ Vykreslení UI                 │
  │ @Controller / @Service │ TaskViewModel           │ TaskViewModel.kt │ Příprava dat, business logika │
  │ Model / Entity         │ data class Task         │ Task.kt          │ Reprezentace dat              │
  │ Vstupní bod (request)  │ MainActivity            │ MainActivity.kt  │ Vstupní bod pro obrazovku     │
  └────────────────────────┴─────────────────────────┴──────────────────┴───────────────────────────────┘



  Doufám, že vám toto detailní shrnutí pomůže lépe se v kódu orientovat. Pokud byste chtěla, mohu tento aktualizovaný popis znovu uložit do souboru
  `ARCHITECTURE.md` ve vašem projektu.
