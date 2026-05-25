# Robocode - pierwszy robot

Ten projekt zawiera dwa roboty dla klasycznego Robocode:

- `org.example.MyBot` - bardzo prosty robot do pierwszego uruchomienia.
- `mybots.AdaptiveBot` - robot sledzacy cel radarem, celujacy niezaleznie
  od ruchu i zmieniajacy kierunek uniku po otrzymaniu trafienia.

Robocode zwykle nie "trenuje" robota automatycznie. Najpierw pisze sie
strategie w Javie, odpala wiele bitew i poprawia parametry. Prawdziwy trening
(np. optymalizacja parametrow albo reinforcement learning) mozna dobudowac
pozniej jako program odpalajacy serie bitew bez GUI.

## Co jest potrzebne

- JDK 25 skonfigurowane w IntelliJ jako `temurin-25`.
- Klasyczny Robocode `1.10.1`, zgodny z zaleznoscia w `pom.xml`.

Aktualne wydanie klasycznego Robocode:
[robocode-1.10.1-setup.jar](https://sourceforge.net/projects/robocode/files/robocode/1.10.1/robocode-1.10.1-setup.jar/download).

## Pierwsza walka w GUI

1. Pobierz i uruchom instalator Robocode `1.10.1`.
2. W IntelliJ wybierz `Build > Build Project`, aby skompilowac roboty.
3. Uruchom Robocode i wejdz w `Options > Preferences > Development Options`.
4. Dodaj katalog klas projektu, zazwyczaj:
   `E:\JB_Projects\Robocode\target\classes`
   albo katalog `out` wskazywany przez konfiguracje IntelliJ.
5. Wybierz `Battle > New`, dodaj `mybots.AdaptiveBot*` oraz jednego z
   robotow `sample`, np. `sample.SpinBot`, i rozpocznij walke.

Po zmianie kodu ponownie zbuduj projekt i uruchom nowa bitwe. Najlatwiej
eksperymentowac ze stalymi w `AdaptiveBot`, np. `MOVE_DISTANCE`, sila strzalu
oraz kat jazdy wzgledem przeciwnika.

W tym katalogu roboczym silnik jest juz zainstalowany lokalnie w
`target\robocode-home`, a skompilowane klasy sa w jego katalogu `robots`.
Gotowa konfiguracja starcia znajduje sie w
`battles\adaptive-vs-spinbot.battle`.
Gwiazdka przy `mybots.AdaptiveBot*` w tym pliku oznacza lokalna wersje
deweloperska robota.

Uruchomienie tej gotowej walki z terminala PowerShell:

```powershell
& 'C:\Users\Piotr\.jdks\temurin-25.0.3\bin\java.exe' `
  -cp 'target\robocode-home\libs\*' `
  '--add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED' `
  '--add-opens=java.base/java.lang.reflect=ALL-UNNAMED' `
  '--add-opens=java.desktop/javax.swing.text=ALL-UNNAMED' `
  '--add-opens=java.desktop/sun.awt=ALL-UNNAMED' `
  robocode.Robocode `
  -cwd 'target\robocode-home' `
  -battle '..\..\battles\adaptive-vs-spinbot.battle' -nosound
```

Dodanie `-nodisplay -results '..\results.txt'` odpala te same rundy bez
animacji i zapisuje wynik, co jest przydatne przy pozniejszym treningu.

## Nastepny krok: trening

Sensowna sciezka treningu jest stopniowa:

1. Uruchamianie turnieju wielu walk bez animacji.
2. Zapis wyniku dla zestawu parametrow ruchu i strzalu.
3. Przeszukiwanie parametrow i zachowanie wariantu z najlepszym wynikiem.
4. Dopiero pozniej ewentualne Q-learning lub inny model uczacy sie.
