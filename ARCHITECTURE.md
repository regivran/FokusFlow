# Architektura Aplikace FokusFlow

Tento dokument vysvětluje strukturu této Android aplikace, její komponenty a porovnává ji s architekturou známou z frameworku Spring MVC.

### **Základní Architektura: Analogie se Spring MVC**

Ve Spring MVC máte typicky následující vrstvy:
*   **`@Controller`**: Přijímá HTTP požadavky, volá `Service` a vrací `View`.
*   **`@Service`**: Obsahuje business logiku.
*   **`@Repository`**: Komunikuje s databází.
*   **Model (Entity)**: Datové třídy, které reprezentují objekty (např. `User`, `Product`).
*   **View (Thymeleaf, JSP)**: Šablony, které se vyplní daty a zobrazí jako HTML.

Moderní Android aplikace má velmi podobnou (i když ne identickou) architekturu, která odděluje zodpovědnosti:

*   **`Activity` / `Composable`**: Vrstva **View**. Zobrazuje data a zachytává uživatelské interakce (kliky).
*   **`ViewModel`**: Podobný **`@Controller`** a částečně i **`@Service`**. Připravuje data pro UI a reaguje na jeho události. Neobsahuje ale přímo kód pro zobrazování.
*   **`Repository`**: Stejné jako ve Springu. Zprostředkovává přístup k datům (z databáze nebo sítě).
*   **Model (Data Class)**: Stejné jako ve Springu. Třídy, které drží data.
*   **Data Source (Room/Retrofit)**: Konkrétní implementace přístupu k datům. `Room` je pro lokální databázi (jako JPA/Hibernate) a `Retrofit` pro síťové API volání.

**Důležité:** Naše aplikace `FokusFlow` je zatím velmi jednoduchá a **postrádá vrstvy `ViewModel` a `Repository`**. Vše je zjednodušené do jednoho souboru.

---

### **Struktura Naší Aplikace (`FokusFlow`)**

Pojďme se podívat na konkrétní soubory v našem projektu:

#### **1. `app/src.main/java/.../fokusflow/MainActivity.kt`**

Toto je v naší aplikaci nejdůležitější soubor. Dělá toho momentálně příliš mnoho.

*   **Třída `MainActivity`**:
    *   `Activity` je základní komponenta Androidu, která reprezentuje jednu obrazovku. Je to vstupní bod pro UI. Můžeme si ji představit jako hodně zjednodušený **`@Controller`**, který má za úkol "nastartovat" zobrazení.
    *   **Metoda `onCreate()`**: Zavolá se, když se obrazovka vytváří. Zde se nastavuje obsah obrazovky pomocí `setContent`.
*   **Funkce `@Composable` (`TaskList`, `TaskItem`)**:
    *   Toto je **View vrstva**, psaná v Jetpack Compose. Je to deklarativní způsob, jak popsat, jak má UI vypadat.
    *   **Analogie se Springem**: Místo psaní HTML v `Thymeleaf` šablonách skládáte UI z těchto znovupoužitelných funkcí (`Composable`) přímo v Kotlinu.
    *   **`TaskList(tasks: List<Task>)`**: Komponenta, která vezme seznam úkolů a zobrazí je pod sebou ve scrollovatelném seznamu.
    *   **`TaskItem(task: Task, ...)`**: Komponenta pro zobrazení jednoho řádku – jednoho úkolu. Zobrazuje název, popis a puntík s barvou priority.
*   **`val testTasks = listOf(...)`**:
    *   Toto je **největší architektonický prohřešek**. Data (naše úkoly) jsou napevno zapsaná přímo v `MainActivity`. `Activity` by data vlastnit neměla, měla by je pouze zobrazovat. Ve Springu by to bylo, jako byste měli seznam produktů napevno v metodě controlleru místo v databázi.

#### **2. `app/src.main/java/.../fokusflow/Task.kt`**

Toto je naše **Modelová vrstva**.

*   **`data class Task(...)`**:
    *   Jednoduchá třída, která pouze drží data. Je to ekvivalent vaší **Entity** nebo **DTO** třídy ve Springu.
    *   Klíčové slovo `data` automaticky generuje metody jako `equals()`, `hashCode()`, `toString()`, atd.
*   **`enum class Priority(...)`**:
    *   Výčtový typ, který je také součástí modelu. Definuje omezenou sadu hodnot pro prioritu a zároveň ke každé přiřazuje barvu (`val color: Color`).

#### **3. `app/src.main/ui/theme/` (složka)**

Tato složka obsahuje soubory (`Theme.kt`, `Color.kt`, `Type.kt`), které definují vizuální styl aplikace.

*   **Analogie**: Je to v podstatě jako vaše **CSS soubory**. Definuje se zde paleta barev, velikosti a styly písma a celkové téma, které se pak aplikuje na celé UI.

---

### **Shrnutí v Tabulce**

| Spring MVC Komponenta | Android (Ideální Architektura) | **Naše Současná Aplikace** | Účel |
| :--- | :--- | :--- | :--- |
| `View` (Thymeleaf/JSP) | `@Composable` funkce | `TaskList`, `TaskItem` (v `MainActivity`) | Vykreslení UI |
| `@Controller` / `@Service`| `ViewModel` | **Chybí** (roli supluje `MainActivity`) | Příprava dat, business logika |
| `@Repository` | `Repository` | **Chybí** | Zprostředkování přístupu k datům |
| `Model` / `Entity` | `data class` | `Task.kt` | Reprezentace dat |
| Vstupní bod (request) | `Activity` / `Fragment` | `MainActivity` | Vstupní bod pro obrazovku |
