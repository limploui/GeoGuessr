package com.example.geoguessr.info_docs

/**
 * Final
 *
 * Am Anfang stand die Suche nach einem halbwegs seriösen Street-View-Anbieter – und das war überraschend zäh.
 * Google Street View fiel wegen der Paywall sofort raus, also habe ich Alternativen sondiert und mich schließlich
 * für Mapillary entschieden. Von dort aus begann der lange Weg zur stabilen Verbindung zwischen App und Dienst.
 * Ich habe mich durch Dokumentationen gearbeitet, Beispiele verglichen und die typischen Stolpersteine gesammelt.
 *
 * Die Anbindung selbst war eine eigene Etappe: erst OAuth-Pfad testen, dann auf Token-basierten Zugriff umstellen,
 * weil das für die App robuster war. Parallel lief permanent Logcat mit, um Header, Statuscodes und Antwortzeiten zu prüfen.
 * Mit gezielten Logs in Kotlin und im Browser-Konsolenlog der WebView ließ sich das entwirren.
 * Der Moment, in dem die erste gültige Antwort ein Bild lieferte, war der eigentliche Durchbruch.
 *
 * Im nächsten Schritt musste aus „ein Bild laden“ ein echtes Pano-Erlebnis werden. Dafür habe ich ein schlankes HTML gebaut
 * (wie in dem Mapillary Docs empfohlen), MapillaryJS eingebunden und das Ganze im WebView gerendert. Die App ruft per
 * JavaScript-Bridge init(token, imageId) auf, der Viewer übernimmt und navigiert zum jeweiligen Panorama. Das klang simpel,
 * war aber wieder Fleißarbeit: Events, Ready-Zustände und Bildwechsel korrekt handlen, bis keine schwarzen Frames oder Hänger mehr auftauchten.
 * Mit zusätzlichem Logging und einem kleinen Watchdog war der Viewer schließlich zuverlässig.
 *
 * Als das Fundament stand, ging es um die Oberfläche. Ich habe das Interface in Canva als minimalistische, s
 * elbsterklärende Abfolge entworfen und anschließend in Compose nachgebaut. Start, Setup, Spiel, Ergebnis und Ende sind bewusst klar getrennt,
 * damit der Flow ohne Erklärung funktioniert. Der Ladebildschirm zeigt eine Animation, bis das erste sichtbare Frame vom Viewer bestätigt ist.
 * Farbwahl, Abstände und Typografie sind schlicht gehalten, damit die Inhalte (Bild, Karte, Buttons) im Vordergrund stehen.
 * So bleibt die App auch auf kleineren Displays übersichtlich.
 *
 * Die Spiellogik ist bewusst transparent. Es gibt zwei Koordinaten – die echte Position des Panos und den Tipp der spielenden Person.
 * Aus der Distanz berechnet eine Kurve die Punkte, nahe Treffer werden stark belohnt, große Abweichungen deutlich bestraft.
 * Wichtig ist hier die Realität von Mapillary: Im Vergleich zu Google gibt es weniger Panoramen, je nach Region sehr ungleich verteilt.
 * Deshalb arbeitet die App mit kuratierten BBox-Regionen; innerhalb dieser Auswahl wird zufällig gewählt und bei Bedarf die Box schrittweise erweitert.
 * So bleibt das Spiel spielbar, ohne dass man ständig ins Leere läuft.
 *
 * Der Tipp-Modus ist als sanfte Einstiegshilfe gedacht, nicht als Abkürzung. Pro aufgedecktem Hinweis sinkt ein Multiplikator, das heißt:
 * Wer viele Tipps nutzt, muss am Ende sehr nah am Ziel sein, um trotzdem gut zu punkten. Dadurch lernen neue Spieler die Weltkarte nebenbei besser
 * kennen, ohne das Kernprinzip zu verwässern. Gleichzeitig bleibt der Modus fair gegenüber geübten Spielern, die ohne Hinweise auskommen.
 * In Summe fühlt sich das wie eine einstellbare „Assist-Stufe“ an, statt wie ein Cheatsystem.
 *
 * Zum Schluss folgte das Feintuning: States entkoppeln, damit Navigation und Laden nicht kollidieren, und kleine Details wie „∞“ bei deaktivierter
 * Zeit einbauen. Fehlerfälle wie leere Trefferlisten oder doppelte Bilder werden abgefangen, damit der Flow nicht stockt. Die Kombination aus
 * WebView-Viewer und Compose-UI ist jetzt stabil, schnell genug und gut nachvollziehbar. Damit ist der Projektablauf im Grunde linear: Anbieter
 * wählen, stabile API schaffen, Pano-Viewer einbetten, UI designen, Spielregeln umsetzen und alles zusammenpolieren. Genau diese Reihenfolge hat
 * das Ganze am Ende rund gemacht.
 *
 *
 *
 *  */