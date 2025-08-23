// server/index.js
// OAuth wird nicht mehr genutzt. Dieser Server ist NICHT erforderlich.
// Das File bleibt leer/minimal, damit dein Projekt weiterstartet, falls du
// ein Node-Server-Script referenzierst.

const express = require('express');
const app = express();

app.get('/', (_, res) => res.send('OK – Kein OAuth nötig.'));

const port = process.env.PORT || 3000;
app.listen(port, () => console.log(`Optional server running on :${port}`));
