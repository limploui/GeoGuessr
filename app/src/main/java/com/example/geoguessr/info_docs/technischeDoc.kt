/**
 *
 * package com.example.geoguessr.info_docs
 *
 * Technische Doku – GeoGuezzr (Android, Compose, Mapillary)
 * Die App ist eine kleine, eigenständige Geoguessr-Variante für Android, gebaut mit Jetpack Compose und einer WebView,
 * die den Mapillary-Viewer rendert. Zentral ist ein sauberer Datenfluss. Die MainActivity initialisiert Regionen und Hints,
 * die ViewModels holen ein Bild und halten Spielzustand, die Screens visualisieren und reagieren auf Nutzerinteraktionen.
 * Das Bild selbst kommt aus der Mapillary Graph API und wird über Retrofit in die App geholt, während die 360°-Ansicht mit
 * MapillaryJS im WebView läuft. Für die Karte der Tippabgabe nutzt die App osmdroid, wodurch Marker und Bounding Box ohne
 * Google-Abhängigkeiten funktionieren. Die Punkteberechnung und Zeitlogik laufen lokal in Kotlin, damit die Runden
 * deterministisch und offline berechenbar bleiben.
 *
 *
 * Projektstruktur und Verknüpfungen
 * Der Code ist nach Aufgaben getrennt:
 * Unter data/ liegt der gesamte API-Zugriff (MapillaryApi, MapillaryClient) und der WebView-Composable für den Viewer.
 * Die game/-Schicht hält den Spielzustand (GameViewModel, RoundResult, GameMode) und sammelt Rundenpunkte und Distanzen.
 * In ui/ befinden sich Screens, Navigation, Map-Composable und Theme; die Screens greifen nur über ViewModels auf Daten zu.
 * Die Hilfsfunktionen wie Haversine, Scoring und Hint-Multiplikator liegen in util/ als GeoUtils, damit die Logik wiederverwendbar und testbar bleibt.
 * App-weite Initialisierung von osmdroid (User-Agent, Cache) steckt in GeoGuessrApp, damit Karten sofort korrekt laden.
 *
 *
 * Daten- und Netzwerkschicht (MapillaryApi & MapillaryClient)
 * MapillaryApi beschreibt exakt einen Endpunkt GET images, inklusive der Felder id, computed_geometry, computed_compass_angle,
 * thumb_1024_url, is_pano. Das DTO ist minimal gehalten, damit nur das ankommt, was die UI wirklich braucht, und die Antwort sauber
 * serialisiert werden kann. MapillaryClient kapselt den kompletten Zugriff. Header mit OAuth-Token, Timeouts, Logging und sichere
 * Filterung auf gültige Einträge. Für zufällige Inhalte gibt es getRandomPano (strict) und getRandomImage (flexibel), jeweils mit
 * Nachbearbeitung, um kaputte Items zu filtern. Die BBox wird bei Bedarf schrittweise vergrößert, um die Trefferwahrscheinlichkeit
 * zu erhöhen, ohne die Region komplett zu sprengen.
 *
 *
 * WebViewer und HTML (Streetview-Ebene)
 * Der MapillaryViewer ist ein Compose-Wrapper um eine WebView, die mly_v2.html aus den Assets lädt. Nach onPageFinished ruft Kotlin
 * via evaluateJavascript window.init(token, imageId) auf, wodurch der JS-Viewer erzeugt und auf die erste Bild-ID bewegt wird.
 * Ein kleiner JS-Bridge-Hook (addJavascriptInterface) sendet ein Signal zurück, wenn das erste sichtbare Frame geladen ist, damit ein
 * etwaiges Lade-Overlay sauber ausgeblendet werden kann. Die HTML-Seite bindet mapillary.js ein, verwaltet einen globalen Viewer-State
 * und arbeitet mit einer Queue, falls Bildwechsel eintreffen, bevor der Viewer ready ist. Ein schlankes Error-Overlay in HTML hilft bei
 * Analyse, sollten Bibliothek oder Netzwerk einmal nicht mitspielen.
 *
 *
 * Spielzustand und Scoring (GameViewModel & GeoUtils)
 * Das GameViewModel ist der Single-Source-of-Truth für Spielparameter wie Modus, Rundenzahl, aktuelle Runde, Zeitlimit und die gesammelten
 * Ergebnisse. Es nimmt nach jeder Runde ein RoundResult entgegen, addiert Punkte und Distanz und rückt die Runde vor; der Abschluss
 * entscheidet über Navigation zum Endbildschirm. Die Distanz wird über GeoUtils.haversineKm berechnet und in Punkte übersetzt, standardmäßig
 * mit einer 0..5000-Kurve, die große Abweichungen stärker abstraft. Im Hinweis-Modus wird mit einem Multiplikator gearbeitet, der mit
 * jedem verwendeten Tipp sinkt, damit „Blind Glück“ nicht besser ist als kluges Lesen von Hints. Die Zeitlogik kennt auch einen ∞-Modus
 * (Int.MAX_VALUE), bei dem kein Countdown läuft und in der UI ein Unendlichkeitszeichen gezeigt wird.
 *
 *
 * Bildauswahl, Regionen und Hints (ViewModeltwo)
 * ViewModeltwo verwaltet die Liste der Regionen (BBox plus Name), die zuletzt verwendete Region, die aktuelle BBox und die Hints.
 * Beim Laden einer Runde wird bewusst nicht die letzte Region wiederverwendet und innerhalb einer Region bis zu fünfmal expandiert,
 * bevor zur nächsten gewechselt wird. Eine Request-Sequenznummer schützt vor „Race Conditions“, falls späte Antworten eintreffen,
 * nachdem der Nutzer schon weiter ist. Um Wiederholungen zu vermeiden, wird die letzte Bild-ID gemerkt und ein paar Mal „weggewürfelt“,
 * falls dieselbe ID erneut auftaucht. Gibt es keine Custom-Hints, erzeugt das ViewModel Fallback-Hinweise aus BBox-Lage, Hemisphäre und
 * groben Gradangaben.
 *
 *
 * Navigation und Einstiegslogik (MainActivity & Routes)
 * Die MainActivity setzt zu Beginn die Regionen und optional Hints, erstellt die ViewModels und startet das Compose-UI mit NavHost.
 * Die Routen sind als Route-Sealed-Class definiert, wodurch die Navigation typsicher und lesbar bleibt. Start → Setup wählt Modus und Zeit,
 * Setup → Game lädt ein erstes Bild und startet die Runde, Game → Result zeigt das Rundenergebnis, und schließlich sammelt End die
 * Gesamtübersicht. Während des Spiels beobachtet die MainActivity die LiveData aus ViewModeltwo: Bild, BBox und Hints werden an den
 * GameScreen gereicht. Wenn kein Bild vorliegt, zeigt eine Loading-UI eine Grafik/Animation, bis der WebViewer das erste Frame meldet.
 *
 *
 * Bildschirme und Interaktion (Start, Setup, Game, Result, End)
 * Der StartScreen bietet zwei große, zugängliche Modus-Karten und eine zentrierte Weltkarte, die sich an kleinere Displays anpasst.
 * Der SetupScreen richtet Runden und Zeitlimit ein, mit einem klaren ∞-Schalter und einem „Los geht’s“-Button, der am Weltbild verankert
 * ist. Der GameScreen zeigt oben Tabs für Streetview, Tipps und Karte, eine kompakte Zeitzeile und unten die Aktion „Bestätigen“ bzw.
 * „Weiter“. Die Tippeingabe erfolgt auf der osmdroid-Karte, die Marker für Guess und „Truth“ (Mittelpunkt bzw. echte Koordinate, wenn
 * vorhanden) setzt und Gesten sauber abfängt. ResultScreen und EndScreen sind schlank, zeigen Runden- bzw. Gesamtbilanz, und der EndScreen
 * kombiniert Liste plus Weltbild mit einem klaren „Neues Spiel“.
 *
 *
 * Karte und Touch (OsmdroidMap)
 * Die OsmdroidMap kapselt MapView-Erzeugung, TileSource, Multitouch und Event-Weitergabe, damit Eltern-Layouts Gesten nicht „schlucken“.
 * Ein MapEventsOverlay verarbeitet Taps zu Guess-Koordinaten und schaltet Auto-Fit ab, sobald der Nutzer interagiert. Beim Wechsel der BBox
 * wird einmalig in die Region gefittet und danach der Zustand respektiert, damit es keine „Zuckler“ gibt. Guess- und Truth-Marker werden
 * idempotent aktualisiert, um unnötige Invalidate-Schleifen zu vermeiden. Die gewählten Grenzen lassen sich leicht schärfen, sollten
 * bestimmte Zoomstufen nötig sein.
 *
 *
 * Theme, Typografie und App-Start (Theme, GeoGuessrApp)
 * Das Theme folgt Material 3, nutzt dynamische Farben ab Android 12, und überschreibt nur, was für Lesbarkeit und Branding nötig ist.
 * Typografie bleibt bewusst generisch, damit UI-Texte im Fokus stehen und die Lesbarkeit auf kleinen Displays hoch bleibt. GeoGuessrApp
 * setzt den osmdroid-User-Agent, legt Cache-Verzeichnisse in den App-Cache und lädt die Konfiguration aus den SharedPreferences. Dadurch
 * verhalten sich Karten zuverlässig, ohne dass man erst Laufzeitfehler der Tile-Server austesten muss. Diese Initialisierung passiert sehr
 * früh, noch vor dem Rendern der ersten Compose-Hierarchy.
 *
 *
 * Lebenszyklus, Performance und Robustheit
 * Die WebView wird in MapillaryViewer sauber erstellt und beim „Release“ zurückgesetzt, damit keine Leaks entstehen. Das HTML nutzt ein
 * Watchdog-Timeout, falls load ausbleibt, und führt dann ein vorsichtiges moveTo aus, um Hänger zu vermeiden. Die Netzwerk-Schicht arbeitet
 * mit Timeouts, Retry auf Verbindungsfehler und Logging, was das Debuggen im Feld deutlich erleichtert. LiveData-Beobachtung in der
 * MainActivity ist bewusst minimal und übergibt nur das, was der Screen wirklich braucht. Die UI reagiert auf Ladezustand, ∞-Zeit und
 * Hint-Freigabe deterministisch, wodurch Edge-Cases wie Timeout ohne Guess korrekt behandelt werden.
 *
 *
 * Datenfluss im Überblick
 * Beim Start setzt die Activity Regionen und Hints, erzeugt ViewModels und öffnet den StartScreen.
 * Nach der Setup-Wahl ruft die Activity viewModelTwo.loadRandomImageClearing() auf, worauf das ViewModel ein Bild beschafft, B
 * Box und Hints setzt und sie als LiveData anbietet. Der GameScreen bekommt das Bild (ID), die BBox, Hints und die Zeitparameter und
 * zeigt Streetview plus Karte; der Nutzer tippt, bestätigt und löst die Punkteberechnung aus. Das Resultat der Runde geht als RoundResult
 * zurück ans GameViewModel, das Punkte und Distanzen aufsummiert und entscheidet, ob weitergespielt oder beendet wird.
 * Am Ende zeigt der EndScreen die Gesamtbilanz und bietet den Neustart, der den Spielzustand im ViewModel zurücksetzt und den Flow wieder
 * von vorn beginnen lässt.
 *
 * */