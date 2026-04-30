# Architektura Aplikace FokusFlow

Tento dokument vysvětluje finální strukturu aplikace FokusFlow, která následuje moderní doporučení pro vývoj Android aplikací (MVVM).

### **Základní Architektura: Model-View-ViewModel (MVVM)**

Aplikace je rozdělena do logických vrstev, což zajišťuje čistotu kódu a snadnou údržbu:

1.  **View (UI vrstva)**:
    *   Implementována v **Jetpack Compose**.
    *   `MainActivity.kt` slouží jako vstupní bod a spravuje navigaci (Drawer).
    *   Jednotlivé obrazovky (`HomeScreen`, `CompletedScreen`, `TrashScreen`) jsou čistě deklarativní a pouze zobrazují data dodaná ViewModelem.
    *   Komponenty v `TaskComponents.kt` zajišťují znovupoužitelnost prvků (seznamy, karty úkolů, dialogy).

2.  **ViewModel (Logika)**:
    *   `TaskViewModel.kt` je "mozkem" aplikace.
    *   Přežívá změny konfigurace (např. otočení displeje).
    *   Komunikuje s databází a externími službami pomocí coroutines (asynchronně).
    *   Poskytuje data pro UI pomocí `StateFlow`, což zajišťuje reaktivní aktualizaci obrazovky při jakékoli změně dat.

3.  **Model (Data)**:
    *   `Task.kt` definuje entitu úkolu.
    *   Obsahuje pole pro název, popis, prioritu, termín (dueDate) a polohu (souřadnice + název místa).

4.  **Data Source (Ukládání a Síť)**:
    *   **Room Database**: Lokální SQLite databáze pro trvalé ukládání úkolů.
    *   **TaskDao.kt**: Definice SQL dotazů. Implementuje pokročilé řazení (priorita High > Medium > Low).
    *   **Retrofit**: Použit pro komunikaci s externím API (`ZenQuotes`) pro načítání motivačních citátů.
    *   **Location Services**: Integrace s `FusedLocationProviderClient` pro získávání přesné GPS polohy uživatele.

---

### **Klíčové Funkce a Technologie**

*   **Inteligentní GPS senzor**: Aplikace vynucuje vysokou přesnost a filtruje nepřesné odhady ze sítě, aby správně detekovala konkrétní obec.
*   **Prioritní řazení**: Úkoly jsou v databázi řazeny pomocí `CASE` statementu, aby nejdůležitější úkoly byly vždy nahoře.
*   **Automatické čištění**: Koš automaticky promazává úkoly starší než 30 dní.
*   **Reaktivní vyhledávání**: Filtrování úkolů probíhá v reálném čase přímo nad databázovým streamem.
*   **Moderní UI**: Použití Material Design 3, animovaných změn velikosti karet a intuitivní navigace.
