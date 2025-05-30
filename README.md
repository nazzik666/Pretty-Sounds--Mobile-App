# PrettySound

Чарівний додаток з чудовими звуками для керування плейлистами та улюбленими аудіозаписами.

## ✨ Основні можливості

* **Керування плейлистами:**
    * Створення нових плейлистів.
    * Перейменування існуючих плейлистів.
    * Видалення плейлистів.
    * Додавання звуків до певного плейлиста.
    * Видалення звуків із плейлиста.
* **Головний екран:**
    * Відображення списку плейлистів разом із їхніми звуками.
    * Можливість розгортати/згортати плейлисти для перегляду вмісту.
    * Позначення плейлистів та звуків як улюблених (іконка зірочки).
* **Екран "Улюблене":**
    * Відображення списку плейлистів та звуків, позначених як улюблені.
    * Кастомізований фон:
        * Реалізовано окремий темний фон (темно-синій градієнт/колір) при активації темної теми на пристрої.
* **Збереження даних:**
    * Використовується локальна база даних Room для збереження плейлистів, звуків та їх статусів (включаючи статус "улюблене").
* **Навігація:**
    * Реалізовано переходи між головним екраном, екраном улюбленого та екраном деталей плейлиста.

## 🛠️ Технології та архітектура

* **Мова:** Kotlin
* **Архітектура:** MVVM (Model-View-ViewModel) з використанням Android Architecture Components.
* **База даних:** Room Persistence Library.
* **Асинхронність:** Kotlin Coroutines (ймовірно, використовується у ViewModel та Repository).
* **Інтерфейс користувача:**
    * XML для створення макетів (Layouts).
    * RecyclerView для відображення списків (з використанням `ListAdapter` та `DiffUtil` для ефективності).
    * Material Components для стилізації елементів.
    * Drawable Resources (зокрема `<shape>` для градієнтів та фонів).
    * Підтримка темної теми через кваліфікатор ресурсів `-night`.
* **Залежності:** AndroidX Libraries (AppCompat, ConstraintLayout, RecyclerView, ViewModel KTX, Room KTX, Navigation Component).

## 🚀 Встановлення та запуск

1.  Клонуйте репозиторій:
    ```bash
    git clone [https://github.com/nazzik666/Pretty-Sounds--Mobile-App.git](https://github.com/nazzik666/Pretty-Sounds--Mobile-App.git)
    ```
2.  Відкрийте проєкт у Android Studio.
3.  Дочекайтеся синхронізації Gradle та завантаження залежностей.
4.  Запустіть додаток на емуляторі або фізичному пристрої Android.