package com.example.geoguessr.info_docs

/**
 * Final
 *
 * Einstieg
 * Du wählst auf dem Startscreen zwischen „Normales Spiel“ und „Hinweis Spiel“.
 * Ziel jeder Runde: Panorama ansehen, Ort auf der Karte tippen, Punkte kassieren.
 *
 * Setup
 * Im Setup stellst du Rundenzahl und Zeitlimit ein; per rotem „x“ schaltest du das Limit auf ∞.
 * „Los geht’s“ unten auf der Weltkarte startet die Partie.
 *
 * Tabs im Spiel
 * Oben steuerst du die Ansichten: „Streetview“, optional „Tipps“, und „Karte“.
 * Darunter zeigt eine Leiste die Restzeit oder „∞“.
 *
 * Streetview
 * Hier drehst du dich im Panorama und sammelst Hinweise aus Umgebung, Schildern und Architektur.
 * Beim Laden erscheint eine kurze Animation und verschwindet, sobald das Bild steht.
 *
 * Tipps (nur Hinweis-Modus)
 * Du kannst bis zu fünf Hinweise aufdecken, die grobe Region und Koordinatenlage andeuten.
 * Jeder Tipp senkt den Punktmultiplikator — sparsam nutzen lohnt sich.
 *
 * Karte
 * Tippe auf die Karte, um deinen Guess zu setzen und bei Bedarf zu verschieben.
 * Nach der Wertung siehst du deinen Guess blau und den wahren Ort rot.
 *
 * Zeit & Bestätigen
 * Mit Zeitlimit läuft ein Countdown; ohne Limit bleibt „∞“.
 * „Bestätigen“ wertet deinen Tipp sofort und stoppt die Runde.
 *
 * Punkte & Ergebnis
 * Die Punkte basieren auf der Distanz zwischen Tipp und wahrer Position (Haversine), mit Deckel pro Runde.
 * Im Hinweis-Modus wirkt zusätzlich ein Multiplikator, der mit jedem Tipp sinkt.
 *
 * Rundenende & Finale
 * Nach jeder Runde folgt ein kurzer Ergebnisschirm, dann geht’s weiter.
 * Am Ende siehst du Gesamtscore, Gesamtentfernung und alle Rundendetails — „Neues Spiel“ setzt alles zurück.
 *
 *
 *
 * */
